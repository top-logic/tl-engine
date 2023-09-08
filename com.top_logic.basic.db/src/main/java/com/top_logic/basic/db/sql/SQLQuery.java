/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;

/**
 * A query to a relational database.
 * 
 * <p>
 * A query is an optionally {@link #getParameters() parameterized} {@link SQLStatement}.
 * </p>
 * 
 * @param <S>
 *        The concrete {@link SQLStatement} type of this query.
 * 
 * @see SQLFactory#query(SQLStatement)
 * @see #toSql(com.top_logic.basic.sql.DBHelper)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLQuery<S extends SQLStatement> extends AbstractSQLPart {
	
	/**
	 * Abstract declaration of a query parameter.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public abstract static class Parameter {
		
		private final String name;
		
		Parameter(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}

	}
	
	/**
	 * Declaration of a simple parameter, later filled with one value.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class SimpleParameter extends Parameter {

		private DBType _dbType;

		SimpleParameter(DBType dbType, String name) {
			super(name);
			_dbType = dbType;
		}

		public DBType getDBType() {
			return _dbType;
		}

	}

	/**
	 * Declaration of a set parameter, later filled with a collection of tuples. Simple values are
	 * regarded as 1-ary tuple.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class SetParameter extends Parameter {

		private DBType[] _dbTypes;

		SetParameter(DBType[] dbTypes, String name) {
			super(name);
			_dbTypes = dbTypes;
		}

		/**
		 * {@link DBType}s of the tuple entries.
		 * 
		 * <p>
		 * Each tuple in the later filled value set must have the same length as the returned
		 * {@link DBType} array. Moreover the types of the values in the tuple must match the
		 * returned {@link DBType}s.
		 * </p>
		 */
		public DBType[] getDBType() {
			return _dbTypes;
		}

	}

	private List<Parameter> _parameters;

	private S _statement;

	SQLQuery(List<Parameter> parameters, S statement) {
		this._parameters = parameters;
		this._statement = statement;
	}
	
	public S getStatement() {
		return _statement;
	}
	
	public void setStatement(S statement) {
		this._statement = statement;
	}
	
	public List<Parameter> getParameters() {
		return _parameters;
	}
	
	public void setParameters(List<Parameter> parameters) {
		this._parameters = parameters;
	}
	
	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLQuery(this, arg);
	}

	/**
	 * Creates a {@link CompiledStatement} for the given {@link SQLQuery}
	 * 
	 * @see SQLQuery#toSql(DBHelper, SQLPart, Map)
	 */
	public CompiledStatement toSql(DBHelper sqlDialect) {
		Map<String, Integer> argumentIndexByName = new HashMap<>();
		List<Parameter> parameters = getParameters();
		for (int n = 0, cnt = parameters.size(); n < cnt; n++) {
			Parameter parameter = parameters.get(n);
			
			argumentIndexByName.put(parameter.getName(), n);
		}
		
		return SQLQuery.toSql(sqlDialect, this, argumentIndexByName);
	}

	/**
	 * Creates a {@link CompiledStatement} for the given {@link SQLPart} and the given argument
	 * index map
	 * 
	 * @param sqlDialect
	 *        abstraction of the used database.
	 * @param expr
	 *        the {@link SQLPart} to build a {@link CompiledStatement} for
	 * @param argumentIndexByName
	 *        a mapping of the arguments in the given {@link SQLPart} to the index in the arguments
	 *        later given to execute the compiled statement (see
	 *        {@link CompiledStatement#executeQuery(java.sql.Connection, Object...)})
	 * 
	 * @return the compiled statement to execute later with the correct arguments
	 */
	public static final CompiledStatement toSql(DBHelper sqlDialect, SQLPart expr, Map<String, Integer> argumentIndexByName) {
		boolean noPrepStatement = AbstractStatementBuilder.checkPreparedStatement(expr, sqlDialect);
		if (noPrepStatement) {
			return DirectStatementBuilder.compileStatement(sqlDialect, expr, argumentIndexByName);
		} else {
			return StatementBuilder.compileStatement(sqlDialect, expr, argumentIndexByName);
		}
	}

}
