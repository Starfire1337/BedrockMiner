package com.starfire1337.bedrockminer;

import com.starfire1337.bedrockminer.compat.AACFastBreakListener;
import com.starfire1337.bedrockminer.compat.NCPFastBreakHook;
import com.starfire1337.metrics.Metrics;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BedrockMiner extends JavaPlugin {

    private static Plugin plugin;
    private PacketListener packetListener;
    private Metrics metrics;

    private NCPFastBreakHook ncpFastBreakHook;

    @Override
    public void onEnable() {
        plugin = this;
        metrics = new Metrics(this);

        saveDefaultConfig();

        if(Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus"))
            ncpFastBreakHook = new NCPFastBreakHook();

        if(Bukkit.getPluginManager().isPluginEnabled("AAC"))
            getServer().getPluginManager().registerEvents(new AACFastBreakListener(), this);

        packetListener = new PacketListener();
        packetListener.addListener();
    }

    @Override
    public void onDisable() {

        metrics.shutdown();
        packetListener.disable();

        ncpFastBreakHook = null;
        metrics = null;
        packetListener = null;
        plugin = null;
    }

    public static Plugin getInstance() {
        return plugin;
    }

}
