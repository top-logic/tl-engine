/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

import java.util.Collection;

/**
 * {@link SharedObject} base class for application type implementations adding functionality.
 * 
 * <p>
 * The base class acts directly inherits {@link ObjectData} providing a container with access to
 * managed instance data.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DefaultSharedObject extends SharedObjectData implements SharedObject {

	/**
	 * Creates a {@link DefaultSharedObject}.
	 *
	 * @param scope
	 *        See {@link #scope()}.
	 */
	public DefaultSharedObject(ObjectScope scope) {
		super(scope);
	}

	@Override
	public Object handle() {
		return this;
	}

	@Override
	public final SharedObjectData data() {
		return this;
	}

	/**
	 * Access to the managed property with the given name.
	 * 
	 * @param property
	 *        Name of the property to access.
	 * @return The value of the given property.
	 * 
	 * @see ObjectData#getData(String)
	 */
	public final <T> T get(String property) {
		return getData(property);
	}

	/**
	 * Updates the managed property with the given name.
	 * 
	 * @param property
	 *        Name of the property to update.
	 * @param value
	 *        The new value of the property.
	 * 
	 * @see ObjectData#setData(String, Object)
	 */
	public final void set(String property, Object value) {
		setData(property, value);
	}

	/**
	 * The {@link DefaultSharedObject}s pointing to this instance through the given reference property.
	 * 
	 * @param expectedType
	 *        The expected type of the referrers.
	 * @param property
	 *        Name of the property.
	 * @return All instances that have this instance assigned to the given property (as single
	 *         instance, not within lists or other complex data structures).
	 * 
	 * @see SharedObjectData#getReferrers(String)
	 */
	public final <T extends DefaultSharedObject> Collection<? extends T> getReferrers(Class<T> expectedType, String property) {
		return new HandleCollectionWrapper<>(data().getReferrers(property));
	}

}
