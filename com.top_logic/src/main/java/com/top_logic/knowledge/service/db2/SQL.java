/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.List;

import com.top_logic.basic.sql.DBType;

/**
 * Database vendor independent representation of a prepared statement or a
 * fragment thereof.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SQL {
	
	public static class Argument {

		/**
		 * @see #getSqlType()
		 */
		private final DBType sqlType;

		/**
		 * @see #getValue()
		 */
		private final Object value;
		
		public Argument(DBType sqlType, Object value) {
			this.sqlType = sqlType;
			this.value = value;
		}
		
		/**
		 * The database type of {@link #getValue()}.
		 */
		public DBType getSqlType() {
			return sqlType;
		}

		/**
		 * The Java value of this argument.
		 */
		public Object getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return "(" + sqlType + ") " + value;
		}

	}
	
	/**
	 * The statement source.
	 */
	String getSource();

	/**
	 * The arguments to pass to parameters of this statement {@link #getSource()}.
	 */
	List getArguments();

}
