/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.temperature;

import java.util.Locale;

import com.top_logic.model.TLClassifier;

/**
 * A unit in which a {@link Temperature} is displayed and entered.
 *
 * <p>
 * The constants are named like the classifiers of the model enumeration
 * <code>demo.temperature:TemperatureUnit</code>.
 * </p>
 *
 * @implNote A classifier of the model enumeration is mapped to its constant by
 *           {@link #fromClassifier(TLClassifier)}.
 */
public enum TemperatureUnit {

	/**
	 * Degrees Celsius, the unit a {@link Temperature} is stored in.
	 */
	CELSIUS("°C") {
		@Override
		public double fromCelsius(double celsius) {
			return celsius;
		}

		@Override
		public double toCelsius(double value) {
			return value;
		}
	},

	/**
	 * Degrees Fahrenheit.
	 */
	FAHRENHEIT("°F") {
		@Override
		public double fromCelsius(double celsius) {
			return celsius * 9 / 5 + 32;
		}

		@Override
		public double toCelsius(double value) {
			return (value - 32) * 5 / 9;
		}
	},

	/**
	 * Kelvin.
	 */
	KELVIN("K") {
		@Override
		public double fromCelsius(double celsius) {
			return celsius + 273.15;
		}

		@Override
		public double toCelsius(double value) {
			return value - 273.15;
		}
	};

	private final String _symbol;

	private TemperatureUnit(String symbol) {
		_symbol = symbol;
	}

	/**
	 * The symbol written after a value in this unit, e.g. <code>°F</code>.
	 */
	public String getSymbol() {
		return _symbol;
	}

	/**
	 * Converts a temperature in degrees Celsius to this unit.
	 */
	public abstract double fromCelsius(double celsius);

	/**
	 * Converts a temperature in this unit to degrees Celsius.
	 */
	public abstract double toCelsius(double value);

	/**
	 * The text of the given temperature in this unit, rounded to one decimal place, e.g.
	 * <code>68.0 °F</code>.
	 *
	 * <p>
	 * The text does not depend on the locale of the user, so that it can be read back by
	 * {@link Temperature#parse(String, TemperatureUnit)} in every locale.
	 * </p>
	 *
	 * @param celsius
	 *        The temperature in degrees Celsius.
	 */
	public String format(double celsius) {
		return String.format(Locale.ROOT, "%.1f %s", fromCelsius(celsius), _symbol);
	}

	/**
	 * The unit with the given symbol.
	 *
	 * <p>
	 * The symbol is matched ignoring case and an optional degree sign, so that <code>F</code>,
	 * <code>°f</code> and <code>°F</code> all denote {@link #FAHRENHEIT}.
	 * </p>
	 *
	 * @param symbol
	 *        The symbol as typed.
	 * @return The unit, or <code>null</code> if there is no unit with this symbol.
	 */
	public static TemperatureUnit fromSymbol(String symbol) {
		String plain = symbol.replace("°", "").trim();
		for (TemperatureUnit unit : values()) {
			if (unit.getSymbol().replace("°", "").equalsIgnoreCase(plain)) {
				return unit;
			}
		}
		return null;
	}

	/**
	 * The unit corresponding to the given classifier of the model enumeration
	 * <code>demo.temperature:TemperatureUnit</code>.
	 *
	 * @param classifier
	 *        The classifier, or <code>null</code>.
	 * @return The unit named like the classifier, or <code>null</code> if there is none.
	 */
	public static TemperatureUnit fromClassifier(TLClassifier classifier) {
		if (classifier == null) {
			return null;
		}
		for (TemperatureUnit unit : values()) {
			if (unit.name().equals(classifier.getName())) {
				return unit;
			}
		}
		return null;
	}

}
