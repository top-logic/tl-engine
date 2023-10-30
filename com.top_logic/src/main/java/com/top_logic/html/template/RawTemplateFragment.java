/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import com.top_logic.html.template.expr.StringLiteral;
import com.top_logic.html.template.parser.HTMLTemplateParser;

/**
 * {@link HTMLTemplateFragment} that can be directly parsed from HTML with embedded expressions.
 * 
 * @see HTMLTemplateParser
 */
public interface RawTemplateFragment extends HTMLTemplateFragment {

	/**
	 * Visit interface for the {@link RawTemplateFragment} hierarchy.
	 */
	interface Visitor<R, A> {
		/**
		 * Visit case for {@link StartTagTemplate}.
		 */
		R visit(StartTagTemplate template, A arg);

		/**
		 * Visit case for {@link StartTagTemplate}.
		 */
		R visit(SpecialStartTag template, A arg);

		/**
		 * Visit case for {@link TagAttributeTemplate}.
		 */
		R visit(TagAttributeTemplate template, A arg);

		/**
		 * Visit case for {@link EndTagTemplate}.
		 */
		R visit(EndTagTemplate template, A arg);

		/**
		 * Visit case for {@link TemplateSequence}.
		 */
		R visit(TemplateSequence template, A arg);

		/**
		 * Visit case for {@link StringLiteral}.
		 */
		R visit(StringLiteral template, A arg);

		/**
		 * Visit case for {@link EmptyTemplate}.
		 */
		R visit(EmptyTemplate template, A arg);

		/**
		 * Visit case for {@link ExpressionTemplate}.
		 */
		R visit(ExpressionTemplate template, A arg);

		/**
		 * Visit case for {@link VariableTemplate}.
		 */
		R visit(VariableTemplate template, A arg);

		/**
		 * Visit case for {@link ConditionalTemplate}.
		 */
		R visit(ConditionalTemplate template, A arg);

		/**
		 * Visit case for {@link ForeachTemplate}.
		 */
		R visit(ForeachTemplate foreachTemplate, A arg);

		/**
		 * Visit case for {@link DefineTemplate}.
		 */
		R visit(DefineTemplate defineTemplate, A arg);
	}

	/**
	 * Visit method for the {@link RawTemplateFragment} hierarchy.
	 */
	<R, A> R visit(Visitor<R, A> visitor, A arg);

}
