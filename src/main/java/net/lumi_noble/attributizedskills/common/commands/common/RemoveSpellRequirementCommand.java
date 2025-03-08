package net.lumi_noble.attributizedskills.common.commands.common;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class RemoveSpellRequirementCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register(){
        return Commands.literal("remove_spell_requirement")
                .then(
                        Commands.argument("spell", ResourceLocationArgument.id())
                                .suggests(
                                        (ctx, builder) -> {
                                            SpellRegistry.REGISTRY
                                                    .get()
                                                    .getKeys()
                                                    .forEach(rl -> builder.suggest(rl.toString()));
                                            return builder.buildFuture();
                                        }).executes(RemoveSpellRequirementCommand::execute)
                );
    }
    private static int execute(CommandContext<CommandSourceStack> context) {
        ResourceLocation spellID = ResourceLocationArgument.getId(context, "spell");
        List<String> currentList = new ArrayList<>(ASConfig.SPELL_REQUIREMENTS.get());
        boolean removedEntry = currentList.removeIf(s -> s.startsWith(spellID.toString()));
        if(removedEntry){
            ASConfig.SPELL_REQUIREMENTS.set(currentList);
            ASConfig.getConfig().save();
            ASConfig.loadSpellRequirements();
            context.getSource().sendSuccess(() -> Component.translatable("command.remove_spell_req.success", Component.translatable("spell." + spellID.toLanguageKey())),true);
            return 1;
        } else
            context.getSource().sendFailure(Component.translatable("command.remove_spell_req.error", Component.translatable("spell." + spellID.toLanguageKey())));
            return 0;
    }
}
