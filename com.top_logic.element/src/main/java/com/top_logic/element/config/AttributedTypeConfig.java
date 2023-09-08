/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import java.util.Collection;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.element.meta.schema.ElementSchemaConstants;
import com.top_logic.model.TLType;
import com.top_logic.model.config.TypeConfig;

/**
 * {@link TypeConfig} interface for {@link TLType} which have some kind of attributes.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface AttributedTypeConfig extends TypeConfig {

	/** Property name of {@link #getAttributes()}. */
	String ATTRIBUTES = "attributes";

	/**
	 * The {@link PartConfig}s by name.
	 */
	@Name(ATTRIBUTES)
	@Key(PartConfig.NAME)
	@Subtypes({
		@Subtype(tag = ElementSchemaConstants.PROPERTY_ELEMENT, type = AttributeConfig.class)
	})
	Collection<PartConfig> getAttributes();

	/**
	 * Indexed getter for {@link #getAttributes()} property.
	 */
	@Indexed(collection = ATTRIBUTES)
	PartConfig getAttribute(String name);

}
