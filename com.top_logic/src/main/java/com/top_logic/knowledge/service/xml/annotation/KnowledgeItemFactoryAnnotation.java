/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.xml.annotation;

import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.meta.MOAnnotation;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.service.db2.KnowledgeItemFactory;

/**
 * {@link MOAnnotation} linking a {@link KnowledgeItemFactory} to a {@link MOClass}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface KnowledgeItemFactoryAnnotation extends MOAnnotation {

	/** Name of the property {@link #getImplementationFactory()} */
	String IMPLEMENTATION_FACTORY_PROPERTY = "implementation-factory";

	/**
	 * Returns the factory that creates the {@link KnowledgeItemFactory} to create the types.
	 */
	@Name(IMPLEMENTATION_FACTORY_PROPERTY)
	@InstanceFormat
	@Mandatory
	KnowledgeItemFactory getImplementationFactory();

	/**
	 * @see #getImplementationFactory()
	 */
	void setImplementationFactory(KnowledgeItemFactory factory);

}