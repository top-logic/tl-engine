/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.svg;

/**
 * Constants for creating SVG images.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SvgConstants {

	/**
	 * Embedded SVG image.
	 */
	String SVG = "svg";

	/**
	 * SVG namespace.
	 */
	String SVG_NS = "http://www.w3.org/2000/svg";

	/**
	 * Version attribute.
	 */
	String VERSION_ATTR = "version";

	/**
	 * View-box attribute.
	 */
	String VIEWBOX_ATTR = "viewBox";

	/**
	 * With of the viewport.
	 */
	String WIDTH_ATTR = "width";

	/**
	 * Height of the viewport.
	 */
	String HEIGHT_ATTR = "height";

	/**
	 * URL of a resource.
	 */
	String HREF_ATTR = "href";

	/**
	 * Image rendering hint.
	 */
	String PRESERVE_ASPECT_RATIO_ATTR = "preserveAspectRatio";

	/**
	 * Fill opacity style.
	 */
	String FILL_OPACITY_ATTR = "fill-opacity";

	/**
	 * Stroke opacity style.
	 */
	String STROKE_OPACITY_ATTR = "stroke-opacity";

	/**
	 * Stroke width style.
	 */
	String STROKE_WIDTH_ATTR = "stroke-width";

	/**
	 * Fill style.
	 */
	String FILL_ATTR = "fill";

	/**
	 * Stroke style.
	 */
	String STROKE_ATTR = "stroke";

	/**
	 * Font family.
	 */
	String FONT_FAMILY_ATTR = "font-family";

	/**
	 * Font size.
	 */
	String FONT_SIZE_ATTR = "font-size";

	/**
	 * Font weight.
	 */
	String FONT_WEIGHT_ATTR = "font-weight";

	/**
	 * Stroke-dasharray style.
	 */
	String STROKE_DASHARRAY_ATTR = "stroke-dasharray";

	/**
	 * Group.
	 */
	String G = "g";

	/**
	 * Element carrying CSS rules that apply within the SVG document.
	 */
	String STYLE = "style";

	/**
	 * Identifier attribute.
	 */
	String ID_ATTR = "id";

	/**
	 * Attribute referencing the {@link #CLIP_PATH} element that clips an element.
	 */
	String CLIP_PATH_ATTR = "clip-path";

	/**
	 * CSS class attribute.
	 */
	String CLASS_ATTR = "class";

	/**
	 * Transform attribute.
	 */
	String TRANSFORM_ATTR = "transform";

	/**
	 * Operator defining a translation transformation.
	 */
	String TRANSLATE_OP = "translate";

	/**
	 * Operator defining a matrix transformation.
	 */
	String MATRIX_OP = "matrix";

	/**
	 * Rectangle element.
	 */
	String RECT = "rect";

	/**
	 * X coordinate.
	 */
	String X_ATTR = "x";

	/**
	 * Y coordinate.
	 */
	String Y_ATTR = "y";

	/**
	 * X radius.
	 */
	String RX_ATTR = "rx";

	/**
	 * Y radius.
	 */
	String RY_ATTR = "ry";

	/**
	 * Text element.
	 */
	String TEXT = "text";

	/**
	 * Path element.
	 */
	String PATH = "path";

	/**
	 * Path data attribute.
	 */
	String D_ATTR = "d";

	/**
	 * Image element.
	 */
	String IMAGE = "image";

	/**
	 * Polygon element.
	 */
	String POLYGON = "polygon";

	/**
	 * Polyline element.
	 */
	String POLYLINE = "polyline";

	/**
	 * {@link #POLYGON} and {@link #POLYLINE} points attribute.
	 */
	String POINTS_ATTR = "points";

	/**
	 * <code>clipPath</code> element.
	 */
	String CLIP_PATH = "clipPath";

	/**
	 * Attribute making an element focusable, see {@link #TABINDEX_FOCUSABLE}.
	 */
	String TABINDEX_ATTR = "tabindex";

	/**
	 * Value of {@link #TABINDEX_ATTR} adding an element to the keyboard focus order.
	 */
	String TABINDEX_FOCUSABLE = "0";

	/**
	 * Attribute giving an element its accessibility role, e.g. {@link #ROLE_BUTTON}.
	 */
	String ROLE_ATTR = "role";

	/**
	 * Value of {@link #ROLE_ATTR} for an element acting as a button.
	 */
	String ROLE_BUTTON = "button";

	/**
	 * Attribute telling whether the content controlled by an element is expanded.
	 */
	String ARIA_EXPANDED_ATTR = "aria-expanded";

	/**
	 * Attribute naming the element whose text is the accessible name of an element.
	 */
	String ARIA_LABELLEDBY_ATTR = "aria-labelledby";
}
