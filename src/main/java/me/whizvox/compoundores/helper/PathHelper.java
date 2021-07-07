package me.whizvox.compoundores.helper;

import me.whizvox.compoundores.CompoundOres;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class PathHelper {

  private static Path getAndCreate(Path path) {
    return FMLPaths.getOrCreateGameRelativePath(path, CompoundOres.MOD_ID);
  }

  public static final Path
      CONFIG_DIR            = getAndCreate(FMLPaths.CONFIGDIR.get().resolve(CompoundOres.MOD_ID)),
      MOD_ROOT_DIR          = getAndCreate(FMLPaths.GAMEDIR.get().resolve(CompoundOres.MOD_ID)),
      COMPONENTS_DIR        = getAndCreate(CONFIG_DIR.resolve("components")),
      GROUPS_DIR            = getAndCreate(CONFIG_DIR.resolve("groups")),
      EXPORT_DIR            = getAndCreate(MOD_ROOT_DIR.resolve("export")),
      EXPORT_COMPONENTS_DIR = getAndCreate(EXPORT_DIR.resolve("components")),
      EXPORT_GROUPS_DIR     = getAndCreate(EXPORT_DIR.resolve("groups"));

}
