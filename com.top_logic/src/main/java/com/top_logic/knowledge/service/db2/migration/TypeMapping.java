/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Provider that resolves a {@link MetaObject} for a type name.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TypeMapping {

	/**
	 * Returns the {@link MetaObject} for the given type name.
	 * 
	 * <p>
	 * Note: The {@link MetaObject} itself may not have the given type name.
	 * </p>
	 * 
	 * @param typeName
	 *        Name of the type to return.
	 * 
	 * @return May be <code>null</code> in case there is no type for the given name.
	 */
	MetaObject getType(String typeName);

	/**
	 * Returns the {@link MetaObject} that describes the table with the given type name.
	 * 
	 * <p>
	 * Note: The {@link MetaObject} itself may not have the given table name nor must it have the
	 * given table name as its DB name.
	 * </p>
	 * 
	 * @param tableName
	 *        Name of the table to search a description for.
	 * 
	 * @return May be <code>null</code> in case there is no type for the given name.
	 */
	MOStructure getTableType(String tableName);

	/**
	 * Initialises the target type repository of this {@link TypeMapping}.
	 * 
	 * @param repository
	 *        The {@link MORepository} of the {@link KnowledgeBase} to migrate to.
	 */
	void initTypeRepository(MORepository repository);

}
