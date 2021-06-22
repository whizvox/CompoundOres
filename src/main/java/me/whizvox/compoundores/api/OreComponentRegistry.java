package me.whizvox.compoundores.api;

import com.google.gson.JsonParseException;
import me.whizvox.compoundores.CompoundOres;
import me.whizvox.compoundores.config.CompoundOresConfig;
import me.whizvox.compoundores.util.JsonHelper;
import me.whizvox.compoundores.util.PathHelper;
import me.whizvox.compoundores.util.RegistryWrapper;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class OreComponentRegistry extends RegistryWrapper<OreComponent> {

  private static final Logger LOGGER = LogManager.getLogger();

  private List<ResourceLocation> registryExceptions;
  private boolean whitelistExceptions;

  private List<OreComponent> sortedComponents;
  private TreeMap<Integer, OreComponent> weighed;
  private AtomicInteger totalWeight;
  private Map<ResourceLocation, OreComponent> revBlockCompMap;

  private OreComponentRegistry(IForgeRegistry<OreComponent> registry) {
    super(registry);
  }

  public OreComponent registerChecked(OreComponent value) {
    if (whitelistExceptions) {
      if (!registryExceptions.contains(value.getRegistryName())) {
        LOGGER.debug("Skipped registry of {} since it isn't on the whitelist", value.getRegistryName());
        return OreComponent.EMPTY;
      }
    } else {
      if (registryExceptions.contains(value.getRegistryName())) {
        LOGGER.debug("Skipped registry of {} since it is on the blacklist", value.getRegistryName());
        return OreComponent.EMPTY;
      }
    }
    if (containsKey(value.getRegistryName())) {
      LOGGER.debug("Attempted to register more than 1 component with the same registry name: {}", value.getRegistryName());
      return OreComponent.EMPTY;
    }
    super.register(value);
    return value;
  }

  @Override
  public void register(OreComponent value) {
    LOGGER.warn("Standard #register(OreComponent) method has been called. Should use #registerChecked(OreComponent) to respect the exceptions list");
    super.register(value);
  }

  @Override
  @Nonnull
  public OreComponent getValue(ResourceLocation key) {
    OreComponent value = super.getValue(key);
    if (value == null) {
      return OreComponent.EMPTY;
    }
    return value;
  }

  public List<OreComponent> getSortedValues() {
    return sortedComponents;
  }

  public int getTotalWeight() {
    return totalWeight.get();
  }

  public OreComponent getRandomComponent(Random rand) {
    return weighed.get(weighed.floorKey(rand.nextInt(getTotalWeight())));
  }

  public OreComponent getComponentFromBlock(Block block) {
    return revBlockCompMap.get(block.getRegistryName());
  }

  private static OreComponentRegistry instance;

  public static OreComponentRegistry getInstance() {
    return instance;
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public static void onNewRegistry(RegistryEvent.NewRegistry event) {
    LOGGER.info("Creating ore component registry");
    instance = new OreComponentRegistry(new RegistryBuilder<OreComponent>()
      .setName(new ResourceLocation(CompoundOres.MOD_ID, "ore_components"))
      .setType(OreComponent.class)
      .create()
    );

    instance.registryExceptions = CompoundOresConfig.COMMON.componentsExceptions();
    instance.whitelistExceptions = CompoundOresConfig.COMMON.componentsWhitelist();
    if (instance.whitelistExceptions) {
      if (instance.registryExceptions.isEmpty()) {
        LOGGER.warn("An empty whitelist was detected for ore component registration. No components will be registered!");
      } else {
        LOGGER.info("A whitelist was detected for ore component registration: [{}]", instance.registryExceptions.stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")));
      }
    } else {
      if (instance.registryExceptions.isEmpty()) {
        LOGGER.info("An empty blacklist was detected for ore component registration. All components will be registered");
      } else {
        LOGGER.info("A blacklist was detected for ore component registration: [{}]", instance.registryExceptions.stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")));
      }
    }

    if (CompoundOresConfig.COMMON.registerComponentsFromDirectory()) {
      final Path dir = PathHelper.COMPONENTS_DIR;
      try {
        int count = registerAllFromDirectory(dir);
        if (count > 0) {
          LOGGER.info("{} new components registered from components directory: {}", count, dir);
        } else {
          LOGGER.debug("No components were found in the components directory: {}", dir);
        }
      } catch (IOException e) {
        LOGGER.error("Could not read from components directory: " + dir + ". Will load backup hardcoded components instead", e);
      }
    }
    if (CompoundOresConfig.COMMON.registerDefaultComponents()) {
      LOGGER.debug("Registering default ore components");
      int prevCount = instance.getValues().size();
      OreComponents.registerDefaults();
      LOGGER.info("{} new components registered from default components", instance.getValues().size() - prevCount);
    } else {
      LOGGER.warn("No ore components have been registered by the Compound Ores mod");
    }

    List<OreComponent> components = new ArrayList<>(instance.getValues());
    components.sort(OreComponent::compareTo);
    instance.sortedComponents = Collections.unmodifiableList(components);

    instance.totalWeight = new AtomicInteger(0);
    TreeMap<Integer, OreComponent> weighed = new TreeMap<>();
    instance.getValues().forEach(oreComp -> weighed.put(instance.totalWeight.getAndAdd(oreComp.getSpawnWeight()), oreComp));
    instance.weighed = weighed;
    Map<ResourceLocation, OreComponent> revBlockCompMap = new HashMap<>();
    instance.getValues().forEach(oreComp -> revBlockCompMap.put(oreComp.getBlock().getRegistryName(), oreComp));
    instance.revBlockCompMap = Collections.unmodifiableMap(revBlockCompMap);
  }

  private static int registerAllFromDirectory(Path dir) throws IOException {
    AtomicInteger count = new AtomicInteger(0);
    Files.walk(dir, 1).filter(p -> p.getFileName().toString().endsWith(".json")).forEach(p -> {
      String baseName = FilenameUtils.getBaseName(p.getFileName().toString());
      try (Reader reader = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
        OreComponent oreComp = JsonHelper.GSON.fromJson(reader, OreComponent.class);
        oreComp.setRegistryName(CompoundOres.MOD_ID, baseName);
        instance.register(oreComp);
        LOGGER.debug("Registered new ore component from {}", p);
        count.incrementAndGet();
      } catch (JsonParseException | IOException e) {
        if (CompoundOresConfig.COMMON.ignoreBadComponents()) {
          LOGGER.error("Could not parse ore component from " + p, e);
        } else {
          throw new RuntimeException("Set to hard crash in the event of a badly-defined or malformed component: " + p, e);
        }
      }
    });
    return count.get();
  }

}