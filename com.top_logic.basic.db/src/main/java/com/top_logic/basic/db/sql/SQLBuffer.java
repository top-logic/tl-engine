/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Map;

import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;

/**
 * {@link SimpleSQLBuffer} that can insert placeholder into the source string to be able to create
 * sources for {@link PreparedStatement} that are filled later with the actual values.
 * 
 *          com.top_logic.knowledge.service.db2.sql.StatementBuilder.SQLBuffer
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class SQLBuffer extends SimpleSQLBuffer {
	
	private final Map<String, Integer> argumentIndexByName;
	
	/** result in {@link DefaultCompiledStatementImpl#_constants} */
	private final ArrayList<Object> constants = new ArrayList<>();

	/** result in {@link DefaultCompiledStatementImpl#types} */
	private final ArrayList<DBType> types = new ArrayList<>();

	/** result in {@link DefaultCompiledStatementImpl#_conversions} */
	private final ArrayList<Conversion> conversions = new ArrayList<>();

	/** result in {@link DefaultCompiledStatementImpl#_parameters} */
	private final ArrayList<Integer> parameters = new ArrayList<>();
	
	/** @see #context() */
	private SQLPart _context;

	/**
	 * Creates a {@link SQLBuffer}.
	 *
	 * @param sqlDialect The SQL dialect in which the statement is constructed.
	 */
	public SQLBuffer(DBHelper sqlDialect, Map<String, Integer> argumentIndexByName) {
		super(sqlDialect);
		this.argumentIndexByName = argumentIndexByName;
	}

	public void appendParameter(DBType jdbcType, Conversion conversion, String name) {
		appendParameter(parameters.size());
		
		Integer argumentIndex = argumentIndexByName.get(name);
		if (argumentIndex == null) {
			throw new IllegalArgumentException("Undeclared parameter: " + name);
		}
		conversions.add(conversion);
		parameters.add(argumentIndex);
		types.add(jdbcType);
	}

	public void appendConstant(DBType jdbcType, Object value) {
		appendParameter(parameters.size());
		
		int constantIndex = -1 - constants.size();
		conversions.add(Conversion.IDENTITY);
		parameters.add(constantIndex);
		types.add(jdbcType);
		constants.add(value);
	}
	
	/**
	 * Adds a '?' to the buffer.
	 * 
	 * @param parameterIndex
	 *        the index of this parameter in the list of parameters
	 */
	private void appendParameter(int parameterIndex) {
		append('?');
	}

	@Override
	SQLPart context() {
		return _context;
	}

	@Override
	void setContext(SQLPart context) {
		this._context = context;
	}

	/**
	 * Creates a {@link CompiledStatement} from the data collected with this buffer.
	 */
	public CompiledStatement createStatement() {
		String source = buffer.toString();
		if (parameters.isEmpty()) {
			return new SimpleCompiledStatement(sqlDialect, source);
		}
		int[] indexArray = new int[parameters.size()];
		DBType[] typeArray = new DBType[parameters.size()];
		for (int n = 0, cnt = parameters.size(); n < cnt; n++) {
			Integer index = parameters.get(n);
			indexArray[n] = index;
			
			DBType type = types.get(n);
			typeArray[n] = type;
		}
		Object[] constantArray = constants.toArray();
		return new DefaultCompiledStatementImpl(sqlDialect, typeArray, source, conversions,
			indexArray, argumentIndexByName, constantArray);
	}

}
