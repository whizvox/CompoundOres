package me.whizvox.compoundores.config;

import me.whizvox.compoundores.api.OreComponent;
import me.whizvox.compoundores.api.OreComponentRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CompoundOresCommonConfig {

  private final ForgeConfigSpec.BooleanValue mRegisterDefaultComponents;
  private final ForgeConfigSpec.BooleanValue mGenerateCompoundOres;
  private final ForgeConfigSpec.IntValue mSpawnChecks;
  private final ForgeConfigSpec.ConfigValue<List<? extends String>> mComponentsExceptions;
  private final ForgeConfigSpec.BooleanValue mComponentsWhitelist;
  private final ForgeConfigSpec.ConfigValue<List<? extends String>> mPrimaryComponentsExceptions;
  private final ForgeConfigSpec.BooleanValue mPrimaryComponentsWhitelist;
  private final ForgeConfigSpec.ConfigValue<List<? extends String>> mSecondaryComponentsExceptions;
  private final ForgeConfigSpec.BooleanValue mSecondaryComponentsWhitelist;
  private final ForgeConfigSpec.BooleanValue mRegisterDebugCommand;

  public final ForgeConfigSpec configSpec;

  CompoundOresCommonConfig() {
    ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    builder.comment("Component registration settings").push("registration");
    mRegisterDefaultComponents = builder
      .comment("Whether to allow the Compound Ores mod to register all of its default ore components",
        "Do NOT change this unless you are absolutely sure you know what you're doing!")
      .define("registerDefaultComponents", true);
    mComponentsWhitelist = builder
      .comment("Whether to treat componentsExceptions as a whitelist or a blacklist")
      .define("componentsWhitelist", false);
    builder.comment("Exceptions to ore component registration");
    mComponentsExceptions = defineResourceLocationList(builder, "componentsExceptions", Collections::emptyList);
    builder.pop();

    builder.comment("World generation settings").push("worldgen");
    mGenerateCompoundOres = builder
      .comment("Whether to generate compound ores in the world")
      .define("generateCompoundOres", true);
    mSpawnChecks = builder
      .comment("How many times per chunk to check for ores to replace with compound ores")
      .defineInRange("spawnChecks", 55, 1, 500);

    // Need to do some major refactoring to have these make sense
    mPrimaryComponentsWhitelist = builder
      .comment("Whether to treat primaryComponentsExceptions as a whitelist or a blacklist")
      .define("primaryComponentsWhitelist", false);
    builder.comment("CURRENTLY UNUSED -- Exceptions to what components generate as a base compound ore");
    mPrimaryComponentsExceptions = defineResourceLocationList(builder, "primaryComponentsExceptions", Collections::emptyList);

    mSecondaryComponentsWhitelist = builder
      .comment("Whether to treat secondaryComponentsExceptions as a whitelist or a blacklist")
      .define("secondaryComponentsWhitelist", false);
    builder.comment("CURRENTLY UNUSED -- Exceptions to what secondary component can be attached to a compound ore");
    mSecondaryComponentsExceptions = defineResourceLocationList(builder, "secondaryComponentsExceptions", Collections::emptyList);
    builder.pop();

    builder.push("general");
    mRegisterDebugCommand = builder
      .comment("Whether to register the /compores debug command")
      // TODO Change to false for stable release
      .define("registerDebugCommand", true);
    builder.pop();

    configSpec = builder.build();
  }

  public boolean registerDefaultComponents() {
    return mRegisterDefaultComponents.get();
  }

  public boolean generateCompoundOres() {
    return mGenerateCompoundOres.get();
  }

  public int spawnChecks() {
    return mSpawnChecks.get();
  }

  public List<OreComponent> componentsExceptions() {
    return oreComponentsList(mComponentsExceptions);
  }

  public boolean componentsWhitelist() {
    return mComponentsWhitelist.get();
  }

  public List<OreComponent> primaryComponentsExceptions() {
    return oreComponentsList(mPrimaryComponentsExceptions);
  }

  public boolean primaryComponentsWhitelist() {
    return mPrimaryComponentsWhitelist.get();
  }

  public List<OreComponent> secondaryComponentsExceptions() {
    return oreComponentsList(mSecondaryComponentsExceptions);
  }

  public boolean secondaryComponentsWhitelist() {
    return mSecondaryComponentsWhitelist.get();
  }

  public boolean registerDebugCommand() {
    return mRegisterDebugCommand.get();
  }

  private static List<OreComponent> oreComponentsList(ForgeConfigSpec.ConfigValue<List<? extends String>> value) {
    return value.get().stream().map(s -> OreComponentRegistry.instance.getValue(new ResourceLocation(s))).collect(Collectors.toList());
  }

  public static ForgeConfigSpec.ConfigValue<List<? extends String>> defineResourceLocationList(ForgeConfigSpec.Builder builder, String path, Supplier<List<? extends String>> defaultValueSupplier) {
    return builder.defineListAllowEmpty(Collections.singletonList(path), defaultValueSupplier, obj -> {
      if (obj instanceof String) {
        ResourceLocation name = ResourceLocation.tryParse((String) obj);
        return name != null;
      }
      return false;
    });
  }

}
