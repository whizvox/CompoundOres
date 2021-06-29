package me.whizvox.compoundores.api.util;

import me.whizvox.compoundores.api.component.OreComponent;
import me.whizvox.compoundores.api.component.OreComponentRegistry;
import me.whizvox.compoundores.config.CompoundOresConfig;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ComponentLootTable {

  private final TreeMap<Integer, OreComponent> map;
  private final int totalWeight;

  public ComponentLootTable(TreeMap<Integer, OreComponent> map, int totalWeight) {
    this.map = map;
    this.totalWeight = totalWeight;
  }

  public int getTotalWeight() {
    return totalWeight;
  }

  public boolean contains(OreComponent oreComponent) {
    return map.containsValue(oreComponent);
  }

  public Collection<OreComponent> all() {
    return map.values();
  }

  public OreComponent next(Random rand) {
    return map.get(map.floorKey(rand.nextInt(totalWeight)));
  }

  public static ComponentLootTable create(OreComponent oreComp) {
    TreeMap<Integer, OreComponent> map = new TreeMap<>();
    AtomicInteger totalWeight = new AtomicInteger(0);
    List<ResourceLocation> exceptions = CompoundOresConfig.COMMON.secondaryExceptions.get();
    boolean whitelist = CompoundOresConfig.COMMON.secondaryExceptionsWhitelist.get();
    OreComponentRegistry.getInstance().getNonEmptyRegistry().values().stream()
      .filter(c ->
        !oreComp.equals(c) &&
          ((whitelist && exceptions.contains(c.getRegistryName())) || (!whitelist && !exceptions.contains(c.getRegistryName()))) &&
          !c.getTarget().getResolvedTargets().isEmpty() &&
          !OreComponentRegistry.getInstance().getGroup(oreComp).contains(c))
      .forEach(c -> map.put(totalWeight.getAndAdd(c.getWeight()), c));
    return new ComponentLootTable(map, totalWeight.get());
  }

  public static final ComponentLootTable EMPTY = new ComponentLootTable(new TreeMap<>(), 0) {
    @Override
    public OreComponent next(Random rand) {
      return OreComponent.EMPTY;
    }
  };

}
