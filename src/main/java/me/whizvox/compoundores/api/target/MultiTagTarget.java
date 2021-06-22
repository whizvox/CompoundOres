package me.whizvox.compoundores.api.target;

import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class MultiTagTarget implements IBlockTarget {

  public final List<ITag<Block>> tags;

  public MultiTagTarget(List<ITag<Block>> tags) {
    this.tags = Collections.unmodifiableList(tags);
  }

  @Override
  public boolean accepted(Block subject) {
    return tags.stream().anyMatch(tag -> tag.contains(subject));
  }

  @Override
  public Set<Block> getResolvedTargets() {
    Set<Block> resolved = new HashSet<>();
    tags.forEach(tag -> resolved.addAll(tag.getValues()));
    return resolved;
  }

  @Override
  public List<String> serialize() {
    return tags.stream().map(tag -> "#" + BlockTags.getAllTags().getIdOrThrow(tag)).collect(Collectors.toList());
  }

}
