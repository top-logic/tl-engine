/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.sequence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;

/**
 * Creates oracle sequences.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateOracleSequences {

	private final SequenceDefinition _tableDef;

	private final Protocol _protocol;

	/**
	 * Creates a {@link CreateOracleSequences}.
	 * 
	 * @param tableDef
	 *        definition of the table and the serial column to create sequence for.
	 * @param protocol
	 *        output to log messages.
	 */
	public CreateOracleSequences(SequenceDefinition tableDef, Protocol protocol) {
		_tableDef = tableDef;
		_protocol = protocol;
	}

	/**
	 * Creates the sequence for the given {@link SequenceDefinition}.
	 * 
	 * @param readCon
	 *        connection to read informations about the next value from.
	 * @param writeCon
	 *        connection to create the actual sequence
	 * 
	 * @throws SQLException
	 *         iff error occurs.
	 */
	public void create(DBHelper sqlDialect, PooledConnection readCon, PooledConnection writeCon) throws SQLException {
		int maxSerial = getMaximimumSerial(sqlDialect, readCon);
		createSequence(sqlDialect, writeCon, maxSerial);
	}

	private void createSequence(DBHelper sqlDialect, PooledConnection writeCon, int maxSerial) throws SQLException {
		Statement writeStatement = writeCon.createStatement();
		try {
			String createSeqStmt = createSeqStmt(sqlDialect, maxSerial);
			writeStatement.execute(createSeqStmt);
			_protocol.info("Created sequence for table " + _tableDef.table);
		} finally {
			writeStatement.close();
		}
	}

	private String createSeqStmt(DBHelper sqlDialect, int maxSerial) {
		String start = Integer.toString(maxSerial + 1);
		StringBuilder stmt = new StringBuilder();
		stmt.append("CREATE SEQUENCE SEQ_");
		stmt.append(sqlDialect.tableRef(_tableDef.table));
		stmt.append(" START WITH ");
		stmt.append(start);
		return stmt.toString();
	}

	private int getMaximimumSerial(DBHelper sqlDialect, PooledConnection readCon) throws SQLException {
		int maxSerial;
		Statement readStatement = readCon.createStatement();
		try {
			StringBuilder select = selectMaxStmt(sqlDialect);
			ResultSet result = readStatement.executeQuery(select.toString());
			try {
				if (result.next()) {
					maxSerial = result.getInt(1);
				} else {
					maxSerial = 0;
				}
			} finally {
				result.close();
			}
		} finally {
			readStatement.close();
		}
		return maxSerial;
	}

	private StringBuilder selectMaxStmt(DBHelper sqlDialect) {
		StringBuilder select = new StringBuilder();
		select.append("SELECT max(");
		select.append(sqlDialect.columnRef(_tableDef.serialColumn));
		select.append(") from ");
		select.append(sqlDialect.tableRef(_tableDef.table));
		return select;
	}

}
