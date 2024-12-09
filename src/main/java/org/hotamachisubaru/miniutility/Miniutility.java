package org.hotamachisubaru.miniutility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import org.hotamachisubaru.miniutility.Listener.Chat;
import org.hotamachisubaru.miniutility.Listener.EnderchestOpener;
import org.hotamachisubaru.miniutility.Listener.InstantCrafter;
import org.hotamachisubaru.miniutility.Listener.NameColor;
import org.hotamachisubaru.miniutility.Nickname.NicknameConfig;

import java.util.HashMap;
import java.util.Map;

public class Miniutility extends JavaPlugin implements Listener {
    private NicknameConfig nicknameConfig;
    private Inventory lastTrashInventory;
    private final Map<Player, Boolean> waitingForColorInput = new HashMap<>();

    @Override
    public void onEnable() {
        // リスナーの登録
        registerListeners();

        // コマンドの登録
        if (getCommand("menu") != null) {
            getCommand("menu").setExecutor(new UtilityCommand());
        }

        // ニックネーム設定の初期化
        nicknameConfig = new NicknameConfig(this);

        getLogger().info("Developed by hotamachisubaru.");
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, this);
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
            player.displayName(Component.text(nickname));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (title.equals("便利箱")) {
            handleUtilityMenuClick(player, clickedItem, event);
        } else if (title.equals("ゴミ箱")) {
            handleTrashBoxClick(player, event);
        } else if (title.equals("本当に捨てますか？")) {
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
            }
        }
    }

    private void promptForColorInput(Player player) {
        player.sendMessage(ChatColor.YELLOW + "名前の色を設定するために、チャットにカラーコードを入力してください（例：&6）。");
        waitingForColorInput.put(player, true);
        player.closeInventory();
    }

    private void promptForNicknameInput(Player player) {
        player.sendMessage(ChatColor.YELLOW + "ニックネームを入力してください。");
        player.closeInventory();
    }

    private void openTrashBox(Player player) {
        Inventory trashInventory = Bukkit.createInventory(player, 54, Component.text("ゴミ箱"));
        trashInventory.setItem(53, createGlassPane(Material.GREEN_STAINED_GLASS_PANE, "捨てる"));
        lastTrashInventory = trashInventory;
        player.openInventory(trashInventory);
    }

    private void handleTrashBoxClick(Player player, InventoryClickEvent event) {
        if (event.getRawSlot() == 53) {
            event.setCancelled(true);
            openTrashConfirm(player);
        }
    }

    private void openTrashConfirm(Player player) {
        Inventory confirmInventory = Bukkit.createInventory(player, 9, Component.text("本当に捨てますか？"));
        confirmInventory.setItem(2, createGlassPane(Material.GREEN_STAINED_GLASS_PANE, "はい"));
        confirmInventory.setItem(6, createGlassPane(Material.RED_STAINED_GLASS_PANE, "いいえ"));
        player.openInventory(confirmInventory);
    }

    private void handleTrashConfirmClick(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        event.setCancelled(true);
        if (clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE) {
            confirmTrashDelete(player);
        } else if (clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
            cancelTrashDelete(player);
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
