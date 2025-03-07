package net.lumi_noble.attributizedskills.common.commands.common;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AddSpellRequirementCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
      int maxSkills = Skill.values().length;
      return Commands.literal("add_spell_requirement")
          .then(
              Commands.argument("spell", ResourceLocationArgument.id())
                  .suggests(
                      (ctx, builder) -> {
                        SpellRegistry.REGISTRY
                            .get()
                            .getKeys()
                            .forEach(rl -> builder.suggest(rl.toString()));
                        return builder.buildFuture();
                      })
                  .then(addSkillArguments(0, maxSkills)));

    }
    private static ArgumentBuilder<CommandSourceStack, ?> addSkillArguments(int index, int maxSkills) {
        if (index >= maxSkills) {
            return addPerLevelArgument();
        }

        return Commands.argument("skill" + index, StringArgumentType.word())
                .suggests((context, builder) -> suggestSkill(context, builder, index))
                .then(Commands.argument("level" + index, IntegerArgumentType.integer(0))
                        .then(addSkillArguments(index + 1, maxSkills))
                        .then(addPerLevelArgument()));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addPerLevelArgument() {
        return Commands.literal("perLevel")
                .then(Commands.argument("perLevelValue", IntegerArgumentType.integer(0))
                        .executes(AddSpellRequirementCommand::execute));
    }

    private static CompletableFuture<Suggestions> suggestSkill(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder, int index) {
        Set<String> existingSkills = new HashSet<>();
        for (int i = 0; i < index; i++) {
            String skill = StringArgumentType.getString(context, "skill" + i).toLowerCase();
            existingSkills.add(skill);
        }

        for (Skill skill : Skill.values()) {
            if (!existingSkills.contains(skill.name().toLowerCase())) {
                builder.suggest(skill.name().toLowerCase());
            }
        }
        return builder.buildFuture();
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        try {
            ResourceLocation spellID = ResourceLocationArgument.getId(context, "spell");
            Map<Skill, Integer> skillMap = new HashMap<>();
            int perLevelIncrement = IntegerArgumentType.getInteger(context, "perLevelValue");
            for (int i = 0; i < Skill.values().length; i++) {
                String skillArg = "skill" + i;
                String levelArg = "level" + i;

                boolean hasSkill = context.getNodes().stream().anyMatch(node -> node.getNode().getName().equals(skillArg));
                boolean hasLevel = context.getNodes().stream().anyMatch(node -> node.getNode().getName().equals(levelArg));

                if (hasSkill && hasLevel) {
                    try {
                        String skillName = StringArgumentType.getString(context, skillArg);
                        int level = IntegerArgumentType.getInteger(context, levelArg);
                        Skill skill = Skill.valueOf(skillName.toUpperCase());
                        skillMap.put(skill, level);
                    } catch (IllegalArgumentException e) {
                        context.getSource().sendFailure(Component.literal("Invalid skill or level format at " + skillArg));
                        return 0;
                    }
                }
            }
            if (skillMap.isEmpty()) {
                context.getSource().sendFailure(Component.literal("No skills specified for this spell!"));
                return 0;
            }
            StringBuilder configLine = new StringBuilder(spellID.toString());
            skillMap.forEach((skill, lvl) -> configLine.append(" ").append(skill.name().toLowerCase()).append(":").append(lvl));
            configLine.append(" perlevel:").append(perLevelIncrement);

            updateSpellRequirementConfig(spellID, configLine.toString());
            context.getSource().sendSuccess(Component.translatable("command.add_spell_req.success", Component.translatable("spell." + spellID.toLanguageKey())), true);
            return 1;

        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error executing command: " + e.getMessage()));
            return 0;
        }
    }


    private static void updateSpellRequirementConfig(ResourceLocation spellID, String newLine) {
        List<String> currentList = new ArrayList<>(ASConfig.SPELL_REQUIREMENTS.get());
        boolean found = false;
        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).startsWith(spellID.toString() + " ")) {
                currentList.set(i, newLine);
                found = true;
                break;
            }
        }
        if (!found) {
            currentList.add(newLine);
        }
        ASConfig.SPELL_REQUIREMENTS.set(currentList);
        ASConfig.getConfig().save();
        ASConfig.loadSpellRequirements();
    }
}
