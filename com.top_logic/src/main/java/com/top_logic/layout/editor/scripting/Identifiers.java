/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.scripting;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Collection of stable identifiers using when creating, modifying, or deleting
 * {@link LayoutComponent} within the application.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Identifiers extends ConfigurationItem {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	/** Configuration name for {@link #getComponentKeys()}. */
	String COMPONENT_KEYS = "component-keys";

	/** Configuration name for {@link #getUUIDs()}. */
	String UUIDS = "uuids";

	/**
	 * Identifiers for inner {@link LayoutComponent}'s.
	 * 
	 * To script the dynamic creation of views, stable component identifiers are needed. Normally
	 * random {@link UUID}'s are used while creating new views.
	 */
	@ListBinding
	@Name(COMPONENT_KEYS)
	List<String> getComponentKeys();

	/**
	 * @see #getComponentKeys()
	 */
	void setComponentKeys(List<String> value);

	/**
	 * Identifiers for configuration properties which are initialised with {@link UUID}s.
	 */
	@ListBinding
	@Name(UUIDS)
	@Label("UUIDs")
	List<String> getUUIDs();

	/**
	 * Setter for {@link #getUUIDs()}.
	 */
	void setUUIDs(List<String> value);

	/**
	 * Adds the given key to the list of {@link #getComponentKeys()}.
	 */
	default void addComponentKey(String key) {
		ArrayList<String> newKeys = new ArrayList<>(getComponentKeys());
		newKeys.add(key);
		setComponentKeys(newKeys);
	}

	/**
	 * Appends the data of the given {@link Identifiers} to this objects.
	 */
	default void append(Identifiers other) {
		List<String> otherKeys = other.getComponentKeys();
		if (!otherKeys.isEmpty()) {
			ArrayList<String> newKeys = new ArrayList<>(getComponentKeys());
			newKeys.addAll(otherKeys);
			setComponentKeys(newKeys);
		}
		List<String> otherUUIDs = other.getUUIDs();
		if (!otherUUIDs.isEmpty()) {
			ArrayList<String> newUUIDs = new ArrayList<>(getUUIDs());
			newUUIDs.addAll(otherUUIDs);
			setUUIDs(otherUUIDs);
		}

	}

	/**
	 * Prepends the given keys to {@link #getComponentKeys()}.
	 */
	default void prependComponentKeys(Collection<String> keys) {
		if (keys.isEmpty()) {
			return;
		}
		ArrayList<String> newKeys = new ArrayList<>(keys);
		newKeys.addAll(getComponentKeys());
		setComponentKeys(newKeys);
	}

}

