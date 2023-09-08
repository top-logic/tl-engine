/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.interpreter;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.model.search.expr.Definition;
import com.top_logic.model.search.expr.Lambda;
import com.top_logic.model.search.expr.TupleExpression;
import com.top_logic.model.search.expr.TupleExpression.Coord;
import com.top_logic.model.search.expr.Var;
import com.top_logic.model.search.expr.visit.DefaultDescendingVisitor;
import com.top_logic.util.error.TopLogicException;

/**
 * Fills {@link Var#setDef(Definition)}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefResolver extends DefaultDescendingVisitor<Void, Void> {

	private Map<Object, Definition> _data = new HashMap<>();

	private Definition enter(Object name, Definition def) {
		return _data.put(name, def);
	}

	private void exit(Object name, Definition before) {
		if (before == null) {
			_data.remove(name);
		} else {
			_data.put(name, before);
		}
	}

	private Definition getDef(Object name) {
		return _data.get(name);
	}

	@Override
	public Void visitLambda(Lambda expr, Void arg) {
		Definition before = enter(expr.getName(), expr);
		Void result = super.visitLambda(expr, arg);
		exit(expr.getName(), before);

		return result;
	}

	@Override
	public Void visitTuple(TupleExpression expr, Void arg) {
		Coord[] coords = expr.getCoords();
		int length = coords.length;
		Definition[] oldDefs = new Definition[length];
		for (int n = 0; n < length; n++) {
			Coord coord = coords[n];
			descendPart(expr, arg, coord.getExpr());

			Definition oldDef = enter(coord.getName(), coord);
			oldDefs[n] = oldDef;
		}
		for (int n = length - 1; n >= 0; n--) {
			Coord coord = coords[n];
			exit(coord.getName(), oldDefs[n]);
		}
		return compose(expr, arg, none());
	}

	@Override
	public Void visitVar(Var expr, Void arg) {
		Object name = expr.getName();
		Definition definition = getDef(name);
		if (definition == null) {
			throw new TopLogicException(I18NConstants.ERROR_UNRESOLVED_VARIABLE__NAME.fill(name));
		}
		expr.setDef(definition);
		
		return super.visitVar(expr, arg);
	}

}