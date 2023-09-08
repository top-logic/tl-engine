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
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.BasicTypes;

/**
 * {@link AbstractDiffDeletionQuery} that reports object deletions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DiffRowDeletionQuery extends AbstractDiffDeletionQuery {

	private static final String[] ROW_ID_ATTRIBUTES = DiffRowAttributesQuery.ROW_ID_ATTRIBUTES;
	
	public static DiffRowDeletionQuery createDiffRowDeletionQuery(DBHelper sqlDialect, MOClass type, long sourceBranch, long sourceRev,
			long destBranch, long destRev) {
		final DiffRowDeletionQuery query = new DiffRowDeletionQuery(sqlDialect, type, sourceBranch, sourceRev, destBranch, destRev);
		query.init();
		return query;
	}

	private DiffRowDeletionQuery(DBHelper sqlDialect, MOClass type, long sourceBranch, long sourceRev, long destBranch, long destRev) {
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
	public DiffRowDeletionResult query(Connection connection) throws SQLException {
		return new DiffRowDeletionResult(getSqlDialect(), executeStatement(connection), getType());
	}
	
	public static final class DiffRowDeletionResult extends DiffDeletionResult {

		private final String _typeName;

		DiffRowDeletionResult(DBHelper sqlDialect, ResultSet resultSet, MOClass type) {
			super(sqlDialect, resultSet, type, BasicTypes.BRANCH_ATTRIBUTE_NAME, BasicTypes.IDENTIFIER_ATTRIBUTE_NAME);
			_typeName = type.getName();
		}

		@Override
		public String getTypeName() throws SQLException {
			return _typeName;
		}

		/**
		 * Returns the value of the given {@link MOAttribute} in this result set (adapted to the
		 * given {@link ObjectContext}).
		 */
		public Object getValue(MOAttribute attribute, ObjectContext context) throws SQLException {
			return attribute.getStorage().fetchValue(_sqlDialect, resultSet, DBAttribute.DEFAULT_DB_OFFSET, attribute,
				context);
		}
	}

}
