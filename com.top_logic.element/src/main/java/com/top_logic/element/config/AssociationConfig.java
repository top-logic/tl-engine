/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.schema.ElementSchemaConstants;
import com.top_logic.model.TLAssociation;

/**
 * Definition of an association.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName(AssociationConfig.TAG_NAME)
public interface AssociationConfig extends AttributedTypeConfig {

	/** Default tag for defining associations. */
	String TAG_NAME = "association";

	/**
	 * Configuration of the names of the subsets of the configured {@link TLAssociation}.
	 * 
	 * @see TLAssociation#getSubsets()
	 */
	List<ExtendsConfig> getSubsets();

	/**
	 * The {@link AttributeConfig}s by name.
	 */
	@Override
	@Subtypes({
		@Subtype(tag = ElementSchemaConstants.PROPERTY_ELEMENT, type = AttributeConfig.class),
		@Subtype(tag = ElementSchemaConstants.ASSOCIATION_END_ELEMENT, type = EndConfig.class) })
	Collection<PartConfig> getAttributes();

	/**
	 * Definition of an association end.
	 */
	@TagName(ElementSchemaConstants.ASSOCIATION_END_ELEMENT)
	interface EndConfig extends EndAspect, TypedPartAspect {
		// Pure marker interface.
	}
}
