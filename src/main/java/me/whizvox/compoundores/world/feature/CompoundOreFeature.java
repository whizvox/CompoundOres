package me.whizvox.compoundores.world.feature;

import me.whizvox.compoundores.api.CompoundOresObjects;
import me.whizvox.compoundores.api.OreComponent;
import me.whizvox.compoundores.api.OreComponentRegistry;
import me.whizvox.compoundores.obj.CompoundOreTile;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class CompoundOreFeature extends Feature<NoFeatureConfig> {

  private static final Logger LOGGER = LogManager.getLogger();

  public CompoundOreFeature() {
    super(NoFeatureConfig.CODEC);
  }

  @Override
  public boolean place(ISeedReader world, ChunkGenerator chunkGenerator, Random rand, BlockPos rootPos, NoFeatureConfig config) {
    OreComponent oreComp = OreComponentRegistry.instance.getComponentFromBlock(world.getBlockState(rootPos).getBlock());
    if (oreComp != null && !oreComp.isEmpty()) {
      LOGGER.debug("COMPOUND ORE VEIN GENERATED AT {}", rootPos);
      BlockState compOreState = CompoundOresObjects.blocks.get(oreComp.getRegistryName()).defaultBlockState();
      // perform a breadth-first search to replace all found ores with compound ores
      /*Queue<BlockPos> queue = new ArrayDeque<>();
      Set<BlockPos> discovered = new HashSet<>();
      discovered.add(rootPos);
      queue.add(rootPos);
      while (!queue.isEmpty()) {
        BlockPos pos = queue.remove();
        if (world.getBlockState(pos).is(oreComp.getBlock())) {
          place_do(world, rand, pos, compOreState);
        }
        for (Direction direction : Direction.values()) {
          BlockPos nPos = pos.relative(direction);
          if (!discovered.contains(nPos)) {
            discovered.add(nPos);
            queue.add(nPos);
          }
        }
      }*/
      place_do(world, rand, rootPos, compOreState);
      return true;
    }
    return false;
  }

  private void place_do(ISeedReader world, Random rand, BlockPos pos, BlockState compOreState) {
    world.setBlock(pos, compOreState, 2);
    TileEntity tile = world.getBlockEntity(pos);
    if (tile instanceof CompoundOreTile) {
      ((CompoundOreTile) tile).setSecondaryComponent(OreComponentRegistry.instance.getRandomComponent(rand));
    }
  }

}
