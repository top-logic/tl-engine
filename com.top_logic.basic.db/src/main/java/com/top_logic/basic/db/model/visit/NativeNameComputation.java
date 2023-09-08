/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model.visit;

import java.io.UnsupportedEncodingException;
import java.util.zip.CRC32;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.db.model.DBNamedPart;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaPart;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.sql.DBHelper;

/**
 * Trim names of {@link DBSchemaPart}s to fit requirements of specific DBMSs.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NativeNameComputation extends DefaultDBSchemaVisitor<Void, Void> {
	private final DBHelper _sqlDialect;

	/**
	 * Creates a {@link NativeNameComputation} for the given SQL dialect.
	 */
	public NativeNameComputation(DBHelper sqlDialect) {
		_sqlDialect = sqlDialect;
	}

	@Override
	public Void visitSchema(DBSchema model, Void arg) {
		descend(model.getTables(), arg);
		return super.visitSchema(model, arg);
	}

	@Override
	public Void visitTable(DBTable model, Void arg) {
		descend(model.getColumns(), arg);
		descend(model.getForeignKeys(), arg);
		descend(model.getIndices(), arg);
		return super.visitTable(model, arg);
	}

	@Override
	protected Void visitNamedPart(DBNamedPart model, Void arg) {
		buildNativeName(model);
		return super.visitNamedPart(model, arg);
	}

	private void buildNativeName(DBNamedPart model) {
		model.setExplicitDBName(nativeName(model.getName()));
	}

	/**
	 * Computes DBMS specific name for the given logical name.
	 */
	protected String nativeName(String name) {
		int nameLength = name.length();
		int maxNameLength = _sqlDialect.getMaxNameLength();
		if (nameLength > maxNameLength) {
			// Make names short by replacing name postfix with deterministic hash code.
			CRC32 crc32 = new CRC32();
			try {
				crc32.update(name.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new UnreachableAssertion(e);
			}
			long hashCode = crc32.getValue();
			int suffixLength = 9;
			return name.substring(0, maxNameLength - suffixLength) + "_"
				+ Long.toHexString(hashCode).toUpperCase();
		} else {
			return name;
		}
	}
}