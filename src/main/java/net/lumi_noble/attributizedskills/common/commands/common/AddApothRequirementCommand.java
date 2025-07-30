package net.lumi_noble.attributizedskills.common.commands.common;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
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
    return Commands.literal("add_apoth_requirement")
            .then(
                    Commands.argument("rarity", ResourceLocationArgument.id())
                            .suggests((ctx, builder) -> {
                              for (LootRarity rarity : RarityRegistry.INSTANCE.getValues()) {
                                ResourceLocation id = RarityRegistry.INSTANCE.getKey(rarity);
                                if (id != null) {
                                  builder.suggest(id.toString());
                                }
                              }
                              return builder.buildFuture();
                            })
                            .then(
                                    Commands.argument("multiplier", FloatArgumentType.floatArg(0))
                                            .executes(AddApothRequirementCommand::execute)
                            )
            );
  }

  private static int execute(CommandContext<CommandSourceStack> context) {
    try {
      ResourceLocation rarityId = ResourceLocationArgument.getId(context, "rarity");
      float multiplier = FloatArgumentType.getFloat(context, "multiplier");

      Map<Skill, Float> skillMap = new HashMap<>();
      for (Skill skill : Skill.values()) {
        skillMap.put(skill, multiplier);
      }

      StringBuilder configLine = new StringBuilder(rarityId.toString());
      skillMap.forEach(
              (skill, lvl) ->
                      configLine.append(" ")
                              .append(skill.name().toLowerCase())
                              .append(":")
                              .append(String.format(Locale.ROOT, "%.2f", lvl)));

      updateApothRequirementConfig(rarityId, configLine.toString());

      context.getSource().sendSuccess(
              () -> Component.translatable(
                      "command.add_apoth_req.success", Component.translatable("rarity." + rarityId)
              ),
              true
      );
      return 1;

    } catch (Exception e) {
      context.getSource().sendFailure(Component.literal("Error executing command: " + e.getMessage()));
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
