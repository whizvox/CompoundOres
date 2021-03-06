package me.whizvox.compoundores;

import me.whizvox.compoundores.api.CompoundOresObjects;
import me.whizvox.compoundores.api.component.OreComponentRegistry;
import me.whizvox.compoundores.command.CompoundOresCommands;
import me.whizvox.compoundores.config.CompoundOresConfig;
import me.whizvox.compoundores.data.CompoundOresDataGenerator;
import me.whizvox.compoundores.network.CompoundOresNetwork;
import me.whizvox.compoundores.render.CompoundOreTileRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.GenerationStage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CompoundOres.MOD_ID)
public class CompoundOres {

  public static final String MOD_ID = "compoundores";

  public static final Logger LOGGER = LogManager.getLogger("CompoundOres");

  public static final ItemGroup ITEM_GROUP_ORES = new ItemGroup("compoundores.ores") {
    @Override
    public ItemStack makeIcon() {
      return CompoundOresObjects.getOresItemGroupIcon();
    }
  };

  public CompoundOres() {
    CompoundOresConfig.register(ModLoadingContext.get());

    IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
    modBus.addListener(OreComponentRegistry::onNewRegistry);
    modBus.register(CompoundOresObjects.class);
    modBus.addListener(CompoundOresDataGenerator::gatherData);
    modBus.addListener(this::onClientSetup);
    modBus.addListener(this::onCommonSetup);

    IEventBus forgeBus = MinecraftForge.EVENT_BUS;
    forgeBus.addListener(this::onRegisterCommands);
    forgeBus.addListener(this::onBiomeModify);
  }

  private void onCommonSetup(final FMLCommonSetupEvent event) {
    CompoundOresNetwork.registerPackets();
  }

  private void onClientSetup(final FMLClientSetupEvent event) {
    CompoundOresObjects.blocks.values().forEach(block -> RenderTypeLookup.setRenderLayer(block, RenderType.cutoutMipped()));
    ClientRegistry.bindTileEntityRenderer(CompoundOresObjects.tileEntityType, CompoundOreTileRenderer::new);
  }

  public void onRegisterCommands(final RegisterCommandsEvent event) {
    CompoundOresCommands.register(event.getDispatcher());
  }

  public void onBiomeModify(final BiomeLoadingEvent event) {
    if (CompoundOresConfig.COMMON.generateCompoundOres.get()) {
      event.getGeneration().getFeatures(GenerationStage.Decoration.UNDERGROUND_ORES).add(() -> CompoundOresObjects.configuredFeature);
    }
  }

}
