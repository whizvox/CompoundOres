package me.whizvox.compoundores.api.util;

import net.minecraft.block.SoundType;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static me.whizvox.compoundores.CompoundOres.LOGGER;

@SuppressWarnings("unused")
public class NamedSoundType {

  public static final NamedSoundType
    WOOD = new NamedSoundType(SoundType.WOOD, "WOOD"),
    GRAVEL = new NamedSoundType(SoundType.GRAVEL, "GRAVEL"),
    GRASS = new NamedSoundType(SoundType.GRASS, "GRASS"),
    LILY_PAD = new NamedSoundType(SoundType.LILY_PAD, "LILY_PAD"),
    STONE = new NamedSoundType(SoundType.STONE, "STONE"),
    METAL = new NamedSoundType(SoundType.METAL, "METAL"),
    GLASS = new NamedSoundType(SoundType.GLASS, "GLASS"),
    WOOL = new NamedSoundType(SoundType.WOOL, "WOOL"),
    SAND = new NamedSoundType(SoundType.SAND, "SAND"),
    SNOW = new NamedSoundType(SoundType.SNOW, "SNOW"),
    LADDER = new NamedSoundType(SoundType.LADDER, "LADDER"),
    ANVIL = new NamedSoundType(SoundType.ANVIL, "ANVIL"),
    SLIME_BLOCK = new NamedSoundType(SoundType.SLIME_BLOCK, "SLIME_BLOCK"),
    HONEY_BLOCK = new NamedSoundType(SoundType.HONEY_BLOCK, "HONEY_BLOCK"),
    WET_GRASS = new NamedSoundType(SoundType.WET_GRASS, "WET_GRASS"),
    CORAL_BLOCK = new NamedSoundType(SoundType.CORAL_BLOCK, "CORAL_BLOCK"),
    BAMBOO = new NamedSoundType(SoundType.BAMBOO, "BAMBOO"),
    BAMBOO_SAPLING = new NamedSoundType(SoundType.BAMBOO_SAPLING, "BAMBOO_SAPLING"),
    SCAFFOLDING = new NamedSoundType(SoundType.SCAFFOLDING, "SCAFFOLDING"),
    SWEET_BERRY_BUSH = new NamedSoundType(SoundType.SWEET_BERRY_BUSH, "SWEET_BERRY_BUSH"),
    CROP = new NamedSoundType(SoundType.CROP, "CROP"),
    HARD_CROP = new NamedSoundType(SoundType.HARD_CROP, "HARD_CROP"),
    VINE = new NamedSoundType(SoundType.VINE, "VINE"),
    NETHER_WART = new NamedSoundType(SoundType.NETHER_WART, "NETHER_WART"),
    LANTERN = new NamedSoundType(SoundType.LANTERN, "LANTERN"),
    STEM = new NamedSoundType(SoundType.STEM, "STEM"),
    NYLIUM = new NamedSoundType(SoundType.NYLIUM, "NYLIUM"),
    FUNGUS = new NamedSoundType(SoundType.FUNGUS, "FUNGUS"),
    ROOTS = new NamedSoundType(SoundType.ROOTS, "ROOTS"),
    SHROOMLIGHT = new NamedSoundType(SoundType.SHROOMLIGHT, "SHROOMLIGHT"),
    WEEPING_VINES = new NamedSoundType(SoundType.WEEPING_VINES, "WEEPING_VINES"),
    TWISTING_VINES = new NamedSoundType(SoundType.TWISTING_VINES, "TWISTING_VINES"),
    SOUL_SAND = new NamedSoundType(SoundType.SOUL_SAND, "SOUL_SAND"),
    SOUL_SOIL = new NamedSoundType(SoundType.SOUL_SOIL, "SOUL_SOIL"),
    BASALT = new NamedSoundType(SoundType.BASALT, "BASALT"),
    WART_BLOCK = new NamedSoundType(SoundType.WART_BLOCK, "WART_BLOCK"),
    NETHERRACK = new NamedSoundType(SoundType.NETHERRACK, "NETHERRACK"),
    NETHER_BRICKS = new NamedSoundType(SoundType.NETHER_BRICKS, "NETHER_BRICKS"),
    NETHER_SPROUTS = new NamedSoundType(SoundType.NETHER_SPROUTS, "NETHER_SPROUTS"),
    NETHER_ORE = new NamedSoundType(SoundType.NETHER_ORE, "NETHER_ORE"),
    BONE_BLOCK = new NamedSoundType(SoundType.BONE_BLOCK, "BONE_BLOCK"),
    NETHERITE_BLOCK = new NamedSoundType(SoundType.NETHERITE_BLOCK, "NETHERITE_BLOCK"),
    ANCIENT_DEBRIS = new NamedSoundType(SoundType.ANCIENT_DEBRIS, "ANCIENT_DEBRIS"),
    LODESTONE = new NamedSoundType(SoundType.LODESTONE, "LODESTONE"),
    CHAIN = new NamedSoundType(SoundType.CHAIN, "CHAIN"),
    NETHER_GOLD_ORE = new NamedSoundType(SoundType.NETHER_GOLD_ORE, "NETHER_GOLD_ORE"),
    GILDED_BLACKSTONE = new NamedSoundType(SoundType.GILDED_BLACKSTONE, "GILDED_BLACKSTONE");

  public final SoundType sound;
  public final String name;
  public final boolean vanilla;

  private NamedSoundType(SoundType sound, String name, boolean vanilla) {
    this.sound = sound;
    this.name = name;
    this.vanilla = vanilla;
  }

  private NamedSoundType(SoundType sound, String name) {
    this(sound, name, true);
  }

  private static Map<String, NamedSoundType> externalCache = new HashMap<>();

  public static final Map<String, NamedSoundType> VANILLA_SOUND_TYPES;
  public static final NamedSoundType DEFAULT = STONE;

  static {
    Map<String, NamedSoundType> map = new HashMap<>();
    Arrays.stream(NamedSoundType.class.getDeclaredFields())
      .filter(f -> f.getType().isAssignableFrom(NamedSoundType.class) && Modifier.isStatic(f.getModifiers()))
      .forEach(f -> {
        try {
          NamedSoundType nSound = (NamedSoundType) f.get(null);
          map.put(nSound.name, nSound);
        } catch (Exception e) {
          LOGGER.warn("Could not retrieve NamedSoundType from " + f.getName(), e);
        }
      });
    VANILLA_SOUND_TYPES = Collections.unmodifiableMap(map);
  }

  public static NamedSoundType from(String name) {
    if (name.indexOf('.') >= 0) {
      return external(name);
    }
    return vanilla(name);
  }

  public static NamedSoundType vanilla(String name) {
    NamedSoundType sound = VANILLA_SOUND_TYPES.get(name);
    if (sound == null) {
      LOGGER.warn("Could not retrieve vanilla sound type: {}", name);
      return DEFAULT;
    }
    return sound;
  }

  public static NamedSoundType external(String name) {
    if (externalCache.containsKey(name)) {
      return externalCache.get(name);
    }
    int lastDotIndex = name.lastIndexOf('.');
    if (lastDotIndex >= 0) {
      String className = name.substring(0, lastDotIndex);
      String fieldName = name.substring(lastDotIndex + 1);
      if (SoundType.class.getName().equals(className)) {
        return vanilla(fieldName);
      }
      try {
        NamedSoundType nSound = new NamedSoundType((SoundType) Class.forName(className).getDeclaredField(fieldName).get(null), name, false);
        externalCache.put(name, nSound);
        return nSound;
      } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException | ClassCastException e) {
        LOGGER.warn("Could not resolve external sound reference: " + name, e);
      }
    } else {
      LOGGER.warn("External sound type name must resolve to a static SoundType object: {}", name);
    }
    return DEFAULT;
  }

}
