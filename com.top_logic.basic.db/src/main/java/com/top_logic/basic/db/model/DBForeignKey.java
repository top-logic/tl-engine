/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model;

import java.util.List;

import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.sql.DBConstraintType;
import com.top_logic.basic.sql.DBDeferability;

/**
 * Definition of a foreign key constraint on a {@link DBTable}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DBForeignKey extends DBTablePart {

	/**
	 * @see #getSourceColumnRefs()
	 */
	String SOURCE_COLUMNS = "source-columns";

	/**
	 * @see #getSourceColumns()
	 */
	String SOURCE_COLUMNS_RESOLVED = "source-columns-resolved";

	/**
	 * @see #getTargetColumnRefs()
	 */
	String TARGET_COLUMNS = "target-columns";
	
	/**
	 * @see #getTargetColumns()
	 */
	String TARGET_COLUMNS_RESOLVED = "target-columns-resolved";

	/**
	 * @see #getOnDelete()
	 */
	String ON_DELETE = "on-delete";

	/**
	 * @see #getOnUpdate()
	 */
	String ON_UPDATE = "on-update";

	/**
	 * @see #getTargetTable()
	 */
	String TARGET_TABLE_REF = "target-table";

	/**
	 * @see #getTargetTableRef()
	 */
	String TARGET_TABLE_RESOLVED = "target-table-resolved";

	/**
	 * The columns that build up the reference.
	 * 
	 * <p>
	 * All columns must be from the {@link #getTable()} of this constraint.
	 * </p>
	 * 
	 * <p>
	 * The number of columns must match the number of {@link #getTargetColumnRefs()} of the
	 * {@link #getTargetTable()}.
	 * </p>
	 */
	@Name(SOURCE_COLUMNS)
	@Key(DBColumnRef.NAME_ATTRIBUTE)
	List<DBColumnRef> getSourceColumnRefs();
	
	/**
	 * The resolved {@link #getSourceColumnRefs()} references from the {@link #getTable()}.
	 * 
	 * <p>
	 * Note: The resulting list is unmodifiable. Therefore it is type parameterized in a way that
	 * makes accidental modification hard.
	 * </p>
	 */
	@Name(SOURCE_COLUMNS_RESOLVED)
	@Derived(fun = DBColumnRef.ReferencedColumns.class, args = { @Ref(TABLE), @Ref({ TABLE, DBTable.COLUMNS }),
		@Ref(SOURCE_COLUMNS) })
	List<? extends DBColumn> getSourceColumns();

	/**
	 * The columns referenced in the {@link #getTargetTable() target table}.
	 * 
	 * @see #getTargetColumnRefs()
	 */
	@Name(TARGET_COLUMNS_RESOLVED)
	@Derived(fun = DBColumnRef.ReferencedColumns.class,
			args = { @Ref(TARGET_TABLE_RESOLVED), @Ref({ TARGET_TABLE_RESOLVED, DBTable.COLUMNS }),
				@Ref(TARGET_COLUMNS) })
	List<DBColumn> getTargetColumns();
	
	/**
	 * The referenced table.
	 * 
	 * @see #getTargetTable()
	 */
	@Name(TARGET_TABLE_REF)
	DBTableRef getTargetTableRef();

	/**
	 * Sets the {@link #getTargetTableRef()} property.
	 */
	void setTargetTableRef(DBTableRef value);
	
	/**
	 * The resolved {@link #getTargetTableRef()} form the {@link DBSchema} context.
	 * 
	 * @see #getTargetColumnRefs()
	 */
	@Name(TARGET_TABLE_RESOLVED)
	@Derived(fun = DBTableRef.ReferencedTable.class, args = { @Ref(SCHEMA), @Ref(TARGET_TABLE_REF) })
	DBTable getTargetTable();

	/**
	 * The unique index on the {@link #getTargetTable() target table} that is referenced.
	 * 
	 * @see #getTargetColumns()
	 */
	@Name(TARGET_COLUMNS)
	List<DBColumnRef> getTargetColumnRefs();

	/**
	 * Action that is taken, if the referenced target row is deleted.
	 */
	@Name(ON_DELETE)
	DBConstraintType getOnDelete();

	/**
	 * Sets the {@link #getOnDelete()} property.
	 */
	void setOnDelete(DBConstraintType value);

	/**
	 * Action that is taken, if the referenced target row is updated.
	 */
	@Name(ON_UPDATE)
	DBConstraintType getOnUpdate();

	/**
	 * Sets the {@link #getOnUpdate()} property.
	 */
	void setOnUpdate(DBConstraintType value);

	/**
	 * Whether checking of the constraint is deferred.
	 */
	DBDeferability getDeferability();

	/**
	 * @see #getDeferability()
	 */
	void setDeferability(DBDeferability value);

}
