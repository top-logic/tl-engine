/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.TypedConfiguration;

/**
 * Factory for {@link DBSchema} parts.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DBSchemaFactory {

	/**
	 * Creates a new {@link DBSchema}.
	 */
	public static DBSchema createDBSchema() {
		return TypedConfiguration.newConfigItem(DBSchema.class);
	}

	/**
	 * Creates a new {@link DBTable}.
	 * 
	 * @param name
	 *        See {@link DBTable#getName()}.
	 */
	public static DBTable createTable(String name) {
		DBTable result = TypedConfiguration.newConfigItem(DBTable.class);
		result.setName(name);
		return result;
	}

	/**
	 * Creates a new {@link DBColumn}.
	 * 
	 * @param name
	 *        See {@link DBColumn#getName()}.
	 */
	public static DBColumn createColumn(String name) {
		DBColumn result = TypedConfiguration.newConfigItem(DBColumn.class);
		result.setName(name);
		return result;
	}

	/**
	 * Creates a new {@link DBIndex}.
	 * 
	 * @param name
	 *        See {@link DBIndex#getName()}.
	 */
	public static DBIndex createIndex(String name) {
		DBIndex result = TypedConfiguration.newConfigItem(DBIndex.class);
		result.setName(name);
		return result;
	}

	/**
	 * Creates a new {@link DBPrimary}.
	 */
	public static DBPrimary createPrimary() {
		return TypedConfiguration.newConfigItem(DBPrimary.class);
	}

	/**
	 * Creates a new {@link DBForeignKey}.
	 * 
	 * @param name
	 *        See {@link DBForeignKey#getName()}.
	 */
	public static DBForeignKey createForeignKey(String name) {
		DBForeignKey result = TypedConfiguration.newConfigItem(DBForeignKey.class);
		result.setName(name);
		return result;
	}

	/**
	 * Creates a {@link DBColumnRef} to the given {@link DBColumn}.
	 */
	public static DBColumnRef ref(DBColumn column) {
		return ref(column.getName());
	}

	/**
	 * Create {@link DBColumnRef references} to the given {@link DBColumn columns}.
	 */
	public static List<DBColumnRef> refs(List<? extends DBColumn> columns) {
		ArrayList<DBColumnRef> result = new ArrayList<>(columns.size());
		for (DBColumn column : columns) {
			result.add(ref(column.getName()));
		}
		return result;
	}

	/**
	 * Creates a {@link DBColumnRef} to a {@link DBColumn} with the given name.
	 */
	public static DBColumnRef ref(String name) {
		DBColumnRef result = TypedConfiguration.newConfigItem(DBColumnRef.class);
		result.setName(name);
		return result;
	}

	/**
	 * Creates an {@link DBIndexRef} to the given {@link DBIndex}.
	 */
	public static DBIndexRef ref(DBIndex index) {
		return indexRef(index.getName());
	}

	/**
	 * Creates a {@link DBIndexRef} to a {@link DBIndex} with the given name.
	 */
	public static DBIndexRef indexRef(String name) {
		DBIndexRef result = TypedConfiguration.newConfigItem(DBIndexRef.class);
		result.setName(name);
		return result;
	}

	/**
	 * Creates an {@link DBTableRef} to the given {@link DBTable}.
	 */
	public static DBTableRef ref(DBTable table) {
		return tableRef(table.getName());
	}

	/**
	 * Creates a {@link DBIndexRef} to a {@link DBIndex} with the given name.
	 */
	public static DBTableRef tableRef(String name) {
		DBTableRef result = TypedConfiguration.newConfigItem(DBTableRef.class);
		result.setName(name);
		return result;
	}

}
