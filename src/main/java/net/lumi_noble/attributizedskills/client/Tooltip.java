package net.lumi_noble.attributizedskills.client;

import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.compat.ApothRarityRequirement;
import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.lumi_noble.attributizedskills.common.skill.Requirement;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.lumi_noble.attributizedskills.common.util.CalculateAttributeValue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class Tooltip {

  @SubscribeEvent
  public void onTooltipDisplay(ItemTooltipEvent event) {
    Player player = Minecraft.getInstance().player;
    if (player == null || event.getEntity() == null) return;

    ItemStack itemStack = event.getItemStack();
    ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
    List<Component> tooltips = event.getToolTip();

    if (SkillModel.isBlacklisted(itemId)) return;

    SkillModel skillModel = SkillModel.get();
    Map<Skill, Double> totalRequirements = new HashMap<>();

    Requirement[] baseRequirements = ASConfig.getItemRequirements(itemId);
    if (baseRequirements != null) {
      for (Requirement req : baseRequirements) {
        totalRequirements.merge(req.getSkill(), req.getLevel(), Double::sum);
      }
    }

    if (ModList.get().isLoaded(Apotheosis.MODID)){
      try {
        DynamicHolder<LootRarity> rarityHolder = AffixHelper.getRarity(itemStack);
        if (rarityHolder != null) {
          ResourceLocation rarityId = rarityHolder.getId();
          ApothRarityRequirement apothReq = ASConfig.APOTH_RARITY_REQUIREMENTS_MAP.get(rarityId);
          if (apothReq != null) {
            for (Map.Entry<Skill, Integer> entry : apothReq.getBaseRequirements().entrySet()) {
              totalRequirements.merge(entry.getKey(), (double) entry.getValue(), Double::sum);
            }
          }
        }
      } catch (Exception ignored) {}
    }

    if (itemStack.isEnchanted()) {
      Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(itemStack);
      for (Map.Entry<Enchantment, Integer> enchantEntry : enchants.entrySet()) {
        Enchantment enchant = enchantEntry.getKey();
        int level = enchantEntry.getValue();
        Requirement[] enchantReqs = ASConfig.getEnchantmentRequirements(ForgeRegistries.ENCHANTMENTS.getKey(enchant));
        if (enchantReqs != null) {
          for (Requirement req : enchantReqs) {
            double base = req.getLevel();
            double total = level == 1 ? base : base + (level * ASConfig.getEnchantmentRequirementIncrease());
            totalRequirements.merge(req.getSkill(), total, Double::sum);
          }
        }
      }
    }

    if (!totalRequirements.isEmpty()) {
      tooltips.add(Component.translatable("tooltip.item.requirements").withStyle(ChatFormatting.YELLOW));
      for (Map.Entry<Skill, Double> entry : totalRequirements.entrySet()) {
        Skill skill = entry.getKey();
        double required = entry.getValue();
        int actual = skillModel.getSkillLevel(skill);

        ChatFormatting color = actual >= required ? ChatFormatting.GREEN : ChatFormatting.RED;
        tooltips.add(Component.translatable(skill.displayName)
                .append(" - " + (int) required)
                .withStyle(color));
      }
    }
  }
}

