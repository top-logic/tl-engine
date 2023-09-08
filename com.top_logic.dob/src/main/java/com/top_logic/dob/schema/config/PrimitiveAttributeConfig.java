/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.schema.config;

import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.xml.DOXMLConstants;

/**
 * Configuration of a primitive attribute in an {@link MetaObject}.
 * 
 * @see ReferenceAttributeConfig
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName(MetaObjectConfig.ATTRIBUTES_PROPERTY)
public interface PrimitiveAttributeConfig extends AttributeConfig, DBConfiguration, DBColumnType {

	/**
	 * The name of the {@link MetaObject} which the attribute stores.
	 * 
	 * @see MOAttribute#getMetaObject()
	 * @see ReferenceAttributeConfig#getValueType()
	 */
	@Mandatory
	@Name(DOXMLConstants.ATT_TYPE_ATTRIBUTE)
	MetaObject getValueType();

	@Override
	@ClassDefault(MOAttributeImpl.class)
	Class<? extends MOAttribute> getImplementationClass();

	/**
	 * The effective name of the DB column.
	 */
	@Override
	@Derived(fun = DBConfiguration.DBNameEffective.class, args = {
		@Ref(DOXMLConstants.ATT_NAME_ATTRIBUTE),
		@Ref(DOXMLConstants.DB_NAME_ATTRIBUTE) })
	String getDBNameEffective();
}

