/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.misc;

import static com.top_logic.basic.config.TypedConfiguration.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * Utilities for working with the {@link TypedConfiguration} and {@link ConfigurationItem}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TypedConfigUtil {

	/**
	 * Creates an instance from the given {@link PolymorphicConfiguration} type.
	 * <p>
	 * <em>Important:</em> Use
	 * {@link #createInstance(InstantiationContext, PolymorphicConfiguration)} instead, if there is
	 * already an {@link InstantiationContext}.
	 * </p>
	 * <p>
	 * A simple {@link InstantiationContext} is used that reports errors be throwing an exception
	 * for the first error.
	 * </p>
	 * 
	 * @param configInterface
	 *        Is not allowed to be null.
	 * @return Never null.
	 * 
	 * @see #newConfiguredInstance(Class)
	 */
	public static <T, C extends PolymorphicConfiguration<T>> T createInstance(Class<C> configInterface) {
		/* As this InstantationContext always throws Exceptions, it should never return null. */
		return Objects.requireNonNull(createInstance(context(), configInterface));
	}

	/**
	 * Creates an instance from the given {@link PolymorphicConfiguration} type.
	 * 
	 * @param configInterface
	 *        Is not allowed to be null.
	 * @return Can be null, if the instantiation failed and the error was logged to the given
	 *         {@link InstantiationContext}.
	 * 
	 * @see #newConfiguredInstance(Class)
	 */
	public static <T, C extends PolymorphicConfiguration<T>> T createInstance(
			InstantiationContext context, Class<C> configInterface) {
		return createInstance(context, newConfigItem(configInterface));
	}

	/**
	 * Creates an instance of the object described with the given {@link PolymorphicConfiguration}.
	 * <p>
	 * <em>Important:</em> Use
	 * {@link #createInstance(InstantiationContext, PolymorphicConfiguration)} instead, if there is
	 * already an {@link InstantiationContext}.
	 * </p>
	 * 
	 * @return Null, if the given config is null.
	 */
	public static <T> T createInstance(PolymorphicConfiguration<T> config) {
		return createInstance(context(), config);
	}

	private static InstantiationContext context() {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
	}

	/**
	 * Creates an implementation described by the given {@link PolymorphicConfiguration}.
	 * 
	 * @return The implementation instance, or <code>null</code>, if the given configuration is
	 *         <code>null</code>.
	 */
	public static <T> T createInstance(InstantiationContext context, PolymorphicConfiguration<T> config) {
		if (config == null) {
			return null;
		}
		return context.getInstance(config);
	}

	/**
	 * Instantiates the given {@link List} of {@link PolymorphicConfiguration}s.
	 * 
	 * <p>
	 * A simple {@link InstantiationContext} is used that reports errors be throwing an exception
	 * for the first error.
	 * </p>
	 * 
	 * @see TypedConfiguration#getInstanceList(InstantiationContext, List)
	 * @see #createInstance(PolymorphicConfiguration)
	 */
	public static <I> List<I> createInstanceList(List<? extends PolymorphicConfiguration<? extends I>> configurations) {
		return TypedConfiguration.getInstanceList(context(), configurations);
	}

	/**
	 * Instantiates the given {@link List} of {@link PolymorphicConfiguration}s.
	 * 
	 * <p>
	 * A simple {@link InstantiationContext} is used that reports errors be throwing an exception
	 * for the first error.
	 * </p>
	 * 
	 * @return An unmodifiable {@link List} of configured instances.
	 * 
	 * @see TypedConfiguration#getInstanceListReadOnly(InstantiationContext, List)
	 * @see #createInstance(PolymorphicConfiguration)
	 */
	public static <I> List<I> createInstanceListReadOnly(
			List<? extends PolymorphicConfiguration<? extends I>> configurations) {
		return TypedConfiguration.getInstanceListReadOnly(context(), configurations);
	}

	/**
	 * Instantiates the given {@link Map} of {@link PolymorphicConfiguration}s.
	 * 
	 * <p>
	 * A simple {@link InstantiationContext} is used that reports errors be throwing an exception
	 * for the first error.
	 * </p>
	 * 
	 * @see TypedConfiguration#getInstanceMap(InstantiationContext, Map)
	 * @see #createInstance(PolymorphicConfiguration)
	 */
	public static <K, I> Map<K, I> createInstanceMap(
			Map<K, ? extends PolymorphicConfiguration<? extends I>> configurations) {
		return TypedConfiguration.getInstanceMap(context(), configurations);
	}

	/**
	 * Instantiates the given {@link Map} of {@link PolymorphicConfiguration}s.
	 * 
	 * <p>
	 * A simple {@link InstantiationContext} is used that reports errors be throwing an exception
	 * for the first error.
	 * </p>
	 * 
	 * @return An unmodifiable {@link Map} of configured instances.
	 * 
	 * @see TypedConfiguration#getInstanceMapReadOnly(InstantiationContext, Map)
	 * @see #createInstance(PolymorphicConfiguration)
	 */
	public static <K, I> Map<K, I> createInstanceMapReadOnly(
			Map<K, ? extends PolymorphicConfiguration<? extends I>> configurations) {
		return TypedConfiguration.getInstanceMapReadOnly(context(), configurations);
	}

	/**
	 * Set the given property to the given value.
	 * 
	 * @param item
	 *        The {@link ConfigurationItem} in which the property should be set. Is not allowed to
	 *        be null.
	 * @param propertyName
	 *        Is not allowed to be null.
	 * @param value
	 *        The new value of the property. Is allowed to be null, if and only if the property
	 *        allows null as value.
	 * @return The given item, to allow method chaining.
	 */
	public static <C extends ConfigurationItem> C setProperty(C item, String propertyName, Object value) {
		PropertyDescriptor property = item.descriptor().getProperty(propertyName);
		if (property == null) {
			throw new ConfigurationError(I18NConstants.ERROR_NO_SUCH_PROPERTY__PROPERTY__DESCRIPTOR
				.fill(propertyName, item.descriptor()));
		}
		item.update(property, value);
		return item;
	}

	/**
	 * Returns the value stored under the given property name.
	 * 
	 * @param item
	 *        Is not allowed to be null.
	 * @param propertyName
	 *        Is not allowed to be null.
	 * @return The value stored under the given property name.
	 */
	public static Object getProperty(ConfigurationItem item, String propertyName) {
		return item.value(getPropertyDescriptor(item, propertyName));
	}

	/**
	 * Returns true if the value of the property by the given name is set in the given
	 * {@link ConfigurationItem}.
	 * 
	 * @param item
	 *        The {@link ConfigurationItem} in which it is checked if the property is set.
	 * @param propertyName
	 *        The technical name of the property that is checked. Is not allowed to be null.
	 * @return True if the property value is set.
	 */
	public static boolean isValueSet(ConfigurationItem item, String propertyName) {
		return item.valueSet(getPropertyDescriptor(item, propertyName));

	}

	private static PropertyDescriptor getPropertyDescriptor(ConfigurationItem item, String propertyName) {
		PropertyDescriptor property = item.descriptor().getProperty(propertyName);
		if (property == null) {
			throw new ConfigurationError(I18NConstants.ERROR_NO_SUCH_PROPERTY__PROPERTY__DESCRIPTOR
				.fill(propertyName, item.descriptor()));
		}
		return property;
	}

	/**
	 * Creates a new instance of the given class, assuming it has a public config constructor.
	 * 
	 * <p>
	 * This is just a service method calling
	 * {@link TypedConfiguration#newConfiguredInstance(InstantiationContext, Class)}.
	 * </p>
	 * 
	 * @param implClass
	 *        The class to get an instance for.
	 * @return An instance of the given implementation class.
	 * 
	 * @see #createInstance(Class)
	 * @see TypedConfiguration#newConfiguredInstance(InstantiationContext, Class)
	 */
	public static <T> T newConfiguredInstance(Class<T> implClass) {
		try {
			return TypedConfiguration.newConfiguredInstance(context(), implClass);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	/**
	 * Replaces property values in the given {@link ConfigurationItem} according to the given
	 * replacement function.
	 * 
	 * <p>
	 * For the given item and all (recursively) referenced items the replacement function is called.
	 * When the replacement function returns for item "A" a different item "B", then "A" is replaced
	 * by "B".
	 * </p>
	 * 
	 * @param config
	 *        The configuration which must (recursively) be replaced.
	 * @param replacement
	 *        Replacement algorithm. Replaced item must not be <code>null</code>. Input is not
	 *        <code>null</code>.
	 */
	public static ConfigurationItem replace(ConfigurationItem config,
			Function<? super ConfigurationItem, ? extends ConfigurationItem> replacement) {
		return new ReplaceConfigurationVisitor(replacement).replace(config);
	}

}
