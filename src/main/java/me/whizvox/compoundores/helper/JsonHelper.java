package me.whizvox.compoundores.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.whizvox.compoundores.api.component.OreComponent;
import me.whizvox.compoundores.util.OreComponentJsonCodec;

public class JsonHelper {

  public static final Gson GSON = new GsonBuilder()
    .registerTypeAdapter(OreComponent.class, OreComponentJsonCodec.INSTANCE)
    .setPrettyPrinting()
    .create();

}
