package me.whizvox.compoundores.api.target;

import net.minecraft.block.Block;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface IBlockTarget {

  boolean accepted(Block subject);

  Set<Block> getResolvedTargets();

  List<String> serialize();

  default Block getResolvedTarget() {
    return getResolvedTargets().stream().findAny().orElse(null);
  }

  IBlockTarget NONE = new IBlockTarget() {
    @Override
    public boolean accepted(Block subject) {
      return false;
    }
    @Override
    public Set<Block> getResolvedTargets() {
      return Collections.emptySet();
    }
    @Override
    public List<String> serialize() {
      return Collections.emptyList();
    }
  };

}
