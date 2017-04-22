package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Point;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapUtilities {
  private static Map<String, ITileAnimation> animations;
  private static Map<String, Boolean> hasAnimation;
  private static Map<String, ITileset> tilesets = new ConcurrentHashMap<String, ITileset>();

  static {
    animations = new ConcurrentHashMap<>();
    hasAnimation = new ConcurrentHashMap<>();
    tilesets = new ConcurrentHashMap<>();
  }

  public static int getMaxMapId(final IMap map) {
    int maxId = 0;
    if (map.getMapObjectLayers() == null) {
      return maxId;
    }

    for (IMapObjectLayer objectLayer : map.getMapObjectLayers()) {
      if (objectLayer == null || objectLayer.getMapObjects() == null) {
        continue;
      }

      for (IMapObject mapObject : objectLayer.getMapObjects()) {
        if (mapObject == null) {
          continue;
        }

        if (mapObject.getId() > maxId) {
          maxId = mapObject.getId();
        }
      }
    }

    return maxId;
  }

  public static Rectangle2D getTileBoundingBox(final IMap map, final Point2D mapLocation) {
    Point location = getTileLocation(map, mapLocation);
    return new Rectangle2D.Double(location.x * map.getTileSize().getWidth(), location.y * map.getTileSize().getHeight(), map.getTileSize().getWidth(), map.getTileSize().getHeight());
  }

  public static Rectangle2D getTileBoundingBox(final IMap map, final Point tile) {
    return new Rectangle2D.Double(tile.x * map.getTileSize().getWidth(), tile.y * map.getTileSize().getHeight(), map.getTileSize().getWidth(), map.getTileSize().getHeight());
  }

  public static Point getTileLocation(final IMap map, final Point2D mapLocation) {
    return new Point((int) (mapLocation.getX() / map.getTileSize().getWidth()), (int) (mapLocation.getY() / map.getTileSize().getHeight()));
  }

  /**
   * Gets the tiles by pixel location.
   *
   * @param location
   *          the location
   * @return the tiles by pixel location
   */
  public static List<ITile> getTilesByPixelLocation(final IMap map, final Point2D location) {
    final List<ITile> tilesAtLocation = new ArrayList<ITile>();
    if (map.getTileLayers() == null || map.getTileLayers().size() == 0) {
      return tilesAtLocation;
    }

    final Point tileLocation = getTileLocation(map, location);
    for (final ITileLayer layer : map.getTileLayers()) {
      final ITile tile = layer.getTile(tileLocation.x, tileLocation.y);
      if (tile != null) {
        tilesAtLocation.add(tile);
      }
    }

    return tilesAtLocation;
  }

  public static ITerrain[] getTerrain(final IMap map, final int gId) {
    for (final ITileset tileset : map.getTilesets()) {
      final int lastGridId = tileset.getFirstGridId() - 1 + tileset.getTilecount();
      if (tileset.getFirstGridId() - 1 > gId) {
        continue;
      }

      if (lastGridId < gId) {
        continue;
      }

      return tileset.getTerrain(gId);
    }

    return new ITerrain[4];
  }

  public static ITileAnimation getAnimation(final IMap map, final int gId) {

    String cacheKey = map.getFileName() + "[" + gId + "]";
    if (hasAnimation.containsKey(cacheKey) && !hasAnimation.get(cacheKey)) {
      return null;
    }

    if (animations.containsKey(cacheKey)) {
      return animations.get(cacheKey);
    }

    for (final ITileset tileset : map.getTilesets()) {
      final int lastGridId = tileset.getFirstGridId() - 1 + tileset.getTilecount();
      if (tileset.getFirstGridId() - 1 > gId) {
        continue;
      }

      if (lastGridId < gId) {
        continue;
      }

      ITileAnimation anim = tileset.getAnimation(gId);
      boolean animation = false;
      if (anim != null) {
        animations.put(cacheKey, anim);
        animation = true;
      }

      hasAnimation.put(cacheKey, animation);

      return anim;
    }

    return null;
  }

  /**
   * Searches for the tile set that contains the specified tile, identified by
   * the grid id.
   *
   * @param map
   *          the map
   * @param tile
   *          the tile
   * @return the tileset
   */
  public static ITileset findTileSet(final IMap map, final ITile tile) {

    String cacheKey = map.getFileName() + "[" + tile.getGridId() + "]";
    if (tilesets.containsKey(cacheKey)) {
      return tilesets.get(cacheKey);
    }

    ITileset match = null;

    for (final ITileset tileset : map.getTilesets()) {
      final int lastGridId = tileset.getFirstGridId() - 1 + tileset.getTilecount();
      if (tileset.getFirstGridId() > tile.getGridId()) {
        continue;
      }

      if (lastGridId < tile.getGridId()) {
        continue;
      }

      match = tileset;
      break;
    }

    if (match != null) {
      tilesets.put(cacheKey, match);
    }

    return match;
  }

  public static Path2D convertPolylineToPath(final IMapObject mapObject) {
    if (mapObject == null || mapObject.getPolyline() == null || mapObject.getPolyline().getPoints().size() == 0) {
      return null;
    }

    Path2D path = new Path2D.Float();
    path.moveTo(mapObject.getLocation().getX(), mapObject.getLocation().getY());
    for (int i = 1; i < mapObject.getPolyline().getPoints().size(); i++) {
      Point2D point = mapObject.getPolyline().getPoints().get(i);
      path.lineTo(mapObject.getLocation().getX() + point.getX(), mapObject.getLocation().getY() + point.getY());
    }

    return path;
  }

  public static List<Point2D> convertPolylineToPointList(final IMapObject mapObject) {
    if (mapObject == null || mapObject.getPolyline() == null || mapObject.getPolyline().getPoints().size() == 0) {
      return null;
    }

    List<Point2D> points = new ArrayList<>();
    for (int i = 1; i < mapObject.getPolyline().getPoints().size(); i++) {
      Point2D point = mapObject.getPolyline().getPoints().get(i);
      points.add(new Point2D.Double(mapObject.getLocation().getX() + point.getX(), mapObject.getLocation().getY() + point.getY()));
    }

    return points;
  }

  public static IMapObject findMapObject(final IMap map, final int id) {
    for (IMapObjectLayer layer : map.getMapObjectLayers()) {
      for (IMapObject obj : layer.getMapObjects()) {
        if (obj.getId() == id) {
          return obj;
        }
      }
    }

    return null;
  }
}
