/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import java.awt.Color;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.form.format.ColorFormat;

/**
 * The color a value is displayed with: either a literal CSS color or a reference to a design token
 * of the active theme.
 *
 * @see ValueColorProvider
 */
public final class ValueColor {

	/** Prefix of the CSS custom property a design token is emitted as. */
	private static final String CUSTOM_PROPERTY_PREFIX = "--";

	private final String _value;

	private final boolean _themeToken;

	private ValueColor(String value, boolean themeToken) {
		_value = value;
		_themeToken = themeToken;
	}

	/**
	 * Creates the {@link ValueColor} a {@link ColorSpec} specifies.
	 *
	 * @param spec
	 *        The specification to resolve. May be <code>null</code>.
	 * @return The specified color, or <code>null</code> if the specification is absent or names
	 *         neither a literal color nor a design token.
	 */
	public static ValueColor of(ColorSpec spec) {
		if (spec == null) {
			return null;
		}
		Color literal = spec.getValue();
		if (literal != null) {
			return color(literal);
		}
		String token = spec.getToken();
		if (!StringServices.isEmpty(token)) {
			return themeToken(token);
		}
		return null;
	}

	/**
	 * Creates a {@link ValueColor} displaying the given literal color.
	 *
	 * @param color
	 *        The color to display with.
	 * @return The {@link ValueColor} for the given color.
	 */
	public static ValueColor color(Color color) {
		return cssColor(ColorFormat.formatColor(color));
	}

	/**
	 * Creates a {@link ValueColor} displaying the given literal CSS color.
	 *
	 * @param cssColor
	 *        A CSS color value such as <code>#04a38d</code>.
	 * @return The {@link ValueColor} for the given CSS color.
	 */
	public static ValueColor cssColor(String cssColor) {
		return new ValueColor(cssColor, false);
	}

	/**
	 * Creates a {@link ValueColor} referencing a design token of the active theme.
	 *
	 * @param tokenName
	 *        The token name, without the {@value #CUSTOM_PROPERTY_PREFIX} prefix of the CSS custom
	 *        property the token is emitted as.
	 * @return The {@link ValueColor} for the given token.
	 */
	public static ValueColor themeToken(String tokenName) {
		return new ValueColor(tokenName, true);
	}

	/**
	 * Whether {@link #getValue()} names a design token instead of being a literal CSS color.
	 */
	public boolean isThemeToken() {
		return _themeToken;
	}

	/**
	 * The literal CSS color, or the name of the design token if {@link #isThemeToken()}.
	 */
	public String getValue() {
		return _value;
	}

	/**
	 * The CSS expression yielding this color, usable wherever CSS expects a color.
	 */
	public String cssValue() {
		return _themeToken ? "var(" + CUSTOM_PROPERTY_PREFIX + _value + ")" : _value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ValueColor other)) {
			return false;
		}
		return _themeToken == other._themeToken && _value.equals(other._value);
	}

	@Override
	public int hashCode() {
		return _value.hashCode() + (_themeToken ? 1 : 0);
	}

	@Override
	public String toString() {
		return cssValue();
	}

}
