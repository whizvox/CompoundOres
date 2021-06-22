package me.whizvox.compoundores.api.target;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class MultiNameTarget implements IBlockTarget {

  private final Set<ResourceLocation> blockNames;

  public MultiNameTarget(Set<ResourceLocation> blockNames) {
    this.blockNames = Collections.unmodifiableSet(blockNames);
  }

  @Override
  public boolean accepted(Block subject) {
    return blockNames.contains(subject.getRegistryName());
  }

  @Override
  public Set<Block> getResolvedTargets() {
    return blockNames.stream()
      .filter(ForgeRegistries.BLOCKS::containsKey)
      .map(ForgeRegistries.BLOCKS::getValue)
      .collect(Collectors.toSet());
  }

  @Override
  public List<String> serialize() {
    return blockNames.stream().map(ResourceLocation::toString).collect(Collectors.toList());
  }

}
