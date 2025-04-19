package net.lumi_noble.attributizedskills.common.commands.common;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import java.util.ArrayList;
import java.util.List;
import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import shadows.apotheosis.adventure.loot.LootRarity;

public class RemoveApothRequirementCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register(){
    return Commands.literal("remove_apoth_requirement")
        .then(
            Commands.argument("rarity", ResourceLocationArgument.id())
                .suggests(
                    (ctx, builder) -> {
                      for (LootRarity rarity : LootRarity.LIST) {
                        ResourceLocation id = new ResourceLocation(rarity.id());
                        if (id != null) {
                          builder.suggest(id.toString());
                        }
                      }
                      return builder.buildFuture();
                    })
                .executes(RemoveApothRequirementCommand::execute));
    }
    private static int execute(CommandContext<CommandSourceStack> context) {
        ResourceLocation rarityId = ResourceLocationArgument.getId(context, "rarity");
        String translatedKey = "rarity.apoth." + rarityId.getPath();
        List<String> currentList = new ArrayList<>(ASConfig.APOTH_RARITY_REQUIREMENTS.get());
        boolean removedEntry = currentList.removeIf(s -> s.startsWith(rarityId.toString()));
        if(removedEntry){
            ASConfig.APOTH_RARITY_REQUIREMENTS.set(currentList);
            ASConfig.getConfig().save();
            ASConfig.loadApothRequirements();
            context.getSource().sendSuccess(Component.translatable("command.remove_apoth_req.success", Component.translatable(translatedKey)),true);
            return 1;
        } else
            context.getSource().sendFailure(Component.translatable("command.remove_apoth_req.error", Component.translatable(translatedKey)));
            return 0;
    }
}
