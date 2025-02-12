package net.lumi_noble.attributizedskills.common.network;

import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.lumi_noble.attributizedskills.common.network.packets.RemoveAllBonusesPacket;
import net.lumi_noble.attributizedskills.common.network.packets.RequestLevelUpPacket;
import net.lumi_noble.attributizedskills.common.network.packets.SyncToClientPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
  private static SimpleChannel INSTANCE;

  private static int packetId = 0;

  private static int id() {
    return packetId++;
  }

  public static void register() {
    SimpleChannel net =
        NetworkRegistry.ChannelBuilder.named(new ResourceLocation(AttributizedSkills.MOD_ID, "messages"))
            .networkProtocolVersion(() -> "1.2")
            .clientAcceptedVersions(s -> true)
            .serverAcceptedVersions(s -> true)
            .simpleChannel();

    INSTANCE = net;
    INSTANCE
        .messageBuilder(SyncToClientPacket.class, 1)
        .encoder(SyncToClientPacket::encode)
        .decoder(SyncToClientPacket::new)
        .consumerMainThread(SyncToClientPacket::handle)
        .add();
    INSTANCE
        .messageBuilder(RequestLevelUpPacket.class, 2)
        .encoder(RequestLevelUpPacket::encode)
        .decoder(RequestLevelUpPacket::new)
        .consumerMainThread(RequestLevelUpPacket::handle)
        .add();
        INSTANCE
        .messageBuilder(RemoveAllBonusesPacket.class, 3)
        .encoder(RemoveAllBonusesPacket::encode)
        .decoder(RemoveAllBonusesPacket::new)
        .consumerMainThread(RemoveAllBonusesPacket::handle)
        .add();


  }

  public static <MSG> void sendToServer(MSG message) {
    INSTANCE.sendToServer(message);
  }

  public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
    INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
  }

  public static void sendToAllPlayers(Object message, ServerLevel level) {
    level
        .players()
        .forEach(
            player -> {
              INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
            });
  }
}
