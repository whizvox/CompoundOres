package me.whizvox.compoundores.util;

import com.google.gson.*;

import java.lang.reflect.Type;

public class OreDistributionJsonSerializer implements JsonSerializer<OreDistribution> {

  private OreDistributionJsonSerializer() {}

  @Override
  public JsonElement serialize(OreDistribution src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject root = new JsonObject();

    root.addProperty("dimension", src.getDimension().toString());

    JsonArray scanned = new JsonArray();
    src.getChunksScanned().forEach(cPos -> scanned.add(cPos.x + "," + cPos.z));
    root.add("scanned", scanned);

    JsonArray skipped = new JsonArray();
    src.getChunksSkipped().forEach(cPos -> skipped.add(cPos.x + "," + cPos.z));
    root.add("skipped", skipped);

    root.addProperty("chunks", src.getTotalChunks());
    root.addProperty("total", src.getTotalScanned());
    root.addProperty("matching", src.getTotalMatching());

    JsonObject raw = new JsonObject();
    src.getRaw().forEach((blockName, positions) -> {
      JsonArray arr = new JsonArray();
      positions.forEach(pos -> arr.add(pos.getX() + "," + pos.getY() + "," + pos.getZ()));
      raw.add(blockName.toString(), arr);
    });
    root.add("raw", raw);

    JsonObject levels = new JsonObject();
    src.getLevelsDistribution().forEach((blockKey, data) -> {
      JsonObject levelData = new JsonObject();
      data.forEach((level, count) -> levelData.addProperty(Integer.toString(level), count));
      levels.add(blockKey.toString(), levelData);
    });
    root.add("levels", levels);

    JsonObject general = new JsonObject();
    src.getGeneralDistribution().forEach((blockKey, count) -> {
      general.addProperty(blockKey.toString(), count);
    });
    root.add("general", general);

    return root;
  }

  public static final OreDistributionJsonSerializer INSTANCE = new OreDistributionJsonSerializer();

}
