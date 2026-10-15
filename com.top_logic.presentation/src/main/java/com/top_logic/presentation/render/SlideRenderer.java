/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.apache.poi.common.usermodel.fonts.FontInfo;
import org.apache.poi.sl.draw.DrawFontManagerDefault;
import org.apache.poi.sl.draw.DrawPictureShape;
import org.apache.poi.sl.draw.DrawShape;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.draw.ImageRenderer;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.PictureShape;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.presentation.template.PlaceholderInfo;
import com.top_logic.presentation.text.TextDrawFactory;
import com.top_logic.presentation.text.TextMeasure;

/**
 * Renders slides, slide layouts and slide masters of a PowerPoint presentation into images.
 *
 * <p>
 * Rendering uses the drawing code of Apache POI. The graphics is configured with the
 * {@link TextMeasure#RENDERING_HINTS}, so text breaks into lines at the positions computed by
 * {@link TextMeasure}, and additionally with the {@link #QUALITY_HINTS}.
 * </p>
 *
 * <p>
 * A picture is drawn from its raster image (e.g. PNG), even if it has a vector alternative (SVG).
 * PowerPoint stores a raster image for each vector graphics, and the SVG icons of Office use
 * styling that the SVG renderer of POI does not support, so that they would be drawn empty.
 * </p>
 *
 * <p>
 * A font that is not installed is silently replaced by a standard font when drawing text. The
 * renderer records the names of all such fonts used while rendering, see
 * {@link #getMissingFonts()}.
 * </p>
 *
 * <p>
 * A renderer is not thread-safe, a new one is created for each rendering task.
 * </p>
 */
public final class SlideRenderer {

	/**
	 * The smallest image width in pixels.
	 */
	public static final int MIN_WIDTH = 16;

	/**
	 * The largest image width in pixels.
	 */
	public static final int MAX_WIDTH = 8000;

	/**
	 * The content type of the images created by {@link #toPng(BufferedImage, String)}.
	 */
	public static final String PNG_CONTENT_TYPE = "image/png";

	/**
	 * Rendering hints for the quality of shapes and images, which do not influence the text
	 * layout.
	 */
	public static final Map<RenderingHints.Key, Object> QUALITY_HINTS = Map.of(
		RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC,
		RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY,
		RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY,
		RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

	private final Set<String> _missingFonts = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

	private final RecordingFontManager _fontManager = new RecordingFontManager();

	private static final TextDrawFactory DRAW_FACTORY = new RasterFirstDrawFactory();

	/**
	 * Renders the given slide, layout or master into an image of the given width.
	 *
	 * <p>
	 * The image height follows from the width and the aspect ratio of the slide size. The image
	 * has an opaque white background, on which the background of the slide is drawn.
	 * </p>
	 *
	 * @param sheet
	 *        The slide, layout or master to render.
	 * @param width
	 *        The image width in pixels, see {@link #isValidWidth(int)}.
	 * @return The rendered image.
	 */
	public BufferedImage render(Sheet<?, ?> sheet, int width) {
		if (!isValidWidth(width)) {
			throw new IllegalArgumentException(
				"Image width must be between " + MIN_WIDTH + " and " + MAX_WIDTH + " pixels: " + width);
		}
		Dimension2D pageSize = sheet.getSlideShow().getPageSize();
		int height = height(pageSize, width);

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		try {
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, width, height);

			TextMeasure.applyRenderingHints(graphics);
			graphics.addRenderingHints(QUALITY_HINTS);
			graphics.setRenderingHint(Drawable.FONT_HANDLER, _fontManager);
			graphics.setRenderingHint(Drawable.DRAW_FACTORY, DRAW_FACTORY);

			graphics.scale(width / pageSize.getWidth(), height / pageSize.getHeight());
			sheet.draw(graphics);
		} finally {
			graphics.dispose();
		}
		return image;
	}

	/**
	 * The names of the fonts that were requested while rendering but are not installed.
	 *
	 * <p>
	 * Text in such a font has been drawn in a standard font. Its line breaks and size may differ
	 * from the presentation shown on a computer having the font.
	 * </p>
	 *
	 * @return The font names in alphabetical order, accumulated over all
	 *         {@link #render(Sheet, int)} calls of this renderer.
	 */
	public Set<String> getMissingFonts() {
		return Collections.unmodifiableSet(_missingFonts);
	}

	/**
	 * Whether the given image width can be rendered.
	 *
	 * @param width
	 *        The image width in pixels.
	 * @return Whether the width is between {@link #MIN_WIDTH} and {@link #MAX_WIDTH}.
	 */
	public static boolean isValidWidth(int width) {
		return width >= MIN_WIDTH && width <= MAX_WIDTH;
	}

	/**
	 * The height in pixels of an image of the given width showing a slide of the given size.
	 *
	 * @param pageSize
	 *        The slide size.
	 * @param width
	 *        The image width in pixels.
	 */
	public static int height(Dimension2D pageSize, int width) {
		return Math.max(1, (int) Math.round(width * pageSize.getHeight() / pageSize.getWidth()));
	}

	/**
	 * Creates a slide that shows how the given layout looks when used.
	 *
	 * <p>
	 * The slide is appended to the given presentation. It contains the placeholders of the layout
	 * filled with their prompt text. A placeholder without prompt text shows its type name as
	 * named in the file format, e.g. <code>body</code> or <code>pic</code>. This way, the text
	 * styles of all placeholders become visible.
	 * </p>
	 *
	 * @param slideShow
	 *        The presentation the layout belongs to.
	 * @param layout
	 *        The layout to show.
	 * @return The created slide.
	 */
	public static XSLFSlide createLayoutPreview(XMLSlideShow slideShow, XSLFSlideLayout layout) {
		XSLFSlide slide = slideShow.createSlide(layout);
		for (XSLFShape shape : slide.getShapes()) {
			if (shape instanceof XSLFTextShape text && text.getText().isBlank()) {
				String type = PlaceholderInfo.typeOf(shape);
				if (type != null) {
					text.setText(type);
				}
			}
		}
		return slide;
	}

	/**
	 * Encodes the given image as PNG.
	 *
	 * @param image
	 *        The image to encode.
	 * @param name
	 *        The name of the result, e.g. <code>slide-03.png</code>.
	 * @return The PNG data with content type {@link #PNG_CONTENT_TYPE}.
	 */
	public static BinaryData toPng(BufferedImage image, String name) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			if (!ImageIO.write(image, "png", buffer)) {
				throw new IllegalStateException("No PNG encoder available.");
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		return BinaryDataFactory.createBinaryData(buffer.toByteArray(), PNG_CONTENT_TYPE, name);
	}

	/**
	 * {@link DrawFontManagerDefault} recording the fonts that are not installed.
	 */
	private final class RecordingFontManager extends DrawFontManagerDefault {

		@Override
		public Font createAWTFont(Graphics2D graphics, FontInfo fontInfo, double fontSize, boolean bold,
				boolean italic) {
			String typeface = fontInfo.getTypeface();
			if (typeface != null && !TextMeasure.isFontAvailable(typeface)) {
				_missingFonts.add(typeface);
			}
			return super.createAWTFont(graphics, fontInfo, fontSize, bold, italic);
		}

	}

	/**
	 * {@link TextDrawFactory} drawing pictures from their raster image in preference to their
	 * vector alternative.
	 */
	private static final class RasterFirstDrawFactory extends TextDrawFactory {

		@Override
		public DrawPictureShape getDrawable(PictureShape<?, ?> shape) {
			return new RasterFirstPictureShape(shape);
		}

	}

	/**
	 * {@link DrawPictureShape} drawing the picture from its raster image, if it can be drawn,
	 * otherwise from its alternative.
	 */
	private static final class RasterFirstPictureShape extends DrawPictureShape {

		RasterFirstPictureShape(PictureShape<?, ?> shape) {
			super(shape);
		}

		@Override
		public void drawContent(Graphics2D graphics) {
			PictureShape<?, ?> shape = getShape();
			Rectangle2D anchor = DrawShape.getAnchor(graphics, shape);
			Insets insets = shape.getClipping();
			for (PictureData data : new PictureData[] { shape.getPictureData(), shape.getAlternativePictureData() }) {
				if (data == null) {
					continue;
				}
				String contentType = data.getContentType();
				ImageRenderer renderer = getImageRenderer(graphics, contentType);
				if (renderer.canRender(contentType)) {
					try {
						renderer.loadImage(data.getData(), contentType);
					} catch (IOException ex) {
						// Try the alternative.
						continue;
					}
					renderer.drawImage(graphics, anchor, insets);
					return;
				}
			}
		}

	}

}
