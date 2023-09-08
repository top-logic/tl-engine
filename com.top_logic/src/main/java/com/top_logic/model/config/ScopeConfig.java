/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config;

import java.util.Collection;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;

/**
 * Container for type definitions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ScopeConfig extends ConfigurationItem {

	/** Property name of {@link #getTypes()}. */
	String TYPES = "types";

	/**
	 * Locally defined types.
	 */
	@Name(TYPES)
	@Key(TypeConfig.NAME)
	Collection<TypeConfig> getTypes();

	/**
	 * Indexed getter for the {@link #getTypes()} property.
	 * 
	 * @param name
	 *        The name of the type to find.
	 * @return The {@link TypeConfig} with the given name, or <code>null</code> if no such type
	 *         exists.
	 */
	@Indexed(collection = TYPES)
	TypeConfig getType(String name);

	/** @see #getTypes() */
	void setTypes(Collection<TypeConfig> value);

}
