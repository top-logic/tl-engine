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

import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.IdentifierTypes;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.knowledge.search.Reference;
import com.top_logic.knowledge.service.HistoryUtils;

/**
 * {@link Symbol} representing the potential target types of some reference.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ReferenceSymbol extends AbstractItemSymbol {

	/**
	 * The {@link DBType}s of a tuple created in
	 * {@link #newTuple(SQLExpression, SQLExpression, SQLExpression)}.
	 */
	public static final DBType[] ITEM_TUPLE_DBTYPE = new DBType[] {
		IdentifierTypes.BRANCH_REFERENCE_MO_TYPE.getDefaultSQLType(),
		IdentifierTypes.REVISION_REFERENCE_MO_TYPE.getDefaultSQLType(),
		IdentifierTypes.REFERENCE_MO_TYPE.getDefaultSQLType()
	};

	/**
	 * Symbol of the type which declares the reference attribute
	 */
	private final TableSymbol contextSymbol;

	private final Map<MOClass, TableSymbol> tableSymbols = new HashMap<>();

	/**
	 * @param contextSymbol
	 *        symbol representing the type which declares the reference attribute.
	 * @param reference
	 *        the reference attribute
	 */
	public ReferenceSymbol(TableSymbol contextSymbol, Reference reference) {
		super(reference);
		this.contextSymbol = contextSymbol;
	}
	

	@Override
	public MOClass getType() {
		return this.getDefinition().getTargetType();
	}
	
	/**
	 * Returns the {@link TableSymbol} representing the table in which the reference is defined.
	 * 
	 * @see com.top_logic.knowledge.service.db2.expr.sym.Symbol#getParent()
	 */
	@Override
	public TableSymbol getParent() {
		return contextSymbol;
	}
	
	@Override
	public void initParent(TupleSymbol parent) {
		throw new UnsupportedOperationException("A reference symbol gets its parent during construction.");
	}
	
	/**
	 * Returns the SQL alias of the table of the type declaring the reference attribute.
	 */
	@Override
	public String getTableAlias() {
		return getParent().getTableAlias();
	}

	/**
	 * Returns the reference attribute.
	 * 
	 * TypeSafe re-declaration, because definition is always a {@link Reference}.
	 * 
	 * @see com.top_logic.knowledge.service.db2.expr.sym.AbstractSymbol#getDefinition()
	 */
	@Override
	public final Reference getDefinition() {
		return (Reference) super.getDefinition();
	}

	public Collection<TableSymbol> getTableSymbols() {
		return tableSymbols.values();
	}
	
	@Override
	public TableSymbol dereference(MOClass concreteType) {
		TableSymbol tableSymbol = tableSymbols.get(concreteType);
		if (tableSymbol == null) {
			tableSymbol = new TableSymbol(this, concreteType);
			tableSymbols.put(concreteType, tableSymbol);
		}
		return tableSymbol;
	}
	
	@Override
	public <R, A> R visit(SymbolVisitor<R,A> v, A arg) {
		return v.visitReferenceSymbol(this, arg);
	}

	@Override
	public SQLExpression createTypeExpr() {
		return getDefinition().createTypeExpression(this);
	}

	@Override
	public SQLExpression createIdentifierExpr() {
		return getDefinition().createIdentifierExpression(this);
	}
	
	@Override
	public SQLExpression createBranchExpr() {
		return getDefinition().createBranchExpression(this, getParent());
	}
	
	@Override
	public SQLExpression createRevisionExpr() {
		return getDefinition().createRevisionExpr(this);
	}

	@Override
	public SQLExpression createHistoryContextExpr() {
		return getDefinition().createRevisionExpr(this);
	}

	/**
	 * Creates an {@link SQLExpression} to access the given aspect of the reference
	 * 
	 * @param aspect
	 *        the part of the reference to access. must not be <code>null</code>.
	 */
	public SQLExpression createExpression(ReferencePart aspect) {
		if (aspect == null) {
			return newTuple(this);
		} else {
			return expressionForAspect(aspect);
		}

	}

	private static SQLExpression newTuple(ReferenceSymbol referenceSymbol) {
		SQLExpression branch = referenceSymbol.createBranchExpr();
		SQLExpression name = referenceSymbol.createIdentifierExpr();
		SQLExpression revision = referenceSymbol.createRevisionExpr();
		return newTuple(branch, name, revision);
	}

	/**
	 * Creates a {@link Tuple} for an {@link ObjectKey}.
	 */
	public static SQLExpression newTuple(ObjectKey identifier) {
		SQLExpression branch = literal(DBType.LONG, ObjectKey.GET_BRANCH.map(identifier));
		SQLExpression revision;
		if (HistoryUtils.isCurrent(identifier)) {
			revision = currentRevision();
		} else {
			revision = literal(DBType.LONG, ObjectKey.GET_HISTORY_CONTEXT.map(identifier));
		}
		SQLExpression name = literal(DBType.ID, ObjectKey.GET_OBJECT_NAME_MAPPING.map(identifier));
		return newTuple(branch, name, revision);
	}

	/**
	 * Creates a tuple to compare with other tuples representing an object.
	 * 
	 * <p>
	 * This does not include the type because the id of an object identifies it all over all types.
	 * </p>
	 * 
	 * @see #ITEM_TUPLE_DBTYPE DBType's of the entries of the result tuple.
	 */
	public static SQLExpression newTuple(SQLExpression branch, SQLExpression name, SQLExpression revision) {
		// WARNING: When changed also #ITEM_TUPLE_DBTYPE must be changed.
		return SQLFactory.tuple(branch, revision, name);
	}

	private SQLExpression expressionForAspect(ReferencePart aspect) {
		switch (aspect) {
			case branch:
				return createBranchExpr();
			case type:
				return createTypeExpr();
			case name:
				return createIdentifierExpr();
			case revision:
				return createRevisionExpr();
		}
		throw ReferencePart.noSuchPart(aspect);

	}

	@Override
	public boolean hasOwnLifetime() {
		return false;
	}

	@Override
	public SQLExpression createRevMinExpr() {
		return getParent().createRevMinExpr();
	}
	
	@Override
	public SQLExpression createRevMaxExpr() {
		return getParent().createRevMaxExpr();
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
	 * @see Reference#getRevisionColumn(ReferenceSymbol)
	 */
	public SQLExpression getRevisonColumn() {
		return getDefinition().getRevisionColumn(this);
	}
}
