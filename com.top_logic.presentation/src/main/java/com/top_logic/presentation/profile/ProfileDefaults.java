/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.profile;

import static com.top_logic.presentation.data.PresentationData.*;

import java.util.List;
import java.util.Map;

import com.top_logic.presentation.data.PresentationData;
import com.top_logic.presentation.template.ColorInfo;
import com.top_logic.presentation.template.PaintInfo;

/**
 * The color roles, styles and rating styles a {@link TemplateProfile} defines unless overridden.
 *
 * <p>
 * The defaults are independent of a concrete template: The roles refer to the theme colors of the
 * template, except the signal colors {@link #RISK}, {@link #WARNING} and {@link #DONE}, which are
 * fixed colors suitable on light backgrounds. The styles are built from the roles only, so that
 * they take the colors of any template.
 * </p>
 */
final class ProfileDefaults {

	/**
	 * The default color of the role {@link PresentationData#ROLE_RISK}.
	 */
	static final String RISK = "FF5733";

	/**
	 * The default color of the role
	 * {@link PresentationData#ROLE_WARNING}.
	 */
	static final String WARNING = "FFB81C";

	/**
	 * The default color of the role {@link PresentationData#ROLE_DONE}.
	 */
	static final String DONE = "4BA861";

	/**
	 * The corner radius of the rounded styles in points.
	 */
	static final double RADIUS = 8;

	/**
	 * The font size of the text in cards.
	 */
	static final double TEXT_SIZE = 14;

	/**
	 * The font size of the text of banners and badges.
	 */
	static final double STRONG_TEXT_SIZE = 16;

	private ProfileDefaults() {
		// Static utility.
	}

	/**
	 * The default color roles in the data representation of {@link TemplateProfile#roles()}.
	 *
	 * <ul>
	 * <li>{@link PresentationData#ROLE_ACCENT} and the topics
	 * {@link PresentationData#ROLE_TOPIC_A} to
	 * {@link PresentationData#ROLE_TOPIC_F}: the accent colors
	 * <code>accent1</code> to <code>accent6</code> of the theme.</li>
	 * <li>{@link PresentationData#ROLE_RISK},
	 * {@link PresentationData#ROLE_WARNING},
	 * {@link PresentationData#ROLE_DONE}: orange, yellow and
	 * green.</li>
	 * <li>{@link PresentationData#ROLE_MUTED}: the text color
	 * <code>tx1</code> lightened by 35%.</li>
	 * <li>{@link PresentationData#ROLE_TEXT}: the text color
	 * <code>tx1</code>, {@link PresentationData#ROLE_INVERSE}: the
	 * background color <code>bg1</code>.</li>
	 * </ul>
	 */
	static Map<String, Object> roles() {
		Map<String, Object> result = map();
		result.put(ROLE_ACCENT, "accent1");
		result.put(ROLE_TOPIC_A, "accent1");
		result.put(ROLE_TOPIC_B, "accent2");
		result.put(ROLE_TOPIC_C, "accent3");
		result.put(ROLE_TOPIC_D, "accent4");
		result.put(ROLE_TOPIC_E, "accent5");
		result.put(ROLE_TOPIC_F, "accent6");
		result.put(ROLE_RISK, RISK);
		result.put(ROLE_WARNING, WARNING);
		result.put(ROLE_DONE, DONE);
		result.put(ROLE_MUTED, color(SCHEME, "tx1", ColorInfo.LUM_MOD, 0.65, ColorInfo.LUM_OFF, 0.35));
		result.put(ROLE_TEXT, "tx1");
		result.put(ROLE_INVERSE, "bg1");
		return result;
	}

	/**
	 * The default styles in the data representation of {@link TemplateProfile#styles()}, named like
	 * the building blocks using them.
	 *
	 * <ul>
	 * <li>{@link PresentationData#TYPE_CARD}: a rounded rectangle
	 * with a light gradient, an outline and a soft shadow in the color of the element's role,
	 * the text at the top left.</li>
	 * <li>{@link PresentationData#TYPE_STAT}: like a card with
	 * wider margins.</li>
	 * <li>{@link PresentationData#TYPE_BANNER}: a rounded rectangle
	 * filled with the color of the element's role, its bold text in the color
	 * {@link PresentationData#ROLE_INVERSE}.</li>
	 * <li>{@link PresentationData#TYPE_BADGE}: a circle filled with
	 * the color of the element's role, its bold text centered.</li>
	 * <li>{@link PresentationData#TYPE_ARROW}: a right arrow filled
	 * with the color of the element's role, by default
	 * {@link PresentationData#ROLE_MUTED}.</li>
	 * </ul>
	 */
	static Map<String, Object> styles() {
		Map<String, Object> result = map();
		result.put(TYPE_CARD, card(insets(13, 10, 13, 10)));
		result.put(TYPE_STAT, card(insets(16, 14, 14, 11)));

		Map<String, Object> banner = map();
		banner.put(GEOMETRY, rounded());
		banner.put(FILL, role(ROLE_ACCENT));
		banner.put(LINE, NONE);
		banner.put(COLOR, role(ROLE_INVERSE));
		banner.put(BOLD, Boolean.TRUE);
		banner.put(SIZE, Double.valueOf(STRONG_TEXT_SIZE));
		banner.put(ALIGN, "left");
		banner.put(ANCHOR, "middle");
		banner.put(INSETS, insets(22, 8, 22, 8));
		result.put(TYPE_BANNER, banner);

		Map<String, Object> badge = map();
		badge.put(GEOMETRY, "ellipse");
		badge.put(FILL, role(ROLE_ACCENT));
		badge.put(LINE, NONE);
		badge.put(COLOR, role(ROLE_INVERSE));
		badge.put(BOLD, Boolean.TRUE);
		badge.put(SIZE, Double.valueOf(STRONG_TEXT_SIZE));
		badge.put(ALIGN, "center");
		badge.put(ANCHOR, "middle");
		badge.put(INSETS, Double.valueOf(0));
		result.put(TYPE_BADGE, badge);

		Map<String, Object> arrow = map();
		arrow.put(ROLE, ROLE_MUTED);
		arrow.put(GEOMETRY, "rightArrow");
		arrow.put(FILL, role(ROLE_ACCENT));
		arrow.put(LINE, NONE);
		result.put(TYPE_ARROW, arrow);
		return result;
	}

	/**
	 * The default rating styles in the data representation of {@link TemplateProfile#ratings()}:
	 * a light fill and a dark bold text of the same hue, centered; green for
	 * {@link PresentationData#RATING_MET} and
	 * {@link PresentationData#RATING_LOW}, yellow for
	 * {@link PresentationData#RATING_PARTIAL} and
	 * {@link PresentationData#RATING_MEDIUM}, red for
	 * {@link PresentationData#RATING_UNMET} and
	 * {@link PresentationData#RATING_HIGH}, grey for
	 * {@link PresentationData#RATING_NONE}.
	 */
	static Map<String, Object> ratings() {
		Map<String, Object> result = map();
		result.put(RATING_MET, rating("DCEFE0", "2E7D43"));
		result.put(RATING_PARTIAL, rating("FFF1D0", "8A6100"));
		result.put(RATING_UNMET, rating("FFE0D9", "C2391B"));
		result.put(RATING_LOW, rating("DCEFE0", "2E7D43"));
		result.put(RATING_MEDIUM, rating("FFF1D0", "8A6100"));
		result.put(RATING_HIGH, rating("FFE0D9", "C2391B"));
		result.put(RATING_NONE, rating("ECEDEF", "676B72"));
		return result;
	}

	private static Map<String, Object> card(Map<String, Object> insets) {
		Map<String, Object> result = map();
		result.put(GEOMETRY, rounded());
		Map<String, Object> fill = map();
		fill.put(TYPE, PaintInfo.Type.GRADIENT.id());
		fill.put(STOPS, List.of(color(SCHEME, "bg1", ColorInfo.LUM_MOD, 0.97),
			color(SCHEME, "bg1", ColorInfo.LUM_MOD, 0.9)));
		fill.put(ANGLE, Double.valueOf(90));
		result.put(FILL, fill);
		Map<String, Object> line = map();
		line.put(COLOR, role(ROLE_ACCENT));
		line.put(WIDTH, Double.valueOf(1));
		result.put(LINE, line);
		Map<String, Object> shadow = map();
		shadow.put(COLOR, color(ROLE, ROLE_ACCENT, ColorInfo.LUM_MOD, 0.75, ColorInfo.ALPHA, 0.2));
		shadow.put(BLUR, Double.valueOf(12));
		shadow.put(DISTANCE, Double.valueOf(5));
		shadow.put(DIRECTION, Double.valueOf(90));
		result.put(SHADOW, shadow);
		result.put(COLOR, role(ROLE_TEXT));
		result.put(SIZE, Double.valueOf(TEXT_SIZE));
		result.put(ALIGN, "left");
		result.put(ANCHOR, "top");
		result.put(INSETS, insets);
		return result;
	}

	private static Map<String, Object> rating(String fill, String color) {
		Map<String, Object> result = map();
		result.put(FILL, fill);
		result.put(COLOR, color);
		result.put(BOLD, Boolean.TRUE);
		result.put(ALIGN, "center");
		return result;
	}

	private static Map<String, Object> rounded() {
		Map<String, Object> result = map();
		result.put(PRESET, "roundRect");
		result.put(CORNER_RADIUS, Double.valueOf(RADIUS));
		return result;
	}

	private static Map<String, Object> role(String role) {
		return color(ROLE, role);
	}

	/**
	 * A color dictionary with the given kind key, name and modifications given as pairs of name
	 * and value.
	 */
	private static Map<String, Object> color(String kind, String name, Object... modifiers) {
		Map<String, Object> result = map();
		result.put(kind, name);
		for (int n = 0; n < modifiers.length; n += 2) {
			result.put((String) modifiers[n], modifiers[n + 1]);
		}
		return result;
	}

	private static Map<String, Object> insets(double left, double top, double right, double bottom) {
		Map<String, Object> result = map();
		result.put(LEFT, Double.valueOf(left));
		result.put(TOP, Double.valueOf(top));
		result.put(RIGHT, Double.valueOf(right));
		result.put(BOTTOM, Double.valueOf(bottom));
		return result;
	}

}
