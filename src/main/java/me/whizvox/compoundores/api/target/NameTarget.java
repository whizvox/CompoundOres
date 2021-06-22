package me.whizvox.compoundores.api.target;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Set;

class NameTarget implements IBlockTarget {

  private final ResourceLocation blockName;

  public NameTarget(ResourceLocation blockName) {
    this.blockName = blockName;
  }

  @Override
  public boolean accepted(Block subject) {
    return blockName.equals(subject.getRegistryName());
  }

  @Override
  public Set<Block> getResolvedTargets() {
    Block resolved = ForgeRegistries.BLOCKS.getValue(blockName);
    if (resolved.is(Blocks.AIR)) {
      return Collections.emptySet();
    }
    return Collections.singleton(resolved);
  }

  @Override
  public Block getResolvedTarget() {
    return ForgeRegistries.BLOCKS.getValue(blockName);
  }

  @Override
  public List<String> serialize() {
    return Collections.singletonList(blockName.toString());
  }

}
