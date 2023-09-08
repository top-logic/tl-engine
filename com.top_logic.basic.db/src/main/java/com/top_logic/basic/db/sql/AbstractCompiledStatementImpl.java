/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.sql.DBHelper;

/**
 * {@link PrepStmtBasedCompiledStatement} that uses parameters and constants in the prepared
 * statement.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class AbstractCompiledStatementImpl extends PrepStmtBasedCompiledStatement {

	protected final List<Conversion> _conversions;

	protected final int[] _parameters;

	protected final Object[] _constants;

	private final Map<String, Integer> _argumentIndexByName;

	/**
	 * Creates a {@link AbstractCompiledStatementImpl}.
	 * 
	 * @param sqlDialect
	 *        The SQL dialect of the used database.
	 * @param conversions
	 *        list of conversions to convert application arguments to SQL arguments.
	 * @param parameters
	 *        Mapping of parameter and constants to index in argument array, i.e. the value for the
	 *        <code>n^th</code> parameter is found at index <code>parameter[n]</code> in the
	 *        argument array if <code>&gt;=0</code> or at index <code>-1 - parameter[n]</code> in
	 *        the constant array if <code>&lt;0</code>. See {@link #getArgument(Object[], int)}.
	 * @param argumentIndexByName
	 *        See {@link Conversion#convert(Object, Map, Object[])}.
	 * @param constants
	 *        array containing the constants that are contained in the query.
	 */
	AbstractCompiledStatementImpl(DBHelper sqlDialect, List<Conversion> conversions, int[] parameters,
			Map<String, Integer> argumentIndexByName, Object[] constants) {
		super(sqlDialect);
		_conversions = conversions;
		_parameters = parameters;
		_argumentIndexByName = argumentIndexByName;
		_constants = constants;
	}

	/**
	 * Extracts the value for the n^th parameter from the given arguments
	 */
	protected Object getArgument(Object[] arguments, int n) {
		int argumentIndex = _parameters[n];

		Object value;
		if (argumentIndex >= 0) {
			value = _conversions.get(n).convert(arguments[argumentIndex], _argumentIndexByName, arguments);
		} else {
			int constantIndex = -1 - argumentIndex;
			value = _constants[constantIndex];
		}
		return value;
	}

}

