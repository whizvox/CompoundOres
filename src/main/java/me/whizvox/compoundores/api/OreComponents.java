package me.whizvox.compoundores.api;

import me.whizvox.compoundores.CompoundOres;
import me.whizvox.compoundores.config.CompoundOresConfig;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class OreComponents {

  private static final Logger LOGGER = LogManager.getLogger();

  public static OreComponent
      // Vanilla ores
      COAL,
      IRON,
      GOLD,
      LAPIS,
      REDSTONE,
      DIAMOND,
      EMERALD,
      NETHER_GOLD,
      NETHER_QUARTZ,
      ANCIENT_DEBRIS,
      // Common modded ores
      COPPER,
      TIN,
      ALUMINUM,
      OSMIUM,
      URANIUM,
      NICKEL,
      ZINC,
      SILVER,
      LEAD,
      BISMUTH,
      SAPPHIRE,
      RUBY,
      IRIDIUM,
      PLATINUM,
      SULFUR,
      FLUORITE,
      // RFToolsBase
      DIMENSIONAL_SHARD;

  private static boolean initialized = false;
  private static List<OreComponent> exceptionsList = null;
  private static boolean whitelistExceptions;

  static void registerAll() {
    if (initialized) {
      return;
    }

    COAL = register("coal", OreComponent.builder().block(Blocks.COAL_ORE).spawnWeight(12).color(0x404040).type(OreType.NONMETAL).build());
    IRON = register("iron", OreComponent.builder().block(Blocks.IRON_ORE).spawnWeight(8).color(0xd8af93).type(OreType.METAL).harvestLevel(1).build());
    LAPIS = register("lapis", OreComponent.builder().block(Blocks.LAPIS_ORE).spawnWeight(3).color(0x2f4ef9).type(OreType.NONMETAL).harvestLevel(1).build());
    GOLD = register("gold", OreComponent.builder().block(Blocks.GOLD_ORE).spawnWeight(4).color(0xffd905).type(OreType.METAL).harvestLevel(2).build());
    REDSTONE = register("redstone", OreComponent.builder().block(Blocks.REDSTONE_ORE).spawnWeight(6).color(0xff1a31).type(OreType.DUST).harvestLevel(2).build());
    DIAMOND = register("diamond", OreComponent.builder().block(Blocks.DIAMOND_ORE).spawnWeight(2).color(0x0fd3ff).type(OreType.GEM).harvestLevel(2).build());
    EMERALD = register("emerald", OreComponent.builder().block(Blocks.EMERALD_ORE).spawnWeight(1).color(0x61ff63).type(OreType.GEM).harvestLevel(2).build());
    NETHER_GOLD = register("nether_gold", OreComponent.builder().block(Blocks.NETHER_GOLD_ORE).spawnWeight(8).color(0xffe761).type(OreType.METAL).build());
    NETHER_QUARTZ = register("nether_quartz", OreComponent.builder().block(Blocks.NETHER_QUARTZ_ORE).spawnWeight(10).color(0xfafcff).type(OreType.GEM).build());
    ANCIENT_DEBRIS = register("ancient_debris", OreComponent.builder().block(Blocks.ANCIENT_DEBRIS).spawnWeight(1).color(0x6d4426).type(OreType.NONMETAL).harvestLevel(3).build());
    COPPER = registerFirstInOreTag("copper", OreComponent.builder().spawnWeight(9).color(0xd77233).type(OreType.METAL).harvestLevel(1));
    TIN = registerFirstInOreTag("tin", OreComponent.builder().spawnWeight(5).color(0xc3d2d5).type(OreType.METAL).harvestLevel(1));
    ALUMINUM = registerFirstInOreTag("aluminum", OreComponent.builder().spawnWeight(5).color(0x743911).type(OreType.METAL).harvestLevel(1));
    OSMIUM = registerFirstInOreTag("osmium", OreComponent.builder().spawnWeight(4).color(0x296c8e).type(OreType.METAL).harvestLevel(1));
    URANIUM = registerFirstInOreTag("uranium", OreComponent.builder().spawnWeight(4).color(0x428549).type(OreType.METAL).harvestLevel(2));
    NICKEL = registerFirstInOreTag("nickel", OreComponent.builder().spawnWeight(4).color(0x999d85).type(OreType.METAL).harvestLevel(2));
    ZINC = registerFirstInOreTag("zinc", OreComponent.builder().spawnWeight(5).color(0xf5f5f5).type(OreType.METAL).harvestLevel(2));
    SILVER = registerFirstInOreTag("silver", OreComponent.builder().spawnWeight(4).color(0xf1f4f0).type(OreType.METAL).harvestLevel(2));
    LEAD = registerFirstInOreTag("lead", OreComponent.builder().spawnWeight(4).color(0x666666).type(OreType.METAL).harvestLevel(2));
    BISMUTH = registerFirstInOreTag("bismuth", OreComponent.builder().spawnWeight(3).color(0xfffedb).type(OreType.METAL).harvestLevel(2));
    SAPPHIRE = registerFirstInOreTag("sapphire", OreComponent.builder().spawnWeight(2).color(0x3b5cf1).type(OreType.GEM).harvestLevel(2));
    RUBY = registerFirstInOreTag("ruby", OreComponent.builder().spawnWeight(2).color(0xff2b1f).type(OreType.GEM).harvestLevel(2));
    IRIDIUM = registerFirstInOreTag("iridium", OreComponent.builder().spawnWeight(1).color(0xffe1f9).type(OreType.METAL).harvestLevel(3));
    PLATINUM = registerFirstInOreTag("iridium", OreComponent.builder().spawnWeight(1).color(0xadadad).type(OreType.METAL).harvestLevel(3));
    SULFUR = registerFirstInOreTag("sulfur", OreComponent.builder().spawnWeight(7).color(0xddb82c).type(OreType.GEM).harvestLevel(2));
    FLUORITE = registerFirstInOreTag("fluorite", OreComponent.builder().spawnWeight(3).color(0xe2efa3).type(OreType.GEM).harvestLevel(2));
    DIMENSIONAL_SHARD = registerFromBlockName("rftoolsbase:dimensionalshard_overworld", "dimensional_shard", OreComponent.builder().spawnWeight(3).color(0xdfebf7).type(OreType.GEM).harvestLevel(1));

    initialized = true;
  }

  private static OreComponent register(String name, OreComponent comp) {
    comp.setRegistryName(CompoundOres.MOD_ID, name);
    if (exceptionsList == null) {
      exceptionsList = CompoundOresConfig.COMMON.componentsExceptions();
      whitelistExceptions = CompoundOresConfig.COMMON.componentsWhitelist();
      if (whitelistExceptions && exceptionsList.isEmpty()) {
        LOGGER.warn("An empty whitelist was configured. If you don't want any components registered, set registerDefaultComponents to false. Reverting to empty blacklist instead");
        whitelistExceptions = false;
      }
      if (whitelistExceptions) {
        LOGGER.debug("A whitelist has been established for registering components: [{}]", exceptionsList.stream().map(c -> c.getRegistryName().toString()).collect(Collectors.joining(", ")));
      } else {
        if (exceptionsList.isEmpty()) {
          LOGGER.debug("An empty blacklist has been established for registering components");
        } else {
          LOGGER.debug("A blacklist has been established for registering components: [{}]", exceptionsList.stream().map(c -> c.getRegistryName().toString()).collect(Collectors.joining(", ")));
        }
      }
    }
    if ((whitelistExceptions && !exceptionsList.contains(comp)) || (!whitelistExceptions && exceptionsList.contains(comp))) {
      LOGGER.debug("Ore component {} was prevented from being registered (not on whitelist or blacklisted)", comp.getRegistryName());
      return OreComponent.EMPTY;
    }
    LOGGER.debug("Registering ore component {} (block={}, weight={})", comp.getRegistryName(), comp.getBlock().getRegistryName(), comp.getSpawnWeight());
    OreComponentRegistry.instance.register(comp);
    return comp;
  }

  private static OreComponent registerFirstInTag(String name, String tagName, OreComponent.Builder compBuilder) {
    ITag<Block> tag = BlockTags.getAllTags().getTag(new ResourceLocation(tagName));
    if (tag == null || tag.getValues().isEmpty()) {
      return OreComponent.EMPTY;
    }
    return register(name, compBuilder.block(tag.getValues().get(0)).build());
  }

  private static OreComponent registerFirstInOreTag(String tagOreName, OreComponent.Builder compBuilder) {
    return registerFirstInTag(tagOreName, "forge:ores/" + tagOreName, compBuilder);
  }

  private static OreComponent registerFromBlockName(String blockName, String compName, OreComponent.Builder compBuilder) {
    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
    if (block == null || block.is(Blocks.AIR)) {
      return OreComponent.EMPTY;
    }
    return register(compName, compBuilder.block(block).build());
  }

}
