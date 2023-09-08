/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree;

import java.util.Map;

/**
 * Represents an 'if' expression ({@link FunctionStatement}) in the template language.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class IfStatement extends FunctionStatement {

	private final Expression condition;
	private final Map<String, String> attributes;
	private TemplateNode thenStm;
	private TemplateNode elseStm;

	/**
	 * Creates a new {@link IfStatement}.
	 */
	public IfStatement(Expression condition, Map<String, String> attributes) {
		this.condition = condition;
		this.attributes = attributes;
	}

	/**
	 * Setter for the 'else' {@link TemplateNode}.
	 */
	public void setElseStm(TemplateNode elseStm) {
		this.elseStm = elseStm;
	}

	/**
	 * Setter for the 'then' {@link TemplateNode}.
	 */
	public void setThenStm(TemplateNode thenStm) {
		this.thenStm = thenStm;
	}

	/**
	 * Getter for the condition {@link Expression}.
	 */
	public final Expression getCondition() {
		return this.condition;
	}

	@Override
	public Map<String, String> getAttributes() {
		return this.attributes;
	}

	/**
	 * Getter for the 'then' {@link TemplateNode}.
	 */
	public final TemplateNode getThenStm() {
		return this.thenStm;
	}

	/**
	 * Getter for the 'else' {@link TemplateNode}.
	 */
	public final TemplateNode getElseStm() {
		return this.elseStm;
	}
	
	@Override
	public <R, A> R visit(TemplateVisitor<R, A> v, A arg) {
		return v.visitIfStatement(this, arg);
	}
}