package me.whizvox.compoundores.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import me.whizvox.compoundores.util.ConfigValue;
import net.minecraft.util.ResourceLocation;

import java.util.List;

import static java.util.Collections.emptyList;

public class CommonConfig {

  public final ConfigValue<Boolean> registerComponentsFromDirectory;
  public final ConfigValue<Boolean> ignoreBadComponents;
  public final ConfigValue<Boolean> registerDefaultComponents;
  public final ConfigValue<List<ResourceLocation>> componentExceptions;
  public final ConfigValue<Boolean> componentExceptionsWhitelist;

  public final ConfigValue<Boolean> generateCompoundOres;
  public final ConfigValue<Integer> spawnChecks;
  public final ConfigValue<Integer> maxReplace;
  public final ConfigValue<List<ResourceLocation>> primaryExceptions;
  public final ConfigValue<Boolean> primaryExceptionsWhitelist;
  public final ConfigValue<List<ResourceLocation>> secondaryExceptions;
  public final ConfigValue<Boolean> secondaryExceptionsWhitelist;
  public final ConfigValue<Boolean> registerDebugCommand;

  public final FileConfig config;

  CommonConfig(CommentedFileConfig config) {
    config.load();
    config.setComment("registration", "Registration settings");
    registerComponentsFromDirectory = new ConfigValue<>(config, "registration.registerComponentsFromDirectory",
      "Whether to register all JSON-defined components in compoundores/components", true);
    ignoreBadComponents = new ConfigValue<>(config, "registration.ignoreBadComponents",
      "Whether to ignore and skip over badly-defined components in compoundores/components", false);
    registerDefaultComponents = new ConfigValue<>(config, "registration.registerDefaultComponents",
      "Whether to register all default components specified by the Compound Ores mod", true);
    componentExceptions = new ConfigValue<>(config, "registration.componentExceptions",
      "Exceptional components to the ore component registration", emptyList(), ConfigValue.Codec.RESOURCE_LOCATION_LIST);
    componentExceptionsWhitelist = new ConfigValue<>(config, "registration.componentExceptionsWhitelist",
      "Whether to treat componentExceptions as a whitelist or not", false);
    registerDebugCommand = new ConfigValue<>(config, "registration.registerDebugCommand",
      "Whether to register the debug-only subcommands of /compores", false);

    config.setComment("worldgen", "World generation settings");
    generateCompoundOres = new ConfigValue<>(config, "worldgen.generateCompoundOres",
      "Whether to generate compound ore veins in the world", true);
    spawnChecks = ConfigValue.intRange(config, "worldgen.spawnChecks",
      "How many times per chunk to try to spawn a compound ore vein", 55, 1, 500);
    maxReplace = ConfigValue.intRange(config, "worldgen.maxReplace",
      "The maximum number of ores to replace in a compound ore vein", 20, 1, 500);
    primaryExceptions = new ConfigValue<>(config, "worldgen.primaryExceptions",
      "Exceptional components for when compound ore blocks spawn", emptyList(), ConfigValue.Codec.RESOURCE_LOCATION_LIST);
    primaryExceptionsWhitelist = new ConfigValue<>(config, "worldgen.primaryExceptionsWhitelist",
      "Whether to treat primaryExceptions as a whitelist or not", false);
    secondaryExceptions = new ConfigValue<>(config, "worldgen.secondaryExceptions",
      "Exceptional components for when attaching a secondary component to a compound ore", emptyList(), ConfigValue.Codec.RESOURCE_LOCATION_LIST);
    secondaryExceptionsWhitelist = new ConfigValue<>(config, "worldgen.secondaryExceptionsWhitelist",
      "Whether to treat secondaryExceptions as a whitelist or not", false);

    config.save();

    this.config = config;
  }

}
