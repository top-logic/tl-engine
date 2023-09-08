/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.exec;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.db.sql.DefaultSQLVisitor;
import com.top_logic.basic.db.sql.SQLJoin;
import com.top_logic.basic.db.sql.SQLPart;
import com.top_logic.basic.db.sql.SQLTable;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;

/**
 * Visitor that collects {@link TableInfo} for all {@link SQLTable} used in the visited
 * {@link SQLPart}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">bhu</a>
 */
public class LifePeriodColumnBuilder extends DefaultSQLVisitor<Void, Void> {

	private static final Void none = null;
	
	/**
	 * Holder class for information about a database table.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">bhu</a>
	 */
	public static class TableInfo {
		private final String tableAlias;
		private final String revMinColumn;
		private final String revMaxColumn;
		
		/**
		 * Creates a new {@link TableInfo}.
		 * 
		 * @param tableAlias
		 *        Value of {@link #getTableAlias()}.
		 * @param revMinColumn
		 *        Value of {@link #getRevMinColumn()}.
		 * @param revMaxColumn
		 *        Value of {@link #getRevMaxColumn()}.
		 */
		TableInfo(String tableAlias, String revMinColumn, String revMaxColumn) {
			this.tableAlias = tableAlias;
			this.revMinColumn = revMinColumn;
			this.revMaxColumn = revMaxColumn;
		}
		
		/**
		 * The table alias of the table.
		 */
		public String getTableAlias() {
			return tableAlias;
		}
		
		/**
		 * The database name of the column holding the minimum revision.
		 */
		public String getRevMinColumn() {
			return revMinColumn;
		}
		
		/**
		 * The database name of the column holding the maximum revision.
		 */
		public String getRevMaxColumn() {
			return revMaxColumn;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("TableInfo [tableAlias=");
			builder.append(tableAlias);
			builder.append(", revMinColumn=");
			builder.append(revMinColumn);
			builder.append(", revMaxColumn=");
			builder.append(revMaxColumn);
			builder.append("]");
			return builder.toString();
		}
	}
	
	private Map<String, TableInfo> tableInfos = new LinkedHashMap<>();

	private final Collection<String> _ignoreTable;

	/**
	 * Creates a new {@link LifePeriodColumnBuilder}.
	 * 
	 * @param ignoreTable
	 *        table aliases for which no info must be build
	 * 
	 */
	public LifePeriodColumnBuilder(Collection<String> ignoreTable) {
		_ignoreTable = ignoreTable;
	}
	
	/**
	 * Returns the collected {@link TableInfo}s.
	 * 
	 * <p>
	 * The returned map is a mapping from the table alias of an {@link SQLTable} to the
	 * corresponding {@link TableInfo}.
	 * </p>
	 */
	public Map<String, TableInfo> getTableInfos() {
		return tableInfos;
	}
	
	@Override
	public Void visitSQLTable(SQLTable sql, Void arg) {
		String tableName = sql.getTableName();
		String tableAlias = sql.getTableAlias();
		if (_ignoreTable.contains(tableAlias)) {
			return none;
		}
		
		if (tableName.equals(AbstractFlexDataManager.FLEX_DATA_DB_NAME)) {
			tableInfos.put(tableAlias,
				new TableInfo(
					sql.getTableAlias(),
					AbstractFlexDataManager.REV_MIN_DBNAME, 
					AbstractFlexDataManager.REV_MAX_DBNAME));
		} else {
			tableInfos.put(tableAlias,
				new TableInfo(
					sql.getTableAlias(),
					BasicTypes.REV_MIN_DB_NAME,
					BasicTypes.REV_MAX_DB_NAME));
		}
		return none;
	}
	
	@Override
	public Void visitSQLJoin(SQLJoin sql, Void arg) {
		sql.getLeftTable().visit(this, arg);
		sql.getRightTable().visit(this, arg);
		return none;
	}

}
