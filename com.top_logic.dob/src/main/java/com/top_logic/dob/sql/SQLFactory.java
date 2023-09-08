/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.sql;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLColumnReference;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLParameter;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.db.sql.SQLTable;
import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOStructure;

/**
 * Service class to created {@link SQLExpression}s from database definitions ({@link DBAttribute},
 * {@link DBMetaObject}, ...).
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SQLFactory extends com.top_logic.basic.db.sql.SQLFactory {

	/**
	 * Create {@link Parameter} of the same type as the given {@link DBAttribute}.
	 */
	public static Parameter parameterDef(DBAttribute attribute, String name) {
		return parameterDef(attribute.getSQLType(), name);
	}

	/**
	 * Create {@link Parameter} of the same types as the given {@link DBAttribute}s.
	 */
	public static Parameter setParameterDef(String name, DBAttribute... attributes) {
		return setParameterDef(name, getTypes(attributes));
	}

	private static DBType[] getTypes(DBAttribute... attributes) {
		DBType[] types = new DBType[attributes.length];
		for (int i = 0; i < types.length; i++) {
			types[i] = attributes[i].getSQLType();
		}
		return types;
	}

	/**
	 * Representation of column represented by given {@link DBAttribute} of the table with the given
	 * table alias.
	 */
	public static SQLColumnReference column(String tableAlias, DBAttribute attribute) {
		return column(tableAlias, attribute, attribute.isSQLNotNull());
	}

	/**
	 * Representation of column represented by given {@link DBAttribute} of the table with the given
	 * table alias.
	 */
	public static SQLColumnReference column(String tableAlias, DBAttribute attribute, boolean notNull) {
		return column(tableAlias, attribute.getDBName(), notNull);
	}

	/**
	 * Creates a parameter with a {@link DBType} that matches the type of the given attribute.
	 * 
	 * @see SQLFactory#parameter(DBType, String)
	 */
	public static SQLParameter parameter(DBAttribute attribute, String name) {
		return parameter(attribute.getSQLType(), name);
	}

	/**
	 * Creates a {@link SQLTable} for the given {@link DBTableMetaObject meta object}.
	 */
	public static SQLTable table(DBTableMetaObject table, String tableAlias) {
		return table(table.getDBName(), tableAlias);
	}

	/**
	 * Creates a {@link SQLColumnDefinition}s for all attributes in the given type.
	 */
	public static List<SQLColumnDefinition> columnDefs(String tableAlias, MOStructure type) {
		List<MOAttribute> attributes = type.getAttributes();
		ArrayList<SQLColumnDefinition> result = new ArrayList<>(attributes.size());
		for (MOAttribute attribute : attributes) {
			for (DBAttribute dbAttr : attribute.getDbMapping()) {
				String dbName = dbAttr.getDBName();
				SQLColumnReference columnRef = column(tableAlias, dbAttr);
				result.add(columnDef(columnRef, dbName));
			}
		}
		return result;
	}

	/**
	 * Creates a list of {@link SQLColumnDefinition} containing a definition for each
	 * {@link DBAttribute} known by the given type.
	 * 
	 * @param type
	 *        The type to create column references for.
	 * @param tableAlias
	 *        The table alias of the type in the result SQL.
	 */
	public static List<SQLColumnDefinition> allColumnRefs(MOStructure type, String tableAlias) {
		List<SQLColumnDefinition> allColumns = new ArrayList<>();
		for (MOAttribute attribute : type.getAttributes()) {
			for (DBAttribute column : attribute.getDbMapping()) {
				String columnName = column.getDBName();
				allColumns.add(columnDef(column(tableAlias, columnName), columnName));
			}
		}
		return allColumns;
	}

	/**
	 * Creates an {@link SQLExpression} that checks that the value of the given attribute is greater
	 * or equal to the start value and less than the stop value.
	 * 
	 * <p>
	 * It is expected that the value value of the attribute is always not <code>null</code>, when
	 * the corresponding attribute is mandatory. (It seems to be clear but the attribute could
	 * represent a column in the empty part of a left join.)
	 * </p>
	 * 
	 * @param tableAlias
	 *        Alias of the table of the attribute used in the SQL.
	 * @param attr
	 *        The attribute to check.
	 * @param startIncl
	 *        Only rows with value &gt;= this value are found.
	 * @param stopExcl
	 *        Only rows with value &lt; this value are found.
	 */
	public static <T extends Comparable<T>> SQLExpression attributeRange(String tableAlias, DBAttribute attr,
			T startIncl, T stopExcl) {
		if (startIncl.compareTo(stopExcl) < 0) {
			return and(
				ge(column(tableAlias, attr), literal(attr.getSQLType(), startIncl)),
				lt(column(tableAlias, attr), literal(attr.getSQLType(), stopExcl)));
		}
		return literalFalseLogical();
	}

	/**
	 * Creates an {@link SQLExpression} that checks that the value of the given attribute is equal
	 * to the given value.
	 * 
	 * <p>
	 * It is expected that the value value of the attribute is always not <code>null</code>, when
	 * the corresponding attribute is mandatory. (It seems to be clear but the attribute could
	 * represent a column in the empty part of a left join.)
	 * </p>
	 * 
	 * @param tableAlias
	 *        Alias of the table of the attribute used in the SQL.
	 * @param attribute
	 *        The attribute to check.
	 * @param value
	 *        Only rows with given value are found.
	 */
	public static SQLExpression attributeEq(String tableAlias, DBAttribute attribute, Object value) {
		SQLExpression result;
		SQLExpression column = column(tableAlias, attribute);
		if (value == null) {
			result = isNull(column);
		} else {
			result = eq(column, literal(attribute.getSQLType(), value));
		}
		return result;
	}

}

