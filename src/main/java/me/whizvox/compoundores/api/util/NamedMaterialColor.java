package me.whizvox.compoundores.api.util;

import net.minecraft.block.material.MaterialColor;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static me.whizvox.compoundores.CompoundOres.LOGGER;

@SuppressWarnings("unused")
public class NamedMaterialColor {

  public static final NamedMaterialColor
    NONE = new NamedMaterialColor(MaterialColor.NONE, "NONE"),
    GRASS = new NamedMaterialColor(MaterialColor.GRASS, "GRASS"),
    SAND = new NamedMaterialColor(MaterialColor.SAND, "SAND"),
    WOOL = new NamedMaterialColor(MaterialColor.WOOL, "WOOL"),
    FIRE = new NamedMaterialColor(MaterialColor.FIRE, "FIRE"),
    ICE = new NamedMaterialColor(MaterialColor.ICE, "ICE"),
    METAL = new NamedMaterialColor(MaterialColor.METAL, "METAL"),
    PLANT = new NamedMaterialColor(MaterialColor.PLANT, "PLANT"),
    SNOW = new NamedMaterialColor(MaterialColor.SNOW, "SNOW"),
    CLAY = new NamedMaterialColor(MaterialColor.CLAY, "CLAY"),
    DIRT = new NamedMaterialColor(MaterialColor.DIRT, "DIRT"),
    STONE = new NamedMaterialColor(MaterialColor.STONE, "STONE"),
    WATER = new NamedMaterialColor(MaterialColor.WATER, "WATER"),
    WOOD = new NamedMaterialColor(MaterialColor.WOOD, "WOOD"),
    QUARTZ = new NamedMaterialColor(MaterialColor.QUARTZ, "QUARTZ"),
    COLOR_ORANGE = new NamedMaterialColor(MaterialColor.COLOR_ORANGE, "COLOR_ORANGE"),
    COLOR_MAGENTA = new NamedMaterialColor(MaterialColor.COLOR_ORANGE, "COLOR_ORANGE"),
    COLOR_LIGHT_BLUE = new NamedMaterialColor(MaterialColor.COLOR_LIGHT_BLUE, "COLOR_LIGHT_BLUE"),
    COLOR_YELLOW = new NamedMaterialColor(MaterialColor.COLOR_YELLOW, "COLOR_YELLOW"),
    COLOR_LIGHT_GREEN = new NamedMaterialColor(MaterialColor.COLOR_LIGHT_GREEN, "COLOR_LIGHT_GREEN"),
    COLOR_PINK = new NamedMaterialColor(MaterialColor.COLOR_PINK, "COLOR_PINK"),
    COLOR_GRAY = new NamedMaterialColor(MaterialColor.COLOR_GRAY, "COLOR_GRAY"),
    COLOR_LIGHT_GRAY = new NamedMaterialColor(MaterialColor.COLOR_LIGHT_GRAY, "COLOR_LIGHT_GRAY"),
    COLOR_CYAN = new NamedMaterialColor(MaterialColor.COLOR_CYAN, "COLOR_CYAN"),
    COLOR_PURPLE = new NamedMaterialColor(MaterialColor.COLOR_PURPLE, "COLOR_PURPLE"),
    COLOR_BLUE = new NamedMaterialColor(MaterialColor.COLOR_BLUE, "COLOR_BLUE"),
    COLOR_BROWN = new NamedMaterialColor(MaterialColor.COLOR_BROWN, "COLOR_BROWN"),
    COLOR_GREEN = new NamedMaterialColor(MaterialColor.COLOR_GREEN, "COLOR_GREEN"),
    COLOR_RED = new NamedMaterialColor(MaterialColor.COLOR_RED, "COLOR_RED"),
    COLOR_BLACK = new NamedMaterialColor(MaterialColor.COLOR_BLACK, "COLOR_BLACK"),
    GOLD = new NamedMaterialColor(MaterialColor.GOLD, "GOLD"),
    DIAMOND = new NamedMaterialColor(MaterialColor.DIAMOND, "DIAMOND"),
    LAPIS = new NamedMaterialColor(MaterialColor.LAPIS, "LAPIS"),
    EMERALD = new NamedMaterialColor(MaterialColor.EMERALD, "EMERALD"),
    PODZOL = new NamedMaterialColor(MaterialColor.PODZOL, "PODZOL"),
    NETHER = new NamedMaterialColor(MaterialColor.NETHER, "NETHER"),
    TERRACOTTA_WHITE = new NamedMaterialColor(MaterialColor.TERRACOTTA_WHITE, "TERRACOTTA_WHITE"),
    TERRACOTTA_ORANGE = new NamedMaterialColor(MaterialColor.TERRACOTTA_ORANGE, "TERRACOTTA_ORANGE"),
    TERRACOTTA_MAGENTA = new NamedMaterialColor(MaterialColor.TERRACOTTA_MAGENTA, "TERRACOTTA_MAGENTA"),
    TERRACOTTA_LIGHT_BLUE = new NamedMaterialColor(MaterialColor.TERRACOTTA_LIGHT_BLUE, "TERRACOTTA_LIGHT_BLUE"),
    TERRACOTTA_YELLOW = new NamedMaterialColor(MaterialColor.TERRACOTTA_YELLOW, "TERRACOTTA_YELLOW"),
    TERRACOTTA_LIGHT_GREEN = new NamedMaterialColor(MaterialColor.TERRACOTTA_LIGHT_GREEN, "TERRACOTTA_LIGHT_GREEN"),
    TERRACOTTA_PINK = new NamedMaterialColor(MaterialColor.TERRACOTTA_PINK, "TERRACOTTA_PINK"),
    TERRACOTTA_GRAY = new NamedMaterialColor(MaterialColor.TERRACOTTA_GRAY, "TERRACOTTA_GRAY"),
    TERRACOTTA_LIGHT_GRAY = new NamedMaterialColor(MaterialColor.TERRACOTTA_LIGHT_GRAY, "TERRACOTTA_LIGHT_GRAY"),
    TERRACOTTA_CYAN = new NamedMaterialColor(MaterialColor.TERRACOTTA_CYAN, "TERRACOTTA_CYAN"),
    TERRACOTTA_PURPLE = new NamedMaterialColor(MaterialColor.TERRACOTTA_PURPLE, "TERRACOTTA_PURPLE"),
    TERRACOTTA_BLUE = new NamedMaterialColor(MaterialColor.TERRACOTTA_BLUE, "TERRACOTTA_BLUE"),
    TERRACOTTA_BROWN = new NamedMaterialColor(MaterialColor.TERRACOTTA_BROWN, "TERRACOTTA_BROWN"),
    TERRACOTTA_GREEN = new NamedMaterialColor(MaterialColor.TERRACOTTA_GREEN, "TERRACOTTA_GREEN"),
    TERRACOTTA_RED = new NamedMaterialColor(MaterialColor.TERRACOTTA_RED, "TERRACOTTA_RED"),
    TERRACOTTA_BLACK = new NamedMaterialColor(MaterialColor.TERRACOTTA_BLACK, "TERRACOTTA_BLACK"),
    CRIMSON_NYLIUM = new NamedMaterialColor(MaterialColor.CRIMSON_NYLIUM, "CRIMSON_NYLIUM"),
    CRIMSON_STEM = new NamedMaterialColor(MaterialColor.CRIMSON_STEM, "CRIMSON_STEM"),
    CRIMSON_HYPHAE = new NamedMaterialColor(MaterialColor.CRIMSON_HYPHAE, "CRIMSON_HYPHAE"),
    WARPED_NYLIUM = new NamedMaterialColor(MaterialColor.WARPED_NYLIUM, "WARPED_NYLIUM"),
    WARPED_STEM = new NamedMaterialColor(MaterialColor.WARPED_STEM, "WARPED_STEM"),
    WARPED_HYPHAE = new NamedMaterialColor(MaterialColor.WARPED_HYPHAE, "WARPED_HYPHAE"),
    WARPED_WART_BLOCK = new NamedMaterialColor(MaterialColor.WARPED_WART_BLOCK, "WARPED_WART_BLOCK");

  public final MaterialColor color;
  public final String name;

  private NamedMaterialColor(MaterialColor color, String name) {
    this.color = color;
    this.name = name;
  }

  public static final Map<String, NamedMaterialColor> NAME_MAP;
  public static final Map<Integer, NamedMaterialColor> ID_MAP;
  public static final NamedMaterialColor DEFAULT = STONE;

  public static NamedMaterialColor from(String name) {
    return NAME_MAP.getOrDefault(name, DEFAULT);
  }

  public static NamedMaterialColor from(int id) {
    return ID_MAP.getOrDefault(id, DEFAULT);
  }

  public static NamedMaterialColor from(MaterialColor color) {
    return from(color.id);
  }

  static {
    Map<String, NamedMaterialColor> nameMap = new HashMap<>();
    Map<Integer, NamedMaterialColor> idMap = new HashMap<>();
    Arrays.stream(NamedMaterialColor.class.getDeclaredFields())
      .filter(f -> f.getType().isAssignableFrom(NamedMaterialColor.class) && Modifier.isStatic(f.getModifiers()))
      .forEach(f -> {
        try {
          NamedMaterialColor nMatColor = (NamedMaterialColor) f.get(null);
          nameMap.put(nMatColor.name, nMatColor);
          idMap.put(nMatColor.color.id, nMatColor);
        } catch (Exception e) {
          LOGGER.warn("Could not retrieve NamedMaterialColor from " + f.getName(), e);
        }
      });
    NAME_MAP = Collections.unmodifiableMap(nameMap);
    ID_MAP = Collections.unmodifiableMap(idMap);
  }

}
