package me.whizvox.compoundores.util;

import com.google.gson.*;
import me.whizvox.compoundores.api.component.OreComponent;
import me.whizvox.compoundores.api.target.BlockTargets;
import me.whizvox.compoundores.api.target.IBlockTarget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class OreComponentJsonCodec implements JsonDeserializer<OreComponent>, JsonSerializer<OreComponent> {

  private OreComponentJsonCodec() {}

  @Override
  public OreComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    JsonObject obj = json.getAsJsonObject();
    IBlockTarget target;
    int weight;
    int color = 0xFFFFFF;
    float hardness = 3.0F;
    float resistance = 3.0F;
    int harvestLevel = 0;

    // REQUIRED
    if (obj.has("target")) {
      JsonElement targetElem = obj.get("target");
      if (targetElem.isJsonPrimitive() && targetElem.getAsJsonPrimitive().isString()) {
        target = BlockTargets.create(targetElem.getAsString());
      } else if (targetElem.isJsonArray()) {
        List<String> targetsList = new ArrayList<>();
        targetElem.getAsJsonArray().forEach(elem -> {
          if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString()) {
            targetsList.add(elem.getAsString());
          } else {
            throw new JsonParseException("Elements in target array must all be strings");
          }
        });
        target = BlockTargets.create(targetsList.toArray());
      } else {
        throw new JsonParseException("Target definition must be either a string array or a single string");
      }
    } else {
      throw new JsonParseException("Must define either a target string array or a single target string");
    }

    // REQUIRED
    if (obj.has("weight")) {
      JsonElement weightElem = obj.get("weight");
      if (weightElem.isJsonPrimitive() && ((JsonPrimitive) weightElem).isNumber()) {
        weight = obj.get("weight").getAsInt();
        if (weight < 0) {
          throw new JsonParseException("Weight must be non-negative: " + weight);
        }
      } else {
        throw new JsonParseException("Weight must be a non-negative integer");
      }
    } else {
      throw new JsonParseException("Must define a weight");
    }

    // OPTIONAL
    if (obj.has("color")) {
      JsonElement colorElem = obj.get("color");
      if (colorElem.isJsonPrimitive()) {
        JsonPrimitive colorPrim = colorElem.getAsJsonPrimitive();
        if (colorPrim.isString()) {
          String colorHex = colorPrim.getAsString();
          try {
            color = MathHelper.clamp(Integer.parseInt(colorHex, 16), 0x000000, 0xFFFFFF);
          } catch (NumberFormatException e) {
            throw new JsonParseException("Invalid color hex: " + colorHex);
          }
        } else if (colorPrim.isNumber()) {
          color = colorElem.getAsInt();
        } else {
          throw new JsonParseException("Color must be either a hex string or an integer: " + colorPrim);
        }
      } else {
        throw new JsonParseException("Color must be either a hex string or an integer: " + colorElem);
      }
    }

    // OPTIONAL
    if (obj.has("hardness")) {
      JsonElement destroySpeedElem = obj.get("hardness");
      if (destroySpeedElem.isJsonPrimitive() && ((JsonPrimitive) destroySpeedElem).isNumber()) {
        hardness = destroySpeedElem.getAsFloat();
      } else {
        throw new JsonParseException("Hardness must be a number: " + destroySpeedElem);
      }
    }

    // OPTIONAL
    if (obj.has("resistance")) {
      JsonElement blastResistanceElem = obj.get("resistance");
      if (blastResistanceElem.isJsonPrimitive() && ((JsonPrimitive) blastResistanceElem).isNumber()) {
        resistance = blastResistanceElem.getAsFloat();
      } else {
        throw new JsonParseException("Resistance must be a number: " + blastResistanceElem);
      }
    }

    // OPTIONAL
    if (obj.has("harvestLevel")) {
      JsonElement harvestLevelElem = obj.get("harvestLevel");
      if (harvestLevelElem.isJsonPrimitive() && ((JsonPrimitive) harvestLevelElem).isNumber()) {
        harvestLevel = harvestLevelElem.getAsInt();
      } else {
        throw new JsonParseException("Harvest level must be a number: " + harvestLevelElem);
      }
    }

    return OreComponent.builder()
      .target(target)
      .weight(weight)
      .color(color)
      .hardness(hardness)
      .resistance(resistance)
      .harvestLevel(harvestLevel)
      .build();
  }

  @Override
  public JsonElement serialize(OreComponent oreComp, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject obj = new JsonObject();
    JsonArray targets = new JsonArray();
    oreComp.getTarget().serialize().forEach(targets::add);
    obj.add("target", targets);
    obj.addProperty("weight", oreComp.getWeight());
    if (oreComp.getHardness() != 3.0F) {
      obj.addProperty("hardness", oreComp.getHardness());
    }
    if (oreComp.getResistance() != 3.0F) {
      obj.addProperty("resistance", oreComp.getResistance());
    }
    if (oreComp.getHarvestLevel() != 0) {
      obj.addProperty("harvestLevel", oreComp.getHarvestLevel());
    }
    if (oreComp.getColor() != 0xFFFFFF) {
      obj.addProperty("color", Integer.toString(oreComp.getColor(), 16));
    }
    return obj;
  }

  public static final OreComponentJsonCodec INSTANCE = new OreComponentJsonCodec();

}
