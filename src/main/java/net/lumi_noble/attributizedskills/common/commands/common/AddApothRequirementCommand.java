package net.lumi_noble.attributizedskills.common.commands.common;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import shadows.apotheosis.adventure.loot.LootRarity;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AddApothRequirementCommand {
  public static LiteralArgumentBuilder<CommandSourceStack> register() {
    int maxSkills = Skill.values().length;
    return Commands.literal("add_apoth_requirement")
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
                .then(addSkillArguments(0, maxSkills)));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> addSkillArguments(
      int index, int maxSkills) {
    if(index >= maxSkills){
      return Commands.literal("end")
              .executes(AddApothRequirementCommand::execute);
    }
    return Commands.argument("skill" + index, StringArgumentType.word())
        .suggests((context, builder) -> suggestSkill(context, builder, index))
        .then(
            Commands.argument("level" + index, IntegerArgumentType.integer(0))
                .then(addSkillArguments(index + 1, maxSkills))
                .executes(AddApothRequirementCommand::execute));
  }

  private static CompletableFuture<Suggestions> suggestSkill(
      CommandContext<CommandSourceStack> context, SuggestionsBuilder builder, int index) {
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

  private static int execute(CommandContext<CommandSourceStack> context){
       try {
    ResourceLocation rarityId = ResourceLocationArgument.getId(context, "rarity");
    Map<Skill, Integer> skillMap = new HashMap<>();
         String translatedKey = "rarity.apoth." + rarityId.getPath();
    for (int i = 0; i < Skill.values().length; i++) {
      String skillArg = "skill" + i;
      String levelArg = "level" + i;

      boolean hasSkill =
              context.getNodes().stream().anyMatch(node -> node.getNode().getName().equals(skillArg));
      boolean hasLevel =
              context.getNodes().stream().anyMatch(node -> node.getNode().getName().equals(levelArg));

      if (hasSkill && hasLevel) {
        try {
          String skillName = StringArgumentType.getString(context, skillArg);
          int level = IntegerArgumentType.getInteger(context, levelArg);
          Skill skill = Skill.valueOf(skillName.toUpperCase());
          skillMap.put(skill, level);
        } catch (IllegalArgumentException e) {
          context
                  .getSource()
                  .sendFailure(Component.literal("Invalid rarity or level format at " + skillArg));
          return 0;
        }
      }
    }
    if (skillMap.isEmpty()) {
      context.getSource().sendFailure(Component.literal("No skills specified for this rarity!"));
      return 0;
    }
    StringBuilder configLine = new StringBuilder(rarityId.toString());
    skillMap.forEach(
            (skill, lvl) ->
                    configLine.append(" ").append(skill.name().toLowerCase()).append(":").append(lvl));

    updateApothRequirementConfig(rarityId, configLine.toString());
    context
            .getSource()
            .sendSuccess(
                            Component.translatable(
                                    "command.add_apoth_req.success", Component.translatable(translatedKey)),
                    true);
    return 1;

  } catch (Exception e) {

    context
            .getSource()
            .sendFailure(Component.literal("Error executing command: " + e.getMessage()));
    return 0;
  }
  }

  private static void updateApothRequirementConfig(ResourceLocation rarity, String newLine) {
    List<String> currentList = new ArrayList<>(ASConfig.APOTH_RARITY_REQUIREMENTS.get());
    boolean found = false;
    for (int i = 0; i < currentList.size(); i++) {
      if (currentList.get(i).startsWith(rarity.toString() + " ")) {
        currentList.set(i, newLine);
        found = true;
        break;
      }
    }
    if (!found) {
      currentList.add(newLine);
    }
    ASConfig.APOTH_RARITY_REQUIREMENTS.set(currentList);
    ASConfig.getConfig().save();
    ASConfig.loadApothRequirements();
  }
}
