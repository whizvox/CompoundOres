package me.whizvox.compoundores.data.provider;

import me.whizvox.compoundores.CompoundOres;
import me.whizvox.compoundores.api.CompoundOresObjects;
import net.minecraft.block.Block;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class COBlockTagProvider extends BlockTagsProvider {

  public COBlockTagProvider(DataGenerator dataGenerator, @Nullable ExistingFileHelper existingFileHelper) {
    super(dataGenerator, CompoundOres.MOD_ID, existingFileHelper);
  }

  private static final List<Pair<ResourceLocation, ResourceLocation>> compOreBlockTags;

  public static final Set<ResourceLocation> uniqueBlockTags;

  static {
    final String[] flatForgeOres = {
      "coal",
      "iron",
      "lapis",
      "redstone",
      "diamond",
      "emerald",
      "ancient_debris",
      "copper",
      "tin",
      "aluminum",
      "osmium",
      "uranium",
      "nickel",
      "zinc",
      "silver",
      "lead",
      "bismuth",
      "sapphire",
      "ruby",
      "iridium",
      "platinum",
      "sulfur",
      "fluorite",
      "uraninite_poor",
      "uraninite",
      "uraninite_dense"
    };
    final String[] otherForgeOres = {
      "nether_quartz",  "quartz",
      "yellorite",      "uranium",
    };
    final String[] otherTags = {
      "gold",         "minecraft:gold_ores",
      "nether_gold",  "minecraft:gold_ores"
    };

    List<Pair<ResourceLocation, ResourceLocation>> bindings = new ArrayList<>();
    for (String s : flatForgeOres) {
      bindings.add(Pair.of(new ResourceLocation(CompoundOres.MOD_ID, s), new ResourceLocation("forge", "ores/" + s)));
    }
    for (int i = 0; i < otherForgeOres.length; i += 2) {
      bindings.add(Pair.of(new ResourceLocation(CompoundOres.MOD_ID, otherForgeOres[i]), new ResourceLocation("forge", "ores/" + otherForgeOres[i + 1])));
    }
    for (int i = 0; i < otherTags.length; i += 2) {
      bindings.add(Pair.of(new ResourceLocation(CompoundOres.MOD_ID, otherTags[i]), new ResourceLocation(otherTags[i + 1])));
    }
    compOreBlockTags = Collections.unmodifiableList(bindings);
    uniqueBlockTags = bindings.stream().map(Pair::getRight).collect(Collectors.toSet());
  }

  @Override
  protected void addTags() {
    // compoundores:compound_ore
    Builder<Block> compOresTag = tag(CompoundOresObjects.COMPOUND_ORES_BLOCK_TAG);
    CompoundOresObjects.blocks.forEach((compKey, block) -> compOresTag.add(block));

    // bind all in compoundores:compound_ore tag to forge:ores
    tag(Tags.Blocks.ORES).addTag(CompoundOresObjects.COMPOUND_ORES_BLOCK_TAG);
    // forge:ores and forge:ores/{name}
    compOreBlockTags.forEach(entry -> tag(BlockTags.createOptional(entry.getRight())).add(CompoundOresObjects.blocks.get(entry.getLeft())));
  }

}
