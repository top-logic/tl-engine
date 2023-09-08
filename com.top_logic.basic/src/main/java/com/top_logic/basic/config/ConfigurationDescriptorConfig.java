/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.annotation.Annotation;
import java.util.Map;

import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * {@link ConfigurationItem} describing a {@link ConfigurationDescriptor}.
 * 
 * @see TypedConfiguration#getConfigurationDescriptor(ConfigurationDescriptorConfig)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ConfigurationDescriptorConfig extends ConfigurationItem {

	/** @see #getExtensions() */
	String EXTENSION_NAME = "extends";

	/**
	 * {@link Annotation}s (indexed by the type of the annotation) for this descriptor.
	 */
	@Key(ConfigurationItem.CONFIGURATION_INTERFACE_NAME)
	Map<Class<? extends Annotation>, Annotation> getAnnotations();

	/**
	 * Whether the {@link ConfigurationDescriptor} is abstract.
	 * 
	 * @see ConfigurationDescriptor#isAbstract()
	 */
	boolean isAbstract();
	
	/**
	 * A resource prefix to get internationalised names for the properties of the configured
	 * {@link ConfigurationDescriptor}.
	 */
	@Nullable
	String getResPrefix();

	/**
	 * The extended (programmatic) {@link ConfigurationItem configurations}.
	 * 
	 * @return May be empty.
	 */
	@Name(EXTENSION_NAME)
	Class<? extends ConfigurationItem>[] getExtensions();

	/**
	 * Definitions of the {@link PropertyDescriptor} for the configured
	 * {@link ConfigurationDescriptor}.
	 */
	@DefaultContainer
	@Key(PropertyDescriptorConfig.NAME_ATTRIBUTE)
	Map<String, PropertyDescriptorConfig> getProperties();

}

