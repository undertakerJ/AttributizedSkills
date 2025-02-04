package net.lumi_noble.rpgskillable_fork.common.network;

import java.util.function.Supplier;

import net.lumi_noble.rpgskillable_fork.RpgSkillable;
import net.lumi_noble.rpgskillable_fork.common.capabilities.SkillModel;
import net.lumi_noble.rpgskillable_fork.common.skill.Skill;

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

			SyncToClient.send(player);
		});

		context.get().setPacketHandled(true);
	}

	public static void send(Skill skill) {
		RpgSkillable.NETWORK.sendToServer(new RequestLevelUp(skill));
	}

}
