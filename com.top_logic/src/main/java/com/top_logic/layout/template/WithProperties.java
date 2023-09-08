/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.ExpressionTemplate;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Interface for UI elements to provide variable contents to {@link HTMLTemplateFragment}s.
 * 
 * <p>
 * Note: This interface is best implemented as subclass of {@link WithPropertiesBase}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface WithProperties {

	/**
	 * Lookup a simple property of this UI element with the given name.
	 *
	 * @param propertyName
	 *        The name of the property to render.
	 * @return The value of the property.
	 * @throws NoSuchPropertyException
	 *         If this object does not define a property with the given name.
	 * 
	 * @see #renderProperty(DisplayContext, TagWriter, String)
	 */
	default Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
		throw errorNoSuchProperty(propertyName);
	}

	/**
	 * Renders the value of a property.
	 * 
	 * <p>
	 * Implementing this method instead of {@link #getPropertyValue(String)} is more efficient for
	 * rendering complex values that are not available as single object.
	 * </p>
	 *
	 * @param context
	 *        The current rendering context.
	 * @param out
	 *        The {@link TagWriter} to render to.
	 * @param propertyName
	 *        The name of the property whose value should be written.
	 * @throws IOException
	 *         If rendering fails.
	 */
	default void renderProperty(DisplayContext context, TagWriter out, String propertyName) throws IOException {
		try {
			Object value = getPropertyValue(propertyName);
			ExpressionTemplate.renderValue(context, out, value);
		} catch (NoSuchPropertyException ex) {
			throw WithProperties.reportError(this, propertyName);
		}
	}

	/**
	 * All properties defined on this instance.
	 * 
	 * <p>
	 * Note: This method is exclusively for error reporting. The implementation may be extremely
	 * inefficient.
	 * </p>
	 * 
	 * @return The properties available for this instance, if this information is available/known.
	 *         Note: If this information is unknown does not mean that no properties are available.
	 */
	default Optional<Collection<String>> getAvailableProperties() {
		return Optional.empty();
	}

	/**
	 * Throws an error that complains about the non-existence of a property with the given name.
	 */
	default RuntimeException errorNoSuchProperty(String propertyName) throws NoSuchPropertyException {
		throw new NoSuchPropertyException("No such property '" + propertyName + "' in '" + this + "'.");
	}

	/**
	 * Utility to convert a {@link NoSuchPropertyException} to a user-friendly message.
	 * 
	 * <p>
	 * Note: The method {@link #getPropertyValue(String)} cannot report useful information by
	 * itself, if a stack of wrapped {@link WithProperties} implementations was searched for a
	 * property and the innermost failed.
	 * </p>
	 *
	 * @param properties
	 *        The object that was accessed.
	 * @param propertyName
	 *        The name of the property that failed.
	 * @return Never returns, throws a {@link TopLogicException} with error information.
	 */
	static TopLogicException reportError(WithProperties properties, String propertyName) {
		return reportError(properties, propertyName, properties.getAvailableProperties());
	}

	/**
	 * Utility to convert a {@link NoSuchPropertyException} to a user-friendly message.
	 * 
	 * <p>
	 * Note: The method {@link #getPropertyValue(String)} cannot report useful information by
	 * itself, if a stack of wrapped {@link WithProperties} implementations was searched for a
	 * property and the innermost failed.
	 * </p>
	 *
	 * @param self
	 *        The object that was accessed.
	 * @param propertyName
	 *        The name of the property that failed.
	 * @param availableProperties
	 *        All available properties, if this information is known.
	 * @return Never returns, throws a {@link TopLogicException} with error information.
	 */
	static TopLogicException reportError(Object self, String propertyName,
			Optional<Collection<String>> availableProperties) {
		if (availableProperties.isPresent()) {
			List<String> sorted = new ArrayList<>(availableProperties.get());
			Collections.sort(sorted);
			throw new TopLogicException(
				I18NConstants.ERROR_NO_SUCH_PROPERTY__SELF_NAME_AVAILABLE.fill(self, propertyName, sorted));
		} else {
			throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_PROPERTY__SELF_NAME.fill(self, propertyName));
		}
	}

	/**
	 * Constructs a {@link WithProperties} object from a {@link Map} of values.
	 */
	public static WithProperties fromMap(Map<String, ?> values) {
		MapWithProperties properties = new MapWithProperties();

		properties.putAll(values);

		return properties;
	}

}
