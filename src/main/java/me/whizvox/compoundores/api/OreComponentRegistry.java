package me.whizvox.compoundores.api;

import me.whizvox.compoundores.CompoundOres;
import me.whizvox.compoundores.util.RegistryWrapper;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class OreComponentRegistry extends RegistryWrapper<OreComponent> {

  public static OreComponentRegistry instance;

  private List<OreComponent> sortedComponents;
  private TreeMap<Integer, OreComponent> weighed;
  private AtomicInteger totalWeight;
  private Map<ResourceLocation, OreComponent> revBlockCompMap;

  private OreComponentRegistry(IForgeRegistry<OreComponent> registry) {
    super(registry);
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
    CompoundOres.LOGGER.info("CREATING ORE COMPONENT REGISTRY");
    instance = new OreComponentRegistry(new RegistryBuilder<OreComponent>()
      .setName(new ResourceLocation(CompoundOres.MOD_ID, "ore_components"))
      .setType(OreComponent.class)
      .create()
    );
    CompoundOres.LOGGER.info("REGISTERING DEFAULT ORE COMPONENTS");
    OreComponents.registerAll();
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

}