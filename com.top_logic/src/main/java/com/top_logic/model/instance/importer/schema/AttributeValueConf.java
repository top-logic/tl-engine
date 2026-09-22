/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.schema;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.model.access.StorageMapping;

/**
 * Configuration specifying an attribute value of an {@link ObjectConf object import declaration}.
 * 
 * @see ObjectConf#getAttributes()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface AttributeValueConf extends ConfigurationItem {

	/**
	 * @see #getValue()
	 */
	String VALUE = "value";

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
	 * Textual representation of a primitive attribute's value.
	 * 
	 * <p>
	 * If the application type of the attribute type's {@link StorageMapping} declares a
	 * {@link Format}, the value is given in that format. Otherwise, the value is a serialized
	 * variant of the value stored in the database.
	 * </p>
	 * 
	 * <p>
	 * For a reference attribute, {@link #getCollectionValue()} must be set instead.
	 * </p>
	 * 
	 * @implNote Without a {@link Format} annotation at the
	 *           {@link StorageMapping#getApplicationType() application type}, the value is
	 *           converted by {@link StorageMapping#getBusinessObject(Object)}.
	 */
	@Nullable
	@Name(VALUE)
	String getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(String value);

	/**
	 * Values to set to the attribute {@link #getName()}.
	 * 
	 * <p>
	 * For single primitive values, {@link #getValue()} may be used as short-cut.
	 * </p>
	 */
	@DefaultContainer
	List<ValueConf> getCollectionValue();

}
