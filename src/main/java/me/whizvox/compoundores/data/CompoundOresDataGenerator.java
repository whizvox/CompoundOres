package me.whizvox.compoundores.data;

import me.whizvox.compoundores.data.provider.COBlockTagProvider;
import me.whizvox.compoundores.data.provider.COItemTagProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CompoundOresDataGenerator {

  private CompoundOresDataGenerator() {}

  @SubscribeEvent
  public static void gatherData(GatherDataEvent event) {
    DataGenerator dataGen = event.getGenerator();
    ExistingFileHelper fileHelper = event.getExistingFileHelper();
    if (event.includeClient()) {
      // TODO Generate assets
    }
    if (event.includeServer()) {
      COBlockTagProvider blockTagProvider = new COBlockTagProvider(dataGen, fileHelper);
      dataGen.addProvider(blockTagProvider);
      dataGen.addProvider(new COItemTagProvider(dataGen, blockTagProvider, fileHelper));
    }
  }

}
