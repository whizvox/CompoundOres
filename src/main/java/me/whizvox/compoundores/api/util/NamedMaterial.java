package me.whizvox.compoundores.api.util;

import net.minecraft.block.material.Material;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static me.whizvox.compoundores.CompoundOres.LOGGER;

@SuppressWarnings("unused")
public class NamedMaterial {

  public static final NamedMaterial
    AIR = new NamedMaterial(Material.AIR, "AIR"),
    STRUCTURAL_AIR = new NamedMaterial(Material.STRUCTURAL_AIR, "STRUCTURAL_AIR"),
    PORTAL = new NamedMaterial(Material.PORTAL, "PORTAL"),
    CLOTH_DECORATION = new NamedMaterial(Material.CLOTH_DECORATION, "CLOTH_DECORATION"),
    PLANT = new NamedMaterial(Material.PLANT, "PLANT"),
    WATER_PLANT = new NamedMaterial(Material.WATER_PLANT, "WATER_PLANT"),
    REPLACEABLE_PLANT = new NamedMaterial(Material.REPLACEABLE_PLANT, "REPLACEABLE_PLANT"),
    REPLACEABLE_FIREPROOF_PLANT = new NamedMaterial(Material.REPLACEABLE_FIREPROOF_PLANT, "REPLACEABLE_FIREPROOF_PLANT"),
    REPLACEABLE_WATER_PLANT = new NamedMaterial(Material.REPLACEABLE_WATER_PLANT, "REPLACABLE_WATER_PLANT"),
    WATER = new NamedMaterial(Material.WATER, "WATER"),
    BUBBLE_COLUMN = new NamedMaterial(Material.BUBBLE_COLUMN, "BUBBLE_COLUMN"),
    LAVA = new NamedMaterial(Material.LAVA, "LAVA"),
    TOP_SNOW = new NamedMaterial(Material.TOP_SNOW, "TOP_SNOW"),
    FIRE = new NamedMaterial(Material.FIRE, "FIRE"),
    DECORATION = new NamedMaterial(Material.DECORATION, "DECORATION"),
    WEB = new NamedMaterial(Material.WEB, "WEB"),
    BUILDABLE_GLASS = new NamedMaterial(Material.BUILDABLE_GLASS, "BUILDABLE_GLASS"),
    CLAY = new NamedMaterial(Material.CLAY, "CLAY"),
    DIRT = new NamedMaterial(Material.DIRT, "DIRT"),
    GRASS = new NamedMaterial(Material.GRASS, "GRASS"),
    ICE_SOLID = new NamedMaterial(Material.ICE_SOLID, "ICE_SOLID"),
    SAND = new NamedMaterial(Material.SAND, "SAND"),
    SPONGE = new NamedMaterial(Material.SPONGE, "SPONGE"),
    SHULKER_SHELL = new NamedMaterial(Material.SHULKER_SHELL, "SHULKER_SHELL"),
    WOOD = new NamedMaterial(Material.WOOD, "WOOD"),
    NETHER_WOOD = new NamedMaterial(Material.NETHER_WOOD, "NETHER_WOOD"),
    BAMBOO_SAPLING = new NamedMaterial(Material.BAMBOO_SAPLING, "BAMBOO_SAPLING"),
    BAMBOO = new NamedMaterial(Material.BAMBOO, "BAMBOO"),
    WOOL = new NamedMaterial(Material.WOOL, "WOOL"),
    EXPLOSIVE = new NamedMaterial(Material.EXPLOSIVE, "EXPLOSIVE"),
    LEAVES = new NamedMaterial(Material.LEAVES, "LEAVES"),
    GLASS = new NamedMaterial(Material.GLASS, "GLASS"),
    ICE = new NamedMaterial(Material.ICE, "ICE"),
    CACTUS = new NamedMaterial(Material.CACTUS, "CACTUS"),
    STONE = new NamedMaterial(Material.STONE, "STONE"),
    METAL = new NamedMaterial(Material.METAL, "METAL"),
    SNOW = new NamedMaterial(Material.SNOW, "SNOW"),
    HEAVY_METAL = new NamedMaterial(Material.HEAVY_METAL, "HEAVY_METAL"),
    BARRIER = new NamedMaterial(Material.BARRIER, "BARRIER"),
    PISTON = new NamedMaterial(Material.PISTON, "PISTON"),
    CORAL = new NamedMaterial(Material.CORAL, "CORAL"),
    VEGETABLE = new NamedMaterial(Material.VEGETABLE, "VEGETABLE"),
    EGG = new NamedMaterial(Material.EGG, "EGG"),
    CAKE = new NamedMaterial(Material.CAKE, "CAKE");

  public final Material material;
  public final String name;
  public final boolean vanilla;

  private NamedMaterial(Material material, String name, boolean vanilla) {
    this.material = material;
    this.name = name;
    this.vanilla = vanilla;
  }

  private NamedMaterial(Material material, String name) {
    this(material, name, true);
  }

  public static final NamedMaterial DEFAULT;
  public static final Map<String, NamedMaterial> VANILLA_MATERIALS;

  private static final Map<String, NamedMaterial> externalCache = new HashMap<>();

  static {
    Map<String, NamedMaterial> map = new HashMap<>();
    Arrays.stream(NamedMaterial.class.getDeclaredFields())
      .filter(f -> f.getType().isAssignableFrom(NamedMaterial.class) && Modifier.isStatic(f.getModifiers()))
      .forEach(f -> {
        try {
          NamedMaterial value = (NamedMaterial) f.get(null);
          map.put(value.name, value);
        } catch (Exception e) {
          LOGGER.warn("Could not retrieve NamedMaterial from " + f.getName(), e);
        }
      });
    VANILLA_MATERIALS = Collections.unmodifiableMap(map);
    DEFAULT = vanilla("STONE");
  }

  public static NamedMaterial from(String name) {
    if (name.indexOf('.') >= 0) {
      return external(name);
    }
    return vanilla(name);
  }

  public static NamedMaterial vanilla(String name) {
    NamedMaterial nMat = VANILLA_MATERIALS.get(name);
    if (nMat == null) {
      LOGGER.warn("Could not locate vanilla material name: " + name);
      return DEFAULT;
    }
    return nMat;
  }

  public static NamedMaterial external(String name) {
    if (externalCache.containsKey(name)) {
      return externalCache.get(name);
    }
    int lastDotIndex = name.lastIndexOf('.');
    if (lastDotIndex >= 0) {
      String className = name.substring(0, lastDotIndex);
      String fieldName = name.substring(lastDotIndex + 1);
      if (Material.class.toString().equals(className)) {
        return vanilla(fieldName);
      }
      try {
        NamedMaterial nMat = new NamedMaterial((Material) Class.forName(className).getDeclaredField(fieldName).get(null), name, false);
        externalCache.put(nMat.name, nMat);
        return nMat;
      } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException | ClassCastException e) {
        LOGGER.warn("Could not locate external material from " + name, e);
      }
    } else {
      LOGGER.warn("External named material name must resolve to a static Material object: {}", name);
    }
    return DEFAULT;
  }

}
