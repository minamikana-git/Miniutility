package org.hotamachisubaru.miniutility;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.hotamachisubaru.miniutility.Command.UtilityCommand;
import org.hotamachisubaru.miniutility.Listener.*;
import org.hotamachisubaru.miniutility.Nickname.NicknameConfig;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class Miniutility extends JavaPlugin implements Listener {
    private NicknameConfig nicknameConfig;
    private Nickname nicknameInputListener;
    private Inventory lastTrashInventory;
    private Map<Player, Boolean> waitingForColorInput;
    private Map<UUID, Boolean> waitingForNicknameInput = new HashMap<>();
    private Chat chat;
    private EnderchestOpener enderchestOpener;
    private InstantCrafter instantCrafter;
    private NameColor nameColor;


    @Override
    public void onEnable() {
        // Initialize nickname config
        nicknameConfig = new NicknameConfig(this);
        // Initialize listeners
        chat = new Chat();
        enderchestOpener = new EnderchestOpener();
        instantCrafter = new InstantCrafter();
        nameColor = new NameColor();
        // Initialize waitingForNicknameInput
        waitingForNicknameInput = new HashMap<>();

        // Initialize nicknameInputListener
        nicknameInputListener = new Nickname(waitingForNicknameInput,nicknameConfig);

        // Register event listeners
        registerListeners();

        // Commands
        getCommand("menu").setExecutor(new UtilityCommand());

        // Initialize other maps
        waitingForColorInput = new HashMap<>();

        // Log copyright information
        Logger logger = Bukkit.getLogger();
        logger.info("copyright 2024 hotamachisubaru all rights reserved.");
        logger.info("developed by hotamachisubaru");
    }



    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(nicknameInputListener, this);
        Bukkit.getPluginManager().registerEvents(new NameColor(), this);
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new Chat(), this);
        Bukkit.getPluginManager().registerEvents(new EnderchestOpener(), this);
        Bukkit.getPluginManager().registerEvents(new InstantCrafter(), this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String nickname = nicknameConfig != null ? nicknameConfig.getNickname(player.getUniqueId(), player.getName()) : null;

        if (nickname != null) {
            player.displayName(Component.text(nickname));
            player.playerListName(Component.text(nickname));
        } else {
            player.displayName(Component.text(player.getName()));
            player.playerListName(Component.text(player.getName()));
        }
    }

    @EventHandler
    public void utility(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || event.getClickedInventory() == null) {
            return; // 無効なクリックはスキップ
        }

        String title = event.getView().getTitle();
        if (title.equals(Constants.UTILITY_BOX_TITLE)) {
            handleUtilityMenuClick(player, clickedItem, event);
        } else if (title.equals(Constants.TRASH_BOX_TITLE)) {
            handleTrashBoxClick(player, event);
        } else if (title.equals(Constants.TRASH_CONFIRM_TITLE)) {
            handleTrashConfirmClick(player, clickedItem, event);
        } else {
            // プレイヤーインベントリ以外はキャンセル
            if (!event.getClickedInventory().equals(player.getInventory())) {
                event.setCancelled(true);
            }
        }
    }


    private void handleUtilityMenuClick(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        event.setCancelled(true);

        if (clickedItem != null) {
            switch (clickedItem.getType()) {
                case GREEN_DYE:
                    promptForColorInput(player);
                    break;
                case CRAFTING_TABLE:
                    player.openWorkbench(player.getLocation(), true);
                    break;
                case ENDER_CHEST:
                    player.openInventory(player.getEnderChest());
                    break;
                case WRITABLE_BOOK:
                    promptForNicknameInput(player);
                    break;
                case DROPPER:
                    openTrashBox(player);
                    break;
                default:
                    // 何もしない、もしくはデフォルトの動作を指定
                    break;
            }
        }
    }

    public void promptForColorInput(Player player) {
        player.sendMessage(ChatColor.YELLOW + "名前の色を設定するために、チャットにカラーコードを入力してください（例：&6）。");
        NameColor.waitingForColorInput.put(player, true);
        player.closeInventory();
    }

    public void promptForNicknameInput(Player player) {
        player.sendMessage(ChatColor.YELLOW + "ニックネームを入力してください。");
        if (nicknameInputListener != null) {
            nicknameInputListener.setWaitingForNickname(player, true);
        }
        player.closeInventory();
    }

    public void openTrashBox(Player player) {
        Inventory trashInventory = Bukkit.createInventory(player, 54, ChatColor.GREEN + "ゴミ箱");

        // 確認ボタンを追加
        ItemStack confirmButton = createGlassPane(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "捨てる");
        trashInventory.setItem(53, confirmButton);

        lastTrashInventory = trashInventory;
        player.openInventory(trashInventory);
    }

    public void handleTrashBoxClick(Player player, InventoryClickEvent event) {
        if (event.getRawSlot() == 53 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.GREEN_STAINED_GLASS_PANE) {
            event.setCancelled(true);
            openTrashConfirm(player);
        } else {
            event.setCancelled(false);
        }
    }

    public void openTrashConfirm(Player player) {
        Inventory confirmInventory = Bukkit.createInventory(player, 9, ChatColor.RED + "本当に捨てますか？");

        // Yesボタン
        ItemStack yesItem = createGlassPane(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "はい");
        confirmInventory.setItem(2, yesItem);

        // Noボタン
        ItemStack noItem = createGlassPane(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "いいえ");
        confirmInventory.setItem(6, noItem);

        player.openInventory(confirmInventory);
    }

    private void handleTrashConfirmClick(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        event.setCancelled(true);

        if (clickedItem != null) {
            switch (clickedItem.getType()) {
                case GREEN_STAINED_GLASS_PANE:
                    confirmTrashDelete(player);
                    break;
                case RED_STAINED_GLASS_PANE:
                    cancelTrashDelete(player);
                    break;
                default:
                    // 何もしない、もしくはデフォルトの動作を指定
                    break;
            }
        }
    }

    private void confirmTrashDelete(Player player) {
        if (lastTrashInventory != null) {
            lastTrashInventory.clear();
        }
        player.closeInventory();
        player.sendMessage(ChatColor.RED + "アイテムを削除しました。");
    }

    private void cancelTrashDelete(Player player) {
        player.closeInventory();
        if (lastTrashInventory != null) {
            for (ItemStack item : lastTrashInventory.getContents()) {
                if (item != null) {
                    player.getInventory().addItem(item);
                }
            }
        }
        player.sendMessage(ChatColor.YELLOW + "アイテムの削除をキャンセルしました。");
    }

    private ItemStack createGlassPane(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    public NicknameConfig getNicknameConfig() {
        return nicknameConfig;
    }
}