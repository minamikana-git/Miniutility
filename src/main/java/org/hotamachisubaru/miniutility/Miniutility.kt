package org.hotamachisubaru.miniutility

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.hotamachisubaru.miniutility.Listener.*
import org.hotamachisubaru.miniutility.Nickname.NicknameCommand
import org.hotamachisubaru.miniutility.Nickname.NicknameConfig

class Miniutility : JavaPlugin(), Listener {
    var nicknameConfig: NicknameConfig? = null
        private set
    private var nicknameInputListener: NicknameInputListener? = null
    private var lastTrashInventory: Inventory? = null
    var waitingForColorInput: Map<Player, Boolean>? = null
    var waitingForNicknameInput: Map<Player, Boolean>? = null

    override fun onEnable() {
        // Register event listeners
        nicknameInputListener = NicknameInputListener(this)
        registerListeners()

        // Commands
        getCommand("menu")!!.setExecutor(UtilityCommand())


        // Initialize nickname config
        nicknameConfig = NicknameConfig(this)
        waitingForColorInput = HashMap()

        // copyrights
        logger.info("copyright 2024 hotamachisubaru all rights reserved.")
        logger.info("developmented by hotamachisubaru")
    }

    private fun registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, this)
        Bukkit.getPluginManager().registerEvents(nicknameInputListener!!, this)
        Bukkit.getPluginManager().registerEvents(ChatListener(), this)
        Bukkit.getPluginManager().registerEvents(EnderchestOpenerListener(), this)
        Bukkit.getPluginManager().registerEvents(InstantCrafterListener(), this)
        Bukkit.getPluginManager().registerEvents(NameColorListener(), this)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val nickname = nicknameConfig?.getNickname(player.uniqueId)
        nickname?.let {
            player.setDisplayName(it)
            player.setPlayerListName(it)
        }
    }

    @EventHandler
    fun utility(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        val clickedItem = event.currentItem

        when (event.view.title) {
            "便利箱" -> handleUtilityMenuClick(player, clickedItem, event)
            ChatColor.GREEN.toString() + "ゴミ箱" -> handleTrashBoxClick(player, event)
            ChatColor.RED.toString() + "本当に捨てますか？" -> handleTrashConfirmClick(player, clickedItem, event)
        }
    }

    private fun handleUtilityMenuClick(player: Player, clickedItem: ItemStack?, event: InventoryClickEvent) {
        event.isCancelled = true

        clickedItem?.let {
            when (it.type) {
                Material.GREEN_DYE -> promptForColorInput(player)
                Material.CRAFTING_TABLE -> player.openWorkbench(player.location, true)
                Material.ENDER_CHEST -> player.openInventory(player.enderChest)
                Material.WRITABLE_BOOK -> promptForNicknameInput(player)
                Material.DROPPER -> openTrashBox(player)
                else -> {
                    // 何もしない、もしくはデフォルトの動作を指定
                }
            }
        }
    }

    private fun promptForColorInput(player: Player) {
        player.sendMessage(ChatColor.YELLOW.toString() + "名前の色を設定するために、チャットにカラーコードを入力してください（例：&6）。")
        NameColorListener.waitingForColorInput[player] = true
        player.closeInventory()
    }

    private fun promptForNicknameInput(player: Player) {
        player.sendMessage(ChatColor.YELLOW.toString() + "ニックネームを入力してください。")
        nicknameInputListener?.setWaitingForNickname(player, true)
        player.closeInventory()
    }

    private fun openTrashBox(player: Player) {
        val trashInventory = Bukkit.createInventory(player, 54, ChatColor.GREEN.toString() + "ゴミ箱")

        // 確認ボタンを追加
        val confirmButton = createGlassPane(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN.toString() + "捨てる")
        trashInventory.setItem(53, confirmButton)

        lastTrashInventory = trashInventory
        player.openInventory(trashInventory)
    }

    private fun handleTrashBoxClick(player: Player, event: InventoryClickEvent) {
        if (event.rawSlot == 53 && event.currentItem?.type == Material.GREEN_STAINED_GLASS_PANE) {
            event.isCancelled = true
            openTrashConfirm(player)
        } else {
            event.isCancelled = false
        }
    }

    private fun openTrashConfirm(player: Player) {
        val confirmInventory = Bukkit.createInventory(player, 9, ChatColor.RED.toString() + "本当に捨てますか？")

        // Yesボタン
        val yesItem = createGlassPane(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN.toString() + "はい")
        confirmInventory.setItem(2, yesItem)

        // Noボタン
        val noItem = createGlassPane(Material.RED_STAINED_GLASS_PANE, ChatColor.RED.toString() + "いいえ")
        confirmInventory.setItem(6, noItem)

        player.openInventory(confirmInventory)
    }

    private fun handleTrashConfirmClick(player: Player, clickedItem: ItemStack?, event: InventoryClickEvent) {
        event.isCancelled = true

        clickedItem?.let {
            when (it.type) {
                Material.GREEN_STAINED_GLASS_PANE -> confirmTrashDelete(player)
                Material.RED_STAINED_GLASS_PANE -> cancelTrashDelete(player)
                else -> {
                    // 何もしない、もしくはデフォルトの動作を指定
                }
            }
        }
    }

    private fun confirmTrashDelete(player: Player) {
        lastTrashInventory?.clear()
        player.closeInventory()
        player.sendMessage(ChatColor.RED.toString() + "アイテムを削除しました。")
    }

    private fun cancelTrashDelete(player: Player) {
        player.closeInventory()
        lastTrashInventory?.contents?.filterNotNull()?.forEach { player.inventory.addItem(it) }
        player.sendMessage(ChatColor.YELLOW.toString() + "アイテムの削除をキャンセルしました。")
    }

    private fun createGlassPane(material: Material, name: String): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta?.setDisplayName(name)
        item.setItemMeta(meta)
        return item
    }
}
