package me.whizvox.compoundores.api.component;

import me.whizvox.compoundores.api.target.BlockTargets;
import me.whizvox.compoundores.api.target.IBlockTarget;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Objects;

public class OreComponent extends ForgeRegistryEntry<OreComponent> implements Comparable<OreComponent> {

  public static final OreComponent EMPTY = builder().build();

  private final IBlockTarget target;
  private final int overlayColor;
  private final MaterialColor materialColor;
  private final ToolType harvestTool;
  // TODO final Add a SoundType property
  private final float hardness;
  private final float resistance;
  private final int harvestLevel;
  private final int weight;

  public OreComponent(IBlockTarget target, int overlayColor, MaterialColor materialColor, ToolType harvestTool, float hardness, float resistance, int harvestLevel, int weight) {
    this.target = target;
    this.overlayColor = overlayColor;
    this.materialColor = materialColor;
    this.harvestTool = harvestTool;
    this.hardness = hardness;
    this.resistance = resistance;
    this.harvestLevel = harvestLevel;
    this.weight = weight;
  }

  public final boolean isEmpty() {
    return this == EMPTY || target == IBlockTarget.NONE || getRegistryName() == null;
  }

  public IBlockTarget getTarget() {
    return target;
  }

  public int getOverlayColor() {
    return overlayColor;
  }

  public MaterialColor getMaterialColor() {
    return materialColor;
  }

  @Nullable
  public ToolType getHarvestTool() {
    return harvestTool;
  }

  public float getHardness() {
    return hardness;
  }

  public float getResistance() {
    return resistance;
  }

  public int getHarvestLevel() {
    return harvestLevel;
  }

  public int getWeight() {
    return weight;
  }

  public String getTranslationKey() {
    if (getRegistryName() == null) {
      return "oreComponent.compoundores.unspecified";
    }
    return "oreComponent." + getRegistryName().getNamespace() + "." + getRegistryName().getPath();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !obj.getClass().isAssignableFrom(OreComponent.class)) {
      return false;
    }
    return Objects.equals(getRegistryName(), ((OreComponent) obj).getRegistryName());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getRegistryName());
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public int compareTo(@Nonnull OreComponent obj) {
    return Objects.compare(this.getRegistryName(), obj.getRegistryName(), Comparator.naturalOrder());
  }

  public static class Builder {
    private IBlockTarget target;
    private int overlayColor;
    private MaterialColor materialColor;
    private ToolType harvestTool;
    private float destroySpeed;
    private float blastResistance;
    private int harvestLevel;
    private int weight;
    public Builder() {
      target = IBlockTarget.NONE;
      overlayColor = DefaultValues.OVERLAY_COLOR;
      materialColor = DefaultValues.MATERIAL_COLOR;
      harvestTool = DefaultValues.HARVEST_TOOL;
      destroySpeed = DefaultValues.HARDNESS;
      blastResistance = DefaultValues.RESISTANCE;
      harvestLevel = DefaultValues.HARVEST_LEVEL;
      weight = DefaultValues.WEIGHT;
    }
    public Builder target(IBlockTarget target) {
      this.target = target;
      return this;
    }
    public Builder target(Object... targets) {
      return target(BlockTargets.create(targets));
    }
    public Builder overlayColor(int overlayColor) {
      this.overlayColor = overlayColor;
      return this;
    }
    public Builder materialColor(MaterialColor materialColor) {
      this.materialColor = materialColor;
      return this;
    }
    public Builder tool(ToolType harvestTool) {
      this.harvestTool = harvestTool;
      return this;
    }
    public Builder hardness(float hardness) {
      this.destroySpeed = hardness;
      return this;
    }
    public Builder resistance(float resistance) {
      this.blastResistance = resistance;
      return this;
    }
    public Builder harvestLevel(int harvestLevel) {
      this.harvestLevel = harvestLevel;
      return this;
    }
    public Builder weight(int weight) {
      this.weight = weight;
      return this;
    }
    public OreComponent build() {
      return new OreComponent(target, overlayColor, materialColor, harvestTool, destroySpeed, blastResistance, harvestLevel, weight);
    }
  }

  public static final class DefaultValues {
    public static final int OVERLAY_COLOR = 0xFFFFFF;
    public static final MaterialColor MATERIAL_COLOR = MaterialColor.STONE;
    public static final ToolType HARVEST_TOOL = ToolType.PICKAXE;
    public static final float HARDNESS = 3.0F;
    public static final float RESISTANCE = 3.0F;
    public static final int HARVEST_LEVEL = 0;
    public static final int WEIGHT = 1;
  }

}
