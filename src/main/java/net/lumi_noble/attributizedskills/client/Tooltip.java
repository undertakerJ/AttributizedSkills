package net.lumi_noble.attributizedskills.client;

import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.lumi_noble.attributizedskills.common.skill.Requirement;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.lumi_noble.attributizedskills.common.util.CalculateAttributeValue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class Tooltip {

  @SubscribeEvent
  public void onTooltipDisplay(ItemTooltipEvent event) {

    if (Minecraft.getInstance().player != null && event.getEntity() != null) {

      ItemStack itemStack = event.getItemStack();
      List<Component> tooltips = event.getToolTip();

      if (SkillModel.isBlacklisted(ForgeRegistries.ITEMS.getKey(itemStack.getItem()))) {
        return;
      }

      SkillModel skillModel = SkillModel.get();

      Requirement[] requirements =
          ASConfig.getItemRequirements(ForgeRegistries.ITEMS.getKey(itemStack.getItem()));

      if (requirements != null) {
        tooltips.add(Component.translatable("tooltip.item.requirements").withStyle(ChatFormatting.YELLOW));
        for (Requirement requirement : requirements) {

          ChatFormatting color =
              skillModel.getSkillLevel(requirement.getSkill()) >= requirement.getLevel()
                  ? ChatFormatting.GREEN
                  : ChatFormatting.RED;
          tooltips.add(
              Component.translatable(requirement.getSkill().displayName)
                  .append(" - " + (int) requirement.getLevel())
                  .withStyle(color));
        }
      }
      if (itemStack.isEnchanted()) {
        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(itemStack);
        Map<Requirement[], Integer> enchantRequirements = new HashMap<>();

        if (!enchants.isEmpty()) {
          Map<Skill, Integer> maxRequirements = new HashMap<>();

            for (Enchantment enchant : enchants.keySet()) {
              int enchantLevel = enchants.get(enchant);
              Requirement[] requirementsPerEnchant =
                  ASConfig.getEnchantmentRequirements(ForgeRegistries.ENCHANTMENTS.getKey(enchant));

              if (requirementsPerEnchant != null) {
                for (Requirement enchantRequirement : requirementsPerEnchant) {
                  double levelRequirement = enchantRequirement.getLevel();
                  int finalValue =
                      (int)
                          (enchantLevel == 1
                              ? Math.round(levelRequirement)
                              : Math.round(
                                  levelRequirement
                                      + (enchantLevel
                                          * ASConfig.getEnchantmentRequirementIncrease())));

                  maxRequirements.merge(enchantRequirement.getSkill(), finalValue, Math::max);
                }
              }
              tooltips.add(Component.translatable("tooltip.enchant.requirements").withStyle(ChatFormatting.YELLOW));
            for (Map.Entry<Skill, Integer> entry : maxRequirements.entrySet()) {
              Skill skill = entry.getKey();
              int maxFinalValue = entry.getValue();
              ChatFormatting color =
                  skillModel.getSkillLevel(skill) >= maxFinalValue
                      ? ChatFormatting.GREEN
                      : ChatFormatting.RED;
              tooltips.add(
                  Component.translatable(skill.displayName)
                      .append(" - " + maxFinalValue)
                      .withStyle(color));
            }
          }
        }
      }
    }
  }

}
