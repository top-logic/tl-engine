/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.internal.gen;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationDescriptorBuilder.VisitMethod;

/**
 * {@link ConfigurationDescriptor} for which a Java implementation can be generated.
 */
public interface ConfigurationDescriptorSPI extends ConfigurationDescriptor {

	@Override
	ConfigurationDescriptorSPI[] getSuperDescriptors();

	@Override
	Collection<? extends PropertyDescriptorSPI> getProperties();

	@Override
	PropertyDescriptorSPI[] getPropertiesOrdered();

	/**
	 * All visit methods declared in the configuration item interface.
	 */
	Collection<Method> getDeclaredVisitMethods();

	/**
	 * Method implementations for declared visit methods.
	 */
	Map<Method, VisitMethod> getVisitImplementations(Class<?> type);

	/**
	 * The set of concrete (visitable) types, the described {@link #getConfigurationInterface()} is
	 * the hierarchy root of.
	 * 
	 * @return Set of interfaces describing concrete types, or <code>null</code>, if this descriptor
	 *         does not describe a hierarchy root.
	 */
	Set<Class<?>> getConcreteTypes();

}
