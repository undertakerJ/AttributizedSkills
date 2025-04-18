package net.lumi_noble.attributizedskills.common.compat;

import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import java.util.List;
import java.util.Map;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class IronSpellsCompat {

  public SpellRequirement getRequirementForSpell(ResourceLocation spellId) {
    SpellRequirement req = ASConfig.SPELL_REQUIREMENTS_MAP.get(spellId);

    return req;
  }

  @SubscribeEvent
  public void onSpellPreCast(SpellPreCastEvent event) {
    ASConfig.loadSpellRequirements();
    ResourceLocation spellId = new ResourceLocation(event.getSpellId());
    SpellRequirement req = getRequirementForSpell(spellId);
    if (req == null) return;

    int spellLevel = event.getSpellLevel();
    Map<Skill, Integer> requirements = req.getRequirementsForSpellLevel(spellLevel);

    ServerPlayer player = (ServerPlayer) event.getEntity();
    SkillModel model = SkillModel.get(player);
    for (Map.Entry<Skill, Integer> entry : requirements.entrySet()) {
      if (model.getSkillLevel(entry.getKey()) < entry.getValue()) {
        event.setCanceled(true);
        player.displayClientMessage(
            Component.translatable(
                    "spell.requirement.not_met",
                    Component.translatable("spell." + spellId.toLanguageKey()),
                    entry.getKey(),
                    entry.getValue())
                .withStyle(ChatFormatting.RED),
            true);
        return;
      }
    }
  }

  private AbstractSpell getSpellFromStack(ItemStack itemStack) {
    return ISpellContainer.get(itemStack).getSpellAtIndex(0).getSpell();
  }

  private SpellData getSpellSlotFromStack(ItemStack itemStack) {
    return ISpellContainer.get(itemStack).getSpellAtIndex(0);
  }

  @SubscribeEvent
  public void onTooltipDisplay(ItemTooltipEvent event) {
    if (Minecraft.getInstance().player != null && event.getEntity() != null) {
      ItemStack itemStack = event.getItemStack();
      var spell = getSpellFromStack(itemStack);
      var spellSlot = getSpellSlotFromStack(itemStack);
      List<Component> tooltips = event.getToolTip();
      SkillModel skillModel = SkillModel.get();
      ResourceLocation spellId = new ResourceLocation(spell.getSpellId());

      SpellRequirement spellRequirement = getRequirementForSpell(spellId);
      if (spellRequirement == null) return;

      int spellLevel = spellSlot.getLevel();
      Map<Skill, Integer> requirements = spellRequirement.getRequirementsForSpellLevel(spellLevel);
      tooltips.add(
          Component.translatable("tooltip.spell.requirements").withStyle(ChatFormatting.YELLOW));

      for (Map.Entry<Skill, Integer> entry : requirements.entrySet()) {
        Skill skill = entry.getKey();
        int requiredLevel = entry.getValue();
        int playerSkillLevel = skillModel.getSkillLevel(skill);

        ChatFormatting color =
            playerSkillLevel >= requiredLevel ? ChatFormatting.GREEN : ChatFormatting.RED;
        tooltips.add(
            Component.translatable(skill.displayName)
                .append(" - " + requiredLevel)
                .withStyle(color));
      }
    }
  }

  @SubscribeEvent
  public void serverSetup(ServerStartedEvent event) {
    ASConfig.loadSpellRequirements();
  }
}
