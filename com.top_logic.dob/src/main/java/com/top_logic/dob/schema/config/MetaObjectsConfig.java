/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.schema.config;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.MOAlternative;
import com.top_logic.dob.MetaObject;

/**
 * Definitions of {@link MetaObject}s and {@link MOAlternative}s.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface MetaObjectsConfig extends ConfigurationItem {
	
	/**
	 * Name of the tag to configure association types.
	 */
	String ASSOCIATION_TAG_NAME = "association";

	/** Name of the tag to configure {@link MOAlternative}s. */
	String ALTERNATIVE_TAG_NAME = "alternative";

	/** Name of the {@link #getTypes()} attribute. */
	String METAOBJECTS = "metaobjects";

	/**
	 * Mapping of the name of an {@link MetaObject} or {@link MOAlternative} to its definition.
	 */
	@Key(MetaObjectName.OBJECT_NAME_ATTRIBUTE)
	@Name(METAOBJECTS)
	@DefaultContainer
	Map<String, MetaObjectName> getTypes();
}

