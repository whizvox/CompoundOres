package me.whizvox.compoundores.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.whizvox.compoundores.api.component.OreComponent;
import me.whizvox.compoundores.helper.RenderHelper;
import me.whizvox.compoundores.obj.CompoundOreTile;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;

import java.util.HashMap;
import java.util.Map;

public class CompoundOreTileRenderer extends TileEntityRenderer<CompoundOreTile> {

  public CompoundOreTileRenderer(TileEntityRendererDispatcher dispatcher) {
    super(dispatcher);
  }

  @Override
  public void render(CompoundOreTile tile, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
    final OreComponent secondary = tile.getSecondaryComponent();
    if (secondary != null && !secondary.isEmpty()) {
      stack.pushPose();
      IVertexBuilder vb = buffer.getBuffer(RenderType.entityCutout(getTexture(secondary)));
      for (Direction dir : Direction.values()) {
        BlockPos pos = tile.getBlockPos().relative(dir);
        if (!tile.getLevel().getBlockState(pos).isSolidRender(tile.getLevel(), pos)) {
          int brightness = tile.getLevel().getMaxLocalRawBrightness(pos);
          // renderer expecting RGBA, so shift left and add FF byte to end
          RenderHelper.addFace(dir, stack.last().pose(), stack.last().normal(), vb, (secondary.getOverlayColor() << 8) | 0xFF, 1.0F, 1.0F, new Vector2f(0.0F, 1.0F), 1.0F, 1.0F, LightTexture.pack(brightness, brightness));
        }
      }
      stack.popPose();
    }
  }

  private static Map<ResourceLocation, ResourceLocation> textureMapCache = new HashMap<>();

  private static ResourceLocation getTexture(OreComponent oreComp) {
    return textureMapCache.computeIfAbsent(
      oreComp.getRegistryName(),
      name -> new ResourceLocation(oreComp.getRegistryName().getNamespace(), "textures/component_overlays/" + oreComp.getRegistryName().getPath() + ".png")
    );
  }

}
