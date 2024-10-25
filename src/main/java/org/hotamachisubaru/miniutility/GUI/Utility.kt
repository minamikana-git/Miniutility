package org.hotamachisubaru.miniutility.GUI

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object Utility {
    fun openUtilityGUI(player: Player) {
        val gui = Bukkit.createInventory(null, 9, "便利箱")




        // 色付け機能
        val nameColorIcon = ItemStack(Material.GREEN_DYE)
        val nameColorMeta = nameColorIcon.itemMeta
        nameColorMeta.setDisplayName("プレイヤーに色を付ける")
        nameColorIcon.setItemMeta(nameColorMeta)
        gui.setItem(0, nameColorIcon)

        // エンダーチェスト機能
        val enderChestIcon = ItemStack(Material.ENDER_CHEST)
        val enderMeta = enderChestIcon.itemMeta
        enderMeta.setDisplayName("どこでもエンダーチェスト")
        enderChestIcon.setItemMeta(enderMeta)
        gui.setItem(2, enderChestIcon)

        // 作業台機能
        val craftingTableIcon = ItemStack(Material.CRAFTING_TABLE)
        val craftMeta = craftingTableIcon.itemMeta
        craftMeta.setDisplayName("どこでも作業台")
        craftingTableIcon.setItemMeta(craftMeta)
        gui.setItem(4, craftingTableIcon)

        // ゴミ箱機能
        val trashBoxIcon = ItemStack(Material.DROPPER)
        val trashMeta = trashBoxIcon.itemMeta
        trashMeta.setDisplayName("ごみ箱")
        trashBoxIcon.setItemMeta(trashMeta)
        gui.setItem(6, trashBoxIcon)

        // ニックネーム機能
        val nicknameIcon = ItemStack(Material.WRITABLE_BOOK)
        val nicknameMeta = nicknameIcon.itemMeta
        nicknameMeta.setDisplayName("ニックネーム変更")
        nicknameIcon.setItemMeta(nicknameMeta)
        gui.setItem(8, nicknameIcon) // ニックネームアイコンを8番スロットに配置

        player.openInventory(gui)
    }
}
