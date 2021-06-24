package me.whizvox.compoundores.obj;

import me.whizvox.compoundores.api.CompoundOresObjects;
import me.whizvox.compoundores.api.component.OreComponent;
import me.whizvox.compoundores.helper.NBTHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
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
    List<ItemStack> drops = new ArrayList<>(primaryComponent.getTarget().getResolvedTarget().getDrops(state, builder));
    TileEntity tile = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
    if (tile == null) {
      Vector3d pos = builder.getOptionalParameter(LootParameters.ORIGIN);
      if (pos != null) {
        tile = builder.getLevel().getBlockEntity(new BlockPos(pos));
      }
    }
    if (tile instanceof CompoundOreTile) {
      OreComponent secondary = ((CompoundOreTile) tile).getSecondaryComponent();
      if (secondary != null && !secondary.isEmpty() && !secondary.getTarget().getResolvedTargets().isEmpty()) {
        drops.addAll(secondary.getTarget().getResolvedTarget().getDrops(state, builder));
      }
    }
    return drops;
  }

  @Override
  public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
    TileEntity tile = world.getBlockEntity(pos);
    if (tile instanceof CompoundOreTile) {
      ItemStack stack = new ItemStack(CompoundOresObjects.blockItems.get(primaryComponent.getRegistryName()));
      NBTHelper.writeOreComponent(stack, CompoundOreBlockItem.TAG_SECONDARY, ((CompoundOreTile) tile).getSecondaryComponent());
      return stack;
    }
    return super.getPickBlock(state, target, world, pos, player);
  }

  @Override
  public int getExpDrop(BlockState state, IWorldReader world, BlockPos pos, int fortune, int silktouch) {
    TileEntity tile = world.getBlockEntity(pos);
    int baseXpDrop = primaryComponent.getTarget().getResolvedTarget().getExpDrop(state, world, pos, fortune, silktouch);
    if (tile instanceof CompoundOreTile) {
      OreComponent secondary = ((CompoundOreTile) tile).getSecondaryComponent();
      if (secondary != null && !secondary.isEmpty() && !secondary.getTarget().getResolvedTargets().isEmpty()) {
        return baseXpDrop + secondary.getTarget().getResolvedTarget().getExpDrop(state, world, pos, fortune, silktouch);
      }
    }
    return baseXpDrop;
  }

}
