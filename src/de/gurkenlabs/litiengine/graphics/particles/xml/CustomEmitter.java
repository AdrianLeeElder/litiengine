package de.gurkenlabs.litiengine.graphics.particles.xml;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.LeftLineParticle;
import de.gurkenlabs.litiengine.graphics.particles.LowQualityRectangleFillParticle;
import de.gurkenlabs.litiengine.graphics.particles.OvalParticle;
import de.gurkenlabs.litiengine.graphics.particles.Particle;
import de.gurkenlabs.litiengine.graphics.particles.RectangleFillParticle;
import de.gurkenlabs.litiengine.graphics.particles.RectangleOutlineParticle;
import de.gurkenlabs.litiengine.graphics.particles.RightLineParticle;
import de.gurkenlabs.litiengine.graphics.particles.ShimmerParticle;
import de.gurkenlabs.litiengine.graphics.particles.TextParticle;
import de.gurkenlabs.util.io.FileUtilities;

@EmitterInfo(maxParticles = 0, spawnAmount = 0, activateOnInit = true)
public class CustomEmitter extends Emitter {
  private static final Logger log = Logger.getLogger(CustomEmitter.class.getName());
  private static final Map<String, CustomEmitterData> loadedCustomEmitters;

  static {
    loadedCustomEmitters = new ConcurrentHashMap<>();
  }

  public static CustomEmitterData load(String emitterXml) {
    final String name = FileUtilities.getFileName(emitterXml);
    if (loadedCustomEmitters.containsKey(name)) {
      return loadedCustomEmitters.get(name);
    }

    try {
      final JAXBContext jaxbContext = JAXBContext.newInstance(CustomEmitterData.class);
      final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

      if (!new File(emitterXml).exists()) {
        emitterXml = Paths.get(Game.getInfo().getEmitterDirectory(), emitterXml).toString();
      }

      final InputStream xml = FileUtilities.getGameResource(emitterXml);
      if (xml == null) {
        return null;
      }

      final CustomEmitterData loaded = (CustomEmitterData) jaxbUnmarshaller.unmarshal(xml);
      loadedCustomEmitters.put(name, loaded);
      return loaded;
    } catch (final JAXBException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return null;
  }

  private CustomEmitterData emitterData;

  public CustomEmitter(final double originX, final double originY, final String emitterXml) {
    super(originX, originY);

    this.emitterData = load(emitterXml);
    if (this.emitterData == null) {
      this.delete();
      return;
    }

    // set emitter parameters
    this.setMaxParticles(this.getEmitterData().getMaxParticles());
    this.setParticleMinTTL(this.getEmitterData().getParticleMinTTL());
    this.setParticleMaxTTL(this.getEmitterData().getParticleMaxTTL());
    this.setTimeToLive(this.getEmitterData().getEmitterTTL());
    this.setSpawnAmount(this.getEmitterData().getSpawnAmount());
    this.setSpawnRate(this.getEmitterData().getSpawnRate());
    this.setParticleUpdateRate(this.getEmitterData().getUpdateRate());
    this.setSize(this.getEmitterData().getWidth(), this.getEmitterData().getHeight());

    for (final ParticleColor color : this.getEmitterData().getColors()) {
      this.addParticleColor(color.toColor());
    }
  }

  public CustomEmitterData getEmitterData() {
    return this.emitterData;
  }

  @Override
  protected Particle createNewParticle() {
    float x;
    float y;
    float deltaX;
    float deltaY;
    float gravityX;
    float gravityY;
    float width;
    float height;
    float deltaWidth;
    float deltaHeight;

    x = this.getEmitterData().getX().get();
    y = this.getEmitterData().getY().get();
    deltaX = this.getEmitterData().getDeltaX().get();
    deltaY = this.getEmitterData().getDeltaY().get();
    gravityX = this.getEmitterData().getGravityX().get();
    gravityY = this.getEmitterData().getGravityY().get();
    width = this.getEmitterData().getParticleWidth().get();
    height = this.getEmitterData().getParticleHeight().get();
    deltaWidth = this.getEmitterData().getDeltaWidth().get();
    deltaHeight = this.getEmitterData().getDeltaHeight().get();

    Particle particle;
    switch (this.getEmitterData().getParticleType()) {
    case LeftLineParticle:
      particle = new LeftLineParticle(x, y, deltaX, deltaY, gravityX, gravityY, width, height, this.getRandomParticleTTL(), this.getRandomParticleColor());
      break;
    case OvalParticle:
      particle = new OvalParticle(x, y, deltaX, deltaY, gravityX, gravityY, width, height, this.getRandomParticleTTL(), this.getRandomParticleColor());
      break;
    case RectangleFillParticle:
      particle = new RectangleFillParticle(x, y, deltaX, deltaY, gravityX, gravityY, width, height, this.getRandomParticleTTL(), this.getRandomParticleColor());
      break;
    case LowQualityRectangleFillParticle:
      particle = new LowQualityRectangleFillParticle(x, y, deltaX, deltaY, gravityX, gravityY, width, height, this.getRandomParticleTTL(), this.getRandomParticleColor());
      break;
    case RectangleOutlineParticle:
      particle = new RectangleOutlineParticle(x, y, deltaX, deltaY, gravityX, gravityY, width, height, this.getRandomParticleTTL(), this.getRandomParticleColor());
      break;
    case RightLineParticle:
      particle = new RightLineParticle(x, y, deltaX, deltaY, gravityX, gravityY, width, height, this.getRandomParticleTTL(), this.getRandomParticleColor());
      break;
    case ShimmerParticle:
      particle = new ShimmerParticle(new Rectangle2D.Float(x, y, this.getWidth(), this.getHeight()), x, y, deltaX, deltaY, gravityX, gravityY, width, height, this.getRandomParticleTTL(), this.getRandomParticleColor());
      break;
    case TextParticle:
      particle = new TextParticle(this.getEmitterData().getParticleText(), x, y, deltaX, deltaY, gravityX, gravityY, this.getRandomParticleTTL(), this.getRandomParticleColor());
      break;
    default:
      particle = new RectangleFillParticle(x, y, deltaX, deltaY, gravityX, gravityY, width, height, this.getRandomParticleTTL(), this.getRandomParticleColor());
      break;
    }

    particle.setDeltaWidth(deltaWidth);
    particle.setDeltaHeight(deltaHeight);
    particle.setApplyPhysics(this.getEmitterData().isApplyingStaticPhysics());
    return particle;
  }

}
