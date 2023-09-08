/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import java.util.Collection;

import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.meta.schema.ElementSchemaConstants;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.model.config.ModelPartConfig;

/**
 * Representation of a
 * {@link ElementSchemaConstants#ROOT_ELEMENT structured element definition file}
 * (configuration for the {@link StructuredElementFactory}).
 * 
 * TODO KBU Move Icon configuration to Theme ?
 * 
 * @author <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public interface ModelConfig extends ModelPartConfig {
    
	/** Property name of {@link #getModules()}. */
	public static final String MODULES = "modules";

	/** Entry tag of {@link #getModules()}. */
	public static final String MODULE = "module";

	/**
	 * Defined {@link ModuleConfig}s in this {@link ModelConfig}.
	 */
	@Name(MODULES)
	@Key(ModuleConfig.NAME)
	@EntryTag(ModelConfig.MODULE)
	@DefaultContainer
	Collection<ModuleConfig> getModules();

	/**
	 * Access to a {@link #getModules() module} with a certain name.
	 */
	@Indexed(collection = MODULES)
	ModuleConfig getModule(String name);

	/** @see #getModules() */
	void setModules(Collection<ModuleConfig> value);

}
