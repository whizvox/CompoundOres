package me.whizvox.compoundores.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import me.whizvox.compoundores.helper.PathHelper;
import net.minecraftforge.fml.ModLoadingContext;

public class CompoundOresConfig {

  public static final CommonConfig COMMON = new CommonConfig(CommentedFileConfig.builder(PathHelper.CONFIG_DIR.resolve("common.toml")).preserveInsertionOrder().build());

  public static void register(ModLoadingContext context) {
  }

}
