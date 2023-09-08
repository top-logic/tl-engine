/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model;

/**
 * Visitor interface for the {@link DBSchemaPart} hierarchy.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DBSchemaVisitor<R, A> {

	/**
	 * Visit case for {@link DBSchema}.
	 */
	R visitSchema(DBSchema model, A arg);

	/**
	 * Visit case for {@link DBTable}.
	 */
	R visitTable(DBTable model, A arg);

	/**
	 * Visit case for {@link DBColumn}.
	 */
	R visitColumn(DBColumn model, A arg);
	
	/**
	 * Visit case for {@link DBIndex}.
	 */
	R visitIndex(DBIndex model, A arg);
	
	/**
	 * Visit case for {@link DBForeignKey}.
	 */
	R visitForeignKey(DBForeignKey model, A arg);
	
}
