/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import com.top_logic.html.template.RawTemplateFragment.Visitor;
import com.top_logic.html.template.expr.StringLiteral;

/**
 * {@link Visitor} descending a template tree.
 */
public class DescendingTemplateVisitor<A> implements RawTemplateFragment.Visitor<Void, A> {

	@Override
	public Void visit(StartTagTemplate template, A arg) {
		for (TagAttributeTemplate attr : template.getAttributes()) {
			descend(attr, arg);
		}
		return null;
	}

	@Override
	public Void visit(SpecialStartTag template, A arg) {
		for (TagAttributeTemplate attr : template.getAttributes()) {
			descend(attr, arg);
		}
		return null;
	}

	@Override
	public Void visit(TagAttributeTemplate template, A arg) {
		descend(template.getContent(), arg);
		return null;
	}

	@Override
	public Void visit(EndTagTemplate template, A arg) {
		return null;
	}

	@Override
	public Void visit(TemplateSequence template, A arg) {
		for (HTMLTemplateFragment content : template.getContents()) {
			descend(content, arg);
		}
		return null;
	}

	@Override
	public Void visit(StringLiteral template, A arg) {
		return null;
	}

	@Override
	public Void visit(EmptyTemplate template, A arg) {
		return null;
	}

	@Override
	public Void visit(ExpressionTemplate template, A arg) {
		return null;
	}

	@Override
	public Void visit(VariableTemplate template, A arg) {
		return null;
	}

	@Override
	public Void visit(ConditionalTemplate template, A arg) {
		descend(template.getThenFragment(), arg);
		descend(template.getElseFragment(), arg);
		return null;
	}

	@Override
	public Void visit(ForeachTemplate template, A arg) {
		descend(template.getContent(), arg);
		return null;
	}

	@Override
	public Void visit(DefineTemplate template, A arg) {
		descend(template.getContent(), arg);
		return null;
	}

	/**
	 * Visits the given fragment.
	 */
	protected void descend(HTMLTemplateFragment template, A arg) {
		if (template instanceof RawTemplateFragment) {
			((RawTemplateFragment) template).visit(this, arg);
		}
	}

}
