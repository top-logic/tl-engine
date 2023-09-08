/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.provider.DefaultProvider;

/**
 * Annotation defining the default value of an attribute.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("default-value")
@InApp
@AnnotationInheritance(Policy.REDEFINE)
public interface TLDefaultValue extends TLAttributeAnnotation, TLTypeAnnotation {
	
	/** Configuration name for the value of {@link #getProvider()}. */
	String PROVIDER_NAME = "provider";

	/**
	 * Provider for the default value of the annotated attribute.
	 */
	@Mandatory
	@Name(PROVIDER_NAME)
	@DefaultContainer
	PolymorphicConfiguration<DefaultProvider> getProvider();

	/**
	 * @see #getProvider()
	 */
	void setProvider(PolymorphicConfiguration<DefaultProvider> value);

}

