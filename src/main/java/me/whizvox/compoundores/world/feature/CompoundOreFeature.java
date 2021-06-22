package me.whizvox.compoundores.world.feature;

import me.whizvox.compoundores.api.CompoundOresObjects;
import me.whizvox.compoundores.api.component.OreComponent;
import me.whizvox.compoundores.api.component.OreComponentRegistry;
import me.whizvox.compoundores.obj.CompoundOreTile;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
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
      BlockState compOreState = CompoundOresObjects.blocks.get(oreComp.getRegistryName()).defaultBlockState();
      // perform a breadth-first search to replace all found ores with compound ores
      Queue<BlockPos> queue = new ArrayDeque<>();
      Set<BlockPos> discovered = new HashSet<>();
      discovered.add(rootPos);
      queue.add(rootPos);
      while (!queue.isEmpty()) {
        BlockPos pos = queue.remove();
        // veins may exist beyond the currently-loaded chunk, so for now, don't attempt to work in unloaded chunks
        IChunk chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.EMPTY, false);
        if (chunk != null) {
          if (oreComp.getTarget().accepted(chunk.getBlockState(pos).getBlock())) {
            place_do(world, rand, pos, compOreState);
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

  private void place_do(ISeedReader world, Random rand, BlockPos pos, BlockState compOreState) {
    world.setBlock(pos, compOreState, 2);
    TileEntity tile = world.getBlockEntity(pos);
    if (tile instanceof CompoundOreTile) {
      ((CompoundOreTile) tile).setSecondaryComponent(OreComponentRegistry.getInstance().getWeighedRandomComponent(rand));
    }
  }

}
