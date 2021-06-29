package me.whizvox.compoundores.util;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

import static me.whizvox.compoundores.CompoundOres.LOGGER;

public class ConfigValue<T> {

  private final T value;

  public ConfigValue(CommentedFileConfig config, String path, String comment, T defaultValue) {
    config.setComment(path, comment);
    if (config.contains(path)) {
      value = config.get(path);
    } else {
      config.add(path, defaultValue);
      value = defaultValue;
    }
  }

  public <O> ConfigValue(CommentedFileConfig config, String path, String comment, T defaultValue, Codec<T, O> codec) {
    config.setComment(path, comment);
    if (config.contains(path)) {
      value = codec.decode(config.get(path));
    } else {
      config.add(path, codec.encode(defaultValue));
      value = defaultValue;
    }
  }

  public T get() {
    return value;
  }

  public static ConfigValue<Integer> intRange(CommentedFileConfig config, String path, String comment, int defaultValue, int min, int max) {
    if (config.contains(path)) {
      int value = config.getInt(path);
      if (value < min || value > max) {
        LOGGER.warn("Integer configuration value out-of-bounds [{},{}]: {}", min, max, value);
        config.set(path, defaultValue);
      }
    }
    return new ConfigValue<>(config, path, comment + "\nRange: " + min + " ~ " + max, defaultValue);
  }

  public interface Codec<T, O> {
    T decode(O obj);
    O encode(T obj);

    Codec<List<ResourceLocation>, List<String>> RESOURCE_LOCATION_LIST = new Codec<List<ResourceLocation>, List<String>>() {
      @Override
      public List<ResourceLocation> decode(List<String> obj) {
        return obj.stream().map(ResourceLocation::new).collect(Collectors.toList());
      }
      @Override
      public List<String> encode(List<ResourceLocation> obj) {
        return obj.stream().map(ResourceLocation::toString).collect(Collectors.toList());
      }
    };
    Codec<ResourceLocation, String> RESOURCE_LOCATION = new Codec<ResourceLocation, String>() {
      @Override
      public ResourceLocation decode(String obj) {
        return new ResourceLocation(obj);
      }
      @Override
      public String encode(ResourceLocation obj) {
        return obj.toString();
      }
    };
  }

}
