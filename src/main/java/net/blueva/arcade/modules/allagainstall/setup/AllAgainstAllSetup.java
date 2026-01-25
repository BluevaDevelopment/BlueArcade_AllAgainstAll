package net.blueva.arcade.modules.allagainstall.setup;

import net.blueva.arcade.api.setup.GameSetupHandler;
import net.blueva.arcade.api.setup.SetupContext;
import net.blueva.arcade.api.setup.SetupDataAPI;
import net.blueva.arcade.api.setup.TabCompleteContext;
import net.blueva.arcade.api.setup.TabCompleteResult;
import net.blueva.arcade.modules.allagainstall.AllAgainstAllModule;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class AllAgainstAllSetup implements GameSetupHandler {

    private final AllAgainstAllModule module;

    public AllAgainstAllSetup(AllAgainstAllModule module) {
        this.module = module;
    }

    @Override
    public boolean handle(SetupContext context) {
        return handleInternal(castSetupContext(context));
    }

    private boolean handleInternal(SetupContext<Player, CommandSender, Location> context) {
        String subcommand = context.getArg(context.getStartIndex() - 1);
        if (subcommand == null) {
            context.getMessagesAPI().send(context.getPlayer(),
                    module.getModuleConfig().getStringFrom("language.yml", "setup_messages.usage"));
            return true;
        }

        subcommand = subcommand.toLowerCase();

        if ("setmode".equals(subcommand)) {
            return handleSetMode(context);
        }

        if ("setregion".equals(subcommand)) {
            return handleSetRegion(context);
        }

        context.getMessagesAPI().send(context.getPlayer(),
                module.getCoreConfig().getLanguage("admin_commands.errors.unknown_subcommand"));
        return true;
    }

    @Override
    public TabCompleteResult tabComplete(TabCompleteContext context) {
        return tabCompleteInternal(castTabContext(context));
    }

    private TabCompleteResult tabCompleteInternal(TabCompleteContext<Player, CommandSender> context) {
        int relIndex = context.getRelativeArgIndex();

        if (relIndex == 0 && "setmode".equals(context.getArg(context.getStartIndex() - 1))) {
            return TabCompleteResult.of("last_standing", "most_kills");
        }

        return TabCompleteResult.empty();
    }

    @Override
    public List<String> getSubcommands() {
        return Arrays.asList("setmode", "setregion");
    }

    @Override
    public boolean validateConfig(SetupContext context) {
        return validateConfigInternal(castSetupContext(context));
    }

    private boolean validateConfigInternal(SetupContext<Player, CommandSender, Location> context) {
        SetupDataAPI data = context.getData();
        if (!data.has("basic.win_mode")) {
            data.setString("basic.win_mode", "last_standing");
        }

        data.save();
        return true;
    }

    private boolean handleSetMode(SetupContext<Player, CommandSender, Location> context) {
        if (!context.hasHandlerArgs(1)) {
            context.getMessagesAPI().send(context.getPlayer(),
                    module.getModuleConfig().getStringFrom("language.yml", "setup_messages.usage_setmode"));
            return true;
        }

        String mode = context.getHandlerArg(0).toLowerCase();
        if (!mode.equals("last_standing") && !mode.equals("most_kills")) {
            context.getMessagesAPI().send(context.getPlayer(),
                    module.getModuleConfig().getStringFrom("language.yml", "setup_messages.usage_setmode"));
            return true;
        }

        context.getData().setString("basic.win_mode", mode);
        context.getData().save();

        context.getMessagesAPI().send(context.getPlayer(),
                module.getModuleConfig().getStringFrom("language.yml", "setup_messages.mode_set")
                        .replace("{mode}", mode));
        return true;
    }

    private boolean handleSetRegion(SetupContext<Player, CommandSender, Location> context) {
        Player player = context.getPlayer();

        if (!context.getSelection().hasCompleteSelection(player)) {
            context.getMessagesAPI().send(player,
                    module.getModuleConfig().getStringFrom("language.yml", "setup_messages.must_use_stick"));
            return true;
        }

        Location pos1 = context.getSelection().getPosition1(player);
        Location pos2 = context.getSelection().getPosition2(player);

        context.getData().registerRegenerationRegion("game.play_area", pos1, pos2);
        context.getData().save();

        int x = (int) Math.abs(pos2.getX() - pos1.getX()) + 1;
        int y = (int) Math.abs(pos2.getY() - pos1.getY()) + 1;
        int z = (int) Math.abs(pos2.getZ() - pos1.getZ()) + 1;
        int blocks = x * y * z;

        context.getMessagesAPI().send(player,
                module.getModuleConfig().getStringFrom("language.yml", "setup_messages.region_set")
                        .replace("{blocks}", String.valueOf(blocks))
                        .replace("{x}", String.valueOf(x))
                        .replace("{y}", String.valueOf(y))
                        .replace("{z}", String.valueOf(z)));
        return true;
    }


    @SuppressWarnings("unchecked")
    private SetupContext<Player, CommandSender, Location> castSetupContext(SetupContext context) {
        return (SetupContext<Player, CommandSender, Location>) context;
    }

    @SuppressWarnings("unchecked")
    private TabCompleteContext<Player, CommandSender> castTabContext(TabCompleteContext context) {
        return (TabCompleteContext<Player, CommandSender>) context;
    }
}
