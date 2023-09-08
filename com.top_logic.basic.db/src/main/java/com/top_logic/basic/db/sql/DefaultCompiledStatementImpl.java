/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;

/**
 * {@link CompiledStatement} that creates a {@link PreparedStatement} from the source and sets the
 * arguments into that statement
 * 
 * @see StatementBuilder
 * @see DirectCompiledStatement compiled statement that inserts the argument into the actual SQL
 *      string
 * 
 *          com.top_logic.knowledge.service.db2.sql.StatementBuilder.CompiledStatementImpl
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class DefaultCompiledStatementImpl extends AbstractCompiledStatementImpl {

	final DBType[] types;
	
	private final String source;

	/**
	 * Creates a {@link DefaultCompiledStatementImpl}.
	 * 
	 * @param sqlDialect
	 *        The SQL dialect of the used database.
	 * @param types
	 *        Array containing the database types of the parameter.
	 * @param source
	 *        The actual SQL String.
	 * @param conversions
	 *        see
	 *        {@link AbstractCompiledStatementImpl#AbstractCompiledStatementImpl(DBHelper,List, int[], Map, Object[])}
	 * @param parameters
	 *        see
	 *        {@link AbstractCompiledStatementImpl#AbstractCompiledStatementImpl(DBHelper,List, int[], Map, Object[])}
	 * @param argumentIndexByName
	 *        see
	 *        {@link AbstractCompiledStatementImpl#AbstractCompiledStatementImpl(DBHelper,List, int[], Map, Object[])}
	 * @param constants
	 *        see
	 *        {@link AbstractCompiledStatementImpl#AbstractCompiledStatementImpl(DBHelper,List, int[], Map, Object[])}
	 */
	DefaultCompiledStatementImpl(DBHelper sqlDialect, DBType[] types, String source, List<Conversion> conversions,
			int[] parameters, Map<String, Integer> argumentIndexByName, Object[] constants) {
		super(sqlDialect, conversions, parameters, argumentIndexByName, constants);
		this.types = types;
		this.source = source;
	}

	@Override
	public String toString(Object[] arguments) {
		return source;
	}
	
	@Override
	protected void setArguments(PreparedStatement statement, Object[] arguments) throws SQLException {
		for (int n = 0, cnt = _parameters.length; n < cnt; n++) {
			Object value = getArgument(arguments, n);
			
			_sqlDialect.setFromJava(statement, value, n + 1, types[n]);
		}
	}

	@Override
	public CompiledStatement bind(Object... environment) {
		return CompiledStatementClosure.bind(this, environment);
	}

}
