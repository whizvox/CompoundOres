package me.whizvox.compoundores.api.component;

import me.whizvox.compoundores.CompoundOres;
import me.whizvox.compoundores.api.util.NamedMaterial;
import me.whizvox.compoundores.api.util.NamedMaterialColor;
import me.whizvox.compoundores.api.util.NamedSoundType;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;

import java.util.Arrays;

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
      GLOWSTONE,
      // Commonly-used ore tags
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
      YELLORITE,
      APATITE,
      CINNABAR,
      NITER,
      // Applied Energistics 2
      CERTUS_QUARTZ,
      CHARGED_CERTUS_QUARTZ,
      // Extreme Reactors 2
      ANGLESITE,
      BENITOITE,
      // Ice and Fire
      AMYTHEST,
      // Powah
      URANINITE_POOR,
      URANINITE,
      URANINITE_DENSE,
      // Quark
      BIOTITE,
      // RFToolsBase
      DIMENSIONALSHARD_OVERWORLD,
      DIMENSIONALSHARD_NETHER,
      DIMENSIONALSHARD_END,
      // Thermal
      BITUMINOUS_SAND,
      BITUMINOUS_RED_SAND,
      // Tinkers' Construct
      TINKERS_COBALT;

  private static boolean initialized = false;

  static void registerDefaults() {
    if (initialized) {
      return;
    }

    // Vanilla
    COAL = register("coal", OreComponent.builder().target(Blocks.COAL_ORE).weight(12).build());
    IRON = register("iron", OreComponent.builder().target(Blocks.IRON_ORE).weight(8).harvestLevel(1).build());
    LAPIS = register("lapis", OreComponent.builder().target(Blocks.LAPIS_ORE).weight(3).harvestLevel(1).build());
    GOLD = register("gold", OreComponent.builder().target(Blocks.GOLD_ORE).weight(4).harvestLevel(2).build());
    REDSTONE = register("redstone", OreComponent.builder().target(Blocks.REDSTONE_ORE).weight(6).harvestLevel(2).build());
    DIAMOND = register("diamond", OreComponent.builder().target(Blocks.DIAMOND_ORE).weight(2).harvestLevel(2).build());
    EMERALD = register("emerald", OreComponent.builder().target(Blocks.EMERALD_ORE).weight(1).harvestLevel(2).build());
    NETHER_GOLD = register("nether_gold", OreComponent.builder().target(Blocks.NETHER_GOLD_ORE).materialColor(NamedMaterialColor.NETHER).sound(NamedSoundType.NETHER_GOLD_ORE).weight(8).build());
    NETHER_QUARTZ = register("nether_quartz", OreComponent.builder().target(Blocks.NETHER_QUARTZ_ORE).materialColor(NamedMaterialColor.NETHER).sound(NamedSoundType.NETHER_ORE).weight(10).build());
    ANCIENT_DEBRIS = register("ancient_debris", OreComponent.builder().target(Blocks.ANCIENT_DEBRIS).material(NamedMaterial.METAL).materialColor(NamedMaterialColor.COLOR_BLACK).sound(NamedSoundType.ANCIENT_DEBRIS).weight(1).hardness(30.0F).resistance(1200.0F).harvestLevel(3).build());
    GLOWSTONE = register("glowstone", OreComponent.builder().target(Blocks.GLOWSTONE).material(NamedMaterial.GLASS).materialColor(NamedMaterialColor.SAND).sound(NamedSoundType.GLASS).hardness(0.3F).resistance(0.3F).tool(null).build());

    // Common tags
    COPPER = registerFirstInOreTag("copper", OreComponent.builder().weight(9).harvestLevel(1));
    TIN = registerFirstInOreTag("tin", OreComponent.builder().weight(5).harvestLevel(1));
    ALUMINUM = registerFirstInOreTag("aluminum", OreComponent.builder().weight(5).harvestLevel(1));
    OSMIUM = registerFirstInOreTag("osmium", OreComponent.builder().weight(4).harvestLevel(1));
    URANIUM = registerFirstInOreTag("uranium", OreComponent.builder().weight(4).harvestLevel(2));
    NICKEL = registerFirstInOreTag("nickel", OreComponent.builder().weight(4).harvestLevel(2));
    ZINC = registerFirstInOreTag("zinc", OreComponent.builder().weight(5).harvestLevel(2));
    SILVER = registerFirstInOreTag("silver", OreComponent.builder().weight(4).harvestLevel(2));
    LEAD = registerFirstInOreTag("lead", OreComponent.builder().weight(4).harvestLevel(2));
    BISMUTH = registerFirstInOreTag("bismuth", OreComponent.builder().weight(3).harvestLevel(2));
    SAPPHIRE = registerFirstInOreTag("sapphire", OreComponent.builder().weight(2).harvestLevel(3));
    RUBY = registerFirstInOreTag("ruby", OreComponent.builder().weight(2).harvestLevel(3));
    IRIDIUM = registerFirstInOreTag("iridium", OreComponent.builder().weight(1).harvestLevel(3));
    PLATINUM = registerFirstInOreTag("platinum", OreComponent.builder().weight(1).harvestLevel(3));
    SULFUR = registerFirstInOreTag("sulfur", OreComponent.builder().weight(7).harvestLevel(2));
    FLUORITE = registerFirstInOreTag("fluorite", OreComponent.builder().weight(3).harvestLevel(2));
    YELLORITE = registerFirstInOreTag("yellorite", OreComponent.builder().weight(2).harvestLevel(2));
    NITER = registerFirstInOreTag("niter", OreComponent.builder().weight(3).harvestLevel(1));
    APATITE = registerFirstInOreTag("apatite", OreComponent.builder().weight(1).harvestLevel(1));
    CINNABAR = registerFirstInOreTag("cinnabar", OreComponent.builder().weight(1).harvestLevel(1));

    // Mod-specific
    TINKERS_COBALT = registerFromBlockName("tinkers_cobalt", "tconstruct:cobalt", OreComponent.builder().sound(NamedSoundType.NETHER_ORE).weight(2).hardness(10.0F).resistance(10.0F).harvestLevel(3));
    CERTUS_QUARTZ = registerFromBlockName("certus_quartz", "appliedenergistics:quartz_ore", OreComponent.builder().weight(5).hardness(3.0F).resistance(5.0F).harvestLevel(1));
    CHARGED_CERTUS_QUARTZ = registerFromBlockName("charged_certus_quartz", "appliedenergistics2:charged_quartz_ore", OreComponent.builder().weight(2).hardness(3.0F).resistance(5.0F).harvestLevel(1));
    DIMENSIONALSHARD_OVERWORLD = registerFromBlockName("dimensionalshard_overworld", "rftoolsbase:dimensionalshard_overworld", OreComponent.builder().resistance(5.0F).weight(3).harvestLevel(1));
    DIMENSIONALSHARD_NETHER = registerFromBlockName("dimensionalshard_nether", "rftoolsbase:dimensionalshard_nether", OreComponent.builder().resistance(5.0F).weight(2).harvestLevel(1));
    DIMENSIONALSHARD_END = registerFromBlockName("dimensionalshard_end", "rftoolsbase:dimensionalshard_end", OreComponent.builder().resistance(5.0F).weight(2).harvestLevel(1));
    URANINITE_POOR = registerFromBlockName("uraninite_poor", "powah:uraninite_ore_poor", OreComponent.builder().weight(5).resistance(8.0F));
    URANINITE = registerFromBlockName("uraninite", "powah:uraninite_ore", OreComponent.builder().weight(3).hardness(3.2F).resistance(8.0F));
    URANINITE_DENSE = registerFromBlockName("uraninite_dense", "powah:uraninite_ore_dense", OreComponent.builder().weight(1).hardness(4.0F).resistance(8.0F));
    ANGLESITE = registerFromBlockName("anglesite", "bigreactors:anglesite_ore", OreComponent.builder().weight(1).materialColor(NamedMaterialColor.COLOR_ORANGE));
    BENITOITE = registerFromBlockName("benitoite", "bigreactors:benitoite_ore", OreComponent.builder().weight(1).materialColor(NamedMaterialColor.COLOR_LIGHT_BLUE));
    AMYTHEST = registerFromBlockName("amythest", "iceandfire:amythest_ore", OreComponent.builder().weight(1).harvestLevel(2).hardness(4.0F).resistance(15.0F));
    BIOTITE = registerFromBlockName("biotite", "quark:biotite_ore", OreComponent.builder().weight(2).materialColor(NamedMaterialColor.SAND).hardness(3.2F).resistance(15.0F));
    BITUMINOUS_SAND = registerFromBlockName("bituminous_sand", "thermal:oil_sand", OreComponent.builder().weight(4).material(NamedMaterial.SAND).materialColor(NamedMaterialColor.SAND).tool(ToolType.SHOVEL));
    BITUMINOUS_RED_SAND = registerFromBlockName("bituminous_red_sand", "thermal:oil_red_sand", OreComponent.builder().weight(3).material(NamedMaterial.SAND).materialColor(NamedMaterialColor.COLOR_ORANGE).tool(ToolType.SHOVEL));

    addGroup("dimensionalshard", DIMENSIONALSHARD_OVERWORLD, DIMENSIONALSHARD_NETHER, DIMENSIONALSHARD_END);
    addGroup("bituminous_sand", BITUMINOUS_SAND, BITUMINOUS_RED_SAND);

    initialized = true;
  }

  private static void addGroup(String name, OreComponent... components) {
    OreComponentRegistry.getInstance().addGroup(name, Arrays.asList(components));
  }

  // the values in this file are technically the "fallback" components, so the components in this file will only
  // register a new component if one under the same name does not already exist
  private static boolean exists(ResourceLocation key) {
    return OreComponentRegistry.getInstance().containsKey(key);
  }

  // Public helpers for any mod to use

  public static OreComponent register(ResourceLocation name, OreComponent comp) {
    if (exists(name)) {
      return wrap(name);
    }
    comp.setRegistryName(name);
    return OreComponentRegistry.getInstance().registerChecked(comp);
  }

  public static OreComponent registerFirstInTag(ResourceLocation name, String tagName, OreComponent.Builder builder) {
    if (exists(name)) {
      return wrap(name);
    }
    return register(name, builder.target(BlockTags.createOptional(new ResourceLocation(tagName))).build());
  }

  public static OreComponent registerFirstInOreTag(String namespace, String tagOreName, OreComponent.Builder builder) {
    return registerFirstInTag(new ResourceLocation(namespace, tagOreName), "forge:ores/" + tagOreName, builder);
  }

  public static OreComponent registerFromBlockName(ResourceLocation name, String blockName, OreComponent.Builder builder) {
    if (exists(name)) {
      return wrap(name);
    }
    return register(name, builder.target(blockName).build());
  }

  public static OreComponent wrap(ResourceLocation name) {
    return OreComponentRegistry.getInstance().getValue(name);
  }

  // Private helpers for this class's default ore components

  private static OreComponent register(String name, OreComponent comp) {
    final ResourceLocation key = new ResourceLocation(CompoundOres.MOD_ID, name);
    if (exists(key)) {
      return wrap(key);
    }
    return register(key, comp);
  }

  private static OreComponent registerFirstInOreTag(String tagOreName, OreComponent.Builder builder) {
    final ResourceLocation key = new ResourceLocation(CompoundOres.MOD_ID, tagOreName);
    if (exists(key)) {
      return wrap(key);
    }
    return registerFirstInOreTag(CompoundOres.MOD_ID, tagOreName, builder);
  }

  private static OreComponent registerFromBlockName(String name, String blockName, OreComponent.Builder builder) {
    final ResourceLocation key = new ResourceLocation(CompoundOres.MOD_ID, name);
    if (exists(key)) {
      return wrap(key);
    }
    return registerFromBlockName(key, blockName, builder);
  }

}
