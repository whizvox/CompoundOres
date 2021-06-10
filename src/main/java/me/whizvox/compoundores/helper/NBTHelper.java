package me.whizvox.compoundores.helper;

import me.whizvox.compoundores.api.OreComponent;
import me.whizvox.compoundores.api.OreComponentRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class NBTHelper {

  @Nullable
  public static OreComponent getOreComponent(ItemStack stack, String key) {
    CompoundNBT tag = getTag(stack);
    if (tag.contains(key)) {
      ResourceLocation oreCompKey = new ResourceLocation(tag.getString(key));
      return OreComponentRegistry.instance.getValue(oreCompKey);
    }
    return null;
  }

  public static void writeOreComponent(ItemStack stack, String key, @Nullable OreComponent oreComp) {
    if (oreComp != null && !oreComp.isEmpty() && oreComp.getRegistryName() != null) {
      getTag(stack).putString(key, oreComp.getRegistryName().toString());
    }
  }

  public static CompoundNBT getTag(ItemStack stack) {
    if (!stack.hasTag()) {
      stack.setTag(new CompoundNBT());
    }
    return stack.getTag();
  }

}
