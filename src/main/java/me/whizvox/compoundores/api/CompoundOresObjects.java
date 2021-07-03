package me.whizvox.compoundores.api;

import me.whizvox.compoundores.CompoundOres;
import me.whizvox.compoundores.api.component.OreComponent;
import me.whizvox.compoundores.api.component.OreComponentRegistry;
import me.whizvox.compoundores.api.component.OreComponents;
import me.whizvox.compoundores.config.CompoundOresConfig;
import me.whizvox.compoundores.helper.NBTHelper;
import me.whizvox.compoundores.obj.CompoundOreBlock;
import me.whizvox.compoundores.obj.CompoundOreBlockItem;
import me.whizvox.compoundores.obj.CompoundOreTile;
import me.whizvox.compoundores.world.feature.CompoundOreFeature;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static me.whizvox.compoundores.CompoundOres.LOGGER;
import static me.whizvox.compoundores.helper.Markers.REGISTRY;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CompoundOresObjects {

  private static final ResourceLocation
      PREFERRED_ITEMGROUP_ICON_PRIMARY = OreComponents.COAL.getRegistryName(),
      PREFERRED_ITEMGROUP_ICON_SECONDARY = OreComponents.DIAMOND.getRegistryName();

  public static final ITag.INamedTag<Block> COMPOUND_ORES_BLOCK_TAG = BlockTags.createOptional(new ResourceLocation(CompoundOres.MOD_ID, "compound_ore"));
  public static final ITag.INamedTag<Item> COMPOUND_ORES_ITEM_TAG = ItemTags.createOptional(new ResourceLocation(CompoundOres.MOD_ID, "compound_ore"));

  public static Map<ResourceLocation, CompoundOreBlock> blocks;
  public static Map<ResourceLocation, CompoundOreBlockItem> blockItems;
  public static CompoundOreFeature feature;
  public static ConfiguredFeature<?, ?> configuredFeature;
  public static TileEntityType<CompoundOreTile> tileEntityType;
  private static ItemStack oresItemGroupIcon = ItemStack.EMPTY;

  public static ItemStack getOresItemGroupIcon() {
    return oresItemGroupIcon;
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

  @SubscribeEvent
  public static void onRegisterBlocks(final RegistryEvent.Register<Block> event) {
    Map<ResourceLocation, CompoundOreBlock> tempCompOres = new HashMap<>();
    OreComponentRegistry.getInstance().getValues().forEach(oreComp -> {
      AbstractBlock.Properties props;
      if (oreComp.getMaterialColor() == null) {
        props = AbstractBlock.Properties.of(oreComp.getMaterial().material);
      } else {
        props = AbstractBlock.Properties.of(oreComp.getMaterial().material, oreComp.getMaterialColor().color);
      }
      props
        .strength(oreComp.getHardness(), oreComp.getResistance())
        .harvestLevel(oreComp.getHarvestLevel())
        .sound(oreComp.getSound().sound);
      if (oreComp.getHarvestTool() != null) {
        props.harvestTool(oreComp.getHarvestTool());
        if (oreComp.isToolRequired()) {
          props.requiresCorrectToolForDrops();
        }
      }
      CompoundOreBlock block = new CompoundOreBlock(props, oreComp);
      registerBlock("compound_ore_" + oreComp.getRegistryName().getPath(), block);
      LOGGER.debug(REGISTRY, "Registered compound ore block {} for component {}", block.getRegistryName(), oreComp.getRegistryName());
      tempCompOres.put(oreComp.getRegistryName(), block);
    });
    blocks = Collections.unmodifiableMap(tempCompOres);
    LOGGER.info(REGISTRY, "Registered {} total compound ore blocks", blocks.size());
  }

  @SubscribeEvent
  public static void onRegisterItems(final RegistryEvent.Register<Item> event) {
    Map<ResourceLocation, CompoundOreBlockItem> tempBlockItems = new HashMap<>();
    // register items alphabetically so it looks neat in the creative menu and JEI
    OreComponentRegistry.getInstance().getValues().stream().sorted(Comparator.comparing(OreComponent::getRegistryName)).forEach(oreComp -> {
      final ResourceLocation key = oreComp.getRegistryName();
      CompoundOreBlockItem item = new CompoundOreBlockItem(blocks.get(key), new Item.Properties().tab(CompoundOres.ITEM_GROUP_ORES));
      registerItem("compound_ore_" + oreComp.getRegistryName().getPath(), item);
      LOGGER.debug(REGISTRY, "Registered compound ore block item {} for component {}", item.getRegistryName(), key);
      tempBlockItems.put(key, item);
    });
    blockItems = Collections.unmodifiableMap(tempBlockItems);
    LOGGER.debug(REGISTRY, "Registered {} total compound ore block items", blockItems.size());

    if (oresItemGroupIcon.isEmpty() && !blockItems.isEmpty() && OreComponentRegistry.getInstance().getValues().size() > 1) {
      if (blockItems.containsKey(PREFERRED_ITEMGROUP_ICON_PRIMARY) && OreComponentRegistry.getInstance().containsKey(PREFERRED_ITEMGROUP_ICON_SECONDARY)) {
        CompoundOreBlockItem blockItem = blockItems.get(PREFERRED_ITEMGROUP_ICON_PRIMARY);
        ItemStack stack = new ItemStack(blockItem);
        OreComponent secondary = OreComponentRegistry.getInstance().getValue(PREFERRED_ITEMGROUP_ICON_SECONDARY);
        NBTHelper.writeOreComponent(stack, CompoundOreBlockItem.TAG_SECONDARY, secondary);
        oresItemGroupIcon = stack;
        LOGGER.debug(REGISTRY, "Set the preferred compound ore creative tab icon : {} / {}", PREFERRED_ITEMGROUP_ICON_PRIMARY, PREFERRED_ITEMGROUP_ICON_SECONDARY);
      } else {
        CompoundOreBlockItem blockItem = blockItems.values().stream().findAny().get();
        OreComponent randSecondary = OreComponentRegistry.getInstance().getValues().stream().filter(c -> c != ((CompoundOreBlock) blockItem.getBlock()).getPrimaryComponent()).findAny().get();
        if (!randSecondary.isEmpty()) {
          ItemStack stack = new ItemStack(blockItem);
          NBTHelper.writeOreComponent(stack, CompoundOreBlockItem.TAG_SECONDARY, randSecondary);
          oresItemGroupIcon = stack;
          LOGGER.debug(
            REGISTRY,
            "Could not find preferred compound ore creative tab icon. Set it to {} / {} instead",
            ((CompoundOreBlock) blockItem.getBlock()).getPrimaryComponent().getRegistryName(),
            randSecondary.getRegistryName()
          );
        }
      }
    }
    if (oresItemGroupIcon.isEmpty()) {
      LOGGER.warn(REGISTRY, "Could not set an icon for the compound ores creative tab as there needs to be at least two registered components");
    }
  }

  @SubscribeEvent
  public static void onRegisterTileTypes(final RegistryEvent.Register<TileEntityType<?>> event) {
    tileEntityType = registerTileType("compound_ore", CompoundOreTile::new, blocks.values().toArray(new Block[0]));
    LOGGER.debug(REGISTRY, "Registered base compound ore tile entity type");
  }

  @SubscribeEvent
  public static void onRegisterFeatures(final RegistryEvent.Register<Feature<?>> event) {
    if (CompoundOresConfig.COMMON.generateCompoundOres.get()) {
      feature = new CompoundOreFeature();
      feature.setRegistryName(CompoundOres.MOD_ID, "compound_ore");
      event.getRegistry().register(feature);
      LOGGER.debug(REGISTRY, "Registered compound ore feature");

      configuredFeature = feature
        .configured(NoFeatureConfig.INSTANCE)
        .range(128)
        .squared()
        .count(CompoundOresConfig.COMMON.spawnChecks.get());
      Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, "compoundores:compound_ore", configuredFeature);
      LOGGER.debug(REGISTRY, "Registered configured compound ore feature");
    }
  }

}
