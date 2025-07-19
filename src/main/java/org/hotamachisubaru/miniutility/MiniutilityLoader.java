package org.hotamachisubaru.miniutility;

import org.bukkit.plugin.java.JavaPlugin;

public class MiniutilityLoader extends JavaPlugin {

    private Miniutility miniutility;

    @Override
    public void onLoad() {
        // プラグインのロード段階でMiniutility本体生成
        miniutility = new Miniutility(this);
    }

    @Override
    public void onEnable() {
        // プラグイン有効化段階でMiniutility初期化
        if (miniutility != null) {
            miniutility.enable();
        }
    }

    @Override
    public void onDisable() {
        // プラグイン無効化時
        if (miniutility != null) {
            miniutility.disable();
        }
    }

    public Miniutility getMiniutility() {
        return miniutility;
    }
}
