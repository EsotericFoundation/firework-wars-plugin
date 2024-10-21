package foundation.esoteric.minecraft.plugins.games.fireworkwars.game.runnables;

import org.bukkit.scheduler.BukkitRunnable;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.FireworkWarsGame;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.FireworkWarsGame.GameState;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;

public class GameCountdown extends BukkitRunnable {

    private final FireworkWarsPlugin plugin;
    private final FireworkWarsGame game;

    private int countDownSeconds;

    public GameCountdown(FireworkWarsPlugin plugin, FireworkWarsGame game) {
        super();
        this.plugin = plugin;
        this.game = game;

        this.countDownSeconds = game.getArena().getCountDownSeconds();
    }

    public void start() {
        game.setGameState(GameState.STARTING);
        runTaskTimer(plugin, 0, 20);
    }

    @Override
    public void run() {
        if (countDownSeconds == 0) {
            game.sendMessage(Message.GAME_STARTED);
        } else if (countDownSeconds == 1) {
            game.sendMessage(Message.GAME_STARTING_IN_TIME_SINGULAR, countDownSeconds);
        } else {
            game.sendMessage(Message.GAME_STARTING_IN_TIME_PLURAL, countDownSeconds);
        }

        if (countDownSeconds-- == 0) {
            cancel();
            game.startGame();
        }
    }
}
