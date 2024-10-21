package foundation.esoteric.minecraft.plugins.games.fireworkwars.game.runnables;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.data.EndgameData;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.data.SupplyDropData;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.structure.Arena;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.FireworkWarsGame;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.TeamPlayer;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.LanguageManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.scoreboard.wrapper.FireworkWarsScoreboard;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Pair;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Util;

@SuppressWarnings("FieldCanBeLocal")
public class GameTickHandler extends BukkitRunnable {
    private final FireworkWarsPlugin plugin;
    private final LanguageManager languageManager;

    private final FireworkWarsGame game;
    private final Arena arena;

    private final SupplyDropData supplyDropData;
    private final EndgameData endgameData;

    private final int second = 20;

    private int ticksElapsed;
    private int ticksUntilSupplyDrop;

    private final int chestRefillInterval;
    private int totalChestRefills;

    private boolean endgameStarted;

    public GameTickHandler(FireworkWarsPlugin plugin, FireworkWarsGame game) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();

        this.game = game;
        this.arena = game.getArena();

        this.chestRefillInterval = arena.getChestRefillIntervalTicks();
        this.totalChestRefills = 0;

        this.supplyDropData = arena.getSupplyDropData();
        this.endgameData = arena.getEndgameData();
    }

    public void start() {
        init();
        runTaskTimer(plugin, 1L, 1L);
    }

    @Override
    public void run() {
        ticksElapsed++;

        if (ticksUntilSupplyDrop-- <= second) {
            handleSupplyDrops();
        }

        if (ticksElapsed >= endgameData.getEndgameStartTicks() - second && !endgameStarted) {
            startEndgame();
        }

        if (ticksElapsed >= arena.getGameDurationTicks()) {
            game.preEndGame();
            return;
        }

        if (totalChestRefills < 10) {
            if (ticksElapsed % chestRefillInterval == chestRefillInterval - (11 * second)) {
                game.sendMessage(Message.EVENT_CHEST_REFILL_WARNING, 10);
            }

            if (ticksElapsed % chestRefillInterval == 0) {
                handleChestRefill();
            }
        }

        updateScoreboards();
    }

    private void init() {
        ticksElapsed = 0;
        ticksUntilSupplyDrop = getNextSupplyDropTicks();
    }

    private int getNextSupplyDropTicks() {
        int interval = supplyDropData.getSupplyDropIntervalTicks();
        int randomness = supplyDropData.getSupplyDropIntervalRandomness();

        return Math.max(1, interval + Util.randomInt(-randomness, randomness));
    }

    private void handleChestRefill() {
        totalChestRefills++;
        game.getChestManager().refillChests(1.0D + totalChestRefills / 10.0D);

        game.sendMessage(Message.EVENT_CHEST_REFILL);
        game.playSound(Sound.BLOCK_CHEST_OPEN);
    }

    private void handleSupplyDrops() {
        game.supplyDrop();
        ticksUntilSupplyDrop = getNextSupplyDropTicks();
    }

    private void startEndgame() {
        endgameStarted = true;
        game.startEndgame();
    }

    private String getMinutesAndSeconds(int ticks) {
        int minutes = ticks / 1200;
        int seconds = (ticks % 1200) / 20;
        boolean soon = startsSoon(ticks);

        if (minutes == 0 && soon) {
            return String.format("%02d", seconds) + "s";
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    private boolean shouldWarnForEndgame() {
        return !endgameStarted && getTicksUntilEndgame() < endgameData.getWarnBeforeEndgameTicks();
    }

    private boolean shouldWarnForGameEnd() {
        return endgameStarted && getTicksUntilGameEnd() < endgameData.getWarnBeforeGameEndTicks();
    }

    private int getTicksUntilEndgame() {
        return endgameData.getEndgameStartTicks() - ticksElapsed;
    }

    private int getTicksUntilGameEnd() {
        return arena.getGameDurationTicks() - ticksElapsed;
    }

    private boolean startsSoon(int ticks) {
        return ticks < 11 * 20;
    }

    private void updateScoreboards() {
        for (TeamPlayer teamPlayer : game.getPlayers()) {
            FireworkWarsScoreboard scoreboard = teamPlayer.getScoreboard();
            Player player = teamPlayer.getPlayer();

            scoreboard.updateLine(4, Pair.of("%", teamPlayer.getKills() + ""));
            scoreboard.updateLine(5, Pair.of("%", (int) (teamPlayer.getDamage()) + ""));

            scoreboard.setIncludeSecondEventLine(false);

            if (startsSoon(ticksUntilSupplyDrop)) {
                scoreboard.setLine(1, languageManager.getMessage(
                        Message.SB_EVENT_SUPPLY_DROP_SOON, player, getMinutesAndSeconds(ticksUntilSupplyDrop)));
            } else {
                scoreboard.setLine(1, languageManager.getMessage(
                        Message.SB_EVENT_SUPPLY_DROP, player, getMinutesAndSeconds(ticksUntilSupplyDrop)));
            }

            if (shouldWarnForEndgame()) {
                scoreboard.setIncludeSecondEventLine(true);
                int ticks = getTicksUntilEndgame();

                if (startsSoon(ticks)) {
                    scoreboard.setEndgameLine(languageManager.getMessage(
                            Message.SB_EVENT_ENDGAME_SOON, player, getMinutesAndSeconds(getTicksUntilEndgame())));
                } else {
                    scoreboard.setEndgameLine(languageManager.getMessage(
                            Message.SB_EVENT_ENDGAME, player, getMinutesAndSeconds(getTicksUntilEndgame())));
                }
            }

            if (shouldWarnForGameEnd()) {
                scoreboard.setIncludeSecondEventLine(true);
                int ticks = getTicksUntilGameEnd();

                if (startsSoon(ticks)) {
                    scoreboard.setEndgameLine(languageManager.getMessage(
                            Message.SB_EVENT_GAME_END_SOON, player, getMinutesAndSeconds(getTicksUntilGameEnd())));
                } else {
                    scoreboard.setEndgameLine(languageManager.getMessage(
                            Message.SB_EVENT_GAME_END, player, getMinutesAndSeconds(getTicksUntilGameEnd())));
                }
            }

            scoreboard.update();
        }
    }
}
