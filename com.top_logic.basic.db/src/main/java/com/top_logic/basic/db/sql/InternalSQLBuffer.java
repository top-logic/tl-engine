/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.Collection;
import java.util.Map;

import com.top_logic.basic.sql.DBHelper;

/**
 * {@link SimpleSQLBuffer} internally used by {@link DirectStatementBuilder}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class InternalSQLBuffer extends SimpleSQLBuffer implements SetParameterResolver {

	private final Map<String, Integer> _argumentIndexByName;

	private final Object[] _arguments;

	public InternalSQLBuffer(DBHelper sqlDialect, Map<String, Integer> argumentIndexByName, Object[] arguments) {
		super(sqlDialect);
		_argumentIndexByName = argumentIndexByName;
		_arguments = arguments;
	}

	public Map<String, Integer> getArgumentIndexByName() {
		return _argumentIndexByName;
	}

	public Object[] getArguments() {
		return _arguments;
	}

	/**
	 * Check, whether a value is assigned to the given parameter name.
	 */
	boolean hasArgument(String parameterName) {
		return index(parameterName) < _arguments.length;
	}

	Object getArgument(String parameterName) {
		return _arguments[index(parameterName)];
	}

	private int index(String argumentName) {
		Integer index = _argumentIndexByName.get(argumentName);
		if (index == null) {
			throw new NullPointerException("Unknown parameter '" + argumentName + "'");
		}
		return index.intValue();
	}

	@Override
	SQLPart context() {
		return null;
	}

	@Override
	void setContext(SQLPart context) {
		// no context necessary
	}

	@Override
	public SQLExpression fillSetParameter(SQLSetParameter setParam) {
		String paramName = setParam.getName();
		if (hasArgument(paramName)) {
			Object converted = getArgumentConverted(setParam);
			SQLExpression values;
			if (converted instanceof Collection<?>) {
				values = SQLFactory.setLiteral((Collection<?>) converted, setParam.getTypes());
			} else if (converted instanceof SQLExpression){
				values = (SQLExpression) converted;
			} else {
				throw new IllegalArgumentException(
					"Expected Collection or SQLExpression as value for a set parameter.");
			}
			return values;
		} else {
			return setParam;
		}
	}

	final Object getArgumentConverted(AbstractSQLParameter sql) {
		String paramName = sql.getName();
		Object argument = getArgument(paramName);
		Conversion conversion = sql.getConversion();
		return conversion.convert(argument, getArgumentIndexByName(), getArguments());
	}

}

