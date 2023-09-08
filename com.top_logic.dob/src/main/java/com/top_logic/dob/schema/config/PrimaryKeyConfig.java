/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.schema.config;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.dob.xml.DOXMLConstants;

/**
 * Configuration of the primary key.
 * 
 * @see MetaObjectConfig#getPrimaryKey()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PrimaryKeyConfig extends ConfigurationItem {
	
	/**
	 * Definition of content of the key.
	 */
	@EntryTag(DOXMLConstants.INDEX_PART_ELEMENT)
	@Key(IndexPartConfig.NAME_ATTRIBUTE)
	List<IndexPartConfig> getIndexParts();


}

