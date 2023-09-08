/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.schema.config;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.xml.DOXMLConstants;

/**
 * Configuration item to configure the name of an {@link MetaObject}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface MetaObjectName extends ConfigurationItem {

	/** Pattern name for a valid object name. */
	String OBJECT_NAME_REXEXP_PATTERN = "[a-zA-Z_][a-zA-Z_0-9\\.]*";

	/** Configuration name of property {@link #getObjectName()} */
	String OBJECT_NAME_ATTRIBUTE = DOXMLConstants.MO_NAME_ATTRIB;

	/**
	 * The name of the represented {@link MetaObject}.
	 * 
	 * @see MetaObject#getName()
	 */
	@Name(OBJECT_NAME_ATTRIBUTE)
	@Mandatory
	@RegexpConstraint(OBJECT_NAME_REXEXP_PATTERN)
	String getObjectName();
}

