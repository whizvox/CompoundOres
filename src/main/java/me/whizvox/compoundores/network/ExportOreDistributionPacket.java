package me.whizvox.compoundores.network;

import me.whizvox.compoundores.helper.JsonHelper;
import me.whizvox.compoundores.helper.PathHelper;
import me.whizvox.compoundores.util.OreDistribution;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

import static me.whizvox.compoundores.CompoundOres.LOGGER;
import static me.whizvox.compoundores.helper.Markers.CLIENT;

public class ExportOreDistributionPacket {

  /*
    BINARY FORMAT:
    dimension name (ResourceLocation)
    number of scanned chunks (short)
    - xChunk (int)
    - zChunk (int)
    number of skipped chunks (short)
    - xChunk (int)
    - zChunk (int)
    number of total scanned blocks (int)
    number of unique blocks (short)
    - block registry name (ResourceLocation)
    - number of block positions (int)
      - block position (BlockPos)
   */

  private static final String
      KEY_EXCEPTION = "message.compoundores.debugCommand.oredist.export.exception",
      KEY_SUCCESS = "message.compoundores.debugCommand.oredist.export.success";

  public final OreDistribution dist;

  public ExportOreDistributionPacket(OreDistribution dist) {
    this.dist = dist;
  }

  public static void encode(ExportOreDistributionPacket packet, PacketBuffer buffer) {
    buffer.writeResourceLocation(packet.dist.getDimension());
    buffer.writeShort(packet.dist.getChunksScanned().size());
    packet.dist.getChunksScanned().forEach(cPos -> {
      buffer.writeInt(cPos.x);
      buffer.writeInt(cPos.z);
    });
    buffer.writeShort(packet.dist.getChunksSkipped().size());
    packet.dist.getChunksSkipped().forEach(cPos -> {
      buffer.writeInt(cPos.x);
      buffer.writeInt(cPos.z);
    });
    buffer.writeInt(packet.dist.getTotalScanned());
    buffer.writeShort(packet.dist.getRaw().size());
    packet.dist.getRaw().forEach((name, positions) -> {
      buffer.writeResourceLocation(name);
      buffer.writeInt(positions.size());
      positions.forEach(buffer::writeBlockPos);
    });
  }

  public static ExportOreDistributionPacket decode(PacketBuffer buffer) {
    Map<ResourceLocation, Collection<BlockPos>> raw = new HashMap<>();
    ResourceLocation dimensionId = buffer.readResourceLocation();
    final int chunksScanned = buffer.readShort();
    List<ChunkPos> scannedChunks = new ArrayList<>(chunksScanned);
    for (int i = 0; i < chunksScanned; i++) {
      scannedChunks.add(new ChunkPos(buffer.readInt(), buffer.readInt()));
    }
    final int chunksSkipped = buffer.readShort();
    List<ChunkPos> skippedChunks = new ArrayList<>(chunksSkipped);
    for (int i = 0; i < chunksSkipped; i++) {
      skippedChunks.add(new ChunkPos(buffer.readInt(), buffer.readInt()));
    }
    int totalScanned = buffer.readInt();
    final int uniqueBlocksCount = buffer.readShort();
    for (int i = 0; i < uniqueBlocksCount; i++) {
      ResourceLocation name = buffer.readResourceLocation();
      int positionsCount = buffer.readInt();
      Collection<BlockPos> positions = new ArrayList<>(positionsCount);
      for (int j = 0; j < positionsCount; j++) {
        positions.add(buffer.readBlockPos());
      }
      raw.put(name, positions);
    }
    return new ExportOreDistributionPacket(new OreDistribution(dimensionId, scannedChunks, skippedChunks, totalScanned, raw));
  }

  public static void handle(ExportOreDistributionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
    contextSupplier.get().enqueueWork(() -> {
      Path path = PathHelper.EXPORT_DIR.resolve("ore_distribution.json");
      try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
        JsonHelper.GSON.toJson(packet.dist, writer);
        Minecraft.getInstance().player.displayClientMessage(new TranslationTextComponent(KEY_SUCCESS,
          new StringTextComponent("compoundores/export/ore_distribution.json")
            .withStyle(TextFormatting.UNDERLINE)
            .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path.toString())))
          ), false);
      } catch (IOException e) {
        LOGGER.error(CLIENT, "Could not export ore distribution in JSON format to file", e);
        Minecraft.getInstance().player.displayClientMessage(new TranslationTextComponent(KEY_EXCEPTION, e.getMessage()), false);
      }
    });
  }

}
