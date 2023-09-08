/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.sym;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.knowledge.service.db2.expr.transform.sql.DBSQLFactory.*;

import java.util.Map;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.db.sql.Conversion;
import com.top_logic.basic.db.sql.MappingConversion;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.search.Literal;
import com.top_logic.knowledge.search.ParameterDeclaration;
import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.expr.transform.sql.SQLBuilder;

/**
 * {@link Symbol} for a literal item value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LiteralItemSymbol extends AbstractItemSymbol {

	private static final Conversion GET_BRANCH = new MappingConversion<>(ObjectKey.GET_BRANCH);

	private static final Conversion GET_TYPE = new MappingConversion<>(ObjectKey.GET_TYPE_NAME);

	private static final Conversion GET_OBJECT_NAME = new MappingConversion<>(ObjectKey.GET_OBJECT_NAME_MAPPING);

	private static final Conversion GET_REVISION = new Conversion() {

		@Override
		public Object convert(Object argument, Map<String, Integer> argumentIndexByName, Object[] arguments) {
			ObjectKey key = (ObjectKey) argument;
			if (HistoryUtils.isCurrent(key)) {
				Integer index = argumentIndexByName.get(SQLBuilder.REQUESTED_REVISION_PARAM);
				if (index == null) {
					return Revision.CURRENT_REV_WRAPPER;
				} else {
					return arguments[index.intValue()];
				}
			} else {
				return key.getHistoryContext();
			}
		}
	};

	private static final Conversion GET_HISTORY_CONTEXT = new MappingConversion<>(ObjectKey.GET_HISTORY_CONTEXT);

	/**
	 * Creates a {@link LiteralItemSymbol}.
	 * 
	 * @param definition
	 *        The {@link Literal} or {@link ParameterDeclaration} for which this
	 *        symbol is created.
	 */
	public LiteralItemSymbol(QueryPart definition) {
		super(definition);
	}

	@Override
	public TableSymbol dereference(MOClass concreteType) {
		throw new AssertionError("Access to literal symbols must be rewritten.");
	}

	@Override
	public MOClass getType() {
		return (MOClass) getDefinition().getPolymorphicType();
	}

	@Override
	public SQLExpression createBranchExpr() {
		return createExpression(DBType.LONG, ObjectKey.GET_BRANCH, GET_BRANCH);
	}

	@Override
	public SQLExpression createTypeExpr() {
		return createExpression(DBType.STRING, ObjectKey.GET_TYPE_NAME, GET_TYPE);
	}
	
	@Override
	public SQLExpression createIdentifierExpr() {
		return createExpression(DBType.ID, ObjectKey.GET_OBJECT_NAME_MAPPING, GET_OBJECT_NAME);
	}

	@Override
	public boolean hasOwnLifetime() {
		return false;
	}
	
	@Override
	public SQLExpression createRevMaxExpr() {
		throw new UnsupportedOperationException("No Rev max for literal value");
	}

	@Override
	public SQLExpression createRevMinExpr() {
		throw new UnsupportedOperationException("No Rev min for literal value");
	}

	@Override
	public SQLExpression createHistoryContextExpr() {
		return createExpression(DBType.LONG, ObjectKey.GET_HISTORY_CONTEXT, GET_HISTORY_CONTEXT);
	}

	@Override
	public SQLExpression createRevisionExpr() {
		/* The revision expression of an item literal is actually the revision of the represented
		 * object. In most cases the natural semantic of a literal representing a current object is,
		 * that if the query is executed in history, the current object also lives in the same time.
		 * Therefore the revision expression is computed from the revision in which the query is
		 * executed. */
		QueryPart definition = getDefinition();
		if (definition instanceof ParameterDeclaration) {
			ParameterDeclaration param = (ParameterDeclaration) definition;
			return parameter(DBType.LONG, GET_REVISION, param.getName());
		}
		else if (definition instanceof Literal) {
			Literal literal = (Literal) definition;
			ObjectKey identifier = (ObjectKey) literal.getValue();
			if (HistoryUtils.isCurrent(identifier)) {
				return currentRevision();
			} else {
				return literal(DBType.LONG, ObjectKey.GET_HISTORY_CONTEXT.map(identifier));
			}
		}
		else {
			throw new AssertionError("Literal item symbols are only created for literals and parameters.");
		}
	}

	private SQLExpression createExpression(DBType sqlType, Mapping<Object, ?> mapping, Conversion conversion)
			throws AssertionError {
		QueryPart definition = getDefinition();
		if (definition instanceof ParameterDeclaration) {
			ParameterDeclaration param = (ParameterDeclaration) definition;
			return parameter(sqlType, conversion, param.getName());
		}
		else if (definition instanceof Literal) {
			Literal literal = (Literal) definition;
			return literal(sqlType, mapping.map(literal.getValue()));
		}
		else {
			throw new AssertionError("Literal item symbols are only created for literals and parameters.");
		}
	}

	@Override
	public Symbol getParent() {
		return null;
	}

	@Override
	public void initParent(TupleSymbol tupleSymbol) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <R, A> R visit(SymbolVisitor<R, A> v, A arg) {
		return v.visitLiteralItemSymbol(this, arg);
	}

	@Override
	public String getTableAlias() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPotentiallyNull() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPotentiallyNull(boolean potentiallyNull) {
		throw new UnsupportedOperationException();
	}

}
