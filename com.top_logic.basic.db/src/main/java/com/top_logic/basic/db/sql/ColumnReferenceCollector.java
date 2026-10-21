/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Visitor collecting all {@link SQLColumnReference}s of an {@link SQLPart}, including those in
 * nested expressions and sub-selects.
 * 
 * <p>
 * The visited {@link SQLPart} is left unchanged.
 * </p>
 * 
 * @see #columnReferences(SQLPart)
 */
public class ColumnReferenceCollector extends SQLInlineTransformation<Collection<? super SQLColumnReference>> {

	/** Singleton {@link ColumnReferenceCollector} instance. */
	public static final ColumnReferenceCollector INSTANCE = new ColumnReferenceCollector();

	/**
	 * Creates a {@link ColumnReferenceCollector}.
	 * 
	 * @see #INSTANCE
	 */
	protected ColumnReferenceCollector() {
		// Singleton constructor.
	}

	/**
	 * All {@link SQLColumnReference}s in the given part in visit order.
	 */
	public static List<SQLColumnReference> columnReferences(SQLPart part) {
		List<SQLColumnReference> result = new ArrayList<>();
		collect(part, result);
		return result;
	}

	/**
	 * Adds all {@link SQLColumnReference}s in the given part to the given collection.
	 */
	public static void collect(SQLPart part, Collection<? super SQLColumnReference> result) {
		part.visit(INSTANCE, result);
	}

	@Override
	public SQLPart visitSQLColumnReference(SQLColumnReference sql, Collection<? super SQLColumnReference> arg) {
		arg.add(sql);
		return sql;
	}

}
