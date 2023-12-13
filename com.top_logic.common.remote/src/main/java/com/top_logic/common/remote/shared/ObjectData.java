/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.common.remote.listener.AbstractAttributeObservable;

/**
 * Data holder/accessor for a member of an {@link ObjectScope}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ObjectData extends AbstractAttributeObservable {

	private final ObjectScope _scope;

	private String _id;

	/**
	 * Creates a {@link ObjectData}.
	 *
	 * @param scope
	 *        See {@link #scope()}.
	 */
	protected ObjectData(ObjectScope scope) {
		_scope = scope;
		_scope.notifyCreate(this);
	}

	/**
	 * The network ID of the {@link #handle()}.
	 * 
	 * @return The ID for the this container, or <code>null</code>, if it is not part of an
	 *         {@link ObjectScope}.
	 */
	public String id() {
		return _id;
	}

	void initId(String id) {
		_id = id;
	}

	/**
	 * The application handle for this data holder.
	 */
	public abstract Object handle();

	/**
	 * The owning {@link ObjectScope}, this {@link ObjectData} is managed in.
	 */
	public final ObjectScope scope() {
		return _scope;
	}

	/**
	 * Accesses the property with the given name.
	 * 
	 * @param property
	 *        The name of the property to access.
	 * @return The value assigned to the given property.
	 */
	public abstract Object getDataRaw(String property);

	/**
	 * Accesses the property with the given name.
	 * 
	 * @param property
	 *        The name of the property to access.
	 * @return The value assigned to the given property.
	 */
	@SuppressWarnings("unchecked")
	public final <T> T getData(String property) {
		Object result = getDataRaw(property);
		if (result instanceof ObjectData) {
			return (T) ((ObjectData) result).handle();
		} else {
			return (T) result;
		}
	}

	/**
	 * Accesses the {@link Integer} property with the given name.
	 * 
	 * @param property
	 *        The name of the property to access.
	 * @return The value assigned to the given property.
	 */
	public final int getDataInt(String property) {
		Number result = getData(property);
		if (result == null) {
			return 0;
		} else {
			return result.intValue();
		}
	}

	/**
	 * Accesses the {@link Double} property with the given name.
	 * 
	 * @param property
	 *        The name of the property to access.
	 * @return The value assigned to the given property.
	 */
	public final double getDataDouble(String property) {
		Number result = getData(property);
		if (result == null) {
			return 0;
		} else {
			return result.doubleValue();
		}
	}

	/**
	 * Accesses the {@link Boolean} property with the given name.
	 * 
	 * @param property
	 *        The name of the property to access.
	 * @return The value assigned to the given property.
	 */
	public final boolean getDataBoolean(String property) {
		return getDataBoolean(property, false);
	}

	/**
	 * Accesses the {@link Boolean} property with the given name.
	 * 
	 * @param property
	 *        The name of the property to access.
	 * @param defaultValue
	 *        The default value to use, if the property has not yet been set.
	 * @return The value assigned to the given property.
	 */
	public final boolean getDataBoolean(String property, boolean defaultValue) {
		Boolean result = getData(property);
		if (result == null) {
			return defaultValue;
		} else {
			return result.booleanValue();
		}
	}

	/**
	 * Updates the given property.
	 * 
	 * @param property
	 *        The name of the property to update.
	 * @param value
	 *        The new value of the property.
	 */
	public final void setData(String property, Object value) {
		ObjectScope scope = scope();
		Object oldValue = setDataRaw(property, scope.toData(value));
		if (!equals(value, oldValue)) {
			scope.notifyUpdate(this, property);
		}
	}

	private static boolean equals(Object v1, Object v2) {
		return v1 == null ? v2 == null : v1.equals(v2);
	}

	/**
	 * Updates the given property with a raw value (only primitives or {@link ObjectData}
	 * supported).
	 * 
	 * @param property
	 *        The name of the property to update.
	 * @param rawValue
	 *        The new value of the property.
	 * @return The old value previously set.
	 */
	public abstract Object setDataRaw(String property, Object rawValue);

	/**
	 * Updates the given {@link Integer} property.
	 * 
	 * @param property
	 *        The name of the property to update.
	 * @param value
	 *        The new value of the property.
	 */
	public final void setDataInt(String property, int value) {
		setData(property, Integer.valueOf(value));
	}

	/**
	 * The values of all properties with explicit assignments in this instance.
	 */
	public abstract Map<String, Object> properties();

	/**
	 * Initializes the properties available for the handle constructor when creating the handle
	 * lazily.
	 * 
	 * @param values
	 *        The properties available for handle creation.
	 */
	public void constructorProperties(Map<String, Object> values) {
		// Ignore.
	}

	/**
	 * Updates this object's properties to the ones given.
	 */
	public void updateProperties(Map<String, Object> values) {
		for (Entry<String, Object> entry : values.entrySet()) {
			setDataRaw(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Service method to convert an appplication value to a raw data object suitable for
	 * {@link #setDataRaw(String, Object)}.
	 */
	protected final Object toData(Object applicationValue) {
		if (applicationValue instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>) applicationValue;
			ArrayList<Object> result = new ArrayList<>(collection.size());
			for (Object element : collection) {
				result.add(toDataSingleton(element));
			}
			return result;
		} else {
			return toDataSingleton(applicationValue);
		}
	}

	private Object toDataSingleton(Object directValue) {
		return scope().toData(directValue);
	}

	/**
	 * Converts a raw value received from {@link #getDataRaw(String)} to an application value.
	 */
	protected final Object fromData(Object rawValue) {
		if (rawValue instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>) rawValue;
			ArrayList<Object> result = new ArrayList<>(collection.size());
			for (Object element : collection) {
				result.add(fromDataSingleton(element));
			}
			return result;
		} else {
			return fromDataSingleton(rawValue);
		}
	}

	private Object fromDataSingleton(Object rawValue) {
		if (rawValue instanceof ObjectData) {
			return ((ObjectData) rawValue).handle();
		} else {
			return rawValue;
		}
	}

	/**
	 * Make visible in this package.
	 */
	@Override
	protected void handleAttributeUpdate(String property) {
		super.handleAttributeUpdate(property);
	}

	/**
	 * Is called by the {@link ObjectScope} when this object is deleted.
	 * <p>
	 * Is called <em>before</em> the object is removed from the {@link ObjectScope}.
	 * </p>
	 */
	protected abstract void onDelete();

	/**
	 * Whether the given property have to be shared.
	 */
	protected boolean isTransient(String property) {
		return false;
	}

}
