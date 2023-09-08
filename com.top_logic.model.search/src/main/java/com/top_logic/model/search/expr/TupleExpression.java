/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientModelFactory;
import com.top_logic.model.search.expr.interpreter.TypeResolver;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} deriving tuples by correlating sub-searches.
 * 
 * @see Coord#getExpr()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TupleExpression extends SearchExpression {

	/**
	 * Definition of a tuple's coordinate.
	 */
	public static class Coord extends LazyTypedAnnotatable implements Definition {
		private boolean _optional;

		private final NamedConstant _key;

		private Object _name;

		private SearchExpression _expr;

		/**
		 * Definition of a tuple's coordinate.
		 *
		 * @param optional
		 *        See {@link #isOptional()}.
		 * @param name
		 *        See {@link #getName()}.
		 * @param expr
		 *        See {@link #getExpr()}.
		 */
		public Coord(boolean optional, Object name, SearchExpression expr) {
			super();
			_optional = optional;
			_name = name;
			_expr = expr;
			_key = new NamedConstant(name.toString());
		}

		/**
		 * Whether this tuple's coordinate is filled with <code>null</code>, if the
		 * {@link #getExpr()} retrieves the empty set.
		 */
		public boolean isOptional() {
			return _optional;
		}

		/**
		 * The name of this tuple's coordinate.
		 */
		public Object getName() {
			return _name;
		}

		@Override
		public NamedConstant getKey() {
			return _key;
		}

		/**
		 * The sub-search deriving potential values for this coordinate.
		 */
		public SearchExpression getExpr() {
			return _expr;
		}

		/**
		 * @see #getExpr()
		 */
		public void setExpr(SearchExpression expr) {
			_expr = expr;
		}
	}

	private Coord[] _coords;

	/**
	 * Creates a {@link TupleExpression}.
	 */
	TupleExpression(Coord[] coords) {
		_coords = coords;
	}

	/**
	 * Definitions of tuple coordinates.
	 */
	public Coord[] getCoords() {
		return _coords;
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		TLClass resultType = (TLClass) TypeResolver.getType(this);
		assert resultType != null : "No type found for tuple expression, type inference stage is missing.";

		ArrayList<Object> result = new ArrayList<>();
		addResults(resultType, result, definitions, new Object[_coords.length], 0);
		return result;
	}

	private void addResults(TLClass resultType, List<Object> buffer, EvalContext definitions, Object[] values,
			int index) {
		if (index == _coords.length) {
			TLObject tuple = TransientModelFactory.createTransientObject(resultType);
			for (Coord coord : _coords) {
				tuple.tUpdateByName(coord.getName().toString(), definitions.getVar(coord.getKey()));
			}
			buffer.add(tuple);
		} else {
			Coord coord = _coords[index];
			SearchExpression coordExpr = coord.getExpr();
			Collection<?> coordValues = asCollection(coordExpr.eval(definitions));
			if (coordValues.isEmpty()) {
				if (coord.isOptional()) {
					addCoordValue(resultType, buffer, coord, definitions, values, index, null);
				}
			} else {
				for (Object coordValue : coordValues) {
					addCoordValue(resultType, buffer, coord, definitions, values, index, coordValue);
				}
			}
		}
	}

	private void addCoordValue(TLClass resultType, List<Object> buffer, Coord coord, EvalContext definitions,
			Object[] values, int index,
			Object coordValue) {
		values[index] = coordValue;
		definitions.defineVar(coord.getKey(), coordValue);
		addResults(resultType, buffer, definitions, values, index + 1);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitTuple(this, arg);
	}

}
