package me.whizvox.compoundores.obj;

import me.whizvox.compoundores.api.OreComponent;
import me.whizvox.compoundores.api.OreComponentRegistry;
import me.whizvox.compoundores.helper.NBTHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class CompoundOreBlockItem extends BlockItem {

  public static final String
      TAG_SECONDARY = "secondary";

  public CompoundOreBlockItem(CompoundOreBlock block, Properties properties) {
    super(block, properties);
  }

  @Override
  public ITextComponent getName(ItemStack stack) {
    OreComponent secondary = NBTHelper.getOreComponent(stack, TAG_SECONDARY);
    if (secondary == null) {
      return super.getName(stack);
    }
    return new TranslationTextComponent("item.compoundores.compound_ore.full",
      new TranslationTextComponent(((CompoundOreBlock) getBlock()).getPrimaryComponent().getTranslationKey()),
      new TranslationTextComponent(secondary.getTranslationKey()));
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> text, ITooltipFlag flag) {
    super.appendHoverText(stack, world, text, flag);
    if (flag.isAdvanced()) {
      OreComponent secondary = NBTHelper.getOreComponent(stack, TAG_SECONDARY);
      if (secondary != null && !secondary.isEmpty() && secondary.getRegistryName() != null) {
        text.add(new TranslationTextComponent("tooltip.compoundores.compoundOreItem.componentsListHeader").withStyle(TextFormatting.GRAY));
        text.add(new StringTextComponent("- ").append(new TranslationTextComponent(
            "tooltip.compoundores.compoundOreItem.primary",
            new StringTextComponent(((CompoundOreBlock) getBlock()).getPrimaryComponent().getRegistryName().toString()).withStyle(TextFormatting.DARK_GRAY))
        ));
        text.add(new StringTextComponent("- ").append(new TranslationTextComponent(
            "tooltip.compoundores.compoundOreItem.secondary",
            new StringTextComponent(secondary.getRegistryName().toString()).withStyle(TextFormatting.DARK_GRAY))
        ));
      }
    }
  }

  @Override
  public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
    if (allowdedIn(group)) {
      OreComponentRegistry.instance.getSortedValues().stream()
        // make sure either the entry is not empty nor its block is not the block specified by this item
        .filter(secondary -> !secondary.isEmpty() && !secondary.getBlock().is(((CompoundOreBlock) getBlock()).getPrimaryComponent().getBlock()))
        .forEach(secondary -> {
          ItemStack stack = new ItemStack(this);
          NBTHelper.writeOreComponent(stack, TAG_SECONDARY, secondary);
          items.add(stack);
        }
      );
    }
    // BlockEntityTag
  }

}
