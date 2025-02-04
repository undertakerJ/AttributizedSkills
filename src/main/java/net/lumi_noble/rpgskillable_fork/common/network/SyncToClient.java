package net.lumi_noble.rpgskillable_fork.common.network;

import java.util.function.Supplier;

import net.lumi_noble.rpgskillable_fork.RpgSkillable;
import net.lumi_noble.rpgskillable_fork.common.capabilities.SkillModel;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class SyncToClient {
	
	private final CompoundTag skillModel;
	
	public SyncToClient(CompoundTag skillModel) {
		this.skillModel = skillModel;
	}
	
	public SyncToClient(FriendlyByteBuf buf) {
		this.skillModel = buf.readNbt();
	}
	
	public void encode(FriendlyByteBuf buf) {
		buf.writeNbt(skillModel);
	}
	
	public void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> SkillModel.get().deserializeNBT(skillModel));
		context.get().setPacketHandled(true);
	}
	
	public static void send(Player player) {
		RpgSkillable.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), new SyncToClient(SkillModel.get(player).serializeNBT()));
	}

}
