package net.lumi_noble.attributizedskills.common.attributes;

import java.util.Map;
import java.util.UUID;
import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.lumi_noble.attributizedskills.common.capabilities.SkillCapability;
import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.network.packets.SyncToClientPacket;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = AttributizedSkills.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AttributesEventHandler {
  @SubscribeEvent
  public static void onPlayerUpdate(TickEvent.PlayerTickEvent event) {
    if (event.phase != TickEvent.Phase.END) return;
    if (event.player instanceof ServerPlayer player
        && player.level().getServer().getTickCount() % 20 == 0
        && !player.isDeadOrDying()) {
      updateSkillsFromAttributes(player);
    }
  }

  public static void updateSkillsFromAttributes(ServerPlayer player) {
    SkillModel model = SkillModel.get(player);
    for (Map.Entry<RegistryObject<Attribute>, Skill> entry :
        ModAttributes.ATTRIBUTE_BY_SKILL.entrySet()) {
      Attribute attribute = entry.getKey().get();
      Skill skill = entry.getValue();
      AttributeInstance instance = player.getAttribute(attribute);
      if (instance != null) {
        int newSkillLevel = (int) Math.floor(instance.getValue());
        if (model.getSkillLevel(skill) != newSkillLevel) {
          model.setSkillLevel(skill, newSkillLevel, player);
        }
      }
    }
    model.updateTotalLevel();
    SyncToClientPacket.send(player);
  }

  @SubscribeEvent
  public static void onPlayerClone(PlayerEvent.Clone event) {
    if (event.isWasDeath()) {
      event.getOriginal().reviveCaps();
      ServerPlayer oldPlayer = (ServerPlayer) event.getOriginal();
      ServerPlayer newPlayer = (ServerPlayer) event.getEntity();

      event
          .getEntity()
          .getCapability(SkillCapability.SKILL_MODEL)
          .ifPresent(
              newSkill -> {
                event
                    .getOriginal()
                    .getCapability(SkillCapability.SKILL_MODEL)
                    .ifPresent(
                        oldSkill -> {
                          cloneOnDeath(newSkill, oldPlayer, newPlayer);
                        });
              });
    }
    event.getOriginal().invalidateCaps();
  }

  private static void cloneOnDeath(
      SkillModel newSkill, ServerPlayer oldPlayer, ServerPlayer newPlayer) {
    for (Skill skill : Skill.values()) {
      UUID uuid = ModAttributes.getModifierUUIDForSkill(skill);
      Attribute attribute = getAttributeForSkill(skill);
      if (attribute != null) {
        AttributeInstance oldInstance = oldPlayer.getAttribute(attribute);
        if (oldInstance != null && oldInstance.getModifier(uuid) != null) {
          double bonus = oldInstance.getModifier(uuid).getAmount();
          int newLevel = (int) Math.round(bonus + 1);
          newSkill.setSkillLevel(skill, newLevel, newPlayer);
        } else {
          newSkill.setSkillLevel(skill, 1, newPlayer);
        }
      }
    }
    newSkill.updateTotalLevel();
    updateAllAttributes(newPlayer, newSkill);
    SyncToClientPacket.send(newPlayer);
  }

  private static void updateAllAttributes(ServerPlayer player, SkillModel skillModel) {
    for (Skill skill : Skill.values()) {
      int level = skillModel.getSkillLevel(skill);
      updatePlayerAttribute(player, skill, level);
    }
    SyncToClientPacket.send(player);
  }

  public static void updatePlayerAttribute(ServerPlayer player, Skill skill, int level) {
    Attribute attribute = getAttributeForSkill(skill);
    if (attribute != null) {
      AttributeInstance instance = player.getAttribute(attribute);
      if (instance != null) {
        UUID modifierUUID = ModAttributes.getModifierUUIDForSkill(skill);
        // Удаляем старый модификатор
        instance.removeModifier(modifierUUID);
        // Вычисляем бонус как (level - 1)
        double bonus = level - 1;
        AttributeModifier modifier =
            new AttributeModifier(
                modifierUUID, skill.displayName, bonus, AttributeModifier.Operation.ADDITION);
        instance.addPermanentModifier(modifier);
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
}
