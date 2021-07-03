package me.whizvox.compoundores.api.component;

import me.whizvox.compoundores.api.target.BlockTargets;
import me.whizvox.compoundores.api.target.IBlockTarget;
import me.whizvox.compoundores.api.util.NamedMaterial;
import me.whizvox.compoundores.api.util.NamedMaterialColor;
import me.whizvox.compoundores.api.util.NamedSoundType;
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
  private final NamedMaterial material;
  private final NamedMaterialColor materialColor;
  private final ToolType harvestTool;
  private final boolean toolRequired;
  private final NamedSoundType sound;
  private final float hardness;
  private final float resistance;
  private final int harvestLevel;
  private final int weight;

  public OreComponent(IBlockTarget target, int overlayColor, NamedMaterial material, NamedMaterialColor materialColor, ToolType harvestTool, boolean toolRequired, NamedSoundType sound, float hardness, float resistance, int harvestLevel, int weight) {
    this.target = target;
    this.overlayColor = overlayColor;
    this.material = material;
    this.materialColor = materialColor;
    this.harvestTool = harvestTool;
    this.toolRequired = toolRequired;
    this.sound = sound;
    this.hardness = hardness;
    this.resistance = resistance;
    this.harvestLevel = harvestLevel;
    this.weight = weight;
  }

  public final boolean isEmpty() {
    return this == EMPTY || getRegistryName() == null;
  }

  public IBlockTarget getTarget() {
    return target;
  }

  public int getOverlayColor() {
    return overlayColor;
  }

  public NamedMaterial getMaterial() {
    return material;
  }

  @Nullable
  public NamedMaterialColor getMaterialColor() {
    return materialColor;
  }

  @Nullable
  public ToolType getHarvestTool() {
    return harvestTool;
  }

  public boolean isToolRequired() {
    return toolRequired;
  }

  public NamedSoundType getSound() {
    return sound;
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
    private NamedMaterial material;
    private NamedMaterialColor materialColor;
    private ToolType harvestTool;
    private boolean toolRequired;
    private NamedSoundType sound;
    private float destroySpeed;
    private float blastResistance;
    private int harvestLevel;
    private int weight;
    public Builder() {
      target = IBlockTarget.NONE;
      overlayColor = DefaultValues.OVERLAY_COLOR;
      material = DefaultValues.MATERIAL;
      materialColor = DefaultValues.MATERIAL_COLOR;
      harvestTool = DefaultValues.HARVEST_TOOL;
      toolRequired = DefaultValues.TOOL_REQUIRED;
      sound = DefaultValues.SOUND;
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
    public Builder material(NamedMaterial material) {
      this.material = material;
      return this;
    }
    public Builder materialColor(NamedMaterialColor materialColor) {
      this.materialColor = materialColor;
      return this;
    }
    public Builder tool(ToolType harvestTool) {
      this.harvestTool = harvestTool;
      return this;
    }
    public Builder tool(ToolType harvestTool, boolean required) {
      this.harvestTool = harvestTool;
      this.toolRequired = required;
      return this;
    }
    public Builder sound(NamedSoundType sound) {
      this.sound = sound;
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
    public Builder strength(float hardness, float resistance) {
      this.destroySpeed = hardness;
      this.blastResistance = resistance;
      return this;
    }
    public Builder strength(float hardnessAndResistance) {
      return strength(hardnessAndResistance, hardnessAndResistance);
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
      return new OreComponent(target, overlayColor, material, materialColor, harvestTool, toolRequired, sound, destroySpeed, blastResistance, harvestLevel, weight);
    }
  }

  public static final class DefaultValues {
    public static final int OVERLAY_COLOR = 0xFFFFFF;
    public static final NamedMaterial MATERIAL = NamedMaterial.DEFAULT;
    public static final NamedMaterialColor MATERIAL_COLOR = NamedMaterialColor.DEFAULT;
    public static final ToolType HARVEST_TOOL = ToolType.PICKAXE;
    public static final boolean TOOL_REQUIRED = true;
    public static final NamedSoundType SOUND = NamedSoundType.DEFAULT;
    public static final float HARDNESS = 3.0F;
    public static final float RESISTANCE = 3.0F;
    public static final int HARVEST_LEVEL = 0;
    public static final int WEIGHT = 1;
  }

}
