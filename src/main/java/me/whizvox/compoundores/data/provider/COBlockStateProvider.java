package me.whizvox.compoundores.data.provider;

import me.whizvox.compoundores.CompoundOres;
import me.whizvox.compoundores.api.CompoundOresObjects;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.*;

import static me.whizvox.compoundores.CompoundOres.LOGGER;
import static me.whizvox.compoundores.helper.Markers.DATAGEN;

public class COBlockStateProvider extends BlockStateProvider {

  public COBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
    super(gen, CompoundOres.MOD_ID, exFileHelper);
  }

  // components whose base texture should be the vanilla texture
  private static final Set<String> USE_VANILLA = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
    "ancient_debris",
    "glowstone"
  )));

  @Override
  protected void registerStatesAndModels() {
    CompoundOresObjects.blocks.forEach((compKey, block) -> {
      if (USE_VANILLA.contains(compKey.getPath())) {
        ResourceLocation parent = new ResourceLocation("minecraft", "block/" + compKey.getPath());
        if (models().existingFileHelper.exists(parent, ResourcePackType.CLIENT_RESOURCES, ".json", "models")) {
          ModelFile file = models().withExistingParent("block/" + block.getRegistryName().getPath(), parent);
          simpleBlock(block, file);
          simpleBlockItem(block, file);
        } else {
          LOGGER.warn(DATAGEN, "Parent model does not exist: {}.json", parent);
        }
      } else {
        ResourceLocation texture = modLoc("block/compound_ore_base/" + compKey.getPath());
        if (models().existingFileHelper.exists(texture, ResourcePackType.CLIENT_RESOURCES, ".png", "textures")) {
          ModelFile file = models().cubeAll("block/" + block.getRegistryName().getPath(), texture);
          simpleBlock(block, file);
          simpleBlockItem(block, file);
        } else {
          LOGGER.warn(DATAGEN, "Texture does not exist: {}.png", texture);
        }
      }
    });
  }

}
