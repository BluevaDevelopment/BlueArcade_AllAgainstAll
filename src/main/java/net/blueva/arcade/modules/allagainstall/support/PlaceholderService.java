package net.blueva.arcade.modules.allagainstall.support;

import net.blueva.arcade.api.config.ModuleConfigAPI;
import net.blueva.arcade.api.game.GameContext;
import net.blueva.arcade.modules.allagainstall.game.AllAgainstAllGame;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceholderService {

    private final ModuleConfigAPI moduleConfig;
    private final AllAgainstAllGame game;

    public PlaceholderService(ModuleConfigAPI moduleConfig, AllAgainstAllGame game) {
        this.moduleConfig = moduleConfig;
        this.game = game;
    }

    public Map<String, String> buildPlaceholders(Player player) {
        Map<String, String> placeholders = new HashMap<>();

        GameContext<Player, Location, World, Material, ItemStack, Sound, Block, Entity> context = game.getContext(player);
        if (context != null) {
            placeholders.put("alive", String.valueOf(context.getAlivePlayers().size()));
            placeholders.put("spectators", String.valueOf(context.getSpectators().size()));
            placeholders.put("kills", String.valueOf(game.getPlayerKills(context, player)));
            placeholders.put("mode", game.getModeLabel(game.getWinMode(context)));
            if ("most_kills".equals(game.getWinMode(context))) {
                List<Player> topPlayers = getTopPlayersByKills(context);
                for (int i = 0; i < 5; i++) {
                    String placeKey = "place_" + (i + 1);
                    String killsKey = "kills_" + (i + 1);
                    if (topPlayers.size() > i) {
                        Player topPlayer = topPlayers.get(i);
                        placeholders.put(placeKey, topPlayer.getName());
                        placeholders.put(killsKey, String.valueOf(game.getPlayerKills(context, topPlayer)));
                    } else {
                        placeholders.put(placeKey, "-");
                        placeholders.put(killsKey, "0");
                    }
                }
            }
        }

        return placeholders;
    }

    public Player getTopKiller(GameContext<Player, Location, World, Material, ItemStack, Sound, Block, Entity> context) {
        List<Player> topPlayers = getTopPlayersByKills(context);
        if (topPlayers.isEmpty()) {
            return null;
        }
        return topPlayers.getFirst();
    }

    public List<Player> getPlayersSortedByKills(
            GameContext<Player, Location, World, Material, ItemStack, Sound, Block, Entity> context,
            List<Player> players,
            int limit) {
        Map<Player, Integer> killCounts = new HashMap<>();
        for (Player player : players) {
            if (player == null || !player.isOnline()) {
                continue;
            }
            killCounts.put(player, game.getPlayerKills(context, player));
        }

        List<Map.Entry<Player, Integer>> sorted = new ArrayList<>(killCounts.entrySet());
        sorted.sort((a, b) -> {
            int compare = Integer.compare(b.getValue(), a.getValue());
            if (compare != 0) {
                return compare;
            }
            return a.getKey().getName().compareToIgnoreCase(b.getKey().getName());
        });

        List<Player> orderedPlayers = new ArrayList<>();
        for (Map.Entry<Player, Integer> entry : sorted) {
            orderedPlayers.add(entry.getKey());
            if (orderedPlayers.size() >= limit) {
                break;
            }
        }

        return orderedPlayers;
    }

    private List<Player> getTopPlayersByKills(
            GameContext<Player, Location, World, Material, ItemStack, Sound, Block, Entity> context) {
        return getPlayersSortedByKills(context, context.getPlayers(), 5);
    }

    public ModuleConfigAPI getModuleConfig() {
        return moduleConfig;
    }
}
