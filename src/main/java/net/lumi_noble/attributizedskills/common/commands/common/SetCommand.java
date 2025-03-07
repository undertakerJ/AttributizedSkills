package net.lumi_noble.attributizedskills.common.commands.common;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.lumi_noble.attributizedskills.common.attributes.ModAttributes;
import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.network.packets.SyncToClientPacket;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.server.command.EnumArgument;

import java.util.UUID;

public class SetCommand {

  public static LiteralArgumentBuilder<CommandSourceStack> register() {
    return Commands.literal("set")
        .then(
            Commands.argument("player", EntityArgument.player())
                .then(
                    Commands.argument("skill", EnumArgument.enumArgument(Skill.class))
                        .then(
                            Commands.argument(
                                    "level", IntegerArgumentType.integer(1, ASConfig.getMaxLevel()))
                                .executes(SetCommand::execute))));
  }

  // Execute Command

  private static int execute(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    ServerPlayer player = EntityArgument.getPlayer(context, "player");
    Skill skill = context.getArgument("skill", Skill.class);
    int level = IntegerArgumentType.getInteger(context, "level");

    updatePlayerAttribute(player, skill, level);

    SkillModel.get(player).setSkillLevel(skill, level, player);
    SyncToClientPacket.send(player);

    context
        .getSource()
            .sendSuccess(
                    Component.translatable("command.set_skill_level",
                            Component.translatable(skill.displayName),
                            level,
                            player.getName()
                    ),
            true);

    return 1;
  }

  public static void updatePlayerAttribute(ServerPlayer player, Skill skill, int level) {
    Attribute attribute = getAttributeForSkill(skill);
    if (attribute != null) {
      AttributeInstance attributeInstance = player.getAttribute(attribute);
      if (attributeInstance != null) {
        UUID modifierUUID = ModAttributes.getModifierUUIDForSkill(skill);
        attributeInstance.removeModifier(modifierUUID);

        double bonus = level - 1;

        AttributeModifier modifier = new AttributeModifier(modifierUUID, skill.displayName, bonus, AttributeModifier.Operation.ADDITION);
        attributeInstance.addPermanentModifier(modifier);
      }
    }
  }
  public static Attribute getAttributeForSkill(Skill skill) {
    return switch (skill) {
      case VITALITY -> ModAttributes.VITALITY.get();
      case STRENGTH -> ModAttributes.STRENGTH.get();
      case MIND -> ModAttributes.MIND.get();
      case DEXTERITY -> ModAttributes.DEXTERITY.get();
      case ENDURANCE -> ModAttributes.ENDURANCE.get();
      case INTELLIGENCE -> ModAttributes.INTELLIGENCE.get();
      default -> null;
    };
  }
}
