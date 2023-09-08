/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.declarative.mapping;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static com.top_logic.basic.config.misc.TypedConfigUtil.*;

import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.model.TLObject;

/**
 * A {@link Function} that returns the configured property.
 * <p>
 * Can navigate {@link ConfigurationItem}s and {@link TLObject}s. It can therefore be used to
 * navigate into a {@link TLObject} and there into a {@link ConfigurationItem}.
 * </p>
 * <p>
 * If the path is empty, the given value is returned.
 * </p>
 * <p>
 * This can be used for example at:
 * {@link com.top_logic.layout.form.declarative.DeclarativeFormBuilder.Config#getModelToFormMapping()}
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TLPropertyAccess
		extends AbstractConfiguredInstance<TLPropertyAccess.Config>
		implements Function<Object, Object> {

	/** {@link ConfigurationItem} for the {@link TLPropertyAccess}. */
	public interface Config extends PolymorphicConfiguration<TLPropertyAccess> {

		/** Property name of {@link #getProperty()}. */
		String PROPERTY = "property";

		/** Property name of {@link #shouldIgnoreNull()}. */
		String IGNORE_NULL = "ignore-null";

		/** Property name of {@link #shouldIgnoreTypeError()}. */
		String IGNORE_TYPE_ERROR = "ignore-type-error";

		/** The property (or path of properties) that should be navigated. */
		@Name(PROPERTY)
		@Mandatory
		@Format(CommaSeparatedStrings.class)
		List<String> getProperty();

		/**
		 * Whether navigating into a null value should return null and not throw a
		 * {@link NullPointerException}.
		 */
		@Name(IGNORE_NULL)
		@BooleanDefault(true)
		boolean shouldIgnoreNull();

		/**
		 * Whether navigating into a value that is not a {@link ConfigurationItem} or
		 * {@link TLObject} or does not have the next property should return null and not throw an
		 * exception.
		 */
		@Name(IGNORE_TYPE_ERROR)
		@BooleanDefault(false)
		boolean shouldIgnoreTypeError();

	}

	private final List<String> _path;

	private final boolean _ignoreNull;

	private final boolean _ignoreTypeError;

	/**
	 * Called by the {@link TypedConfiguration} for creating a
	 * {@link TLPropertyAccess}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public TLPropertyAccess(InstantiationContext context, Config config) {
		super(context, config);
		_path = config.getProperty();
		_ignoreNull = config.shouldIgnoreNull();
		_ignoreTypeError = config.shouldIgnoreTypeError();
	}

	@Override
	public Object apply(Object value) {
		Object result = navigatePath(value);
		if (result instanceof ConfigurationItem) {
			/* Copy the item to avoid side-effect changes in the KnowledgeBase. */
			return copy((ConfigurationItem) result);
		}
		return result;
	}

	private Object navigatePath(Object startValue) {
		Object currentValue = startValue;
		for (String property : _path) {
			if (currentValue instanceof TLObject) {
				TLObject tlObject = (TLObject) currentValue;
				if (tlObject.tType().getPart(property) == null) {
					return handleNavigationTypeError(startValue, currentValue, property);
				}
				currentValue = tlObject.tValueByName(property);
				continue;
			}
			if (currentValue instanceof ConfigurationItem) {
				ConfigurationItem configItem = (ConfigurationItem) currentValue;
				if (!configItem.descriptor().hasProperty(property)) {
					return handleNavigationTypeError(startValue, currentValue, property);
				}
				currentValue = getProperty(configItem, property);
				continue;
			}
			if (currentValue == null) {
				return handleNavigationIntoNull(startValue, property);
			}
			return handleNavigationTypeError(startValue, currentValue, property);
		}
		return currentValue;
	}

	private ConfigurationItem handleNavigationIntoNull(Object value, String property) {
		if (_ignoreNull) {
			return null;
		}
		throw new NullPointerException("Failed to navigate into '" + property + "' when navigating path '"
			+ String.join(" > ", _path) + "' on value: " + value + ". The last value is: null");
	}

	private ConfigurationItem handleNavigationTypeError(Object value, Object currentValue, String property) {
		if (_ignoreTypeError) {
			return null;
		}
		throw new RuntimeException("Failed to navigate into '" + property + "' when navigating path '"
			+ String.join(" > ", _path) + "' on value: " + value + ". The last navigable value was: " + currentValue);
	}

}
