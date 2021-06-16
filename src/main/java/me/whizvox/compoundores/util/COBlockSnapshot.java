package me.whizvox.compoundores.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.Constants;

// forge version keeps inconsistently crashing
public class COBlockSnapshot {

  private IWorld world;
  private BlockPos pos;
  private BlockState state;
  private CompoundNBT nbt;

  public COBlockSnapshot(IWorld world, BlockPos pos, BlockState state, CompoundNBT nbt) {
    this.world = world;
    this.pos = pos;
    this.state = state;
    this.nbt = nbt;
  }

  public IWorld getWorld() {
    return world;
  }

  public BlockPos getPos() {
    return pos;
  }

  public BlockState getState() {
    return state;
  }

  public CompoundNBT getNbt() {
    return nbt;
  }

  public void restore() {
    restore(true);
  }

  public void restore(boolean force) {
    BlockState current = world.getBlockState(pos);
    if (force || current.is(Blocks.AIR)) {
      world.setBlock(pos, state, Constants.BlockFlags.DEFAULT);
      if (nbt != null) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile != null) {
          tile.load(state, nbt);
        }
      }
    }
  }

  public static COBlockSnapshot create(IWorld world, BlockPos pos) {
    BlockState state = world.getBlockState(pos);
    TileEntity tile = world.getBlockEntity(pos);
    CompoundNBT nbt = null;
    if (tile != null) {
      nbt = new CompoundNBT();
      tile.save(nbt);
    }
    return new COBlockSnapshot(world, pos, state, nbt);
  }

}
