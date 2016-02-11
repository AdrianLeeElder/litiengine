package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;

public class RightLineParticle extends Particle {

  public RightLineParticle(final float xCurrent, final float yCurrent, final float dx, final float dy, final float gravityX, final float gravityY, final byte width, final byte height, final int life, final Color color) {
    super(xCurrent, yCurrent, dx, dy, gravityX, gravityY, width, height, life, color);
  }

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    final Point2D renderLocation = this.getLocation(Game.getScreenManager().getCamera().getViewPortLocation(emitterOrigin));
    g.setColor(this.getColor());
    g.draw(new Line2D.Double(renderLocation.getX(), renderLocation.getY(), renderLocation.getX() + this.getWidth(), renderLocation.getY() + this.getHeight()));
  }
}