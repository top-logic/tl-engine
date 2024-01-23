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
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager.AttributeItemQuery.AttributeItemResult;
import com.top_logic.knowledge.service.db2.ItemQuery.DirectItemResult;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;

/**
 * {@link AbstractDiffUpdateQuery} that reports changes in flex attributes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DiffFlexAttributesQuery extends AbstractDiffUpdateQuery {

	static final String[] FLEX_ID_ATTRIBUTES = new String[] {
		AbstractFlexDataManager.TYPE_DBNAME,
		AbstractFlexDataManager.IDENTIFIER_DBNAME,
		AbstractFlexDataManager.ATTRIBUTE_DBNAME
	};

	public static DiffFlexAttributesQuery createDiffFlexAttributesQuery(DBHelper sqlDialect,
			MOKnowledgeItemImpl flexDataType, String type, long sourceBranch, long sourceRev,
			long destBranch, long destRev) {
		final DiffFlexAttributesQuery query =
			new DiffFlexAttributesQuery(sqlDialect, flexDataType, type, sourceBranch, sourceRev, destBranch, destRev);
		query.init();
		return query;
	}

	private final String elementType;

	private final MOKnowledgeItemImpl flexDataType;

	private DiffFlexAttributesQuery(DBHelper sqlDialect, MOKnowledgeItemImpl flexDataType, String type, long sourceBranch,
			long sourceRev, long destBranch, long destRev) {
		super(sqlDialect, flexDataType, sourceBranch, sourceRev, destBranch, destRev);
		this.elementType = type;
		this.flexDataType = flexDataType;
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
	public DiffFlexUpdateResult query(Connection connection) throws SQLException {
		return new DiffFlexUpdateResult(this.sqlDialect, flexDataType, type, executeStatement(connection));
	}
	
	public static class DiffFlexUpdateResult extends DiffUpdateResult<AttributeItemResult> {
		
		private MOAttributeImpl _revMaxAttribute;

		public DiffFlexUpdateResult(DBHelper sqlDialect, MOKnowledgeItemImpl flexData, MOClass type,
				ResultSet resultSet) throws SQLException {
			super(sqlDialect, type, resultSet);
			_revMaxAttribute = AbstractFlexDataManager.getAttribute(flexData, AbstractFlexDataManager.REV_MAX);
		}
		
		@Override
		public boolean isCreation() throws SQLException {
			return getOldValues().getLongValue(_revMaxAttribute) == 0;
		}

		@Override
		protected AttributeItemResult createNewValues(DBHelper sqlDialect, MOClass type, ResultSet wrappedResult, int offset) {
			return new AttributeItemResult(type, DirectItemResult.createDirectItemResult(sqlDialect, wrappedResult,
				offset));
		}


		@Override
		protected AttributeItemResult createOldValues(DBHelper sqlDialect, MOClass type, ResultSet wrappedResult, int offset) {
			return new AttributeItemResult(type, DirectItemResult.createDirectItemResult(sqlDialect, wrappedResult,
				offset)) {
				
				/**
				 * It may be that the result set is a result for an old value so all values in the
				 * given ResultSet may be <code>null</code>. If that is the case the
				 * {@link AttributeItemResult} won't work as the
				 * {@link AbstractFlexDataManager#DATA_TYPE type} column is mandatory. In that case
				 * the value is always <code>null</code>
				 * 
				 * @see AttributeItemResult#getAttributeValue()
				 */
				@Override
				public Object getAttributeValue() throws SQLException {
					if (getRevMax() == 0) {
						return null;
					} else {
						return super.getAttributeValue();
					}
				}
			};
		}
	}
}
