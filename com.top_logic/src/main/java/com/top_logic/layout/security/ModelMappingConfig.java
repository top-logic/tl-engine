/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.TLObject;

/**
 * Mapping of some object, e.g. the row object in a table, to an object on which security can be
 * computed. Security for the input object is computed on the output object.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ModelMappingConfig extends ConfigurationItem {

	/** Name of {@link #getModelMapping()} property. */
	String MODEL_MAPPING_ATTRIBUTE = "model-mapping";

	/**
	 * Mapping from row object to represented business objects.
	 * 
	 * <p>
	 * If there is no {@link TLObject} that is represented by the row, the mapping must return
	 * <code>null</code>.
	 * </p>
	 */
	@Name(MODEL_MAPPING_ATTRIBUTE)
	PolymorphicConfiguration<? extends Mapping<Object, ? extends TLObject>> getModelMapping();

	/**
	 * @see #getModelMapping()
	 */
	void setModelMapping(PolymorphicConfiguration<? extends Mapping<Object, ? extends TLObject>> mapping);

}
