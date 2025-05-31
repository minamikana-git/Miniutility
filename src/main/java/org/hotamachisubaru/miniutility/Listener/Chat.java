package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chat リスナー:
 *  - ニックネーム入力待機中／色コード入力待機中／経験値入力待機中 のときだけチャットをキャンセルして処理を行い、
 *    それ以外は何もしない（LunaChat 等の別プラグインに委ねる）。
 */
public class Chat implements Listener {

    // -------------------------------
    // フィールドとマップの定義
    // -------------------------------

    private final Plugin plugin;

    /** ニックネーム入力待機フラグを保持するマップ */
    private static final Map<UUID, Boolean> waitingForNickname = new ConcurrentHashMap<>();

    /** 色コード入力待機フラグを保持するマップ */
    private static final Map<UUID, Boolean> waitingForColorInput = new ConcurrentHashMap<>();

    /** 経験値入力待機フラグを保持するマップ */
    private static final Map<UUID, Boolean> waitingForExpInput = new ConcurrentHashMap<>();

    /**
     * コンストラクタ。プラグインインスタンスを保持しておく。
     */
    public Chat(Plugin plugin) {
        this.plugin = plugin;
    }

    // -------------------------------
    // 待機フラグの setter/getter
    // -------------------------------

    /** ニックネーム入力待機フラグをセット／クリア */
    public static void setWaitingForNickname(Player player, boolean flag) {
        if (flag) {
            waitingForNickname.put(player.getUniqueId(), true);
        } else {
            waitingForNickname.remove(player.getUniqueId());
        }
    }

    /** プレイヤーが現在ニックネーム入力待機中か？ */
    public static boolean isWaitingForNickname(Player player) {
        return waitingForNickname.containsKey(player.getUniqueId());
    }

    /** 色コード入力待機フラグをセット／クリア */
    public static void setWaitingForColorInput(Player player, boolean flag) {
        if (flag) {
            waitingForColorInput.put(player.getUniqueId(), true);
        } else {
            waitingForColorInput.remove(player.getUniqueId());
        }
    }

    /** プレイヤーが現在色コード入力待機中か？ */
    public static boolean isWaitingForColorInput(Player player) {
        return waitingForColorInput.containsKey(player.getUniqueId());
    }

    /** 経験値入力待機フラグをセット／クリア */
    public static void setWaitingForExpInput(Player player, boolean flag) {
        if (flag) {
            waitingForExpInput.put(player.getUniqueId(), true);
        } else {
            waitingForExpInput.remove(player.getUniqueId());
        }
    }

    /** プレイヤーが現在経験値入力待機中か？ */
    public static boolean isWaitingForExpInput(Player player) {
        return waitingForExpInput.containsKey(player.getUniqueId());
    }

    /**
     * DisplayName および PlayerListName のプレフィックス更新
     * config.yml の "combine-prefix" 設定に応じて Prefix を結合するか切り替える
     */
    public static void updateDisplayNamePrefix(Player player, String nickname) {
        // 1. LuckPerms からプレフィックスを取得
        String prefix = "";
        try {
            CachedMetaData metaData = LuckPermsProvider.get()
                    .getPlayerAdapter(Player.class)
                    .getMetaData(player);
            prefix = metaData.getPrefix();
            if (prefix == null) prefix = "";
        } catch (IllegalStateException e) {
            Bukkit.getLogger().warning("LuckPerms がロードされていないため、プレイヤーの Prefix を取得できません: "
                    + player.getName());
            prefix = "";
        }

        // 2. config.yml の combine-prefix を参照
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Miniutility");
        boolean combine = true;  // デフォルト値は true
        if (plugin != null) {
            combine = plugin.getConfig().getBoolean("combine-prefix", true);
        }

        // 3. combine の値に応じて表示名を生成
        String formatted;
        if (combine) {
            // Prefix + ニックネームを結合し、色コードを適用
            formatted = prefix + nickname;
        } else {
            // ニックネームのみ
            formatted = nickname;
        }

        // 4. Legacy 形式の色コード（&）を Adventure Component に変換
        Component formattedComponent;
        try {
            formattedComponent = LegacyComponentSerializer.legacy('&').deserialize(
                    ChatColor.translateAlternateColorCodes('&', formatted)
            );
        } catch (Exception ex) {
            Bukkit.getLogger().warning("色コードの変換に失敗しました。combined=" + formatted
                    + " / error=" + ex.getMessage());
            formattedComponent = Component.text(prefix + nickname);
        }

        // 5. プレイヤーの表示名とリスト名を更新
        try {
            player.displayName(formattedComponent);
            player.playerListName(formattedComponent);
        } catch (Exception e) {
            Bukkit.getLogger().warning("プレイヤーの表示名を設定中にエラーが発生しました: "
                    + e.getMessage());
        }
    }

    // -------------------------------
    // メインイベントハンドラ
    // -------------------------------

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // ----------------------------------------------------------------
        // ① ニックネーム入力待機中の処理
        // ----------------------------------------------------------------
        if (isWaitingForNickname(player)) {
            event.setCancelled(true);  // チャットをキャンセルして、自前処理に移る

            String inputNick = event.getMessage().trim();
            if (inputNick.isEmpty()) {
                player.sendMessage(
                        Component.text("ニックネームが空です。もう一度入力してください。")
                                .color(NamedTextColor.RED)
                );
                return; // フラグはまだクリアせず、再度待機させる
            }

            // 非同期で DB 保存を行い、その後メインスレッドで表示名を更新する
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                // 1) データベースにニックネームを保存
                NicknameDatabase.saveNickname(player.getUniqueId().toString(), inputNick);

                // 2) メインスレッドに戻してプレイヤー表示名を更新
                Bukkit.getScheduler().runTask(plugin, () -> {
                    // NicknameManager 内で updateDisplayNamePrefix を呼び出す実装を想定
                    NicknameManager.applyFormattedDisplayName(player);
                });
            });

            // フラグをクリアして、二重入力を防ぐ
            setWaitingForNickname(player, false);

            // フィードバックメッセージ
            player.sendMessage(
                    Component.text("✅ ニックネームを「" + inputNick + "」に設定しました。")
                            .color(NamedTextColor.GREEN)
            );
            return;
        }

        // ----------------------------------------------------------------
        // ② 色コード入力待機中の処理
        // ----------------------------------------------------------------
        if (isWaitingForColorInput(player)) {
            event.setCancelled(true);  // チャットをキャンセルして、自前処理に移る

            String inputCode = event.getMessage().trim();
            if (inputCode.isEmpty() || inputCode.length() > 16) {
                player.sendMessage(
                        Component.text("無効な入力です。色付き表示したいニックネームを16文字以内で入力してください。")
                                .color(NamedTextColor.RED)
                );
                // フラグをクリアして待機を解除
                setWaitingForColorInput(player, false);
                return;
            }

            // &記号付き色コードを翻訳（例: "&6ほたまち" → Gold + "ほたまち"）
            String translated = ChatColor.translateAlternateColorCodes('&', inputCode);
            // 「色付きコードが含まれているか」を判定
            if (!ChatColor.stripColor(translated).equals(translated)) {
                String updatedNickname = translated;

                // 非同期で DB 保存、その後メインスレッドで表示名更新
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    NicknameDatabase.saveNickname(player.getUniqueId().toString(), updatedNickname);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        NicknameManager.applyFormattedDisplayName(player);
                    });
                });

                player.sendMessage(
                        Component.text("✅ ニックネームの色を変更しました: ")
                                .color(NamedTextColor.GREEN)
                                .append(Component.text(updatedNickname).color(NamedTextColor.AQUA))
                );
            } else {
                player.sendMessage(
                        Component.text("無効なカラーコードです。例: &6テキスト")
                                .color(NamedTextColor.RED)
                );
            }

            // フラグをクリアして待機を解除
            setWaitingForColorInput(player, false);
            return;
        }

        // ----------------------------------------------------------------
        // ③ 経験値入力待機中の処理
        // ----------------------------------------------------------------
        if (isWaitingForExpInput(player)) {
            event.setCancelled(true);  // チャットをキャンセルして、自前処理に移る

            String msg = event.getMessage().trim();
            try {
                int inputValue = Integer.parseInt(msg);
                if (inputValue >= 0) {
                    player.giveExpLevels(inputValue);
                    player.sendMessage(Component.text("経験値レベルに +" + inputValue + " しました。")
                            .color(NamedTextColor.AQUA));
                } else {
                    int currentLevel = player.getLevel();
                    int target = currentLevel + inputValue; // inputValue は負の値
                    if (target < 0) target = 0;
                    player.setLevel(target);
                    player.sendMessage(Component.text("経験値レベルから " + (-inputValue) + " 減らしました。")
                            .color(NamedTextColor.RED));
                }
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("無効な入力です。整数（例: 10 または -5）を入力してください。")
                        .color(NamedTextColor.RED));
                // フラグをクリアせず再度入力待機状態にする場合はここで return し
                // ただし今回は一度解除した上で再度案内する実装とします
            }

            // フラグをクリアして待機を解除
            setWaitingForExpInput(player, false);
        }

        // ----------------------------------------------------------------
        // ④ 通常チャット：何もしない → 他プラグインに委ねる
        // ----------------------------------------------------------------
        // event.setCancelled(false) のままにしておけば、以降のプラグイン（LunaChat 等）がチャットを処理します。
    }
}
