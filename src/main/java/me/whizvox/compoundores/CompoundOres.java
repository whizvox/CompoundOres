package me.whizvox.compoundores;

import me.whizvox.compoundores.api.CompoundOresObjects;
import me.whizvox.compoundores.api.OreComponentRegistry;
import me.whizvox.compoundores.render.CompoundOreTileRenderer;
import me.whizvox.compoundores.command.CompoundOresCommands;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CompoundOres.MOD_ID)
public class CompoundOres {

  public static final String MOD_ID = "compoundores";

  public static final Logger LOGGER = LogManager.getLogger();

  public static final ItemGroup ORES_CATEGORY = new ItemGroup("compoundores.ores") {
    private final LazyOptional<ItemStack> iconOp = LazyOptional.of(() -> {
      if (CompoundOresObjects.compoundOreBlocks.isEmpty()) {
        return ItemStack.EMPTY;
      }
      return new ItemStack(CompoundOresObjects.getOresCategoryIcon());
    });
    @Override
    public ItemStack makeIcon() {
      return iconOp.resolve().get();
    }
  };

  public CompoundOres() {
    IEventBus meBus = FMLJavaModLoadingContext.get().getModEventBus();
    meBus.register(OreComponentRegistry.class);
    meBus.register(CompoundOresObjects.class);
    meBus.addListener(this::onClientSetup);

    IEventBus forgeBus = MinecraftForge.EVENT_BUS;
    forgeBus.register(this);
  }

  private void onClientSetup(final FMLClientSetupEvent event) {
    CompoundOresObjects.compoundOreBlocks.values().forEach(block -> RenderTypeLookup.setRenderLayer(block, RenderType.cutoutMipped()));
    ClientRegistry.bindTileEntityRenderer(CompoundOresObjects.compoundOreTileType, CompoundOreTileRenderer::new);
  }

  @SubscribeEvent
  public void onRegisterCommands(final RegisterCommandsEvent event) {
    CompoundOresCommands.register(event.getDispatcher());
  }

}
