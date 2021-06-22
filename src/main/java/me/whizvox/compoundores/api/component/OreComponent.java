package me.whizvox.compoundores.api.component;

import me.whizvox.compoundores.api.target.BlockTargets;
import me.whizvox.compoundores.api.target.IBlockTarget;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Objects;

public class OreComponent extends ForgeRegistryEntry<OreComponent> implements Comparable<OreComponent> {

  public static final OreComponent EMPTY = new OreComponent(IBlockTarget.NONE, 0xFFFFFF, 0.0F, 0.0F, 0, 0);

  private IBlockTarget target;
  private int color;
  private float hardness;
  private float resistance;
  private int harvestLevel;
  private int weight;

  private OreComponent(IBlockTarget target, int color, float hardness, float resistance, int harvestLevel, int weight) {
    this.target = target;
    this.color = color;
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

  public int getColor() {
    return color;
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
    if (obj == null || obj.getClass().isAssignableFrom(OreComponent.class)) {
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
    private int color;
    private float destroySpeed;
    private float blastResistance;
    private int harvestLevel;
    private int spawnWeight;
    public Builder() {
      target = IBlockTarget.NONE;
      color = 0xFFFFFF; // white
      destroySpeed = 3.0F;
      blastResistance = 3.0F;
      harvestLevel = 0;
      spawnWeight = 1;
    }
    public Builder target(IBlockTarget target) {
      this.target = target;
      return this;
    }
    public Builder target(Object... targets) {
      return target(BlockTargets.create(targets));
    }
    public Builder color(int color) {
      this.color = color;
      return this;
    }
    public Builder hardness(float destroySpeed) {
      this.destroySpeed = destroySpeed;
      return this;
    }
    public Builder resistance(float blastResistance) {
      this.blastResistance = blastResistance;
      return this;
    }
    public Builder harvestLevel(int harvestLevel) {
      this.harvestLevel = harvestLevel;
      return this;
    }
    public Builder weight(int spawnWeight) {
      this.spawnWeight = spawnWeight;
      return this;
    }
    public OreComponent build() {
      return new OreComponent(target, color, destroySpeed, blastResistance, harvestLevel, spawnWeight);
    }
  }

}
