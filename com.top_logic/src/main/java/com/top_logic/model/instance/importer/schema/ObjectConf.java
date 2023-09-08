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
import com.top_logic.model.TLClass;
import com.top_logic.model.instance.importer.XMLInstanceImporter;

/**
 * Import description of a single new object to create.
 * 
 * @see XMLInstanceImporter#importInstances(List)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ObjectConf extends ConfigurationItem {

	/**
	 * Optional import-local identifier that is used to reference this object from references of
	 * other objects that are imported.
	 * 
	 * @see InstanceRefConf#getId()
	 */
	@Nullable
	String getId();

	/**
	 * @see #getId()
	 */
	void setId(String value);

	/**
	 * The type name (qualified name of the {@link TLClass}) the imported object should be an
	 * instance of.
	 */
	@Mandatory
	String getType();

	/**
	 * @see #getType()
	 */
	void setType(String value);

	/**
	 * Attributes to set on the imported object.
	 */
	@DefaultContainer
	List<AttributeValueConf> getAttributes();

}
