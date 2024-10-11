package org.hotamachisubaru.miniutility.GUI

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object UtilityGUI {
    fun openUtilityGUI(player: Player) {
        val gui = Bukkit.createInventory(null, 9, "便利箱")



        val nameColorIcon = ItemStack(Material.GREEN_DYE)
        val nameColorMeta = nameColorIcon.itemMeta
        nameColorMeta.setDisplayName("プレイヤーに色を付ける")
        nameColorIcon.setItemMeta(nameColorMeta)
        gui.setItem(1, nameColorIcon)

        // EnderchestOpener icon
        val enderChestIcon = ItemStack(Material.ENDER_CHEST)
        val enderMeta = enderChestIcon.itemMeta
        enderMeta.setDisplayName("どこでもエンダーチェスト")
        enderChestIcon.setItemMeta(enderMeta)
        gui.setItem(2, enderChestIcon)

        // InstantCrafter icon
        val craftingTableIcon = ItemStack(Material.CRAFTING_TABLE)
        val craftMeta = craftingTableIcon.itemMeta
        craftMeta.setDisplayName("どこでも作業台")
        craftingTableIcon.setItemMeta(craftMeta)
        gui.setItem(4, craftingTableIcon)

        // TrashBox icon
        val trashBoxIcon = ItemStack(Material.DROPPER)
        val trashMeta = trashBoxIcon.itemMeta
        trashMeta.setDisplayName("ごみ箱")
        trashBoxIcon.setItemMeta(trashMeta)
        gui.setItem(6, trashBoxIcon)

        // Nickname icon (本と筆ペン)
        val nicknameIcon = ItemStack(Material.WRITABLE_BOOK)
        val nicknameMeta = nicknameIcon.itemMeta
        nicknameMeta.setDisplayName("ニックネーム変更")
        nicknameIcon.setItemMeta(nicknameMeta)
        gui.setItem(8, nicknameIcon) // ニックネームアイコンを8番スロットに配置

        player.openInventory(gui)
    }
}
