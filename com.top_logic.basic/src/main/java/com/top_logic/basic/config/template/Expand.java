/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

import com.top_logic.basic.config.template.TemplateExpression.Tag;

/**
 * Algorithm for textually expanding {@link TemplateExpression}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Expand implements AbstractExpand<Void, RuntimeException> {

	/**
	 * Callback interface to write a template expansion to.
	 * 
	 * <p>
	 * A template expansion consists of a sequence of result objects produced by expression
	 * evaluations.
	 * </p>
	 */
	public interface Output {
		/**
		 * Adds the given result object to this {@link Output}.
		 */
		void add(Object result);
	}

	private final Output _out;

	private final TemplateScope _scope;

	private final Eval _eval;

	/**
	 * Creates a {@link Expand}.
	 * 
	 * @param scope
	 *        Scope of all templates available.
	 */
	public Expand(TemplateScope scope, Expand.Output out) {
		_eval = new Eval(ConfigModelAccess.INSTANCE);
		_scope = scope;
		_out = out;
	}

	@Override
	public Eval eval() {
		return _eval;
	}

	@Override
	public TemplateExpression getTemplate(String templateName) {
		return _scope.getTemplate(templateName, false);
	}

	@Override
	public Void visitTag(Tag expr, Eval.IContext arg) {
		return visitSequence(expr, arg);
	}

	/**
	 * Adds the given literal value to the created output.
	 */
	@Override
	public void output(Object result) {
		_out.add(result);
	}
}
