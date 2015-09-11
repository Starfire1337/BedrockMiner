package com.starfire1337.bedrockminer;

import com.starfire1337.metrics.Metrics;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BedrockMiner extends JavaPlugin {

    private static Plugin plugin;
    private PacketListener packetListener;
    private Metrics metrics;

    @Override
    public void onEnable() {
        plugin = this;
        metrics = new Metrics(this);

        saveDefaultConfig();

        packetListener = new PacketListener();
        packetListener.addListener();
    }

    @Override
    public void onDisable() {

        metrics.shutdown();
        packetListener.disable();

        plugin = null;
        metrics = null;
        packetListener = null;
    }

    public static Plugin getInstance() {
        return plugin;
    }

}
