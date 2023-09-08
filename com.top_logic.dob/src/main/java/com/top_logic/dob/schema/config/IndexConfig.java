/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.schema.config;

import java.util.List;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.dob.meta.MOIndex;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBIndex;
import com.top_logic.dob.xml.DOXMLConstants;

/**
 * Configuration of an {@link MOIndex} in an {@link MOStructure}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface IndexConfig extends DBConfiguration, NamedConfigMandatory {

	/**
	 * Configuration of the name of the represented {@link MOIndex}.
	 * 
	 * @see MOIndex#getName()
	 */
	@Override
	String getName();

	/**
	 * The effective name to use for the index in the DB.
	 */
	@Override
	@Derived(fun = DBConfiguration.DBNameEffective.class, args = {
		@Ref(NAME_ATTRIBUTE), @Ref(DOXMLConstants.DB_NAME_ATTRIBUTE)
	})
	String getDBNameEffective();

	/**
	 * Whether the represented {@link MOIndex} is unique.
	 * 
	 * @see MOIndex#isUnique()
	 */
	@Name(DOXMLConstants.UNIQUE_ATTRIBUTE)
	@BooleanDefault(true)
	boolean isUnique();

	/**
	 * Whether technical columns should not automatically be added to the index.
	 */
	@Name(DOXMLConstants.CUSTOM_ATTRIBUTE)
	boolean isCustom();

	/**
	 * Whether the represented {@link MOIndex} is an "in memory" index.
	 * 
	 * @see DBIndex#isInMemory()
	 */
	@Name(DOXMLConstants.IN_MEMORY_ATTRIBUTE)
	@BooleanDefault(false)
	boolean isInMemory();

	/**
	 * Definition of content of the index.
	 */
	@EntryTag(DOXMLConstants.INDEX_PART_ELEMENT)
	@Key(IndexPartConfig.NAME_ATTRIBUTE)
	List<IndexPartConfig> getIndexParts();

}

