/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.react.flow.svg.SvgWriter;

/**
 * Color roles of the user interface for drawn diagram elements.
 *
 * <p>
 * A role is given by its external name ({@code neutral}, {@code brand}, {@code error},
 * {@code warning}, {@code success}, {@code info}, {@code category-1} ... {@code category-8}).
 * An element drawn in a role carries the role in the attribute {@link #ROLE_ATTR}, through which
 * the stylesheet of the theme colors it, also in the dark mode. The element itself is drawn in a
 * fixed substitute color of the role, which an export without stylesheet (e.g. a PDF) shows.
 * </p>
 */
public final class ColorRoles {

	/** SVG attribute holding the color role of an element. */
	public static final String ROLE_ATTR = "data-tl-role";

	/** The role of an element without a meaning of its own. */
	public static final String NEUTRAL = "neutral";

	/** The role of something that went wrong, e.g. a violated dependency. */
	public static final String ERROR = "error";

	/** Substitute color of a line in the {@link #NEUTRAL} role (and of a role without one). */
	public static final String NEUTRAL_LINE_FALLBACK = "#606060";

	/** Substitute color of an area in the {@link #NEUTRAL} role (and of a role without one). */
	public static final String NEUTRAL_AREA_FALLBACK = "rgba(100,100,100,0.2)";

	private static final Map<String, String> LINE_FALLBACKS = new HashMap<>();

	private static final Map<String, String> AREA_FALLBACKS = new HashMap<>();

	static {
		// The colors of the light mode of the standard theme: the border of a role for a line,
		// its subtle surface for an area.
		fallback(NEUTRAL, NEUTRAL_LINE_FALLBACK, NEUTRAL_AREA_FALLBACK);
		fallback("brand", "#0B5ED7", "#DFEBFF");
		fallback(ERROR, "#DC3B32", "#FFEBE8");
		fallback("warning", "#98720C", "#FEEFCF");
		fallback("success", "#36A376", "#DCF8E9");
		fallback("info", "#159FB6", "#CEF3FB");
		fallback("category-1", "#2B99D5", "#E2F3FF");
		fallback("category-2", "#D77290", "#FFEAEF");
		fallback("category-3", "#8C87E0", "#EEEEFF");
		fallback("category-4", "#00A398", "#D7F8F4");
		fallback("category-5", "#CF7C3B", "#FFECDF");
		fallback("category-6", "#AF7DD0", "#F6EBFF");
		fallback("category-7", "#7F9A36", "#EBF4DB");
		fallback("category-8", "#C774B2", "#FFE9F8");
	}

	private ColorRoles() {
		// Constants and static methods only.
	}

	private static void fallback(String role, String line, String area) {
		LINE_FALLBACKS.put(role, line);
		AREA_FALLBACKS.put(role, area);
	}

	/**
	 * The substitute color of a line (a stroke) in the given role.
	 */
	public static String lineFallback(String role) {
		String result = LINE_FALLBACKS.get(role);
		return result != null ? result : NEUTRAL_LINE_FALLBACK;
	}

	/**
	 * The substitute color of an area (a fill) in the given role.
	 */
	public static String areaFallback(String role) {
		String result = AREA_FALLBACKS.get(role);
		return result != null ? result : NEUTRAL_AREA_FALLBACK;
	}

	/**
	 * Writes the given role to the element currently open in the given writer.
	 *
	 * @param role
	 *        The external name of the role, or {@code null} for an element drawn in a fixed color.
	 */
	public static void writeRole(SvgWriter out, String role) {
		if (role != null) {
			out.writeAttribute(ROLE_ATTR, role);
		}
	}

	/**
	 * Whether the given color or role value is not given.
	 */
	public static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

}
