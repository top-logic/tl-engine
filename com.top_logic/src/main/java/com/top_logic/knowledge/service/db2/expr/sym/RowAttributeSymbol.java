/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.sym;

import static com.top_logic.dob.sql.SQLFactory.*;

import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.search.Attribute;

/**
 * {@link Symbol} for an access to a row attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RowAttributeSymbol extends AttributeSymbol {
	private final TableSymbol contextSymbol;

	public RowAttributeSymbol(TableSymbol contextSymbol, Attribute attribute) {
		super(attribute);
		this.contextSymbol = contextSymbol;
	}
	
	@Override
	public String getAttributeName() {
		return getDefinition().getAttribute().getName();
	}

	@Override
	public Attribute getDefinition() {
		return (Attribute) super.getDefinition();
	}
	
	@Override
	public TableSymbol getParent() {
		return contextSymbol;
	}
	
	@Override
	public String getDataTableAlias() {
		return contextSymbol.getTableAlias();
	}
	
	public MOAttribute getAttribute() {
		return getDefinition().getAttribute();
	}
	
	@Override
	public <R, A> R visit(SymbolVisitor<R,A> v, A arg) {
		return v.visitRowAttributeSymbol(this, arg);
	}

	@Override
	public boolean hasOwnLifetime() {
		return false;
	}

	@Override
	public SQLExpression createRevMaxExpr() {
		return getParent().createRevMaxExpr();
	}

	@Override
	public SQLExpression createRevMinExpr() {
		return getParent().createRevMinExpr();
	}

	@Override
	public SQLExpression createBranchExpr() {
		return getParent().createBranchExpr();
	}

	@Override
	public SQLExpression createTypeExpr() {
		return getParent().createTypeExpr();
	}

	@Override
	public SQLExpression createIdentifierExpr() {
		return getParent().createIdentifierExpr();
	}

	@Override
	public boolean isPotentiallyNull() {
		return getParent().isPotentiallyNull();
	}

	@Override
	public void setPotentiallyNull(boolean potentiallyNull) {
		getParent().setPotentiallyNull(potentiallyNull);
	}

	/**
	 * Creates the column represented by the given {@link RowAttributeSymbol}.
	 */
	public static SQLExpression createColumn(RowAttributeSymbol sym) {
		MOAttribute attribute = sym.getAttribute();
		DBAttribute dbAttr = (DBAttribute) attribute;
		boolean notNull = !sym.getParent().isPotentiallyNull() && dbAttr.isSQLNotNull();
		return column(sym.getDataTableAlias(), dbAttr, notNull);
	}
}
