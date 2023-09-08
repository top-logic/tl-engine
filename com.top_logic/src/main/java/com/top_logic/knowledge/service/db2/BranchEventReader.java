/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.column;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLOrder;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.EventReader;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;

/**
 * {@link EventReader} that reports {@link Branch} creations as {@link BranchEvent}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BranchEventReader extends AbstractKnowledgeEventReader<BranchEvent> {

	private ItemResult branches;
	private DBAttribute _baseBranchAttr;
	private DBAttribute _baseRevAttr;
	private DBAttribute _branchIDAttr;
	private DBAttribute _createRevAttr;

	/**
	 * Creates a {@link BranchEventReader}.
	 * 
	 * @param kb
	 *        See {@link #getKnowledgeBase()}
	 * @param startRev
	 *        See {@link #getStartRev()}
	 * @param stopRev
	 *        See {@link #getStopRev()}
	 */
	public BranchEventReader(DBKnowledgeBase kb, long startRev, long stopRev) throws SQLException {
		super(kb, startRev, stopRev);
		
		boolean success = false;
		try {
			init();
			success = true;
		} finally {
			if (! success) {
				// Free potentially allocated resources, caller has no chance to
				// close reader, because the object construction fails.
				cleanup();
			}
		}
	}

	private void init() throws SQLException {
		initBranchQueue();
	}

	private void initBranchQueue() throws SQLException {
		String tableAlias = NO_TABLE_ALIAS;
		MOClass branchType = BasicTypes.getBranchType(kb);
		_baseBranchAttr = BranchSupport.getBaseBranchAttr(branchType);
		_baseRevAttr = BranchSupport.getBaseRevAttr(branchType);
		_branchIDAttr = BranchSupport.getBranchIDAttr(branchType);
		_createRevAttr = BranchSupport.getCreateRevAttr(branchType);
		SQLExpression branchFilter = attributeRange(tableAlias, _createRevAttr, startRev, stopRev);
		List<SQLOrder> branchOrder = orders(order(false, column(tableAlias, _createRevAttr, NOT_NULL)));
		
		ItemQuery branchQuery =
			new ItemQuery(kb.dbHelper, branchType, tableAlias, branchFilter, branchOrder);
		branches = branchQuery.query(getReadConnection());
	}

	@Override
	public BranchEvent readEvent() {
		try {
			return nextBranchEvent();
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	private BranchEvent nextBranchEvent() throws SQLException {
		if (branches.next()) {
			long nextBranchId = branches.getLongValue(_branchIDAttr);
			long nextCreateRev = branches.getLongValue(_createRevAttr);
			long nextBaseBranch = branches.getLongValue(_baseBranchAttr);
			long nextBaseRev = branches.getLongValue(_baseRevAttr);
			
			BranchEvent branchEvent = new BranchEvent(nextCreateRev, nextBranchId, nextBaseBranch, nextBaseRev);
			
			// Fill data branch by type.
			Set<String> branchedTypeNames = BranchImpl.readBranchedTypeNames(getReadConnection(), kb, nextBranchId);
			branchEvent.setBranchedTypeNames(branchedTypeNames);
			
			return branchEvent;
		} else {
			return null;
		}
	}

	@Override
	public void close() {
		try {
			try {
				super.close();
			} finally {
				cleanup();
			}
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}
	
	private void cleanup() throws SQLException {
		if (branches == null) {
			return;
		}
		ItemResult result = branches;
		branches = null;
		result.close();
	}
	
}
