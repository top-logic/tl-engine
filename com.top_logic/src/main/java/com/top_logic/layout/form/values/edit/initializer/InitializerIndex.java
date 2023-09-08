/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.initializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * Dynamic factory for combining {@link Initializer}s that are specified for certain interfaces
 * allowing wildcards.
 * 
 * <p>
 * <b>Note:</b> This implementation must only be used single-threaded. This holds for the
 * construction <b>and</b> usage phase.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InitializerIndex extends LazyTypedAnnotatable implements InitializerProvider {

	private Map<Class<?>, Map<String, Initializer>> _definitions = new HashMap<>();

	private Map<Class<?>, Map<String, Initializer>> _initializers = new HashMap<>();

	private Set<Initializer> _search = new LinkedHashSet<>();

	/**
	 * Adds the given {@link Initializer} to the list of initializers that are called, whenever a
	 * model is modified that implements the given interface type by setting the given property.
	 * 
	 * @param type
	 *        The type to add the {@link Initializer} for, or <code>null</code> for all types.
	 * @param property
	 *        The name of the property for which the given {@link Initializer} should be called or
	 *        <code>null</code> for all properties.
	 * @param initializer
	 *        The {@link Initializer} algorithm to use.
	 */
	public void add(Class<? extends ConfigurationItem> type, String property, Initializer initializer) {
		index(_definitions, type).put(property, initializer);
	}

	@Override
	public Initializer getInitializer(ConfigurationDescriptor descriptor, PropertyDescriptor property) {
		Class<?> type = descriptor.getConfigurationInterface();
		Map<String, Initializer> index = index(_initializers, type);
		String propertyName = property.getPropertyName();
		Initializer result = index.get(propertyName);
		if (result == null) {
			addInitializer(null, null);
			collectInitializers(type, propertyName);
			if (_search.isEmpty()) {
				result = EmptyInitializer.INSTANCE;
			} else if (_search.size() == 1) {
				result = _search.iterator().next();
			} else {
				result = new CompositeInitializer(_search);
			}
			_search.clear();
			index.put(propertyName, result);
		}
		return result;
	}

	private void collectInitializers(Class<?> type, String propertyName) {
		if (type == null) {
			return;
		}

		for (Class<?> superInterfaces : type.getInterfaces()) {
			collectInitializers(superInterfaces, propertyName);
		}

		collectInitializers(type.getSuperclass(), propertyName);

		addInitializer(type, null);
		addInitializer(type, propertyName);
	}

	private void addInitializer(Class<?> type, String propertyName) {
		Initializer result = initializersForType(type).get(propertyName);
		if (result != null && !_search.contains(result)) {
			_search.add(result);
		}
	}

	private Map<String, Initializer> initializersForType(Class<?> type) {
		Map<String, Initializer> result = _definitions.get(type);
		if (result == null) {
			return Collections.emptyMap();
		}
		return result;
	}

	private static <K1, K2, V> Map<K2, V> index(Map<K1, Map<K2, V>> base, K1 type) {
		Map<K2, V> mapping = base.get(type);
		if (mapping == null) {
			mapping = new HashMap<>();
			base.put(type, mapping);
		}
		return mapping;
	}

}
