package com.starfire1337.bedrockminer;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Tools {

    private static boolean isTool(Material material) {
        String mat = material.name();
        if(mat.endsWith("AXE") || mat.endsWith("_SPADE") || mat.endsWith("_SWORD") || mat.endsWith("_HOE"))
            return true;
        return false;
    }

    public static ItemStack addDamage(ItemStack itemStack) {
        if(!isTool(itemStack.getType()))
            return itemStack;
        itemStack.setDurability((short) (itemStack.getDurability() + 1));

        String mat = itemStack.getType().name();
        int dmg = itemStack.getDurability();
        if((mat.startsWith("WOOD_") && dmg >= 60) || (mat.startsWith("STONE_") && dmg >= 132) || (mat.startsWith("IRON_") && dmg >= 251) || (mat.startsWith("GOLD_") && dmg >= 33) || (mat.startsWith("DIAMOND_") && dmg >= 1562)) {
            itemStack.setType(Material.AIR);
            itemStack.setDurability((short)0);
        }

        return itemStack;
    }

}
