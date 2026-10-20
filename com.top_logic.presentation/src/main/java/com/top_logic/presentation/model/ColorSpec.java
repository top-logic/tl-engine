/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.presentation.data.PresentationData;
import com.top_logic.presentation.template.ColorInfo;

/**
 * A color in a presentation description.
 *
 * <p>
 * A color is either a {@link Scheme theme color}, a fixed {@link Rgb RGB color}, or a {@link Role
 * color role} that is mapped to a concrete color when the presentation is created. Each form can
 * be modified by the modifications listed in {@link ColorInfo#MODIFIERS}, e.g. to lighten a theme
 * color or to make it transparent.
 * </p>
 */
public sealed interface ColorSpec {

	/**
	 * The names of the theme colors.
	 */
	List<String> SCHEME_COLORS = List.of("bg1", "tx1", "bg2", "tx2", "dk1", "lt1", "dk2", "lt2", "accent1",
		"accent2", "accent3", "accent4", "accent5", "accent6", "hlink", "folHlink");

	/**
	 * Pattern of a hexadecimal color, optionally starting with <code>#</code>.
	 */
	Pattern HEX = Pattern.compile("#?([0-9a-fA-F]{6})");

	/**
	 * Pattern of a color role name.
	 */
	Pattern ROLE_NAME = Pattern.compile("[\\p{L}_][\\p{L}\\p{N}_.\\-]*");

	/**
	 * The modifications of the color by their names in {@link ColorInfo#MODIFIERS}, given as
	 * fractions (e.g. 0.75 for 75%).
	 */
	Map<String, Double> modifiers();

	/**
	 * The data representation of this color: a dictionary with the key
	 * {@link PresentationData#SCHEME}, {@link PresentationData#RGB} or {@link PresentationData#ROLE}
	 * and the names of the {@link #modifiers()}.
	 */
	default Map<String, Object> toData() {
		Map<String, Object> result = PresentationData.map();
		if (this instanceof Scheme scheme) {
			result.put(PresentationData.SCHEME, scheme.name());
		} else if (this instanceof Rgb rgb) {
			result.put(PresentationData.RGB, rgb.rgb());
		} else if (this instanceof Role role) {
			result.put(PresentationData.ROLE, role.name());
		}
		for (String modifier : ColorInfo.MODIFIERS) {
			PresentationData.put(result, modifier, modifiers().get(modifier));
		}
		return result;
	}

	/**
	 * Interprets the given color name.
	 *
	 * <p>
	 * A name in {@link #SCHEME_COLORS} is a theme color, six hexadecimal digits (optionally
	 * preceded by <code>#</code>) are an RGB color, and any other name matching
	 * {@link #ROLE_NAME} is a color role.
	 * </p>
	 *
	 * @param name
	 *        The color name.
	 * @param modifiers
	 *        See {@link #modifiers()}.
	 * @return The color, <code>null</code> if the name is no valid color name.
	 */
	static ColorSpec parse(String name, Map<String, Double> modifiers) {
		if (SCHEME_COLORS.contains(name)) {
			return new Scheme(name, modifiers);
		}
		Matcher hex = HEX.matcher(name);
		if (hex.matches()) {
			return new Rgb(hex.group(1).toUpperCase(), modifiers);
		}
		if (name.startsWith("#") || PresentationData.NONE.equals(name) || !ROLE_NAME.matcher(name).matches()) {
			return null;
		}
		return new Role(name, modifiers);
	}

	/**
	 * A color of the theme of the slide the color is used on.
	 *
	 * @param name
	 *        The name of the theme color, one of {@link ColorSpec#SCHEME_COLORS}.
	 * @param modifiers
	 *        See {@link ColorSpec#modifiers()}.
	 */
	record Scheme(String name, Map<String, Double> modifiers) implements ColorSpec {
		// Pure data.
	}

	/**
	 * A fixed color.
	 *
	 * @param rgb
	 *        The color as six upper case hexadecimal digits <code>RRGGBB</code>.
	 * @param modifiers
	 *        See {@link ColorSpec#modifiers()}.
	 */
	record Rgb(String rgb, Map<String, Double> modifiers) implements ColorSpec {
		// Pure data.
	}

	/**
	 * A color given by its role in the presentation, e.g. the color marking risks.
	 *
	 * <p>
	 * A role is mapped to a theme color or a fixed color when the presentation is created.
	 * </p>
	 *
	 * @param name
	 *        The name of the role.
	 * @param modifiers
	 *        See {@link ColorSpec#modifiers()}.
	 */
	record Role(String name, Map<String, Double> modifiers) implements ColorSpec {
		// Pure data.
	}

}
