package me.whizvox.compoundores.util;

import com.google.gson.*;
import me.whizvox.compoundores.api.component.OreComponent;
import me.whizvox.compoundores.api.target.BlockTargets;
import me.whizvox.compoundores.api.util.NamedMaterial;
import me.whizvox.compoundores.api.util.NamedMaterialColor;
import me.whizvox.compoundores.api.util.NamedSoundType;
import net.minecraftforge.common.ToolType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class OreComponentJsonCodec implements JsonDeserializer<OreComponent>, JsonSerializer<OreComponent> {

  private OreComponentJsonCodec() {}

  private static void err(String msg) {
    throw new JsonParseException(msg);
  }

  @Override
  public OreComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    JsonObject obj = json.getAsJsonObject();
    OreComponent.Builder builder = OreComponent.builder();

    // REQUIRED
    if (obj.has("target")) {
      JsonElement targetElem = obj.get("target");
      if (targetElem.isJsonPrimitive() && targetElem.getAsJsonPrimitive().isString()) {
        builder.target(BlockTargets.create(targetElem.getAsString()));
      } else if (targetElem.isJsonArray()) {
        List<String> targetsList = new ArrayList<>();
        targetElem.getAsJsonArray().forEach(elem -> {
          if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString()) {
            targetsList.add(elem.getAsString());
          } else {
            err("Elements in target array must all be strings");
          }
        });
        builder.target(BlockTargets.create(targetsList.toArray()));
      } else {
        err("Target definition must be either a string array or a single string");
      }
    } else {
      err("Must define either a target string array or a single target string");
    }

    // REQUIRED
    if (obj.has("weight")) {
      JsonElement weightElem = obj.get("weight");
      if (weightElem.isJsonPrimitive() && ((JsonPrimitive) weightElem).isNumber()) {
        int weight = obj.get("weight").getAsInt();
        if (weight < 0) {
          err("Weight must be non-negative: " + weight);
        } else {
          builder.weight(weight);
        }
      } else {
        err("Weight must be a non-negative integer");
      }
    } else {
      err("Must define a weight");
    }

    // OPTIONAL
    if (obj.has("overlayColor")) {
      JsonElement colorElem = obj.get("overlayColor");
      if (colorElem.isJsonPrimitive()) {
        JsonPrimitive colorPrim = colorElem.getAsJsonPrimitive();
        if (colorPrim.isString()) {
          String colorHex = colorPrim.getAsString();
          if (colorHex.startsWith("#")) {
            try {
              builder.overlayColor(Integer.parseInt(colorHex.substring(1), 16));
            } catch (NumberFormatException e) {
              err("Invalid overlay color hex: " + colorHex);
            }
          } else {
            err("Overlay color hex string must start with hash (#): " + colorHex);
          }
        } else if (colorPrim.isNumber()) {
          builder.overlayColor(colorElem.getAsInt());
        } else {
          err("Overlay color must be either a hex string or an integer: " + colorPrim);
        }
      } else {
        err("Overlay color must be either a hex string or an integer: " + colorElem);
      }
    }

    // OPTIONAL
    if (obj.has("material")) {
      JsonElement materialElem = obj.get("material");
      if (materialElem.isJsonPrimitive() && materialElem.getAsJsonPrimitive().isString()) {
        builder.material(NamedMaterial.from(materialElem.getAsString()));
      } else {
        err("Material must be a string: " + materialElem);
      }
    }

    // OPTIONAL
    if (obj.has("materialColor")) {
      JsonElement colorElem = obj.get("materialColor");
      if (colorElem.isJsonPrimitive() && colorElem.getAsJsonPrimitive().isString()) {
        if (colorElem.getAsJsonPrimitive().isString()) {
          builder.materialColor(NamedMaterialColor.from(colorElem.getAsString()));
        } else {
          err("Material color must either be a string: " + colorElem);
        }
      } else if (colorElem.isJsonPrimitive() && colorElem.getAsJsonPrimitive().isNumber()) {
        builder.materialColor(NamedMaterialColor.from(colorElem.getAsInt()));
      } else {
        err("Material color must be a string: " + colorElem);
      }
    }

    // OPTIONAL
    if (obj.has("tool")) {
      JsonElement toolElem = obj.get("tool");
      if (toolElem.isJsonPrimitive() && toolElem.getAsJsonPrimitive().isString()) {
        ToolType harvestTool = ToolType.get(toolElem.getAsString());
        if (harvestTool == null) {
          err("Invalid harvest tool type: " + toolElem);
        } else {
          boolean toolRequired = OreComponent.DefaultValues.TOOL_REQUIRED;
          // OPTIONAL
          if (obj.has("toolRequired")) {
            JsonElement toolReqElem = obj.get("toolRequired");
            if (toolReqElem.isJsonPrimitive() && toolReqElem.getAsJsonPrimitive().isBoolean()) {
              toolRequired = toolReqElem.getAsBoolean();
            } else {
              err("toolRequired property must be a boolean");
            }
          }
          builder.tool(harvestTool, toolRequired);
        }
      } else if (toolElem.isJsonNull()) {
        builder.tool(null);
      } else {
        err("Harvest tool must be a string: " + toolElem);
      }
    }

    // OPTIONAL
    if (obj.has("sound")) {
      JsonElement soundElem = obj.get("sound");
      if (soundElem.isJsonPrimitive() && soundElem.getAsJsonPrimitive().isString()) {
        builder.sound(NamedSoundType.from(soundElem.getAsString()));
      } else {
        err("Sound must be a string: " + soundElem);
      }
    }

    // OPTIONAL
    if (obj.has("hardness")) {
      JsonElement destroySpeedElem = obj.get("hardness");
      if (destroySpeedElem.isJsonPrimitive() && ((JsonPrimitive) destroySpeedElem).isNumber()) {
        builder.hardness(destroySpeedElem.getAsFloat());
      } else {
        err("Hardness must be a number: " + destroySpeedElem);
      }
    }

    // OPTIONAL
    if (obj.has("resistance")) {
      JsonElement resistanceElem = obj.get("resistance");
      if (resistanceElem.isJsonPrimitive() && ((JsonPrimitive) resistanceElem).isNumber()) {
        builder.resistance(resistanceElem.getAsFloat());
      } else {
        err("Resistance must be a number: " + resistanceElem);
      }
    }

    // OPTIONAL
    if (obj.has("harvestLevel")) {
      JsonElement harvestLevelElem = obj.get("harvestLevel");
      if (harvestLevelElem.isJsonPrimitive() && ((JsonPrimitive) harvestLevelElem).isNumber()) {
        builder.harvestLevel(harvestLevelElem.getAsInt());
      } else {
        err("Harvest level must be a number: " + harvestLevelElem);
      }
    }

    return builder.build();
  }

  @Override
  public JsonElement serialize(OreComponent oreComp, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject obj = new JsonObject();
    JsonArray targets = new JsonArray();
    oreComp.getTarget().serialize().forEach(targets::add);
    obj.add("target", targets);
    obj.addProperty("weight", oreComp.getWeight());
    if (oreComp.getHardness() != OreComponent.DefaultValues.HARDNESS) {
      obj.addProperty("hardness", oreComp.getHardness());
    }
    if (oreComp.getResistance() != OreComponent.DefaultValues.RESISTANCE) {
      obj.addProperty("resistance", oreComp.getResistance());
    }
    if (oreComp.getHarvestLevel() != OreComponent.DefaultValues.HARVEST_LEVEL) {
      obj.addProperty("harvestLevel", oreComp.getHarvestLevel());
    }
    if (oreComp.getOverlayColor() != OreComponent.DefaultValues.OVERLAY_COLOR) {
      obj.addProperty("overlayColor", Integer.toString(oreComp.getOverlayColor(), 16));
    }
    if (oreComp.getMaterial() != OreComponent.DefaultValues.MATERIAL) {
      obj.addProperty("material", oreComp.getMaterial().name);
    }
    if (oreComp.getMaterialColor() != OreComponent.DefaultValues.MATERIAL_COLOR) {
      if (oreComp.getMaterialColor() == null) {
        obj.add("materialColor", JsonNull.INSTANCE);
      } else {
        obj.addProperty("materialColor", oreComp.getMaterialColor().name);
      }
    }
    if (oreComp.getSound() != OreComponent.DefaultValues.SOUND) {
      obj.addProperty("sound", oreComp.getSound().name);
    }
    if (oreComp.getHarvestTool() != OreComponent.DefaultValues.HARVEST_TOOL) {
      if (oreComp.getHarvestTool() == null) {
        obj.add("harvestTool", JsonNull.INSTANCE);
      } else {
        obj.addProperty("harvestTool", oreComp.getHarvestTool().getName());
      }
    }
    if (oreComp.isToolRequired() != OreComponent.DefaultValues.TOOL_REQUIRED) {
      obj.addProperty("toolRequired", oreComp.isToolRequired());
    }
    return obj;
  }

  public static final OreComponentJsonCodec INSTANCE = new OreComponentJsonCodec();

}
