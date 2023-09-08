/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.sym;

import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.service.HistoryUtils;

/**
 * {@link Symbol}s representing links or objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ItemSymbol extends DataSymbol {
	MOClass getType();
	
	/**
	 * Dereferences this {@link Symbol} to the concrete {@link Symbol}. E.g. if this symbol
	 * represents the definition of an untyped reference valued attribute the resulting
	 * {@link TableSymbol} with concrete type <code>A</code> represents the table in which values of
	 * the reference with type <code>A</code> are stored.
	 */
	TableSymbol dereference(MOClass concreteType);
	
	FlexAttributeSymbol getFlexSymbol(QueryPart definition, TypeSystem typeSystem, MetaObject type, String attributeName);
	
	String getTableAlias();

	/**
	 * Creates an expression for the revision of the represented item.
	 * 
	 * <p>
	 * In contrast to {@link #createHistoryContextExpr()}, this method returns the history of the
	 * request when the object represented by this item is a
	 * {@link HistoryUtils#isCurrent(ObjectKey) current} item.
	 * </p>
	 * 
	 * @see #createHistoryContextExpr()
	 */
	SQLExpression createRevisionExpr();

	/**
	 * Creates an expression for the history context of the represented item.
	 * 
	 * @see #createRevisionExpr()
	 */
	SQLExpression createHistoryContextExpr();

}