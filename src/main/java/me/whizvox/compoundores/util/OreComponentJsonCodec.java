package me.whizvox.compoundores.util;

import com.google.gson.*;
import me.whizvox.compoundores.api.component.OreComponent;
import me.whizvox.compoundores.api.target.BlockTargets;
import me.whizvox.compoundores.api.util.NamedMaterialColor;
import net.minecraft.block.material.MaterialColor;
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
    if (obj.has("materialColor")) {
      JsonElement colorElem = obj.get("materialColor");
      if (colorElem.isJsonPrimitive() && colorElem.getAsJsonPrimitive().isString()) {
        if (colorElem.getAsJsonPrimitive().isString()) {
        String matColorStr = colorElem.getAsString();
          MaterialColor matColor = NamedMaterialColor.NAME_MAP.get(matColorStr).base;
          if (matColor != null) {
            builder.materialColor(matColor);
          } else {
            err("Material color string does not match any field: " + matColorStr);
          }
        } else {
          err("Material color must either be a string: " + colorElem);
        }
      } else if (colorElem.isJsonPrimitive() && colorElem.getAsJsonPrimitive().isNumber()) {
        int id = colorElem.getAsInt();
        if (id >= 0 && id < MaterialColor.MATERIAL_COLORS.length) {
          builder.materialColor(MaterialColor.MATERIAL_COLORS[id]);
        } else {
          err("Material color id is out of bounds [0,64): " + id);
        }
      } else {
        err("Material color must be a string: " + colorElem);
      }
    }

    // OPTIONAL
    if (obj.has("harvestTool")) {
      JsonElement toolElem = obj.get("harvestTool");
      if (toolElem.isJsonPrimitive() && toolElem.getAsJsonPrimitive().isString()) {
        ToolType harvestTool = ToolType.get(toolElem.getAsString());
        if (harvestTool == null) {
          err("Invalid harvest tool type: " + toolElem);
        } else {
          builder.tool(harvestTool);
        }
      } else if (toolElem.isJsonNull()) {
        builder.tool(null);
      } else {
        err("Harvest tool must be a string: " + toolElem);
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
    if (oreComp.getMaterialColor() != OreComponent.DefaultValues.MATERIAL_COLOR) {
      obj.addProperty("materialColor", NamedMaterialColor.ID_MAP.get(oreComp.getMaterialColor().id).name);
    }
    if (oreComp.getHarvestTool() != OreComponent.DefaultValues.HARVEST_TOOL) {
      if (oreComp.getHarvestTool() == null) {
        obj.add("harvestTool", JsonNull.INSTANCE);
      } else {
        obj.addProperty("harvestTool", oreComp.getHarvestTool().getName());
      }
    }
    return obj;
  }

  public static final OreComponentJsonCodec INSTANCE = new OreComponentJsonCodec();

}
