package me.whizvox.compoundores.data.provider;

import me.whizvox.compoundores.CompoundOres;
import me.whizvox.compoundores.api.CompoundOresObjects;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class COItemTagProvider extends ItemTagsProvider {

  public COItemTagProvider(DataGenerator dataGenerator, BlockTagsProvider blockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
    super(dataGenerator, blockTagsProvider, CompoundOres.MOD_ID, existingFileHelper);
  }

  @Override
  protected void addTags() {
    Builder<Item> compOresTag = tag(CompoundOresObjects.COMPOUND_ORES_ITEM_TAG);
    CompoundOresObjects.blockItems.forEach((compKey, item) -> compOresTag.add(item));
    tag(Tags.Items.ORES).addTag(CompoundOresObjects.COMPOUND_ORES_ITEM_TAG);
    COBlockTagProvider.uniqueBlockTags.forEach(tagName -> copy(BlockTags.createOptional(tagName), ItemTags.createOptional(tagName)));
  }

}
