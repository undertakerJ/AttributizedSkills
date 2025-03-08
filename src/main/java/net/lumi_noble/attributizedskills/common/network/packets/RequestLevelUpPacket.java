package net.lumi_noble.attributizedskills.common.network.packets;

import java.util.UUID;
import java.util.function.Supplier;

import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.lumi_noble.attributizedskills.common.attributes.ModAttributes;
import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.skill.Skill;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.network.NetworkEvent;

public class RequestLevelUpPacket {

	private final int skill;
	private final int levels;
	private final boolean useTearPoints;

	public RequestLevelUpPacket(Skill skill, int levels, boolean useTearPoints) {
		this.skill = skill.index;
		this.levels = levels;
		this.useTearPoints = useTearPoints;

	}

	public RequestLevelUpPacket(FriendlyByteBuf buf) {
		this.skill = buf.readInt();
		this.levels = buf.readInt();
		this.useTearPoints = buf.readBoolean();
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(skill);
		buf.writeInt(levels);
		buf.writeBoolean(useTearPoints);
	}

	public void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayer player = context.get().getSender();
			if (player == null) return;

			SkillModel skillModel = SkillModel.get(player);
			Skill skill = Skill.values()[this.skill];
			int currentLevel = skillModel.getSkillLevel(skill);
			int maxLevel = ASConfig.getMaxLevel();
			int totalCost = 0;

			for (int i = 0; i < levels && (currentLevel + i) < maxLevel; i++) {
				totalCost += ASConfig.getStartCost() + ((currentLevel + i) - 1) * ASConfig.getCostIncrease();
			}

			int tearPoints = skillModel.getTearPoints();

			boolean canUseTearPoints = useTearPoints && tearPoints >= levels;

			if (currentLevel < maxLevel && skillModel.underMaxTotal()) {
				if (canUseTearPoints) {
					skillModel.setTearPoints(tearPoints - levels);
				} else if (!player.isCreative()) {
					player.giveExperienceLevels(-totalCost);
				}

				for (int i = 0; i < levels && skillModel.getSkillLevel(skill) < maxLevel; i++) {
					skillModel.setSkillLevel(skill, skillModel.getSkillLevel(skill) + 1, player);
				}

				updatePlayerAttribute(player, skill, skillModel.getSkillLevel(skill));
				skillModel.updateTotalLevel();
				SyncToClientPacket.send(player);
			}
		});

		context.get().setPacketHandled(true);
	}

	private void updatePlayerAttribute(ServerPlayer player, Skill skill, int level) {
		Attribute attribute = getAttributeForSkill(skill);
		if (attribute == null) return;

		AttributeInstance instance = player.getAttribute(attribute);
		if (instance == null) return;

		UUID uuid = ModAttributes.getModifierUUIDForSkill(skill);

		if (instance.getModifier(uuid) != null) {
			instance.removeModifier(uuid);
		}

		double bonus = level - 1;

		AttributeModifier modifier = new AttributeModifier(uuid, skill.displayName, bonus, AttributeModifier.Operation.ADDITION);

		instance.addPermanentModifier(modifier);
	}



	private Attribute getAttributeForSkill(Skill skill) {
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
