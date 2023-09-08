/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.xml.annotation;

import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.dob.meta.MOAnnotation;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.search.InstancesQueryBuilder;
import com.top_logic.knowledge.service.db2.MOKnowledgeItem;

/**
 * Annotation for an {@link InstancesQueryBuilder} on {@link MOClass}.
 * 
 * @see InstancesQueryBuilder
 * @see MOKnowledgeItem#getInstancesQueryBuilder()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface InstancesQueryAnnotation extends MOAnnotation {

	/**
	 * The {@link InstancesQueryBuilder} to use for the annotated table.
	 */
	@InstanceFormat
	@Mandatory
	InstancesQueryBuilder getBuilder();

}

