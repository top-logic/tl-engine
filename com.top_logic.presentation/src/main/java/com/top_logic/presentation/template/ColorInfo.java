/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.util.List;
import java.util.Map;

/**
 * A color used in a presentation template.
 *
 * <p>
 * Besides the resolved color value, the description keeps the theme color the color refers to and
 * the modifications applied to it, since a shape reproducing the template's style must use the
 * same theme reference to follow a theme change.
 * </p>
 *
 * @param rgb
 *        The resolved color as six hexadecimal digits <code>RRGGBB</code>, all modifications
 *        except the transparency applied, <code>null</code> if the color cannot be resolved.
 * @param scheme
 *        The name of the theme color (e.g. <code>accent1</code>, <code>tx1</code>) the color
 *        refers to, <code>null</code> for a fixed color.
 * @param modifiers
 *        The modifications of the base color by their names in {@link #MODIFIERS}, given as
 *        fractions (e.g. 0.75 for 75%).
 */
public record ColorInfo(String rgb, String scheme, Map<String, Double> modifiers) {

	/** Modification scaling the luminance of a color. */
	public static final String LUM_MOD = "lumMod";

	/** Modification shifting the luminance of a color. */
	public static final String LUM_OFF = "lumOff";

	/** Modification setting the opacity of a color. */
	public static final String ALPHA = "alpha";

	/** Modification mixing a color with white. */
	public static final String TINT = "tint";

	/** Modification mixing a color with black. */
	public static final String SHADE = "shade";

	/** Modification scaling the saturation of a color. */
	public static final String SAT_MOD = "satMod";

	/** Modification shifting the saturation of a color. */
	public static final String SAT_OFF = "satOff";

	/**
	 * All modifications reported in {@link #modifiers()}.
	 */
	public static final List<String> MODIFIERS = List.of(LUM_MOD, LUM_OFF, ALPHA, TINT, SHADE, SAT_MOD, SAT_OFF);

	/**
	 * The opacity of the color, 1 for an opaque color.
	 */
	public double alpha() {
		Double alpha = modifiers.get(ALPHA);
		return alpha == null ? 1.0 : alpha.doubleValue();
	}

	/**
	 * The data representation with the keys {@link TemplateData#RGB}, {@link TemplateData#SCHEME}
	 * and the names of the {@link #modifiers()}.
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = TemplateData.map();
		TemplateData.put(result, TemplateData.RGB, rgb);
		TemplateData.put(result, TemplateData.SCHEME, scheme);
		for (String modifier : MODIFIERS) {
			Double value = modifiers.get(modifier);
			if (value != null) {
				result.put(modifier, TemplateData.fraction(value.doubleValue()));
			}
		}
		return result;
	}

}
