package org.hotamachisubaru.miniutility.Listener

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

class TrashBox : Listener {
    // プレイヤーごとのゴミ箱インベントリを保持
    private val lastTrashInventories: MutableMap<UUID, Inventory> = HashMap()

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        val clickedItem = event.currentItem

        // 便利箱のインベントリが開いている場合
        if (event.view.title == "便利箱") {
            event.isCancelled = true // 便利箱内の操作をキャンセル

            if (clickedItem == null || clickedItem.type == Material.AIR) {
                return
            }

            if (clickedItem.type == Material.DROPPER) {
                // ゴミ箱を開く処理
                val trashInventory = Bukkit.createInventory(player, 54, ChatColor.GREEN.toString() + "ゴミ箱")

                // 確認ボタンを右下に配置
                val confirmButton = ItemStack(Material.GREEN_STAINED_GLASS_PANE)
                val confirmMeta = confirmButton.itemMeta
                confirmMeta.setDisplayName(ChatColor.GREEN.toString() + "捨てる")
                confirmButton.setItemMeta(confirmMeta)
                trashInventory.setItem(53, confirmButton)

                // ゴミ箱のインベントリをプレイヤーごとに保存
                lastTrashInventories[player.uniqueId] = trashInventory

                player.openInventory(trashInventory)
            }
        }

        // ゴミ箱インベントリが開いている場合
        if (event.view.title == ChatColor.GREEN.toString() + "ゴミ箱") {
            if (event.rawSlot == 53 && clickedItem != null && clickedItem.type == Material.GREEN_STAINED_GLASS_PANE) {
                // 「捨てる」ボタンのクリックをキャンセル
                event.isCancelled = true

                // 確認画面を開く
                val confirmInventory =
                    Bukkit.createInventory(player, 27, ChatColor.RED.toString() + "本当に捨てますか？")

                // Yesボタン (緑色のガラス)
                val yesItem = ItemStack(Material.GREEN_STAINED_GLASS_PANE)
                val yesMeta = yesItem.itemMeta
                yesMeta.setDisplayName(ChatColor.GREEN.toString() + "はい")
                yesItem.setItemMeta(yesMeta)
                confirmInventory.setItem(11, yesItem)

                // Noボタン (赤色のガラス)
                val noItem = ItemStack(Material.RED_STAINED_GLASS_PANE)
                val noMeta = noItem.itemMeta
                noMeta.setDisplayName(ChatColor.RED.toString() + "いいえ")
                noItem.setItemMeta(noMeta)
                confirmInventory.setItem(15, noItem)

                player.openInventory(confirmInventory)
            } else {
                // ゴミ箱内でのアイテム移動を許可
                event.isCancelled = false
            }
        }

        // 確認画面が開いている場合
        if (event.view.title == ChatColor.RED.toString() + "本当に捨てますか？") {
            event.isCancelled = true // 確認画面内での操作をキャンセル

            if (clickedItem == null || clickedItem.type == Material.AIR) return

            if (clickedItem.type == Material.GREEN_STAINED_GLASS_PANE) {
                // プレイヤーのインベントリをクリア（ゴミ箱にあるアイテムを削除）
                val trashInventory = lastTrashInventories[player.uniqueId]
                if (trashInventory != null) {
                    trashInventory.clear() // アイテムを削除
                    player.closeInventory()
                    player.sendMessage(ChatColor.RED.toString() + "アイテムを削除しました。")
                } else {
                    player.sendMessage(ChatColor.RED.toString() + "エラー: ゴミ箱のインベントリが見つかりません。")
                }
            } else if (clickedItem.type == Material.RED_STAINED_GLASS_PANE) {
                // キャンセル（ゴミ箱のアイテムを戻す）
                player.closeInventory()
                val trashInventory = lastTrashInventories[player.uniqueId]
                if (trashInventory != null) {
                    for (item in trashInventory.contents) {
                        if (item != null) {
                            player.inventory.addItem(item) // プレイヤーにアイテムを返す
                        }
                    }
                    player.sendMessage(ChatColor.YELLOW.toString() + "アイテムの削除をキャンセルしました。")
                } else {
                    player.sendMessage(ChatColor.RED.toString() + "エラー: ゴミ箱のインベントリが見つかりません。")
                }
            }
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.view.title == ChatColor.GREEN.toString() + "ゴミ箱") {
            val inventory = event.inventory
            if (inventory != null) {
                inventory.clear() // ゴミ箱の内容をクリア
            }
        }
    }
}