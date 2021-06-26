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

public class GeneratePacket {

  public final Which which;

  public GeneratePacket(Which which) {
    this.which = which;
  }

  public enum Which {
    COMPONENTS,
    GROUPS,
    BOTH
  }

  public static void encode(GeneratePacket message, PacketBuffer buffer) {
    buffer.writeEnum(message.which);
  }

  public static GeneratePacket decode(PacketBuffer buffer) {
    return new GeneratePacket(buffer.readEnum(Which.class));
  }

  public static void handle(GeneratePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
    contextSupplier.get().enqueueWork(() -> {
      AtomicInteger count = new AtomicInteger(0);
      if (packet.which == Which.COMPONENTS || packet.which == Which.BOTH) {
        OreComponentRegistry.getInstance().getValues().forEach(oreComp -> {
          final Path genFilePath = PathHelper.GEN_COMPONENTS_DIR.resolve(oreComp.getRegistryName().getPath() + ".json");
          try (Writer out = Files.newBufferedWriter(genFilePath, StandardCharsets.UTF_8)) {
            JsonHelper.GSON.toJson(oreComp, out);
            LOGGER.debug(CLIENT, "Generated JSON for ore component {} at {}", oreComp.getRegistryName(), genFilePath);
            count.incrementAndGet();
          } catch (IOException e) {
            LOGGER.error(CLIENT, "Could not generate JSON file for ore component: " + genFilePath, e);
          }
        });
      }
      if (packet.which == Which.GROUPS || packet.which == Which.BOTH) {
        OreComponentRegistry.getInstance().forEachGroup((name, group) -> {
          final Path genFilePath = PathHelper.GEN_GROUPS_DIR.resolve(name + ".json");
          try (Writer out = Files.newBufferedWriter(genFilePath, StandardCharsets.UTF_8)) {
            JsonHelper.GSON.toJson(group.stream().map(ResourceLocation::toString).collect(Collectors.toList()), out);
            LOGGER.debug(CLIENT, "Generate JSON file for group {} at {}", name, genFilePath);
            count.incrementAndGet();
          } catch (IOException e) {
            LOGGER.error(CLIENT, "Could not generate JSON file for group: " + genFilePath, e);
          }
        });
      }
      LOGGER.info(CLIENT, "Generated {} JSON files for {}", count.get(), packet.which == Which.BOTH ? "components and groups" : packet.which.toString().toLowerCase());
      Minecraft.getInstance().player.displayClientMessage(
        new TranslationTextComponent("message.compoundores.debugCommand.generate." + packet.which.toString().toLowerCase(),
          new StringTextComponent("compoundores/generated")
            .withStyle(TextFormatting.UNDERLINE)
            .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, PathHelper.GENERATED_DIR.toString())))
        ), false
      );
    });
  }

}
