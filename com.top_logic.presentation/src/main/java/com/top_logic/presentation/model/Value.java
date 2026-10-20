/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.error.TopLogicException;

/**
 * A value in the data representation of a presentation description together with its path.
 *
 * <p>
 * The path names the position of the value in the description, e.g.
 * <code>slides[2].content[0].fill.color</code>, and is reported in all errors about the value.
 * The conversion methods check the type of the value and fail with an error naming the path and
 * the expected value. Methods starting with <code>opt</code> accept a missing value and return
 * <code>null</code> for it.
 * </p>
 */
public final class Value {

	private static final int MAX_DESCRIPTION_LENGTH = 60;

	private final Object _raw;

	private final String _path;

	/**
	 * Creates a {@link Value}.
	 *
	 * @param raw
	 *        The value in the data representation, <code>null</code> if it is missing.
	 * @param path
	 *        The path of the value in the description.
	 */
	public Value(Object raw, String path) {
		_raw = raw;
		_path = path;
	}

	/**
	 * The value in the data representation, <code>null</code> if it is missing.
	 */
	public Object raw() {
		return _raw;
	}

	/**
	 * The path of this value in the description.
	 */
	public String path() {
		return _path;
	}

	/**
	 * Whether the value is missing.
	 */
	public boolean isAbsent() {
		return _raw == null;
	}

	/**
	 * Whether the value is a text.
	 */
	public boolean isString() {
		return _raw instanceof CharSequence;
	}

	/**
	 * Whether the value is a number.
	 */
	public boolean isNumber() {
		return _raw instanceof Number;
	}

	/**
	 * Whether the value is a truth value.
	 */
	public boolean isBoolean() {
		return _raw instanceof Boolean;
	}

	/**
	 * Whether the value is a dictionary.
	 */
	public boolean isMap() {
		return _raw instanceof Map<?, ?>;
	}

	/**
	 * Whether the value is a list.
	 */
	public boolean isList() {
		return _raw instanceof Collection<?>;
	}

	/**
	 * Fails with an error, if the value is missing.
	 *
	 * @param expected
	 *        Description of the expected value.
	 * @return This value.
	 */
	public Value require(ResKey expected) {
		if (isAbsent()) {
			throw missing(expected);
		}
		return this;
	}

	/**
	 * The text value.
	 */
	public String asString() {
		if (!isString()) {
			throw invalid(I18NConstants.EXPECTED_TEXT);
		}
		return _raw.toString();
	}

	/**
	 * The text value, <code>null</code> if missing.
	 */
	public String optString() {
		return isAbsent() ? null : asString();
	}

	/**
	 * The finite number value.
	 */
	public double asNumber() {
		if (_raw instanceof Number number) {
			double result = number.doubleValue();
			if (Double.isFinite(result)) {
				return result;
			}
		}
		throw invalid(I18NConstants.EXPECTED_NUMBER);
	}

	/**
	 * The finite number value, <code>null</code> if missing.
	 */
	public Double optNumber() {
		return isAbsent() ? null : Double.valueOf(asNumber());
	}

	/**
	 * The number value that is not negative.
	 */
	public double asNonNegative() {
		double result = asNumber();
		if (result < 0) {
			throw invalid(I18NConstants.EXPECTED_NON_NEGATIVE_NUMBER);
		}
		return result;
	}

	/**
	 * The number value that is not negative, <code>null</code> if missing.
	 */
	public Double optNonNegative() {
		return isAbsent() ? null : Double.valueOf(asNonNegative());
	}

	/**
	 * The number value that is greater than zero.
	 */
	public double asPositive() {
		double result = asNumber();
		if (result <= 0) {
			throw invalid(I18NConstants.EXPECTED_POSITIVE_NUMBER);
		}
		return result;
	}

	/**
	 * The number value that is greater than zero, <code>null</code> if missing.
	 */
	public Double optPositive() {
		return isAbsent() ? null : Double.valueOf(asPositive());
	}

	/**
	 * The number value in the given range.
	 */
	public double asRange(double min, double max) {
		double result = asNumber();
		if (result < min || result > max) {
			throw invalid(I18NConstants.EXPECTED_RANGE__MIN_MAX.fill(min, max));
		}
		return result;
	}

	/**
	 * The number value in the given range, <code>null</code> if missing.
	 */
	public Double optRange(double min, double max) {
		return isAbsent() ? null : Double.valueOf(asRange(min, max));
	}

	/**
	 * The whole number value in the given range.
	 */
	public int asInteger(int min, int max) {
		if (_raw instanceof Number number) {
			double value = number.doubleValue();
			if (value == Math.rint(value) && value >= min && value <= max) {
				return (int) value;
			}
		}
		throw invalid(I18NConstants.EXPECTED_INTEGER__MIN_MAX.fill(min, max));
	}

	/**
	 * The whole number value in the given range, <code>null</code> if missing.
	 */
	public Integer optInteger(int min, int max) {
		return isAbsent() ? null : Integer.valueOf(asInteger(min, max));
	}

	/**
	 * The truth value.
	 */
	public boolean asBoolean() {
		if (_raw instanceof Boolean flag) {
			return flag.booleanValue();
		}
		throw invalid(I18NConstants.EXPECTED_BOOLEAN);
	}

	/**
	 * The truth value, <code>null</code> if missing.
	 */
	public Boolean optBoolean() {
		return isAbsent() ? null : Boolean.valueOf(asBoolean());
	}

	/**
	 * The binary data value.
	 */
	public BinaryDataSource asBinary() {
		if (_raw instanceof BinaryDataSource data) {
			return data;
		}
		throw invalid(I18NConstants.EXPECTED_BINARY);
	}

	/**
	 * The enumeration value given by its {@link DataEnum#id()}.
	 */
	public <E extends Enum<E> & DataEnum> E asEnum(Class<E> type) {
		E result = isString() ? DataEnum.forId(type, _raw.toString()) : null;
		if (result == null) {
			throw invalid(I18NConstants.EXPECTED_ONE_OF__VALUES.fill(String.join(", ", DataEnum.ids(type))));
		}
		return result;
	}

	/**
	 * The enumeration value given by its {@link DataEnum#id()}, <code>null</code> if missing.
	 */
	public <E extends Enum<E> & DataEnum> E optEnum(Class<E> type) {
		return isAbsent() ? null : asEnum(type);
	}

	/**
	 * The text value, which must be one of the given values.
	 */
	public String asOneOf(List<String> values) {
		if (isString() && values.contains(_raw.toString())) {
			return _raw.toString();
		}
		throw invalid(I18NConstants.EXPECTED_ONE_OF__VALUES.fill(String.join(", ", values)));
	}

	/**
	 * The text value, which must be one of the given values, <code>null</code> if missing.
	 */
	public String optOneOf(List<String> values) {
		return isAbsent() ? null : asOneOf(values);
	}

	/**
	 * The elements of the list value, each with its path.
	 */
	public List<Value> asList() {
		if (!isList()) {
			throw invalid(I18NConstants.EXPECTED_LIST);
		}
		return items((Collection<?>) _raw);
	}

	/**
	 * The elements of the list value, or this value as only element, if it is no list.
	 *
	 * <p>
	 * An empty list is returned, if the value is missing.
	 * </p>
	 */
	public List<Value> asListOrSingle() {
		if (isAbsent()) {
			return List.of();
		}
		if (isList()) {
			return items((Collection<?>) _raw);
		}
		return List.of(this);
	}

	private List<Value> items(Collection<?> collection) {
		List<Value> result = new ArrayList<>(collection.size());
		int index = 0;
		for (Object item : collection) {
			result.add(new Value(item, _path + "[" + index++ + "]"));
		}
		return result;
	}

	/**
	 * The entries of the dictionary value.
	 */
	public Entries asEntries() {
		if (!isMap()) {
			throw invalid(I18NConstants.EXPECTED_DICTIONARY);
		}
		return new Entries((Map<?, ?>) _raw, _path);
	}

	/**
	 * Creates the error reporting that this value is not the expected one.
	 *
	 * @param expected
	 *        Description of the expected value.
	 */
	public TopLogicException invalid(ResKey expected) {
		if (isAbsent()) {
			return missing(expected);
		}
		return new TopLogicException(
			I18NConstants.ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED.fill(_path, describe(_raw), expected));
	}

	/**
	 * Creates the error reporting that this value is missing.
	 *
	 * @param expected
	 *        Description of the expected value.
	 */
	public TopLogicException missing(ResKey expected) {
		return new TopLogicException(I18NConstants.ERROR_MISSING_ENTRY__PATH_EXPECTED.fill(_path, expected));
	}

	/**
	 * A short description of the given value for an error message.
	 */
	static String describe(Object value) {
		if (value instanceof Map<?, ?>) {
			return "{...}";
		}
		if (value instanceof Collection<?>) {
			return "[...]";
		}
		String result = value instanceof CharSequence ? "\"" + value + "\"" : String.valueOf(value);
		if (result.length() > MAX_DESCRIPTION_LENGTH) {
			return result.substring(0, MAX_DESCRIPTION_LENGTH - 1) + "…";
		}
		return result;
	}

}
