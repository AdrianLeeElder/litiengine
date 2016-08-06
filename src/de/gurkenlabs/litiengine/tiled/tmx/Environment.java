/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.tiled.tmx;

import java.awt.Color;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.gurkenlabs.litiengine.entities.DecorMob;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IMovableCombatEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.PropState;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;
import de.gurkenlabs.tiled.tmx.IMap;
import de.gurkenlabs.tiled.tmx.IMapLoader;
import de.gurkenlabs.tiled.tmx.IMapObject;
import de.gurkenlabs.tiled.tmx.IMapObjectLayer;
import de.gurkenlabs.tiled.tmx.TmxMapLoader;
import de.gurkenlabs.tiled.tmx.utilities.MapUtilities;
import de.gurkenlabs.util.geom.GeometricUtilities;

/**
 * The Class MapContainerBase.
 */
public class Environment implements IEnvironment {
  public static final String COLLISIONBOX = "COLLISIONBOX";
  private static final String MAP_OBJECT_LIGHTSOURCE = "LIGHTSOURCE";



  private static int localIdSequence = 0;
  private static int mapIdSequence;
  /** The map. */
  private final IMap map;

  private final Map<Integer, ICombatEntity> combatEntities;
  private final List<Prop> props;
  private final Map<Integer, IMovableEntity> movableEntities;

  private final List<LightSource> lightSources;

  /**
   * Instantiates a new map container base.
   *
   * @param map
   *          the map
   */
  public Environment(final String mapPath) {
    final IMapLoader tmxLoader = new TmxMapLoader();
    this.map = tmxLoader.LoadMap(mapPath);
    mapIdSequence = MapUtilities.getMaxMapId(this.getMap());

    this.combatEntities = new ConcurrentHashMap<>();
    this.movableEntities = new ConcurrentHashMap<>();
    this.lightSources = new CopyOnWriteArrayList<>();
    this.props = new CopyOnWriteArrayList<>();
  }

  public void clear() {
    List<IEntity> allEntities = Stream.concat(this.combatEntities.values().stream(), this.movableEntities.values().stream()).collect(Collectors.toList());
    for (IEntity e : allEntities) {
      if (e.getAnimationController() != null) {
        e.getAnimationController().dispose();
      }
    }

    this.combatEntities.clear();
    this.movableEntities.clear();
    this.lightSources.clear();
  }

  @Override
  public void init() {
    this.loadMapObjects();
  }

  @Override
  public List<Prop> getProps() {
    return this.props;
  }

  @Override
  public void add(final int mapId, final IMovableCombatEntity entity) {
    this.addCombatEntity(mapId, entity);
    this.addMovableEntity(mapId, entity);
  }

  @Override
  public void addCombatEntity(final int mapId, final ICombatEntity entity) {
    this.combatEntities.put(mapId, entity);
  }

  @Override
  public void addMovableEntity(final int mapId, final IMovableEntity entity) {
    this.movableEntities.put(mapId, entity);
  }

  public void addCollisionBoxes(final IMapObject mapObject) {
    if (mapObject.getType().equals(COLLISIONBOX)) {
      Game.getPhysicsEngine().add(mapObject.getCollisionBox());
    }
  }

  @Override
  public Collection<ICombatEntity> getCombatEntities() {
    return this.combatEntities.values();
  }

  @Override
  public ICombatEntity getCombatEntity(final int mapId) {
    if (this.combatEntities.containsKey(mapId)) {
      return this.combatEntities.get(mapId);
    }

    return null;
  }

  @Override
  public List<LightSource> getLightSources() {
    return this.lightSources;
  }

  @Override
  public List<ICombatEntity> findCombatEntities(final Shape shape) {
    return this.findCombatEntities(shape, (entity) -> true);
  }

  @Override
  public List<ICombatEntity> findCombatEntities(final Shape shape, final Predicate<ICombatEntity> condition) {
    final ArrayList<ICombatEntity> entities = new ArrayList<>();
    if (shape == null) {
      return entities;
    }

    // for rectangle we can jsut use the intersects method
    if (shape instanceof Rectangle2D) {
      Rectangle2D rect = (Rectangle2D) shape;
      for (final ICombatEntity combatEntity : this.getCombatEntities().stream().filter(condition).collect(Collectors.toList())) {
        if (combatEntity.getHitBox().intersects(rect)) {
          entities.add(combatEntity);
        }
      }

      return entities;
    }

    // for other shapes, we check if the shape's bounds intersect the hitbox and
    // if so, we then check if the actual shape intersects the hitbox
    for (final ICombatEntity combatEntity : this.getCombatEntities().stream().filter(condition).collect(Collectors.toList())) {
      if (combatEntity.getHitBox().intersects(shape.getBounds())) {
        if (GeometricUtilities.shapeIntersects(combatEntity.getHitBox(), shape)) {
          entities.add(combatEntity);
        }
      }
    }

    return entities;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.map.IMapContainer#getMap()
   */
  @Override
  public IMap getMap() {
    return this.map;
  }

  @Override
  public Collection<IMovableEntity> getMovableEntities() {
    return this.movableEntities.values();
  }

  @Override
  public IMovableEntity getMovableEntity(final int mapId) {
    if (this.movableEntities.containsKey(mapId)) {
      return this.movableEntities.get(mapId);
    }

    return null;
  }

  /**
   * Negative map ids are only used locally.
   */
  @Override
  public synchronized int getLocalMapId() {
    return --localIdSequence;
  }

  @Override
  public void remove(final int mapId) {
    if (this.movableEntities.containsKey(mapId)) {
      this.movableEntities.remove(mapId);
    }

    if (this.combatEntities.containsKey(mapId)) {
      this.combatEntities.remove(mapId);
    }
  }

  @Override
  public synchronized int getMapId() {
    return ++mapIdSequence;
  }

  protected void addMapObject(final IMapObject mapObject) {
    if (mapObject.getType().equals(MAP_OBJECT_LIGHTSOURCE)) {
      final String propBrightness = mapObject.getCustomProperty(MapObjectProperties.LIGHTBRIGHTNESS);
      final String propColor = mapObject.getCustomProperty(MapObjectProperties.LIGHTCOLOR);
      if (propBrightness == null || propBrightness.isEmpty() || propColor == null || propColor.isEmpty()) {
        return;
      }

      final int brightness = Integer.parseInt(propBrightness);
      final Color color = Color.decode(propColor);

      this.getLightSources().add(new LightSource(this, new Point(mapObject.getLocation()), (int) (mapObject.getDimension().getWidth() / 2.0), brightness, new Color(color.getRed(), color.getGreen(), color.getBlue(), brightness)));
    }

    this.addCollisionBoxes(mapObject);
  }

  private void loadMapObjects() {
    for (final IMapObjectLayer layer : this.getMap().getMapObjectLayers()) {
      for (final IMapObject mapObject : layer.getMapObjects()) {
        if (mapObject.getType() == null || mapObject.getType().isEmpty()) {
          continue;
        }

        this.addMapObject(mapObject);
      }
    }
  }

  
  
  @Override
  public void addProp(IMapObject mapObject) {
    if (!mapObject.getType().equalsIgnoreCase(MapObjectTypes.PROP)) {
      return;
    }
    Prop prop = new Prop(mapObject.getLocation(), mapObject.getCustomProperty(MapObjectProperties.SPRITESHEETNAME), Material.valueOf(mapObject.getCustomProperty(MapObjectProperties.MATERIAL)));
    if (!mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE).isEmpty()) {
      prop.setIndestructible(Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE)));
    }
    if (!prop.isIndestructible()) {
      prop.getAnimationController().add(PropAnimationController.createAnimation(prop, PropState.Damaged));
      prop.getAnimationController().add(PropAnimationController.createAnimation((Prop) prop, PropState.Destroyed));
    }
    prop.getAttributes().getHealth().addMaxModifier(new AttributeModifier<>(Modification.Set, Integer.parseInt(mapObject.getCustomProperty(MapObjectProperties.HEALTH))));
    prop.setCollision(Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperties.COLLISION)));
    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTHFACTOR) != null) {
      prop.setCollisionBoxWidthFactor(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTHFACTOR)));
    }
    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHTFACTOR) != null) {
      prop.setCollisionBoxHeightFactor(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHTFACTOR)));
    }
    prop.setSize(mapObject.getDimension().width, mapObject.getDimension().height);
    this.getProps().add(prop);
    this.addCombatEntity(mapObject.getId(), prop);
    if (mapObject.getCustomProperty(MapObjectProperties.TEAM) != null) {
      prop.setTeam(Integer.parseInt((mapObject.getCustomProperty(MapObjectProperties.TEAM))));
    }
    if (prop.hasCollision()) {
      Game.getPhysicsEngine().add(prop);
    }
  }

  @Override
  public void addEffect(IMapObject mapObject) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addMob(IMapObject mapObject) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addDecorMob(final IMapObject mapObject) {
    if (!mapObject.getType().equalsIgnoreCase(MapObjectTypes.DECORMOB)) {
      return;
    }
    DecorMob mob = new DecorMob(mapObject.getLocation(), mapObject.getCustomProperty(MapObjectProperties.MOBTYPE));
    mob.setCollision(Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperties.COLLISION)));
    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTHFACTOR) != null) {
      mob.setCollisionBoxWidthFactor(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTHFACTOR)));
    }
    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHTFACTOR) != null) {
      mob.setCollisionBoxHeightFactor(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHTFACTOR)));
    }
    mob.setSize(mapObject.getDimension().width, mapObject.getDimension().height);
    this.addMovableEntity(mapObject.getId(), mob);
    if (mob.hasCollision()) {
      Game.getPhysicsEngine().add(mob);
    }
  }
}