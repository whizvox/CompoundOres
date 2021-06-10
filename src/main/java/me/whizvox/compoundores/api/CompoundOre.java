package me.whizvox.compoundores.api;

import net.minecraft.block.Block;

import javax.annotation.Nonnull;
import java.util.Objects;

public class CompoundOre {

  private OreComponent primary;
  private OreComponent secondary;

  public CompoundOre(OreComponent primary, OreComponent secondary) {
    this.primary = primary;
    this.secondary = secondary;
  }

  public OreComponent getPrimary() {
    return primary;
  }

  public OreComponent getSecondary() {
    return secondary;
  }

  public boolean eitherBlockIs(@Nonnull Block other) {
    return primary.getBlock().is(other) || secondary.getBlock().is(other);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    CompoundOre that = (CompoundOre) obj;
    return primary.equals(that.primary) && secondary.equals(that.secondary);
  }

  @Override
  public int hashCode() {
    return Objects.hash(primary.getRegistryName(), secondary.getRegistryName());
  }

}
