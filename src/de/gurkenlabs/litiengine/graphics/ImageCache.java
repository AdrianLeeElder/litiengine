/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

/**
 * The Class ImageCache.
 */
public class ImageCache {
  /** The Constant CACHE_DIRECTORY. */
  public static final String CACHE_DIRECTORY = "cache/";

  /** The Constant MAP_CACHE. */
  public static final ImageCache MAPS = new ImageCache("map");

  /** The Constant SPRITESHEET_CACHE. */
  public static final ImageCache SPRITES = new ImageCache("sprites");

  /** The Constant SPRITESHEET_CACHE. */
  public static final ImageCache IMAGES = new ImageCache("images");

  /** The cache. */
  private final ConcurrentHashMap<String, BufferedImage> cache;

  /** The sub folder. */
  private final String subFolder;

  /**
   * Instantiates a new image cache.
   *
   * @param subfolder
   *          the subfolder
   */
  private ImageCache(final String subfolder) {
    this.cache = new ConcurrentHashMap<>();
    this.subFolder = subfolder;
  }

  /**
   * Contains key.
   *
   * @param key
   *          the key
   * @return true, if successful
   */
  public boolean containsKey(final String key) {
    return this.cache.containsKey(key) || new File(this.getFileName(key)).exists();
  }

  /**
   * Gets the.
   *
   * @param key
   *          the key
   * @return the buffered image
   */
  public BufferedImage get(final String key) {
    if (this.cache.containsKey(key)) {
      return this.cache.get(key);
    }

    return this.loadImage(key);
  }

  /**
   * Load all.
   */
  public void loadAll() {
    final File dir = new File(this.getSubFolderName());
    if (!dir.exists() || !dir.isDirectory()) {
      return;
    }
    final File[] directoryListing = dir.listFiles();
    if (directoryListing != null) {
      for (final File child : directoryListing) {
        if (!child.isFile()) {
          continue;
        }

        final BufferedImage img = this.loadImage(child.getName());

        // clean up cached file if the image is null
        if (img == null) {
          child.delete();
        }
      }
    }
  }

  /**
   * Put.
   *
   * @param key
   *          the key
   * @param value
   *          the value
   * @return the buffered image
   */
  public BufferedImage put(final String key, final BufferedImage value) {
    return this.cache.put(key, value);
  }

  /**
   * Put persistent.
   *
   * @param key
   *          the key
   * @param value
   *          the value
   * @return the buffered image
   */
  public BufferedImage putPersistent(final String key, final BufferedImage value) {
    this.cache.put(key, value);
    this.saveImage(key, value);
    return value;
  }

  /**
   * Gets the file name.
   *
   * @param key
   *          the key
   * @return the file name
   */
  private String getFileName(final String key) {
    return this.getSubFolderName() + "\\" + key;
  }

  /**
   * Gets the sub folder name.
   *
   * @return the sub folder name
   */
  private String getSubFolderName() {
    return CACHE_DIRECTORY + this.subFolder;
  }

  /**
   * Load image.
   *
   * @param key
   *          the key
   * @return the buffered image
   */
  private synchronized BufferedImage loadImage(final String key) {
    final File file = new File(this.getFileName(key));
    if (!file.exists()) {
      return null;
    }

    BufferedImage img;
    try {
      img = ImageIO.read(file);
      if (img == null) {
        return null;
      }

      final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
      final GraphicsDevice device = env.getDefaultScreenDevice();
      final GraphicsConfiguration config = device.getDefaultConfiguration();
      final BufferedImage compatibleImg = config.createCompatibleImage(img.getWidth(), img.getHeight(), Transparency.TRANSLUCENT);
      compatibleImg.getGraphics().drawImage(img, 0, 0, null);

      this.cache.put(key, compatibleImg);
      return compatibleImg;
    } catch (final Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Save image.
   *
   * @param key
   *          the key
   * @param value
   *          the value
   */
  private void saveImage(final String key, final BufferedImage value) {
    try {
      final File file = new File(this.getFileName(key));
      final Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("png");
      final ImageWriter writer = iter.next();
      final ImageWriteParam iwp = writer.getDefaultWriteParam();

      file.getParentFile().mkdirs();
      final FileImageOutputStream output = new FileImageOutputStream(file.getAbsoluteFile());
      writer.setOutput(output);
      final IIOImage outimage = new IIOImage(value, null, null);
      writer.write(null, outimage, iwp);
      writer.dispose();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}