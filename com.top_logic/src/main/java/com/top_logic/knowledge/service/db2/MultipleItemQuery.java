/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.basic.db.sql.SQLFactory.column;
import static com.top_logic.dob.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.db.sql.SQLOrder;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.db.sql.SQLTableReference;
import com.top_logic.basic.sql.CollationHint;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.db2.ItemQuery.DirectItemResult;

/**
 * Selects objects of given type that matches one of the given filters. The result are retrieved
 * orderd by given attributes.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MultipleItemQuery {

	private final CompiledStatement[] _statements;

	private final DBHelper _sqlDialect;

	private final DBAttribute[] _order;

	private final boolean[] _descending;

	private final MOClass _type;

	/**
	 * Creates a new {@link MultipleItemQuery}.
	 * 
	 * @param type
	 *        Type of the objects to fetch.
	 * @param tableAlias
	 *        Alias of the table used to create the filter expressions.
	 * @param filter
	 *        Only objects that matches one of the given filter are includes in the result.
	 * @param order
	 *        The results are ordered by the given attributes.
	 * @param descending
	 *        Whether the sort oder of the corresponding attribute is descending.
	 */
	public MultipleItemQuery(DBHelper sqlDialect, MOClass type, String tableAlias, SQLExpression[] filter,
			DBAttribute[] order, boolean[] descending) {
		assert order.length == descending.length : "Number order attribute and order direction must match: "
			+ Arrays.toString(order) + " " + Arrays.toString(descending);
		_sqlDialect = sqlDialect;
		_type = type;
		_order = order;
		_descending = descending;
		CompiledStatement[] statements = new CompiledStatement[filter.length];
		for (int index = 0; index < filter.length; index++) {
			List<SQLColumnDefinition> columns = allColumnRefs(type, tableAlias);
			SQLTableReference table = table(type.getDBMapping(), tableAlias);
			SQLExpression where = filter[index];
			List<SQLOrder> resultOrder;
			resultOrder = createOrder(tableAlias);
			SQLSelect select = select(columns, table, where, resultOrder);
			statements[index] = SQLFactory.query(select).toSql(sqlDialect);
		}
		_statements = statements;
	}

	private List<SQLOrder> createOrder(String tableAlias) {
		if (_order.length == 0) {
			return SQLFactory.noOrder();
		}
		List<SQLOrder> orderSpecs = new ArrayList<>(_order.length);
		for (int i = 0; i < _order.length; i++) {
			DBAttribute order = _order[i];
			CollationHint collationHint;
			if (order.getSQLType().binaryParam) {
				// Comparison always binary for types supporting binary.
				collationHint = CollationHint.BINARY;
			} else {
				collationHint = CollationHint.NONE;
			}
			SQLExpression orderColumn = column(tableAlias, order.getDBName());
			orderSpecs.add(order(_descending[i], collationHint, orderColumn));
		}
		return orderSpecs;
	}

	/**
	 * Retrieves all rows from the given connection matching this {@link ItemQuery}.
	 */
	public ItemResult query(Connection connection) throws SQLException {
		ResultSet[] results = new ResultSet[_statements.length];
		for (int i = 0; i < _statements.length; i++) {
			results[i] = _statements[i].executeQuery(connection);
		}
		ResultSet result = ResultSetMerger.newMultipleResultSet(results, _order, _descending);
		return DirectItemResult.createDirectItemResult(_sqlDialect, result);
	}

	/**
	 * The SQL dialect using by this {@link ItemQuery}.
	 */
	public DBHelper getSqlDialect() {
		return _sqlDialect;
	}

	/**
	 * The type of the objects that are fetched by this {@link ItemQuery}.
	 */
	public MOClass getType() {
		return _type;
	}

}