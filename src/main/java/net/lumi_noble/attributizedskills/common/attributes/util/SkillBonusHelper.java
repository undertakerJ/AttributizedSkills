package net.lumi_noble.attributizedskills.common.attributes.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.lumi_noble.attributizedskills.common.config.Config;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

public class SkillBonusHelper {

  private static final Map<Skill, Map<Attribute, UUID>> assignedUUIDsPerSkill = new HashMap<>();

  public static List<UUID> getUUIDPoolForSkill(Skill skill) {
    switch (skill) {
      case VITALITY:
        return SkillUUIDs.VITALITY_UUIDS;
      case STRENGTH:
        return SkillUUIDs.STRENGTH_UUIDS;
      case MIND:
        return SkillUUIDs.MIND_UUIDS;
      case DEXTERITY:
        return SkillUUIDs.DEXTERITY_UUIDS;
      case ENDURANCE:
        return SkillUUIDs.ENDURANCE_UUIDS;
      case INTELLIGENCE:
        return SkillUUIDs.INTELLIGENCE_UUIDS;
      default:
        throw new IllegalArgumentException("Неизвестный скилл: " + skill);
    }
  }

  public static UUID getAssignedUUIDForAttribute(Attribute attribute, Skill skill) {
    Map<Attribute, UUID> map = assignedUUIDsPerSkill.computeIfAbsent(skill, k -> new HashMap<>());
    return map.computeIfAbsent(
        attribute,
        attr -> {
          List<UUID> pool = getUUIDPoolForSkill(skill);
          int index = map.size();
          if (index < pool.size()) {
            return pool.get(index);
          } else {
            throw new IllegalStateException("Не хватает UUID для скилла " + skill.name());
          }
        });
  }

  public static void clearAssignedUUIDs(Skill skill) {
    assignedUUIDsPerSkill.remove(skill);
  }

  public static void removeBonusForSkill(
      ServerPlayer player, Skill skill, Map<String, AttributeBonus> bonusMap) {
    if (bonusMap == null) return;
    for (Map.Entry<String, AttributeBonus> entry : bonusMap.entrySet()) {
      String attrId = entry.getKey();
      Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attrId));
      if (attribute == null) continue;
      AttributeInstance instance = player.getAttribute(attribute);
      if (instance == null) continue;

      UUID assignedUUID = getAssignedUUIDForAttribute(attribute, skill);
      if (instance.getModifier(assignedUUID) != null) {
        instance.removeModifier(assignedUUID);
      }
    }
  }

  public static void applyStrengthBonus(ServerPlayer player, int strengthLevel) {
    for (Map.Entry<String, AttributeBonus> entry : Config.strengthAttributeMultipliers.entrySet()) {
      String attrId = entry.getKey();
      AttributeBonus bonusEntry = entry.getValue();
      double multiplier = bonusEntry.multiplier;
      AttributeModifier.Operation op = bonusEntry.operation;

      Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attrId));
      if (attribute == null) continue;

      AttributeInstance instance = player.getAttribute(attribute);
      if (instance == null) continue;
      if (strengthLevel > 1) {
        double bonus = strengthLevel * multiplier;

        UUID modifierUUID = SkillBonusHelper.getAssignedUUIDForAttribute(attribute, Skill.STRENGTH);

        AttributeModifier existing = instance.getModifier(modifierUUID);
        if (existing != null) {
          if (existing.getAmount() != bonus) {
            instance.removeModifier(modifierUUID);
            instance.addPermanentModifier(
                new AttributeModifier(modifierUUID, "StrengthBonus", bonus, op));
          }
        } else {
          instance.addPermanentModifier(
              new AttributeModifier(modifierUUID, "StrengthBonus", bonus, op));
        }
      }
    }
  }

  public static void applyDexterityBonus(ServerPlayer player, int dexterityLevel) {
    for (Map.Entry<String, AttributeBonus> entry :
        Config.dexterityAttributeMultipliers.entrySet()) {
      String attrId = entry.getKey();
      AttributeBonus bonusEntry = entry.getValue();
      double multiplier = bonusEntry.multiplier;
      AttributeModifier.Operation op = bonusEntry.operation;

      Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attrId));
      if (attribute == null) continue;

      AttributeInstance instance = player.getAttribute(attribute);
      if (instance == null) continue;
      if (dexterityLevel > 1) {
        double bonus = dexterityLevel * multiplier;
        if (bonus == 0) return;
        UUID modifierUUID =
            SkillBonusHelper.getAssignedUUIDForAttribute(attribute, Skill.DEXTERITY);

        AttributeModifier existing = instance.getModifier(modifierUUID);

        if (existing != null) {
          if (existing.getAmount() != bonus) {
            instance.removeModifier(modifierUUID);
            instance.addPermanentModifier(
                new AttributeModifier(modifierUUID, "DexterityBonus", bonus, op));
          }
        } else {
          instance.addPermanentModifier(
              new AttributeModifier(modifierUUID, "DexterityBonus", bonus, op));
        }
      }
    }
  }

  public static void applyEnduranceBonus(ServerPlayer player, int enduranceLevel) {
    for (Map.Entry<String, AttributeBonus> entry :
        Config.enduranceAttributeMultipliers.entrySet()) {
      String attrId = entry.getKey();
      AttributeBonus bonusEntry = entry.getValue();
      double multiplier = bonusEntry.multiplier;
      AttributeModifier.Operation op = bonusEntry.operation;

      Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attrId));
      if (attribute == null) continue;

      AttributeInstance instance = player.getAttribute(attribute);
      if (instance == null) continue;
      if (enduranceLevel > 1) {
        double bonus = enduranceLevel * multiplier;
        if (bonus == 0) continue;
        UUID modifierUUID =
            SkillBonusHelper.getAssignedUUIDForAttribute(attribute, Skill.ENDURANCE);

        AttributeModifier existing = instance.getModifier(modifierUUID);
        if (existing != null) {
          if (existing.getAmount() != bonus) {
            instance.removeModifier(modifierUUID);
            instance.addPermanentModifier(
                new AttributeModifier(modifierUUID, "EnduranceBonus", bonus, op));
          }
        } else {
          instance.addPermanentModifier(
              new AttributeModifier(modifierUUID, "EnduranceBonus", bonus, op));
        }
      }
    }
  }

  public static void applyVitalityBonus(ServerPlayer player, int vitalityLevel) {
    for (Map.Entry<String, AttributeBonus> entry : Config.vitalityAttributeMultipliers.entrySet()) {
      String attrId = entry.getKey();
      AttributeBonus bonusEntry = entry.getValue();
      double multiplier = bonusEntry.multiplier;
      AttributeModifier.Operation op = bonusEntry.operation;

      Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attrId));
      if (attribute == null) continue;

      AttributeInstance instance = player.getAttribute(attribute);
      if (instance == null) continue;
      if (vitalityLevel > 1) {
        double bonus = vitalityLevel * multiplier;
        if (bonus == 0) continue;
        UUID modifierUUID = SkillBonusHelper.getAssignedUUIDForAttribute(attribute, Skill.VITALITY);

        AttributeModifier existing = instance.getModifier(modifierUUID);
        if (existing != null) {
          if (existing.getAmount() != bonus) {
            instance.removeModifier(modifierUUID);
            instance.addPermanentModifier(
                new AttributeModifier(modifierUUID, "VitalityBonus", bonus, op));
          }
        } else {
          instance.addPermanentModifier(
              new AttributeModifier(modifierUUID, "VitalityBonus", bonus, op));
        }
      }
    }
  }

  public static void applyMindBonus(ServerPlayer player, int mindLevel) {
    for (Map.Entry<String, AttributeBonus> entry : Config.mindAttributeMultipliers.entrySet()) {
      String attrId = entry.getKey();
      AttributeBonus bonusEntry = entry.getValue();
      double multiplier = bonusEntry.multiplier;
      AttributeModifier.Operation op = bonusEntry.operation;

      Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attrId));
      if (attribute == null) continue;

      AttributeInstance instance = player.getAttribute(attribute);
      if (instance == null) continue;
      if (mindLevel > 1) {
        double bonus = mindLevel * multiplier;
        if (bonus == 0) continue;
        UUID modifierUUID = SkillBonusHelper.getAssignedUUIDForAttribute(attribute, Skill.MIND);

        AttributeModifier existing = instance.getModifier(modifierUUID);
        if (existing != null) {
          if (existing.getAmount() != bonus) {
            instance.removeModifier(modifierUUID);
            instance.addPermanentModifier(
                new AttributeModifier(modifierUUID, "MindBonus", bonus, op));
          }
        } else {
          instance.addPermanentModifier(
              new AttributeModifier(modifierUUID, "MindBonus", bonus, op));
        }
      }
    }
  }

  public static void applyIntelligenceBonus(ServerPlayer player, int intelligenceLevel) {
    for (Map.Entry<String, AttributeBonus> entry :
        Config.intelligenceAttributeMultipliers.entrySet()) {
      String attrId = entry.getKey();
      AttributeBonus bonusEntry = entry.getValue();
      double multiplier = bonusEntry.multiplier;
      AttributeModifier.Operation op = bonusEntry.operation;

      Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attrId));
      if (attribute == null) continue;

      AttributeInstance instance = player.getAttribute(attribute);
      if (instance == null) continue;
      if (intelligenceLevel > 1) {
        double bonus = intelligenceLevel * multiplier;
        if (bonus == 0) continue;
        UUID modifierUUID =
            SkillBonusHelper.getAssignedUUIDForAttribute(attribute, Skill.INTELLIGENCE);

        AttributeModifier existing = instance.getModifier(modifierUUID);
        if (existing != null) {
          if (existing.getAmount() != bonus) {
            instance.removeModifier(modifierUUID);
            instance.addPermanentModifier(
                new AttributeModifier(modifierUUID, "IntelligenceBonus", bonus, op));
          }
        } else {
          instance.addPermanentModifier(
              new AttributeModifier(modifierUUID, "IntelligenceBonus", bonus, op));
        }
      }
    }
  }
}
