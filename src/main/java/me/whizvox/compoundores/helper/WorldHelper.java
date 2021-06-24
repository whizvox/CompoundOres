package me.whizvox.compoundores.helper;

import me.whizvox.compoundores.api.CompoundOresObjects;
import me.whizvox.compoundores.api.component.OreComponent;
import me.whizvox.compoundores.obj.CompoundOreTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class WorldHelper {

  private static final float PLAYER_REACH_DISTANCE = 5.0F;

  public static BlockRayTraceResult getLookingAt(PlayerEntity player) {
    World world = player.getCommandSenderWorld();
    Vector3d look = player.getLookAngle();
    Vector3d start = new Vector3d(player.getX(), player.getEyeY(), player.getZ());
    Vector3d end = new Vector3d(start.x + look.x * PLAYER_REACH_DISTANCE, start.y + look.y * PLAYER_REACH_DISTANCE, start.z + look.z * PLAYER_REACH_DISTANCE);
    RayTraceContext ctx = new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, player);
    return world.clip(ctx);
  }

  public static void placeCompoundOreBlock(ISeedReader world, BlockPos pos, OreComponent primary, OreComponent secondary) {
    world.setBlock(pos, CompoundOresObjects.blocks.get(primary.getRegistryName()).defaultBlockState(), Constants.BlockFlags.DEFAULT);
    ((CompoundOreTile) world.getBlockEntity(pos)).setSecondaryComponent(secondary);
  }

}
