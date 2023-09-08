/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.dob.meta.MOAnnotation;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.knowledge.search.InstancesQueryBuilder;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.xml.annotation.FullLoadAnnotation;
import com.top_logic.knowledge.service.xml.annotation.InstancesQueryAnnotation;
import com.top_logic.knowledge.service.xml.annotation.KnowledgeItemFactoryAnnotation;
import com.top_logic.knowledge.service.xml.annotation.SystemAnnotation;

/**
 * {@link KnowledgeBase}-specific implementation of {@link MOClass}.
 * 
 * @author <a href=mailto:bhu@top-logic.com>bhu</a>
 */
public interface MOKnowledgeItem extends DBTableMetaObject, MOClass {

	/**
	 * Cached value of the {@link SystemAnnotation}.
	 * 
	 * @see #addAnnotation(MOAnnotation)
	 * @see MOKnowledgeItemUtil#setSystem(MOClass, boolean)
	 */
	boolean isSystem();
	
	/**
	 * Cached value of the {@link FullLoadAnnotation}.
	 * 
	 * @see #addAnnotation(MOAnnotation)
	 * @see MOKnowledgeItemUtil#setFullLoad(MOClass, boolean)
	 */
	boolean getFullLoad();

	/**
	 * Cached value of the {@link KnowledgeItemFactoryAnnotation}.
	 * 
	 * @see #addAnnotation(MOAnnotation)
	 * @see MOKnowledgeItemUtil#setImplementationFactory(MOClass, KnowledgeItemFactory)
	 */
	KnowledgeItemFactory getImplementationFactory();
	
	/**
	 * The {@link InstancesQueryBuilder} to create search expressions for instances of given types
	 * stored in this tables.
	 * 
	 * <p>
	 * The value is the cached value of a {@link InstancesQueryAnnotation} annotation, or inherited
	 * from a super class.
	 * </p>
	 */
	InstancesQueryBuilder getInstancesQueryBuilder();

}
