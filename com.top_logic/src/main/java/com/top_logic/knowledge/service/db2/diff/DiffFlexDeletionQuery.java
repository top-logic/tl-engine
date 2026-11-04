/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.diff;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.DBBinaryData;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager.AttributeResult;
import com.top_logic.knowledge.service.db2.ItemQuery.DirectItemResult;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;

/**
 * {@link AbstractDiffDeletionQuery} that reports deletions of flex attributes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DiffFlexDeletionQuery extends AbstractDiffDeletionQuery {

	static final String[] FLEX_ID_ATTRIBUTES = DiffFlexAttributesQuery.FLEX_ID_ATTRIBUTES;
	
	private final String elementType;

	private DiffFlexDeletionQuery(DBHelper sqlDialect, MOKnowledgeItemImpl flexDataType, String type, long sourceBranch,
			long sourceRev, long destBranch, long destRev) {
		super(sqlDialect, flexDataType, sourceBranch, sourceRev, destBranch, destRev);
		this.elementType = type;
	}

	/**
	 * Creates a query for the flexible attribute values of objects of the given type that exist in
	 * the given source but not in the destination.
	 *
	 * @param sqlDialect
	 *        The SQL dialect of the database.
	 * @param flexDataType
	 *        The type of the flex data table.
	 * @param type
	 *        The name of the object type whose flex attributes are compared.
	 * @param sourceBranch
	 *        The branch to compare.
	 * @param sourceRev
	 *        The revision to compare in the source branch.
	 * @param destBranch
	 *        The branch to compare with.
	 * @param destRev
	 *        The revision to compare with in the destination branch.
	 * @return The new, initialized query.
	 */
	public static DiffFlexDeletionQuery createDiffFlexDeletionQuery(DBHelper sqlDialect, MOKnowledgeItemImpl flexDataType,
			String type, long sourceBranch, long sourceRev,
			long destBranch, long destRev) {
		final DiffFlexDeletionQuery query =
			new DiffFlexDeletionQuery(sqlDialect, flexDataType, type, sourceBranch, sourceRev, destBranch, destRev);
		query.init();
		return query;
	}
	
	@Override
	protected String[] getIdColumnNames() {
		return FLEX_ID_ATTRIBUTES;
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
		return AbstractFlexDataManager.BRANCH_DBNAME;
	}
	
	@Override
	protected SQLExpression restrictResult(String tableAlias) {
		if (elementType == null) {
			return super.restrictResult(tableAlias);
		}
		return eq(notNullColumn(tableAlias, AbstractFlexDataManager.TYPE_DBNAME), literalString(elementType));
	}
	
	@Override
	public DiffFlexDeletionResult query(Connection connection) throws SQLException {
		return new DiffFlexDeletionResult(getSqlDialect(), type, executeStatement(connection));
	}
	
	/**
	 * {@link DiffDeletionResult} of a {@link DiffFlexDeletionQuery}.
	 */
	public static class DiffFlexDeletionResult extends DiffDeletionResult implements AttributeResult {

		private final MOClass _flexDataType;

		DiffFlexDeletionResult(DBHelper sqlDialect, MOClass flexDataType, ResultSet resultSet) {
			super(sqlDialect, resultSet, flexDataType, AbstractFlexDataManager.BRANCH,
				AbstractFlexDataManager.IDENTIFIER);
			_flexDataType = flexDataType;
		}

		/**
		 * The value of the flexible attribute in the current row.
		 */
		public Object getValue() throws SQLException {
			return AbstractFlexDataManager.fetchValue(this);
		}

		private int columnIndex(String attribute) {
			return _flexDataType.getAttributeOrNull(attribute).getDbMapping()[0].getDBColumnIndex() + 1;
		}

		@Override
		public long getRevMin() throws SQLException {
			return this.resultSet.getLong(columnIndex(AbstractFlexDataManager.REV_MIN));
		}

		@Override
		public String getTypeName() throws SQLException {
			return this.resultSet.getString(columnIndex(AbstractFlexDataManager.TYPE));
		}

		@Override
		public String getAttributeName() throws SQLException {
			return this.resultSet.getString(columnIndex(AbstractFlexDataManager.ATTRIBUTE));
		}
		
		@Override
		public byte getDataType() throws SQLException {
			return this.resultSet.getByte(columnIndex(AbstractFlexDataManager.DATA_TYPE));
		}
		
		@Override
		public long getLongData() throws SQLException {
			return this.resultSet.getLong(columnIndex(AbstractFlexDataManager.LONG_DATA));
		}
		
		@Override
		public double getDoubleData() throws SQLException {
			return this.resultSet.getDouble(columnIndex(AbstractFlexDataManager.DOUBLE_DATA));
		}
		
		@Override
		public String getVarcharData() throws SQLException {
			return this.resultSet.getString(columnIndex(AbstractFlexDataManager.VARCHAR_DATA));
		}
		
		@Override
		public String getClobData() throws SQLException {
			int clobIndex = columnIndex(AbstractFlexDataManager.CLOB_DATA);
			return DirectItemResult.getClobStringValue(_sqlDialect, resultSet, clobIndex);
		}
		
		@Override
		public BinaryData getBlobData() throws SQLException {
			int contentTypeIndex = columnIndex(AbstractFlexDataManager.VARCHAR_DATA);
			int sizeIndex = columnIndex(AbstractFlexDataManager.LONG_DATA);
			int blobIndex = columnIndex(AbstractFlexDataManager.BLOB_DATA);
			return DBBinaryData.fromBlobColumn(_sqlDialect, resultSet, getClobData(), contentTypeIndex, sizeIndex,
				blobIndex);
		}

	}

}
