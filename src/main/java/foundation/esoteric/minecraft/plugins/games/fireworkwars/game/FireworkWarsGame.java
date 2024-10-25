package foundation.esoteric.minecraft.plugins.games.fireworkwars.game;

import foundation.esoteric.minecraft.plugins.games.fireworkwars.events.game.HouseKeepingListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitTask;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.data.TeamData;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.data.WorldBorderData;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.structure.Arena;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.events.game.GameEventListener;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.events.game.PlayerConnectionListener;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.chests.ChestManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.runnables.GameCountdown;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.runnables.GameTickHandler;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.FireworkWarsTeam;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.TeamPlayer;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.CustomItemManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.LanguageManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.kyori.adventure.title.Title.title;
import static foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Util.randomInt;

@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "FieldCanBeLocal"})
public class FireworkWarsGame {
    private final FireworkWarsPlugin plugin;
    private final LanguageManager languageManager;
    private final CustomItemManager customItemManager;

    private final Arena arena;
    private final ChestManager chestManager;

    private final GameEventListener eventListener;
    private final HouseKeepingListener houseKeepingListener;

    private final PlayerConnectionListener connectionListener;

    private GameTickHandler tickHandler;
    private @Nullable GameCountdown countdown;

    private GameState gameState = GameState.WAITING;
    private final Map<String, Boolean> worldLoadStates = new HashMap<>();

    private final List<FireworkWarsTeam> teams = new ArrayList<>();
    private final List<TeamPlayer> players = new ArrayList<>();

    private final List<BukkitTask> tasks = new ArrayList<>();

    public FireworkWarsPlugin getPlugin() {
        return plugin;
    }

    public Arena getArena() {
        return arena;
    }

    public ChestManager getChestManager() {
        return chestManager;
    }

    public GameEventListener getEventListener() {
        return eventListener;
    }

    public GameState getGameState() {
        return gameState;
    }

    public Map<String, Boolean> getWorldLoadStates() {
        return worldLoadStates;
    }

    public List<FireworkWarsTeam> getTeams() {
        return teams;
    }

    public List<TeamPlayer> getPlayers() {
        return players;
    }

    public boolean isPlaying() {
        return gameState == GameState.PLAYING;
    }

    public boolean isWaiting() {
        return gameState == GameState.WAITING;
    }

    public boolean isStarting() {
        return gameState == GameState.STARTING;
    }

    public boolean isResetting() {
        return gameState == GameState.RESETTING;
    }

    public boolean isAlive(Player player) {
        return gameState == GameState.PLAYING && containsPlayer(player) && TeamPlayer.from(player.getUniqueId()).isAlive();
    }

    public boolean isSpectator(Player player) {
        return TeamPlayer.from(player.getUniqueId()).isSpectator();
    }

    public boolean containsPlayer(Player player) {
        TeamPlayer teamPlayer = TeamPlayer.from(player.getUniqueId());
        return players.contains(teamPlayer);
    }

    public boolean usesWorld(String worldName) {
        return arena.getWorlds().contains(worldName);
    }

    public List<FireworkWarsTeam> getRemainingTeams() {
        return teams
            .stream()
            .filter(team -> !team.isEliminated())
            .toList();
}

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public FireworkWarsGame(FireworkWarsPlugin plugin, Arena arena) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.customItemManager = plugin.getCustomItemManager();

        this.arena = arena;
        this.chestManager = new ChestManager(plugin, this);

        this.eventListener = new GameEventListener(plugin, this);

        this.houseKeepingListener = new HouseKeepingListener(plugin, this);
        this.connectionListener = new PlayerConnectionListener(plugin, this);

        houseKeepingListener.register();
        connectionListener.register();

        loopThroughWorlds(world -> worldLoadStates.put(world.getName(), true));
    }

    public void addPlayer(Player player) {
        TeamPlayer teamPlayer = new TeamPlayer(player.getUniqueId(), this);

        players.add(teamPlayer);
        teamPlayer.teleportToWaitingArea();

        if (isWaiting() && players.size() >= arena.getMinimumPlayerCount()) {
            startCountdown();
        }

        player.setGameMode(GameMode.ADVENTURE);
        plugin.getHealCommand().healPlayer(player);

        sendMessage(Message.ARENA_JOIN, player.displayName(), players.size(), arena.getMaximumPlayerCount());
    }

    public void removePlayer(@NotNull TeamPlayer player) {
        player.unregister(true);

        if (players.size() < arena.getMinimumPlayerCount()) {
            if (countdown != null) {
                countdown.cancel();
                gameState = GameState.WAITING;
            }
        }

        player.teleportToLobby();
        sendMessage(Message.ARENA_LEAVE, player.getPlayer().displayName(), players.size(), arena.getMaximumPlayerCount());
    }

    public void sendMessage(Message message, Object... arguments) {
        for (TeamPlayer player : players) {
            languageManager.sendMessage(message, player.getPlayer(), arguments);
        }
    }

    public void playSound(Sound sound) {
        players.forEach(player -> player.playSound(sound));
    }

    private void startCountdown() {
        countdown = new GameCountdown(plugin, this);
        countdown.start();
    }

    public void startGame() {
        gameState = GameState.PLAYING;
        eventListener.register();

        tickHandler = new GameTickHandler(plugin, this);
        tickHandler.start();

        chestManager.refillChests(1.0D);

        createWorldBorder();
        clearDroppedItems();

        for (TeamData teamData : arena.getTeamInformation()) {
            teams.add(new FireworkWarsTeam(teamData, this, plugin));
        }

        for (TeamPlayer player : getPlayers()) {
            plugin.getResetInventoryCommand().giveItems(player.getPlayer());
            plugin.getHealCommand().healPlayer(player.getPlayer());
        }

        distributePlayersAcrossTeams();
        players.forEach(TeamPlayer::showScoreboard);
        players.forEach(TeamPlayer::showWorldBorder);
    }

    private void createWorldBorder() {
        WorldBorderData borderData = arena.getWorldBorderInformation();

        loopThroughWorlds(world -> {
            WorldBorder border = world.getWorldBorder();

            border.setCenter(borderData.getCenter(world));
            border.setSize(borderData.getDiameter());
            border.setDamageAmount(1.0D);
            border.setDamageBuffer(1.0D);
            border.setWarningDistance(8);
        });
    }

    private void clearDroppedItems() {
        loopThroughWorlds(world -> world.getEntitiesByClass(Item.class).forEach(Entity::remove));
    }

    public void preEndGame(@Nullable FireworkWarsTeam winningTeam) {
        if (winningTeam != null) {
            sendMessage(Message.TEAM_WON, winningTeam.getColoredTeamName());
        } else {
            sendMessage(Message.DRAW);
        }

        players.forEach(TeamPlayer::stopSpectating);
        players.forEach(TeamPlayer::becomeSpectator);
        players.forEach(teamPlayer -> teamPlayer.getPlayer().getInventory().clear());

        for (TeamPlayer teamPlayer : players) {
            Player player = teamPlayer.getPlayer();
            Title title;

            if (teamPlayer.getTeam().equals(winningTeam)) {
                title = title(languageManager.getMessage(Message.YOU_WIN, player), Component.empty());
                teamPlayer.playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
            } else if (winningTeam == null) {
                title = title(languageManager.getMessage(Message.DRAW, player), Component.empty());
            } else {
                title = title(languageManager.getMessage(Message.YOU_LOSE, player), Component.empty());
            }

            player.sendTitlePart(TitlePart.TITLE, title.title());
        }

        plugin.runTaskLater(this::endGame, 20 * 10L);
    }

    public void preEndGame() {
        int mostRemainingPlayers = getRemainingTeams().stream()
            .mapToInt(FireworkWarsTeam::getRemainingPlayerCount)
            .max()
            .orElse(0);

        if (mostRemainingPlayers == 0) {
            preEndGame(null);
            return;
        }

        List<FireworkWarsTeam> winningTeams = getRemainingTeams().stream()
            .filter(team -> team.getRemainingPlayerCount() == mostRemainingPlayers)
            .toList();

        if (winningTeams.size() == 1) {
            preEndGame(winningTeams.get(0));
        } else {
            preEndGame(null);
        }
    }

    public void endGame() {
        eventListener.unregister();
        tickHandler.cancel();

        connectionListener.getDisconnectedPlayers().values().forEach(teamPlayer -> teamPlayer.unregister(false));
        connectionListener.getDisconnectedPlayers().clear();
        connectionListener.getDiedFromDisconnect().clear();

        tasks.stream()
            .filter(Predicate.not(BukkitTask::isCancelled))
            .forEach(BukkitTask::cancel);
        tasks.clear();

        chestManager.getSupplyDropMinecarts().clear();

        for (TeamPlayer player : players) {
            plugin.getHealCommand().healPlayer(player.getPlayer());
        }

        players.forEach(TeamPlayer::teleportToLobby);
        players.forEach(player -> player.unregister(false));

        teams.forEach(team -> team.getPlayers().clear());

        loopThroughWorlds(world ->
            world.getPlayers().forEach(player ->
                player.teleport(plugin.getArenaManager().getFirstLobbySpawnLocation())));

        teams.clear();
        players.clear();

        gameState = GameState.RESETTING;
        plugin.runTaskLater(this::resetMap, 1L);
    }

    private void resetMap() {
        loopThroughWorlds(this::resetWorld);
    }

    private void resetWorld(World world) {
        worldLoadStates.put(world.getName(), false);
        Bukkit.unloadWorld(world, false);

        World newWorld = Bukkit.createWorld(new WorldCreator(world.getName()));
        assert newWorld != null;

        newWorld.setAutoSave(false);
        newWorld.getWorldBorder().reset();
    }

    private void distributePlayersAcrossTeams() {
        Collections.shuffle(players);

        for (int i = 0; i < players.size(); i++) {
            int teamIndex = i % teams.size();

            FireworkWarsTeam team = teams.get(teamIndex);
            players.get(i).joinTeam(team);
        }
    }

    public void supplyDrop() {
        Location location = arena.getSupplyDropData().getRandomLocation().getBukkitLocation();
        StorageMinecart chestMinecart = (StorageMinecart) location.getWorld().spawnEntity(location, EntityType.CHEST_MINECART);

        chestMinecart.customName(Component.text("Supply Drop"));

        int slot = randomInt(0, chestMinecart.getInventory().getSize() - 2);

        chestMinecart.getInventory().setItem(
            slot, customItemManager.getItem("rocket_launcher").getItem(null));
        chestMinecart.getInventory().setItem(
            slot + 1, customItemManager.getItem("rocket_launcher_ammo").getItem(null, 2));

        Location fireworkLocation = location.getWorld().getHighestBlockAt(location).getLocation();

        runTaskLater(() -> sendSupplyDropFireworks(fireworkLocation), 1L);
        runTaskLater(() -> sendSupplyDropFireworks(fireworkLocation), 20L);
        runTaskLater(() -> sendSupplyDropFireworks(fireworkLocation), 40L);

        sendMessage(Message.EVENT_SUPPLY_DROP, location.getBlockX(), fireworkLocation.getBlockY(), location.getBlockZ());

        runTaskLater(() -> {
            if (chestMinecart.isValid()) {
                chestManager.getSupplyDropMinecarts().add(chestMinecart);
            }
        }, 20L * 30);
    }

    private void sendSupplyDropFireworks(Location location) {
        for (int i = 0; i < 5; i++) {
            location.getWorld().spawn(location, Firework.class, firework -> {
                firework.setFireworkMeta(addRandomFireworkEffect(firework.getFireworkMeta()));
                firework.setTicksToDetonate(randomInt(30, 32));

                firework.setNoPhysics(true);
            });
        }
    }

    private FireworkMeta addRandomFireworkEffect(FireworkMeta meta) {
        FireworkEffect.Type type = Util.randomElement(
            List.of(FireworkEffect.Type.BURST, FireworkEffect.Type.STAR));

        Color fade = Util.randomElement(
            List.of(Color.BLUE, Color.PURPLE, Color.AQUA));

        meta.addEffect(FireworkEffect.builder()
            .with(type)
            .withColor(Color.WHITE)
            .withFade(fade)
            .build());

        return meta;
    }

    public void startEndgame() {
        sendMessage(Message.EVENT_ENDGAME);
        playSound(Sound.ENTITY_ENDER_DRAGON_GROWL);

        WorldBorderData borderData = arena.getWorldBorderInformation();

        loopThroughWorlds(world -> world.getWorldBorder().setSize(
            borderData.getEndgameDiameter(),
            borderData.getSecondsToReachEndgameRadius()));
    }

    public void eliminateTeam(FireworkWarsTeam team) {
        sendMessage(Message.TEAM_ELIMINATED, team.getColoredTeamName());
    }

    public void runTaskLater(Runnable runnable, long delay) {
        BukkitTask task = plugin.runTaskLater(runnable, delay);
        tasks.add(task);
    }

    private void loopThroughWorlds(Consumer<World> consumer) {
        for (String worldName : arena.getWorlds()) {
            World world = Bukkit.getWorld(worldName);
            assert world != null;

            consumer.accept(world);
        }
    }

    public enum GameState {
        WAITING,
        STARTING,
        PLAYING,
        RESETTING
    }
}
