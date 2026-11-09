/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.reflect.JavaTypeUtil;

/**
 * {@link ConfigurationValueProvider} for generic Java {@link Type}s in source code notation.
 *
 * <p>
 * A type is written as in Java source code, but with {@link Class#getName() binary class names},
 * e.g. <code>java.util.Map$Entry&lt;java.lang.String, ? extends java.lang.Number&gt;[]</code>.
 * Primitive types are written with their keyword, e.g. <code>int[]</code>. Type variables cannot
 * be written, they are written as their erasure, see {@link JavaTypeUtil#toTypeName(Type)}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JavaTypeFormat extends AbstractConfigurationValueProvider<Type> {

	/**
	 * Singleton {@link JavaTypeFormat} instance.
	 */
	public static final JavaTypeFormat INSTANCE = new JavaTypeFormat();

	private static final String WILDCARD = "?";

	private static final String EXTENDS = "extends";

	private static final String SUPER = "super";

	private static final Map<String, Class<?>> PRIMITIVES = Map.of(
		"boolean", boolean.class,
		"byte", byte.class,
		"char", char.class,
		"short", short.class,
		"int", int.class,
		"long", long.class,
		"float", float.class,
		"double", double.class,
		"void", void.class);

	/**
	 * Creates a {@link JavaTypeFormat}.
	 */
	protected JavaTypeFormat() {
		super(Type.class);
	}

	@Override
	protected Type getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return new Parser(propertyName, propertyValue.toString()).parse();
	}

	@Override
	protected String getSpecificationNonNull(Type configValue) {
		return JavaTypeUtil.toTypeName(configValue);
	}

	/**
	 * Recursive descent parser for the notation of a type.
	 */
	private static final class Parser {

		private final String _propertyName;

		private final String _text;

		private int _pos;

		Parser(String propertyName, String text) {
			_propertyName = propertyName;
			_text = text;
		}

		Type parse() throws ConfigurationException {
			Type result = type();
			skipSpace();
			if (_pos < _text.length()) {
				throw error("end of type");
			}
			return result;
		}

		private Type type() throws ConfigurationException {
			String name = name();
			Type result;
			skipSpace();
			if (lookingAt('<')) {
				_pos++;
				Class<?> rawType = resolve(name);
				List<Type> arguments = new ArrayList<>();
				while (true) {
					arguments.add(argument());
					skipSpace();
					if (lookingAt(',')) {
						_pos++;
						continue;
					}
					expect('>');
					break;
				}
				try {
					result = JavaTypeUtil.newParameterizedType(rawType, arguments.toArray(new Type[arguments.size()]));
				} catch (IllegalArgumentException ex) {
					throw new ConfigurationException(ex.getMessage(), _propertyName, _text, ex);
				}
			} else {
				result = resolve(name);
			}
			while (true) {
				skipSpace();
				if (!lookingAt('[')) {
					break;
				}
				_pos++;
				skipSpace();
				expect(']');
				result = JavaTypeUtil.newArrayType(result);
			}
			return result;
		}

		private Type argument() throws ConfigurationException {
			skipSpace();
			if (!lookingAt(WILDCARD.charAt(0))) {
				return type();
			}
			_pos++;
			skipSpace();
			if (lookingAtWord(EXTENDS)) {
				_pos += EXTENDS.length();
				return JavaTypeUtil.newWildcardType(new Type[] { type() }, new Type[0]);
			}
			if (lookingAtWord(SUPER)) {
				_pos += SUPER.length();
				return JavaTypeUtil.newWildcardType(new Type[] { Object.class }, new Type[] { type() });
			}
			return JavaTypeUtil.newWildcardType(new Type[] { Object.class }, new Type[0]);
		}

		private String name() throws ConfigurationException {
			skipSpace();
			int start = _pos;
			while (_pos < _text.length()) {
				char ch = _text.charAt(_pos);
				if (!Character.isJavaIdentifierPart(ch) && ch != '.') {
					break;
				}
				_pos++;
			}
			if (_pos == start || !Character.isJavaIdentifierStart(_text.charAt(start))) {
				_pos = start;
				throw error("class name");
			}
			return _text.substring(start, _pos);
		}

		private Class<?> resolve(String name) throws ConfigurationException {
			Class<?> primitive = PRIMITIVES.get(name);
			if (primitive != null) {
				return primitive;
			}
			return ClassFormat.INSTANCE.getValue(_propertyName, name);
		}

		private void expect(char ch) throws ConfigurationException {
			if (!lookingAt(ch)) {
				throw error("'" + ch + "'");
			}
			_pos++;
		}

		private boolean lookingAt(char ch) {
			return _pos < _text.length() && _text.charAt(_pos) == ch;
		}

		private boolean lookingAtWord(String word) {
			int end = _pos + word.length();
			return _text.startsWith(word, _pos)
				&& (end == _text.length() || !Character.isJavaIdentifierPart(_text.charAt(end)));
		}

		private void skipSpace() {
			while (_pos < _text.length() && Character.isWhitespace(_text.charAt(_pos))) {
				_pos++;
			}
		}

		private ConfigurationException error(String expected) {
			return new ConfigurationException(
				I18NConstants.ERROR_INVALID_JAVA_TYPE__VALUE_EXPECTED_POSITION.fill(_text, expected, _pos),
				_propertyName, _text);
		}
	}

}
