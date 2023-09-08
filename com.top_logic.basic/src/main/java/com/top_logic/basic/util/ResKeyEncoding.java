/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.util.ResKey.Builder;
import com.top_logic.basic.xml.TagUtil;

/**
 * Encoding and decoding algorithms for {@link ResKey} values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResKeyEncoding {

	/**
	 * Character separating resource key alternatives.
	 */
	static final char FALLBACK_SEPARATOR = '|';

	private static final String FALLBACK_SEPARATOR_STRING = "" + FALLBACK_SEPARATOR;

	private static final String QUOTED_FALLBACK = FALLBACK_SEPARATOR_STRING + FALLBACK_SEPARATOR_STRING;

	/** Strings that need no special encoding when written as XML. */
	protected static final String DONT_ENCODE = "()+-*., =_";

	/** Prefix for a <code>null</code> value. */
	private static final char NULL_PREFIX = 'n';

	private static final String NULL_ENCODING = Character.toString(NULL_PREFIX);

	/** Prefix for a long text value (no longer in use). */
	private static final char TEXT_PREFIX = 't';

	/** Prefix for a {@link String} value. */
	private static final char STRING_PREFIX = 's';

	/** Prefix for an integer value. */
	private static final char INTEGER_PREFIX = 'i';

	/** Prefix for a float value. */
	private static final char FLOAT_PREFIX = 'f';

	/** Prefix for a long value. */
	private static final char LONG_PREFIX = 'l';

	/** Prefix for a double value. */
	private static final char DOUBLE_PREFIX = 'd';

	/** Prefix for a boolean value. */
	private static final char BOOLEAN_PREFIX = 'b';

	/** Prefix for a {@link java.util.Date} value. */
	private static final char DATE_PREFIX = 'D';

	/** Prefix for a {@link ResKey} value. */
	private static final char KEY_PREFIX = '[';

	/** Suffix for a {@link ResKey} value. */
	private static final char KEY_SUFFIX = ']';

	/** Prefix for a {@link ResKey} value. */
	private static final char ENUM_PREFIX = 'e';

	/** Prefix for an unknown value. */
	private static final char UNKNOWN_PREFIX = 'U';

	private static final Map<String, String> XML_ENTITIES =
		new MapBuilder<String, String>()
			.put("amp", "&")
			.put("lt", "<")
			.put("gt", ">")
			.put("quot", "\"")
			.put("apos", "'")
			.toMap();

	/**
	 * Use {@link ResKey#internalEncode()}
	 */
	@FrameworkInternal
	public static String encodeMessage(ResKey messageKey, Object argument) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(messageKey.getKey());
		appendArgument(buffer, argument);
		return buffer.toString();
	}

	/**
	 * Use {@link ResKey#internalEncode()}
	 */
	@FrameworkInternal
	public static String encodeMessage(ResKey messageKey, Object... arguments) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(messageKey.internalEncode());
		for (Object argument : arguments) {
			appendArgument(buffer, argument);
		}
		return buffer.toString();
	}

	/**
	 * Use {@link ResKey#internalEncode()}
	 */
	@FrameworkInternal
	public static String encodeLiteralText(String literalText) {
		StringBuilder buffer = new StringBuilder();
		appendArgument(buffer, literalText);
		return buffer.toString();
	}

	private static void appendArgument(StringBuilder buffer, Object argument) {
		buffer.append('/');
		buffer.append(encodeArgument(argument));
	}

	/**
	 * Convert the given object to a matching string representation.
	 * 
	 * @param anObject
	 *        The object to be converted.
	 * @return The string representation of the given object.
	 */
	private static String encodeArgument(Object anObject) {
		if (anObject == null) {
			return NULL_ENCODING;
		}
		if (anObject instanceof String) {
			return STRING_PREFIX + encodeStringContents((String) anObject);
		}
		else if (anObject instanceof Date) {
			return DATE_PREFIX + Long.toString(((Date) anObject).getTime());
		}
		else if (anObject instanceof ResKey) {
			return KEY_PREFIX + ResKey.encode((ResKey) anObject).replace(FALLBACK_SEPARATOR_STRING, QUOTED_FALLBACK)
				+ KEY_SUFFIX;
		}
		else if (anObject instanceof Enum<?>) {
			return ENUM_PREFIX + anObject.getClass().getName() + "." + anObject.toString();
		}
		else {
			char prefix;
	
			if (anObject instanceof Integer) {
				prefix = INTEGER_PREFIX;
				return prefix + anObject.toString();
			}
			else if (anObject instanceof Double) {
				prefix = DOUBLE_PREFIX;
				return prefix + anObject.toString();
			}
			else if (anObject instanceof Float) {
				prefix = FLOAT_PREFIX;
				return prefix + anObject.toString();
			}
			else if (anObject instanceof Long) {
				prefix = LONG_PREFIX;
				return prefix + anObject.toString();
			}
			else if (anObject instanceof Boolean) {
				prefix = BOOLEAN_PREFIX;
				return prefix + anObject.toString();
			}
			else {
				prefix = UNKNOWN_PREFIX;
				return prefix + encodeStringContents(anObject.toString());
			}
		}
	}


	/**
	 * Replace all non-valid characters by unicodes.
	 * 
	 * @param aString
	 *        The string to be encoded.
	 */
	private static String encodeStringContents(String aString) {
		char theChar;
		int theLength = aString.length();
		StringBuffer theBuffer = new StringBuffer(theLength);
	
		for (int thePos = 0; thePos < theLength; thePos++) {
			theChar = aString.charAt(thePos);
	
			if (Character.isLetterOrDigit(theChar)
				|| (DONT_ENCODE.indexOf(theChar) > -1)) {
				theBuffer.append(theChar);
			}
			else {
				theBuffer.append(encodeSpecialChar(theChar));
			}
		}
	
		return (theBuffer.toString());
	}


	/**
	 * Returns a unicode string for the given character.
	 * 
	 * Because the toString() method of Integer is slow, we encode some known values with constants.
	 * 
	 * @param aChar
	 *        The character to be encoded.
	 * @return The unicode for the character (HTML conform).
	 */
	private static String encodeSpecialChar(char aChar) {
		String theEncoding;
	
		switch (aChar) {
			case '&':
				theEncoding = TagUtil.AMP_CHAR;
				break;
			case '\"':
				theEncoding = TagUtil.QUOT_CHAR;
				break;
			case '\'':
				theEncoding = TagUtil.APOS_CHAR;
				break;
			case '<':
				theEncoding = TagUtil.LT_CHAR;
				break;
			case '>':
				theEncoding = TagUtil.GT_CHAR;
				break;
			default:
				theEncoding = "&#" + Integer.toString(aChar) + ';';
				break;
		}
	
		return (theEncoding);
	}

	/**
	 * Retrieves a {@link ResKey} from its {@link ResKey#internalEncode() encoded form}.
	 */
	public static ResKey decode(String encodedForm) {
		if (StringServices.isEmpty(encodedForm)) {
			return (null);
		}
	
		ResKey result = null;
		int start = 0;
		while (true) {
			int fallbackSep = encodedForm.indexOf(FALLBACK_SEPARATOR, start);

			// Skip escaped (doubled) `|` characters.
			while (fallbackSep >= 0 && fallbackSep < encodedForm.length()) {
				if (encodedForm.charAt(fallbackSep + 1) == FALLBACK_SEPARATOR) {
					fallbackSep = encodedForm.indexOf(FALLBACK_SEPARATOR, fallbackSep + 2);
				} else {
					break;
				}
			}

			boolean last = fallbackSep < 0;
			int end = last ? encodedForm.length() : fallbackSep;

			String part = encodedForm.substring(start, end);

			result = ResKey.fallback(result, atomicKey(part));

			if (last) {
				return result;
			}

			start = end + 1;
		}
	}

	static final String QUOTED_SPECIAL = "\\'|\\\"|\\\\";

	static final String LITERAL =
		"'" + "((?:" + "[^\\']*" + "|" + QUOTED_SPECIAL + ")*)" + "'" + "|" +
			"\"" + "((?:" + "[^\\\"]*" + "|" + QUOTED_SPECIAL + ")*)" + "\"";

	static final String LANGTAG = "@([a-zA-Z]+(?:-[a-zA-Z0-9]+)*)";

	static final Pattern TAGGED_STRING_PATTERN = Pattern.compile(LITERAL + LANGTAG);

	private static ResKey atomicKey(String part) {
		ResKey plain;
		int keyLength;
		if (part.startsWith("#(")) {
			Matcher matcher = TAGGED_STRING_PATTERN.matcher(part);
			keyLength = 2;
			matcher.region(keyLength, part.length());
			Builder translations = ResKey.builder();
			while (matcher.find()) {
				Locale lang = Locale.forLanguageTag(matcher.group(3));
				String value = unquote(matcher.group(1), matcher.group(2));
				translations.add(lang, value);
				keyLength = matcher.end();
			}
			if (keyLength < part.length() && part.charAt(keyLength) == ')') {
				// Skip ending parenthesis.
				keyLength++;
			}

			plain = translations.build();
		} else {
			String key = decodeKey(part);
			keyLength = key.length();
			plain = ResKey.internalCreate(key);
		}

		if (keyLength == part.length()) {
			return plain;
		}

		List<Object> arguments = decodeArguments(part, keyLength);

		if (keyLength == 0) {
			// An encoded literal string (no resource key at all).
			if (arguments.size() == 1 || arguments.get(0) instanceof String) {
				return ResKey.text((String) arguments.get(0));
			}
		}
	
		return ResKey.message(plain, arguments.toArray());
	}

	private static String unquote(String a, String b) {
		return a == null ? unquote(b) : unquote(a);
	}

	private static String unquote(String value) {
		return value.replaceAll("\\\\(.)", "$1");
	}

	private static String decodeKey(String encodedForm) {
		int keySeparatorIdx = encodedForm.indexOf('/');
		if (keySeparatorIdx < 0) {
			// No arguments at all.
			return encodedForm;
		}
	
		if (keySeparatorIdx == 0) {
			return "";
		}
	
		return encodedForm.substring(0, keySeparatorIdx);
	}


	private static List<Object> decodeArguments(String encodedArguments, int index) {
		List<Object> arguments = new ArrayList<>();
	
		int encodedLength = encodedArguments.length();
		while (index < encodedLength) {
			assert encodedArguments.charAt(index) == '/';
			// skip leading '/'
			index++;
	
			// Fetch type code.
			char typeCode;
			if (index < encodedLength) {
				typeCode = encodedArguments.charAt(index++);
			} else {
				// Missing argument.
				arguments.add(null);
				break;
			}
	
			// Find start of next argument
			int endIndex;
			boolean legacyEncodedSlash = false;

			if (typeCode == KEY_PREFIX) {
				endIndex = closingBrace(encodedArguments, index);
			} else {
				endIndex = index;
				while (true) {
					int nextIndex = encodedArguments.indexOf('/', endIndex);
					if (nextIndex < 0) {
						endIndex = encodedLength;
						break;
					}
					int afterNext = nextIndex + 1;
					if (afterNext < encodedLength && encodedArguments.charAt(afterNext) == '/') {
						// Legacy-encoded '/' character.
						legacyEncodedSlash = true;
						endIndex = afterNext + 1;
						continue;
					}
					endIndex = nextIndex;
					break;
				}
			}
	
			String encodedValue = encodedArguments.substring(index, endIndex);
			if (legacyEncodedSlash) {
				encodedValue = encodedValue.replace("//", "/");
			}
	
			Object arg = decodeArgument(typeCode, encodedValue);
			arguments.add(arg);
	
			index = endIndex;
		}
	
		return arguments;
	}


	private static int closingBrace(String str, final int index) {
		int openBrace = 1;
		int length = str.length();
		int pos = index;
		while (pos < length) {
			char ch = str.charAt(pos++);
			if (ch == '[') {
				openBrace++;
			} else if (ch == ']') {
				openBrace--;
				if (openBrace == 0) {
					return pos;
				}
			}
		}
		throw new IllegalArgumentException("Invalid key argument suffix '" + str.substring(index) + "'.");
	}

	/**
	 * Decodes a single object.
	 * 
	 * @param typeCode
	 *        The type code for the encoding.
	 * @param encodedValue
	 *        The encoded value.
	 * @return The decoded object.
	 */
	private static Object decodeArgument(char typeCode, String encodedValue) {
		switch (typeCode) {
			case NULL_PREFIX:
				return null;
			case UNKNOWN_PREFIX:
			case STRING_PREFIX:
				return decodeStringContents(encodedValue);
			case TEXT_PREFIX:
				return null;
			case INTEGER_PREFIX:
				return Integer.valueOf(encodedValue);
			case LONG_PREFIX:
				return Long.valueOf(encodedValue);
			case DOUBLE_PREFIX:
				return Double.valueOf(encodedValue);
			case FLOAT_PREFIX:
				return Float.valueOf(encodedValue);
			case BOOLEAN_PREFIX:
				return Boolean.valueOf(encodedValue);
			case DATE_PREFIX:
				return new Date(Long.parseLong(encodedValue));
			case KEY_PREFIX:
				return ResKey.decode(encodedValue.substring(0, encodedValue.length() - 1).replace(QUOTED_FALLBACK,
					FALLBACK_SEPARATOR_STRING));
			case ENUM_PREFIX:
				int classSep = encodedValue.lastIndexOf('.');
				String className = encodedValue.substring(0, classSep);
				String constantName = encodedValue.substring(classSep + 1);
				try {
					@SuppressWarnings("unchecked")
					Class<Enum<?>> enumClass = (Class<Enum<?>>) Class.forName(className);
					Enum<?>[] enumConstants = enumClass.getEnumConstants();
					for (Enum<?> enumConst : enumConstants) {
						if (enumConst.name().equals(constantName)) {
							return enumConst;
						}
					}
				} catch (ClassNotFoundException ex) {
					return null;
				}
				return null;
			default:
				return null;
		}
	}

	private static String decodeStringContents(String encoded) {
		int firstEscape = encoded.indexOf('&');
		if (firstEscape < 0) {
			return encoded;
		}

		int length = encoded.length();
		StringBuffer result = new StringBuffer(length);
		result.append(encoded, 0, firstEscape);
		for (int n = firstEscape; n < length; n++) {
			char ch = encoded.charAt(n);
			if (ch == '&') {
				int entityNameStartIndex = n + 1;
				if (entityNameStartIndex < length) {
					if (encoded.charAt(entityNameStartIndex) == '#') {
						int numberStartIndex = entityNameStartIndex + 1;
						int numberEndIndex = encoded.indexOf(';', numberStartIndex);
						if (numberEndIndex >= 0) {
							String entityNumber = encoded.substring(numberStartIndex, numberEndIndex);
							result.append((char) Integer.parseInt(entityNumber));

							// Skip entity reference.
							n = numberEndIndex;
							continue;
						}
					} else {
						int entityEndIndex = encoded.indexOf(';', entityNameStartIndex);
						String entityName = encoded.substring(entityNameStartIndex, entityEndIndex);
						String replacement = XML_ENTITIES.get(entityName);
						if (replacement != null) {
							result.append(replacement);

							// Skip entity reference.
							n = entityEndIndex;
							continue;
						}
					}
				}
			}
			result.append(ch);
		}

		return result.toString();
	}

}
