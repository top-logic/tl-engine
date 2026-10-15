/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import com.top_logic.presentation.data.PresentationData;

/**
 * Description of a shape on a slide, layout or master of a presentation template.
 *
 * <p>
 * Fill, outline and shadow are the ones defined at the shape itself, either directly or by a
 * reference to the style matrix of the theme. Properties a placeholder inherits from its layout are
 * not reported here but at the {@link LayoutInfo#placeholders() layout placeholder}.
 * </p>
 *
 * @param kind
 *        The kind of shape.
 * @param name
 *        The name of the shape.
 * @param bounds
 *        The position and size on the slide in points, also for shapes in groups.
 * @param rotation
 *        The rotation in degrees.
 * @param geometry
 *        The outline geometry, <code>null</code> for shapes without one (groups, tables).
 * @param fill
 *        The fill, <code>null</code> if not defined.
 * @param line
 *        The outline, <code>null</code> if the shape has none.
 * @param shadow
 *        The outer shadow, <code>null</code> if the shape has none.
 * @param text
 *        The text, <code>null</code> if the shape has no text.
 * @param placeholder
 *        The placeholder this shape fills, <code>null</code> if it is no placeholder.
 * @param contentType
 *        The content type of the image of a {@link Kind#PICTURE}.
 * @param table
 *        The description of a {@link Kind#TABLE}.
 * @param children
 *        The shapes of a {@link Kind#GROUP}.
 */
public record ShapeInfo(Kind kind, String name, Rectangle2D bounds, double rotation, GeometryInfo geometry,
		PaintInfo fill, LineInfo line, ShadowInfo shadow, TextInfo text, PlaceholderRef placeholder,
		String contentType, TableInfo table, List<ShapeInfo> children) {

	/**
	 * Kinds of shapes.
	 */
	public enum Kind {

		/** A geometric shape, optionally with text. */
		SHAPE("shape"),

		/** A text box. */
		TEXT_BOX("textBox"),

		/** An image. */
		PICTURE("picture"),

		/** A table. */
		TABLE("table"),

		/** A group of shapes. */
		GROUP("group"),

		/** A chart. */
		CHART("chart"),

		/** A connector line. */
		CONNECTOR("connector"),

		/** Any other shape, e.g. an embedded object. */
		OTHER("other");

		private final String _id;

		Kind(String id) {
			_id = id;
		}

		/**
		 * The name of this kind in the data representation.
		 */
		public String id() {
			return _id;
		}
	}

	/**
	 * Reference of a shape to the placeholder it fills.
	 *
	 * @param type
	 *        The placeholder type, see {@link PlaceholderInfo#type()}.
	 * @param idx
	 *        The placeholder index, see {@link PlaceholderInfo#idx()}.
	 */
	public record PlaceholderRef(String type, long idx) {

		/**
		 * The data representation with the keys {@link PresentationData#TYPE} and
		 * {@link PresentationData#IDX}.
		 */
		public Map<String, Object> toData() {
			Map<String, Object> result = PresentationData.map();
			result.put(PresentationData.TYPE, type);
			result.put(PresentationData.IDX, Double.valueOf(idx));
			return result;
		}
	}

	/**
	 * The data representation with the keys {@link PresentationData#KIND}, {@link PresentationData#NAME},
	 * the bounds ({@link PresentationData#X}, {@link PresentationData#Y}, {@link PresentationData#WIDTH},
	 * {@link PresentationData#HEIGHT}), {@link PresentationData#ROTATION}, {@link PresentationData#GEOMETRY},
	 * {@link PresentationData#FILL}, {@link PresentationData#LINE}, {@link PresentationData#SHADOW},
	 * {@link PresentationData#TEXT}, {@link PresentationData#PLACEHOLDER},
	 * {@link PresentationData#CONTENT_TYPE}, {@link PresentationData#TABLE} and
	 * {@link PresentationData#CHILDREN}.
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = PresentationData.map();
		result.put(PresentationData.KIND, kind.id());
		PresentationData.put(result, PresentationData.NAME, name);
		PresentationData.putBounds(result, bounds);
		if (rotation != 0) {
			result.put(PresentationData.ROTATION, PresentationData.number(rotation));
		}
		PresentationData.put(result, PresentationData.GEOMETRY, geometry == null ? null : geometry.toData());
		PresentationData.put(result, PresentationData.FILL, fill == null ? null : fill.toData());
		PresentationData.put(result, PresentationData.LINE, line == null ? null : line.toData());
		PresentationData.put(result, PresentationData.SHADOW, shadow == null ? null : shadow.toData());
		PresentationData.put(result, PresentationData.TEXT, text == null ? null : text.toData());
		PresentationData.put(result, PresentationData.PLACEHOLDER, placeholder == null ? null : placeholder.toData());
		PresentationData.put(result, PresentationData.CONTENT_TYPE, contentType);
		PresentationData.put(result, PresentationData.TABLE, table == null ? null : table.toData());
		if (!children.isEmpty()) {
			result.put(PresentationData.CHILDREN, PresentationData.list(children, ShapeInfo::toData));
		}
		return result;
	}

}
