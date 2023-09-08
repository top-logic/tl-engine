/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.exec;

import java.util.List;

import com.top_logic.basic.col.LongRange;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.sql.ResultSetReader;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;

/**
 * Composite of utilities for a search through history.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HistorySearch {

	private final SQLSelect select;
	private final ResultSetReader<ObjectBranchId> objectIdReader;
	private final ResultSetReader<List<LongRange>> lifePeriodReader;

	public HistorySearch(SQLSelect select, ResultSetReader<ObjectBranchId> objectIdReader, ResultSetReader<List<LongRange>> lifePeriodReader) {
		this.select = select;
		this.objectIdReader = objectIdReader;
		this.lifePeriodReader = lifePeriodReader;
	}
	
	public SQLSelect getSelect() {
		return select;
	}
	
	public ResultSetReader<List<LongRange>> getLifePeriodReader() {
		return lifePeriodReader;
	}
	
	public ResultSetReader<ObjectBranchId> getObjectIdReader() {
		return objectIdReader;
	}

}
