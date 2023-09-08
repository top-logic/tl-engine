/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.sql.DBHelper;

/**
 * Definition of a database table.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DBTable extends DBNamedPart {
	
	/**
	 * @see #getColumns()
	 */
	String COLUMNS = "columns";
	
	/**
	 * @see #getPrimaryKey()
	 */
	String PRIMARY_KEY = "primary-key";

	/**
	 * @see #getIndices()
	 */
	String KEYS = "keys";

	/**
	 * @see #getForeignKeys()
	 */
	String FOREIGN_KEYS = "foreign-keys";

	/**
	 * @see #getPKeyStorage()
	 */
	String PKEY_STORAGE = "pkey-storage";

	/**
	 * @see #getCompress()
	 */
	String COMPRESS = "compress";

	@Override
	@Container
	public DBSchema getSchema();

	/**
	 * The columns of this table.
	 * 
	 * @see DBColumn#getTable()
	 */
	@Name(COLUMNS)
	@Key(DBColumn.NAME)
	List<DBColumn> getColumns();

	/**
	 * Find the column with the given name.
	 * 
	 * @param name
	 *        The {@link DBColumn#getName() name} of the column to search.
	 * 
	 * @return The {@link DBColumn} with the given name, or <code>null</code>,
	 *         if this table does not contain a column with the given name.
	 */
	@Indexed(collection = COLUMNS)
	DBColumn getColumn(String name);

	/**
	 * The database primary key of this table.
	 * 
	 * @return The primary ({@link DBIndex#isUnique() unique}) index of this
	 *         table, <code>null</code>, if this table has no explicit primary
	 *         key.
	 */
	@Name(PRIMARY_KEY)
	DBPrimary getPrimaryKey();
	
	/**
	 * Sets the {@link #getPrimaryKey()} property.
	 */
	void setPrimaryKey(DBPrimary value);

	/**
	 * Additional indices of this table.
	 * 
	 * @return A list of additional indices of this table, witch does not
	 *         contain the {@link #getPrimaryKey()}.
	 */
	@Name(KEYS)
	@Key(DBIndex.NAME)
	Collection<DBIndex> getIndices();

	/**
	 * Find the {@link DBIndex} with the given {@link DBIndex#getName() name}.
	 * 
	 * @param name
	 *        The index name to search for.
	 * @return The index of this table with the given name, or <code>null</code>,
	 *         if this table does not contain an index with the given name.
	 */
	@Indexed(collection = KEYS)
	DBIndex getIndex(String name);
	
	/**
	 * The {@link DBForeignKey} constraints defined on this table. 
	 */
	@Name(FOREIGN_KEYS)
	@Key(DBForeignKey.NAME)
	Collection<DBForeignKey> getForeignKeys();

	/**
	 * Retrieves the {@link DBForeignKey} of this table with the given
	 * {@link DBForeignKey#getName() name}.
	 * 
	 * @param name
	 *        The name of the {@link DBForeignKey} to search.
	 * 
	 * @return The {@link DBForeignKey} with the given name, or
	 *         <code>null</code>, if there is no {@link DBForeignKey} constraint
	 *         on this table with the given name.
	 */
	@Indexed(collection = FOREIGN_KEYS)
	DBForeignKey getForeignKey(String name);

	/**
	 * Store data along the primary key (if supported by the RDBMS).
	 * 
	 * <p>
	 * Primary key storage improves the performance of bulk access to rows with adjacent primary key
	 * values. Since the data is physically aligning along the primary key, seek operations for
	 * those bulk access are dramatically reduced. On the other hand, performance of updating and
	 * inserting data is worse on primary key aligned data.
	 * </p>
	 * 
	 * @see DBHelper#appendTableOptions(Appendable, boolean, int)
	 */
	@Name(PKEY_STORAGE)
	boolean getPKeyStorage();

	/**
	 * @see #getPKeyStorage()
	 */
	void setPKeyStorage(boolean value);

	/**
	 * The number of columns to store in compressed format, if {@link #getPKeyStorage()} is also
	 * given.
	 * 
	 * @see DBHelper#appendTableOptions(Appendable, boolean, int)
	 */
	int getCompress();

	/**
	 * @see #getCompress()
	 */
	@Name(COMPRESS)
	void setCompress(int value);

}
