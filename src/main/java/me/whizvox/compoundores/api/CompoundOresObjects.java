package me.whizvox.compoundores.api;

import me.whizvox.compoundores.CompoundOres;
import me.whizvox.compoundores.obj.CompoundOreBlock;
import me.whizvox.compoundores.obj.CompoundOreBlockItem;
import me.whizvox.compoundores.obj.CompoundOreTile;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CompoundOresObjects {

  private static final Logger LOGGER = LogManager.getLogger();

  public static Map<ResourceLocation, CompoundOreBlock> compoundOreBlocks;
  public static Map<ResourceLocation, CompoundOreBlockItem> compoundOreBlockItems;
  public static TileEntityType<CompoundOreTile> compoundOreTileType;

  private static Item oresCategoryIcon;

  public static Item getOresCategoryIcon() {
    return oresCategoryIcon;
  }

  private static <T extends IForgeRegistryEntry<?>> T register(IForgeRegistry<? super T> registry, String name, T entry) {
    entry.setRegistryName(new ResourceLocation(CompoundOres.MOD_ID, name));
    registry.register(entry);
    return entry;
  }

  private static <T extends Block> T registerBlock(String name, T block) {
    return register(ForgeRegistries.BLOCKS, name, block);
  }

  private static <T extends TileEntity> TileEntityType<T> registerTileType(String name, Supplier<T> supplier, Block... blocks) {
    return register(ForgeRegistries.TILE_ENTITIES, name, TileEntityType.Builder.of(supplier, blocks).build(null));
  }

  private static <T extends Item> T registerItem(String name, T item) {
    return register(ForgeRegistries.ITEMS, name, item);
  }

  @SubscribeEvent(priority = EventPriority.HIGH)
  public static void onRegisterBlocks(final RegistryEvent.Register<Block> event) {
    CompoundOres.LOGGER.info("REGISTERING BLOCKS");
    Map<ResourceLocation, CompoundOreBlock> tempCompOres = new HashMap<>();
    OreComponentRegistry.instance.getSortedValues().forEach(oreComp -> {
      if (!oreComp.isEmpty()) {
        CompoundOreBlock block = new CompoundOreBlock(AbstractBlock.Properties.of(Material.STONE, oreComp.getBlock().defaultMaterialColor())
          .requiresCorrectToolForDrops()
          .harvestTool(ToolType.PICKAXE)
          .strength(oreComp.getDestroySpeed(), oreComp.getBlastResistance())
          .harvestLevel(oreComp.getHarvestLevel()), oreComp);
        registerBlock("compound_ore_" + oreComp.getRegistryName().getPath(), block);
        tempCompOres.put(oreComp.getRegistryName(), block);
      }
    });
    compoundOreBlocks = Collections.unmodifiableMap(tempCompOres);
    LOGGER.info("Registered {} total compound ore blocks", compoundOreBlocks.size());
  }

  @SubscribeEvent
  public static void onRegisterItems(final RegistryEvent.Register<Item> event) {
    CompoundOres.LOGGER.info("REGISTERING ITEMS");
    Map<ResourceLocation, CompoundOreBlockItem> tempComp = new HashMap<>();
    OreComponentRegistry.instance.getSortedValues().forEach(oreComp -> {
      final ResourceLocation key = oreComp.getRegistryName();
      CompoundOreBlockItem item = new CompoundOreBlockItem(compoundOreBlocks.get(key), new Item.Properties().tab(CompoundOres.ORES_CATEGORY));
      if (oresCategoryIcon == null) {
        oresCategoryIcon = item;
      }
      registerItem("compound_ore_" + oreComp.getRegistryName().getPath(), item);
      tempComp.put(key, item);
    });
    compoundOreBlockItems = Collections.unmodifiableMap(tempComp);
  }

  @SubscribeEvent
  public static void onRegisterTileTypes(final RegistryEvent.Register<TileEntityType<?>> event) {
    CompoundOres.LOGGER.info("REGISTERING ITEMS");
    compoundOreTileType = registerTileType("compound_ore", CompoundOreTile::new, compoundOreBlocks.values().toArray(new Block[0]));
  }

}
