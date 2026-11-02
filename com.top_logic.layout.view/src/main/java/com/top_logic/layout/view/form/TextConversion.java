/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.format.NumberFormats;
import com.top_logic.basic.type.PrimitiveTypeUtil;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.field.FieldControlRegistry;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.util.error.TopLogicException;

/**
 * Reads a value from a text the user entered, for the types of values that have a text form of
 * their own: strings, numbers and points in time.
 *
 * <p>
 * A string is taken as it is, a number is read in the number format of the field, a point in time
 * in its date format or in the formats a person is used to type it in. A text that is no value of
 * the type is refused with a {@link TopLogicException}.
 * </p>
 *
 * @see TLFreeEntry
 */
final class TextConversion {

	private TextConversion() {
		// Static utilities only.
	}

	/**
	 * Whether the values of the given model type can be read from a text.
	 */
	static boolean takesText(TLType type) {
		if (!(type instanceof TLPrimitive primitive)) {
			return false;
		}
		switch (primitive.getKind()) {
			case STRING:
			case INT:
			case FLOAT:
			case DATE:
				return takesText(FieldControlService.valueType(type));
			default:
				return false;
		}
	}

	/**
	 * Whether values of the given Java type can be read from a text.
	 */
	static boolean takesText(Class<?> valueType) {
		Class<?> type = PrimitiveTypeUtil.asNonPrimitive(valueType);
		return type == String.class || type == Date.class || isIntegral(type) || isFractional(type);
	}

	/**
	 * The function reading a value of the given field from a text, or {@code null} where the values
	 * of the field cannot be read from a text.
	 *
	 * @param part
	 *        The edited attribute, deciding by its type; {@code null} where the field edits no
	 *        attribute, and the value type of the field decides.
	 * @param field
	 *        The edited field, whose formats read the text.
	 */
	static Function<String, Object> conversion(TLStructuredTypePart part, FieldSpec field) {
		if (part != null && !takesText(part.getType())) {
			return null;
		}
		Class<?> type = PrimitiveTypeUtil.asNonPrimitive(field.getValueType());
		if (type == String.class) {
			return text -> text;
		}
		if (type == Date.class) {
			List<DateFormat> formats = dateFormats(field);
			return text -> parseDate(formats, text);
		}
		if (isIntegral(type) || isFractional(type)) {
			Format format = FieldControlRegistry.numberFormat(field);
			return text -> parseNumber(format, type, text);
		}
		return null;
	}

	private static boolean isIntegral(Class<?> type) {
		return type == Long.class || type == Integer.class || type == Short.class || type == Byte.class;
	}

	private static boolean isFractional(Class<?> type) {
		return type == Double.class || type == Float.class;
	}

	/**
	 * The number the given text denotes, as value of the given type.
	 */
	private static Object parseNumber(Format format, Class<?> type, String text) {
		Number number = NumberFormats.parse(format, text);
		if (number == null) {
			throw new TopLogicException(I18NConstants.ERROR_INVALID_NUMBER__VALUE.fill(text));
		}
		if (type == Double.class) {
			return Double.valueOf(number.doubleValue());
		}
		if (type == Float.class) {
			return Float.valueOf(number.floatValue());
		}
		long value = number.longValue();
		if (value != number.doubleValue()) {
			// A whole number is expected, a fraction would be dropped silently.
			throw new TopLogicException(I18NConstants.ERROR_INVALID_NUMBER__VALUE.fill(text));
		}
		if (type == Integer.class) {
			return Integer.valueOf((int) value);
		}
		if (type == Short.class) {
			return Short.valueOf((short) value);
		}
		if (type == Byte.class) {
			return Byte.valueOf((byte) value);
		}
		return Long.valueOf(value);
	}

	/**
	 * The formats a point in time is read in: the format of the field first, then the formats a
	 * person is used to type a value of its kind in.
	 */
	private static List<DateFormat> dateFormats(FieldSpec field) {
		List<DateFormat> result = new ArrayList<>();
		if (field.getDateFormat() != null) {
			result.add(field.getDateFormat());
		}
		for (DateFormat format : field.getDateKind().inputFormats()) {
			if (!result.contains(format)) {
				result.add(format);
			}
		}
		return result;
	}

	/**
	 * The point in time the given text denotes in one of the given formats, the whole text being
	 * consumed.
	 */
	private static Date parseDate(List<DateFormat> formats, String text) {
		for (DateFormat format : formats) {
			// A format is not safe to be used by several threads at once.
			DateFormat parser = (DateFormat) format.clone();
			ParsePosition position = new ParsePosition(0);
			Date result = parser.parse(text, position);
			if (result != null && position.getErrorIndex() < 0 && position.getIndex() == text.length()) {
				return result;
			}
		}
		throw new TopLogicException(I18NConstants.ERROR_INVALID_DATE__VALUE.fill(text));
	}

}
