/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.diff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.util.TLContext;

/**
 * Base class for difference queries.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AbstractDiffQuery {

	final DBHelper sqlDialect;
	
	protected final MOClass type;
	
	protected final long sourceBranch;

	protected final long sourceRev;

	protected final long destBranch;

	protected final long destRev;

	/**
	 * Creates a {@link AbstractDiffQuery}.
	 *
	 * @param sqlDialect
	 *        See {@link #getSqlDialect()}
	 * @param type
	 *        See {@link #getType()}
	 * @param sourceBranch
	 *        See {@link #getSourceBranch()}
	 * @param sourceRev
	 *        See {@link #getSourceRev()}
	 * @param destBranch
	 *        See {@link #getDestBranch()}
	 * @param destRev
	 *        See {@link #getDestRev()}
	 */
	public AbstractDiffQuery(DBHelper sqlDialect, MOClass type, long sourceBranch, long sourceRev, long destBranch, long destRev) {
		this.type = type;
		if (!multipleBranches()) {
			checkBranchArgument("Source branch", sourceBranch);
			checkBranchArgument("Destination branch", destBranch);
		}
		this.sqlDialect = sqlDialect;
		this.sourceBranch = sourceBranch;
		this.sourceRev = sourceRev;
		this.destBranch = destBranch;
		this.destRev = destRev;
	}

	private static void checkBranchArgument(String branchName, long branchId) {
		if (branchId != TLContext.TRUNK_ID) {
			StringBuilder msg = new StringBuilder();
			msg.append(branchName);
			msg.append(" must be ");
			msg.append(TLContext.TRUNK_ID);
			msg.append(" as no branch column exists. Was: ");
			msg.append(branchId);
			throw new IllegalArgumentException(msg.toString());
		}
	}

	/**
	 * Whether the type supports multiple branches.
	 */
	protected boolean multipleBranches() {
		return this.type.getDBMapping().multipleBranches();
	}

	/**
	 * The SQL dialect, in which this query is constructed.
	 */
	public DBHelper getSqlDialect() {
		return sqlDialect;
	}
	
	/**
	 * The concrete type from which items are read.
	 */
	public MOClass getType() {
		return type;
	}
	
	/**
	 * The stating branch of the difference.
	 */
	public long getSourceBranch() {
		return sourceBranch;
	}
	
	/**
	 * The stating revision of the difference.
	 */
	public long getSourceRev() {
		return sourceRev;
	}
	
	/**
	 * The ending branch of the difference.
	 */
	public long getDestBranch() {
		return destBranch;
	}
	
	/**
	 * The ending revision of the difference.
	 */
	public long getDestRev() {
		return destRev;
	}

	/**
	 * Creates a mapping from the {@link DBAttribute#getDBName() database name} of the
	 * {@link DBAttribute} of {@link #type} to the attribute itself.
	 */
	protected Map<String, DBAttribute> newDBAttributeMapping() {
		List<DBAttribute> mapping = type.getDBMapping().getDBAttributes();
		Map<String, DBAttribute> attrByDBName = new HashMap<>();
		for (DBAttribute dbAttr : mapping) {
			attrByDBName.put(dbAttr.getDBName(), dbAttr);
		}
		return attrByDBName;
	}

}
