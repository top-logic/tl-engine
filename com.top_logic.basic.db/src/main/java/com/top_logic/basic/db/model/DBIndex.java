/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model;

import java.util.List;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.sql.DBHelper;

/**
 * Definition of an index of a {@link DBTable}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DBIndex extends DBTablePart {
	
	/**
	 * @see #getKind()
	 */
	String KIND = "kind";

	/**
	 * @see #getColumnRefs()
	 */
	String COLUMNS = "columns";

	/**
	 * @see #getColumns()
	 */
	String COLUMNS_RESOLVED = "columns-resolved";

	/**
	 * @see #getCompress()
	 */
	String COMPRESS = "compress";

	/**
	 * Kind of a {@link DBIndex}.
	 * 
	 * @see DBIndex#getKind()
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public enum Kind implements ExternallyNamed {
		/**
		 * An index that does not imply any constraints.
		 * 
		 * <p>
		 * A default index is for pure access optimization.
		 * </p>
		 */
		DEFAULT("default"),
		
		/**
		 * Marks the {@link DBIndex} as unique. 
		 * 
		 * <p>
		 * A table with a unique index may only contain rows that differ in at least
		 * one column of the unique index.
		 * </p>
		 */
		UNIQUE("unique"),

		/**
		 * A {@link #UNIQUE} index that serves as primary identifier for table rows.
		 * 
		 * <p>
		 * A table may only contain one primary index.
		 * </p>
		 */
		PRIMARY("primary");

		private final String _externalName;

		private Kind(String name) {
			_externalName = name;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * The {@link Kind} of this {@link DBIndex}.
	 */
	@Name(KIND)
	Kind getKind();

	/**
	 * Sets the {@link #getKind()} property.
	 */
	void setKind(Kind value);
	
	/**
	 * Whether this index is unique (of {@link Kind} primary or unique).
	 */
	@Derived(fun = IsDerived.class, args = @Ref(value = KIND))
	boolean isUnique();
	
	/**
	 * All columns of the {@link #getTable()} that are indexed by this index.
	 */
	@Name(COLUMNS)
	@Key(DBColumnRef.NAME_ATTRIBUTE)
	List<DBColumnRef> getColumnRefs();
	
	/**
	 * Resolved {@link DBColumn} references.
	 * 
	 * <p>
	 * <b>Note:</b> The resulting list is unmodifiable. Therefore it is type parameterized in a way
	 * that makes accidental modification hard.
	 * </p>
	 * 
	 * @see #getColumnRefs()
	 */
	@Name(COLUMNS_RESOLVED)
	@Derived(fun = DBColumnRef.ReferencedColumns.class, args = { @Ref(value = { TABLE }),
		@Ref(value = { TABLE, DBTable.COLUMNS }), @Ref(value = COLUMNS) })
	List<? extends DBColumn> getColumns();

	/**
	 * The number of columns to store in compressed format (if supported by the RDBMS).
	 * 
	 * @see DBHelper#getAppendIndex(int)
	 */
	@Name(COMPRESS)
	int getCompress();

	/**
	 * @see #getCompress()
	 */
	void setCompress(int compress);

	/**
	 * Implementation of {@link DBIndex#isUnique()}.
	 */
	class IsDerived extends Function1<Boolean, Kind> {
		@Override
		public Boolean apply(Kind arg) {
			if (arg == null) {
				return null;
			}
			switch (arg) {
				case PRIMARY:
				case UNIQUE:
					return true;
				default:
					return false;
			}
		}
	}
}
