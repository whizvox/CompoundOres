package me.whizvox.compoundores.helper;

import me.whizvox.compoundores.CompoundOres;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class PathHelper {

  private static Path getAndCreate(Path path) {
    return FMLPaths.getOrCreateGameRelativePath(path, CompoundOres.MOD_ID);
  }

  public static final Path
      MOD_ROOT_DIR          = getAndCreate(FMLPaths.GAMEDIR.get().resolve(CompoundOres.MOD_ID)),
      COMPONENTS_DIR        = getAndCreate(MOD_ROOT_DIR.resolve("components")),
      EXPORT_DIR            = getAndCreate(MOD_ROOT_DIR.resolve("export")),
      GROUPS_DIR            = getAndCreate(MOD_ROOT_DIR.resolve("groups")),
      EXPORT_COMPONENTS_DIR = getAndCreate(EXPORT_DIR.resolve("components")),
      EXPORT_GROUPS_DIR     = getAndCreate(EXPORT_DIR.resolve("groups"));

}
