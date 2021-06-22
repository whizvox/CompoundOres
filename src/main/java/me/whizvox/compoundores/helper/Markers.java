package me.whizvox.compoundores.helper;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class Markers {

  public static final Marker
      REGISTRY = MarkerManager.getMarker("REGISTRY"),
      SERVER = MarkerManager.getMarker("SERVER"),
      CLIENT = MarkerManager.getMarker("CLIENT");

}
