package net.lumi_noble.attributizedskills.common.commands.common;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.lumi_noble.attributizedskills.common.config.Config;
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
                                    "level", IntegerArgumentType.integer(1, Config.getMaxLevel()))
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
        UUID modifierUUID = getModifierUUIDForSkill(skill);
        attributeInstance.removeModifier(modifierUUID);

        double bonus = level - 1;

        AttributeModifier modifier = new AttributeModifier(modifierUUID, skill.displayName, bonus, AttributeModifier.Operation.ADDITION);
        attributeInstance.addPermanentModifier(modifier);
      }
    }
  }
  private static Attribute getAttributeForSkill(Skill skill) {
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
  private static UUID getModifierUUIDForSkill(Skill skill) {
    return switch (skill) {
      case VITALITY -> UUID.fromString("e9810faf-3386-482a-af96-0f8b8b28de04");
      case STRENGTH -> UUID.fromString("b2deb2fe-d35f-42e9-b5fe-c0509ddbe7f9");
      case MIND -> UUID.fromString("f0af87e8-f034-4826-8557-4225f4ef3885");
      case DEXTERITY -> UUID.fromString("77c42b06-b4d3-4e2b-8bf2-e32d8efeee6e");
      case ENDURANCE -> UUID.fromString("ec8e0daf-0229-49bd-ab8d-3c0f578c3e50");
      case INTELLIGENCE -> UUID.fromString("5c783fc7-2705-450d-b1c1-2e49c8527871");
      default -> UUID.randomUUID();
    };
  }
}
