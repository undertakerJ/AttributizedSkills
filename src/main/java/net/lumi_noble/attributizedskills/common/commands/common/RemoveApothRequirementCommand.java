package net.lumi_noble.attributizedskills.common.commands.common;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import java.util.ArrayList;
import java.util.List;
import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RemoveApothRequirementCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register(){
        return Commands.literal("remove_apoth_requirement")
                .then(
                        Commands.argument("rarity", ResourceLocationArgument.id())
                                .suggests(
                                        (ctx, builder) -> {
                                            for (LootRarity rarity : RarityRegistry.INSTANCE.getValues()) {
                                                ResourceLocation id = RarityRegistry.INSTANCE.getKey(rarity);
                                                if (id != null) {
                                                    builder.suggest(id.toString());
                                                }
                                            }
                                            return builder.buildFuture();
                                        }).executes(RemoveApothRequirementCommand::execute)
                );
    }
    private static int execute(CommandContext<CommandSourceStack> context) {
        ResourceLocation rarityId = ResourceLocationArgument.getId(context, "rarity");
        List<String> currentList = new ArrayList<>(ASConfig.APOTH_RARITY_REQUIREMENTS.get());
        boolean removedEntry = currentList.removeIf(s -> s.startsWith(rarityId.toString()));
        if(removedEntry){
            ASConfig.APOTH_RARITY_REQUIREMENTS.set(currentList);
            ASConfig.getConfig().save();
            ASConfig.loadApothRequirements();
            context.getSource().sendSuccess(() -> Component.translatable("command.remove_apoth_req.success", Component.translatable("rarity."+rarityId)),true);
            return 1;
        } else
            context.getSource().sendFailure(Component.translatable("command.remove_apoth_req.error", Component.translatable("rarity."+rarityId)));
            return 0;
    }
}
