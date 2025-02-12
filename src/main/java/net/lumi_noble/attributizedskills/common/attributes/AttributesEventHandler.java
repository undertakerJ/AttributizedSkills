package net.lumi_noble.attributizedskills.common.attributes;

import static net.lumi_noble.attributizedskills.common.commands.common.SetCommand.updatePlayerAttribute;

import java.util.Map;
import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.lumi_noble.attributizedskills.common.attributes.util.SkillBonusHelper;
import net.lumi_noble.attributizedskills.common.capabilities.SkillCapability;
import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.config.Config;
import net.lumi_noble.attributizedskills.common.network.packets.SyncToClientPacket;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
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
        && player.getLevel().getServer().getTickCount() % 50 == 0 && player.isAlive() && !player.isDeadOrDying()) {
      updateSkillsFromAttributes(player);
    }
  }

  @SubscribeEvent
  public static void onPlayerClone(PlayerEvent.Clone event) {
    event.getOriginal().reviveCaps();
    if (event.isWasDeath()) {
      event
          .getOriginal()
          .getCapability(SkillCapability.SKILL_MODEL)
          .ifPresent(
              oldSkill -> {
                event
                    .getEntity()
                    .getCapability(SkillCapability.SKILL_MODEL)
                    .ifPresent(
                        newSkill -> {
                          if (!Config.getDeathReset()) {
                            newSkill.copyForRespawn(oldSkill);
                            updateAllAttributes((ServerPlayer) event.getEntity(), newSkill);
                          }
                        });
              });
    }
    event.getOriginal().invalidateCaps();
  }

  private static void updateAllAttributes(ServerPlayer player, SkillModel skillModel) {
    for (Skill skill : Skill.values()) {
      int level = skillModel.getSkillLevel(skill);
      updatePlayerAttribute(player, skill, level);
    }
  }

  public static void updateSkillsFromAttributes(ServerPlayer player) {
    for (Map.Entry<RegistryObject<Attribute>, Skill> entry :
        ModAttributes.ATTRIBUTE_BY_SKILL.entrySet()) {
      Attribute attribute = entry.getKey().get();
      Skill skill = entry.getValue();

      AttributeInstance attributeInstance = player.getAttribute(attribute);
      if (attributeInstance != null) {
        int newSkillLevel = (int) attributeInstance.getValue();
        SkillModel model = SkillModel.get(player);
        if (model == null) return;
        model.setSkillLevel(skill, newSkillLevel, player);
        model.updateTotalLevel();
        SyncToClientPacket.send(player);
      }
    }
  }

  @SubscribeEvent
  public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    if (event.player.level.isClientSide()) return;
    if (event.phase != TickEvent.Phase.END) return;
    ServerPlayer player = (ServerPlayer) event.player;
    if (event.player.tickCount % 50 == 0 && player.isAlive() && !player.isDeadOrDying()) return;
  if(event.player.getCapability(SkillCapability.SKILL_MODEL).isPresent()){
      SkillModel model = SkillModel.get(player);

      SkillBonusHelper.applyStrengthBonus(player, model.getSkillLevel(Skill.STRENGTH));
      SkillBonusHelper.applyDexterityBonus(player, model.getSkillLevel(Skill.DEXTERITY));
      SkillBonusHelper.applyEnduranceBonus(player, model.getSkillLevel(Skill.ENDURANCE));
      SkillBonusHelper.applyVitalityBonus(player, model.getSkillLevel(Skill.VITALITY));
      SkillBonusHelper.applyMindBonus(player, model.getSkillLevel(Skill.MIND));
      SkillBonusHelper.applyIntelligenceBonus(player, model.getSkillLevel(Skill.INTELLIGENCE));
    }
  }
}
