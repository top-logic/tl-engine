/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.internal.gen;

import java.lang.reflect.Method;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * {@link PropertyDescriptor} of a {@link ConfigurationDescriptorSPI}.
 */
public interface PropertyDescriptorSPI extends PropertyDescriptor {

	@Override
	PropertyDescriptorSPI[] getSuperProperties();

	/**
	 * A getter method for this property.
	 * 
	 * <p>
	 * The getter may be declared in a super interface. In case of multiple inheritance, it is not
	 * specified, which getter is retrieved.
	 * </p>
	 * 
	 * @return The first getter method found for this property.
	 */
	Method getSomeGetter();

	/**
	 * The locally declared setter method for this property.
	 */
	Method getSetter();

	/**
	 * The getter {@link Method} of this property in the {@link ConfigurationItem} interface of
	 * {@link #getDescriptor()}.
	 * 
	 * <p>
	 * Note: If the property is inherited from a super interface and the getter method is not
	 * redeclared in the configuration interface of {@link #getDescriptor()}, the getter is
	 * <code>null</code>.
	 * </p>
	 * 
	 * @return The getter method locally declared in the configuration interface of
	 *         {@link #getDescriptor()}, or <code>null</code> for an inherited non-overridden
	 *         property.
	 * 
	 * @see #getAnnotation(Class)
	 */
	Method getLocalGetter();

	/**
	 * Whether the getter has in index parameter of type <code>int</code>.
	 * 
	 * <p>
	 * An indexed property is implicitly of type {@link List} with the return type of the getter as
	 * {@link #getElementType()}.
	 * </p>
	 */
	boolean isIndexed();

	/**
	 * Getter methods that access this collection property with a key argument.
	 */
	List<Method> getIndexedGetters();

	/**
	 * Cast the given {@link PropertyDescriptor} to a {@link PropertyDescriptorSPI}.
	 */
	static PropertyDescriptorSPI cast(PropertyDescriptor property) {
		return PropertyDescriptorSPI.class.cast(property);
	}

}
