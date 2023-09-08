/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * Basic Part of an SQL-Statement.
 * 
 * @see SQLQuery#toSql(com.top_logic.basic.sql.DBHelper)
 * 
 * @author <a href=mailto:bhu@top-logic.com>bhu</a>
 */
public interface SQLPart {

	/**
	 * Calls a special method for the concrete type of this {@link SQLPart} in the given
	 * {@link SQLVisitor}.
	 * 
	 * @param <R>
	 *        return type of the visitor
	 * @param <A>
	 *        argument type of the visitor
	 * @param v
	 *        the visitor to call back
	 * @param arg
	 *        the argument to deliver to the visitor
	 * 
	 * @return the return value of the visitor
	 */
	public abstract <R,A> R visit(SQLVisitor<R,A> v, A arg);
	
}
