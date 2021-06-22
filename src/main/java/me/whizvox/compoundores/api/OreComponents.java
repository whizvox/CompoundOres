package me.whizvox.compoundores.api;

import me.whizvox.compoundores.CompoundOres;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class OreComponents {

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
      YELLORIUM,
      // RFToolsBase
      DIMENSIONAL_SHARD_OVERWORLD,
      DIMENSIONAL_SHARD_NETHER,
      DIMENSIONAL_SHARD_END,
      // Powah
      URANINITE_POOR,
      URANINITE,
      URANINITE_DENSE;

  private static boolean initialized = false;

  static void registerDefaults() {
    if (initialized) {
      return;
    }

    COAL = register("coal", OreComponent.builder().block(Blocks.COAL_ORE).spawnWeight(12).build());
    IRON = register("iron", OreComponent.builder().block(Blocks.IRON_ORE).spawnWeight(8).harvestLevel(1).build());
    LAPIS = register("lapis", OreComponent.builder().block(Blocks.LAPIS_ORE).spawnWeight(3).harvestLevel(1).build());
    GOLD = register("gold", OreComponent.builder().block(Blocks.GOLD_ORE).spawnWeight(4).harvestLevel(2).build());
    REDSTONE = register("redstone", OreComponent.builder().block(Blocks.REDSTONE_ORE).spawnWeight(6).harvestLevel(2).build());
    DIAMOND = register("diamond", OreComponent.builder().block(Blocks.DIAMOND_ORE).spawnWeight(2).harvestLevel(2).build());
    EMERALD = register("emerald", OreComponent.builder().block(Blocks.EMERALD_ORE).spawnWeight(1).harvestLevel(2).build());
    NETHER_GOLD = register("nether_gold", OreComponent.builder().block(Blocks.NETHER_GOLD_ORE).spawnWeight(8).build());
    NETHER_QUARTZ = register("nether_quartz", OreComponent.builder().block(Blocks.NETHER_QUARTZ_ORE).spawnWeight(10).build());
    ANCIENT_DEBRIS = register("ancient_debris", OreComponent.builder().block(Blocks.ANCIENT_DEBRIS).spawnWeight(1).destroySpeed(30.0F).resistance(1200.0F).harvestLevel(3).build());
    COPPER = registerFirstInOreTag("copper", OreComponent.builder().spawnWeight(9).harvestLevel(1));
    TIN = registerFirstInOreTag("tin", OreComponent.builder().spawnWeight(5).harvestLevel(1));
    ALUMINUM = registerFirstInOreTag("aluminum", OreComponent.builder().spawnWeight(5).harvestLevel(1));
    OSMIUM = registerFirstInOreTag("osmium", OreComponent.builder().spawnWeight(4).harvestLevel(1));
    URANIUM = registerFirstInOreTag("uranium", OreComponent.builder().spawnWeight(4).harvestLevel(2));
    NICKEL = registerFirstInOreTag("nickel", OreComponent.builder().spawnWeight(4).harvestLevel(2));
    ZINC = registerFirstInOreTag("zinc", OreComponent.builder().spawnWeight(5).harvestLevel(2));
    SILVER = registerFirstInOreTag("silver", OreComponent.builder().spawnWeight(4).harvestLevel(2));
    LEAD = registerFirstInOreTag("lead", OreComponent.builder().spawnWeight(4).harvestLevel(2));
    BISMUTH = registerFirstInOreTag("bismuth", OreComponent.builder().spawnWeight(3).harvestLevel(2));
    SAPPHIRE = registerFirstInOreTag("sapphire", OreComponent.builder().spawnWeight(2).harvestLevel(2));
    RUBY = registerFirstInOreTag("ruby", OreComponent.builder().spawnWeight(2).harvestLevel(2));
    IRIDIUM = registerFirstInOreTag("iridium", OreComponent.builder().spawnWeight(1).harvestLevel(3));
    PLATINUM = registerFirstInOreTag("platinum", OreComponent.builder().spawnWeight(1).harvestLevel(3));
    SULFUR = registerFirstInOreTag("sulfur", OreComponent.builder().spawnWeight(7).harvestLevel(2));
    FLUORITE = registerFirstInOreTag("fluorite", OreComponent.builder().spawnWeight(3).harvestLevel(2));
    YELLORIUM = registerFirstInOreTag("yellorium", OreComponent.builder().spawnWeight(2).harvestLevel(2));
    DIMENSIONAL_SHARD_OVERWORLD = registerFromBlockName("dimensionalshard_overworld", "rftoolsbase:dimensionalshard_overworld", OreComponent.builder().spawnWeight(3).harvestLevel(1));
    DIMENSIONAL_SHARD_NETHER = registerFromBlockName("dimensionalshard_nether", "rftoolsbase:dimensionalshard_nether", OreComponent.builder().spawnWeight(2).harvestLevel(1));
    DIMENSIONAL_SHARD_END = registerFromBlockName("dimensionalshard_end", "rftoolsbase:dimensionalshard_end", OreComponent.builder().spawnWeight(2).harvestLevel(1));
    URANINITE_POOR = registerFromBlockName("uraninite_poor", "powah:uraninite_ore_poor", OreComponent.builder().spawnWeight(5).resistance(8.0F));
    URANINITE = registerFromBlockName("uraninite", "powah:uraninite_ore", OreComponent.builder().spawnWeight(3).destroySpeed(3.2F).resistance(8.0F));
    URANINITE_DENSE = registerFromBlockName("uraninite_dense", "powah:uraninite_ore_dense", OreComponent.builder().spawnWeight(1).destroySpeed(4.0F).resistance(8.0F));

    initialized = true;
  }

  private static boolean shouldWrap(ResourceLocation key) {
    return OreComponentRegistry.getInstance().containsKey(key);
  }

  // Public helpers for any mod to use

  public static OreComponent register(ResourceLocation name, OreComponent comp) {
    if (shouldWrap(name)) {
      return wrap(name);
    }
    comp.setRegistryName(name);
    return OreComponentRegistry.getInstance().registerChecked(comp);
  }

  public static OreComponent registerFirstInTag(ResourceLocation name, String tagName, OreComponent.Builder builder) {
    if (shouldWrap(name)) {
      return wrap(name);
    }
    ITag<Block> tag = BlockTags.getAllTags().getTag(new ResourceLocation(tagName));
    if (tag == null || tag.getValues().isEmpty()) {
      return OreComponent.EMPTY;
    }
    return register(name, builder.block(tag.getValues().get(0)).build());
  }

  public static OreComponent registerFirstInOreTag(String namespace, String tagOreName, OreComponent.Builder builder) {
    return registerFirstInTag(new ResourceLocation(namespace, tagOreName), "forge:ores/" + tagOreName, builder);
  }

  public static OreComponent registerFromBlockName(ResourceLocation name, String blockName, OreComponent.Builder builder) {
    if (shouldWrap(name)) {
      return wrap(name);
    }
    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
    if (block == null || block.is(Blocks.AIR)) {
      return OreComponent.EMPTY;
    }
    return register(name, builder.block(block).build());
  }

  public static OreComponent wrap(ResourceLocation name) {
    return OreComponentRegistry.getInstance().getValue(name);
  }

  // Private helpers for this class's default ore components

  private static OreComponent register(String name, OreComponent comp) {
    final ResourceLocation key = new ResourceLocation(CompoundOres.MOD_ID, name);
    if (shouldWrap(key)) {
      return wrap(key);
    }
    return register(key, comp);
  }

  private static OreComponent registerFirstInOreTag(String tagOreName, OreComponent.Builder builder) {
    final ResourceLocation key = new ResourceLocation(CompoundOres.MOD_ID, tagOreName);
    if (shouldWrap(key)) {
      return wrap(key);
    }
    return registerFirstInOreTag(CompoundOres.MOD_ID, tagOreName, builder);
  }

  private static OreComponent registerFromBlockName(String name, String blockName, OreComponent.Builder builder) {
    final ResourceLocation key = new ResourceLocation(CompoundOres.MOD_ID, name);
    if (shouldWrap(key)) {
      return wrap(key);
    }
    return registerFromBlockName(key, blockName, builder);
  }

}
