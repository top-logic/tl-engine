/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.server.svg;

import static com.top_logic.graphic.blocks.svg.SvgConstants.*;

import java.awt.Color;
import java.io.IOError;
import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.form.format.ColorFormat;
import com.top_logic.graphic.blocks.svg.SVGColor;
import com.top_logic.graphic.blocks.svg.SvgConstants;
import com.top_logic.graphic.blocks.svg.SvgUtil;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.ImageAlign;
import com.top_logic.graphic.flow.data.ImageScale;

/**
 * {@link SvgWriter} creating XML output through a {@link TagWriter}.
 *
 * <p>
 * The output is portable SVG that renderers limited to SVG 1.1 and CSS2 (such as Apache Batik)
 * display: A {@link SvgConstants#FILL_ATTR fill} or {@link SvgConstants#STROKE_ATTR stroke} color
 * is written as {@code #rrggbb}, its alpha value as {@link SvgConstants#FILL_OPACITY_ATTR
 * fill-opacity} or {@link SvgConstants#STROKE_OPACITY_ATTR stroke-opacity} (multiplied with an
 * opacity set explicitly on the same element), and a fully transparent color as {@code none}. A
 * paint value that is no color (e.g. {@code none}, {@code url(#g)} or {@code currentColor}) is
 * written unchanged. To combine color and opacity independent of the call order, the paint
 * attributes of an element are collected and written when its start tag is completed.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SvgTagWriter implements SvgWriter {

	private final TagWriter _out;

	private boolean _tagOpen;

	private boolean _inData;

	private boolean _firstData;

	private String _textContent;

	private final Paint _fill = new Paint(FILL_ATTR, FILL_OPACITY_ATTR);

	private final Paint _stroke = new Paint(STROKE_ATTR, STROKE_OPACITY_ATTR);

	/**
	 * Creates a {@link SvgTagWriter}.
	 *
	 */
	public SvgTagWriter(TagWriter out) {
		_out = out;
	}

	@Override
	public void beginSvg() {
		beginBeginTag(SVG);
		_out.writeAttribute("xmlns", SVG_NS);
		_out.writeAttribute(VERSION_ATTR, "1.1");
	}

	@Override
	public void dimensions(String width, String height, double x1, double y1, double x2, double y2) {
		_out.writeAttribute(WIDTH_ATTR, width);
		_out.writeAttribute(HEIGHT_ATTR, height);
		_out.writeAttribute(VIEWBOX_ATTR, x1 + " " + y1 + " " + x2 + " " + y2);
	}

	@Override
	public void endSvg() {
		endTag(SVG);
	}

	@Override
	public void style(CharSequence css) {
		endBeginTag();
		_out.beginTag(STYLE);
		_out.writeText(css);
		_out.endTag(STYLE);
	}

	@Override
	public void beginGroup(Object model) {
		beginBeginTag(G);
	}

	@Override
	public void translate(double dx, double dy) {
		_out.beginAttribute(TRANSFORM_ATTR);
		append(TRANSLATE_OP);
		append('(');
		append(dx);
		append(',');
		append(dy);
		append(')');
		_out.endAttribute();
	}

	@Override
	public void transform(double a, double b, double c, double d, double e, double f) {
		_out.beginAttribute(TRANSFORM_ATTR);
		append(MATRIX_OP);
		append('(');
		append(a);
		append(',');
		append(b);
		append(',');
		append(c);
		append(',');
		append(d);
		append(',');
		append(e);
		append(',');
		append(f);
		append(')');
		_out.endAttribute();
	}

	@Override
	public void writeCssClass(String cssClass) {
		_out.writeAttribute(CLASS_ATTR, cssClass);
	}

	@Override
	public void writePoints(String points) {
		_out.writeAttribute(POINTS_ATTR, points);
	}

	@Override
	public void writeId(String id) {
		_out.writeAttribute(ID_ATTR, id);
	}

	@Override
	public void endGroup() {
		endTag(G);
	}

	@Override
	public void beginPath(Object model) {
		beginBeginTag(PATH);
	}

	@Override
	public void beginPolyline(Object model) {
		beginBeginTag(POLYLINE);
	}

	@Override
	public void beginPolygon(Object model) {
		beginBeginTag(POLYGON);
	}

	@Override
	public void beginClipPath(Object model) {
		beginBeginTag(CLIP_PATH);
	}

	@Override
	public void endClipPath() {
		endTag(CLIP_PATH);
	}

	@Override
	public void beginData() {
		_out.beginAttribute(D_ATTR);
		_inData = true;
		_firstData = true;
	}

	@Override
	public void moveToRel(double dx, double dy) {
		beforeDataEntry();
		append('m');
		append(' ');
		append(dx);
		append(',');
		append(dy);
	}

	@Override
	public void moveToAbs(double x, double y) {
		beforeDataEntry();
		append('M');
		append(' ');
		append(x);
		append(',');
		append(y);
	}

	@Override
	public void lineToRel(double dx, double dy) {
		beforeDataEntry();
		append('l');
		append(' ');
		append(dx);
		append(',');
		append(dy);
	}

	@Override
	public void lineToAbs(double x, double y) {
		beforeDataEntry();
		append('L');
		append(' ');
		append(x);
		append(',');
		append(y);
	}

	@Override
	public void lineToHorizontalRel(double dx) {
		beforeDataEntry();
		append('h');
		append(' ');
		append(dx);
	}

	@Override
	public void lineToHorizontalAbs(double x) {
		beforeDataEntry();
		append('H');
		append(' ');
		append(x);
	}

	@Override
	public void lineToVerticalRel(double dy) {
		beforeDataEntry();
		append('v');
		append(' ');
		append(dy);
	}

	@Override
	public void lineToVerticalAbs(double y) {
		beforeDataEntry();
		append('V');
		append(' ');
		append(y);
	}

	@Override
	public void curveToRel(double dx1, double dy1, double dx2, double dy2, double dx, double dy) {
		beforeDataEntry();
		append('c');
		append(' ');
		append(dx1);
		append(',');
		append(dy1);
		append(' ');
		append(dx2);
		append(',');
		append(dy2);
		append(' ');
		append(dx);
		append(',');
		append(dy);
	}

	@Override
	public void curveToAbs(double x1, double y1, double x2, double y2, double x, double y) {
		beforeDataEntry();
		append('C');
		append(' ');
		append(x1);
		append(',');
		append(y1);
		append(' ');
		append(x2);
		append(',');
		append(y2);
		append(' ');
		append(x);
		append(',');
		append(y);
	}

	@Override
	public void closePath() {
		beforeDataEntry();
		append('z');
	}

	@Override
	public void beginRect(double x, double y, double w, double h) {
		beginBeginTag(RECT);
		writeAttribute(X_ATTR, x);
		writeAttribute(Y_ATTR, y);
		writeAttribute(WIDTH_ATTR, w);
		writeAttribute(HEIGHT_ATTR, h);
	}

	@Override
	public void beginRect(double x, double y, double w, double h, double rx, double ry) {
		beginRect(x, y, w, h);
		writeAttribute(RX_ATTR, rx);
		writeAttribute(RY_ATTR, ry);
	}

	@Override
	public void endRect() {
		endEmptyTag();
	}

	private void writeAttribute(String name, double value) {
		writeAttribute(name, Double.toString(value));
	}

	/**
	 * Writes an attribute to the current element.
	 *
	 * <p>
	 * The paint attributes {@link SvgConstants#FILL_ATTR fill}, {@link SvgConstants#STROKE_ATTR
	 * stroke}, {@link SvgConstants#FILL_OPACITY_ATTR fill-opacity} and
	 * {@link SvgConstants#STROKE_OPACITY_ATTR stroke-opacity} are normalized as if set through
	 * {@link #setFill(String)}, {@link #setStroke(String)}, {@link #setFillOpacity(double)} and
	 * {@link #setStrokeOpacity(double)}.
	 * </p>
	 */
	@Override
	public void writeAttribute(String name, String value) {
		switch (name) {
			case FILL_ATTR:
				setFill(value);
				break;
			case STROKE_ATTR:
				setStroke(value);
				break;
			case FILL_OPACITY_ATTR:
				_fill.setOpacity(value);
				break;
			case STROKE_OPACITY_ATTR:
				_stroke.setOpacity(value);
				break;
			default:
				_out.writeAttribute(name, value);
		}
	}

	@Override
	public void endData() {
		_inData = false;
		_out.endAttribute();
	}

	@Override
	public void endPath() {
		endTag(PATH);
	}

	@Override
	public void endPolygon() {
		endTag(POLYGON);
	}

	@Override
	public void endPolyline() {
		endTag(POLYLINE);
	}

	@Override
	public void beginText(double x, double y, String text) {
		beginBeginTag(TEXT);
		writeAttribute(X_ATTR, x);
		writeAttribute(Y_ATTR, y);
		_textContent = text;
	}
	
	@Override
	public void setTextStyle(String fontFamily, String fontSize, String fontWeight) {
		writeAttribute(FONT_FAMILY_ATTR, fontFamily);
		writeAttribute(FONT_SIZE_ATTR, fontSize);
		writeAttribute(FONT_WEIGHT_ATTR, fontWeight);
	}

	@Override
	public void endText() {
		endBeginTag();
		_out.writeText(_textContent);
		endTag(TEXT);

		_textContent = null;
	}

	@Override
	public void image(double x, double y, double width, double height, String href, ImageAlign align,
			ImageScale scale) {
		beginBeginTag(IMAGE);
		writeAttribute(X_ATTR, x);
		writeAttribute(Y_ATTR, y);
		writeAttribute(WIDTH_ATTR, width);
		writeAttribute(HEIGHT_ATTR, height);
		writeAttribute(HREF_ATTR, href);
		writeAttribute(PRESERVE_ASPECT_RATIO_ATTR, align.protocolName() + " " + scale.protocolName());
		endEmptyTag();
	}

	private void beginBeginTag(String tagName) {
		endBeginTag();
		_out.beginBeginTag(tagName);
		_tagOpen = true;
	}

	/**
	 * Called when a begin tag is implicitly closed, because an inner tag is started (or the element
	 * is closed).
	 */
	protected void endBeginTag() {
		if (_tagOpen) {
			writePaint();
			_out.endBeginTag();
			_tagOpen = false;
		}
	}

	private void endEmptyTag() {
		if (_tagOpen) {
			writePaint();
			_out.endEmptyTag();
			_tagOpen = false;
		}
	}

	/**
	 * Writes the paint attributes collected for the current element, before its start tag is
	 * completed.
	 */
	private void writePaint() {
		_fill.write(_out);
		_stroke.write(_out);
	}

	private void endTag(String tagName) {
		endBeginTag();
		_out.endTag(tagName);
	}

	private void beforeDataEntry() {
		if (!_inData) {
			throw new IllegalStateException("Dot within path data");
		}
		if (_firstData) {
			_firstData = false;
		} else {
			append(' ');
		}
	}

	private void append(String s) {
		try {
			_out.append(s);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	private void append(char c) {
		try {
			_out.append(c);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	private void append(double value) {
		append(Double.toString(value));
	}

	@Override
	public void close() {
		// Ignore.
	}

	@Override
	public void setFillOpacity(double value) {
		_fill.setOpacity(value);
	}

	@Override
	public void setStrokeOpacity(double value) {
		_stroke.setOpacity(value);
	}

	@Override
	public void setStrokeWidth(double value) {
		_out.writeAttribute(STROKE_WIDTH_ATTR, Double.toString(value));
	}

	@Override
	public void setFill(SVGColor color) {
		setFill(SvgUtil.html(color));
	}

	@Override
	public void setFill(String style) {
		_fill.setPaint(style);
	}

	@Override
	public void setStroke(SVGColor color) {
		setStroke(SvgUtil.html(color));
	}

	@Override
	public void setStroke(String style) {
		_stroke.setPaint(style);
	}

	@Override
	public void setStrokeDasharray(double... dashes) {
		_out.writeAttribute(STROKE_DASHARRAY_ATTR, SvgUtil.valueList(dashes));
	}

	/**
	 * The paint ({@link SvgConstants#FILL_ATTR fill} or {@link SvgConstants#STROKE_ATTR stroke})
	 * of the current element, collected until its start tag is completed.
	 */
	private static final class Paint {

		/**
		 * Number of decimal places of a written opacity.
		 */
		private static final double OPACITY_PRECISION = 1000.0;

		private static final String NONE = "none";

		private final String _paintAttr;

		private final String _opacityAttr;

		private String _paint;

		private double _alpha = 1.0;

		private Double _opacity;

		private String _unparsedOpacity;

		/**
		 * Creates a {@link Paint}.
		 *
		 * @param paintAttr
		 *        The attribute receiving the paint.
		 * @param opacityAttr
		 *        The attribute receiving the opacity of the paint.
		 */
		Paint(String paintAttr, String opacityAttr) {
			_paintAttr = paintAttr;
			_opacityAttr = opacityAttr;
		}

		/**
		 * Sets the paint, a color in any notation of {@link ColorFormat}, or another paint value
		 * that is written unchanged.
		 */
		void setPaint(String paint) {
			Color color = paint == null ? null : ColorFormat.parseColorOrNull(paint.trim());
			if (color == null) {
				_paint = paint;
				_alpha = 1.0;
			} else if (color.getAlpha() == 0) {
				_paint = NONE;
				_alpha = 1.0;
			} else {
				_paint = ColorFormat.formatColor(new Color(color.getRGB() & 0xFFFFFF));
				_alpha = color.getAlpha() / 255.0;
			}
		}

		/**
		 * Sets an explicit opacity, multiplied with the alpha value of the paint color.
		 */
		void setOpacity(double opacity) {
			_opacity = Double.valueOf(opacity);
			_unparsedOpacity = null;
		}

		/**
		 * Sets an explicit opacity given as attribute value.
		 * 
		 * <p>
		 * A value that is no number is written unchanged and not combined with the alpha value of
		 * the paint color.
		 * </p>
		 */
		void setOpacity(String opacity) {
			_opacity = null;
			_unparsedOpacity = null;
			if (opacity != null) {
				try {
					_opacity = Double.valueOf(opacity.trim());
				} catch (NumberFormatException ex) {
					_unparsedOpacity = opacity;
				}
			}
		}

		/**
		 * Writes the collected attributes and resets this {@link Paint} for the next element.
		 */
		void write(TagWriter out) {
			out.writeAttribute(_paintAttr, _paint);
			if (_opacity != null) {
				out.writeAttribute(_opacityAttr, format(_opacity.doubleValue() * _alpha));
			} else if (_unparsedOpacity != null) {
				out.writeAttribute(_opacityAttr, _unparsedOpacity);
			} else if (_alpha < 1.0) {
				out.writeAttribute(_opacityAttr, format(_alpha));
			}

			_paint = null;
			_alpha = 1.0;
			_opacity = null;
			_unparsedOpacity = null;
		}

		private static String format(double opacity) {
			return Double.toString(Math.round(opacity * OPACITY_PRECISION) / OPACITY_PRECISION);
		}
	}
}
