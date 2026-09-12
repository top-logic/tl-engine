/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.TextAttribute;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.batik.bridge.StrokingTextPainter;
import org.apache.batik.bridge.TextSpanLayout;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.text.TextPaintInfo;

/**
 * {@link StrokingTextPainter} that draws text with
 * {@link Graphics2D#drawString(String, float, float)} instead of filling its glyph outlines.
 *
 * <p>
 * The inherited painter renders a text run by filling the outline of its glyphs. On a
 * {@link Graphics2D} that records PDF drawing operators, the result is a set of vector contours: it
 * looks like the text, but the document holds no characters, so the text cannot be selected,
 * copied, or searched. Drawing through {@link Graphics2D#drawString(String, float, float)} makes
 * the same run arrive as text and lets the target embed the font.
 * </p>
 *
 * <p>
 * Not every run can be expressed as a single string drawn at one position. A run placed along a
 * path, one laid out vertically or right-to-left, one whose glyphs are individually positioned, and
 * one painted with an outline or a non-uniform fill all keep their outline rendering — as does a
 * run whose text would not land at the position the layout computed for it. Such a run is passed
 * back to the inherited painter, so its appearance is unchanged.
 * </p>
 *
 * @author <a href="mailto:bernhard.haumacher@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectableTextPainter extends StrokingTextPainter {

	/**
	 * Largest deviation between the advance of the drawn string and the advance the layout
	 * computed, as a fraction of the latter, up to which both are considered to occupy the same
	 * space.
	 *
	 * <p>
	 * The two are measured through different font APIs and a different
	 * {@link java.awt.font.FontRenderContext}, so they differ slightly even for the same font. The
	 * tolerance separates that noise from an actually different font, whose text would not cover
	 * the space reserved for it.
	 * </p>
	 */
	private static final double ADVANCE_TOLERANCE = 0.02;

	/**
	 * Smallest absolute deviation tolerated regardless of {@link #ADVANCE_TOLERANCE}, so that very
	 * short runs are not rejected over a fraction of a unit.
	 */
	private static final double ADVANCE_TOLERANCE_MIN = 0.5;

	/**
	 * The {@link TextAttribute}s of a text run that describe the font to draw it with.
	 *
	 * @implNote {@link TextAttribute#FAMILY} is absent: the resolved font family is not among a
	 *           run's {@link TextAttribute}s, it is carried by the {@link #GVT_FONT} attribute.
	 */
	private static final List<TextAttribute> FONT_ATTRIBUTES =
		List.of(TextAttribute.SIZE, TextAttribute.WEIGHT, TextAttribute.POSTURE, TextAttribute.WIDTH);

	/** Singleton {@link SelectableTextPainter} instance. */
	public static final SelectableTextPainter INSTANCE = new SelectableTextPainter();

	private SelectableTextPainter() {
		// Singleton.
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void paintTextRuns(List textRuns, Graphics2D g2d) {
		for (Object entry : textRuns) {
			TextRun run = (TextRun) entry;
			if (!drawAsText(run, g2d)) {
				super.paintTextRuns(Collections.singletonList(run), g2d);
			}
		}
	}

	/**
	 * Draws the given run as text.
	 *
	 * @return Whether the run was drawn. A run this painter cannot express as text is left
	 *         untouched, so that the caller can hand it to the inherited painter.
	 */
	private boolean drawAsText(TextRun run, Graphics2D g2d) {
		TextSpanLayout layout = run.getLayout();
		if (layout.isOnATextPath() || layout.isVertical() || !layout.isLeftToRight()) {
			return false;
		}
		if (run.getLength() != null) {
			// A run stretched or squeezed to a requested length positions its glyphs
			// individually.
			return false;
		}

		AttributedCharacterIterator aci = run.getACI();
		aci.first();
		TextPaintInfo painting = (TextPaintInfo) aci.getAttribute(PAINT_INFO);
		if (painting == null) {
			return false;
		}
		if (!painting.visible) {
			// Nothing to draw, but nothing for the inherited painter to draw either.
			return true;
		}
		if (painting.strokePaint != null) {
			// Only the outline carries the stroke around the glyphs.
			return false;
		}
		if (!(painting.fillPaint instanceof Color)) {
			// A gradient or pattern fill cannot be expressed as a text color.
			return false;
		}

		// Resolve the font before consuming the iterator: text() leaves it past its end, where no
		// attributes are defined.
		Font font = font(aci);
		if (font == null) {
			return false;
		}
		String text = text(aci);
		if (text.isEmpty()) {
			return true;
		}
		if (!sameAdvance(font, text, layout, g2d)) {
			// The font resolved for drawing measures differently from the one the layout was
			// computed with, so the text would not cover the space reserved for it.
			return false;
		}

		Composite composite = g2d.getComposite();
		Paint paint = g2d.getPaint();
		Font current = g2d.getFont();
		try {
			if (painting.composite != null) {
				g2d.setComposite(painting.composite);
			}
			g2d.setPaint(painting.fillPaint);
			g2d.setFont(font);

			Point2D origin = layout.getOffset();
			g2d.drawString(text, (float) origin.getX(), (float) origin.getY());
		} finally {
			g2d.setFont(current);
			g2d.setPaint(paint);
			g2d.setComposite(composite);
		}
		return true;
	}

	/**
	 * The font the given run is laid out with.
	 *
	 * @return The font, or <code>null</code> if the run names none.
	 */
	private static Font font(AttributedCharacterIterator aci) {
		GVTFont laidOutWith = (GVTFont) aci.getAttribute(GVT_FONT);
		if (laidOutWith == null) {
			return null;
		}

		Map<AttributedCharacterIterator.Attribute, Object> attributes = new HashMap<>();
		for (TextAttribute attribute : FONT_ATTRIBUTES) {
			Object value = aci.getAttribute(attribute);
			if (value != null) {
				attributes.put(attribute, value);
			}
		}
		attributes.put(TextAttribute.FAMILY, laidOutWith.getFamilyName());
		return Font.getFont(attributes);
	}

	/**
	 * Whether the given text, drawn in the given font, advances as far as the given layout.
	 */
	private static boolean sameAdvance(Font font, String text, TextSpanLayout layout, Graphics2D g2d) {
		double drawn = font.getStringBounds(text, g2d.getFontRenderContext()).getWidth();
		double reserved = layout.getAdvance2D().getX();
		return Math.abs(drawn - reserved) <= Math.max(ADVANCE_TOLERANCE_MIN, ADVANCE_TOLERANCE * reserved);
	}

	/**
	 * The characters the given iterator runs over.
	 */
	private static String text(AttributedCharacterIterator aci) {
		StringBuilder result = new StringBuilder(aci.getEndIndex() - aci.getBeginIndex());
		for (char c = aci.first(); c != AttributedCharacterIterator.DONE; c = aci.next()) {
			result.append(c);
		}
		return result.toString();
	}

}
