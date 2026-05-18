/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.ui;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.TextMetricsImpl;

/**
 * {@link RenderContext} based on AWT {@link Font}s.
 *
 * <p>
 * Unit model: AWT's {@link Font#deriveFont(float)} interprets its size argument as <b>points</b>
 * (Java convention: 1 pt = 1/72 inch). Consequently, {@link Font#getStringBounds} returns its
 * width/height in user-space units that correspond to points at the default 72 DPI
 * {@link FontRenderContext}. SVG and browsers, on the other hand, work in <b>CSS pixels</b> at 96
 * DPI. Without a conversion, a box sized to the raw AWT measurement (in pt) would be displayed at
 * only {@code 72/96 = 75 %} of the rendered text's physical size, and the text would overflow.
 * </p>
 *
 * <p>
 * Therefore, measurements are converted from pt to CSS px via {@link #PX_PER_PT} before being
 * returned. The caller (e.g.
 * {@code com.top_logic.graphic.flow.server.script.FlowFactory#toSvg}) is expected to write a
 * matching {@code font-size} (in px) on the rendered {@code <text>} element (or as a default CSS
 * rule) so that measurement and rendering stay in lock-step.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AWTContext implements RenderContext {

	/**
	 * Conversion factor from points (AWT's font unit) to CSS pixels (the SVG/browser unit) at the
	 * default 96 DPI.
	 */
	public static final double PX_PER_PT = 96.0 / 72.0;

	/**
	 * Conversion factor from CSS pixels (SVG/browser unit) to points (AWT's font unit) at the
	 * default 96 DPI.
	 */
	public static final double PT_PER_PX = 72.0 / 96.0;

	private final float _defaultSizePt;

	private final FontRenderContext _fontRenderContext;

	private final Map<FontKey, Font> _fontCache = new HashMap<>();

	/**
	 * Creates a {@link AWTContext}.
	 *
	 * @param textSize
	 *        The default font size in points used to measure texts that have no explicit
	 *        {@code font-size} set.
	 */
	public AWTContext(float textSize) {
		_defaultSizePt = textSize;
		Graphics2D graphics = (Graphics2D) new BufferedImage(8, 8, BufferedImage.TYPE_4BYTE_ABGR).getGraphics();
		_fontRenderContext = graphics.getFontRenderContext();
	}

	/**
	 * The default font size in CSS pixels used by this context when no per-text
	 * {@code font-size} is set.
	 */
	public double getDefaultSizePx() {
		return _defaultSizePt * PX_PER_PT;
	}

	/**
	 * Extra horizontal padding (in CSS px) added to AWT-measured text widths.
	 *
	 * <p>
	 * {@link Font#getStringBounds} returns the advance width (where the next glyph would be
	 * placed). The visible ink ends a bit before that — by a font-design-dependent right-side
	 * bearing. For Arial Regular that bearing is comfortably wide (~1 px at 16 CSS px), so the
	 * text appears to sit inside its box with visible margin. For Arial Bold the bearing is much
	 * smaller (&lt; 0.5 px), and once the browser adds its sub-pixel anti-aliasing the bold ink
	 * appears to touch (or graze) the right edge of the box. This margin compensates for that
	 * cross-engine rendering wobble, so the visible box-vs-ink relation stays consistent across
	 * font weights.
	 * </p>
	 */
	private static final double WIDTH_SAFETY_PX = 1.0;

	@Override
	public TextMetricsImpl measure(String text, String fontFamily, String fontSize, String fontWeight) {
		Font font = font(fontFamily, fontSize, fontWeight);
		LineMetrics metrics = font.getLineMetrics(text, _fontRenderContext);
		Rectangle2D bounds = font.getStringBounds(text, _fontRenderContext);

		// Convert from AWT points to CSS pixels — see class JavaDoc.
		return new TextMetricsImpl(
			bounds.getWidth() * PX_PER_PT + WIDTH_SAFETY_PX,
			bounds.getHeight() * PX_PER_PT,
			metrics.getAscent() * PX_PER_PT);
	}

	private Font font(String fontFamily, String fontSize, String fontWeight) {
		String family = fontFamily != null ? fontFamily : DEFAULT_FONT_FAMILY;
		float sizePt = fontSize != null ? parseSizePt(fontSize) : _defaultSizePt;
		int style = isBold(fontWeight) ? Font.BOLD : Font.PLAIN;

		FontKey key = new FontKey(family, sizePt, style);
		// Construct the Font with the requested style up-front (instead of deriving from a PLAIN
		// font), so that AWT picks the real bold/italic face — Font.decode(name).deriveFont(style,
		// size) tends to synthesize bold by thickening strokes rather than loading the actual
		// bold font, which underestimates the width of CSS bold renderings in the browser.
		return _fontCache.computeIfAbsent(key,
			k -> new Font(k.family, k.style, 1).deriveFont(k.sizePt));
	}

	/**
	 * Parses an SVG/CSS {@code font-size} value and returns the equivalent size in points (the
	 * unit AWT expects).
	 *
	 * <p>
	 * Supported syntax: a number optionally followed by {@code px} or {@code pt}. A plain number
	 * is interpreted as SVG user units (= CSS pixels) and converted to points.
	 * </p>
	 */
	static float parseSizePt(String fontSize) {
		String s = fontSize.trim();
		if (s.endsWith("pt")) {
			// Already in AWT's native unit — no conversion.
			return Float.parseFloat(s.substring(0, s.length() - 2).trim());
		}
		if (s.endsWith("px")) {
			// SVG/CSS px → AWT pt.
			return (float) (Float.parseFloat(s.substring(0, s.length() - 2).trim()) * PT_PER_PX);
		}
		// Unitless: SVG user units = CSS px → AWT pt.
		return (float) (Float.parseFloat(s) * PT_PER_PX);
	}

	/**
	 * Whether the given SVG/CSS {@code font-weight} value represents a bold weight.
	 */
	static boolean isBold(String fontWeight) {
		if (fontWeight == null) {
			return false;
		}
		String w = fontWeight.trim().toLowerCase();
		if (w.equals("bold") || w.equals("bolder")) {
			return true;
		}
		try {
			return Integer.parseInt(w) >= 600;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	private static final class FontKey {
		final String family;

		final float sizePt;

		final int style;

		FontKey(String family, float sizePt, int style) {
			this.family = family;
			this.sizePt = sizePt;
			this.style = style;
		}

		@Override
		public int hashCode() {
			return Objects.hash(family, sizePt, style);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof FontKey)) {
				return false;
			}
			FontKey o = (FontKey) other;
			return style == o.style && Float.compare(sizePt, o.sizePt) == 0 && family.equals(o.family);
		}
	}

}
