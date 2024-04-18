/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeUpdate.StoreAlgorithm;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link StoreAlgorithm} invoking a method on an object's wrapper implementation.
 */
public class DerivedReflectionAttribute extends AbstractDerivedStorage<DerivedReflectionAttribute.Config<?>> {

	private static final String DOT = "\\.";

	private static final String NAME__PATTERN_SRC = TLModelUtil.NAME_PATTERN_SRC;

	private static final String PACKAGE__PATTERN_SRC = NAME__PATTERN_SRC + "(?:" + DOT + NAME__PATTERN_SRC + ")*";

	private static final String CLASS__PATTERN_SRC = "(?:" + PACKAGE__PATTERN_SRC + DOT + ")?" + NAME__PATTERN_SRC;

	private static final String METHOD_PATTERN_SRC =
		"(" + CLASS__PATTERN_SRC + ")" + DOT + "(" + NAME__PATTERN_SRC
			+ ")" + "(" + "\\(\\)" + ")?";

	private static final Pattern METHOD_PATTERN = Pattern.compile(METHOD_PATTERN_SRC);

	private static final Class<?>[] NO_PARAMS = {};

	private static final Object[] NO_ARGS = {};

	private final Method _method;

	/**
	 * Configuration options for {@link DerivedReflectionAttribute}.
	 */
	@TagName("reflective-storage")
	public interface Config<I extends DerivedReflectionAttribute> extends AbstractDerivedStorage.Config<I> {
		/**
		 * The public no-arg method to invoke on the object's Java implementation binding object.
		 * 
		 * <p>
		 * The method is given in full-qualified form: <code>[package].[class]#[method]()</code>
		 * </p>
		 */
		@RegexpConstraint(METHOD_PATTERN_SRC)
		String getMethod();
	}

	/**
	 * Creates a {@link DerivedReflectionAttribute}.
	 */
	public DerivedReflectionAttribute(InstantiationContext context, Config<?> config) {
		super(context, config);

		Matcher matcher = METHOD_PATTERN.matcher(config.getMethod());
		if (!matcher.matches()) {
			throw new IllegalStateException("Invalid method name: " + config.getMethod());
		}

		String className = matcher.group(1);
		String methodName = matcher.group(2);

		try {
			Class<?> type = Class.forName(className);
			_method = type.getDeclaredMethod(methodName, NO_PARAMS);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
			throw new IllegalStateException("The reflective method cannot be resolved: " + config.getMethod(), ex);
		}

		if (!Modifier.isPublic(_method.getModifiers())) {
			throw new IllegalStateException(
				"Only public methods may be used for reflective access: " + config.getMethod());
		}
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) throws AttributeException {
		try {
			return _method.invoke(object, NO_ARGS);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_CANNOT_ACCESS_ATTRIBUTE__OBJ_ATTR_ERR.fill(object, attribute, ex.getMessage()));
		}
	}

}
