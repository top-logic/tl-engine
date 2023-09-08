/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;
import java.util.Collection;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.layout.template.WithProperties;

/**
 * Algorithm for looking up properties for a {@link WithProperties} implementation.
 */
public interface WithPropertiesDelegate {

	/**
	 * Creates a {@link WithPropertiesDelegate} for the given type based on reflection.
	 */
	static WithPropertiesDelegate lookup(Class<?> type) {
		return WithPropertiesDelegateFactory.lookup(type);
	}

	/**
	 * Representation of a single property of a {@link WithPropertiesDelegate}.
	 */
	interface Property {

		/**
		 * The name of the property that is accessed by this delegate.
		 */
		String getPropertyName();

		/**
		 * The documentation of this property.
		 */
		ResKey getDocumentation();

		/**
		 * Retrieves the value of this property from the given object.
		 */
		Object getPropertyValue(Object self);

		/**
		 * Renders the value of this property of the given object.
		 */
		void renderProperty(DisplayContext context, TagWriter out, Object self) throws IOException;

	}

	/**
	 * Implementation of {@link WithProperties#getPropertyValue(String)}.
	 */
	Object getPropertyValue(Object self, String propertyName) throws NoSuchPropertyException;

	/**
	 * Implementation of {@link WithProperties#getAvailableProperties()}.
	 * 
	 * @param self
	 *        The object with properties.
	 * 
	 * @return The properties available for this instance.
	 * 
	 * @see #getAvailablePropertyInstances()
	 */
	Collection<String> getAvailableProperties(Object self);

	/**
	 * Access to all property information of this delegate.
	 * 
	 * @see #getAvailableProperties(Object)
	 */
	Collection<? extends Property> getAvailablePropertyInstances();

	/**
	 * Implementation of {@link WithProperties#renderProperty(DisplayContext, TagWriter, String)}.
	 */
	void renderProperty(DisplayContext context, TagWriter out, Object self, String propertyName) throws IOException;

}