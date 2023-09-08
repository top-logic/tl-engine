/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.schema;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Configuration specifying an attribute value of an {@link ObjectConf object import declaration}.
 * 
 * @see ObjectConf#getAttributes()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface AttributeValueConf extends ConfigurationItem {

	/**
	 * Name of the attribute to import.
	 */
	@Mandatory
	String getName();

	/**
	 * @see #getName()
	 */
	void setName(String value);

	/**
	 * Textual value of a primitive attribute.
	 * 
	 * <p>
	 * For a reference attribute, {@link #getReferences()} must be set instead.
	 * </p>
	 */
	@Nullable
	String getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(String value);

	/**
	 * References to other objects to set in the {@link #getName()} attribute.
	 * 
	 * <p>
	 * For a primitive attribute, {@link #getValue()} must be set instead.
	 * </p>
	 */
	@DefaultContainer
	List<RefConf> getReferences();

}
