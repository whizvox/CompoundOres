package me.whizvox.compoundores.api.target;

import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class TagTarget implements IBlockTarget {

  public final ITag<Block> tag;

  public TagTarget(ITag<Block> tag) {
    this.tag = tag;
  }

  @Override
  public boolean accepted(Block subject) {
    return tag.contains(subject);
  }

  @Override
  public Set<Block> getResolvedTargets() {
    return new HashSet<>(tag.getValues());
  }

  @Override
  public List<String> serialize() {
    return Collections.singletonList("#" + BlockTags.getAllTags().getIdOrThrow(tag));
  }

}
