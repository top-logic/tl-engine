/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.sym;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.knowledge.service.db2.expr.transform.sql.DBSQLFactory.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.search.Attribute;
import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.search.Reference;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.DBAccess;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;

/**
 * {@link Symbol} for accessing a database table row.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableSymbol extends AbstractItemSymbol {
	private final MOClass type;

	private final Map<QueryPart, Symbol> attributeSymbols = new HashMap<>();

	private Symbol parentSymbol;
	private String tableAlias;

	/** @see #isPotentiallyNull() */
	private boolean _potentiallyNull;

	public TableSymbol(QueryPart definition, MOClass type) {
		super(definition);
		this.parentSymbol = null;
		this.type = type;
	}
	
	public TableSymbol(ReferenceSymbol parentSymbol, MOClass type) {
		super(null);
		this.parentSymbol = parentSymbol;
		this._potentiallyNull = parentSymbol.isPotentiallyNull();
		this.type = type;
	}

	@Override
	public Symbol getParent() {
		return parentSymbol;
	}
	
	@Override
	public void initParent(TupleSymbol newParent) {
		assert this.parentSymbol == null : "Must initialize parent only once.";
		assert newParent != null : "Parent must not be null.";
		
		this.parentSymbol = newParent;
	}
	
	@Override
	public String getTableAlias() {
		return tableAlias;
	}
	
	public void setTableAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}
	
	public DBAttribute getIdentifierColumn() {
		try {
			return (DBAttribute) type.getAttribute(BasicTypes.IDENTIFIER_ATTRIBUTE_NAME);
		} catch (NoSuchAttributeException ex) {
			throw new UnreachableAssertion(ex);
		}
	}
	
	@Override
	public SQLExpression createTypeExpr() {
		return dbAccess().createTypeExpr(this);
	}
	
	@Override
	public SQLExpression createBranchExpr() {
		return dbAccess().createBranchExpr(this);
	}

	@Override
	public SQLExpression createIdentifierExpr() {
		return dbAccess().createIdentifierExpr(this);
	}

	@Override
	public boolean hasOwnLifetime() {
		return true;
	}
	
	@Override
	public SQLExpression createRevMaxExpr() {
		return dbAccess().createRevMaxExpr(this);
	}

	@Override
	public SQLExpression createRevMinExpr() {
		return dbAccess().createRevMinExpr(this);
	}
	
	private DBAccess dbAccess() {
		return ((MOKnowledgeItemImpl) getType()).getDBAccess();
	}

	@Override
	public SQLExpression createRevisionExpr() {
		if (parentSymbol instanceof ItemSymbol) {
			/* E.g.: It could be that this table is derived from a reference, if that reference is a
			 * historic reference not the current revision (i.e. the requested revision) is the
			 * revision of the represented item, but the revision which is stored in the reference
			 * is the needed. */
			return ((ItemSymbol) parentSymbol).createRevisionExpr();
		}
		return currentRevision();
	}

	@Override
	public SQLExpression createHistoryContextExpr() {
		if (parentSymbol instanceof ItemSymbol) {
			/* E.g.: It could be that this table is derived from a reference, if that reference is a
			 * historic reference not the current revision (i.e. the requested revision) is the
			 * revision of the represented item, but the revision which is stored in the reference
			 * is the needed. */
			return ((ItemSymbol) parentSymbol).createHistoryContextExpr();
		}
		return literalLong(Revision.CURRENT_REV);
	}

	@Override
	public MOClass getType() {
		return type;
	}
	
	@Override
	public TableSymbol dereference(MOClass concreteType) {
		return this;
	}
	
	/**
	 * Returns an {@link Symbol} for the given {@link Attribute row attribute} or {@link Reference
	 * reference attribute}.
	 */
	public Symbol getAttributeSymbol(QueryPart attribute) {
		Symbol attributeSymbol = attributeSymbols.get(attribute);
		if (attributeSymbol == null) {
			if (attribute instanceof Attribute) {
				attributeSymbol = new RowAttributeSymbol(this, (Attribute) attribute);
			} else if (attribute instanceof Reference) {
				attributeSymbol = new ReferenceSymbol(this, (Reference) attribute);
			} else {
				assert false : "Could only create Symbols for row attributes and reference attributes: " + attribute;
			}
			attributeSymbols.put(attribute, attributeSymbol);
		} else {
			assert attributeSymbol.getDefinition() == attribute : "Attribute mismatch.";
		}
		
		return attributeSymbol;
	}

	/**
	 * Returns all {@link Symbol}s for the known attributes of this table.
	 */
	public Collection<Symbol> getAttributeSymbols() {
		return attributeSymbols.values();
	}
	
	@Override
	public <R, A> R visit(SymbolVisitor<R,A> v, A arg) {
		return v.visitTableSymbol(this, arg);
	}

	@Override
	public boolean isPotentiallyNull() {
		return _potentiallyNull;
	}

	@Override
	public void setPotentiallyNull(boolean potentiallyNull) {
		_potentiallyNull = potentiallyNull;
	}

}