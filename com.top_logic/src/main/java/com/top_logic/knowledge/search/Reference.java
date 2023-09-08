/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.column;

import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.AbstractMOReference;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBMetaObject;
import com.top_logic.knowledge.KnowledgeReferenceStorageImpl;
import com.top_logic.knowledge.service.db2.expr.sym.ReferenceSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.TableSymbol;
import com.top_logic.util.TLContext;

/**
 * Access to a reference attribute of the context object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Reference extends ContextExpression {

	/**
	 * The part of the reference to access. <code>null</code> means that the whole reference is
	 * desired
	 */
	private final ReferencePart _accessPart;

	/**
	 * name of the reference attribute. Used to identify {@link #_attribute}.
	 */
	private final String _attributeName;

	/**
	 * name of the type which declares the reference attribute with name {@link #_attributeName}.
	 * Used to identify {@link #_attribute}.
	 */
	private final String _ownerTypeName;

	/**
	 * The resolved {@link MOReference}.
	 */
	private MOReference _attribute;

	/**
	 * Creates a {@link Reference} with the given parameter
	 * 
	 * @param context
	 *        The context to evaluate the reference.
	 * @param ownerTypeName
	 *        see {@link #getOwnerTypeName()}
	 * @param attributeName
	 *        see {@link #getAttributeName()}
	 * @param accessPart
	 *        see {@link #getAccessType()}
	 */
	Reference(Expression context, String ownerTypeName, String attributeName, ReferencePart accessPart) {
		super(context);
		_ownerTypeName = ownerTypeName;
		_attributeName = attributeName;
		_accessPart = accessPart;
	}
	
	@Override
	public <R,A> R visit(ExpressionVisitor<R,A> v, A arg) {
		return v.visitReference(this, arg);
	}

	/**
	 * Initializes the resolved {@link MOReference}.
	 */
	void setAttribute(MOReference attribute) {
		_attribute = attribute;

	}

	/**
	 * Access to the reference attribute
	 */
	public MOReference getAttribute() {
		return _attribute;
	}

	/**
	 * Access to the name of the reference attribute
	 */
	public String getAttributeName() {
		return _attributeName;
	}

	/**
	 * Access to the name of the owner type of the reference attribute
	 */
	public String getOwnerTypeName() {
		return _ownerTypeName;
	}

	/**
	 * Access to the desired part of the reference. <code>null</code> means that the whole reference
	 * is desired.
	 */
	public ReferencePart getAccessType() {
		return _accessPart;
	}

	/**
	 * Access to the column used to store the data of the given reference aspect
	 */
	public DBAttribute getColumn(ReferencePart aspect) {
		DBAttribute column = getAttribute().getColumn(aspect);
		if (column == null) {
			StringBuilder msg = new StringBuilder();
			msg.append("No column for aspect '");
			msg.append(aspect);
			msg.append("' in reference '");
			msg.append(getAttribute());
			msg.append("'");
			throw new NullPointerException(msg.toString());
		}
		return column;
	}

	/**
	 * Access to the database type of the column which stores the data of the given reference
	 * aspect.
	 */
	public DBMetaObject getColumnType(ReferencePart aspect) {
		return getAttribute().getType(aspect);
	}

	/**
	 * Creates an {@link SQLExpression} whose value is the {@link MetaObject#getName() name of the
	 * type} of the referenced object.
	 * 
	 * @param table
	 *        The representation of the table in the SQL statement which represents the type which
	 *        declares the reference attribute.
	 */
	public SQLExpression createTypeExpression(ReferenceSymbol table) {
		final MOReference attribute = getAttribute();
		if (attribute.isMonomorphic()) {
			return literalString(attribute.getMetaObject().getName());
		} else {
			return getColumn(table, ReferencePart.type);
		}
	}

	/**
	 * Creates an {@link SQLExpression} whose value is the {@link ObjectKey#getBranchContext()
	 * branch} of the referenced object.
	 * 
	 * @param table
	 *        the alias of the table in the SQL statement which represents the type which declares
	 *        the reference attribute.
	 * @param parentSymbol
	 *        The representation of the table in the SQL statement which represents the type which
	 *        declares the reference attribute.
	 */
	public SQLExpression createBranchExpression(ReferenceSymbol table, TableSymbol parentSymbol) {
		final AbstractMOReference attribute = (AbstractMOReference) getAttribute();
		if (!attribute.targetTypeHasBranchColumn()) {
			// values are always in trunk.
			return literalLong(TLContext.TRUNK_ID);
		} else if (attribute.isBranchGlobal()) {
			return getColumn(table, ReferencePart.branch);
		} else {
			return parentSymbol.createBranchExpr();
		}
	}

	/**
	 * Creates an {@link SQLExpression} whose value is the {@link ObjectKey#getObjectName() name} of
	 * the referenced object.
	 * 
	 * @param table
	 *        The representation of the table in the SQL statement which represents the type which
	 *        declares the reference attribute.
	 */
	public SQLExpression createIdentifierExpression(ReferenceSymbol table) {
		return getColumn(table, ReferencePart.name);
	}

	/**
	 * Creates an {@link SQLExpression} whose value is the {@link ObjectKey#getHistoryContext()
	 * revision} of the referenced object.
	 * 
	 * @param table
	 *        the alias of the table in the SQL statement which represents the type which declares
	 *        the reference attribute.
	 */
	public SQLExpression createRevisionExpr(ReferenceSymbol table) {
		switch (getAttribute().getHistoryType()) {
			case MIXED: {
				SQLExpression historicValue = getRevisionColumn(table);
				SQLExpression currentValue = currentRevision(table);
				SQLExpression currentValueStored =
					eq(historicValue, literalLong(KnowledgeReferenceStorageImpl.MIXED_REFERENCE_CURRENT_REPRESENTATION));
				return sqlCase(currentValueStored, currentValue, historicValue);
			}
			case HISTORIC: {
				return getRevisionColumn(table);
			}
			case CURRENT: {
				return currentRevision(table);
			}
		}
		throw HistoryType.noSuchType(getAttribute().getHistoryType());
	}

	private SQLExpression currentRevision(ReferenceSymbol table) {
		// The current revision is defined by the table in which the reference is defined.
		return table.getParent().createRevisionExpr();
	}

	/**
	 * Creates an {@link SQLExpression} that represents the revision column of the reference.
	 * 
	 * <p>
	 * Must not be called when no revision column is available
	 * </p>
	 */
	public SQLExpression getRevisionColumn(ReferenceSymbol table) {
		return getColumn(table, ReferencePart.revision);
	}

	/**
	 * Returns (a supertype of) the {@link MetaObject} the reference points to.
	 */
	public MOClass getTargetType() {
		return (MOClass) getAttribute().getMetaObject();
	}

	private SQLExpression getColumn(ReferenceSymbol table, ReferencePart aspect) {
		DBAttribute dbAttribute = getColumn(aspect);
		boolean notNull = !table.isPotentiallyNull() && dbAttribute.isSQLNotNull();
		return column(table.getTableAlias(), dbAttribute, notNull);
	}

}

