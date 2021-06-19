package me.whizvox.compoundores.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import me.whizvox.compoundores.CompoundOres;
import me.whizvox.compoundores.config.CompoundOresConfig;
import me.whizvox.compoundores.config.OreComponentJsonCodec;
import me.whizvox.compoundores.util.RegistryWrapper;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class OreComponentRegistry extends RegistryWrapper<OreComponent> {

  private static final Logger LOGGER = LogManager.getLogger();

  public static OreComponentRegistry instance;

  private List<OreComponent> sortedComponents;
  private TreeMap<Integer, OreComponent> weighed;
  private AtomicInteger totalWeight;
  private Map<ResourceLocation, OreComponent> revBlockCompMap;

  private OreComponentRegistry(IForgeRegistry<OreComponent> registry) {
    super(registry);
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

    boolean directoryComponentsRegistered = false;
    boolean shouldGenerateDefaultJsons = false;
    if (CompoundOresConfig.COMMON.registerComponentsFromDirectory()) {
      Path dir = getComponentsDirectory();
      try {
        int count = registerAllFromDirectory(dir);
        if (count > 0) {
          directoryComponentsRegistered = true;
          LOGGER.info("{} new components registered from components directory: {}", count, dir);
        } else {
          LOGGER.warn("No components were found in the components directory: {}. Will load hardcoded values instead", dir);
          if (CompoundOresConfig.COMMON.generateComponentsNotFound()) {
            shouldGenerateDefaultJsons = true;
          }
        }
      } catch (IOException e) {
        LOGGER.error("Could not read from components directory: " + dir + ". Will load backup hardcoded components instead", e);
      }
    }
    if (!directoryComponentsRegistered) {
      if (CompoundOresConfig.COMMON.registerDefaultComponents()) {
        LOGGER.debug("Registering hardcoded default ore components");
        int prevCount = instance.getValues().size();
        OreComponents.registerDefaults();
        LOGGER.info("{} new components registered from hardcoded default components", instance.getValues().size() - prevCount);
      } else {
        LOGGER.warn("No ore components have been registered by the Compound Ores mod");
      }
    }
    if (shouldGenerateDefaultJsons && !instance.getValues().isEmpty()) {
      LOGGER.debug("Configured to generate JSONs for all components");
      final Path dir = getComponentsDirectory();
      AtomicInteger count = new AtomicInteger(0);
      instance.getValues().forEach(oreComp -> {
        final Path genFilePath = dir.resolve(oreComp.getRegistryName().getPath() + ".json");
        try (Writer out = Files.newBufferedWriter(genFilePath, StandardCharsets.UTF_8)) {
          GSON.toJson(oreComp, out);
          LOGGER.debug("Generated JSON for default ore component {} at {}", oreComp.getRegistryName(), genFilePath);
          count.incrementAndGet();
        } catch (IOException e) {
          LOGGER.error("Could not generate JSON for default ore component: " + genFilePath, e);
        }
      });
      LOGGER.info("Generated {} JSON files for default components", count.get());
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

  private static Path getComponentsDirectory() {
    return FMLPaths.getOrCreateGameRelativePath(FMLPaths.GAMEDIR.get().resolve(CompoundOres.MOD_ID).resolve("components"), CompoundOres.MOD_ID);
  }

  private static final Gson GSON = new GsonBuilder()
    .registerTypeAdapter(OreComponent.class, OreComponentJsonCodec.INSTANCE)
    .setPrettyPrinting()
    .create();

  private static int registerAllFromDirectory(Path dir) throws IOException {
    AtomicInteger count = new AtomicInteger(0);
    Files.walk(dir, 1).filter(p -> p.getFileName().toString().endsWith(".json")).forEach(p -> {
      String baseName = FilenameUtils.getBaseName(p.getFileName().toString());
      try (Reader reader = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
        OreComponent oreComp = GSON.fromJson(reader, OreComponent.class);
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