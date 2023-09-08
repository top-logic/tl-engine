/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.basic.db.sql.SQLFactory.table;
import static com.top_logic.dob.sql.SQLFactory.parameter;
import static com.top_logic.dob.sql.SQLFactory.parameterDef;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLInsert;
import com.top_logic.basic.db.sql.SQLParameter;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.sql.DBHelper;

/**
 * Various static helper functions for SQL.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class SQLH extends com.top_logic.basic.sql.SQLH {

	/**
	 * Creates an insert statement for the given table.
	 */
	public static CompiledStatement createInsert(DBHelper sqlDialect, DBTableMetaObject table) {
		return createInsert(sqlDialect, table.getDBName(), table.getDBAttributes());
	}

	/**
	 * Creates an insert statement for the given attributes into the table with the given name.
	 */
	public static CompiledStatement createInsert(DBHelper sqlDialect, String tableName, List<DBAttribute> attributes) {
		List<String> columnNames = getColumnNames(attributes);
		List<? extends SQLExpression> values = getParameters(attributes);
		SQLInsert insert = insert(table(tableName, NO_TABLE_ALIAS), columnNames, values);

		List<Parameter> parameters = getParameterDefinitions(attributes);
		SQLQuery<?> query = query(parameters, insert);

		return query.toSql(sqlDialect);
	}

	private static List<Parameter> getParameterDefinitions(List<DBAttribute> attributes) {
		ArrayList<Parameter> parameters = new ArrayList<>(attributes.size());
		for (DBAttribute attribute : attributes) {
			parameters.add(paramDef(attribute));
		}
		return parameters;
	}

	private static List<? extends SQLExpression> getParameters(List<DBAttribute> attributes) {
		ArrayList<SQLExpression> parameters = new ArrayList<>(attributes.size());
		for (DBAttribute attribute : attributes) {
			parameters.add(param(attribute));
		}
		return parameters;
	}

	private static Parameter paramDef(DBAttribute attribute) {
		return parameterDef(attribute, attribute.getDBName());
	}

	private static SQLParameter param(DBAttribute attribute) {
		return parameter(attribute, attribute.getDBName());
	}

	private static List<String> getColumnNames(List<DBAttribute> attributes) {
		ArrayList<String> columnNames = new ArrayList<>(attributes.size());
		for (DBAttribute attribute : attributes) {
			columnNames.add(attribute.getDBName());
		}
		return columnNames;
	}

}

