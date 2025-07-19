package org.hotamachisubaru.miniutility.PluginLoad;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;

public class MiniutilityBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext context) {
        // 必要ならログ出力や先行初期化も可能（ほとんどは空実装でOK）
        // System.out.println("Miniutility Bootstrapper loaded!");
    }
}
