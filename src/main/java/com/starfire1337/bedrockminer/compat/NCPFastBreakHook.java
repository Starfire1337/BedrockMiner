package com.starfire1337.bedrockminer.compat;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo;
import fr.neatmonster.nocheatplus.hooks.NCPHook;
import fr.neatmonster.nocheatplus.hooks.NCPHookManager;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Set;

public class NCPFastBreakHook implements NCPHook {

    public NCPFastBreakHook() {
        NCPHookManager.addHook(CheckType.BLOCKBREAK_FASTBREAK, this);
    }

    public String getHookName() {
        return "BedrockMiner NCPCompat";
    }

    public String getHookVersion() {
        return "1.0.0";
    }

    public boolean onCheckFailure(CheckType checkType, Player player, IViolationInfo info) {
        if(!checkType.equals(CheckType.BLOCKBREAK_FASTBREAK))
            return false;
        Block block = player.getTargetBlock((Set<Material>) null, 5);
        if(block.getType().equals(Material.BEDROCK))
            return true;
        return false;
    }

}
