package me.whizvox.compoundores.network;

import me.whizvox.compoundores.api.component.OreComponentRegistry;
import me.whizvox.compoundores.helper.JsonHelper;
import me.whizvox.compoundores.helper.PathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static me.whizvox.compoundores.CompoundOres.LOGGER;
import static me.whizvox.compoundores.helper.Markers.CLIENT;

public class ExportComponentsPacket {

  public final Which which;

  public ExportComponentsPacket(Which which) {
    this.which = which;
  }

  public enum Which {
    COMPONENTS,
    GROUPS,
    BOTH
  }

  public static void encode(ExportComponentsPacket message, PacketBuffer buffer) {
    buffer.writeEnum(message.which);
  }

  public static ExportComponentsPacket decode(PacketBuffer buffer) {
    return new ExportComponentsPacket(buffer.readEnum(Which.class));
  }

  public static void handle(ExportComponentsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
    contextSupplier.get().enqueueWork(() -> {
      AtomicInteger count = new AtomicInteger(0);
      if (packet.which == Which.COMPONENTS || packet.which == Which.BOTH) {
        OreComponentRegistry.getInstance().getValues().forEach(oreComp -> {
          final Path genFilePath = PathHelper.EXPORT_COMPONENTS_DIR.resolve(oreComp.getRegistryName().getPath() + ".json");
          try (Writer out = Files.newBufferedWriter(genFilePath, StandardCharsets.UTF_8)) {
            JsonHelper.GSON.toJson(oreComp, out);
            LOGGER.debug(CLIENT, "Exported JSON for ore component {} at {}", oreComp.getRegistryName(), genFilePath);
            count.incrementAndGet();
          } catch (IOException e) {
            LOGGER.error(CLIENT, "Could not export JSON file for ore component: " + genFilePath, e);
          }
        });
      }
      if (packet.which == Which.GROUPS || packet.which == Which.BOTH) {
        OreComponentRegistry.getInstance().forEachGroup((name, group) -> {
          final Path genFilePath = PathHelper.EXPORT_GROUPS_DIR.resolve(name + ".json");
          try (Writer out = Files.newBufferedWriter(genFilePath, StandardCharsets.UTF_8)) {
            JsonHelper.GSON.toJson(group.stream().map(ResourceLocation::toString).collect(Collectors.toList()), out);
            LOGGER.debug(CLIENT, "Exported JSON file for group {} at {}", name, genFilePath);
            count.incrementAndGet();
          } catch (IOException e) {
            LOGGER.error(CLIENT, "Could not export JSON file for group: " + genFilePath, e);
          }
        });
      }
      LOGGER.info(CLIENT, "Exported {} JSON files for {}", count.get(), packet.which == Which.BOTH ? "components and groups" : packet.which.toString().toLowerCase());
      Minecraft.getInstance().player.displayClientMessage(
        new TranslationTextComponent("message.compoundores.debugCommand.generate." + packet.which.toString().toLowerCase(),
          new StringTextComponent("compoundores/export")
            .withStyle(TextFormatting.UNDERLINE)
            .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, PathHelper.EXPORT_DIR.toString())))
        ), false
      );
    });
  }

}
