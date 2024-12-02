package org.hotamachisubaru.miniutility;

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

public class Miniutility extends JavaPlugin implements Listener {
    private NicknameConfig nicknameConfig = null;
    private Nickname nicknameInputListener = null;
    private Inventory lastTrashInventory = null;
    private Map<Player, Boolean> waitingForColorInput = null;
    private Map<Player, Boolean> waitingForNicknameInput = null;

    @Override
    public void onEnable() {
        // Register event listeners
        nicknameInputListener = new Nickname(this);
        registerListeners();

        // Commands
        if (getCommand("menu") != null) {
            getCommand("menu").setExecutor(new UtilityCommand());
        }

        // Initialize nickname config
        nicknameConfig = new NicknameConfig(this);
        waitingForColorInput = new HashMap<>();

        // Log startup information
        getLogger().info("copyright 2024 hotamachisubaru all rights reserved.");
        getLogger().info("developed by hotamachisubaru");
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(nicknameInputListener, this);
        Bukkit.getPluginManager().registerEvents(new Chat(), this);
        Bukkit.getPluginManager().registerEvents(new EnderchestOpener(), this);
        Bukkit.getPluginManager().registerEvents(new InstantCrafter(), this);
        Bukkit.getPluginManager().registerEvents(new NameColor(), this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String nickname = nicknameConfig.getNickname(player.getUniqueId(), player.getName());
        if (nickname != null) {
            player.setDisplayName(nickname);
            player.setPlayerListName(nickname);
        }
    }

    @EventHandler
    public void utility(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        String title = event.getView().getTitle();
        if (title.equals("便利箱")) {
            handleUtilityMenuClick(player, clickedItem, event);
        } else if (title.equals(ChatColor.GREEN + "ゴミ箱")) {
            handleTrashBoxClick(player, event);
        } else if (title.equals(ChatColor.RED + "本当に捨てますか？")) {
            handleTrashConfirmClick(player, clickedItem, event);
        }
    }

    private void handleUtilityMenuClick(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        event.setCancelled(true);

        if (clickedItem != null) {
            switch (clickedItem.getType()) {
                case GREEN_DYE -> promptForColorInput(player);
                case CRAFTING_TABLE -> player.openWorkbench(player.getLocation(), true);
                case ENDER_CHEST -> player.openInventory(player.getEnderChest());
                case WRITABLE_BOOK -> promptForNicknameInput(player);
                case DROPPER -> openTrashBox(player);
                default -> {
                }
            }
        }
    }

    private void promptForColorInput(Player player) {
        player.sendMessage(ChatColor.YELLOW + "名前の色を設定するために、チャットにカラーコードを入力してください（例：&6）。");
        NameColor.waitingForColorInput.put(player, true);
        player.closeInventory();
    }

    private void promptForNicknameInput(Player player) {
        player.sendMessage(ChatColor.YELLOW + "ニックネームを入力してください。");
        nicknameInputListener.setWaitingForNickname(player, true);
        player.closeInventory();
    }

    private void openTrashBox(Player player) {
        Inventory trashInventory = Bukkit.createInventory(player, 54, ChatColor.GREEN + "ゴミ箱");

        // 確認ボタンを追加
        ItemStack confirmButton = createGlassPane(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "捨てる");
        trashInventory.setItem(53, confirmButton);

        lastTrashInventory = trashInventory;
        player.openInventory(trashInventory);
    }

    private void handleTrashBoxClick(Player player, InventoryClickEvent event) {
        if (event.getRawSlot() == 53 && event.getCurrentItem().getType() == Material.GREEN_STAINED_GLASS_PANE) {
            event.setCancelled(true);
            openTrashConfirm(player);
        } else {
            event.setCancelled(false);
        }
    }

    private void openTrashConfirm(Player player) {
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
                case GREEN_STAINED_GLASS_PANE -> confirmTrashDelete(player);
                case RED_STAINED_GLASS_PANE -> cancelTrashDelete(player);
                default -> {
                }
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
            for (ItemStack itemStack : lastTrashInventory.getContents()) {
                if (itemStack != null) {
                    player.getInventory().addItem(itemStack);
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
