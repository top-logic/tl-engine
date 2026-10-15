/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.util.List;
import java.util.Map;

/**
 * The fill of a shape, a line or a background in a presentation template.
 *
 * @param type
 *        The kind of fill.
 * @param color
 *        The color of a {@link Type#SOLID} fill, or the foreground color of a {@link Type#PATTERN}
 *        fill.
 * @param backgroundColor
 *        The background color of a {@link Type#PATTERN} fill.
 * @param stops
 *        The color stops of a {@link Type#GRADIENT} fill.
 * @param angle
 *        The angle in degrees of a linear {@link Type#GRADIENT} fill.
 * @param path
 *        The path shape (<code>circle</code>, <code>rect</code>, <code>shape</code>) of a non-linear
 *        {@link Type#GRADIENT} fill, or the preset name of a {@link Type#PATTERN} fill.
 * @param contentType
 *        The content type of the image of a {@link Type#PICTURE} fill.
 * @param opacity
 *        The opacity of the image of a {@link Type#PICTURE} fill, if it is not opaque.
 */
public record PaintInfo(Type type, ColorInfo color, ColorInfo backgroundColor, List<GradientStop> stops,
		Double angle, String path, String contentType, Double opacity) {

	/**
	 * Kinds of fills.
	 */
	public enum Type {

		/** No fill at all. */
		NONE("none"),

		/** A single color. */
		SOLID("solid"),

		/** A color gradient. */
		GRADIENT("gradient"),

		/** An image. */
		PICTURE("picture"),

		/** A two-colored pattern. */
		PATTERN("pattern"),

		/** The fill of the group the shape belongs to. */
		GROUP("group");

		private final String _id;

		Type(String id) {
			_id = id;
		}

		/**
		 * The name of this fill type in the data representation.
		 */
		public String id() {
			return _id;
		}
	}

	/**
	 * A color at a position of a gradient.
	 *
	 * @param position
	 *        The position along the gradient, between 0 and 1.
	 * @param color
	 *        The color at this position.
	 */
	public record GradientStop(double position, ColorInfo color) {

		/**
		 * The data representation with the keys {@link TemplateData#POSITION} and
		 * {@link TemplateData#COLOR}.
		 */
		public Map<String, Object> toData() {
			Map<String, Object> result = TemplateData.map();
			result.put(TemplateData.POSITION, TemplateData.fraction(position));
			TemplateData.put(result, TemplateData.COLOR, color == null ? null : color.toData());
			return result;
		}
	}

	/**
	 * Creates a fill of the given type without further properties.
	 */
	public static PaintInfo of(Type type) {
		return new PaintInfo(type, null, null, List.of(), null, null, null, null);
	}

	/**
	 * Creates a {@link Type#SOLID} fill.
	 */
	public static PaintInfo solid(ColorInfo color) {
		return new PaintInfo(Type.SOLID, color, null, List.of(), null, null, null, null);
	}

	/**
	 * Whether this fill paints nothing.
	 */
	public boolean isNone() {
		return type == Type.NONE;
	}

	/**
	 * The data representation with the keys {@link TemplateData#TYPE}, {@link TemplateData#COLOR},
	 * {@link TemplateData#BACKGROUND_COLOR}, {@link TemplateData#STOPS},
	 * {@link TemplateData#ANGLE}, {@link TemplateData#PATH}, {@link TemplateData#CONTENT_TYPE} and
	 * {@link TemplateData#OPACITY}.
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = TemplateData.map();
		result.put(TemplateData.TYPE, type.id());
		TemplateData.put(result, TemplateData.COLOR, color == null ? null : color.toData());
		TemplateData.put(result, TemplateData.BACKGROUND_COLOR,
			backgroundColor == null ? null : backgroundColor.toData());
		if (!stops.isEmpty()) {
			result.put(TemplateData.STOPS, TemplateData.list(stops, GradientStop::toData));
		}
		TemplateData.put(result, TemplateData.ANGLE, angle == null ? null : TemplateData.number(angle));
		TemplateData.put(result, TemplateData.PATH, path);
		TemplateData.put(result, TemplateData.CONTENT_TYPE, contentType);
		TemplateData.put(result, TemplateData.OPACITY, opacity == null ? null : TemplateData.fraction(opacity));
		return result;
	}

}
