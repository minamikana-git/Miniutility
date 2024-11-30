package org.hotamachisubaru.miniutility.GUI;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Utility {
    public static void openUtilityGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "便利箱");


        // エンダーチェスト機能
        ItemStack enderChestIcon = new ItemStack(Material.ENDER_CHEST);
        ItemMeta enderMeta = enderChestIcon.getItemMeta();
        enderMeta.setDisplayName("どこでもエンダーチェスト");
        enderChestIcon.setItemMeta(enderMeta);
        gui.setItem(2, enderChestIcon);

        // 作業台機能
        ItemStack craftingTableIcon = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta craftMeta = craftingTableIcon.getItemMeta();
        craftMeta.setDisplayName("どこでも作業台");
        craftingTableIcon.setItemMeta(craftMeta);
        gui.setItem(4, craftingTableIcon);

        // ゴミ箱機能
        ItemStack trashBoxIcon = new ItemStack(Material.DROPPER);
        ItemMeta trashMeta = trashBoxIcon.getItemMeta();
        trashMeta.setDisplayName("ごみ箱");
        trashBoxIcon.setItemMeta(trashMeta);
        gui.setItem(6, trashBoxIcon);


        player.openInventory(gui);
    }
}
