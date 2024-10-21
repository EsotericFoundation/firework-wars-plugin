package foundation.esoteric.minecraft.plugins.games.fireworkwars;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.registry.event.RegistryEvents;
import org.bukkit.plugin.java.JavaPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.CustomItemManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.ReflectUtil;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public final class FireworkWarsPluginBootstrapper implements PluginBootstrap {
    private final ReflectUtil reflectUtil = new ReflectUtil();
    private final CustomItemManager customItemManager = new CustomItemManager(reflectUtil);

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        reflectUtil.useLogger(context.getLogger());

        context.getLifecycleManager().registerEventHandler(RegistryEvents.GAME_EVENT.freeze(), event -> {
            customItemManager.registerNMSItems();
        });
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        return new FireworkWarsPlugin(customItemManager);
    }
}
