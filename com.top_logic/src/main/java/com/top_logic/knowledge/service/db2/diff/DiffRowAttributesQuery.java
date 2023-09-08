/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.diff;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.db2.ItemQuery.DirectItemResult;
import com.top_logic.knowledge.service.db2.ItemResult;

/**
 * {@link AbstractDiffUpdateQuery} that reports changes in row attributes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DiffRowAttributesQuery extends AbstractDiffUpdateQuery {

	static final String[] ROW_ID_ATTRIBUTES = new String[] { BasicTypes.IDENTIFIER_DB_NAME };

	public static DiffRowAttributesQuery createDiffRowAttributesQuery(DBHelper sqlDialect, MOClass type, long sourceBranch, long sourceRev,
			long destBranch, long destRev) {
		final DiffRowAttributesQuery query = new DiffRowAttributesQuery(sqlDialect, type, sourceBranch, sourceRev, destBranch, destRev);
		query.init();
		return query;
	}

	private DiffRowAttributesQuery(DBHelper sqlDialect, MOClass type, long sourceBranch, long sourceRev, long destBranch, long destRev) {
		super(sqlDialect, type, sourceBranch, sourceRev, destBranch, destRev);
	}

	@Override
	protected String[] getIdColumnNames() {
		return ROW_ID_ATTRIBUTES;
	}

	@Override
	protected String getRevMaxColumnName() {
		return BasicTypes.REV_MAX_DB_NAME;
	}

	@Override
	protected String getRevMinColumnName() {
		return BasicTypes.REV_MIN_DB_NAME;
	}

	@Override
	protected String getBranchColumnName() {
		return BasicTypes.BRANCH_DB_NAME;
	}

	@Override
	public DiffUpdateResult query(Connection connection) throws SQLException {
		return new RowDiffUpdateResult(this.sqlDialect, type, executeStatement(connection));
	}
	
	public static class RowDiffUpdateResult extends DiffUpdateResult<ItemResult> {

		private MOAttributeImpl _revMaxAttribute;

		public RowDiffUpdateResult(DBHelper sqlDialect, MOClass type, ResultSet resultSet) throws SQLException {
			super(sqlDialect, type, resultSet);
			_revMaxAttribute = (MOAttributeImpl) type.getAttributeOrNull(BasicTypes.REV_MAX_ATTRIBUTE_NAME);
		}

		@Override
		public boolean isCreation() throws SQLException {
			return getOldValues().getLongValue(_revMaxAttribute) == 0;
		}

		@Override
		protected ItemResult createNewValues(DBHelper sqlDialect, MOClass type, ResultSet wrappedResult, int offset) {
			return DirectItemResult.createDirectItemResult(sqlDialect, wrappedResult, offset);
		}

		@Override
		protected ItemResult createOldValues(DBHelper sqlDialect, MOClass type, ResultSet wrappedResult, int offset) {
			return DirectItemResult.createDirectItemResult(sqlDialect, wrappedResult, offset);
		}
		
	}

}
