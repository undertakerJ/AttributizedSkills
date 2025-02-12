package net.lumi_noble.attributizedskills.common.commands.common;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.lumi_noble.attributizedskills.common.config.Config;
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

import java.util.LinkedHashMap;
import java.util.Map;

public class AddRequirementsCommand {


    // Регистрируем команду: /attributized addrequirements <player> <requirements...>
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("add_requirements")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("requirements", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    String remaining = builder.getRemaining().trim().toLowerCase();
                                    if (!remaining.contains(" ")) {
                                        for (Skill s : Skill.values()) {
                                            String name = s.name().toLowerCase();
                                            if (name.startsWith(remaining)) {
                                                builder.suggest(name);
                                            }
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(AddRequirementsCommand::execute)
                        )
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

        String requirementsStr = StringArgumentType.getString(context, "requirements");

        String[] tokens = requirementsStr.trim().split("\\s+");
        if (tokens.length % 2 != 0) {
            context.getSource().sendFailure(Component.translatable("command.add_requirements.failure.wrong_format"));
            return 0;
        }

        Map<String, Integer> newRequirements = new LinkedHashMap<>();
        for (int i = 0; i < tokens.length; i += 2) {
            String inputSkill = tokens[i].toUpperCase();
            int level;
            try {
                level = Integer.parseInt(tokens[i + 1]);
                if (level < 1) {
                    context.getSource().sendFailure(
                            Component.translatable("command.add_requirements.failure.wrong_format_number", tokens[i])
                    );
                    return 0;
                }
            } catch (NumberFormatException e) {
                context.getSource().sendFailure(
                        Component.translatable("command.add_requirements.failure.wrong_number", tokens[i])
                );
                return 0;
            }

            try {
                Skill skillEnum = Skill.valueOf(inputSkill);
                newRequirements.put(skillEnum.name().toLowerCase(), level);
            } catch (IllegalArgumentException e) {
                context.getSource().sendFailure(
                        Component.translatable("command.add_requirements.failure.wrong_skill", tokens[i])
                );
                return 0;
            }
        }

        Map<String, Integer> mergedRequirements = new LinkedHashMap<>();
        Requirement[] currentReqs = Config.getItemRequirements(new ResourceLocation(itemId));
        if (currentReqs != null) {
            for (Requirement req : currentReqs) {
                mergedRequirements.put(req.getSkill().name().toLowerCase(), (int) req.getLevel());
            }
        }
        mergedRequirements.putAll(newRequirements);

        StringBuilder configLine = new StringBuilder(itemId);
        for (Map.Entry<String, Integer> entry : mergedRequirements.entrySet()) {
            configLine.append(" ").append(entry.getKey()).append(":").append(entry.getValue());
        }

        updateConfig(itemId, configLine.toString());

        StringBuilder recommendation = new StringBuilder("For item ");
        recommendation.append(itemId).append(" set next requirements: ");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : mergedRequirements.entrySet()) {
            if (!first) {
                recommendation.append(", ");
            }
            recommendation.append(entry.getKey()).append(":").append(entry.getValue());
            first = false;
        }
        context.getSource().sendSuccess(Component.literal(recommendation.toString()), true);
        return 1;
    }

    private static void updateConfig(String itemId, String newLine) {
        java.util.List<String> currentList = new java.util.ArrayList<>(Config.OVERRIDE_SKILL_LOCKS.get());
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
        Config.OVERRIDE_SKILL_LOCKS.set(currentList);
        Config.getConfig().save();
        Config.load();
    }

}
