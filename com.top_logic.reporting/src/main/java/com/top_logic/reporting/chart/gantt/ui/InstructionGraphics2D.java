/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.ui;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pseudo vector graphic format. This class stores instruction calls to a {@link Graphics2D} to be
 * able to replay them later on another {@link Graphics2D} object.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class InstructionGraphics2D extends Graphics2D {

	/**
	 * This class represents an instruction step to a target graphic.
	 */
	public static abstract class Instruction {

		/**
		 * Executes the instruction step on the given target graphic.
		 */
		public abstract void execute(Graphics2D target);
	}

	/**
	 * Holds the instruction list to draw an image.<br/>
	 */
	private List<Instruction> _instructions;


	/** Flag whether this graphic object is already disposed (closed). */
	private boolean _disposed;



	/**
	 * Creates a new {@link InstructionGraphics2D}.
	 */
	public InstructionGraphics2D() {
		clear();
	}



	@Override
	public InstructionGraphics2D create() {
		return clone();
	}

	@Override
	public InstructionGraphics2D create(int x, int y, int width, int height) {
		return (InstructionGraphics2D) super.create(x, y, width, height);
	}

	@Override
	protected InstructionGraphics2D clone() {
		InstructionGraphics2D clone = new InstructionGraphics2D();
		clone._instructions = new ArrayList<>(_instructions);
		clone._disposed = false;
		return clone;
	}

	@Override
	public void dispose() {
		_disposed = true;
	}

	/**
	 * Clears the current {@link InstructionGraphics2D} and discards all recorded instructions.
	 */
	public void clear() {
		_instructions = new ArrayList<>();
		_disposed = false;
	}

	@Override
	public String toString() {
		return getClass().getName() + " [instruction count = " + _instructions.size() + "]";
	}



	/**
	 * Adds an image instruction to the instruction list.
	 * 
	 * @param instruction
	 *        the instruction to add; must not be <code>null</code>
	 * @throws IllegalArgumentException
	 *         if the graphic is already disposed
	 */
	private void addInstruction(Instruction instruction) throws IllegalArgumentException {
		if (_disposed) {
			throw new IllegalArgumentException("This graphic was already disposed. No modifying operations are allowed any more.");
		}
		_instructions.add(instruction);
	}


	/**
	 * Draws the image of this graphic object to the given graphic object by replaying the method
	 * calls which were recorded so far.
	 *
	 * @param graphics the {@link Graphics2D} on which to draw the recorded image.
	 */
	public void replay(Graphics2D graphics) {
		for (Instruction instruction : _instructions) {
			instruction.execute(graphics);
		}
	}

	@Override
	public void draw(final Shape s) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.draw(s);
			}
		});
	}

	@Override
	public void drawGlyphVector(final GlyphVector g, final float x, final float y) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawGlyphVector(g, x, y);
			}
		});
	}

	@Override
	public void drawImage(final BufferedImage img, final BufferedImageOp op, final int x, final int y) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawImage(img, op, x, y);
			}
		});
	}

	@Override
	public void drawRenderableImage(final RenderableImage img, final AffineTransform xform) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawRenderableImage(img, xform);
			}
		});
	}

	@Override
	public void drawRenderedImage(final RenderedImage img, final AffineTransform xform) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawRenderedImage(img, xform);
			}
		});
	}

	@Override
	public void drawString(final String str, final int x, final int y) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawString(str, x, y);
			}
		});
	}

	@Override
	public void drawString(final String str, final float x, final float y) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawString(str, x, y);
			}
		});
	}

	@Override
	public void drawString(final AttributedCharacterIterator iterator, final int x, final int y) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawString(iterator, x, y);
			}
		});
	}

	@Override
	public void drawString(final AttributedCharacterIterator iterator, final float x, final float y) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawString(iterator, x, y);
			}
		});
	}

	@Override
	public void fill(final Shape s) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.fill(s);
			}
		});
	}

	@Override
	public void rotate(final double theta) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.rotate(theta);
			}
		});
	}

	@Override
	public void rotate(final double theta, final double x, final double y) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.rotate(theta, x, y);
			}
		});
	}

	@Override
	public void scale(final double sx, final double sy) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.scale(sx, sy);
			}
		});
	}

	@Override
	public void shear(final double shx, final double shy) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.shear(shx, shy);
			}
		});
	}

	@Override
	public void transform(final AffineTransform Tx) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.transform(Tx);
			}
		});
	}

	@Override
	public void translate(final int x, final int y) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.translate(x, y);
			}
		});
	}

	@Override
	public void translate(final double tx, final double ty) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.translate(tx, ty);
			}
		});
	}

	@Override
	public void copyArea(final int x, final int y, final int width, final int height, final int dx, final int dy) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.copyArea(x, y, width, height, dx, dy);
			}
		});
	}

	@Override
	public void drawLine(final int x1, final int y1, final int x2, final int y2) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawLine(x1, y1, x2, y2);
			}
		});
	}

	@Override
	public void fillRect(final int x, final int y, final int width, final int height) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.fillRect(x, y, width, height);
			}
		});
	}

	@Override
	public void clearRect(final int x, final int y, final int width, final int height) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.clearRect(x, y, width, height);
			}
		});
	}

	@Override
	public void drawRoundRect(final int x, final int y, final int width, final int height, final int arcWidth,
			final int arcHeight) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
			}
		});
	}

	@Override
	public void fillRoundRect(final int x, final int y, final int width, final int height, final int arcWidth,
			final int arcHeight) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
			}
		});
	}

	@Override
	public void drawOval(final int x, final int y, final int width, final int height) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawOval(x, y, width, height);
			}
		});
	}

	@Override
	public void fillOval(final int x, final int y, final int width, final int height) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.fillOval(x, y, width, height);
			}
		});
	}

	@Override
	public void drawArc(final int x, final int y, final int width, final int height, final int startAngle,
			final int arcAngle) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawArc(x, y, width, height, startAngle, arcAngle);
			}
		});
	}

	@Override
	public void fillArc(final int x, final int y, final int width, final int height, final int startAngle,
			final int arcAngle) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.fillArc(x, y, width, height, startAngle, arcAngle);
			}
		});
	}

	@Override
	public void drawPolyline(final int[] xPoints, final int[] yPoints, final int nPoints) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawPolyline(xPoints, yPoints, nPoints);
			}
		});
	}

	@Override
	public void drawPolygon(final int[] xPoints, final int[] yPoints, final int nPoints) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawPolygon(xPoints, yPoints, nPoints);
			}
		});
	}

	@Override
	public void fillPolygon(final int[] xPoints, final int[] yPoints, final int nPoints) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.fillPolygon(xPoints, yPoints, nPoints);
			}
		});
	}



	@Override
	public void draw3DRect(final int x, final int y, final int width, final int height, final boolean raised) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.draw3DRect(x, y, width, height, raised);
			}
		});
	}

	@Override
	public void fill3DRect(final int x, final int y, final int width, final int height, final boolean raised) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.fill3DRect(x, y, width, height, raised);
			}
		});
	}

	@Override
	public void drawBytes(final byte[] data, final int offset, final int length, final int x, final int y) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawBytes(data, offset, length, x, y);
			}
		});
	}

	@Override
	public void drawChars(final char[] data, final int offset, final int length, final int x, final int y) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawChars(data, offset, length, x, y);
			}
		});
	}

	@Override
	public void drawPolygon(final Polygon p) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawPolygon(p);
			}
		});
	}

	@Override
	public void fillPolygon(final Polygon p) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.fillPolygon(p);
			}
		});
	}

	@Override
	public void drawRect(final int x, final int y, final int width, final int height) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawRect(x, y, width, height);
			}
		});
	}



	@Override
	public boolean drawImage(final Image img, final AffineTransform xform, final ImageObserver obs) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawImage(img, xform, obs);
			}
		});
		return true;
	}

	@Override
	public boolean drawImage(final Image img, final int x, final int y, final ImageObserver observer) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawImage(img, x, y, observer);
			}
		});
		return true;
	}

	@Override
	public boolean drawImage(final Image img, final int x, final int y, final int width, final int height,
			final ImageObserver observer) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawImage(img, x, y, width, height, observer);
			}
		});
		return true;
	}

	@Override
	public boolean drawImage(final Image img, final int x, final int y, final Color bgcolor,
			final ImageObserver observer) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawImage(img, x, y, bgcolor, observer);
			}
		});
		return true;
	}

	@Override
	public boolean drawImage(final Image img, final int x, final int y, final int width, final int height,
			final Color bgcolor, final ImageObserver observer) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawImage(img, x, y, width, height, bgcolor, observer);
			}
		});
		return true;
	}

	@Override
	public boolean drawImage(final Image img, final int dx1, final int dy1, final int dx2, final int dy2,
			final int sx1, final int sy1, final int sx2, final int sy2, final ImageObserver observer) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
			}
		});
		return true;
	}

	@Override
	public boolean drawImage(final Image img, final int dx1, final int dy1, final int dx2, final int dy2,
			final int sx1, final int sy1, final int sx2, final int sy2, final Color bgcolor,
			final ImageObserver observer) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
			}
		});
		return true;
	}



	@Override
	public void setColor(final Color c) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.setColor(c);
			}
		});
	}

	@Override
	public void setBackground(final Color color) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.setBackground(color);
			}
		});
	}

	@Override
	public void setComposite(final Composite comp) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.setComposite(comp);
			}
		});
	}

	@Override
	public void setPaintMode() {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.setPaintMode();
			}
		});
	}

	@Override
	public void setXORMode(final Color c1) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.setXORMode(c1);
			}
		});
	}

	@Override
	public void setPaint(final Paint paint) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.setPaint(paint);
			}
		});
	}

	@Override
	public void setRenderingHint(final Key hintKey, final Object hintValue) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.setRenderingHint(hintKey, hintValue);
			}
		});
	}

	@Override
	public void setRenderingHints(final Map<?, ?> hints) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.setRenderingHints(hints);
			}
		});
	}

	@Override
	public void addRenderingHints(final Map<?, ?> hints) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.addRenderingHints(hints);
			}
		});
	}

	@Override
	public void setStroke(final Stroke s) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.setStroke(s);
			}
		});
	}

	@Override
	public void setTransform(final AffineTransform Tx) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.setTransform(Tx);
			}
		});
	}

	@Override
	public void setFont(final Font font) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.setFont(font);
			}
		});
	}

	@Override
	public void clip(final Shape s) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.clip(s);
			}
		});
	}

	@Override
	public void setClip(final Shape clip) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.setClip(clip);
			}
		});
	}

	@Override
	public void setClip(final int x, final int y, final int width, final int height) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.setClip(x, y, width, height);
			}
		});
	}

	@Override
	public void clipRect(final int x, final int y, final int width, final int height) {
		addInstruction(new Instruction() {
			@Override
			public void execute(Graphics2D target) {
				target.clipRect(x, y, width, height);
			}
		});
	}



	@Override
	public Color getColor() {
		throw new UnsupportedOperationException("Getter methods are not supported by this class.");
	}

	@Override
	public Color getBackground() {
		throw new UnsupportedOperationException("Getter methods are not supported by this class.");
	}

	@Override
	public Composite getComposite() {
		throw new UnsupportedOperationException("Getter methods are not supported by this class.");
	}

	@Override
	public Paint getPaint() {
		throw new UnsupportedOperationException("Getter methods are not supported by this class.");
	}

	@Override
	public Object getRenderingHint(Key hintKey) {
		throw new UnsupportedOperationException("Getter methods are not supported by this class.");
	}

	@Override
	public RenderingHints getRenderingHints() {
		throw new UnsupportedOperationException("Getter methods are not supported by this class.");
	}

	@Override
	public Stroke getStroke() {
		throw new UnsupportedOperationException("Getter methods are not supported by this class.");
	}

	@Override
	public AffineTransform getTransform() {
		throw new UnsupportedOperationException("Getter methods are not supported by this class.");
	}

	@Override
	public Font getFont() {
		throw new UnsupportedOperationException("Getter methods are not supported by this class.");
	}

	@Override
	public Shape getClip() {
		throw new UnsupportedOperationException("Getter methods are not supported by this class.");
	}

	@Override
	public Rectangle getClipBounds() {
		throw new UnsupportedOperationException("Getter methods are not supported by this class.");
	}

	@Override
	public Rectangle getClipBounds(Rectangle r) {
		throw new UnsupportedOperationException("Getter methods are not supported by this class.");
	}

	@Override
	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		throw new UnsupportedOperationException("Getter methods are not supported by this class.");
	}

	@Override
	public boolean hitClip(int x, int y, int width, int height) {
		throw new UnsupportedOperationException("Getter methods are not supported by this class.");
	}



	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		throw new UnsupportedOperationException("Getter methods are not supported by this class.");
	}

	@Override
	public FontRenderContext getFontRenderContext() {
		throw new UnsupportedOperationException("Getter methods are not supported by this class.");
	}

	@Override
	public FontMetrics getFontMetrics() {
		throw new UnsupportedOperationException("Getter methods are not supported by this class.");
	}

	@Override
	public FontMetrics getFontMetrics(Font f) {
		throw new UnsupportedOperationException("Getter methods are not supported by this class.");
	}

}
