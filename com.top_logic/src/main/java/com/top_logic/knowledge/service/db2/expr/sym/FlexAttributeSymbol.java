/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.sym;

import static com.top_logic.dob.sql.SQLFactory.*;

import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.util.TLContext;

/**
 * {@link Symbol} for an access to a flex attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FlexAttributeSymbol extends AttributeSymbol implements DataSymbol {
	private ItemSymbol contextSymbol;
	private final String attributeName;
	private String dataTableAlias;
	private final MetaObject type;

	private final TypeSystem typeSystem;
	
	/** @see #isPotentiallyNull() */
	private boolean _potentialNull;

	public FlexAttributeSymbol(QueryPart definition, TypeSystem typeSystem, ItemSymbol contextSymbol,
			MetaObject type, String attributeName) {
		super(definition);
		this.contextSymbol = contextSymbol;
		this.type = type;
		this.attributeName = attributeName;
		this.typeSystem = typeSystem;
	}
	
	public MOKnowledgeItemImpl getFlexDataType() {
		try {
			return (MOKnowledgeItemImpl) this.typeSystem.getType(AbstractFlexDataManager.FLEX_DATA);
		} catch (UnknownTypeException ex) {
			throw (AssertionError) new AssertionError("No flex data type in type system.").initCause(ex);
		}
	}

	public MetaObject getType() {
		return type;
	}
	
	@Override
	public String getAttributeName() {
		return attributeName;
	}
	
	@Override
	public ItemSymbol getParent() {
		return contextSymbol;
	}
	
	public void setDataTableAlias(String dataTableAlias) {
		this.dataTableAlias = dataTableAlias;
	}
	
	@Override
	public String getDataTableAlias() {
		return dataTableAlias;
	}

	@Override
	public SQLExpression createTypeExpr() {
		return createColumn(AbstractFlexDataManager.TYPE);
	}
	
	/**
	 * Access to the attribute name in the flex table.
	 */
	public SQLExpression createAttrExpr() {
		return createColumn(AbstractFlexDataManager.ATTRIBUTE);
	}

	@Override
	public SQLExpression createIdentifierExpr() {
		return createColumn(AbstractFlexDataManager.IDENTIFIER);
	}
	
	@Override
	public SQLExpression createBranchExpr() {
		MOKnowledgeItemImpl flexDataType = getFlexDataType();
		if (flexDataType.multipleBranches()) {
			return createColumn(AbstractFlexDataManager.BRANCH);
		} else {
			return SQLFactory.literalLong(TLContext.TRUNK_ID);
		}
	}
	
	private SQLExpression createColumn(String attribute) {
		DBAttribute dbAttribute = AbstractFlexDataManager.getDBAttribute(getFlexDataType(), attribute);
		boolean notNull = !isPotentiallyNull() && dbAttribute.isSQLNotNull();
		return column(getDataTableAlias(), dbAttribute, notNull);
	}

	@Override
	public boolean hasOwnLifetime() {
		return true;
	}

	@Override
	public SQLExpression createRevMaxExpr() {
		DBAttribute dbAttribute =
			AbstractFlexDataManager.getDBAttribute(getFlexDataType(), AbstractFlexDataManager.REV_MAX);
		return column(getDataTableAlias(), dbAttribute, false);
	}
	
	@Override
	public SQLExpression createRevMinExpr() {
		DBAttribute dbAttribute =
			AbstractFlexDataManager.getDBAttribute(getFlexDataType(), AbstractFlexDataManager.REV_MIN);
		return column(getDataTableAlias(), dbAttribute, false);
	}
	
	@Override
	public <R, A> R visit(SymbolVisitor<R,A> v, A arg) {
		return v.visitFlexAttributeSymbol(this, arg);
	}

	@Override
	public boolean isPotentiallyNull() {
		return _potentialNull;
	}

	@Override
	public void setPotentiallyNull(boolean potentialNull) {
		_potentialNull = potentialNull;
	}
}