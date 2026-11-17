/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.ui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.Logger;
import com.top_logic.react.flow.svg.RenderContext;

/**
 * Resolves a CSS {@code font-family} list against the font families installed for AWT.
 *
 * <p>
 * Resolution follows CSS: the first family of the list that is installed wins, family names
 * match independent of their case, and the generic names {@value #GENERIC_SANS_SERIF},
 * {@value #GENERIC_SERIF} and {@value #GENERIC_MONOSPACE} stand for the corresponding logical
 * fonts of Java ({@link Font#SANS_SERIF}, {@link Font#SERIF}, {@link Font#MONOSPACED}). A list
 * that names no installed family resolves to the default family.
 * </p>
 *
 * <p>
 * The default family is resolved from {@link RenderContext#DEFAULT_FONT_DECLARATION}. If none of
 * its families is installed, a warning is logged the first time the default is resolved.
 * </p>
 *
 * @see #getInstance()
 */
public class FontFamilyResolver {

	/**
	 * Generic CSS family name for a proportional font without serifs.
	 */
	public static final String GENERIC_SANS_SERIF = "sans-serif";

	/**
	 * Generic CSS family name for a proportional font with serifs.
	 */
	public static final String GENERIC_SERIF = "serif";

	/**
	 * Generic CSS family name for a fixed-pitch font.
	 */
	public static final String GENERIC_MONOSPACE = "monospace";

	private static final Map<String, String> LOGICAL_FONT_BY_GENERIC_NAME;

	static {
		Map<String, String> logicalFonts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		logicalFonts.put(GENERIC_SANS_SERIF, Font.SANS_SERIF);
		logicalFonts.put(GENERIC_SERIF, Font.SERIF);
		logicalFonts.put(GENERIC_MONOSPACE, Font.MONOSPACED);
		LOGICAL_FONT_BY_GENERIC_NAME = Collections.unmodifiableMap(logicalFonts);
	}

	private static volatile FontFamilyResolver _instance;

	private final Map<String, String> _installedFamilies;

	private final Map<String, Resolution> _resolutions = new ConcurrentHashMap<>();

	private Resolution _defaultResolution;

	/**
	 * Creates a {@link FontFamilyResolver}.
	 *
	 * @param installedFamilies
	 *        Names of the font families that count as installed.
	 */
	public FontFamilyResolver(Collection<String> installedFamilies) {
		Map<String, String> families = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		for (String family : installedFamilies) {
			families.put(family, family);
		}
		_installedFamilies = families;
	}

	/**
	 * The {@link FontFamilyResolver} for the font families installed on this machine.
	 */
	public static FontFamilyResolver getInstance() {
		FontFamilyResolver result = _instance;
		if (result == null) {
			synchronized (FontFamilyResolver.class) {
				result = _instance;
				if (result == null) {
					result = new FontFamilyResolver(Arrays.asList(
						GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(Locale.ROOT)));
					_instance = result;
				}
			}
		}
		return result;
	}

	/**
	 * The resolution of {@link RenderContext#DEFAULT_FONT_DECLARATION}.
	 */
	public synchronized Resolution getDefault() {
		if (_defaultResolution == null) {
			Resolution resolution = resolveList(RenderContext.DEFAULT_FONT_DECLARATION);
			if (resolution == null) {
				resolution = generic(GENERIC_SANS_SERIF, parseFamilies(RenderContext.DEFAULT_FONT_DECLARATION));
			}
			if (resolution.isGeneric()) {
				Logger.warn("None of the font families '" + RenderContext.DEFAULT_FONT_DECLARATION
					+ "' is installed, texts of diagrams are measured with the logical font '"
					+ resolution.getAWTFamily()
					+ "'. A renderer of an exported diagram may draw them in a font with other advance widths."
					+ " Install 'Liberation Sans', which is metric-compatible with 'Arial'.",
					FontFamilyResolver.class);
			}
			_defaultResolution = resolution;
		}
		return _defaultResolution;
	}

	/**
	 * Resolves the given CSS {@code font-family} list.
	 *
	 * @param declaration
	 *        A comma-separated list of family names, optionally quoted. {@code null} or an empty
	 *        list means the {@link #getDefault() default}.
	 * @return The first installed family of the list, or the {@link #getDefault() default}, if no
	 *         family of the list is installed.
	 */
	public Resolution resolve(String declaration) {
		if (declaration == null || declaration.trim().isEmpty()) {
			return getDefault();
		}
		return _resolutions.computeIfAbsent(declaration, list -> {
			Resolution resolution = resolveList(list);
			return resolution != null ? resolution : getDefault();
		});
	}

	private Resolution resolveList(String declaration) {
		List<String> families = parseFamilies(declaration);
		for (String family : families) {
			String installed = _installedFamilies.get(family);
			if (installed != null) {
				return new Resolution(family, installed, false, families);
			}
			if (LOGICAL_FONT_BY_GENERIC_NAME.containsKey(family)) {
				return generic(family, families);
			}
		}
		return null;
	}

	private static Resolution generic(String genericName, List<String> families) {
		return new Resolution(genericName, LOGICAL_FONT_BY_GENERIC_NAME.get(genericName), true, families);
	}

	/**
	 * Splits a CSS {@code font-family} list into its family names, without quotes.
	 */
	static List<String> parseFamilies(String declaration) {
		List<String> result = new ArrayList<>();
		for (String entry : declaration.split(",")) {
			String family = entry.trim();
			if (family.length() >= 2) {
				char first = family.charAt(0);
				if ((first == '\'' || first == '"') && family.charAt(family.length() - 1) == first) {
					family = family.substring(1, family.length() - 1).trim();
				}
			}
			if (!family.isEmpty()) {
				result.add(family);
			}
		}
		return result;
	}

	/**
	 * The family a {@code font-family} list resolved to.
	 */
	public static final class Resolution {

		private final String _cssFamily;

		private final String _awtFamily;

		private final boolean _generic;

		private final String _declaration;

		Resolution(String cssFamily, String awtFamily, boolean generic, List<String> families) {
			_cssFamily = cssFamily;
			_awtFamily = awtFamily;
			_generic = generic;
			_declaration = declaration(cssFamily, families);
		}

		/**
		 * The name of the resolved family as it appears in the resolved list.
		 */
		public String getCSSFamily() {
			return _cssFamily;
		}

		/**
		 * The family name to create an AWT {@link Font} with.
		 */
		public String getAWTFamily() {
			return _awtFamily;
		}

		/**
		 * Whether the list resolved to a generic family, i.e. names no installed family before
		 * the generic one.
		 */
		public boolean isGeneric() {
			return _generic;
		}

		/**
		 * The resolved list as CSS {@code font-family} declaration: the resolved family first,
		 * followed by the other families of the list in their order.
		 *
		 * <p>
		 * A renderer on the same machine draws the text in the measured family, while a renderer
		 * elsewhere keeps the substitutes of the list.
		 * </p>
		 */
		public String getDeclaration() {
			return _declaration;
		}

		private static String declaration(String first, List<String> families) {
			StringBuilder result = new StringBuilder(quote(first));
			for (String family : families) {
				if (!family.equalsIgnoreCase(first)) {
					result.append(", ").append(quote(family));
				}
			}
			return result.toString();
		}

		private static String quote(String family) {
			if (LOGICAL_FONT_BY_GENERIC_NAME.containsKey(family)) {
				return family;
			}
			for (int n = 0, cnt = family.length(); n < cnt; n++) {
				char ch = family.charAt(n);
				if (!(Character.isLetterOrDigit(ch) || ch == '-' || ch == '_')) {
					return "'" + family + "'";
				}
			}
			return family;
		}
	}

}
