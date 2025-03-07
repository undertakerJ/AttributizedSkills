package net.lumi_noble.attributizedskills.common.attributes.util;

import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = AttributizedSkills.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SkillBonusManager {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide()) return;
        ServerPlayer player = (ServerPlayer) event.player;
        if(player.isDeadOrDying()) return;
        if (player.tickCount % 50 == 0) {
            SkillModel model = SkillModel.get(player);
            for (Skill skill : Skill.values()) {
                Map<String, AttributeBonus> bonusMap = getBonusMapForSkill(skill);
                if(bonusMap == null) return;
                SkillBonusHelper.removeBonusForSkill(player, skill, bonusMap);
                SkillBonusHelper.clearAssignedUUIDs(skill);
                switch (skill) {
                    case VITALITY:
                        SkillBonusHelper.applyVitalityBonus(player, model.getSkillLevel(skill));
                        break;
                    case STRENGTH:
                        SkillBonusHelper.applyStrengthBonus(player, model.getSkillLevel(skill));
                        break;
                    case MIND:
                        SkillBonusHelper.applyMindBonus(player, model.getSkillLevel(skill));
                        break;
                    case DEXTERITY:
                        SkillBonusHelper.applyDexterityBonus(player, model.getSkillLevel(skill));
                        break;
                    case ENDURANCE:
                        SkillBonusHelper.applyEnduranceBonus(player, model.getSkillLevel(skill));
                        break;
                    case INTELLIGENCE:
                        SkillBonusHelper.applyIntelligenceBonus(player, model.getSkillLevel(skill));
                        break;
                }
            }
        }
    }
    public static Map<String, AttributeBonus> getBonusMapForSkill(Skill skill) {
        ASConfig.load();
        switch (skill) {
            case VITALITY:
                return ASConfig.vitalityAttributeMultipliers;
            case STRENGTH:
                return ASConfig.strengthAttributeMultipliers;
            case MIND:
                return ASConfig.mindAttributeMultipliers;
            case DEXTERITY:
                return ASConfig.dexterityAttributeMultipliers;
            case ENDURANCE:
                return ASConfig.enduranceAttributeMultipliers;
            case INTELLIGENCE:
                return ASConfig.intelligenceAttributeMultipliers;
            default:
                return null;
        }
    }
}
