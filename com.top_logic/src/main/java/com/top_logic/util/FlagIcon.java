/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.basic.ThemeImage;

/**
 * The flag images the UI draws from, addressed by the code of what they stand for.
 *
 * <p>
 * A flag is a CSS-class icon of the {@code flag-icons} stylesheet, which offers one image per
 * ISO 3166-1 country code and a handful beyond it: the flag of a region ({@code gb-eng},
 * {@code es-ct}), of a union ({@code eu}, {@code un}), of a language area that is no country
 * ({@code arab}), and one for something it has no flag for.
 * </p>
 *
 * @see LanguageFlags
 */
public final class FlagIcon {

	/** The CSS class selecting the flag stylesheet. */
	private static final String FLAG_CLASS = "fi";

	/**
	 * The code of the flag standing for something no other flag covers.
	 */
	public static final String FALLBACK_CODE = "xx";

	/**
	 * The flag standing for something the stylesheet has no flag for.
	 */
	public static final ThemeImage FALLBACK = icon(FALLBACK_CODE);

	private FlagIcon() {
		// Utility class.
	}

	/**
	 * The flag of the given code.
	 *
	 * @param code
	 *        The code of a country, region, union or language area as the flag stylesheet spells
	 *        it, in either case; {@code null} or empty yields the {@link #FALLBACK}.
	 * @return The image, never {@code null}.
	 */
	public static ThemeImage forCode(String code) {
		return StringServices.isEmpty(code) ? FALLBACK : icon(code);
	}

	private static ThemeImage icon(String code) {
		return ThemeImage.cssIcon(FLAG_CLASS + " " + FLAG_CLASS + "-" + code.toLowerCase());
	}

}
