/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.json;

import static com.top_logic.basic.json.I18NConstants.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.util.ResKey;

/**
 * Utility class for converting primitive Java objects and collections thereof
 * to and from JSON notation.
 * 
 * <p>
 * Supported values are:
 * </p>
 * 
 * <ul>
 * <li>{@link Integer}</li>
 * <li>{@link Number}</li>
 * <li>{@link Boolean}</li>
 * <li>{@link String}</li>
 * <li>{@link List} with all supported values as elements</li>
 * <li>{@link Map} with {@link String} keys and any supported values as map
 * values</li>
 * <li>The <code>null</code> value</li>
 * </ul>
 * 
 * <p>
 * The value storage and retrieval can be extended to arbitrary Java objects
 * that can be serialized to untyped JSON values through implementing a
 * {@link ValueAnalyzer} and {@link ValueFactory}.
 * </p>
 * 
 * @see #toString(Object) Default serialization to JSON notation of primitive
 *      Java values and collections thereof.
 * @see #fromString(CharSequence) Reconstructing primitive Java values and
 *      collections thereof from JSON notation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSON {

	private static final String INDENT = "    ";

	private static final int NO_INDENTATION = -1;

	/**
	 * Serialize the given object to {@link JSON} notation.
	 * 
	 * @param valueAnalyzer
	 *        The {@link ValueAnalyzer} used to analyze and encode supported
	 *        types.
	 * @param object
	 *        The object to serialize.
	 * @return A JSON string representing the object.
	 */
	public static String toString(ValueAnalyzer valueAnalyzer, Object object) {
		StringWriter buffer = new StringWriter();
		try {
			write(buffer, valueAnalyzer, object);
		} catch (IOException ex) {
			throw new UnreachableAssertion(ex);
		}
		return buffer.toString();
	}
	
	/**
	 * Serialize the given object to {@link JSON} notation.
	 * 
	 * <p>
	 * Supported values are:
	 * </p>
	 * 
	 * <ul>
	 * <li>{@link Integer}</li>
	 * <li>{@link Number}</li>
	 * <li>{@link Boolean}</li>
	 * <li>{@link String}</li>
	 * <li>{@link List} with all supported values as elements</li>
	 * <li>{@link Map} with {@link String} keys and any supported values as map values</li>
	 * <li>The <code>null</code> value</li>
	 * </ul>
	 * 
	 * @param object
	 *        The object to serialize.
	 * @return A JSON string representing the object.
	 */
	public static String toString(Object object) {
		return toString(DefaultValueAnalyzer.INSTANCE, object);
	}

	public static String toStringListContents(Object object) {
		return toStringListContents(object, DefaultValueAnalyzer.INSTANCE);
	}

	public static String toStringListContents(Object object, ValueAnalyzer valueAnalyzer) {
		StringWriter buffer = new StringWriter();
		try {
			writeListContents(buffer, valueAnalyzer, object);
		} catch (IOException ex) {
			throw new UnreachableAssertion(ex);
		}
		return buffer.toString();
	}
	
	public static String toStringMapContents(Object object) {
		return toStringMapContents(object, DefaultValueAnalyzer.INSTANCE);
	}
	
	public static String toStringMapContents(Object object, ValueAnalyzer valueAnalyzer) {
		StringWriter buffer = new StringWriter();
		try {
			writeMapContents(buffer, valueAnalyzer, object);
		} catch (IOException ex) {
			throw new UnreachableAssertion(ex);
		}
		return buffer.toString();
	}
	
	/**
	 * Constructs an object from the given {@link JSON} notation.
	 * 
	 * @param json
	 *        A serialized object in {@link JSON} notation.
	 * @return The re-constructed object.
	 * @throws ParseException
	 *         If the given string is not correct {@link JSON} syntax.
	 */
	public static Object fromString(CharSequence json) throws ParseException {
		return fromString(json, DefaultValueFactory.INSTANCE);
	}
	
	public static List<?> fromStringListContents(CharSequence json) throws ParseException {
		return (List<?>) fromStringListContents(json, DefaultValueFactory.INSTANCE);
	}

	public static Object fromStringListContents(CharSequence json, ValueFactory valueFactory) throws ParseException {
		return parseListContents(valueFactory, new BufferedInputSource(json));
	}

	public static Map<?, ?> fromStringMapContents(CharSequence json) throws ParseException {
		return (Map<?, ?>) fromStringMapContents(json, DefaultValueFactory.INSTANCE);
	}

	public static Object fromStringMapContents(CharSequence json, ValueFactory valueFactory) throws ParseException {
		return parseMapContents(valueFactory, new BufferedInputSource(json));
	}
	
	/**
	 * Constructs an object from the given {@link JSON} notation.
	 * 
	 * @param json
	 *        A serialized object in {@link JSON} notation.
	 * @param valueFactory
	 *        The factory used to instantiate objects with.
	 * @return The re-constructed object.
	 * @throws ParseException
	 *         If the given string is not correct {@link JSON} syntax.
	 */
	public static Object fromString(CharSequence json, ValueFactory valueFactory) throws ParseException {
		return parseValue(valueFactory, new BufferedInputSource(json));
	}

	/**
	 * Constructs an object from the {@link JSON} notation read from the given reader.
	 * 
	 * @param reader
	 *        A serialized object in {@link JSON} notation.
	 * @return The re-constructed object.
	 * @throws ParseException
	 *         If the given string is not correct {@link JSON} syntax.
	 */
	public static Object read(Reader reader) throws ParseException {
		return read(reader, DefaultValueFactory.INSTANCE);
	}
	
	/**
	 * Constructs an object from the {@link JSON} notation read from the given reader.
	 * 
	 * @param reader
	 *        A serialized object in {@link JSON} notation.
	 * @param valueFactory
	 *        The factory used to instantiate objects with.
	 * @return The re-constructed object.
	 * @throws ParseException
	 *         If the given string is not correct {@link JSON} syntax.
	 */
	public static Object read(Reader reader, ValueFactory valueFactory) throws ParseException {
		return parseValue(valueFactory, new ReaderInputSource(reader));
	}

	public static List<?> readListContents(Reader reader) throws ParseException {
		return (List<?>) readListContents(reader, DefaultValueFactory.INSTANCE);
	}
	
	public static Object readListContents(Reader reader, ValueFactory valueFactory) throws ParseException {
		return parseListContents(valueFactory, new ReaderInputSource(reader));
	}
	
	public static Map<?, ?> readMapContents(Reader reader) throws ParseException {
		return (Map<?, ?>) readMapContents(reader, DefaultValueFactory.INSTANCE);
	}
	
	public static Object readMapContents(Reader reader, ValueFactory valueFactory) throws ParseException {
		return parseMapContents(valueFactory, new ReaderInputSource(reader));
	}
	
	public static Object parseValue(ValueFactory valueFactory, InputSource input) throws ParseException, UnreachableAssertion {
		Parser parser = new Parser(valueFactory, input);
		Object result = parser.parse();
		checkEndOfInput(input, parser);
		return result;
	}
	
	public static Object parseMapContents(ValueFactory valueFactory, InputSource input) throws ParseException, UnreachableAssertion {
		Parser parser = new Parser(valueFactory, input);
		Object result = parser.parseMapContents();
		checkEndOfInput(input, parser);
		return result;
	}
	
	public static Object parseListContents(ValueFactory valueFactory, InputSource input) throws ParseException, UnreachableAssertion {
		Parser parser = new Parser(valueFactory, input);
		Object result = parser.parseListContents();
		checkEndOfInput(input, parser);
		return result;
	}

	private static void checkEndOfInput(InputSource input, Parser parser) throws ParseException {
		parser.skipWhiteSpace();
		try {
			if (input.hasNext()) {
				throw parseProblem(parser, ERROR_UNEXPECTED_INPUT);
			}
		} catch (IOException e) {
			throw parseProblem(parser, ERROR_READING_SOURCE);
		}
	}

	/**
	 * Serialize the given object to the given writer in {@link JSON} notation.
	 * 
	 * @see #toString(Object) Supported object types.
	 */
	public static void write(Appendable out, Object object) throws IOException {
		write(out, DefaultValueAnalyzer.INSTANCE, object);
	}

	/**
	 * Serialize the given object in compact form to the given writer in {@link JSON} notation.
	 * 
	 * @param out
	 *        The writer to append content to.
	 * @param valueAnalyzer
	 *        {@link JSON.ValueAnalyzer} to determine {@link JSON} types of given Java objects.
	 * 
	 * @see #toString(Object) Supported object types.
	 * @see #write(Appendable, ValueAnalyzer, Object, boolean)
	 */
	public static void write(Appendable out, ValueAnalyzer valueAnalyzer, Object object) throws IOException {
		write(out, valueAnalyzer, object, false);
	}

	/**
	 * Serialize the given object to the given writer in {@link JSON} notation.
	 * 
	 * @param out
	 *        The writer to append content to.
	 * @param valueAnalyzer
	 *        {@link JSON.ValueAnalyzer} to determine {@link JSON} types of given Java objects.
	 * @param pretty
	 *        Whether the output should be pretty printed.
	 */
	public static void write(Appendable out, ValueAnalyzer valueAnalyzer, Object object, boolean pretty)
			throws IOException {
		internalWrite(out, valueAnalyzer, object, pretty ? 0 : NO_INDENTATION);
	}

	private static void internalWrite(Appendable out, ValueAnalyzer valueAnalyzer, Object object, int indentation)
			throws IOException {
		switch (valueAnalyzer.getType(object)) {
		case ValueAnalyzer.INTEGER_TYPE:
			out.append(valueAnalyzer.getNumber(object).toString());
			break;
		case ValueAnalyzer.FLOAT_TYPE: {
			double num = valueAnalyzer.getNumber(object).doubleValue();
			if (num == Math.floor(num)) {
				out.append(Long.toString((long) num));
			} else {
				out.append(Double.toString(num));
			}
			break;
		}
		case ValueAnalyzer.STRING_TYPE:
			String s = valueAnalyzer.getString(object);
			writeString(out, s);
			break;
		case ValueAnalyzer.TRUE_TYPE:
			out.append("true");
			break;
		case ValueAnalyzer.FALSE_TYPE:
			out.append("false");
			break;
		case ValueAnalyzer.LIST_TYPE:
			out.append('[');
			internalWriteListContents(out, valueAnalyzer, object, nextIndent(indentation));
			out.append(']');
			break;
		case ValueAnalyzer.MAP_TYPE:
			out.append('{');
			internalWriteMapContents(out, valueAnalyzer, object, nextIndent(indentation));
			out.append('}');
			break;
		case ValueAnalyzer.DATE_TYPE:
			out.append("date(");
			out.append(XmlDateTimeFormat.INSTANCE.format(valueAnalyzer.getDateValue(object)));
			out.append(")");
			break;
		case ValueAnalyzer.NULL_TYPE:
			out.append("null");
			break;
		case ValueAnalyzer.UNSUPPORTED_TYPE:
			throw new IllegalArgumentException("Argument not supported for JSON serialization: " + object.getClass());
		default:
			throw new UnreachableAssertion("Type of value could not be determined: " + object);
		}
	}

	private static void writeString(Appendable out, String s) throws IOException {
		out.append('"');
		for (int n = 0, cnt = s.length(); n < cnt; n++) {
			char ch = s.charAt(n);
			switch (ch) {
				case 0:
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
				case 0x0b:
				case 0x0e:
				case 0x0f:
				case 0x10:
				case 0x11:
				case 0x12:
				case 0x13:
				case 0x14:
				case 0x15:
				case 0x16:
				case 0x17:
				case 0x18:
				case 0x19:
				case 0x1a:
				case 0x1b:
				case 0x1c:
				case 0x1d:
				case 0x1e:
				case 0x1f:
					out.append('\\');
					out.append('u');
					out.append('0');
					out.append('0');
					if (ch < 0x10) {
						out.append('0');
					} else {
						out.append(hexDigit(ch >>> 4));
					}
					out.append(hexDigit(ch & 0x0f));
					break;
			case '"': {
				out.append('\\');
				out.append('"');
				break;
			}
			case '\\': {
				out.append('\\');
				out.append('\\');
				break;
			}
			case '\n': {
				out.append('\\');
				out.append('n');
				break;
			}
			case '\r': {
				out.append('\\');
				out.append('r');
				break;
			}
			case '\t': {
				out.append('\\');
				out.append('t');
				break;
			}
			case '\b': {
				out.append('\\');
				out.append('b');
				break;
			}
			case '\f': {
				out.append('\\');
				out.append('f');
				break;
			}
			default: {
				out.append(ch);
			}
			}
		}
		out.append('"');
	}

	private static char hexDigit(int n) {
		if (n < 0x0a) {
			return (char) ('0' + n);
		} else {
			return (char) ('A' + (n - 0x0a));
		}
	}

	public static void writeListContents(Appendable out, Object list) throws IOException {
		writeListContents(out, DefaultValueAnalyzer.INSTANCE, list);
	}
	
	public static void writeListContents(Appendable out, ValueAnalyzer valueAnalyzer, Object list) throws IOException {
		internalWriteListContents(out, valueAnalyzer, list, NO_INDENTATION);
	}

	private static void internalWriteListContents(Appendable out, ValueAnalyzer valueAnalyzer, Object list,
			int indentation) throws IOException {
		Iterator<?> it = valueAnalyzer.getListIterator(list);
		if (it.hasNext()) {
			boolean first = true;
			hasNextEntry:
			while (true) {
				Object next = it.next();
				if (first) {
					internalWrite(out, valueAnalyzer, next, indentation);
				} else {
					switch (valueAnalyzer.getType(next)) {
						case ValueAnalyzer.LIST_TYPE:
						case ValueAnalyzer.MAP_TYPE:
							nlWithIndentation(out, indentation);
							internalWrite(out, valueAnalyzer, next, indentation);
							break;
						default:
							if (pretty(indentation)) {
								out.append(StringServices.BLANK_CHAR);
							}
							internalWrite(out, valueAnalyzer, next, indentation);
					}
				}
				if (it.hasNext()) {
					out.append(',');
					first = false;
				} else {
					break hasNextEntry;
				}
			}
		}
	}

	private static void nlWithIndentation(Appendable out, int indentation) throws IOException {
		if (pretty(indentation)) {
			out.append(StringServices.LINE_BREAK_CHAR);
			appendIndentation(out, indentation);
		}
	}

	private static int nextIndent(int indentation) {
		return pretty(indentation) ? indentation + 1 : NO_INDENTATION;
	}

	private static boolean pretty(int indentation) {
		return indentation > NO_INDENTATION;
	}

	private static void appendIndentation(Appendable out, int indentation) throws IOException {
		for (int i = 0; i < indentation; i++) {
			out.append(INDENT);
		}
	}

	public static void writeMapContents(Appendable out, Object object) throws IOException {
		writeMapContents(out, DefaultValueAnalyzer.INSTANCE, object);
	}
	
	public static void writeMapContents(Appendable out, ValueAnalyzer valueAnalyzer, Object object) throws IOException {
		internalWriteMapContents(out, valueAnalyzer, object, NO_INDENTATION);
	}

	private static void internalWriteMapContents(Appendable out, ValueAnalyzer valueAnalyzer, Object object,
			int indentiation) throws IOException {
		Iterator<? extends Map.Entry<?, ?>> it = valueAnalyzer.getMapIterator(object);
		if (it.hasNext()) {
			while (true) {
				nlWithIndentation(out, indentiation);
				Entry<?, ?> entry = it.next();
				String key = (String) entry.getKey();
				
				writeString(out, key);
				
				out.append(':');
				if (pretty(indentiation)) {
					out.append(' ');
				}
				internalWrite(out, valueAnalyzer, entry.getValue(), indentiation);
				if (it.hasNext()) {
					out.append(',');
				} else {
					break;
				}
			}
			nlWithIndentation(out, indentiation - 1);
		}
	}

	/**
	 * Exception denoting that parsing a {@link JSON} string failed due to
	 * invalid syntax.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class ParseException extends I18NException {
		private final int errorOffset;

		public ParseException(ResKey message, int pos) {
			super(buildMessage(message, pos));
			errorOffset = pos;
		}
		
		public ParseException(ResKey message, int pos, CharSequence source) {
			super(buildMessage(message, pos, source));
			errorOffset = pos;
		}

		/**
		 * Returns the position where the error was found.
		 */
		public int getErrorOffset() {
			return errorOffset;
		}

		private static ResKey buildMessage(ResKey message, int pos, CharSequence source) {
			return PARSE_ERROR__MESSAGE__BEFORE_PROBLEM__AFTER_PROBLEM.fill(
				buildMessage(message, pos),
				source.subSequence(Math.max(0, pos - 10), pos),
				source.subSequence(pos, Math.min(source.length(), pos + 10)));
		}

		private static ResKey buildMessage(ResKey message, int pos) {
			return PARSE_ERROR__MESSAGE_POSITION.fill(message, pos);
		}
	}

	/**
	 * Strategy for determining the {@link JSON} encoding for arbitrary
	 * objects.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface ValueAnalyzer {
		/**
		 * Objects that cannot be {@link JSON} encoded.
		 */
		int UNSUPPORTED_TYPE = -1;
		
		/**
		 * Objects encoded as <code>null</code> literal.
		 */
		int NULL_TYPE = 0;

		/**
		 * Objects encoded as <code>true</code> literal.
		 */
		int TRUE_TYPE = 1;
		
		/**
		 * Objects encoded as <code>false</code> literal.
		 */
		int FALSE_TYPE = 2;
		
		/**
		 * Objects encoded quoted string.
		 */
		int STRING_TYPE = 3;
		
		/**
		 * Objects encoded as integer numbers.
		 */
		int INTEGER_TYPE = 4;
		
		/**
		 * Objects encoded as floating point numbers.
		 */
		int FLOAT_TYPE = 5;

		/**
		 * Objects encoded as list literals <code>[value, value, ...]</code>.
		 */
		int LIST_TYPE = 6;
		
		/**
		 * Objects encoded as map literals <code>{key: value, ...}</code>.
		 */
		int MAP_TYPE = 7;

		/**
		 * Objects encoded as dates.
		 */
		int DATE_TYPE = 9;

		/**
		 * Determine the {@link JSON} type of the given object.
		 * 
		 * <p>
		 * For {@link #STRING_TYPE} objects, {@link #getString(Object)} is
		 * called to retrieve the string value. For {@link #LIST_TYPE} objects,
		 * {@link #getListIterator(Object)} is called to retrieve the list
		 * elements. For {@link #MAP_TYPE} objects,
		 * {@link #getMapIterator(Object)} is called to retrieve an entry set
		 * iterator.
		 * </p>
		 * 
		 * @param value
		 *        The object to analyze.
		 * @return one of {@link #NULL_TYPE}, {@link #TRUE_TYPE},
		 *         {@link #FALSE_TYPE}, {@link #STRING_TYPE},
		 *         {@link #INTEGER_TYPE}, {@link #FLOAT_TYPE},
		 *         {@link #LIST_TYPE}, {@link #MAP_TYPE},
		 *         {@link #DATE_TYPE}, or {@link #UNSUPPORTED_TYPE}.
		 */
		int getType(Object value);

		/**
		 * Retrieve the {@link String} value from a {@link #STRING_TYPE} object.
		 */
		String getString(Object object);

		/**
		 * Retrieve the {@link Number} value from a {@link #INTEGER_TYPE} or {@link #FLOAT_TYPE}
		 * object.
		 */
		Number getNumber(Object object);

		/**
		 * Retrieve the {@link Date} value from a {@link #DATE_TYPE} object.
		 */
		Date getDateValue(Object object);
		
		/**
		 * Retrieve the entry set {@link Iterator} from a {@link #MAP_TYPE} object.
		 */
		Iterator<? extends Map.Entry<?, ?>> getMapIterator(Object map);

		/**
		 * Retrieve the element {@link Iterator} from a {@link #LIST_TYPE} object.
		 */
		Iterator<?> getListIterator(Object list);
	}

	/**
	 * {@link JSON.ValueAnalyzer} for primitive Java types and collections thereof.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class DefaultValueAnalyzer implements ValueAnalyzer {

		public static final ValueAnalyzer INSTANCE = new DefaultValueAnalyzer();
		
		protected DefaultValueAnalyzer() {
			// Singleton constructor.
		}

		@Override
		public int getType(Object value) {
			if (value instanceof Number) {
				if (value instanceof Integer) {
					return INTEGER_TYPE;
				}
				if (value instanceof Long) {
					return INTEGER_TYPE;
				}
				if (value instanceof Float) {
					return FLOAT_TYPE;
				}
				if (value instanceof Double) {
					return FLOAT_TYPE;
				}
				
				return UNSUPPORTED_TYPE;
			}
			else if (value instanceof String) {
				return STRING_TYPE;
			}
			else if (value instanceof Boolean) {
				return ((Boolean) value).booleanValue() ? TRUE_TYPE : FALSE_TYPE;
			}
			else if (value instanceof List) {
				return LIST_TYPE;
			}
			else if (value instanceof Map) {
				return MAP_TYPE;
			}
			else if (value instanceof Date) {
				return DATE_TYPE;
			}
			else if (value == null) {
				return NULL_TYPE;
			}
			else {
				return UNSUPPORTED_TYPE;
			}
		}

		@Override
		public Iterator<?> getListIterator(Object list) {
			return ((List<?>) list).iterator();
		}

		@Override
		public Iterator<? extends Map.Entry<?, ?>> getMapIterator(Object map) {
			return ((Map<?, ?>) map).entrySet().iterator();
		}

		@Override
		public String getString(Object object) {
			return (String) object;
		}
		
		@Override
		public Date getDateValue(Object object) {
			return (Date) object;
		}

		@Override
		public Number getNumber(Object object) {
			return (Number) object;
		}

	}

	/**
	 * Factory to instantiate Java values in
	 * {@link JSON#fromString(CharSequence, JSON.ValueFactory) parse operations}.
	 * 
	 * <p>
	 * Through an implementation of this interface, the concrete Java types used
	 * to represent {@link JSON} values can be configured.
	 * </p>
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface ValueFactory {

		/**
		 * A value representing the <code>null</code> literal.
		 */
		Object createNullValue();

		/**
		 * A value representing the <code>true</code> literal.
		 */
		Object createTrueValue();

		/**
		 * A value representing the <code>false</code> literal.
		 */
		Object createFalseValue();

		/**
		 * Creates an object representing a string value from the given string
		 * contents.
		 */
		Object createStringValue(CharSequence stringContents);
		
		/**
		 * Creates an object representing an integral value.
		 */
		Object createIntegerValue(CharSequence integerValue) throws NumberFormatException;

		/**
		 * Creates an object representing an floating point value.
		 */
		Object createFloatValue(CharSequence floatValue) throws NumberFormatException;

		/**
		 * Creates an object representing a list.
		 */
		Object createListValue();
		
		/**
		 * Appends the given value to the given list value object.
		 */
		void appendList(Object list, Object value);

		/**
		 * Hook to modify list after processing.
		 */
		Object finishList(Object list);

		/**
		 * Creates an object representing a map.
		 */
		Object createMapValue();

		/**
		 * Puts the given key value pair into the given map value object.
		 */
		void putMap(Object map, String key, Object value);

		/**
		 * Hook to modify map after processing.
		 */
		Object finishMap(Object map);
	}

	/**
	 * Default implementation of {@link JSON.ValueFactory} that creates basic
	 * Java types and collections.
	 * 
	 * <p>
	 * This implementation used {@link Integer} for integral values and
	 * {@link Double} for floating point values. {@link Map}s are implemented
	 * with {@link HashMap} and {@link List}s are implemented with
	 * {@link ArrayList}.
	 * </p>
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class DefaultValueFactory implements ValueFactory {

		/**
		 * Singleton instance of {@link JSON.DefaultValueFactory}.
		 */
		public static final ValueFactory INSTANCE = new DefaultValueFactory();
		
		protected DefaultValueFactory() {
			// Singleton constructor.
		}

		@Override
		public Object createNullValue() {
			return null;
		}

		@Override
		public Object createTrueValue() {
			return Boolean.TRUE;
		}
		
		@Override
		public Object createFalseValue() {
			return Boolean.FALSE;
		}
		
		@Override
		public Object createStringValue(CharSequence stringContents) {
			return stringContents.toString();
		}

		@Override
		public Object createIntegerValue(CharSequence integerValue) {
			long parsedNumber = Long.parseLong(integerValue.toString());
			if (Integer.MIN_VALUE <= parsedNumber && parsedNumber <= Integer.MAX_VALUE) {
				return Integer.valueOf((int) parsedNumber);
			} else {
				return Long.valueOf(parsedNumber);
			}
		}

		@Override
		public Object createFloatValue(CharSequence floatValue) {
			return Double.valueOf(Double.parseDouble(floatValue.toString()));
		}

		@Override
		public Object createListValue() {
			return new ArrayList<>();
		}
		
		@Override
		public void appendList(Object list, Object value) {
			/* List is constructed via #createListValue() */
			@SuppressWarnings("unchecked")
			List<Object> typeSafeList = (List<Object>) list;
			typeSafeList.add(value);
		}

		@Override
		public Object finishList(Object list) {
			return list;
		}

		@Override
		public Object createMapValue() {
			// Ensure that parsing and formatting leads to the same result.
			return new LinkedHashMap<String, Object>();
		}
		
		@Override
		public void putMap(Object map, String key, Object value) {
			/* Map is constructed via #createMapValue() */
			@SuppressWarnings("unchecked")
			Map<String, Object> typeSafeMap = (Map<String, Object>) map;
			typeSafeMap.put(key, value);
		}

		@Override
		public Object finishMap(Object map) {
			return map;
		}

	}

	/**
	 * Base class for {@link JSON.Parser} input sources.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static abstract class InputSource {
		
		protected InputSource() {
			// Default constructor.
		}
		
		/**
		 * The current source position.
		 */
		public abstract int getPos();

		/**
		 * Whether there is another character available.
		 */
		protected abstract boolean hasNext() throws IOException;

		/**
		 * Looks up the next character without removing it from the input
		 * source.
		 */
		protected abstract char peek() throws IOException;

		/**
		 * Retrieves the next character and removes it from the input source.
		 */
		protected abstract char next() throws IOException;

		/**
		 * Ignores the next character.
		 */
		protected abstract void drop() throws IOException;

		/**
		 * Transfers the next character into a buffer for later lookup.
		 * 
		 * <p>
		 * {@link #clearBuffer()} must be called before this method may be
		 * called.
		 * </p>
		 * 
		 * @see #next()
		 * @see #bufferContent()
		 */
		protected abstract void bufferChar() throws IOException;
		
		/**
		 * Transfers the given character into the buffer for later lookup.
		 * 
		 * <p>
		 * {@link #clearBuffer()} must be called before this method may be
		 * called.
		 * </p>
		 * 
		 * @see #bufferContent()
		 */
		protected abstract void bufferChar(char ch);

		/**
		 * Allocates a buffer for storing characters.
		 * 
		 * @see #bufferChar()
		 */
		protected abstract void clearBuffer();

		/**
		 * The size of the current buffer.
		 * 
		 * <p>
		 * {@link #clearBuffer()} must be called before this method may be
		 * called.
		 * </p>
		 */
		protected abstract int bufferSize();

		/**
		 * The contents of the current buffer.
		 * 
		 * <p>
		 * {@link #clearBuffer()} must be called before this method may be
		 * called.
		 * </p>
		 * 
		 * <p>
		 * After this method has been called, {@link #clearBuffer()} must be
		 * called again, before any of the methods {@link #bufferChar()},
		 * {@link #bufferSize()}, or {@link #bufferContent()} may be called.
		 * </p>
		 */
		protected abstract CharSequence bufferContent();

	}
	
	private static final class ReaderInputSource extends InputSource {

		private final Reader reader;
		private final StringBuilder buffer = new StringBuilder();
		
		private int pos;
		private char currentChar;
		private boolean hasLookahead;

		public ReaderInputSource(Reader reader) {
			this.reader = reader;
			this.pos = 0;
		}
		
		@Override
		protected void bufferChar() throws IOException {
			bufferChar(next());
		}

		@Override
		protected void bufferChar(char ch) {
			buffer.append(ch);
		}

		@Override
		protected CharSequence bufferContent() {
			return buffer;
		}

		@Override
		protected int bufferSize() {
			return buffer.length();
		}

		@Override
		protected void clearBuffer() {
			buffer.setLength(0);
		}

		@Override
		protected void drop() throws IOException {
			next();
		}

		@Override
		public int getPos() {
			return pos;
		}

		@Override
		protected boolean hasNext() throws IOException {
			if (hasLookahead) {
				return true;
			}
			
			return lookahead();
		}

		private boolean lookahead() throws IOException {
			int result = reader.read();
			if (result < 0) {
				return false;
			}
			
			hasLookahead = true;
			currentChar = (char) result;
			
			return true;
		}

		@Override
		protected char next() throws IOException {
			if (hasLookahead) {
				hasLookahead = false;
				return currentChar;
			}
			
			if (! lookahead()) {
				throw new IndexOutOfBoundsException("No more input to read.");
			}
			
			hasLookahead = false;
			return currentChar;
		}

		@Override
		protected char peek() throws IOException {
			if (hasLookahead) {
				return currentChar;
			}

			if (! lookahead()) {
				throw new IndexOutOfBoundsException("No more input to read.");
			}
			
			return currentChar;
		}
		
	}
		
	private static final class BufferedInputSource extends InputSource {
		private final CharSequence source;
		private final int size;
		
		private int pos;

		private StringBuilder buffer;
		private boolean hasBufferCopy;
		private boolean hasBuffer;
		private int bufferStart;
		
		public BufferedInputSource(CharSequence source) {
			this.source = source;
			this.size = source.length();
		}

		public final CharSequence getSourceText() {
			return source;
		}

		@Override
		public final int getPos() {
			return pos;
		}

		@Override
		protected final boolean hasNext() {
			return pos < size;
		}
		
		@Override
		protected final char peek() {
			return source.charAt(pos);
		}
		
		@Override
		protected final char next() {
			handleDroppedChar();
			return source.charAt(pos++);
		}

		@Override
		protected final void drop() {
			handleDroppedChar();
			pos++;
		}

		private void handleDroppedChar() {
			if (hasBuffer) {
				if (! hasBufferCopy) {
					// Explicit buffer copy is required.
					if (buffer == null) {
						buffer = new StringBuilder();
					} else {
						buffer.setLength(0);
					}
					for (int n = bufferStart; n < pos; n++) {
						buffer.append(source.charAt(n));
					}
					hasBufferCopy = true;
				}
			}
		}

		@Override
		protected final void bufferChar() {
			if (hasBufferCopy) {
				bufferChar(source.charAt(pos++));
			} else {
				pos++;
			}
		}

		@Override
		protected void bufferChar(char ch) {
			buffer.append(ch);
		}

		@Override
		protected final void clearBuffer() {
			bufferStart = pos;
			hasBuffer = true;
			hasBufferCopy = false;
		}

		@Override
		protected final int bufferSize() {
			if (hasBufferCopy) {
				return buffer.length();
			} else {
				return pos - bufferStart;
			}
		}

		@Override
		protected final CharSequence bufferContent() {
			assert hasBuffer : "There is no buffer to read from.";
		
			hasBuffer = false;
			if (hasBufferCopy) {
				return buffer;
			} else {
				return source.subSequence(bufferStart, pos);
			}
		}
		
	}
	
	/**
	 * Parser for {@link JSON} notation.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class Parser {
		
		private final InputSource source;
		private final ValueFactory valueFactory;

		public Parser(ValueFactory valueFactory, InputSource source) {
			this.source = source;
			this.valueFactory = valueFactory;
		}

		/**
		 * This parsers {@link JSON.InputSource} where characters are read from.
		 */
		public final InputSource getSource() {
			return source;
		}

		/**
		 * This parsers {@link JSON.ValueFactory} that is used to create
		 * corresponding Java objects.
		 */
		public final ValueFactory getValueFactory() {
			return valueFactory;
		}
		
		/**
		 * Parses and returns the object from this parser's {@link #getSource()}.
		 * 
		 * @return The parsed object.
		 * @throws ParseException
		 *         If the {@link #getSource()} does not contain valid
		 *         {@link JSON} syntax.
		 */
		public final Object parse() throws ParseException {
			skipWhiteSpace();
			return readObject();
		}
		
		private Object readObject() throws ParseException {
			if (! hasNext()) {
				throw parseProblem(this, ERROR_UNEXPECTED_END_OF_INPUT);
			}
			char currentChar = peek();
			switch (currentChar) {
			case '\'':
			case '"': 
				return readString();
				
			case '[': 
				return readList();
				
			case '{': 
				return readMap();
				
			case '.': 
			case '-': 
			case '0': 
			case '1': 
			case '2': 
			case '3': 
			case '4': 
			case '5': 
			case '6': 
			case '7': 
			case '8': 
			case '9': 
				return readNumber();
				
			default: 
				if (Character.isJavaIdentifierStart(currentChar)) {
					char firstChar = currentChar;
					clearBuffer();
					bufferChar();
					while (hasNext() && (Character.isJavaIdentifierPart(currentChar = peek()) || (currentChar == '.'))) {
						bufferChar();
					}
					
					// Check for reserved words.
					String identifier = source.bufferContent().toString();
					switch (firstChar) {
					case 'd': {
						if ("date".equals(identifier)) {
							skipWhiteSpace();
							if (!hasNext()) {
								throw parseProblem(this, ERROR_UNEXPECTED_END_OF_INPUT);
							}
							assertCharacterValue('(', next());
							
							int dateStart = getPos();
							clearBuffer();
							while (hasNext() && (peek() != ')')) {
								bufferChar();
							}
							
							if (!hasNext()) {
								throw parseProblem(this, ERROR_UNEXPECTED_END_OF_INPUT);
							}
							assertCharacterValue(')', next());
							
							try {
								return XmlDateTimeFormat.INSTANCE.parseObject(bufferContent().toString());
							} catch (java.text.ParseException ex) {
								throw parseProblem(this, ERROR_INVALID_DATE_FORMAT, dateStart + ex.getErrorOffset());
							}
						}
						break;
					}
					case 'n': {
						if ("null".equals(identifier)) {
							return valueFactory.createNullValue();
						}
						break;
					}
					case 'f': {
						if ("false".equals(identifier)) {
							return valueFactory.createFalseValue();
						}
						break;
					}
					case 't': {
						if ("true".equals(identifier)) {
							return valueFactory.createTrueValue();
						}
						break;
					}
					}
				}
				throw parseProblem(this, ERROR_INVALID_CHARACTER__CHARACTER.fill(currentChar));
			}
		}

		private Object readNumber() throws ParseException {
			clearBuffer();
			
			int startPos = getPos();
			boolean isFloat = false;
			
			buffering:
			while (hasNext()) {
				char currentChar = peek();
				switch (currentChar) {
				case '.':
				case 'E':
					isFloat = true;
					bufferChar();
					break;
					
				case '-': 
				case '0': 
				case '1': 
				case '2': 
				case '3': 
				case '4': 
				case '5': 
				case '6': 
				case '7': 
				case '8': 
				case '9':
					bufferChar();
					break;

				default:
					break buffering;
				}
			}
			
			try {
				if (isFloat) {
					return valueFactory.createFloatValue(bufferContent());
				} else {
					return valueFactory.createIntegerValue(bufferContent());
				}
			} catch (NumberFormatException ex) {
				throw (ParseException) parseProblem(this, ERROR_INVALID_NUMBER_FORMAT, startPos).initCause(ex);
			}
		}

		private Object readMap() throws ParseException {
			drop();
			
			Object result = parseMapContents();
			
			if (! hasNext()) {
				throw parseProblem(this, ERROR_UNEXPECTED_END_OF_INPUT);
			}
			assertCharacterValue('}', peek());
			drop();
			
			return result;
		}

		private void assertCharacterValue(char expected, char actual) throws ParseException {
			if (actual != expected) {
				throw parseProblem(this, ERROR_UNEXPECTED_VALUE__EXPECTED_ACUTAL.fill(expected, actual));
			}
		}

		/**
		 * Parses the contents of a map from this parsers's
		 * {@link #getSource()}.
		 * 
		 * @return A map as created by
		 *         {@link JSON.ValueFactory#createMapValue()}.
		 * @throws ParseException
		 *         If the source does not contain a valid {@link JSON} map
		 *         content.
		 */
		public final Object parseMapContents() throws ParseException {
			Object result = valueFactory.createMapValue();
			
			skipWhiteSpace();
			
			if (! hasNext()) {
				return finishMap(result);
			}
			char currentChar = peek();
			if (currentChar == '}') {
				return finishMap(result);
			}

			while (true) {
				String key = readKey();
				skipWhiteSpace();

				if (! hasNext()) {
					throw parseProblem(this, ERROR_UNEXPECTED_END_OF_INPUT);
				}
				assertCharacterValue(':', next());

				skipWhiteSpace();
				valueFactory.putMap(result, key, readObject());
				
				skipWhiteSpace();
				if (! hasNext()) {
					return finishMap(result);
				}
				currentChar = peek();
				switch (currentChar) {
				case '}': {
						return finishMap(result);
				}
				case ',': {
					drop();
					skipWhiteSpace();
					break;
				}
				default: {
					throw parseProblem(this, ERROR_INVALID_CHARACTER__CHARACTER.fill(currentChar));
				}
				}
			}
		}

		private Object finishMap(Object result) {
			return valueFactory.finishMap(result);
		}

		private String readKey() throws ParseException {
			clearBuffer();

			char currentChar = peek();
			if (Character.isJavaIdentifierStart(currentChar)) {
				bufferChar();
				bufferPlainIdentifierPart();
			} else {
				switch (currentChar) {
				case '\'':
				case '"': 
					bufferString();
					break;

				default:
					throw parseProblem(this, ERROR_MISSING_MAP_KEY);
				}
			}
			
			if (bufferSize() == 0) {
				throw parseProblem(this, ERROR_MISSING_MAP_KEY_NAME);
			}
			
			return bufferContent().toString();
		}

		private void bufferPlainIdentifierPart() throws ParseException {
			while (hasNext()) {
				char currentChar = peek();
				if (Character.isJavaIdentifierPart(currentChar)) {
					bufferChar();
				} else {
					break;
				}
			}
		}

		private Object readList() throws ParseException {
			drop();
			
			Object result = parseListContents();

			if (! hasNext()) {
				throw parseProblem(this, ERROR_UNEXPECTED_END_OF_INPUT);
			}
			assertCharacterValue(']', peek());
			drop();
			
			return result;
		}

		/**
		 * Parses the contents of a list from this parsers's
		 * {@link #getSource()}.
		 * 
		 * @return A list as created by
		 *         {@link JSON.ValueFactory#createListValue()}.
		 * @throws ParseException
		 *         If the source does not contain a valid {@link JSON} list
		 *         content.
		 */
		public final Object parseListContents() throws ParseException {
			Object result = valueFactory.createListValue();

			skipWhiteSpace();
			
			if (! hasNext()) {
				return finishList(result);
			}
			char currentChar = peek();
			if (currentChar == ']') {
				return finishList(result);
			}

			while (true) {
				valueFactory.appendList(result, readObject());
				
				skipWhiteSpace();
				if (! hasNext()) {
					return finishList(result);
				}
				currentChar = peek();
				switch (currentChar) {
				case ',':
					// Continue reading.
					drop();
					skipWhiteSpace();
					break;

				case ']':
						return finishList(result);
					
				default:
					throw parseProblem(this, ERROR_INVALID_CHARACTER__CHARACTER.fill(currentChar));
				}
			}
		}

		private Object finishList(Object result) {
			return valueFactory.finishList(result);
		}

		private Object readString() throws ParseException {
			clearBuffer();
			bufferString();
			
			return valueFactory.createStringValue(bufferContent());
		}

		private void bufferString() throws ParseException {
			char quoteChar = next();
			while (true) {
				if (! hasNext()) {
					throw parseProblem(this, ERROR_UNTERMINATED_STRING_VALUE);
				}
				char currentChar = peek();
				if (currentChar == quoteChar) {
					drop();
					break;
				}
				else if (currentChar == '\\') {
					drop();
					if (! hasNext()) {
						throw parseProblem(this, ERROR_UNTERMINATED_STRING_ESCAPE);
					}
					
					char escapedChar = next();
					switch (escapedChar) {
						case 't':
							bufferChar('\t');
							break;
						case 'n':
							bufferChar('\n');
							break;
						case 'r':
							bufferChar('\r');
							break;
						case 'f':
							bufferChar('\f');
							break;
						case 'b':
							bufferChar('\b');
							break;
						case 'u':
							int code = 0;
							for (int n = 0; n < 4; n++) {
								if (!hasNext()) {
									throw parseProblem(this, ERROR_UNTERMINATED_UNICODE_ESCAPE);
								}
								char digitChar = Character.toUpperCase(next());
								
								int digit;
								if (digitChar >= '0' && digitChar <= '9') {
									digit = digitChar - '0';
								}
								else if (digitChar >= 'A' && digitChar <= 'F') {
									digit = 0x0a + digitChar - 'A';
								}
								else {
									throw parseProblem(this, ERROR_INVALID_UNICODE_ESCAPE);
								}
								code = (code << 4) | digit;
							}
							bufferChar((char) code);
							break;
						default:
							bufferChar(escapedChar);
							break;
					}
				}
				else {
					bufferChar();
				}
			}
		}
		
		private char next() throws ParseException {
			try {
				return source.next();
			} catch (IOException e) {
				throw (ParseException) parseProblem(this, ERROR_READING_SOURCE).initCause(e);
			}
		}

		private void bufferChar() throws ParseException {
			try {
				source.bufferChar();
			} catch (IOException e) {
				throw (ParseException) parseProblem(this, ERROR_READING_SOURCE).initCause(e);
			}
		}

		private void bufferChar(char ch) {
			source.bufferChar(ch);
		}
		
		public final void skipWhiteSpace() throws ParseException {
			while (hasNext()) {
				switch(peek()) {
				case ' ':
				case '\t':
				case '\n':
				case '\r': {
					drop();
					break;
				}
				
				default: 
					return;
				}
			}
		}

		private boolean hasNext() throws ParseException {
			try {
				return source.hasNext();
			} catch (IOException e) {
				throw (ParseException) parseProblem(this, ERROR_READING_SOURCE).initCause(e);
			}
		}

		private char peek() throws ParseException {
			try {
				return source.peek();
			} catch (IOException e) {
				throw (ParseException) parseProblem(this, ERROR_READING_SOURCE).initCause(e);
			}
		}

		private void drop() throws ParseException {
			try {
				source.drop();
			} catch (IOException e) {
				throw (ParseException) parseProblem(this, ERROR_READING_SOURCE).initCause(e);
			}
		}
		
		private int getPos() {
			return source.getPos();
		}

		private void clearBuffer() {
			source.clearBuffer();
		}

		private int bufferSize() {
			return source.bufferSize();
		}

		private CharSequence bufferContent() {
			return source.bufferContent();
		}
		
	}
	
	protected static ParseException parseProblem(Parser parser, ResKey message) {
		return parseProblem(parser, message, parser.getSource().getPos());
	}

	protected static ParseException parseProblem(Parser parser, ResKey message, int pos) {
		InputSource source = parser.getSource();
		if (source instanceof BufferedInputSource) {
			BufferedInputSource bufferedSource = (BufferedInputSource) source;
			
			return new ParseException(message, pos, bufferedSource.getSourceText());
		} else {
			return new ParseException(message, pos);
		}
	}
	
}
