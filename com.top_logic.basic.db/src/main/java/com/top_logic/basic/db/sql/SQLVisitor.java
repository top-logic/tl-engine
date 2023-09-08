/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * Visitor of {@link SQLExpression}s.
 * 
 * @param <R>
 *        The result type of the visit.
 * @param <A>
 *        The argument type of the visit.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SQLVisitor<R,A> {

	/** Argument for case argument is {@link Void}. */
	Void none = null;

	/**
	 * Visit case for {@link SQLQuery}.
	 */
	<S extends SQLStatement> R visitSQLQuery(SQLQuery<S> sql, A arg);
	
	/**
	 * Visit case for {@link SQLSelect}.
	 */
	R visitSQLSelect(SQLSelect sql, A arg);
	
	/**
	 * Visit case for {@link SQLInsert}.
	 */
	R visitSQLInsert(SQLInsert sql, A arg);

	/**
	 * Visit case for {@link SQLInsertSelect}.
	 */
	R visitSQLInsertSelect(SQLInsertSelect sql, A arg);

	/**
	 * Visit case for {@link SQLUpdate}.
	 */
	R visitSQLUpdate(SQLUpdate sql, A arg);

	/**
	 * Visit case for {@link SQLUnion}.
	 */
	R visitSQLUnion(SQLUnion sql, A arg);
	
	/**
	 * Visit case for {@link SQLColumnReference}.
	 */
	R visitSQLColumnReference(SQLColumnReference sql, A arg);
	
	/**
	 * Visit case for {@link SQLColumnDefinition}.
	 */
	R visitSQLColumnDefinition(SQLColumnDefinition sql, A arg);
	
	/**
	 * Visit case for {@link SQLOrder}.
	 */
	R visitSQLOrder(SQLOrder sql, A arg);
	
	/**
	 * Visit case for {@link SQLParameter}.
	 */
	R visitSQLParameter(SQLParameter sql, A arg);
	
	/**
	 * Visit case for {@link SQLLiteral}.
	 */
	R visitSQLLiteral(SQLLiteral sql, A arg);
	
	/**
	 * Visit case for {@link SQLBoolean}.
	 */
	R visitSQLBoolean(SQLBoolean sql, A arg);
	
	/**
	 * Visit case for {@link SQLBinaryExpression}.
	 */
	R visitSQLBinaryExpression(SQLBinaryExpression sql, A arg);
	
	/**
	 * Visit case for {@link SQLCast}.
	 */
	R visitSQLCast(SQLCast sql, A arg);

	/**
	 * Visit case for {@link SQLFunction}.
	 */
	R visitSQLFunction(SQLFunction sql, A arg);
	
	/**
	 * Visit case for {@link SQLNot}.
	 */
	R visitSQLNot(SQLNot sql, A arg);
	
	/**
	 * Visit case for {@link SQLIsNull}.
	 */
	R visitSQLIsNull(SQLIsNull sql, A arg);
	
	/**
	 * Visit case for {@link SQLInSet}.
	 */
	R visitSQLInSet(SQLInSet sql, A arg);
	
	/**
	 * Visit case for {@link SQLTuple}.
	 */
	R visitSQLTuple(SQLTuple sql, A arg);

	/**
	 * Visit case for {@link SQLTable}.
	 */
	R visitSQLTable(SQLTable sql, A arg);
	
	/**
	 * Visit case for {@link SQLJoin}.
	 */
	R visitSQLJoin(SQLJoin sql, A arg);
	
	/**
	 * Visit case for {@link SQLSubQuery}.
	 */
	R visitSQLSubQuery(SQLSubQuery sql, A arg);

	/**
	 * Visit case for {@link SQLLimit}
	 */
	R visitSQLLimit(SQLLimit sql, A arg);

	/**
	 * Visit case for {@link SQLSetLiteral}
	 */
	R visitSQLSetLiteral(SQLSetLiteral sql, A arg);

	/**
	 * Visit case for {@link SQLSetParameter}
	 */
	R visitSQLSetParameter(SQLSetParameter sql, A arg);

	/**
	 * Visit case for {@link SQLCase}
	 */
	R visitSQLCase(SQLCase sql, A arg);

	/**
	 * Visit case for {@link SQLDelete}
	 */
	R visitSQLDelete(SQLDelete sql, A arg);

	/**
	 * Visit case for {@link SQLAlterTable}
	 */
	R visitSQLAlterTable(SQLAlterTable sql, A arg);

	/**
	 * Visit case for {@link SQLAddColumn}
	 */
	R visitSQLAddColumn(SQLAddColumn sql, A arg);

	/**
	 * Visit case for {@link SQLModifyColumn}
	 */
	R visitSQLModifyColumn(SQLModifyColumn sql, A arg);

	/**
	 * Visit case for {@link SQLDropColumn}
	 */
	R visitSQLDropColumn(SQLDropColumn sql, A arg);

	/**
	 * Visit case for {@link SQLAddIndex}
	 */
	R visitSQLAddIndex(SQLAddIndex sql, A arg);

	/**
	 * Visit case for {@link SQLDropIndex}
	 */
	R visitSQLDropIndex(SQLDropIndex sql, A arg);
}
