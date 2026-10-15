/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 *
 * <p>
 * The first argument of each error is the path of the faulty entry in the presentation
 * description, e.g. <code>slides[2].content[0].fill.color</code>. The constants starting with
 * <code>EXPECTED_</code> describe what an entry must contain.
 * </p>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The entry {0} of the presentation description has the invalid value {1}. Expected: {2}
	 */
	public static ResKey3 ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED;

	/**
	 * @en The entry {0} of the presentation description is missing. Expected: {1}
	 */
	public static ResKey2 ERROR_MISSING_ENTRY__PATH_EXPECTED;

	/**
	 * @en The presentation description contains the unknown entry {0}. Allowed entries are: {1}.
	 */
	public static ResKey2 ERROR_UNKNOWN_ENTRY__PATH_KEYS;

	/**
	 * @en The element {0} of the presentation description has the unknown type "{1}". Known types
	 *     are: {2}.
	 */
	public static ResKey3 ERROR_UNKNOWN_TYPE__PATH_TYPE_TYPES;

	/**
	 * @en The entry {0} of the presentation description must not be given together with {1}.
	 */
	public static ResKey2 ERROR_EXCLUSIVE_ENTRIES__PATH_OTHER;

	/**
	 * @en The element {0} of a row or column gives a position. The row or column places its
	 *     children, give only their size ("width", "height") or their share ("weight").
	 */
	public static ResKey1 ERROR_POSITION_IN_CONTAINER__PATH;

	/**
	 * @en A text.
	 */
	public static ResKey EXPECTED_TEXT;

	/**
	 * @en A number.
	 */
	public static ResKey EXPECTED_NUMBER;

	/**
	 * @en A number that is not negative.
	 */
	public static ResKey EXPECTED_NON_NEGATIVE_NUMBER;

	/**
	 * @en A number greater than zero.
	 */
	public static ResKey EXPECTED_POSITIVE_NUMBER;

	/**
	 * @en A whole number between {0} and {1}.
	 */
	public static ResKey2 EXPECTED_INTEGER__MIN_MAX;

	/**
	 * @en A number between {0} and {1}.
	 */
	public static ResKey2 EXPECTED_RANGE__MIN_MAX;

	/**
	 * @en A truth value.
	 */
	public static ResKey EXPECTED_BOOLEAN;

	/**
	 * @en A list.
	 */
	public static ResKey EXPECTED_LIST;

	/**
	 * @en A dictionary.
	 */
	public static ResKey EXPECTED_DICTIONARY;

	/**
	 * @en Binary data such as an uploaded file.
	 */
	public static ResKey EXPECTED_BINARY;

	/**
	 * @en One of the values {0}.
	 */
	public static ResKey1 EXPECTED_ONE_OF__VALUES;

	/**
	 * @en A color: a theme color name such as "accent1", a hexadecimal color "RRGGBB" or "#RRGGBB",
	 *     a color role name, or a dictionary with one of the entries "scheme", "rgb" or "role" and
	 *     optional modifications such as "lumMod" or "alpha".
	 */
	public static ResKey EXPECTED_COLOR;

	/**
	 * @en A fill: "none", a color, or a dictionary with the fill type ("none", "solid", "gradient"
	 *     or "picture") and its properties.
	 */
	public static ResKey EXPECTED_FILL;

	/**
	 * @en A text: a string (each line a paragraph), a paragraph dictionary with the entry "runs", a
	 *     text run dictionary with the entry "text", or a list of those.
	 */
	public static ResKey EXPECTED_TEXT_CONTENT;

	/**
	 * @en Text runs: a string, a text run dictionary with the entry "text", or a list of those.
	 */
	public static ResKey EXPECTED_RUNS;

	/**
	 * @en A bullet: a truth value, "none", the bullet character, or a dictionary with the entries
	 *     "char" and "color".
	 */
	public static ResKey EXPECTED_BULLET;

	/**
	 * @en A slide layout: its index, its name or one of the roles {0}.
	 */
	public static ResKey1 EXPECTED_LAYOUT__ROLES;

	/**
	 * @en The name of a preset geometry such as "rect", "roundRect" or "ellipse", or a dictionary
	 *     with the entries "preset", "adjust" and "cornerRadius".
	 */
	public static ResKey EXPECTED_GEOMETRY;

	/**
	 * @en Inner margins: a number for all sides or a dictionary with the entries "left", "top",
	 *     "right" and "bottom".
	 */
	public static ResKey EXPECTED_INSETS;

	/**
	 * @en A row with {0} cells.
	 */
	public static ResKey1 EXPECTED_ROW_LENGTH__COUNT;

	/**
	 * @en A list of {0} positive numbers, one for each child.
	 */
	public static ResKey1 EXPECTED_WEIGHTS__COUNT;

	/**
	 * @en A height: a number that is not negative, or "auto" for the height of the content.
	 */
	public static ResKey EXPECTED_HEIGHT;

	/**
	 * @en A dictionary describing a slide element with the entry "type".
	 */
	public static ResKey EXPECTED_ELEMENT;

	/**
	 * @en A list of at least two gradient stops, each a color or a dictionary with the entries
	 *     "position" and "color".
	 */
	public static ResKey EXPECTED_STOPS;

	/**
	 * @en An outline: "none", a color, or a dictionary with the entries "color", "width" and
	 *     "dash".
	 */
	public static ResKey EXPECTED_LINE;

	/**
	 * @en A shadow: a truth value or a dictionary with the entries "color", "blur", "distance" and
	 *     "direction".
	 */
	public static ResKey EXPECTED_SHADOW;

	/**
	 * @en The name of a style of the template profile.
	 */
	public static ResKey EXPECTED_STYLE;

	/**
	 * @en The name of a color role, e.g. "topicA" or "risk".
	 */
	public static ResKey EXPECTED_ROLE;

	/**
	 * @en A legend entry: a list of its label and its color role, e.g. ["Done", "done"].
	 */
	public static ResKey EXPECTED_LEGEND_ITEM;

	static {
		initConstants(I18NConstants.class);
	}
}
