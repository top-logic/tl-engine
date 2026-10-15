/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.temperature;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The value of the model type <code>demo.temperature:Temperature</code>: a temperature together
 * with the unit it is displayed in.
 *
 * <p>
 * The temperature itself is held in degrees Celsius, which is also how it is stored. The
 * {@link #getUnit() display unit} is no part of the value: it is attached when the value is read
 * from an object, from the context of that object (see {@link TemperatureStorage}). Two
 * temperatures are therefore {@link #equals(Object) equal} and {@link #compareTo(Temperature)
 * ordered} by their degrees Celsius alone, whatever unit they are displayed in.
 * </p>
 *
 * <p>
 * {@link #toString()} is the display text, e.g. <code>68.0 °F</code>.
 * </p>
 */
public final class Temperature implements Comparable<Temperature> {

	/**
	 * The lowest possible temperature in degrees Celsius.
	 */
	public static final double ABSOLUTE_ZERO = -273.15;

	/**
	 * A number with an optional unit symbol behind it, e.g. <code>68</code>, <code>68 °F</code>,
	 * <code>-3,5C</code> or <code>300 K</code>.
	 */
	private static final Pattern TEXT_PATTERN =
		Pattern.compile("([+-]?(?:\\d+(?:[.,]\\d*)?|[.,]\\d+))\\s*(°?\\s*[a-zA-Z]+)?");

	private final double _celsius;

	private final TemperatureUnit _unit;

	private Temperature(double celsius, TemperatureUnit unit) {
		_celsius = celsius;
		_unit = Objects.requireNonNull(unit);
	}

	/**
	 * The given temperature, displayed in degrees Celsius.
	 *
	 * @param celsius
	 *        The temperature in degrees Celsius.
	 */
	public static Temperature ofCelsius(double celsius) {
		return new Temperature(celsius, TemperatureUnit.CELSIUS);
	}

	/**
	 * The given temperature, displayed in the unit it is given in.
	 *
	 * @param value
	 *        The temperature in the given unit.
	 * @param unit
	 *        The unit of the value.
	 */
	public static Temperature of(double value, TemperatureUnit unit) {
		return new Temperature(unit.toCelsius(value), unit);
	}

	/**
	 * The temperature in degrees Celsius.
	 */
	public double getCelsius() {
		return _celsius;
	}

	/**
	 * The unit this temperature is displayed in.
	 */
	public TemperatureUnit getUnit() {
		return _unit;
	}

	/**
	 * The temperature in its {@link #getUnit() display unit}.
	 */
	public double getValue() {
		return _unit.fromCelsius(_celsius);
	}

	/**
	 * The same temperature displayed in the given unit.
	 *
	 * @param unit
	 *        The unit to display the temperature in.
	 */
	public Temperature withUnit(TemperatureUnit unit) {
		if (unit == _unit) {
			return this;
		}
		return new Temperature(_celsius, unit);
	}

	/**
	 * The display text, e.g. <code>68.0 °F</code>.
	 */
	public String format() {
		return _unit.format(_celsius);
	}

	/**
	 * Reads a temperature from the text a user typed.
	 *
	 * <p>
	 * The text is a number, written with a decimal point or a decimal comma, optionally followed by
	 * a unit symbol (see {@link TemperatureUnit#fromSymbol(String)}). A number without a unit is
	 * taken in the given unit. The result is displayed in the given unit in any case, so that a
	 * value typed as <code>20 °C</code> in a field displaying degrees Fahrenheit is shown as
	 * <code>68.0 °F</code> afterwards.
	 * </p>
	 *
	 * @param text
	 *        The text to read.
	 * @param unit
	 *        The unit of a number typed without unit, and the display unit of the result.
	 * @return The temperature, or <code>null</code> if the text is no temperature.
	 */
	public static Temperature parse(String text, TemperatureUnit unit) {
		Matcher matcher = TEXT_PATTERN.matcher(text.trim());
		if (!matcher.matches()) {
			return null;
		}
		double number = Double.parseDouble(matcher.group(1).replace(',', '.'));
		String symbol = matcher.group(2);
		TemperatureUnit inputUnit;
		if (symbol == null) {
			inputUnit = unit;
		} else {
			inputUnit = TemperatureUnit.fromSymbol(symbol);
			if (inputUnit == null) {
				return null;
			}
		}
		return of(number, inputUnit).withUnit(unit);
	}

	@Override
	public int compareTo(Temperature other) {
		return Double.compare(_celsius, other._celsius);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Temperature other)) {
			return false;
		}
		return Double.compare(_celsius, other._celsius) == 0;
	}

	@Override
	public int hashCode() {
		return Double.hashCode(_celsius);
	}

	@Override
	public String toString() {
		return format();
	}

}
