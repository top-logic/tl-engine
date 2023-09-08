/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.interpreter.transform;

import com.top_logic.basic.treexf.Expr;
import com.top_logic.basic.treexf.Node;
import com.top_logic.model.search.expr.Access;
import com.top_logic.model.search.expr.All;
import com.top_logic.model.search.expr.InstanceOf;
import com.top_logic.model.search.expr.Lambda;
import com.top_logic.model.search.expr.Var;
import com.top_logic.model.search.expr.interpreter.transform.GenericSearchExprFactory;

/**
 * Factory for {@link Node}s useful only for tests.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GenericSearchExprTestFactory extends GenericSearchExprFactory {

	/**
	 * Creates an {@link All} node that uses a plain {@link String} as type value.
	 */
	public static Node all(String type) {
		return all(value(type));
	}

	/**
	 * Creates an {@link InstanceOf} node that uses a plain {@link String} as type value.
	 */
	public static Node instanceOf(String type, Node value) {
		return instanceOf(value(type), value);
	}

	/**
	 * @see Access
	 */
	public static Expr access(Node self, String part) {
		return access(self, value(part));
	}

	/**
	 * Creates a {@link Lambda} node with a concrete variable name.
	 */
	public static Expr lambda(String var, Node body) {
		return lambda(value(var), body);
	}

	/**
	 * Creates a {@link Var} node with a concrete variable name.
	 */
	public static Expr var(String name) {
		return var(value(name));
	}

	/**
	 * Creates a block with a bound variable using a concrete variable name.
	 */
	public static Expr let(String var, Node init, Node body) {
		return let(value(var), init, body);
	}

}
