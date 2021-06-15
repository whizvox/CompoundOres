package me.whizvox.compoundores.obj;

import me.whizvox.compoundores.api.CompoundOresObjects;
import me.whizvox.compoundores.api.OreComponent;
import me.whizvox.compoundores.api.OreComponentRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
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

  @Nullable
  @Override
  public SUpdateTileEntityPacket getUpdatePacket() {
    CompoundNBT tag = new CompoundNBT();
    save(tag);
    return new SUpdateTileEntityPacket(getBlockPos(), 42, tag);
  }

  @Override
  public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
    BlockState state = level.getBlockState(worldPosition);
    load(state, pkt.getTag());
  }

  @Override
  public CompoundNBT getUpdateTag() {
    CompoundNBT tag = new CompoundNBT();
    save(tag);
    return tag;
  }

  @Override
  public void handleUpdateTag(BlockState state, CompoundNBT tag) {
    load(state, tag);
  }

}
