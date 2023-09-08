/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.Set;

import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.service.db2.expr.sym.Symbol;
import com.top_logic.knowledge.service.db2.expr.visit.ExpressionPrinter;


/**
 * Base class for all parts of a {@link AbstractQuery}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class QueryPart {

	private MetaObject polymorphicType;
	private Set<MetaObject> concreteTypes;
	private Symbol symbol;

	/**
	 * Use {@link ExpressionPrinter} to render this as String.
	 */
	@Override
	public String toString() {
	    return ExpressionPrinter.toString(this);
	}

	/**
	 * Polymorphic return type of an {@link Expression} or {@link Function}, the
	 * polymorphic content type of a {@link SetExpression}. Undefined otherwise.
	 */
	public MetaObject getPolymorphicType() {
		return polymorphicType;
	}

	/**
	 * @see #getPolymorphicType()
	 */
	public void setPolymorphicType(MetaObject type) {
		this.polymorphicType = type;
	}

	/**
	 * Set of concrete types, this {@link Expression} may return or this
	 * {@link SetExpression} may contain.
	 */
	public Set<MetaObject> getConcreteTypes() {
		return concreteTypes;
	}
	
	/**
	 * @see #getConcreteTypes()
	 */
	public void setConcreteTypes(Set<MetaObject> concreteTypes) {
		this.concreteTypes = concreteTypes;
	}

	public final Symbol getSymbol() {
		return symbol;
	}
	
	public final void setSymbol(Symbol symbol) {
		this.symbol = symbol;
	}

	/**
	 * Visits this {@link QueryPart} using the given visitor.
	 * 
	 * @param v
	 *        The visiting visitor.
	 * @param arg
	 *        An argument to the visit.
	 * @return The result of the visit.
	 */
	public abstract <RQ, RE extends RQ,RS extends RQ,RF extends RQ,RO extends RQ,A> RQ visitQuery(QueryVisitor<RQ,RE,RS,RF,RO,A> v, A arg);

}
