package me.whizvox.compoundores.network;

import me.whizvox.compoundores.api.OreComponentRegistry;
import me.whizvox.compoundores.util.JsonHelper;
import me.whizvox.compoundores.util.PathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class GenerateComponentsPacket {

  private static final Logger LOGGER = LogManager.getLogger();

  // IntelliJ keeps giving me warnings about how I shouldn't "create instances of utility classes". This'll shut it up.
  private int dummy = 0;

  public GenerateComponentsPacket() {
  }

  public static GenerateComponentsPacket decode(PacketBuffer buffer) {
    return new GenerateComponentsPacket();
  }

  public static void handle(GenerateComponentsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
    contextSupplier.get().enqueueWork(() -> {
      AtomicInteger count = new AtomicInteger(0);
      OreComponentRegistry.getInstance().getValues().forEach(oreComp -> {
        final Path genFilePath = PathHelper.GENERATED_DIR.resolve(oreComp.getRegistryName().getPath() + ".json");
        try (Writer out = Files.newBufferedWriter(genFilePath, StandardCharsets.UTF_8)) {
          JsonHelper.GSON.toJson(oreComp, out);
          LOGGER.debug("Generated JSON for default ore component {} at {}", oreComp.getRegistryName(), genFilePath);
          count.incrementAndGet();
        } catch (IOException e) {
          LOGGER.error("Could not generate JSON for default ore component: " + genFilePath, e);
        }
      });
      LOGGER.info("Generated {} JSON files for default components", count.get());
      Minecraft.getInstance().player.displayClientMessage(
        new TranslationTextComponent("message.compoundores.debugCommand.generate.success",
          new StringTextComponent("compoundores/generated")
            .withStyle(TextFormatting.UNDERLINE)
            .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, PathHelper.GENERATED_DIR.toString())))
        ), false
      );
    });
  }

}
