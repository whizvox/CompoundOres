package me.whizvox.compoundores.util;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class RegistryWrapper<V extends IForgeRegistryEntry<V>> implements IForgeRegistry<V> {

  private IForgeRegistry<V> registry;

  public RegistryWrapper(IForgeRegistry<V> registry) {
    this.registry = registry;
  }

  public IForgeRegistry<V> getBaseRegistry() {
    return registry;
  }

  @Override
  public ResourceLocation getRegistryName() {
    return registry.getRegistryName();
  }

  @Override
  public Class<V> getRegistrySuperType() {
    return registry.getRegistrySuperType();
  }

  @Override
  public void register(V value) {
    registry.register(value);
  }

  @Override
  public void registerAll(V... values) {
    registry.registerAll(values);
  }

  @Override
  public boolean containsKey(ResourceLocation key) {
    return registry.containsKey(key);
  }

  @Override
  public boolean containsValue(V value) {
    return registry.containsValue(value);
  }

  @Override
  public boolean isEmpty() {
    return registry.isEmpty();
  }

  @Nullable
  @Override
  public V getValue(ResourceLocation key) {
    return registry.getValue(key);
  }

  @Nullable
  @Override
  public ResourceLocation getKey(V value) {
    return registry.getKey(value);
  }

  @Nullable
  @Override
  public ResourceLocation getDefaultKey() {
    return registry.getDefaultKey();
  }

  @Nonnull
  @Override
  public Set<ResourceLocation> getKeys() {
    return registry.getKeys();
  }

  @Nonnull
  @Override
  public Collection<V> getValues() {
    return registry.getValues();
  }

  @Nonnull
  @Override
  public Set<Map.Entry<RegistryKey<V>, V>> getEntries() {
    return registry.getEntries();
  }

  @Override
  public <T> T getSlaveMap(ResourceLocation slaveMapName, Class<T> type) {
    return registry.getSlaveMap(slaveMapName, type);
  }

  @Override
  public Iterator<V> iterator() {
    return registry.iterator();
  }

}
