/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Helper for building custom SQL statements in a manageable way.
 *
 * see TestStatementScope for an example
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StatementScope {
	private int nextParamIndex = 1;
	private int nextResultIndex = 1;

	/*package protected*/ int nextParamIndex() {
		return nextParamIndex++;
	}

	/*package protected*/ int nextResultIndex() {
		return nextResultIndex++;
	}

	/**
	 * A new parameter object that can be used to construct a <code>?</code>
	 * parameter in a {@link PreparedStatement}.
	 *
	 * <p>
	 * After the statement string is created by appending the resulting
	 * {@link Param} object at its location, its {@link Param#getIndex()} is
	 * automatically deduced. This index can be used in
	 * {@link PreparedStatement#setObject(int, Object)} and other methods to
	 * fill the parameter.
	 * </p>
	 */
	public Param newParam() {
		return new ParamImpl() {
			@Override
			public String toString() {
				init(nextParamIndex());
				return "?";
			}
		};
	}

	/**
	 * A new result expression object that can be used to construct a result
	 * expression (with the given value) in a {@link PreparedStatement}.
	 *
	 * <p>
	 * After the statement string is created by appending the resulting
	 * {@link Param} object at its location, its {@link Param#getIndex()} is
	 * automatically deduced. This index can be used in
	 * {@link ResultSet#getObject(int)} and other methods to get the
	 * corresponding value.
	 * </p>
	 */
	public Param newResult(final String expr) {
		return new ParamImpl() {
			@Override
			public String toString() {
				init(nextResultIndex());
				return expr;
			}
		};
	}

	/**
	 * Internal {@link Param} implementation.
	 *
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private abstract class ParamImpl implements Param {
		private int paramIndex;

		protected ParamImpl() {
			// Subclass constructor.
		}

		@Override
		public int getIndex() {
			assert paramIndex > 0 : "Parameter index not yet initialized.";
			return paramIndex;
		}

		protected void init(int index) {
			assert paramIndex == 0 : "Parameter must be inserted into a statement only once.";
			paramIndex = index;
		}

		@Override
		public abstract String toString();
	}

}