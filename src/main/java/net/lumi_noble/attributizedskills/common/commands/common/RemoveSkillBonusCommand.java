package net.lumi_noble.attributizedskills.common.commands.common;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.List;

import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.lumi_noble.attributizedskills.common.network.ModNetworking;
import net.lumi_noble.attributizedskills.common.network.packets.RemoveAllBonusesPacket;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RemoveSkillBonusCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("remove_skill_bonus")
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
                                    for (ResourceLocation loc : net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES.getKeys()) {
                                        builder.suggest(loc.toString());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(RemoveSkillBonusCommand::executeRemove)
                        )
                );
    }

    private static int executeRemove(CommandContext<CommandSourceStack> context) {
        String skillArg = StringArgumentType.getString(context, "skill").toUpperCase();
        ResourceLocation attrRL = ResourceLocationArgument.getId(context, "attribute");
        String attributeArg = attrRL.toString();

        Skill skill;
        try {
            skill = Skill.valueOf(skillArg);
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal("Invalid skill: " + skillArg));
            return 0;
        }

        List<String> currentList = new ArrayList<>(getBonusListForSkill(skill));
        boolean removedEntry = currentList.removeIf(s -> s.startsWith(attributeArg + ":"));
        if (removedEntry) {
            updateSkillBonuses(skill, currentList);
            ModNetworking.sendToServer(new RemoveAllBonusesPacket());
            context.getSource().sendSuccess(Component.translatable("command.remove_bonus.success", attributeArg, skill.name()), true);
            ASConfig.getConfig().save();
            ASConfig.load();
            return 1;
        } else {
            context.getSource().sendFailure(Component.translatable("command.remove_bonus.failure", attributeArg, skill.name()));
            return 0;
        }
    }

    private static List<String> getBonusListForSkill(Skill skill) {
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
    }
}
