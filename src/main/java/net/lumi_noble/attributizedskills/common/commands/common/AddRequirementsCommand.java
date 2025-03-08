package net.lumi_noble.attributizedskills.common.commands.common;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.lumi_noble.attributizedskills.common.skill.Requirement;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AddRequirementsCommand {


    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        int maxSkills = Skill.values().length;
        return Commands.literal("add_requirements")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(addSkillArguments(0, maxSkills)));

    }

    private static ArgumentBuilder<CommandSourceStack, ?> addSkillArguments(int index, int maxSkills) {
        if (index >= maxSkills) {
            return Commands.literal("error")
                    .executes(context -> {
                        context.getSource().sendFailure(
                                Component.literal("Превышено максимальное количество навыков (" + maxSkills + ")")
                        );
                        return 0;
                    });
        }

        return Commands.argument("skill" + index, StringArgumentType.word())
                .suggests((context, builder) -> suggestSkill(context, builder, index))
                .then(Commands.argument("level" + index, IntegerArgumentType.integer(0))
                        .then(addSkillArguments(index + 1, maxSkills))
                        .executes(AddRequirementsCommand::execute));
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

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ItemStack itemStack = player.getMainHandItem();

        if (itemStack.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("command.remove_requirements.failure.no_item"));
            return 0;
        }

        String itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString();

        Map<Skill, Integer> skillRequirements = new HashMap<>();

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
                    skillRequirements.put(skill, level);
                } catch (IllegalArgumentException e) {
                    context.getSource().sendFailure(Component.literal("Invalid skill or level format at " + skillArg));
                    return 0;
                }
            }
        }



        if (skillRequirements.isEmpty()) {
            context.getSource().sendFailure(Component.literal("No skill requirements specified!"));
            return 0;
        }

        StringBuilder configLine = new StringBuilder(itemId.toString());
        skillRequirements.forEach((skill, lvl) -> configLine.append(" ").append(skill.name().toLowerCase()).append(":").append(lvl));

        updateConfig(itemId, configLine.toString());
        context.getSource().sendSuccess(() -> Component.literal("Set requirements for item: " + itemId), true);
        return 1;
    }


    private static void updateConfig(String itemId, String newLine) {
        java.util.List<String> currentList = new java.util.ArrayList<>(ASConfig.OVERRIDE_SKILL_LOCKS.get());
        boolean found = false;
        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).startsWith(itemId)) {
                currentList.set(i, newLine);
                found = true;
                break;
            }
        }
        if (!found) {
            currentList.add(newLine);
        }
        ASConfig.OVERRIDE_SKILL_LOCKS.set(currentList);
        ASConfig.getConfig().save();
        ASConfig.load();
    }

}
