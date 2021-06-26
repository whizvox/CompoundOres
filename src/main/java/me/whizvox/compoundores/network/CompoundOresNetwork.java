package me.whizvox.compoundores.network;

import me.whizvox.compoundores.CompoundOres;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class CompoundOresNetwork {

  private static final String PROTOCOL_VERSION = "1";

  public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
    new ResourceLocation(CompoundOres.MOD_ID, "main"),
    () -> PROTOCOL_VERSION,
    version -> version.equals(PROTOCOL_VERSION),
    version -> version.equals(PROTOCOL_VERSION)
  );

  public static <T> void noEncode(T message, PacketBuffer buffer) {}

  private static boolean initialized = false;

  public static void registerPackets() {
    if (initialized) {
      return;
    }

    int id = 0;
    CHANNEL.registerMessage(id, GeneratePacket.class, GeneratePacket::encode, GeneratePacket::decode, GeneratePacket::handle);

    initialized = true;
  }

  public static void sendToPlayer(Object msg, ServerPlayerEntity player) {
    CHANNEL.sendTo(msg, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
  }

}
