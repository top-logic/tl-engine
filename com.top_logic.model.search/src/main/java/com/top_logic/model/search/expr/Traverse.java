/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;

/**
 * Traverses and optionally transforms a graph.
 * 
 * <code><pre>
 * $root.traverse(
 *     descend: node -> parent -> lastResult -> { ... },
 *     mapping: node -> parent -> lastResult -> { ... },
 *     compose: result -> childResults -> node -> parent -> { ... }
 * )
 * </pre></code>
 * 
 * <p>
 * Traversal happens in the following order:
 * </p>
 * 
 * <ol>
 * <li>The root node is mapped by applying the <code>mapping</code> function to it.</li>
 * <li>Then the next nodes to visit are computed by applying the <code>descend</code> function to
 * the current node.</li>
 * <li>All next nodes are processed recursively in the same way.</li>
 * <li>When recursion returns, the optional <code>compose</code> function is applied to the result
 * produced for the current node and all of it's children nodes forming the final result of the
 * visit. If no <code>compose</code> function is given, the result of the traversal is the result
 * produced by the mapping function for the root node.</li>
 * </ol>
 * 
 * <h3>Parameter <code>root</code></h3>
 * <p>
 * <b>Type:</b> Any object.
 * </p>
 * <p>
 * The graph traversal starts with this object.
 * </p>
 * 
 * <h3>Parameter <code>descend</code> (mandatory)</h3>
 * <p>
 * <b>Type:</b> Function taking up to three arguments (<code>node</code>, <code>parent</code>,
 * <code>lastResult</code>).
 * </p>
 * <p>
 * The graph traversal is specified by the function <code>descend</code> that computes the next
 * nodes to visit after the given node. The descend function accepts the current node and optionally
 * the node's parent and the last result produced by the visit, if a node is hit more than once. The
 * <code>lastResult</code> argument can be used to stop the visit to prevent endless recursion when
 * traversing a cyclic graph.
 * </p>
 * 
 * <h3>Parameter <code>mapping</code> (optional)</h3>
 * <p>
 * <b>Type:</b> Function taking up to three arguments (<code>node</code>, <code>parent</code>,
 * <code>lastResult</code>).
 * </p>
 * <p>
 * The mapping function produces the visit result of a node, or performs an operation on a node,
 * when used in a transaction. If not given, the result of the visit is the visited node.
 * </p>
 * 
 * <h3>Parameter <code>compose</code> (optional)</h3>
 * <p>
 * <b>Type:</b> Function taking up to four arguments (<code>result</code>,
 * <code>childResults</code>, <code>node</code>, <code>parent</code>).
 * </p>
 * <p>
 * The function produces the final traversal result after the mapping result of a node and it's
 * children are available. If not given, the result of the transform operation is the result of the
 * mapping function applied to the root node.
 * </p>
 * <ul>
 * <li><code>result</code>: The result produced by the <code>mapping</code> function for the current
 * node.</li>
 * <li><code>childResults</code>: A list with results produced for each visited child of the current
 * node.</li>
 * <li><code>node</code>: The current node.</li>
 * <li><code>parent</code>: The current node's parent.</li>
 * </ul>
 */
public class Traverse extends GenericMethod {

	/**
	 * Creates a {@link Traverse} method.
	 */
	protected Traverse(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Traverse(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		// The type is the result type of the function passed as second argument.
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object root = arguments[0];
		if (root == null) {
			return null;
		}

		SearchExpression descend = asSearchExpression(arguments[1]);
		SearchExpression mapping = asSearchExpression(arguments[2]);
		SearchExpression compose = asSearchExpression(arguments[3]);

		Traversal traversal = new Traversal(definitions, mapping, descend, compose);
		return traversal.descend(null, root);
	}

	static class Traversal {
		private final EvalContext _definitions;

		private final SearchExpression _mapping;

		private final SearchExpression _descend;

		private final SearchExpression _compose;

		private final Map<Object, Object> _resultByInput = new HashMap<>();

		/**
		 * Creates a {@link Traversal}.
		 */
		public Traversal(EvalContext definitions, SearchExpression mapping, SearchExpression descend,
				SearchExpression compose) {
			_definitions = definitions;
			_mapping = mapping;
			_descend = descend;
			_compose = compose;
		}

		public Object descend(Object parent, Object node) {
			Object lastResult = _resultByInput.get(node);
			Object result = _mapping == null ? node : _mapping.eval(_definitions, node, parent, lastResult);
			Collection<?> children = asCollection(_descend.eval(_definitions, node, parent, lastResult));

			_resultByInput.put(node, result);

			if (_compose == null) {
				// Optimization for "identity" composition.
				for (Object next : children) {
					descend(node, next);
				}
				return result;
			} else {
				ArrayList<Object> childResults = new ArrayList<>();
				for (Object next : children) {
					Object childResult = descend(node, next);
					childResults.add(childResult);
				}
				return _compose.eval(_definitions, result, childResults, node, parent);
			}
		}
	}

	/**
	 * Factory for {@link Traverse} methods.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Traverse> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("root")
			.mandatory("descend")
			.optional("mapping")
			.optional("compose")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return Traverse.Builder.DESCRIPTOR;
		}

		@Override
		public Traverse build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new Traverse(getConfig().getName(), args);
		}

	}
}
