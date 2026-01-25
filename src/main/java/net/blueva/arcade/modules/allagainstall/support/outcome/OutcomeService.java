package net.blueva.arcade.modules.allagainstall.support.outcome;

import net.blueva.arcade.api.game.GameContext;
import net.blueva.arcade.api.module.ModuleInfo;
import net.blueva.arcade.api.stats.StatsAPI;
import net.blueva.arcade.modules.allagainstall.game.AllAgainstAllGame;
import net.blueva.arcade.modules.allagainstall.state.ArenaState;
import net.blueva.arcade.modules.allagainstall.support.PlaceholderService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OutcomeService {

    private final ModuleInfo moduleInfo;
    private final StatsAPI statsAPI;
    private final AllAgainstAllGame game;
    private final PlaceholderService placeholderService;

    public OutcomeService(ModuleInfo moduleInfo,
                          StatsAPI statsAPI,
                          AllAgainstAllGame game,
                          PlaceholderService placeholderService) {
        this.moduleInfo = moduleInfo;
        this.statsAPI = statsAPI;
        this.game = game;
        this.placeholderService = placeholderService;
    }

    public void endGame(GameContext<Player, Location, World, Material, ItemStack, Sound, Block, Entity> context,
                        ArenaState state) {
        if (state.markEnded()) {
            return;
        }

        context.getSchedulerAPI().cancelArenaTasks(context.getArenaId());

        List<Player> alivePlayers = new ArrayList<>(context.getAlivePlayers());
        String winMode = game.getWinMode(context);
        if (alivePlayers.size() == 1 && "last_standing".equals(winMode)) {
            Player winner = alivePlayers.getFirst();
            declareWinner(state, winner);
        } else if (alivePlayers.size() > 1 && "last_standing".equals(winMode)) {
            handleLastStandingTimeout(context);
        }

        if ("most_kills".equals(winMode)) {
            handleMostKillsOutcome(context);
        }

        context.endGame();
    }

    private void handleMostKillsOutcome(
            GameContext<Player, Location, World, Material, ItemStack, Sound, Block, Entity> context) {
        List<Player> topPlayers = placeholderService.getPlayersSortedByKills(
                context, context.getPlayers(), context.getPlayers().size());
        if (topPlayers.isEmpty()) {
            return;
        }

        Player winner = topPlayers.getFirst();
        declareWinner(game.getArenaState(context), winner);

        for (int i = 1; i < topPlayers.size(); i++) {
            Player player = topPlayers.get(i);
            if (!context.isPlayerPlaying(player)) {
                continue;
            }
            context.finishPlayer(player);
        }
    }

    private void handleLastStandingTimeout(
            GameContext<Player, Location, World, Material, ItemStack, Sound, Block, Entity> context) {
        List<Player> sortedByKills = placeholderService.getPlayersSortedByKills(
                context, new ArrayList<>(context.getAlivePlayers()), context.getAlivePlayers().size());
        if (sortedByKills.isEmpty()) {
            return;
        }

        Player winner = sortedByKills.getFirst();
        declareWinner(game.getArenaState(context), winner);

        for (int i = 1; i < sortedByKills.size(); i++) {
            Player player = sortedByKills.get(i);
            if (!context.isPlayerPlaying(player)) {
                continue;
            }
            context.finishPlayer(player);
        }
    }

    private void declareWinner(ArenaState state, Player winner) {
        GameContext<Player, Location, World, Material, ItemStack, Sound, Block, Entity> context = state.getContext();
        context.setWinner(winner);
        handleWinStats(state, winner);
    }

    private void handleWinStats(ArenaState state, Player winner) {
        if (statsAPI == null) {
            return;
        }

        UUID winnerId = state.getWinnerId();
        if (winnerId != null) {
            return;
        }

        state.setWinner(winner.getUniqueId());
        statsAPI.addModuleStat(winner, moduleInfo.getId(), "wins", 1);
        statsAPI.addGlobalStat(winner, "wins", 1);
    }
}
