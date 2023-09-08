/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform;

import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.db2.expr.meta.MetaExpressionFactory;
import com.top_logic.knowledge.service.db2.expr.meta.MetaSet;
import com.top_logic.knowledge.service.db2.expr.meta.MetaSetExpressionVisitor;
import com.top_logic.knowledge.service.db2.expr.meta.MetaVariable;

/**
 * Template for a {@link SetExpression}. It starts with a simple {@link MetaVariable}, step by step
 * the
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class SimpleSetTemplate implements Cloneable {

	/**
	 * {@link ExpressionTransformer} that clones ordinary {@link QueryPart}. Bounded instances of
	 * {@link MetaSet} are replaces by a copy of their bindings. Unbounded {@link MetaSet}s are
	 * replaced by the given {@link MetaSet} argument.
	 */
	private static final class TemplateCloner extends ExpressionTransformer<MetaSet> implements MetaSetExpressionVisitor<SetExpression, MetaSet> {
		
		/**
		 * Singleton {@link TemplateCloner} instance.
		 */
		public static final TemplateCloner INSTANCE = new TemplateCloner();

		private TemplateCloner() {
			// Singleton constructor.
		}
		
		@Override
		public SetExpression visitMetaSet(MetaSet mexpr, MetaSet newVar) {
			SetExpression binding = mexpr.getBinding();
			if (binding != null) {
				return binding.visitSetExpr(this, newVar);
			} else {
				return newVar;
			}
		}
	}

	private SetExpression root;
	private MetaSet var;
	
	public SimpleSetTemplate() {
		MetaSet rootVar = newVar();
		this.root = rootVar;
		this.var = rootVar;
	}
	
	private SimpleSetTemplate(SetExpression root, MetaSet rootVar) {
		this.root = root;
		this.var = rootVar;
	}
	
	/**
	 * the expanded template
	 */
	public SetExpression getExpansion() {
		assert this.var == null : "There is still a free variable.";
		return internalCloneRoot(null);
	}
	
	/**
	 * the current variable of this template
	 */
	public MetaSet getVar() {
		return var;
	}
	
	/**
	 * Binds the current variable to the given {@link SetExpression} and installs the
	 * <code>newVar</code>. Typically the new variable is the only variable of the given expression
	 * or <code>null</code>.
	 * 
	 * @param expr
	 *        expression to bind the current variable to
	 * @param newVar
	 *        new variable of this template
	 * 
	 * @see #getVar()
	 */
	public void expandVar(SetExpression expr, MetaSet newVar) {
		var.bind(expr);
		var = newVar;
	}
	
	@Override
	public SimpleSetTemplate clone() {
		MetaSet varClone = newVar();
		SetExpression rootClone = internalCloneRoot(varClone);
		return new SimpleSetTemplate(rootClone, varClone);
	}

	private SetExpression internalCloneRoot(MetaSet varClone) {
		return this.root.visitSetExpr(TemplateCloner.INSTANCE, varClone);
	}

	private MetaSet newVar() {
		return MetaExpressionFactory.metaSet(null);
	}

}
