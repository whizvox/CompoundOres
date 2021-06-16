package me.whizvox.compoundores.api;

import me.whizvox.compoundores.CompoundOres;
import me.whizvox.compoundores.obj.CompoundOreBlock;
import me.whizvox.compoundores.obj.CompoundOreBlockItem;
import me.whizvox.compoundores.obj.CompoundOreTile;
import me.whizvox.compoundores.world.feature.CompoundOreFeature;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
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

  public static Map<ResourceLocation, CompoundOreBlock> blocks;
  public static Map<ResourceLocation, CompoundOreBlockItem> blockItems;
  public static CompoundOreFeature feature;
  public static ConfiguredFeature<?, ?> configuredFeature;
  public static TileEntityType<CompoundOreTile> tileEntityType;
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
        LOGGER.debug("Registered compound ore block {} for component {}", block.getRegistryName(), oreComp.getRegistryName());
        tempCompOres.put(oreComp.getRegistryName(), block);
      }
    });
    blocks = Collections.unmodifiableMap(tempCompOres);
    LOGGER.info("Registered {} total compound ore blocks", blocks.size());
  }

  @SubscribeEvent
  public static void onRegisterItems(final RegistryEvent.Register<Item> event) {
    Map<ResourceLocation, CompoundOreBlockItem> tempComp = new HashMap<>();
    // register items alphabetically so it looks neat in the creative menu and JEI
    OreComponentRegistry.instance.getSortedValues().forEach(oreComp -> {
      final ResourceLocation key = oreComp.getRegistryName();
      CompoundOreBlockItem item = new CompoundOreBlockItem(blocks.get(key), new Item.Properties().tab(CompoundOres.ORES_CATEGORY));
      if (oresCategoryIcon == null) {
        oresCategoryIcon = item;
      }
      registerItem("compound_ore_" + oreComp.getRegistryName().getPath(), item);
      LOGGER.debug("Registered compound ore block item {} for component {}", item.getRegistryName(), key);
      tempComp.put(key, item);
    });
    blockItems = Collections.unmodifiableMap(tempComp);
  }

  @SubscribeEvent(priority = EventPriority.NORMAL)
  public static void onRegisterTileTypes(final RegistryEvent.Register<TileEntityType<?>> event) {
    tileEntityType = registerTileType("compound_ore", CompoundOreTile::new, blocks.values().toArray(new Block[0]));
    CompoundOres.LOGGER.debug("Registered base compound ore tile entity type");
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public static void onRegisterFeatures(final RegistryEvent.Register<Feature<?>> event) {
    feature = new CompoundOreFeature();
    feature.setRegistryName(CompoundOres.MOD_ID, "compound_ore");
    event.getRegistry().register(feature);

    configuredFeature = feature
      .configured(NoFeatureConfig.INSTANCE)
      .range(128)
      .squared()
      .count(40);
    Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, "compoundores:compound_ore", configuredFeature);
  }

}
