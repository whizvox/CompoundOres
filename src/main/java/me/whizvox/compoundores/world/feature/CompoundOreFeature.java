package me.whizvox.compoundores.world.feature;

import me.whizvox.compoundores.api.component.OreComponent;
import me.whizvox.compoundores.api.component.OreComponentRegistry;
import me.whizvox.compoundores.helper.WorldHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.*;

public class CompoundOreFeature extends Feature<NoFeatureConfig> {

  public CompoundOreFeature() {
    super(NoFeatureConfig.CODEC);
  }

  @Override
  public boolean place(ISeedReader world, ChunkGenerator chunkGenerator, Random rand, BlockPos rootPos, NoFeatureConfig config) {
    OreComponentRegistry.getInstance().getComponentFromBlock(world.getBlockState(rootPos).getBlock(), rand);
    OreComponent oreComp = OreComponentRegistry.getInstance().getComponentFromBlock(world.getBlockState(rootPos).getBlock(), rand);
    if (oreComp != null && !oreComp.isEmpty()) {
      // perform a breadth-first search to replace all found ores with compound ores
      Queue<BlockPos> queue = new ArrayDeque<>();
      Set<BlockPos> discovered = new HashSet<>();
      discovered.add(rootPos);
      queue.add(rootPos);
      while (!queue.isEmpty()) {
        BlockPos pos = queue.remove();
        // (Issue #4) veins may extend to chunks that are not loaded yet. don't know how to deal with that, so for now,
        // don't work in chunks that are not loaded
        IChunk chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.EMPTY, false);
        if (chunk != null) {
          if (oreComp.getTarget().accepted(chunk.getBlockState(pos).getBlock())) {
            WorldHelper.placeCompoundOreBlock(world, pos, oreComp, OreComponentRegistry.getInstance().getRandomComponent(oreComp, rand));
          }
          for (Direction direction : Direction.values()) {
            BlockPos nPos = pos.relative(direction);
            if (!discovered.contains(nPos) && oreComp.getTarget().accepted(chunk.getBlockState(nPos).getBlock())) {
              discovered.add(nPos);
              queue.add(nPos);
            }
          }
        }
      }
      return true;
    }
    return false;
  }

}
