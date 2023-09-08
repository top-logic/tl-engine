/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.annotation.Format;

/**
 * Textual reference of a {@link Method} object.
 * 
 * <p>
 * A {@link MethodRef} can be parsed from configuration, even if the referenced class or method does
 * not exist. The resolution of the runtime {@link Method} is deferrd to {@link #resolve()}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Format(MethodRef.JavaMethodFormat.class)
public class MethodRef {

	private final String _className;

	private final String _name;

	/**
	 * Creates a {@link MethodRef}.
	 *
	 * @param className
	 *        See {@link #getDeclaringClassName()}
	 * @param name
	 *        Sess {@link #getName()}.
	 */
	public MethodRef(String className, String name) {
		_className = className;
		_name = name;
	}

	/**
	 * The qualified class name that declares the method.
	 */
	public String getDeclaringClassName() {
		return _className;
	}

	/**
	 * The name of the referenced method.
	 * 
	 * @see #getDeclaringClassName()
	 */
	public String getName() {
		return _name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_className == null) ? 0 : _className.hashCode());
		result = prime * result + ((_name == null) ? 0 : _name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodRef other = (MethodRef) obj;
		if (_className == null) {
			if (other._className != null)
				return false;
		} else if (!_className.equals(other._className))
			return false;
		if (_name == null) {
			if (other._name != null)
				return false;
		} else if (!_name.equals(other._name))
			return false;
		return true;
	}

	/**
	 * Resolves the runtime {@link Method} implementation.
	 */
	public Method resolve() throws ConfigurationException {
		try {
			Method method = resolveType().getMethod(_name, OperationFactory.NO_ARGS);
			if (method.getReturnType() == void.class) {
				throw new ConfigurationException(
					I18NConstants.ERROR_METHOD_DECLARED_VIOD__VALUE.fill(toString()), null, null);
			}
			return method;
		} catch (NoSuchMethodException ex) {
			throw new ConfigurationException(I18NConstants.ERROR_METHOD_NOT_FOUND__CLASS_METHOD.fill(
				_className, _name), null, null, ex);
		} catch (SecurityException ex) {
			throw new ConfigurationException(I18NConstants.ERROR_CANNOT_ACCESS_METHOD__METHOD.fill(toString()),
				null, null, ex);
		}

	}

	private Class<?> resolveType() throws ConfigurationException {
		return ConfigUtil.getClassForNameMandatory(Object.class, "", _className);
	}

	@Override
	public String toString() {
		return getDeclaringClassName() + "#" + getName() + "()";
	}

	/**
	 * {@link ConfigurationValueProvider} parsing a no-arg {@link MethodRef} object in the format
	 * <code>[class-name]#[method-name]()</code>.
	 */
	public static class JavaMethodFormat extends AbstractConfigurationValueProvider<MethodRef> {

		private static final String NAME_PATTERN = "[a-zA-Z_][a-zA-Z0-9_]*";

		private static final String CLASS_PATTERN = NAME_PATTERN + "(?:" + "\\." + NAME_PATTERN + ")*";

		private static final Pattern PATTERN = Pattern
			.compile("(" + CLASS_PATTERN + ")" + "#" + "(" + NAME_PATTERN + ")" + "\\(" + "\\)");

		/**
		 * Singleton {@link JavaMethodFormat} instance.
		 */
		public static final JavaMethodFormat INSTANCE = new JavaMethodFormat();

		private JavaMethodFormat() {
			super(MethodRef.class);
		}

		@Override
		protected MethodRef getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			String value = propertyValue.toString();
			Matcher matcher = PATTERN.matcher(value);
			if (!matcher.matches()) {
				throw new ConfigurationException(
					I18NConstants.ERROR_METHOD_PATTERN_MISMATCH__VALUE.fill(value),
					propertyName, propertyValue);
			}
			String className = matcher.group(1);
			String methodName = matcher.group(2);
			return new MethodRef(className, methodName);
		}

		@Override
		protected String getSpecificationNonNull(MethodRef configValue) {
			return configValue.toString();
		}

	}

}
