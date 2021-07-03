package me.whizvox.compoundores.api.component;

import com.google.gson.JsonParseException;
import me.whizvox.compoundores.CompoundOres;
import me.whizvox.compoundores.api.util.ComponentLootTable;
import me.whizvox.compoundores.config.CompoundOresConfig;
import me.whizvox.compoundores.helper.JsonHelper;
import me.whizvox.compoundores.helper.PathHelper;
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
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static me.whizvox.compoundores.CompoundOres.LOGGER;
import static me.whizvox.compoundores.helper.Markers.REGISTRY;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class OreComponentRegistry extends RegistryWrapper<OreComponent> {

  private Map<ResourceLocation, OreComponent> nonEmptyRegistry;
  private List<OreComponent> sortedComponents;
  private Map<ResourceLocation, List<OreComponent>> blockLookupMap;
  private Map<String, Collection<ResourceLocation>> originalGroups;
  private Map<ResourceLocation, Set<OreComponent>> groups;
  private Map<ResourceLocation, ComponentLootTable> lootTables;

  private OreComponentRegistry(IForgeRegistry<OreComponent> registry) {
    super(registry);
    nonEmptyRegistry = null;
    originalGroups = new HashMap<>();
    groups = new HashMap<>();
  }

  private void resolveCaches() {
    if (nonEmptyRegistry == null) {
      LOGGER.info(REGISTRY, "Resolving ore component registry wrapper's caches");
      // Resolve additional map that only contains targets that have at least 1 resolved block
      nonEmptyRegistry = new HashMap<>();
      blockLookupMap = new HashMap<>();
      getValues().forEach(oreComp -> {
        Set<Block> resolved = oreComp.getTarget().getResolvedTargets();
        if (!oreComp.isEmpty() && !resolved.isEmpty()) {
          nonEmptyRegistry.put(oreComp.getRegistryName(), oreComp);
          resolved.forEach(block -> {
            List<OreComponent> oreComponents = blockLookupMap.computeIfAbsent(block.getRegistryName(), o -> new ArrayList<>());
            if (!oreComponents.contains(oreComp)) {
              oreComponents.add(oreComp);
            }
          });
        }
      });
      nonEmptyRegistry = Collections.unmodifiableMap(nonEmptyRegistry);
      blockLookupMap = Collections.unmodifiableMap(blockLookupMap);

      try {
        loadGroupsFromDirectory(PathHelper.GROUPS_DIR).forEach((file, items) -> {
          items.forEach(compName -> {
            OreComponent comp = nonEmptyRegistry.get(compName);
            if (comp != null) {
              Set<OreComponent> affiliated = new HashSet<>();
              items.stream().filter(compName2 -> !compName2.equals(compName)).forEach(compName2 -> {
                OreComponent comp2 = nonEmptyRegistry.get(compName2);
                if (comp2 != null) {
                  affiliated.add(comp2);
                }
              });
              if (groups.containsKey(compName)) {
                LOGGER.debug(REGISTRY, "Overwriting default component group affiliation for {}", compName);
              }
              groups.put(compName, Collections.unmodifiableSet(affiliated));
            }
          });
          originalGroups.put(file, items);
        });
      } catch (IOException e) {
        LOGGER.error(REGISTRY, "Could not load groups from directory: " + PathHelper.GROUPS_DIR, e);
      }

      lootTables = new HashMap<>();
      nonEmptyRegistry.values().stream()
        // don't create a loot table for configured primary component exceptions
        .filter(c -> CompoundOresConfig.COMMON.permitPrimaryOre(c.getRegistryName()))
        .forEach(oreComp -> lootTables.put(oreComp.getRegistryName(), ComponentLootTable.create(oreComp)));
      lootTables = Collections.unmodifiableMap(lootTables);

      // Resolve sorted components list
      sortedComponents = new ArrayList<>(nonEmptyRegistry.values());
      sortedComponents.sort(Comparator.comparing(OreComponent::getRegistryName));
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

  public Set<OreComponent> getGroup(OreComponent comp) {
    return Collections.unmodifiableSet(groups.getOrDefault(comp.getRegistryName(), Collections.emptySet()));
  }

  public void forEachGroup(BiConsumer<? super String, ? super Collection<ResourceLocation>> action) {
    originalGroups.forEach(action);
  }

  public void addGroup(String name, Collection<OreComponent> components) {
    if (components.size() > 1) {
      components.forEach(comp1 -> {
        List<OreComponent> affiliated = new ArrayList<>();
        components.stream().filter(comp2 -> !comp2.equals(comp1)).forEach(affiliated::add);
        groups.computeIfAbsent(comp1.getRegistryName(), k -> new HashSet<>()).addAll(affiliated);
      });
      originalGroups.put(name, components.stream().map(OreComponent::getRegistryName).collect(Collectors.toList()));
    } else if (components.size() == 1) {
      LOGGER.warn(REGISTRY, "Attempted to add an ore component group of invalid size (0 or 1): {}", components.stream().findFirst().get());
    } else {
      LOGGER.warn(REGISTRY, "Attempted to add an empty ore component group");
    }
  }

  @Override
  public void register(OreComponent value) {
    if (value.isEmpty()) {
      LOGGER.warn(REGISTRY, "Attempted to register an empty ore: {}", value.getRegistryName());
    } else if (!CompoundOresConfig.COMMON.permitOre(value.getRegistryName())) {
      LOGGER.debug(REGISTRY, "Ore configured to not register: {}", value.getRegistryName());
    } else {
      super.register(value);
    }
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
    List<OreComponent> oreComponents = blockLookupMap.getOrDefault(block.getRegistryName(), Collections.emptyList());
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

    List<ResourceLocation> exceptions = CompoundOresConfig.COMMON.componentExceptions.get();
    if (CompoundOresConfig.COMMON.componentExceptionsWhitelist.get()) {
      if (exceptions.isEmpty()) {
        LOGGER.warn(REGISTRY, "An empty whitelist was detected for ore component registration. No components will be registered!");
      } else {
        LOGGER.info(REGISTRY, "A whitelist was detected for ore component registration: [{}]", exceptions.stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")));
      }
    } else {
      if (exceptions.isEmpty()) {
        LOGGER.info(REGISTRY, "An empty blacklist was detected for ore component registration. All components will be registered");
      } else {
        LOGGER.info(REGISTRY, "A blacklist was detected for ore component registration: [{}]", exceptions.stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")));
      }
    }

    if (CompoundOresConfig.COMMON.registerComponentsFromDirectory.get()) {
      final Path dir = PathHelper.COMPONENTS_DIR;
      try {
        int count = registerComponentsFromDirectory(dir);
        if (count > 0) {
          LOGGER.info(REGISTRY, "{} new components registered from components directory: {}", count, dir);
        } else {
          LOGGER.debug(REGISTRY, "No components were found in the components directory: {}", dir);
        }
      } catch (IOException e) {
        LOGGER.error(REGISTRY, "Could not read from components directory: " + dir + ". Will load backup hardcoded components instead", e);
      }
    }
    if (CompoundOresConfig.COMMON.registerDefaultComponents.get()) {
      LOGGER.debug(REGISTRY, "Registering default ore components");
      int prevCount = instance.getCount();
      OreComponents.registerDefaults();
      LOGGER.info(REGISTRY, "{} new components registered from default components", instance.getCount() - prevCount);
    } else {
      LOGGER.warn(REGISTRY, "No ore components have been registered by the Compound Ores mod");
    }
  }

  private static int registerComponentsFromDirectory(Path dir) throws IOException {
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
        if (CompoundOresConfig.COMMON.ignoreBadComponents.get()) {
          LOGGER.error(REGISTRY, "Could not parse ore component from " + p, e);
        } else {
          throw new RuntimeException("Set to hard crash in the event of a badly-defined or malformed component: " + p, e);
        }
      }
    });
    return count.get();
  }

  private static Map<String, List<ResourceLocation>> loadGroupsFromDirectory(Path dir) throws IOException {
    Map<String, List<ResourceLocation>> groups = new HashMap<>();
    Files.walk(dir, 1).filter(p -> p.getFileName().toString().endsWith(".json")).forEach(p -> {
      try (Reader reader = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
        String[] componentStr = JsonHelper.GSON.fromJson(reader, String[].class);
        List<ResourceLocation> list = new ArrayList<>();
        for (String nameStr : componentStr) {
          ResourceLocation name = new ResourceLocation(nameStr);
          list.add(name);
        }
        groups.put(FilenameUtils.getBaseName(p.getFileName().toString()), list);
      } catch (JsonParseException | IOException e) {
        if (CompoundOresConfig.COMMON.ignoreBadComponents.get()) {
          LOGGER.error(REGISTRY, "Could not read group definition from " + p, e);
        } else {
          throw new RuntimeException("Failed to read group definition from " + p, e);
        }
      }
    });
    return groups;
  }

}