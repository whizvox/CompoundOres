package me.whizvox.compoundores.helper;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.*;

public class RenderHelper {

  // adapted from TheGreyGhost's MinecraftByExample
  // https://github.com/TheGreyGhost/MinecraftByExample/blob/e9862e606f6306463fccde5e3ebe576ea88f0745/src/main/java/minecraftbyexample/mbe21_tileentityrenderer/RenderQuads.java

  private static final Vector3f
    XDIR_NORTH = new Vector3f(1, 0, 0),
    YDIR_NORTH = new Vector3f(0, -1, 0),
    CENTER_NORTH = new Vector3f(0.5F, 0.5F, -0.001F),
    XDIR_SOUTH = new Vector3f(-1, 0, 0),
    YDIR_SOUTH = new Vector3f(0, -1, 0),
    CENTER_SOUTH = new Vector3f(0.5F, 0.5F, 1.001F),
    XDIR_EAST = new Vector3f(0, 0, 1),
    YDIR_EAST = new Vector3f(0, -1, 0),
    CENTER_EAST = new Vector3f(1.001F, 0.5F, 0.5F),
    XDIR_WEST = new Vector3f(0, 0, -1),
    YDIR_WEST = new Vector3f(0, -1, 0),
    CENTER_WEST = new Vector3f(-0.001F, 0.5F, 0.5F),
    XDIR_UP = new Vector3f(-1, 0, 0),
    YDIR_UP = new Vector3f(0, 0, 1),
    CENTER_UP = new Vector3f(0.5F, 1.001F, 0.5F),
    XDIR_DOWN = new Vector3f(-1, 0, 0),
    YDIR_DOWN = new Vector3f(0, 0, -1),
    CENTER_DOWN = new Vector3f(0.5F, -0.001F, 0.5F);

  public static void drawCubeQuads(ResourceLocation texture, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer,
                                    int color, int combinedLight) {

    matrixStack.pushPose();
    IVertexBuilder vertexBuilderBlockQuads = renderBuffer.getBuffer(RenderType.entityCutout(texture));
    // other typical RenderTypes used by TER are:
    // getEntityCutout, getBeaconBeam (which has translucency),

    Matrix4f matrixPos = matrixStack.last().pose();     // retrieves the current transformation matrix
    Matrix3f matrixNormal = matrixStack.last().normal();  // retrieves the current transformation matrix for the normal vector

    // we use the whole texture
    Vector2f bottomLeftUV = new Vector2f(0.0F, 1.0F);
    float UVwidth = 1.0F;
    float UVheight = 1.0F;

    // all faces have the same height and width
    final float WIDTH = 1.0F;
    final float HEIGHT = 1.0F;

    final Vector3d EAST_FACE_MIDPOINT = new Vector3d(1.0001, 0.5, 0.5);
    final Vector3d WEST_FACE_MIDPOINT = new Vector3d(-0.0001, 0.5, 0.5);
    final Vector3d NORTH_FACE_MIDPOINT = new Vector3d(0.5, 0.5, -0.0001);
    final Vector3d SOUTH_FACE_MIDPOINT = new Vector3d(0.5, 0.5, 1.0001);
    final Vector3d UP_FACE_MIDPOINT = new Vector3d(0.5, 1.0001, 0.5);
    final Vector3d DOWN_FACE_MIDPOINT = new Vector3d(0.5, -0.0001, 0.5);

    addFace(Direction.EAST, matrixPos, matrixNormal, vertexBuilderBlockQuads,
      color, /*EAST_FACE_MIDPOINT, */WIDTH, HEIGHT, bottomLeftUV, UVwidth, UVheight, combinedLight);
    addFace(Direction.WEST, matrixPos, matrixNormal, vertexBuilderBlockQuads,
      color, /*WEST_FACE_MIDPOINT, */WIDTH, HEIGHT, bottomLeftUV, UVwidth, UVheight, combinedLight);
    addFace(Direction.NORTH, matrixPos, matrixNormal, vertexBuilderBlockQuads,
      color, /*NORTH_FACE_MIDPOINT, */WIDTH, HEIGHT, bottomLeftUV, UVwidth, UVheight, combinedLight);
    addFace(Direction.SOUTH, matrixPos, matrixNormal, vertexBuilderBlockQuads,
      color, /*SOUTH_FACE_MIDPOINT, */WIDTH, HEIGHT, bottomLeftUV, UVwidth, UVheight, combinedLight);
    addFace(Direction.UP, matrixPos, matrixNormal, vertexBuilderBlockQuads,
      color, /*UP_FACE_MIDPOINT, */WIDTH, HEIGHT, bottomLeftUV, UVwidth, UVheight, combinedLight);
    addFace(Direction.DOWN, matrixPos, matrixNormal, vertexBuilderBlockQuads,
      color, /*DOWN_FACE_MIDPOINT, */WIDTH, HEIGHT, bottomLeftUV, UVwidth, UVheight, combinedLight);
    matrixStack.popPose();
  }

  public static void addFace(Direction whichFace,
                              Matrix4f matrixPos, Matrix3f matrixNormal, IVertexBuilder renderBuffer,
                              int color, float width, float height,
                              Vector2f bottomLeftUV, float texUwidth, float texVheight,
                              int lightmapValue) {
    // the Direction class has a bunch of methods which can help you rotate quads
    //  I've written the calculations out long hand, and based them on a centre position, to make it clearer what
    //   is going on.
    // Beware that the Direction class is based on which direction the face is pointing, which is opposite to
    //   the direction that the viewer is facing when looking at the face.
    // Eg when drawing the NORTH face, the face points north, but when we're looking at the face, we are facing south,
    //   so that the bottom left corner is the eastern-most, not the western-most!


    // calculate the bottom left, bottom right, top right, top left vertices from the VIEWER's point of view (not the
    //  face's point of view)

    Vector3f leftToRightDirection, bottomToTopDirection, centerPos;

    switch (whichFace) {
      case NORTH: {
        leftToRightDirection = XDIR_NORTH.copy();
        bottomToTopDirection = YDIR_NORTH.copy();
        centerPos = CENTER_NORTH;
        break;
      }
      case SOUTH: {
        leftToRightDirection = XDIR_SOUTH.copy();
        bottomToTopDirection = YDIR_SOUTH.copy();
        centerPos = CENTER_SOUTH;
        break;
      }
      case EAST: {
        leftToRightDirection = XDIR_EAST.copy();
        bottomToTopDirection = YDIR_EAST.copy();
        centerPos = CENTER_EAST;
        break;
      }
      case WEST: {
        leftToRightDirection = XDIR_WEST.copy();
        bottomToTopDirection = YDIR_WEST.copy();
        centerPos = CENTER_WEST;
        break;
      }
      case UP: {
        leftToRightDirection = XDIR_UP.copy();
        bottomToTopDirection = YDIR_UP.copy();
        centerPos = CENTER_UP;
        break;
      }
      default: {  // DOWN
        leftToRightDirection = XDIR_DOWN.copy();
        bottomToTopDirection = YDIR_DOWN.copy();
        centerPos = CENTER_DOWN;
        break;
      }
    }
    leftToRightDirection.mul(0.5F * width);  // convert to half width
    bottomToTopDirection.mul(0.5F * height);  // convert to half height

    // calculate the four vertices based on the centre of the face

    Vector3f bottomLeftPos = centerPos.copy();
    bottomLeftPos.sub(leftToRightDirection);
    bottomLeftPos.sub(bottomToTopDirection);

    Vector3f bottomRightPos = centerPos.copy();
    bottomRightPos.add(leftToRightDirection);
    bottomRightPos.sub(bottomToTopDirection);

    Vector3f topRightPos = centerPos.copy();
    topRightPos.add(leftToRightDirection);
    topRightPos.add(bottomToTopDirection);

    Vector3f topLeftPos = centerPos.copy();
    topLeftPos.sub(leftToRightDirection);
    topLeftPos.add(bottomToTopDirection);

    // texture coordinates are "upside down" relative to the face
    // eg bottom left = [U min, V max]
    /*Vector2f bottomLeftUVpos = new Vector2f(bottomLeftUV.x, bottomLeftUV.y);
    Vector2f bottomRightUVpos = new Vector2f(bottomLeftUV.x + texUwidth, bottomLeftUV.y);
    Vector2f topLeftUVpos = new Vector2f(bottomLeftUV.x + texUwidth, bottomLeftUV.y + texVheight);
    Vector2f topRightUVpos = new Vector2f(bottomLeftUV.x, bottomLeftUV.y + texVheight);*/
    Vector2f bottomRightUVpos = new Vector2f(bottomLeftUV.x, bottomLeftUV.y);
    Vector2f bottomLeftUVpos = new Vector2f(bottomLeftUV.x + texUwidth, bottomLeftUV.y);
    Vector2f topRightUVpos = new Vector2f(bottomLeftUV.x + texUwidth, bottomLeftUV.y + texVheight);
    Vector2f topLeftUVpos = new Vector2f(bottomLeftUV.x, bottomLeftUV.y + texVheight);

    Vector3f normalVector = whichFace.step();  // gives us the normal to the face

    addQuad(matrixPos, matrixNormal, renderBuffer,
      bottomLeftPos, bottomRightPos, topRightPos, topLeftPos,
      bottomLeftUVpos, bottomRightUVpos, topLeftUVpos, topRightUVpos,
      normalVector, color, lightmapValue);
  }

  /**
   * Add a quad.
   * The vertices are added in anti-clockwise order from the VIEWER's  point of view, i.e.
   * bottom left; bottom right, top right, top left
   * If you add the vertices in clockwise order, the quad will face in the opposite direction; i.e. the viewer will be
   *   looking at the back face, which is usually culled (not visible)
   * See
   * http://greyminecraftcoder.blogspot.com/2014/12/the-tessellator-and-worldrenderer-18.html
   * http://greyminecraftcoder.blogspot.com/2014/12/block-models-texturing-quads-faces.html
   */
  private static void addQuad(Matrix4f matrixPos, Matrix3f matrixNormal, IVertexBuilder renderBuffer,
                              Vector3f blpos, Vector3f brpos, Vector3f trpos, Vector3f tlpos,
                              Vector2f blUVpos, Vector2f brUVpos, Vector2f trUVpos, Vector2f tlUVpos,
                              Vector3f normalVector, int color, int lightmapValue) {
    addQuadVertex(matrixPos, matrixNormal, renderBuffer, blpos, blUVpos, normalVector, color, lightmapValue);
    addQuadVertex(matrixPos, matrixNormal, renderBuffer, brpos, brUVpos, normalVector, color, lightmapValue);
    addQuadVertex(matrixPos, matrixNormal, renderBuffer, trpos, trUVpos, normalVector, color, lightmapValue);
    addQuadVertex(matrixPos, matrixNormal, renderBuffer, tlpos, tlUVpos, normalVector, color, lightmapValue);
  }

  // suitable for vertexbuilders using the DefaultVertexFormats.ENTITY format
  private static void addQuadVertex(Matrix4f matrixPos, Matrix3f matrixNormal, IVertexBuilder renderBuffer,
                                    Vector3f pos, Vector2f texUV,
                                    Vector3f normalVector, int color, int lightmapValue) {
    renderBuffer.vertex(matrixPos, pos.x(), pos.y(), pos.z()) // position coordinate
      .color((color >> 24) & 0xFF, (color >> 16) & 0xFF, (color >> 8) & 0xFF, /*color & 0xFF*/255)        // color
      .uv(texUV.x, texUV.y)                     // texel coordinate
      .overlayCoords(OverlayTexture.NO_OVERLAY)  // only relevant for rendering Entities (Living)
      .uv2(lightmapValue)             // lightmap with full brightness
      .normal(matrixNormal, normalVector.x(), normalVector.y(), normalVector.z())
      .endVertex();
  }

}
