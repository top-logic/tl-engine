/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model.visit;

import java.util.Collection;

import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBForeignKey;
import com.top_logic.basic.db.model.DBIndex;
import com.top_logic.basic.db.model.DBNamedPart;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaPart;
import com.top_logic.basic.db.model.DBSchemaVisitor;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.DBTablePart;

/**
 * Default adaptor implementation of {@link DBSchemaVisitor}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultDBSchemaVisitor<R, A> implements DBSchemaVisitor<R, A> {

	@Override
	public R visitColumn(DBColumn model, A arg) {
		return visitTablePart(model, arg);
	}

	@Override
	public R visitForeignKey(DBForeignKey model, A arg) {
		return visitTablePart(model, arg);
	}

	@Override
	public R visitIndex(DBIndex model, A arg) {
		return visitTablePart(model, arg);
	}

	@Override
	public R visitSchema(DBSchema model, A arg) {
		return visitSchemaPart(model, arg);
	}

	@Override
	public R visitTable(DBTable model, A arg) {
		return visitNamedPart(model, arg);
	}

	protected R visitTablePart(DBTablePart model, A arg) {
		return visitNamedPart(model, arg);
	}

	protected R visitNamedPart(DBNamedPart model, A arg) {
		return visitSchemaPart(model, arg);
	}

	protected R visitSchemaPart(DBSchemaPart model, A arg) {
		return null;
	}

	protected void descend(Collection<? extends DBSchemaPart> parts, A arg) {
		for (DBSchemaPart part : parts) {
			descend(part, arg);
		}
	}

	protected void descendOptional(DBSchemaPart part, A arg) {
		if (part != null) {
			descend(part, arg);
		}
	}
	
	protected void descend(DBSchemaPart part, A arg) {
		part.visit(this, arg);
	}

}
