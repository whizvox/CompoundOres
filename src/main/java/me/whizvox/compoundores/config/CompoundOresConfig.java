package me.whizvox.compoundores.config;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class CompoundOresConfig {

  public static final CompoundOresCommonConfig COMMON = new CompoundOresCommonConfig();

  public static void register(ModLoadingContext context) {
    context.registerConfig(ModConfig.Type.COMMON, COMMON.configSpec, "compoundores-common.toml");
  }

}
