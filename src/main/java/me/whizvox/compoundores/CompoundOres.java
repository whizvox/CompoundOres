package me.whizvox.compoundores;

import me.whizvox.compoundores.api.CompoundOresObjects;
import me.whizvox.compoundores.api.OreComponentRegistry;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
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
    IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
    modBus.register(OreComponentRegistry.class);
    modBus.register(CompoundOresObjects.class);
  }

}
