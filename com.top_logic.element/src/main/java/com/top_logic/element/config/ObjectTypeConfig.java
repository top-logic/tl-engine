/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.element.meta.schema.ElementSchemaConstants;

/**
 * Base configuration for all type definitions that define types of business objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface ObjectTypeConfig extends AttributedTypeConfig {

	/** Property name of {@link #getGeneralizations()}. */
	String GENERALIZATIONS = "generalizations";

	/**
	 * The extended type.
	 */
	@Name(GENERALIZATIONS)
	@EntryTag(ExtendsConfig.TAG_NAME)
	List<ExtendsConfig> getGeneralizations();

	/**
	 * The {@link AttributeConfig}s by name.
	 */
	@Override
	@Subtypes({
		@Subtype(tag = ElementSchemaConstants.PROPERTY_ELEMENT, type = AttributeConfig.class),
		@Subtype(tag = ElementSchemaConstants.REFERENCE_ELEMENT, type = ReferenceConfig.class) })
	Collection<PartConfig> getAttributes();

}
