package net.lumi_noble.attributizedskills.common.network.packets;

import net.lumi_noble.attributizedskills.common.attributes.util.AttributeBonus;
import net.lumi_noble.attributizedskills.common.attributes.util.SkillBonusHelper;
import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class RemoveAllBonusesPacket {


    public RemoveAllBonusesPacket() {
    }

    public RemoveAllBonusesPacket(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            ctx.getSender().server.getPlayerList().getPlayers().forEach(player -> {
                for (Skill skill : Skill.values()) {
                    Map<String, AttributeBonus> bonusMap = getBonusMapForSkill(skill);
                    SkillBonusHelper.removeBonusForSkill(player, skill, bonusMap);
                    SkillBonusHelper.clearAssignedUUIDs(skill);
                }
            });
        });
        ctx.getSender().sendSystemMessage(Component.translatable("command.error.remove_all_bonuses").withStyle(ChatFormatting.AQUA));
        ctx.setPacketHandled(true);
    }

    private Map<String, net.lumi_noble.attributizedskills.common.attributes.util.AttributeBonus> getBonusMapForSkill(Skill skill) {
        ASConfig.load();
        return switch (skill) {
            case VITALITY -> ASConfig.vitalityAttributeMultipliers;
            case STRENGTH -> ASConfig.strengthAttributeMultipliers;
            case MIND -> ASConfig.mindAttributeMultipliers;
            case DEXTERITY -> ASConfig.dexterityAttributeMultipliers;
            case ENDURANCE -> ASConfig.enduranceAttributeMultipliers;
            case INTELLIGENCE -> ASConfig.intelligenceAttributeMultipliers;
            default -> null;
        };
    }
}