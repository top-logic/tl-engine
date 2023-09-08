/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * {@link Accessor} implementation that dispatches to a set of {@link PropertyAccessor}s.
 * 
 * Consider also regular {@link Accessor}. They can be used in table definitions to be set as
 * accessor for certain columns. The dispatch is then made by the framework.
 * 
 * <p>
 * Note: Instances of this class are immutable.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DispatchingAccessor<T> implements Accessor<T> {

	private final Map<String, PropertyAccessor<? super T>> propertyAccessors;
	
	private final Accessor<? super T> defaultAccessor;

	/**
	 * Creates a {@link DispatchingAccessor}.
	 * 
	 * @param propertyAccessors
	 *        Map of property names to corresponding {@link PropertyAccessor}.
	 */
	public DispatchingAccessor(Map<String, ? extends PropertyAccessor<? super T>> propertyAccessors) {
		this(propertyAccessors, null);
	}

	/**
	 * Creates a {@link DispatchingAccessor}.
	 * 
	 * @param propertyAccessors
	 *        Map of property names to corresponding {@link PropertyAccessor}.
	 * @param defaultAccessor
	 *        See {@link #getDefaultAccessor()}.
	 */
	public DispatchingAccessor(Map<String, ? extends PropertyAccessor<? super T>> propertyAccessors, Accessor<? super T> defaultAccessor) {
		if (defaultAccessor instanceof DispatchingAccessor<?>) {
			DispatchingAccessor<? super T> dispatchingDefault = asDispatchingAccessor(defaultAccessor);

			// Use the inherited property accessors as base set. 
			this.propertyAccessors = new HashMap<>(dispatchingDefault.getPropertyAccessors());
			this.propertyAccessors.putAll(propertyAccessors);
			this.defaultAccessor = dispatchingDefault.getDefaultAccessor();
		} else {
			// Make sure, this class is immutable.
			this.propertyAccessors = new HashMap<>(propertyAccessors);
			this.defaultAccessor = defaultAccessor;
		}
	}

	/**
	 * Cast the given {@link Accessor} to {@link DispatchingAccessor} by keeping
	 * its generic type.
	 * 
	 * @param <T>
	 *        The target type of the given accessor.
	 * @param accessor
	 *        The accessor to cast.
	 * @return The given accessor cast to {@link DispatchingAccessor}.
	 */
	@SuppressWarnings("unchecked")
	private static <T> DispatchingAccessor<T> asDispatchingAccessor(Accessor<T> accessor) {
		DispatchingAccessor<T> dispatchingDefault = (DispatchingAccessor) accessor;
		return dispatchingDefault;
	}

	@Override
	public Object getValue(T object, String property) {
		PropertyAccessor<? super T> propertyAccessor = getPropertyAccessor(property);
		if (propertyAccessor != null) {
			return propertyAccessor.getValue(object);
		} else if (defaultAccessor != null) {
			return defaultAccessor.getValue(object, property);
		} else {
			throw noSuchProperty(property);
		}
	}

	@Override
	public void setValue(T object, String property, Object value) {
		PropertyAccessor<? super T> propertyAccessor = getPropertyAccessor(property);
		if (propertyAccessor != null) {
			propertyAccessor.setValue(object, value);
		} else if (defaultAccessor != null) {
			defaultAccessor.setValue(object, property, value);
		} else {
			throw noSuchProperty(property);
		}
	}
	
	private NoSuchElementException noSuchProperty(String property) {
		return new NoSuchElementException("No such property '" + property + "'.");
	}

	/**
	 * Returns the map of {@link PropertyAccessor}s this
	 * {@link DispatchingAccessor} relies on. The resulting map is indexed by
	 * the property names.
	 * 
	 * @return A map of (propety name, property accessor) assignments of this
	 *         accessor.
	 */
	public Map<String, PropertyAccessor<? super T>> getPropertyAccessors() {
		// Make sure, this class is immutable.
		return Collections.unmodifiableMap(propertyAccessors);
	}
	
	/**
	 * The {@link Accessor} that is used, if the dispatch does not find a {@link PropertyAccessor}.
	 */
	public Accessor<? super T> getDefaultAccessor() {
		return defaultAccessor;
	}

	private PropertyAccessor<? super T> getPropertyAccessor(String property) {
		return propertyAccessors.get(property);
	}
	
}
