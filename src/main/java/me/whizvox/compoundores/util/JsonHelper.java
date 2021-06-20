package me.whizvox.compoundores.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.whizvox.compoundores.api.OreComponent;

public class JsonHelper {

  public static final Gson GSON = new GsonBuilder()
    .registerTypeAdapter(OreComponent.class, OreComponentJsonCodec.INSTANCE)
    .setPrettyPrinting()
    .create();

}
