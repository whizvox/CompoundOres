package me.whizvox.compoundores.api.component;

import com.google.gson.JsonParseException;
import me.whizvox.compoundores.CompoundOres;
import me.whizvox.compoundores.api.util.ComponentLootTable;
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
  private Map<ResourceLocation, ComponentLootTable> lootTables;

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
      getValues().forEach(oreComp -> {
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

      lootTables = new HashMap<>();
      nonEmptyRegistry.forEach((key, oreComp) -> lootTables.put(key, ComponentLootTable.create(oreComp)));
      lootTables = Collections.unmodifiableMap(lootTables);

      // Resolve sorted components list
      sortedComponents = new ArrayList<>(nonEmptyRegistry.values());
      sortedComponents.sort(Comparator.comparing(o -> o.getRegistryName()));
      sortedComponents = Collections.unmodifiableList(sortedComponents);
    }
  }

  public Map<ResourceLocation, ComponentLootTable> getLootTables() {
    resolveCaches();
    return lootTables;
  }

  public ComponentLootTable getLootTable(OreComponent oreComp) {
    return getLootTables().getOrDefault(oreComp.getRegistryName(), ComponentLootTable.EMPTY);
  }

  /**
   * Will register an {@link OreComponent} with respect to the mod's configuration files. Both of the following
   * conditions must evaluate to true for the component to be registered.
   * <ol>
   *   <li>Either the component's registry name is on the whitelist or not on the blacklist</li>
   *   <li>The component's registry name does not match another registry entry</li>
   * </ol>
   * If either of the following conditions, however, evaluates to false, then the component is not registered, and
   * {@link OreComponent#EMPTY} is returned instead.
   * @param value The ore component to register
   * @return The provided value if registration was successful, otherwise {@link OreComponent#EMPTY}
   */
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

  /**
   * Get how many total registered entries there are.
   * @return The number of registered entries. This will also include any components with empty resolved targets.
   */
  public int getCount() {
    return getValues().size();
  }

  /**
   * <p>Get a trimmed-down version of this registry's values where each component's target resolves to at least a one
   * block.</p>
   * <p><strong>Do not call until after {@link RegistryEvent.Register}&lt;{@link Block}&gt; has finished being
   * dispatched.</strong></p>
   * @return An unmodifiable, filtered map of this registry's values. Each value's target (via
   * {@link OreComponent#getTarget()}) must match at least one registered block. If this condition is not met, then the
   * value is not included in the returned list.
   */
  public Map<ResourceLocation, OreComponent> getNonEmptyRegistry() {
    resolveCaches();
    return nonEmptyRegistry;
  }

  @Override
  public void register(OreComponent value) {
    LOGGER.warn(REGISTRY, "Standard #register(OreComponent) method has been called. Should use #registerChecked(OreComponent) to respect the exceptions list");
    super.register(value);
  }

  @Override
  @Nonnull
  public OreComponent getValue(ResourceLocation key) {
    OreComponent comp = super.getValue(key);
    if (comp == null) {
      return OreComponent.EMPTY;
    }
    return comp;
  }

  /**
   * Get a list of all non-empty components sorted in alphabetical order.
   * <p><strong>Do not call until after {@link RegistryEvent.Register}&lt;{@link Block}&gt; has finished being
   * dispatched.</strong></p>
   * @return A list where each non-empty ore component is sorted in alphabetical order (by their registry name's path)
   * @see #getNonEmptyRegistry()
   */
  public List<OreComponent> getSortedValues() {
    resolveCaches();
    return sortedComponents;
  }

  /**
   * Get a random, non-empty component from this registry.
   * <p><strong>Do not call until after {@link RegistryEvent.Register}&lt;{@link Block}&gt; has finished being
   * dispatched.</strong></p>
   * @param rand The random object to use when picking the value
   * @return A random registered ore component that is guaranteed to be non-empty
   * @see #getNonEmptyRegistry()
   * @see #getRandomComponent(OreComponent, Random)
   */
  public OreComponent getRandomComponent(Random rand) {
    resolveCaches();
    return sortedComponents.get(rand.nextInt(sortedComponents.size()));
  }

  /**
   * Get a random, weighted, non-empty component that is specific to a specified {@link OreComponent}. This will
   * respect the mod's configuration files in its determination, meaning that if a certain secondary component is
   * either blacklisted entirely or blacklisted only in combination with the specified component, then that component
   * will never show up.
   * <p><strong>Do not call until after {@link RegistryEvent.Register}&lt;{@link Block}&gt; has finished being
   * dispatched.</strong></p>
   * @param primary The primary ore component
   * @param rand The random object to use when picking the component
   * @return An ore component that is configured to generate with the specified primary component
   * @see #getNonEmptyRegistry()
   * @see #getRandomComponent(Random)
   */
  public OreComponent getRandomComponent(OreComponent primary, Random rand) {
    resolveCaches();
    return lootTables.getOrDefault(primary.getRegistryName(), ComponentLootTable.EMPTY).next(rand);
  }

  /**
   * Get a random component from a targeted block.
   * <p><strong>Do not call until after {@link RegistryEvent.Register}&lt;{@link Block}&gt; has finished being
   * dispatched.</strong></p>
   * @param block The target block
   * @param rand The random component to use when picking a resulting ore component
   * @return Some ore component that matches the target block. If multiple components target the same block, then a
   * random one will be returned. If no ore component targets this block, then {@link OreComponent#EMPTY} is returned
   * instead.
   * @see #getNonEmptyRegistry()
   */
  public OreComponent getComponentFromBlock(Block block, Random rand) {
    resolveCaches();
    List<OreComponent> oreComponents = blockComponentLookupMap.getOrDefault(block.getRegistryName(), Collections.emptyList());
    if (oreComponents.isEmpty()) {
      return OreComponent.EMPTY;
    }
    if (oreComponents.size() == 1) {
      return oreComponents.get(0);
    }
    // it's possible for multiple components to target the same block, so get a random one
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