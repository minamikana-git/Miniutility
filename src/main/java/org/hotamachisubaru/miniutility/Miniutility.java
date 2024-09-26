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
import org.hotamachisubaru.miniutility.Listener.*;
import org.hotamachisubaru.miniutility.Nickname.NicknameCommand;
import org.hotamachisubaru.miniutility.Nickname.NicknameConfig;

public class Miniutility extends JavaPlugin implements Listener {

    private NicknameConfig nicknameConfig;
    private NicknameInputListener nicknameInputListener;
    private Inventory lastTrashInventory;

    @Override
    public void onEnable() {
        // Register event listeners
        Bukkit.getPluginManager().registerEvents(this, this);  // PlayerJoinEvent listener
        nicknameInputListener = new NicknameInputListener(this);
        Bukkit.getPluginManager().registerEvents(nicknameInputListener, this);  // Nickname input listener
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);

        // Register other listeners
        Bukkit.getPluginManager().registerEvents(new EnderchestOpenerListener(), this);
        Bukkit.getPluginManager().registerEvents(new InstantCrafterListener(), this);
          // Pass plugin instance

        // Commands
        this.getCommand("menu").setExecutor(new UtilityCommand());
        this.getCommand("nick").setExecutor(new NicknameCommand(this));

        // Initialize nickname config
        nicknameConfig = new NicknameConfig(this);

        getLogger().info("copyright 2024 hotamachisubaru all rights reserved." );
        getLogger().info("version " + getDescription().getVersion());
        getLogger().info("author " + getDescription().getAuthors());
        getLogger().info("developmented by hotamachisubaru");
    }

    public NicknameConfig getNicknameConfig() {
        return nicknameConfig;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String nickname = nicknameConfig.getNickname(player.getUniqueId());

        if (nickname != null) {
            player.setDisplayName(nickname);
            player.setPlayerListName(nickname);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        // 便利箱のインベントリが開いている場合
        if (event.getView().getTitle().equals("便利箱")) {
            event.setCancelled(true);  // 便利箱内の操作をキャンセル

            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            // 作業台のクリック
            else if (clickedItem.getType() == Material.CRAFTING_TABLE) {
                player.openWorkbench(player.getLocation(), true); // 位置を明示的に指定
            }

            // エンダーチェストのクリック
            else if (clickedItem.getType() == Material.ENDER_CHEST) {
                Inventory enderChestInventory = player.getEnderChest(); // エンダーチェストのインベントリを取得
                player.openInventory(enderChestInventory); // インベントリを開く
            }



            // ニックネーム変更のボタンをクリックした場合
            else if (clickedItem.getType() == Material.WRITABLE_BOOK) {
                player.sendMessage(ChatColor.YELLOW + "ニックネームを入力してください。");
                nicknameInputListener.setWaitingForNickname(player, true);
                player.closeInventory();
            }

            // ゴミ箱のアイコンがクリックされた場合
            else if (clickedItem.getType() == Material.DROPPER) {
                Inventory trashInventory = Bukkit.createInventory(player, 54, ChatColor.GREEN + "ゴミ箱");

                // 確認ボタンを右下に配置
                ItemStack confirmButton = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                ItemMeta confirmMeta = confirmButton.getItemMeta();
                confirmMeta.setDisplayName(ChatColor.GREEN + "捨てる");
                confirmButton.setItemMeta(confirmMeta);

                trashInventory.setItem(53, confirmButton);

                // ゴミ箱のインベントリをlastTrashInventoryに保存
                lastTrashInventory = trashInventory;

                player.openInventory(trashInventory);
            }
        }

        // ゴミ箱インベントリを開いている場合
        if (event.getView().getTitle().equals(ChatColor.GREEN + "ゴミ箱")) {
            if (event.getRawSlot() == 53 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.GREEN_STAINED_GLASS_PANE) {
                event.setCancelled(true);  // 「捨てる」ボタンのクリックをキャンセル

                // 確認画面を開く
                Inventory confirmInventory = Bukkit.createInventory(player, 27, ChatColor.RED + "本当に捨てますか？");

                // Yesボタン (緑色のガラス)
                ItemStack yesItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                ItemMeta yesMeta = yesItem.getItemMeta();
                yesMeta.setDisplayName(ChatColor.GREEN + "はい");
                yesItem.setItemMeta(yesMeta);

                // Noボタン (赤色のガラス)
                ItemStack noItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta noMeta = noItem.getItemMeta();
                noMeta.setDisplayName(ChatColor.RED + "いいえ");
                noItem.setItemMeta(noMeta);

                confirmInventory.setItem(11, yesItem);
                confirmInventory.setItem(15, noItem);

                player.openInventory(confirmInventory);
            } else {
                event.setCancelled(false);  // ゴミ箱内でのアイテム移動を許可
            }
        }

        // 確認画面が開いている場合
        if (event.getView().getTitle().equals(ChatColor.RED + "本当に捨てますか？")) {
            event.setCancelled(true);  // 確認画面内での操作をキャンセル

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            if (clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE) {
                if (lastTrashInventory != null) {
                    lastTrashInventory.clear();  // アイテムを削除
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "アイテムを削除しました。");
                } else {
                    player.sendMessage(ChatColor.RED + "エラー: ゴミ箱のインベントリが見つかりません。");
                }
            } else if (clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
                player.closeInventory();
                if (lastTrashInventory != null) {
                    for (ItemStack item : lastTrashInventory.getContents()) {
                        if (item != null) {
                            player.getInventory().addItem(item);  // プレイヤーにアイテムを返す
                        }
                    }
                    player.sendMessage(ChatColor.YELLOW + "アイテムの削除をキャンセルしました。");
                } else {
                    player.sendMessage(ChatColor.RED + "エラー: ゴミ箱のインベントリが見つかりません。");
                }
            }
        }
    }
}
