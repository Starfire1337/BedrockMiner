package com.starfire1337.bedrockminer.compat;

import me.konsolas.aac.api.HackType;
import me.konsolas.aac.api.PlayerViolationEvent;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Set;

public class AACFastBreakListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onAACFastBreak(PlayerViolationEvent e) {
        if(!e.getHackType().equals(HackType.FASTBREAK))
            return;
        Block block = e.getPlayer().getTargetBlock((Set<Material>) null, 5);
        if(block.getType().equals(Material.BEDROCK))
            e.setCancelled(true);
    }

}
