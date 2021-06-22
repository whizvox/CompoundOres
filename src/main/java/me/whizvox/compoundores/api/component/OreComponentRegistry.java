package me.whizvox.compoundores.api.component;

import com.google.gson.JsonParseException;
import me.whizvox.compoundores.CompoundOres;
import me.whizvox.compoundores.config.CompoundOresConfig;
import me.whizvox.compoundores.util.JsonHelper;
import me.whizvox.compoundores.util.PathHelper;
import me.whizvox.compoundores.util.RegistryWrapper;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static me.whizvox.compoundores.CompoundOres.LOGGER;
import static me.whizvox.compoundores.helper.Markers.REGISTRY;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class OreComponentRegistry extends RegistryWrapper<OreComponent> {

  private List<ResourceLocation> registryExceptions;
  private boolean whitelistExceptions;

  private Map<ResourceLocation, OreComponent> nonEmptyRegistry;
  private List<OreComponent> sortedComponents;
  private TreeMap<Integer, OreComponent> weighedValues;
  private final AtomicInteger totalWeight;
  private Map<ResourceLocation, List<OreComponent>> blockComponentLookupMap;

  private OreComponentRegistry(IForgeRegistry<OreComponent> registry) {
    super(registry);
    totalWeight = new AtomicInteger(0);
    nonEmptyRegistry = null;
  }

  private void resolveCaches() {
    if (nonEmptyRegistry == null) {
      LOGGER.info(REGISTRY, "Resolving ore component registry wrapper's caches");
      // Resolve additional map that only contains targets that have at least 1 resolved block
      nonEmptyRegistry = new HashMap<>();
      // Also update the total weight to only consider these values
      totalWeight.set(0);
      // Also update the weighted component map
      weighedValues = new TreeMap<>();
      blockComponentLookupMap = new HashMap<>();
      super.getValues().forEach(oreComp -> {
        Set<Block> resolved = oreComp.getTarget().getResolvedTargets();
        if (!resolved.isEmpty()) {
          nonEmptyRegistry.put(oreComp.getRegistryName(), oreComp);
          weighedValues.put(totalWeight.getAndAdd(oreComp.getWeight()), oreComp);
          resolved.forEach(block -> {
            List<OreComponent> oreComponents = blockComponentLookupMap.computeIfAbsent(block.getRegistryName(), o -> new ArrayList<>());
            if (!oreComponents.contains(oreComp)) {
              oreComponents.add(oreComp);
            }
          });
        }
      });
      nonEmptyRegistry = Collections.unmodifiableMap(nonEmptyRegistry);
      blockComponentLookupMap = Collections.unmodifiableMap(blockComponentLookupMap);

      // Resolve sorted components list
      sortedComponents = new ArrayList<>(nonEmptyRegistry.values());
      sortedComponents.sort(Comparator.comparing(o -> o.getRegistryName()));
      sortedComponents = Collections.unmodifiableList(sortedComponents);
    }
  }

  public OreComponent registerChecked(OreComponent value) {
    if (whitelistExceptions) {
      if (!registryExceptions.contains(value.getRegistryName())) {
        LOGGER.debug(REGISTRY, "Skipped registry of {} since it isn't on the whitelist", value.getRegistryName());
        return OreComponent.EMPTY;
      }
    } else {
      if (registryExceptions.contains(value.getRegistryName())) {
        LOGGER.debug(REGISTRY, "Skipped registry of {} since it is on the blacklist", value.getRegistryName());
        return OreComponent.EMPTY;
      }
    }
    if (containsKey(value.getRegistryName())) {
      LOGGER.debug(REGISTRY, "Attempted to register more than 1 component with the same registry name: {}", value.getRegistryName());
      return OreComponent.EMPTY;
    }
    super.register(value);
    return value;
  }

  public int getCount() {
    return super.getValues().size();
  }

  @Override
  public void register(OreComponent value) {
    LOGGER.warn(REGISTRY, "Standard #register(OreComponent) method has been called. Should use #registerChecked(OreComponent) to respect the exceptions list");
    super.register(value);
  }

  @Override
  @Nonnull
  public OreComponent getValue(ResourceLocation key) {
    resolveCaches();
    return nonEmptyRegistry.getOrDefault(key, OreComponent.EMPTY);
  }

  @Nonnull
  @Override
  public Collection<OreComponent> getValues() {
    resolveCaches();
    return nonEmptyRegistry.values();
  }

  public List<OreComponent> getSortedValues() {
    resolveCaches();
    return sortedComponents;
  }

  public int getTotalWeight() {
    resolveCaches();
    return totalWeight.get();
  }

  public OreComponent getWeighedRandomComponent(Random rand) {
    resolveCaches();
    return weighedValues.get(weighedValues.floorKey(rand.nextInt(getTotalWeight())));
  }

  public OreComponent getRandomComponent(Random rand) {
    resolveCaches();
    // sortedComponents is already a List, while nonEmptyRegistry.getValues() is Collection
    return sortedComponents.get(rand.nextInt(sortedComponents.size()));
  }

  public OreComponent getComponentFromBlock(Block block, Random rand) {
    resolveCaches();
    List<OreComponent> oreComponents = blockComponentLookupMap.getOrDefault(block.getRegistryName(), Collections.emptyList());
    if (oreComponents.isEmpty()) {
      return OreComponent.EMPTY;
    }
    return oreComponents.get(rand.nextInt(oreComponents.size()));
  }

  private static OreComponentRegistry instance;

  public static OreComponentRegistry getInstance() {
    return instance;
  }

  @SubscribeEvent
  public static void onNewRegistry(RegistryEvent.NewRegistry event) {
    LOGGER.info(REGISTRY, "Creating ore component registry");
    instance = new OreComponentRegistry(new RegistryBuilder<OreComponent>()
      .setName(new ResourceLocation(CompoundOres.MOD_ID, "ore_components"))
      .setType(OreComponent.class)
      .create()
    );

    instance.registryExceptions = CompoundOresConfig.COMMON.componentsExceptions();
    instance.whitelistExceptions = CompoundOresConfig.COMMON.componentsWhitelist();
    if (instance.whitelistExceptions) {
      if (instance.registryExceptions.isEmpty()) {
        LOGGER.warn(REGISTRY, "An empty whitelist was detected for ore component registration. No components will be registered!");
      } else {
        LOGGER.info(REGISTRY, "A whitelist was detected for ore component registration: [{}]", instance.registryExceptions.stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")));
      }
    } else {
      if (instance.registryExceptions.isEmpty()) {
        LOGGER.info(REGISTRY, "An empty blacklist was detected for ore component registration. All components will be registered");
      } else {
        LOGGER.info(REGISTRY, "A blacklist was detected for ore component registration: [{}]", instance.registryExceptions.stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")));
      }
    }

    if (CompoundOresConfig.COMMON.registerComponentsFromDirectory()) {
      final Path dir = PathHelper.COMPONENTS_DIR;
      try {
        int count = registerAllFromDirectory(dir);
        if (count > 0) {
          LOGGER.info(REGISTRY, "{} new components registered from components directory: {}", count, dir);
        } else {
          LOGGER.debug(REGISTRY, "No components were found in the components directory: {}", dir);
        }
      } catch (IOException e) {
        LOGGER.error(REGISTRY, "Could not read from components directory: " + dir + ". Will load backup hardcoded components instead", e);
      }
    }
    if (CompoundOresConfig.COMMON.registerDefaultComponents()) {
      LOGGER.debug(REGISTRY, "Registering default ore components");
      int prevCount = instance.getCount();
      OreComponents.registerDefaults();
      LOGGER.info(REGISTRY, "{} new components registered from default components", instance.getCount() - prevCount);
    } else {
      LOGGER.warn(REGISTRY, "No ore components have been registered by the Compound Ores mod");
    }
  }

  private static int registerAllFromDirectory(Path dir) throws IOException {
    AtomicInteger count = new AtomicInteger(0);
    Files.walk(dir, 1).filter(p -> p.getFileName().toString().endsWith(".json")).forEach(p -> {
      String baseName = FilenameUtils.getBaseName(p.getFileName().toString());
      try (Reader reader = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
        OreComponent oreComp = JsonHelper.GSON.fromJson(reader, OreComponent.class);
        oreComp.setRegistryName(CompoundOres.MOD_ID, baseName);
        instance.register(oreComp);
        LOGGER.debug(REGISTRY, "Registered new ore component from {}", p);
        count.incrementAndGet();
      } catch (JsonParseException | IOException e) {
        if (CompoundOresConfig.COMMON.ignoreBadComponents()) {
          LOGGER.error(REGISTRY, "Could not parse ore component from " + p, e);
        } else {
          throw new RuntimeException("Set to hard crash in the event of a badly-defined or malformed component: " + p, e);
        }
      }
    });
    return count.get();
  }

}