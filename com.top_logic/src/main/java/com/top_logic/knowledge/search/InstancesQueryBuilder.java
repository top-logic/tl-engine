/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.Set;

import com.top_logic.dob.meta.MOClass;
import com.top_logic.model.TLClass;

/**
 * Builder to create a {@link SetExpression query} for the instances of a table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface InstancesQueryBuilder {

	/**
	 * Creates a {@link SetExpression} which finds all instances of the given types in the given
	 * table.
	 * 
	 * @param table
	 *        The table that stores instances of the given types.
	 * @param types
	 *        The types whose instances are searched. It must be ensured that each type stores it
	 *        instances in the given table.
	 */
	SetExpression createInstancesQuery(MOClass table, Set<TLClass> types);

}

