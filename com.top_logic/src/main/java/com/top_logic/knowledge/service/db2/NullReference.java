/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.KnowledgeReferenceStorageImpl;

/**
 * The database representation of a {@link MOReference} that has no value.
 *
 * <p>
 * Reading a reference that holds these values answers nothing. A maintenance operation that removes
 * an object from the database writes them into every column of every reference that pointed to the
 * removed object, so that the referring object stays readable.
 * </p>
 *
 * <p>
 * The values are the ones {@link KnowledgeReferenceStorageImpl} writes for a reference without a
 * value: the identifier reserved for "no object", no type, and the
 * {@link KnowledgeReferenceStorageImpl#NULL_REPLACEMENT} marker in the branch and revision columns.
 * </p>
 *
 * @see IdentifierUtil#nullIdForMandatoryDatabaseColumns()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class NullReference {

	private final List<String> _columnNames;

	private final List<SQLExpression> _values;

	private NullReference(List<String> columnNames, List<SQLExpression> values) {
		_columnNames = Collections.unmodifiableList(columnNames);
		_values = Collections.unmodifiableList(values);
	}

	/**
	 * Creates the {@link NullReference} representation of the given reference.
	 *
	 * @param reference
	 *        The reference to clear.
	 */
	public static NullReference create(ItemTables.Reference reference) {
		List<String> columnNames = new ArrayList<>();
		List<SQLExpression> values = new ArrayList<>();

		DBAttribute idColumn = reference.getIdColumn();
		columnNames.add(idColumn.getDBName());
		values.add(literalID(IdentifierUtil.nullIdForMandatoryDatabaseColumns()));

		DBAttribute typeColumn = reference.getTypeColumn();
		if (typeColumn != null) {
			columnNames.add(typeColumn.getDBName());
			values.add(literalNull(typeColumn.getSQLType()));
		}

		DBAttribute revColumn = reference.getRevisionColumn();
		if (revColumn != null) {
			columnNames.add(revColumn.getDBName());
			values.add(literal(revColumn.getSQLType(), KnowledgeReferenceStorageImpl.NULL_REPLACEMENT));
		}

		DBAttribute branchColumn = reference.getBranchColumn();
		if (branchColumn != null) {
			columnNames.add(branchColumn.getDBName());
			values.add(literal(branchColumn.getSQLType(), KnowledgeReferenceStorageImpl.NULL_REPLACEMENT));
		}

		return new NullReference(columnNames, values);
	}

	/**
	 * The columns to assign, in the same order as {@link #getValues()}.
	 */
	public List<String> getColumnNames() {
		return _columnNames;
	}

	/**
	 * The values to assign to {@link #getColumnNames()}.
	 */
	public List<SQLExpression> getValues() {
		return _values;
	}

}
