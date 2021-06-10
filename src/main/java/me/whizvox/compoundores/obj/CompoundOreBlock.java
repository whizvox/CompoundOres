package me.whizvox.compoundores.obj;

import me.whizvox.compoundores.api.OreComponent;
import me.whizvox.compoundores.helper.NBTHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CompoundOreBlock extends OreBlock {

  private OreComponent primaryComponent;

  public CompoundOreBlock(Properties properties, OreComponent primaryComponent) {
    super(properties);
    this.primaryComponent = primaryComponent;
  }

  public OreComponent getPrimaryComponent() {
    return primaryComponent;
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Nullable
  @Override
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new CompoundOreTile();
  }

  @Override
  public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(world, pos, state, placer, stack);
    TileEntity tile = world.getBlockEntity(pos);
    if (tile instanceof CompoundOreTile) {
      CompoundOreTile coTile = (CompoundOreTile) tile;
      OreComponent secondary = NBTHelper.getOreComponent(stack, CompoundOreBlockItem.TAG_SECONDARY);
      if (secondary != null && !secondary.isEmpty()) {
        coTile.setSecondaryComponent(secondary);
      }
    }
  }

  @Override
  public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
    List<ItemStack> drops = new ArrayList<>(primaryComponent.getBlock().getDrops(state, builder));
    TileEntity tile = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
    if (tile == null) {
      Vector3d pos = builder.getOptionalParameter(LootParameters.ORIGIN);
      if (pos != null) {
        tile = builder.getLevel().getBlockEntity(new BlockPos(pos));
      }
    }
    if (tile instanceof CompoundOreTile) {
      CompoundOreTile compOre = (CompoundOreTile) tile;
      if (compOre.getSecondaryComponent() != null && !compOre.getSecondaryComponent().isEmpty()) {
        drops.addAll(((CompoundOreTile) tile).getSecondaryComponent().getBlock().getDrops(state, builder));
      }
    }
    return drops;
  }

}
