package me.whizvox.compoundores.api;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Objects;

public class OreComponent extends ForgeRegistryEntry<OreComponent> implements Comparable<OreComponent> {

  public static final OreComponent EMPTY = new OreComponent(Blocks.AIR, OreType.NONMETAL, 0x0, 0.0F, 0.0F, 0, 0);

  private Block block;
  private OreType type;
  private int color;
  private float destroySpeed;
  private float blastResistance;
  private int harvestLevel;
  private int spawnWeight;

  private OreComponent(Block block, OreType type, int color, float destroySpeed, float blastResistance, int harvestLevel, int spawnWeight) {
    this.block = block;
    this.type = type;
    this.color = color;
    this.destroySpeed = destroySpeed;
    this.blastResistance = blastResistance;
    this.harvestLevel = harvestLevel;
    this.spawnWeight = spawnWeight;
  }

  public final boolean isEmpty() {
    return block.is(Blocks.AIR) || getRegistryName() == null;
  }

  public Block getBlock() {
    return block;
  }

  public OreType getType() {
    return type;
  }

  public int getColor() {
    return color;
  }

  public float getDestroySpeed() {
    return destroySpeed;
  }

  public float getBlastResistance() {
    return blastResistance;
  }

  public int getHarvestLevel() {
    return harvestLevel;
  }

  public int getSpawnWeight() {
    return spawnWeight;
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
    private Block block;
    private OreType type;
    private int color;
    private float destroySpeed;
    private float blastResistance;
    private int harvestLevel;
    private int spawnWeight;
    public Builder() {
      block = Blocks.AIR;
      type = OreType.METAL;
      color = 0x0;
      destroySpeed = 3.0F;
      blastResistance = 3.0F;
      harvestLevel = 0;
      spawnWeight = 1;
    }
    public Builder block(Block block) {
      this.block = block;
      return this;
    }
    public Builder type(OreType type) {
      this.type = type;
      return this;
    }
    public Builder color(int color) {
      this.color = color;
      return this;
    }
    public Builder destroySpeed(float destroySpeed) {
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
    public Builder spawnWeight(int spawnWeight) {
      this.spawnWeight = spawnWeight;
      return this;
    }
    public OreComponent build() {
      return new OreComponent(block, type, color, destroySpeed, blastResistance, harvestLevel, spawnWeight);
    }
  }

}
