/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Keys and helpers for the data representation of a {@link TemplateAnalysis}.
 *
 * <p>
 * The data representation consists of dictionaries with a stable key order, lists, strings,
 * numbers ({@link Double}) and booleans only, so that it can be serialized to JSON. Entries without
 * a value are left out. All lengths are given in points.
 * </p>
 */
public final class TemplateData {

	/** Key of the slide size of a presentation. */
	public static final String SLIDE_SIZE = "slideSize";

	/** Key of the slide masters of a presentation. */
	public static final String MASTERS = "masters";

	/** Key of the slide layouts of a presentation. */
	public static final String LAYOUTS = "layouts";

	/** Key of the slides of a presentation. */
	public static final String SLIDES = "slides";

	/** Key of the horizontal position of a shape. */
	public static final String X = "x";

	/** Key of the vertical position of a shape. */
	public static final String Y = "y";

	/** Key of a width. */
	public static final String WIDTH = "width";

	/** Key of a height. */
	public static final String HEIGHT = "height";

	/** Key of the position of an element in its list. */
	public static final String INDEX = "index";

	/** Key of a name. */
	public static final String NAME = "name";

	/** Key of a type. */
	public static final String TYPE = "type";

	/** Key of the slide master a layout belongs to. */
	public static final String MASTER = "master";

	/** Key of the layout a slide is based on. */
	public static final String LAYOUT = "layout";

	/** Key of the theme of a slide master. */
	public static final String THEME = "theme";

	/** Key of the name of the color scheme of a theme. */
	public static final String COLOR_SCHEME = "colorScheme";

	/** Key of the colors of a theme. */
	public static final String COLORS = "colors";

	/** Key of the fonts of a theme. */
	public static final String FONTS = "fonts";

	/** Key of the font for headings in a theme. */
	public static final String MAJOR = "major";

	/** Key of the font for body text in a theme. */
	public static final String MINOR = "minor";

	/** Key of the background of a slide, layout or master. */
	public static final String BACKGROUND = "background";

	/** Key of the placeholders of a layout or master. */
	public static final String PLACEHOLDERS = "placeholders";

	/** Key of the shapes of a layout or master that are no placeholders. */
	public static final String DECORATIONS = "decorations";

	/** Key of the shapes of a slide. */
	public static final String SHAPES = "shapes";

	/** Key of the index of a placeholder. */
	public static final String IDX = "idx";

	/** Key of a text style or a text summary. */
	public static final String TEXT = "text";

	/** Key of the prompt text of a placeholder. */
	public static final String PROMPT = "prompt";

	/** Key of the kind of a shape. */
	public static final String KIND = "kind";

	/** Key of the rotation of a shape in degrees. */
	public static final String ROTATION = "rotation";

	/** Key of the geometry of a shape. */
	public static final String GEOMETRY = "geometry";

	/** Key of the name of a preset geometry. */
	public static final String PRESET = "preset";

	/** Key of the adjust values of a preset geometry. */
	public static final String ADJUST = "adjust";

	/** Key of the corner radius of a rounded rectangle. */
	public static final String CORNER_RADIUS = "cornerRadius";

	/** Key of the fill of a shape. */
	public static final String FILL = "fill";

	/** Key of the outline of a shape. */
	public static final String LINE = "line";

	/** Key of the outer shadow of a shape. */
	public static final String SHADOW = "shadow";

	/** Key of the placeholder reference of a shape. */
	public static final String PLACEHOLDER = "placeholder";

	/** Key of the table description of a table shape. */
	public static final String TABLE = "table";

	/** Key of the shapes in a group. */
	public static final String CHILDREN = "children";

	/** Key of the content type of an image. */
	public static final String CONTENT_TYPE = "contentType";

	/** Key of a color. */
	public static final String COLOR = "color";

	/** Key of the background color of a pattern fill. */
	public static final String BACKGROUND_COLOR = "backgroundColor";

	/** Key of the resolved RGB value of a color. */
	public static final String RGB = "rgb";

	/** Key of the theme color a color refers to. */
	public static final String SCHEME = "scheme";

	/** Key of the stops of a gradient. */
	public static final String STOPS = "stops";

	/** Key of the position of a gradient stop. */
	public static final String POSITION = "position";

	/** Key of the angle of a linear gradient in degrees. */
	public static final String ANGLE = "angle";

	/** Key of the shape of a path gradient. */
	public static final String PATH = "path";

	/** Key of the opacity of a picture fill. */
	public static final String OPACITY = "opacity";

	/** Key of the dash style of a line. */
	public static final String DASH = "dash";

	/** Key of the blur radius of a shadow. */
	public static final String BLUR = "blur";

	/** Key of the distance of a shadow. */
	public static final String DISTANCE = "distance";

	/** Key of the direction of a shadow in degrees. */
	public static final String DIRECTION = "direction";

	/** Key of a font family. */
	public static final String FONT = "font";

	/** Key of a font size. */
	public static final String SIZE = "size";

	/** Key of the bold flag of a text style. */
	public static final String BOLD = "bold";

	/** Key of the italic flag of a text style. */
	public static final String ITALIC = "italic";

	/** Key of the horizontal alignment of a text. */
	public static final String ALIGN = "align";

	/** Key of the flag telling whether a text is shown in capitals. */
	public static final String CAPS = "caps";

	/** Key of the vertical alignment of a text in its shape. */
	public static final String ANCHOR = "anchor";

	/** Key of the inner margins of a text shape. */
	public static final String INSETS = "insets";

	/** Key of a left margin. */
	public static final String LEFT = "left";

	/** Key of a top margin. */
	public static final String TOP = "top";

	/** Key of a right margin. */
	public static final String RIGHT = "right";

	/** Key of a bottom margin. */
	public static final String BOTTOM = "bottom";

	/** Key of the additional space between characters. */
	public static final String CHARACTER_SPACING = "characterSpacing";

	/** Key of the line spacing as a multiple of the single line height. */
	public static final String LINE_SPACING = "lineSpacing";

	/** Key of the number of paragraphs of a text. */
	public static final String PARAGRAPHS = "paragraphs";

	/** Key of the plain text content of a shape. */
	public static final String CONTENT = "content";

	/** Key of the number of rows of a table. */
	public static final String ROWS = "rows";

	/** Key of the number of columns of a table. */
	public static final String COLUMNS = "columns";

	/** Key of the column widths of a table. */
	public static final String COLUMN_WIDTHS = "columnWidths";

	/** Key of the table style of a table. */
	public static final String STYLE = "style";

	/** Key of the description of the first cell of a table. */
	public static final String HEADER_CELL = "headerCell";

	/** Key of the description of the first cell in the second row of a table. */
	public static final String BODY_CELL = "bodyCell";

	private TemplateData() {
		// Constants only.
	}

	/**
	 * Converts a length to its data representation, rounded to hundredths.
	 */
	public static Double number(double value) {
		return Double.valueOf(Math.round(value * 100) / 100.0);
	}

	/**
	 * Converts a fraction to its data representation, rounded to five digits.
	 */
	public static Double fraction(double value) {
		return Double.valueOf(Math.round(value * 100000) / 100000.0);
	}

	/**
	 * Adds the given value, if it is not <code>null</code>.
	 */
	public static void put(Map<String, Object> data, String key, Object value) {
		if (value != null) {
			data.put(key, value);
		}
	}

	/**
	 * Adds the position and size of the given bounds with the keys {@link #X}, {@link #Y},
	 * {@link #WIDTH} and {@link #HEIGHT}.
	 */
	public static void putBounds(Map<String, Object> data, Rectangle2D bounds) {
		if (bounds == null) {
			return;
		}
		data.put(X, number(bounds.getX()));
		data.put(Y, number(bounds.getY()));
		data.put(WIDTH, number(bounds.getWidth()));
		data.put(HEIGHT, number(bounds.getHeight()));
	}

	/**
	 * Creates the list representation of the given elements.
	 */
	public static <T> List<Object> list(List<T> elements, Function<? super T, ?> toData) {
		List<Object> result = new ArrayList<>(elements.size());
		for (T element : elements) {
			result.add(toData.apply(element));
		}
		return result;
	}

	/**
	 * Creates an empty dictionary with a stable key order.
	 */
	public static Map<String, Object> map() {
		return new LinkedHashMap<>();
	}

}
