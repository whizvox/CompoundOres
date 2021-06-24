package me.whizvox.compoundores.api.target;

import me.whizvox.compoundores.obj.CompoundOreBlock;
import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    return tag.getValues().stream().filter(block -> !(block instanceof CompoundOreBlock)).collect(Collectors.toSet());
  }

  @Override
  public List<String> serialize() {
    return Collections.singletonList("#" + BlockTags.getAllTags().getIdOrThrow(tag));
  }

}
