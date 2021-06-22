package me.whizvox.compoundores.api.target;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class CachedTargetWrapper implements IBlockTarget {

  private final IBlockTarget base;
  private final List<String> serialized;

  private Set<Block> resolvedCache;

  public CachedTargetWrapper(IBlockTarget base) {
    this.base = base;
    resolvedCache = null;
    serialized = base.serialize();
  }

  @Override
  public boolean accepted(Block subject) {
    resolve(false);
    return resolvedCache.contains(subject);
  }

  @Override
  public Set<Block> getResolvedTargets() {
    resolve(false);
    return resolvedCache;
  }

  @Override
  public List<String> serialize() {
    return serialized;
  }

  public IBlockTarget getBaseTarget() {
    return base;
  }

  public void resolve(boolean force) {
    if (force || resolvedCache == null) {
      resolvedCache = Collections.unmodifiableSet(base.getResolvedTargets().stream().filter(block -> !block.is(Blocks.AIR)).collect(Collectors.toSet()));
    }
  }

}
