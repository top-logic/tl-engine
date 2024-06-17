/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.schema.config;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.MODefaultProvider;
import com.top_logic.dob.xml.DOXMLConstants;

/**
 * Configuration of an {@link MOAttribute}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface AttributeConfig extends PolymorphicConfiguration<MOAttribute> {

	/**
	 * @see #isOverride()
	 */
	String OVERRIDE = "override";

	/** The name of the property represented by {@link #getAttributeName()}. */
	String ATTRIBUTE_NAME_KEY = DOXMLConstants.ATT_NAME_ATTRIBUTE;

	/** The name of the property represented by {@link #getDefaultProvider()}. */
	String DEFAULT_PROVIDER_NAME = "default-provider";

	/**
	 * Returns the name of the {@link MOAttribute}.
	 * 
	 * @see MOAttribute#getName()
	 */
	@Name(ATTRIBUTE_NAME_KEY)
	@Mandatory
	String getAttributeName();

	/**
	 * @see MOAttribute#isMandatory()
	 */
	@Name(DOXMLConstants.MANDATORY_ATTRIBUTE)
	@BooleanDefault(false)
	boolean isMandatory();

	/**
	 * @see MOAttribute#isImmutable()
	 */
	@Name(DOXMLConstants.IMMUTABLE_ATTRIBUTE)
	@BooleanDefault(false)
	boolean isImmutable();

	/**
	 * Whether the value of this {@link MOAttribute} is necessary for construction of an object.
	 * 
	 * @see MOAttribute#isInitial()
	 */
	@Name(DOXMLConstants.INITIAL_ATTRIBUTE)
	@BooleanDefault(false)
	boolean isInitialAttribute();

	/**
	 * Whether this attributes overrides an attribute with the same name of a super class.
	 * 
	 * <p>
	 * Note: This is only allowed by {@link AttributeConfig} whose {@link MetaObjectConfig} is an
	 * {@link AssociationConfig}.
	 * </p>
	 */
	@Name(OVERRIDE)
	@BooleanDefault(false)
	boolean isOverride();

	/**
	 * The {@link AttributeStorage storage strategy} of the configured {@link MOAttribute}.
	 * 
	 * @see MOAttribute#getStorage()
	 */
	@InstanceFormat
	AttributeStorage getStorage();

	/**
	 * @see #getStorage()
	 */
	void setStorage(AttributeStorage value);

	/** @see MOAttribute#isHidden() */
	@Name(DOXMLConstants.HIDDEN_ATTRIBUTE)
	boolean isHidden();

	/**
	 * Whether this attribute is an internal attribute that should not be exposed to the
	 * application.
	 * 
	 * @see MOAttribute#isSystem()
	 */
	boolean isSystem();

	/**
	 * The provider creating the default value for the configured {@link MOAttribute}.
	 */
	@Name(DEFAULT_PROVIDER_NAME)
	@InstanceFormat
	MODefaultProvider getDefaultProvider();

	/**
	 * Visit method for {@link AttributeConfig} hierarchy.
	 * 
	 * @param visitor
	 *        The visitor visiting this {@link AttributeConfig}.
	 */
	<R, A> R visit(AttributeConfigVisitor<R, A> visitor, A arg);

}

