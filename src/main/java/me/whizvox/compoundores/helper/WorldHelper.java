package me.whizvox.compoundores.helper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

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

}
