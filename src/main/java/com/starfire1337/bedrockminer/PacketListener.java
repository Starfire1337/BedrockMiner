package com.starfire1337.bedrockminer;

import com.comphenix.tinyprotocol.Reflection;
import com.comphenix.tinyprotocol.TinyProtocol;

import io.netty.channel.Channel;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PacketListener {

    private TinyProtocol tinyProtocol;

    private void sendPacket(Player p, Object packet) throws Exception {
        Object player = p.getClass().getMethod("getHandle").invoke(p);
        Object playerConnection = player.getClass().getField("playerConnection").get(player);
        playerConnection.getClass().getMethod("sendPacket", Reflection.getMinecraftClass("Packet")).invoke(playerConnection, packet);
    }

    private void sendBlockBreakEffect(Player p, Object blockPosition, int stage) throws Exception {
        Class<?> blockPositionClass = Reflection.getMinecraftClass("BlockPosition");
        Class<?> packetPlayOutBlockBreakAnimationClass = Reflection.getMinecraftClass("PacketPlayOutBlockBreakAnimation");
        Object packetPlayOutBlockBreakAnimation = packetPlayOutBlockBreakAnimationClass.getConstructor(int.class, blockPositionClass, int.class).newInstance(0, blockPosition, stage);

        this.sendPacket(p, packetPlayOutBlockBreakAnimation);
    }

    private void sendWorldEvent(Player p, Object blockPosition) throws Exception {
        Class<?> blockPositionClass = Reflection.getMinecraftClass("BlockPosition");
        Class<?> packetPlayOutWorldEventClass = Reflection.getMinecraftClass("PacketPlayOutWorldEvent");
        Object packetPlayOutWorldEvent = packetPlayOutWorldEventClass.getConstructor(int.class, blockPositionClass, int.class, boolean.class).newInstance(2001, blockPosition, 7, false);

        this.sendPacket(p, packetPlayOutWorldEvent);
    }

    public void addListener() {
        tinyProtocol = new TinyProtocol(BedrockMiner.getInstance()) {

            private ConcurrentHashMap<String, Block> mine = new ConcurrentHashMap<>();

            @Override
            public Object onPacketInAsync(final Player sender, Channel channel, final Object packet) {

                if(Reflection.getMinecraftClass("PacketPlayInBlockDig").isInstance(packet)) {

                    try {
                        final Object blockPosition = packet.getClass().getMethod("a").invoke(packet);
                        final Object enumPlayerDigType = packet.getClass().getMethod("c").invoke(packet);

                        BedrockMiner.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(BedrockMiner.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final Block block = sender.getWorld().getBlockAt((int) blockPosition.getClass().getMethod("getX").invoke(blockPosition), (int) blockPosition.getClass().getMethod("getY").invoke(blockPosition), (int) blockPosition.getClass().getMethod("getZ").invoke(blockPosition));
                                    if (block == null || block.getType() != Material.BEDROCK || block.getY() < BedrockMiner.getInstance().getConfig().getInt("minimum-height") || !sender.hasPermission("bedrockminer.mine") || sender.getGameMode() == GameMode.CREATIVE)
                                        return;
                                    List<Material> allowedMaterials = new ArrayList<>();
                                    for(String material : BedrockMiner.getInstance().getConfig().getStringList("allowed-tools")) {
                                        allowedMaterials.add(Material.getMaterial(material));
                                    }
                                    if(!allowedMaterials.contains(sender.getItemInHand().getType()) && !sender.hasPermission("bedrockminer.bypass"))
                                        return;

                                    Object[] enumConstants = Reflection.getMinecraftClass("PacketPlayInBlockDig").getDeclaredClasses()[0].getEnumConstants();
                                    if (enumConstants[0].equals(enumPlayerDigType)) {
                                        mine.put(sender.getName(), block);

                                        BedrockMiner.getInstance().getServer().getScheduler().runTaskAsynchronously(BedrockMiner.getInstance(), new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    int speed = BedrockMiner.getInstance().getConfig().getInt("mining-speed");
                                                    int enchantLevel = sender.getItemInHand().containsEnchantment(Enchantment.DIG_SPEED) ? sender.getItemInHand().getEnchantmentLevel(Enchantment.DIG_SPEED) : 0;
                                                    long sleepTime = BedrockMiner.getInstance().getConfig().getBoolean("efficiency") ? Math.round(speed / (1 + (0.3 * enchantLevel))) * 2 : (speed * 2);
                                                    if(sleepTime != 0) {
                                                        for (int i = 0; i < 100; i++) {
                                                            if (!mine.containsKey(sender.getName()) || !mine.get(sender.getName()).equals(block)) {
                                                                sendBlockBreakEffect(sender, blockPosition, -1);
                                                                break;
                                                            }
                                                            if (i % 10 == 0)
                                                                sendBlockBreakEffect(sender, blockPosition, i / 10);
                                                            Thread.sleep(sleepTime);
                                                            i++;
                                                        }
                                                    }
                                                    if (mine.containsKey(sender.getName())) {
                                                        if (!mine.get(sender.getName()).equals(block))
                                                            return;
                                                        mine.remove(sender.getName());
                                                        BedrockMiner.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(BedrockMiner.getInstance(), new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                block.getDrops().clear();
                                                                block.getDrops().add(new ItemStack(Material.BEDROCK));

                                                                BlockBreakEvent e = new BlockBreakEvent(block, sender);
                                                                BedrockMiner.getInstance().getServer().getPluginManager().callEvent(e);

                                                                if (e.isCancelled()) {
                                                                    try {
                                                                        sendBlockBreakEffect(sender, blockPosition, -1);
                                                                    } catch (Exception e1) {
                                                                        e1.printStackTrace();
                                                                    }
                                                                    return;
                                                                }
                                                                block.breakNaturally();
                                                                for (Player p : BedrockMiner.getInstance().getServer().getOnlinePlayers()) {
                                                                    try {
                                                                        sendWorldEvent(p, blockPosition);
                                                                        sendBlockBreakEffect(sender, blockPosition, -1);
                                                                    } catch (Exception e2) {
                                                                        e2.printStackTrace();
                                                                    }
                                                                    p.playEffect(block.getLocation(), Effect.STEP_SOUND, 7);
                                                                    sender.setItemInHand(Tools.addDamage(sender.getItemInHand()));
                                                                    sender.updateInventory();
                                                                }
                                                            }
                                                        });
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    } else if (enumConstants[1].equals(enumPlayerDigType)) {
                                        mine.remove(sender.getName());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return super.onPacketInAsync(sender, channel, packet);
            }
        };
    }

    public void disable() {
        this.tinyProtocol = null;
    }

}
