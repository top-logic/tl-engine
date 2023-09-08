/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.schema;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.instance.importer.XMLInstanceImporter;

/**
 * {@link RefConf Reference} to another object in the same import scope.
 * 
 * @see AttributeValueConf#getReferences()
 * @see XMLInstanceImporter#getObject(String)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("ref")
public interface InstanceRefConf extends RefConf {

	/**
	 * {@link ObjectConf#getId() ID} of another object in the same import that is referenced.
	 * 
	 * @see ObjectConf#getId()
	 */
	@Mandatory
	String getId();

	/**
	 * @see #getId()
	 */
	void setId(String value);

}
