package me.whizvox.compoundores.obj;

import me.whizvox.compoundores.api.CompoundOresObjects;
import me.whizvox.compoundores.api.OreComponent;
import me.whizvox.compoundores.api.OreComponentRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class CompoundOreTile extends TileEntity {

  private static final Logger LOGGER = LogManager.getLogger();

  private static final String
    TAG_SECONDARY_ORE = "secondaryOre";

  private OreComponent secondaryComponent;

  public CompoundOreTile() {
    super(CompoundOresObjects.compoundOreTileType);
  }

  public CompoundOreTile(OreComponent secondaryComponent) {
    this();
    setSecondaryComponent(secondaryComponent);
  }

  public void setSecondaryComponent(OreComponent secondaryComponent) {
    this.secondaryComponent = secondaryComponent;
    setChanged();
  }

  @Nullable
  public OreComponent getSecondaryComponent() {
    return secondaryComponent;
  }

  @Override
  public void load(BlockState state, CompoundNBT tag) {
    if (tag.contains(TAG_SECONDARY_ORE)) {
      ResourceLocation key = new ResourceLocation(tag.getString(TAG_SECONDARY_ORE));
      OreComponent loadedOreComp = OreComponentRegistry.instance.getValue(key);
      if (loadedOreComp != null) {
        secondaryComponent = loadedOreComp;
      } else {
        LOGGER.warn("Could not load secondary component from unassociated key: {}", key);
      }
    } else {
      secondaryComponent = null;
    }
    super.load(state, tag);
  }

  @Override
  public CompoundNBT save(CompoundNBT tag) {
    if (secondaryComponent != null) {
      tag.putString(TAG_SECONDARY_ORE, secondaryComponent.getRegistryName().toString());
    }
    return super.save(tag);
  }

}
