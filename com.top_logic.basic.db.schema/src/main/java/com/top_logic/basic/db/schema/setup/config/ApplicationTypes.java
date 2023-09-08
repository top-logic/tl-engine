/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.schema.setup.config;

import java.util.Collection;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.db.schema.setup.SchemaSetup;

/**
 * Configuration of type systems.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ApplicationTypes extends ConfigurationItem {

	/**
	 * @see #getTypeSystems()
	 */
	String TYPE_SYSTEMS = "type-systems";

	/**
	 * Returns all known {@link SchemaConfiguration}s indexed by its
	 * {@link SchemaConfiguration#getName() name}.
	 */
	@Name(TYPE_SYSTEMS)
	@Key(SchemaConfiguration.NAME_ATTRIBUTE)
	@ImplementationClassDefault(SchemaSetup.class)
	Collection<SchemaSetup> getTypeSystems();

	/**
	 * Indexed access to {@link #getTypeSystems()}
	 */
	@Indexed(collection = TYPE_SYSTEMS)
	SchemaSetup getTypeSystem(String name);
}
