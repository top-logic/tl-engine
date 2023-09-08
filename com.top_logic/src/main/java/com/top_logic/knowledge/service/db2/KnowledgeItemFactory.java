/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.identifier.MutableObjectKey;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Factory for creating {@link KnowledgeItem} implementation for {@link MOClass} types of the
 * {@link KnowledgeBase}.
 * 
 * @author <a href=mailto:bhu@top-logic.com>bhu</a>
 */
@FrameworkInternal
public interface KnowledgeItemFactory {

	/**
	 * Create an implementation for the given type.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} in which holds the new item.
	 * @param staticType
	 *        The type to instantiate.
	 * @return The newly created object.
	 */
	AbstractDBKnowledgeItem newKnowledgeItem(DBKnowledgeBase kb, MOKnowledgeItem staticType);

	/**
	 * Create an (immutable historic) implementation for the given type.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} in which holds the new item.
	 * @param staticType
	 *        The type to instantiate.
	 * @return The newly created object.
	 */
	AbstractDBKnowledgeItem newImmutableItem(DBKnowledgeBase kb, MOKnowledgeItem staticType);

	/**
	 * Creates an identifier object for the given type in the given historic context.
	 * 
	 * @param historyContext
	 *        The revision number that identifies the historic context.
	 * @param staticType
	 *        The type of the item to create identifier for.
	 * 
	 * @return The identifier for the given item in the given historic context.
	 */
	DBObjectKey createIdentifier(DBHelper sqlDialect, ResultSet dbResult, int resultOffset,
			long historyContext, MOKnowledgeItem staticType) throws SQLException;

	/**
	 * Updates the given {@link MutableObjectKey identifier} with the informations from the given
	 * database result.
	 * 
	 * @param historyContext
	 *        The revision number that identifies the historic context.
	 * @param staticType
	 *        The type of the item to create identifier for.
	 * @param key
	 *        The key to update.
	 */
	void loadIdentifier(DBHelper sqlDialect, ResultSet dbResult, int resultOffset,
			long historyContext, MOKnowledgeItem staticType, MutableObjectKey<?> key) throws SQLException;
}
