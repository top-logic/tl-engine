/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.lang.reflect.Method;

import com.top_logic.basic.util.ResKey;

/**
 * The place where a {@link ValueConverter} built by {@link ScriptConversions} converts a script
 * value to a Java value.
 *
 * <p>
 * The site names the conversion in the error message reported when a script value cannot be
 * converted to the expected Java type.
 * </p>
 *
 * @see ScriptConversions#fromScript(java.lang.reflect.Type, java.lang.reflect.AnnotatedElement,
 *      ConversionSite)
 */
public interface ConversionSite {

	/**
	 * The error message for a script value that cannot be converted at this site.
	 *
	 * @param expectedType
	 *        Description of the Java type expected at this site.
	 * @param value
	 *        The script value that cannot be converted.
	 */
	ResKey wrongValue(String expectedType, Object value);

	/**
	 * {@link ConversionSite} of an argument passed to a parameter of a TL-Script function.
	 *
	 * @param function
	 *        The name of the TL-Script function.
	 * @param parameter
	 *        The name of the parameter receiving the argument.
	 */
	record FunctionArgument(String function, String parameter) implements ConversionSite {
		@Override
		public ResKey wrongValue(String expectedType, Object value) {
			return I18NConstants.ERROR_WRONG_ARGUMENT__FUN_ARG_EXPECTED_VAL.fill(function, parameter, expectedType,
				value);
		}
	}

	/**
	 * {@link ConversionSite} of the result of a TL-Script implementation of a Java method.
	 *
	 * @param type
	 *        The Java type implemented by the script.
	 * @param method
	 *        The method of the given type whose result is converted.
	 */
	record MethodResult(Class<?> type, Method method) implements ConversionSite {
		@Override
		public ResKey wrongValue(String expectedType, Object value) {
			return I18NConstants.ERROR_WRONG_RESULT__TYPE_METHOD_EXPECTED_VAL.fill(type.getName(), method.getName(),
				expectedType, value);
		}
	}

}
