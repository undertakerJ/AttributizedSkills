package net.lumi_noble.attributizedskills.common.commands.common;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class AddSkillBonusCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("add_skill_bonus")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("skill", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (Skill s : Skill.values()) {
                                builder.suggest(s.name().toLowerCase());
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("attribute", ResourceLocationArgument.id())
                                .suggests((context, builder) -> {
                                    for (ResourceLocation loc : ForgeRegistries.ATTRIBUTES.getKeys()) {
                                        builder.suggest(loc.toString());
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("operation", StringArgumentType.word())
                                                .suggests((context, builder) -> {
                                                    for (AttributeModifier.Operation op : new AttributeModifier.Operation[] {
                                                            AttributeModifier.Operation.ADDITION,
                                                            AttributeModifier.Operation.MULTIPLY_BASE,
                                                            AttributeModifier.Operation.MULTIPLY_TOTAL
                                                    }) {
                                                        builder.suggest(op.name().toLowerCase());
                                                    }
                                                    return builder.buildFuture();
                                                })
                                                .executes(AddSkillBonusCommand::executeAdd)
                                        )
                                )
                        )

        );
    }

    private static int executeAdd(CommandContext<CommandSourceStack> context) {
        String skillArg = StringArgumentType.getString(context, "skill").toUpperCase();
        // Получаем ResourceLocation из аргумента "attribute"
        ResourceLocation attrRL = ResourceLocationArgument.getId(context, "attribute");
        String attributeArg = attrRL.toString();
        double multiplier = DoubleArgumentType.getDouble(context, "multiplier");
        String operationStr = StringArgumentType.getString(context, "operation").toUpperCase();

        Skill skill;
        try {
            skill = Skill.valueOf(skillArg);
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal("Invalid skill: " + skillArg));
            return 0;
        }

        AttributeModifier.Operation op;
        try {
            op = AttributeModifier.Operation.valueOf(operationStr);
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal("Invalid operation: " + operationStr));
            return 0;
        }

        String bonusEntry = attributeArg + ":" + multiplier + ":" + op.name().toLowerCase();

        List<String> currentList = new ArrayList<>(getBonusListForSkill(skill));
        boolean exists = currentList.stream().anyMatch(s -> s.startsWith(attributeArg + ":"));
        if (exists) {
            context.getSource().sendFailure(Component.translatable("command.add_bonus.failure.exist", attributeArg, skill.name()));

            return 0;
        }
        currentList.add(bonusEntry);
        updateSkillBonuses(skill, currentList);
        ASConfig.getConfig().save();
        ASConfig.load();
        context.getSource().sendSuccess(Component.translatable("command.add_bonus.success",
                skill.name(), bonusEntry), true);
        return 1;
    }

    private static List<String> getBonusListForSkill(Skill skill) {
        // Возвращаем список из соответствующего конфигурационного значения
        switch (skill) {
            case VITALITY:
                return new ArrayList<>(ASConfig.VITALITY_SKILL_ATTRIBUTE_BONUSES.get());
            case STRENGTH:
                return new ArrayList<>(ASConfig.STRENGTH_SKILL_ATTRIBUTE_BONUSES.get());
            case MIND:
                return new ArrayList<>(ASConfig.MIND_SKILL_ATTRIBUTE_BONUSES.get());
            case DEXTERITY:
                return new ArrayList<>(ASConfig.DEXTERITY_SKILL_ATTRIBUTE_BONUSES.get());
            case ENDURANCE:
                return new ArrayList<>(ASConfig.ENDURANCE_SKILL_ATTRIBUTE_BONUSES.get());
            case INTELLIGENCE:
                return new ArrayList<>(ASConfig.INTELLIGENCE_SKILL_ATTRIBUTE_BONUSES.get());
            default:
                return new ArrayList<>();
        }
    }

    private static void updateSkillBonuses(Skill skill, List<String> updatedList) {
        // Обновляем соответствующее конфигурационное значение
        switch (skill) {
            case VITALITY:
                ASConfig.VITALITY_SKILL_ATTRIBUTE_BONUSES.set(updatedList);
                break;
            case STRENGTH:
                ASConfig.STRENGTH_SKILL_ATTRIBUTE_BONUSES.set(updatedList);
                break;
            case MIND:
                ASConfig.MIND_SKILL_ATTRIBUTE_BONUSES.set(updatedList);
                break;
            case DEXTERITY:
                ASConfig.DEXTERITY_SKILL_ATTRIBUTE_BONUSES.set(updatedList);
                break;
            case ENDURANCE:
                ASConfig.ENDURANCE_SKILL_ATTRIBUTE_BONUSES.set(updatedList);
                break;
            case INTELLIGENCE:
                ASConfig.INTELLIGENCE_SKILL_ATTRIBUTE_BONUSES.set(updatedList);
                break;
        }
        // Сохраняем и перечитываем конфиг
        ASConfig.getConfig().save();
        ASConfig.load();
    }

}
