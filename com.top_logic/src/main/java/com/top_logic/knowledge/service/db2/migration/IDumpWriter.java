/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import java.io.IOException;
import java.util.Iterator;

import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;

/**
 * Writer for knowledge base dumps.
 * 
 * <p>
 * The writer must be operated in the following way:
 * </p>
 * 
 * <ol>
 * <li>{@link #beginChangeSets()} must be called.</li>
 * <li>{@link #writeChangeSet(ChangeSet)} must be called for each {@link ChangeSet} to dump.</li>
 * <li>{@link #endChangeSets()} must be called.</li>
 * <li>{@link #beginTypes()} must be called.</li>
 * <li>{@link #writeUnversionedType(MetaObject, Iterator)} must be called for each unversioned type
 * to dump.</li>
 * <li>{@link #endTypes()} must be called.</li>
 * <li>{@link #beginTables()} must be called.</li>
 * <li>{@link #writeTable(String, Iterable)} must be called for each table to dump.</li>
 * <li>{@link #endTables()} must be called.</li>
 * </ol>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface IDumpWriter {

	/**
	 * Notifies this {@link IDumpWriter} about the begin of change sets.
	 * 
	 * @see #writeChangeSet(ChangeSet)
	 * 
	 * @throws IOException
	 *         When writing causes errors. Is actually not thrown but declared for subclasses.
	 */
	default void beginChangeSets() throws IOException {
		// Nothing special here
	}

	/**
	 * Writes a single {@link ChangeSet} to the dump file.
	 * 
	 * @param cs
	 *        The {@link ChangeSet} to write.
	 */
	void writeChangeSet(ChangeSet cs) throws IOException;

	/**
	 * Notifies this {@link IDumpWriter} about the end of change sets.
	 * 
	 * @see #writeChangeSet(ChangeSet)
	 * 
	 * @throws IOException
	 *         When writing causes errors. Is actually not thrown but declared for subclasses.
	 */
	default void endChangeSets() throws IOException {
		// Nothing special here
	}

	/**
	 * Notifies this {@link IDumpWriter} about the begin of additional tables.
	 * 
	 * @see #writeTable(String, Iterable)
	 * 
	 * @throws IOException
	 *         When writing causes errors. Is actually not thrown but declared for subclasses.
	 */
	default void beginTables() throws IOException {
		// Nothing special here
	}

	/**
	 * Writes the value for a non {@link KnowledgeBase} table.
	 */
	void writeTable(String tableName, Iterable<RowValue> rows) throws IOException;

	/**
	 * Notifies this {@link IDumpWriter} about the end of additional tables.
	 * 
	 * @see #writeTable(String, Iterable)
	 * 
	 * @throws IOException
	 *         When writing causes errors. Is actually not thrown but declared for subclasses.
	 */
	default void endTables() throws IOException {
		// Nothing special here
	}

	/**
	 * Notifies this {@link IDumpWriter} about the begin of unversioned types.
	 * 
	 * @see #writeUnversionedType(MetaObject, Iterator)
	 * 
	 * @throws IOException
	 *         When writing causes errors. Is actually not thrown but declared for subclasses.
	 */
	default void beginTypes() throws IOException {
		// Nothing special here
	}

	/**
	 * Writes the value for an unversioned {@link KnowledgeBase} type.
	 */
	void writeUnversionedType(MetaObject type, Iterator<? extends KnowledgeItem> items) throws IOException;

	/**
	 * Notifies this {@link IDumpWriter} about the end of unversioned types.
	 * 
	 * @see #writeUnversionedType(MetaObject, Iterator)
	 * 
	 * @throws IOException
	 *         When writing causes errors. Is actually not thrown but declared for subclasses.
	 */
	default void endTypes() throws IOException {
		// Nothing special here
	}

}
