package net.blueva.arcade.modules.allagainstall.support;

import net.blueva.arcade.api.config.ModuleConfigAPI;
import net.blueva.arcade.api.game.GameContext;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DescriptionService {

    private final ModuleConfigAPI moduleConfig;

    public DescriptionService(ModuleConfigAPI moduleConfig) {
        this.moduleConfig = moduleConfig;
    }

    public void sendDescription(GameContext<Player, Location, World, Material, ItemStack, Sound, Block, Entity> context) {
        String winMode = getWinMode(context);
        String descriptionKey = "description." + winMode;
        List<String> description = moduleConfig.getStringListFrom("language.yml", descriptionKey);
        if (description == null || description.isEmpty()) {
            description = moduleConfig.getStringListFrom("language.yml", "description");
        }

        for (Player player : context.getPlayers()) {
            for (String line : description) {
                context.getMessagesAPI().sendRaw(player, line);
            }
        }
    }

    private String getWinMode(GameContext<Player, Location, World, Material, ItemStack, Sound, Block, Entity> context) {
        String mode = context.getDataAccess().getGameData("basic.win_mode", String.class);
        if (mode == null) {
            return "last_standing";
        }
        mode = mode.toLowerCase();
        if (!mode.equals("last_standing") && !mode.equals("most_kills")) {
            return "last_standing";
        }
        return mode;
    }
}
