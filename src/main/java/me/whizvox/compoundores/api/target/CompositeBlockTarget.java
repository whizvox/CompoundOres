package me.whizvox.compoundores.api.target;

import net.minecraft.block.Block;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

import java.util.*;

class CompositeBlockTarget implements IBlockTarget {

  private MultiNameTarget simple;
  private MultiTagTarget tagged;

  public CompositeBlockTarget(MultiNameTarget simple, MultiTagTarget tagged) {
    this.simple = simple;
    this.tagged = tagged;
  }

  public CompositeBlockTarget(Set<ResourceLocation> blockNames, List<ITag<Block>> tags) {
    this(new MultiNameTarget(blockNames), new MultiTagTarget(tags));
  }

  @Override
  public boolean accepted(Block subject) {
    return simple.accepted(subject) || tagged.accepted(subject);
  }

  @Override
  public Set<Block> getResolvedTargets() {
    Set<Block> resolved = new HashSet<>();
    resolved.addAll(simple.getResolvedTargets());
    resolved.addAll(tagged.getResolvedTargets());
    return resolved;
  }

  @Override
  public List<String> serialize() {
    List<String> serialized = new ArrayList<>();
    serialized.addAll(simple.serialize());
    serialized.addAll(tagged.serialize());
    return serialized;
  }

}
