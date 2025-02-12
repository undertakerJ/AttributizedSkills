package net.lumi_noble.attributizedskills.common.network;

import java.util.function.Supplier;

import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.network.packets.RequestLevelUpPacket;
import net.lumi_noble.attributizedskills.common.network.packets.SyncToClientPacket;
import net.lumi_noble.attributizedskills.common.skill.Skill;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class RequestReset {




	public void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			
			ServerPlayer player = context.get().getSender();
			SkillModel skillModel = SkillModel.get(player);
			
			if (!player.isCreative()) {
				player.giveExperienceLevels(skillModel.getTotalLevel());
			}
			
			skillModel.resetSkills(player);

			SyncToClientPacket.send(player);
		});

		context.get().setPacketHandled(true);
	}


}
