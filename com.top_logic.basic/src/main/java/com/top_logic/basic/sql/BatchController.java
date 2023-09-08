/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Controller for batch processing with {@link PreparedStatement}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BatchController {
	private final long batchSize;
	private final PreparedStatement stmt;

	/**
	 * @see #getDataRows()
	 */
	private long dataRows = 0;
	
	/**
	 * The number of pending batch operations.
	 */
	private int batchRows = 0;

	/**
	 * Creates a {@link BatchController}.
	 * 
	 * @param stmt
	 *        The {@link PreparedStatement} whose batch mode is controlled by
	 *        this instance.
	 * @param batchSize
	 *        The batch size.
	 */
	public BatchController(PreparedStatement stmt, long batchSize) {
		this.stmt = stmt;
		this.batchSize = batchSize;
	}

	/**
	 * Should be called instead of {@link PreparedStatement#addBatch()} on the
	 * {@link PreparedStatement} controlled by this {@link BatchController}.
	 * 
	 * @throws SQLException
	 *         If adding the batch fails.
	 */
	public void addBatch() throws SQLException {
		stmt.addBatch();
		batchRows++;
		if (batchRows > batchSize) {
			dataRows += batchRows;
			batchRows = 0;
			
			stmt.executeBatch();
		}
	}

	/**
	 * Should be called instead of {@link PreparedStatement#executeBatch()} on
	 * the {@link PreparedStatement} controlled by this {@link BatchController}.
	 * 
	 * @throws SQLException
	 *         If executing the batch failed.
	 */
	public void finishBatch() throws SQLException {
		if (batchRows > 0) {
			dataRows += batchRows;
			batchRows = 0;
			
			stmt.executeBatch();
		}
	}

	/**
	 * The number of batch operations already executed.
	 */
	public long getDataRows() {
		return dataRows;
	}

}