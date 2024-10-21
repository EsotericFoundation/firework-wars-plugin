package foundation.esoteric.minecraft.plugins.games.fireworkwars;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.commands.*;
import net.kyori.adventure.text.Component;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.manager.ArenaManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.events.global.ItemOwnerChangeListener;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.events.global.PlayerLoseHungerListener;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.file.FileManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.FireworkWarsGame;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.GameManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.CustomItemManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.LanguageManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.managers.PlayerVelocityManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.profile.PlayerDataManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.PersistentDataManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class FireworkWarsPlugin extends JavaPlugin implements Listener {
    private static FireworkWarsPlugin instance;
    private static Logger logger;

    private final FileManager fileManager;
    private final Path mapsDirectory = Paths.get("plugins/FireworkWarsPlugin/maps");
    private final Path rootDirectory = Paths.get("").toAbsolutePath();

    private final CustomItemManager customItemManager;
    private PlayerDataManager playerDataManager;
    private LanguageManager languageManager;
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private PersistentDataManager pdcManager;
    private PlayerVelocityManager playerVelocityManager;

    private ResetInventoryCommand resetInventoryCommand;
    private HealCommand healCommand;

    public static FireworkWarsPlugin getInstance() {
        return instance;
    }

    public static Logger logger() {
        return logger;
    }

    public PlayerDataManager getPlayerDataManager() {
        return this.playerDataManager;
    }

    public LanguageManager getLanguageManager() {
        return this.languageManager;
    }

    public ArenaManager getArenaManager() {
        return this.arenaManager;
    }

    public GameManager getGameManager() {
        return this.gameManager;
    }

    public CustomItemManager getCustomItemManager() {
        return this.customItemManager;
    }

    public PersistentDataManager getPdcManager() {
        return this.pdcManager;
    }

    public PlayerVelocityManager getPlayerVelocityManager() {
        return this.playerVelocityManager;
    }

    public ResetInventoryCommand getResetInventoryCommand() {
        return this.resetInventoryCommand;
    }

    public HealCommand getHealCommand() {
        return this.healCommand;
    }

    public FireworkWarsPlugin(CustomItemManager customItemManager) {
        FireworkWarsPlugin.instance = this;
        FireworkWarsPlugin.logger = getLogger();

        this.customItemManager = customItemManager;
        this.fileManager = new FileManager(this);

        try {
            saveMaps();
        } catch (IOException exception) {
            getLogger().severe(exception.getMessage() + Arrays.toString(exception.getStackTrace()));
        }
    }

    private void saveMaps() throws IOException {
        if (new File("barracks").exists()) {
            getLogger().info("Worlds already exist, skipping saving procedure.");
        } else {
            FileUtils.deleteDirectory(new File("world"));

            fileManager.saveResourceFileFolder("maps/barracks");
            saveResource("maps/barracks/level.dat", true);

            fileManager.saveResourceFileFolder("maps/world");
            saveResource("maps/world/level.dat", true);

            moveMapsToRoot();
        }
    }

    private void moveMapsToRoot() throws IOException {
        moveFolderToRoot(mapsDirectory);

        new File("world/playerdata").mkdir();
    }

    private void moveFolderToRoot(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path relativePath = mapsDirectory.relativize(file);
                    Path targetPath = rootDirectory.resolve(relativePath);

                    Files.createDirectories(targetPath.getParent());
                    Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });

            getLogger().info("All files moved successfully!");
        } else {
            getLogger().info("Directory does not exist.");
        }
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void onEnable() {
        getDataFolder().mkdir();
        saveDefaultConfig();

        CommandAPIBukkitConfig commandAPIConfig = new CommandAPIBukkitConfig(this);

        CommandAPI.onLoad(commandAPIConfig);
        CommandAPI.onEnable();

        this.playerDataManager = new PlayerDataManager(this);
        this.languageManager = new LanguageManager(this);
        this.arenaManager = new ArenaManager(this);
        this.gameManager = new GameManager(this);
        this.pdcManager = new PersistentDataManager();
        this.playerVelocityManager = new PlayerVelocityManager(this);

        customItemManager.setPlugin(this);
        customItemManager.registerCustomItems();

        new SetLanguageCommand(this);
        new ArenaCommand(this);
        new GiveItemCommand(this);
        this.resetInventoryCommand = new ResetInventoryCommand(this);
        this.healCommand = new HealCommand(this);

        new ItemOwnerChangeListener(this).register();
        new PlayerLoseHungerListener(this).register();

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.save();
        }
    }

    public BukkitTask runTaskLater(Runnable runnable, long delay) {
        return getServer().getScheduler().runTaskLater(this, runnable, delay);
    }

    public void runTaskTimer(Runnable runnable, long delay, long period) {
        getServer().getScheduler().runTaskTimer(this, runnable, delay, period);
    }

    public void logLoudly(String message) {
        this.getServer().broadcast(Component.text(message));
    }

    @EventHandler
    public void onDamage(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
           return;
        }

        FireworkWarsGame game = gameManager.getFireworkWarsGame(player);

        if (game == null || game.getGameState() != FireworkWarsGame.GameState.PLAYING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();

        FireworkWarsGame game = gameManager.getFireworkWarsGame(player);

        if (game == null || game.getGameState() != FireworkWarsGame.GameState.PLAYING) {
            event.setCancelled(true);
        }
    }
}
