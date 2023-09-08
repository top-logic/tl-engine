/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.ResultSetReader;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;

/**
 * {@link ResultSetReader} that reads <code>branch</code>/<code>type</code>/<code>objectName</code>
 * columns into {@link ObjectBranchId}s.
 * 
 *          com.top_logic.knowledge.service.db2.expr.transform.sql.SQLBuilder
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class ObjectIdReader implements ResultSetReader<ObjectBranchId> {

	static final long NO_BRANCH = Long.MIN_VALUE;

	static final String NO_TYPE = null;

	private final TypeSystem typeSystem;
	private final int objectIdColumnOffset;

	private ResultSet resultSet;

	private final String _typeName;

	private final long _branch;

	/**
	 * Creates a new {@link ObjectIdReader} with an already known type or branch.
	 * 
	 * @param typeName
	 *        The type of the {@link ObjectBranchId}.
	 * @param branch
	 *        The branch of the object.
	 */
	public ObjectIdReader(TypeSystem typeSystem, int objectIdColumnOffset, String typeName, long branch) {
		this.typeSystem = typeSystem;
		this.objectIdColumnOffset = objectIdColumnOffset;
		_typeName = typeName;
		_branch = branch;
	}

	public ObjectIdReader(TypeSystem typeSystem, int objectIdColumnOffset) {
		this(typeSystem, objectIdColumnOffset, NO_TYPE, NO_BRANCH);
	}

	@Override
	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	@Override
	public ObjectBranchId read() {
		try {
			int offset = objectIdColumnOffset + 1;
			long branchId;
			if (_branch == NO_BRANCH) {
				branchId = resultSet.getLong(offset++);
			} else {
				branchId = _branch;
			}
			String typeName;
			if (_typeName == NO_TYPE) {
				typeName = resultSet.getString(offset++);
			} else {
				typeName = _typeName;
			}
			TLID objectName = IdentifierUtil.getId(resultSet, offset++);
			
			return new ObjectBranchId(branchId, typeSystem.getType(typeName), objectName);
		} catch (UnknownTypeException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}
}
