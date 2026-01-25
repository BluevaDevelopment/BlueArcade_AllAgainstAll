package net.blueva.arcade.modules.allagainstall.state;

import net.blueva.arcade.api.game.GameContext;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ArenaState {

    private final GameContext<Player, Location, World, Material, ItemStack, Sound, Block, Entity> context;
    private final Map<UUID, Integer> playerKills = new ConcurrentHashMap<>();

    private UUID winnerId;
    private boolean ended;
    private int supplyTicks;

    public ArenaState(GameContext<Player, Location, World, Material, ItemStack, Sound, Block, Entity> context) {
        this.context = context;
    }

    public GameContext<Player, Location, World, Material, ItemStack, Sound, Block, Entity> getContext() {
        return context;
    }

    public int getArenaId() {
        return context.getArenaId();
    }

    public void initializePlayer(UUID playerId) {
        playerKills.putIfAbsent(playerId, 0);
    }

    public int addKill(UUID playerId) {
        return playerKills.merge(playerId, 1, Integer::sum);
    }

    public int getKills(UUID playerId) {
        return playerKills.getOrDefault(playerId, 0);
    }

    public Map<UUID, Integer> getKillSnapshot() {
        return new ConcurrentHashMap<>(playerKills);
    }

    public boolean markEnded() {
        boolean wasEnded = ended;
        ended = true;
        return wasEnded;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setWinner(UUID winnerId) {
        this.winnerId = winnerId;
    }

    public UUID getWinnerId() {
        return winnerId;
    }

    public int incrementSupplyTicks(int increment) {
        supplyTicks += increment;
        return supplyTicks;
    }

    public void resetSupplyTicks() {
        supplyTicks = 0;
    }
}
