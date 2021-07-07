package me.whizvox.compoundores.data;

import me.whizvox.compoundores.data.provider.COBlockStateProvider;
import me.whizvox.compoundores.data.provider.COBlockTagProvider;
import me.whizvox.compoundores.data.provider.COItemTagProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

public class CompoundOresDataGenerator {

  private CompoundOresDataGenerator() {}

  public static void gatherData(GatherDataEvent event) {
    DataGenerator dataGen = event.getGenerator();
    ExistingFileHelper fileHelper = event.getExistingFileHelper();
    if (event.includeClient()) {
      dataGen.addProvider(new COBlockStateProvider(dataGen, fileHelper));
    }
    if (event.includeServer()) {
      COBlockTagProvider blockTagProvider = new COBlockTagProvider(dataGen, fileHelper);
      dataGen.addProvider(blockTagProvider);
      dataGen.addProvider(new COItemTagProvider(dataGen, blockTagProvider, fileHelper));
    }
  }

}
