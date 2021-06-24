package me.whizvox.compoundores.api.util;

import net.minecraft.block.material.MaterialColor;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NamedMaterialColor {

  public final MaterialColor base;
  public final String name;

  private NamedMaterialColor(MaterialColor base, String name) {
    this.base = base;
    this.name = name;
  }

  public static final Map<String, NamedMaterialColor> NAME_MAP;
  public static final Map<Integer, NamedMaterialColor> ID_MAP;

  static {
    Map<String, NamedMaterialColor> nameMap = new HashMap<>();
    Map<Integer, NamedMaterialColor> idMap = new HashMap<>();
    Arrays.stream(MaterialColor.class.getDeclaredFields())
      .filter(field -> field.getType().isAssignableFrom(MaterialColor.class) && Modifier.isStatic(field.getModifiers()))
      .forEach(field -> {
        try {
          MaterialColor color = (MaterialColor) field.get(null);
          String name = field.getName().toLowerCase();
          NamedMaterialColor nmc = new NamedMaterialColor(color, name);
          nameMap.put(name, nmc);
          idMap.put(color.id, nmc);
        } catch (IllegalAccessException e) {
          throw new RuntimeException("Could not access MaterialColor field: " + field.getName(), e);
        }
      });
    NAME_MAP = Collections.unmodifiableMap(nameMap);
    ID_MAP = Collections.unmodifiableMap(idMap);
  }

}
