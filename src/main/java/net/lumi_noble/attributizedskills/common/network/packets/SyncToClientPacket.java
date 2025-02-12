package net.lumi_noble.attributizedskills.common.network.packets;

import java.util.function.Supplier;

import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;

import net.lumi_noble.attributizedskills.common.network.ModNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class SyncToClientPacket {
	
	private final CompoundTag skillModel;
	
	public SyncToClientPacket(CompoundTag skillModel) {
		this.skillModel = skillModel;
	}
	
	public SyncToClientPacket(FriendlyByteBuf buf) {
		this.skillModel = buf.readNbt();
	}
	
	public void encode(FriendlyByteBuf buf) {
		buf.writeNbt(skillModel);
	}
	
	public void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> SkillModel.get().deserializeNBT(skillModel));
		context.get().setPacketHandled(true);
	}
	
	public static void send(ServerPlayer player) {
		ModNetworking.sendToPlayer(new SyncToClientPacket(SkillModel.get(player).serializeNBT()), player);
	}

}
