/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.List;

import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.service.xml.KnowledgeBaseImporter;

/**
 * Configuration of the {@link MetaObject}s in the system.
 * 
 * @see TypeSystemSetup
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TypeSystemConfiguration extends SchemaConfiguration {

	/** Attribute name of the {@link #getInitialData()} property. */
	String DATA_ATTRIBUTE = "data";

	/**
	 * List of resources containing the initial data of the configured types.
	 * 
	 * @see KnowledgeBaseImporter#importObjects(KnowledgeBase, String, boolean,
	 *      com.top_logic.basic.Protocol)
	 */
	@EntryTag("definition")
	@Name(DATA_ATTRIBUTE)
	@Key(ResourceDeclaration.RESOURCE_ATTRIBUTE)
	List<ResourceDeclaration> getInitialData();

}

