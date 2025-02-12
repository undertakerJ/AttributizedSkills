package net.lumi_noble.attributizedskills.common.commands.common;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.lumi_noble.attributizedskills.common.config.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class RemoveRequirementsCommand {

    // Регистрируем команду: /attributized addrequirements <player> <requirements...>
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("remove_requirements")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                                .executes(RemoveRequirementsCommand::execute)

                );
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("command.remove_requirements.failure.no_item"));
            return 0;
        }

        String itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString();
        removeConfig(itemId);
        context.getSource().sendSuccess(Component.translatable("command.remove_requirements.success" + itemId), true);

        return 1;
    }
    private static void removeConfig(String itemId) {
        java.util.List<String> currentList = new java.util.ArrayList<>(Config.OVERRIDE_SKILL_LOCKS.get());
        String entryToRemove = null;
        for (String entry : currentList) {
            if (entry.startsWith(itemId)) {
                entryToRemove = entry;
                break;
            }
        }
        if (entryToRemove != null) {
            currentList.remove(entryToRemove);
        }
        Config.OVERRIDE_SKILL_LOCKS.set(currentList);
        Config.getConfig().save();
        Config.load();
    }

}
