package foundation.esoteric.minecraft.plugins.games.fireworkwars.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.commandsenders.BukkitPlayer;
import dev.jorel.commandapi.executors.ExecutionInfo;
import org.bukkit.World;
import org.bukkit.entity.Player;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.structure.Arena;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.manager.ArenaManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.FireworkWarsGame;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.GameManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.TeamPlayer;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class ArenaCommand extends CommandAPICommand {
    private final String arenaNumberNodeName = "arena-number";

    private final FireworkWarsPlugin plugin;

    private final ArenaManager arenaManager;
    private final GameManager gameManager;

    public ArenaCommand(FireworkWarsPlugin plugin) {
        super("arena");

        this.plugin = plugin;

        this.arenaManager = plugin.getArenaManager();
        this.gameManager = plugin.getGameManager();

        withSubcommand(
            new CommandAPICommand("join")
                .withArguments(this.getArenaArgument())
                .executesPlayer(this::onPlayerJoinExecution));

        withSubcommand(
            new CommandAPICommand("leave")
                .executesPlayer(this::onPlayerLeaveExecution)
        );

        register(plugin);
    }

    private Argument<Integer> getArenaArgument() {
        return new IntegerArgument(arenaNumberNodeName)
            .includeSuggestions(ArgumentSuggestions.strings((suggestionsInfo) -> {
                List<String> suggestions = new ArrayList<>();

                Predicate<Arena> hasOngoingGame = gameManager::hasOngoingGame;
                Function<Integer, Integer> increment = i -> ++i;

                List<Arena> arenas = arenaManager.getArenas();

                arenas.stream()
                    .filter(hasOngoingGame.negate())
                    .map(arenas::indexOf).map(increment)
                    .map(String::valueOf)
                    .forEach(suggestions::add);

                return suggestions.toArray(String[]::new);
        }));
    }

    private void onPlayerJoinExecution(ExecutionInfo<Player, BukkitPlayer> info) {
        Player player = info.sender();
        Integer arenaNumber = (Integer) info.args().get(arenaNumberNodeName);

        List<Arena> arenas = arenaManager.getArenas();

        if (arenaNumber == null || arenaNumber <= 0 || arenaNumber - 1 >= arenas.size()) {
            plugin.getLanguageManager().sendMessage(Message.INVALID_ARENA, player);
            return;
        }

        Arena arena = arenas.get(arenaNumber - 1);
        FireworkWarsGame game = gameManager.getFireworkWarsGame(arena);

        World world = player.getWorld();

        if (!arenaManager.isLobby(world) && !arenaManager.isArena(world)) {
            plugin.getLanguageManager().sendMessage(Message.NOT_IN_LOBBY, player);
            return;
        }

        if (game.isPlaying()) {
            plugin.getLanguageManager().sendMessage(Message.GAME_ALREADY_PLAYING, player);
            return;
        }

        if (game.containsPlayer(player)) {
            plugin.getLanguageManager().sendMessage(Message.GAME_ALREADY_CONTAINS_PLAYER, player);
            return;
        }

        if (game.isResetting()) {
            plugin.getLanguageManager().sendMessage(Message.GAME_RELOADING, player);
            return;
        }

        game.addPlayer(player);
    }

    private void onPlayerLeaveExecution(@NotNull ExecutionInfo<Player, BukkitPlayer> info) {
        Player player = info.sender();
        TeamPlayer teamPlayer = TeamPlayer.from(player);

        FireworkWarsGame game = gameManager.getFireworkWarsGame(player);

        if (game == null) {
            plugin.getLanguageManager().sendMessage(Message.NOT_IN_GAME, player);
            return;
        }

        game.removePlayer(teamPlayer);
    }
}
