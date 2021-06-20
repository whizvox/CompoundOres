package me.whizvox.compoundores.util;

import com.google.gson.*;
import me.whizvox.compoundores.api.OreComponent;
import me.whizvox.compoundores.api.OreType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OreComponentJsonCodec implements JsonDeserializer<OreComponent>, JsonSerializer<OreComponent> {

  private OreComponentJsonCodec() {}

  @Override
  public OreComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    JsonObject obj = json.getAsJsonObject();
    Block target = null;
    int weight;
    OreType type = OreType.NONMETAL;
    int color = 0xFFFFFF;
    float destroySpeed = 3.0F;
    float blastResistance = 3.0F;
    int harvestLevel = 0;

    // REQUIRED
    if (obj.has("target")) {
      JsonElement targetElem = obj.get("target");
      List<String> targetCandidates = new ArrayList<>();
      if (targetElem.isJsonArray()) {
        for (JsonElement targetCandidate : ((JsonArray) targetElem)) {
          if (targetCandidate.isJsonPrimitive() && ((JsonPrimitive) targetCandidate).isString()) {
            targetCandidates.add(targetCandidate.getAsString());
          } else {
            throw new JsonParseException("Target list entry must only consist of strings: " + targetCandidate);
          }
        }
      } else if (targetElem.isJsonPrimitive() && ((JsonPrimitive) targetElem).isString()) {
        targetCandidates.add(targetElem.getAsString());
      } else {
        throw new JsonParseException("Target must be either a string array or single string: " + targetElem);
      }

      for (String targetCandidate : targetCandidates) {
        if (!targetCandidate.isEmpty() && targetCandidate.charAt(0) == '#') {
          ResourceLocation tagName = ResourceLocation.tryParse(targetCandidate.substring(1));
          if (tagName == null) {
            throw new JsonParseException("Target tag is improperly formatted: " + targetCandidate.substring(1));
          }
          ITag<Block> tag = BlockTags.getAllTags().getTag(tagName);
          if (tag != null) {
            if (!tag.getValues().isEmpty()) {
              target = tag.getValues().get(0);
              break;
            }
          }
        } else {
          ResourceLocation blockName = ResourceLocation.tryParse(targetCandidate);
          if (blockName == null) {
            throw new JsonParseException("Target block name is improperly formatted: " + targetCandidate);
          }
          Block resolvedTarget = ForgeRegistries.BLOCKS.getValue(blockName);
          if (resolvedTarget != null && !resolvedTarget.is(Blocks.AIR)) {
            target = resolvedTarget;
            break;
          }
        }
      }
    } else {
      throw new JsonParseException("Invalid ore component definition: must define a target");
    }
    if (target == null) {
      throw new JsonParseException("No target could be resolved");
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
    if (obj.has("type")) {
      JsonElement typeElem = obj.get("type");
      if (typeElem.isJsonPrimitive() && ((JsonPrimitive) typeElem).isString()) {
        String typeStr = obj.get("type").getAsString();
        type = null;
        for (OreType oreType : OreType.values()) {
          if (oreType.toString().equalsIgnoreCase(typeStr)) {
            type = oreType;
            break;
          }
        }
        if (type == null) {
          throw new JsonParseException("Unknown type: " + typeStr);
        }
      } else {
        throw new JsonParseException("Type must be a string");
      }
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
    if (obj.has("destroySpeed")) {
      JsonElement destroySpeedElem = obj.get("destroySpeed");
      if (destroySpeedElem.isJsonPrimitive() && ((JsonPrimitive) destroySpeedElem).isNumber()) {
        destroySpeed = destroySpeedElem.getAsFloat();
      } else {
        throw new JsonParseException("Destroy speed must be a number: " + destroySpeedElem);
      }
    }

    // OPTIONAL
    if (obj.has("blastResistance")) {
      JsonElement blastResistanceElem = obj.get("blastResistance");
      if (blastResistanceElem.isJsonPrimitive() && ((JsonPrimitive) blastResistanceElem).isNumber()) {
        blastResistance = blastResistanceElem.getAsFloat();
      } else {
        throw new JsonParseException("Blast resistance must be a number: " + blastResistanceElem);
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
      .block(target)
      .spawnWeight(weight)
      .color(color)
      .type(type)
      .destroySpeed(destroySpeed)
      .resistance(blastResistance)
      .harvestLevel(harvestLevel)
      .build();
  }

  @Override
  public JsonElement serialize(OreComponent oreComp, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject obj = new JsonObject();
    obj.addProperty("target", oreComp.getBlock().getRegistryName().toString());
    obj.addProperty("weight", oreComp.getSpawnWeight());
    if (oreComp.getDestroySpeed() != 3.0F) {
      obj.addProperty("destroySpeed", oreComp.getDestroySpeed());
    }
    if (oreComp.getBlastResistance() != 3.0F) {
      obj.addProperty("blastResistance", oreComp.getBlastResistance());
    }
    if (oreComp.getHarvestLevel() != 0) {
      obj.addProperty("harvestLevel", oreComp.getHarvestLevel());
    }
    if (oreComp.getColor() != 0xFFFFFF) {
      obj.addProperty("color", oreComp.getColor());
    }
    obj.addProperty("type", oreComp.getType().toString().toLowerCase(Locale.ENGLISH));
    return obj;
  }

  public static final OreComponentJsonCodec INSTANCE = new OreComponentJsonCodec();

}
