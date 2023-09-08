/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import static com.top_logic.layout.form.template.model.Templates.*;
import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.form.values.edit.TemplateProvider;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.ui.model.exec.ExpressionBuilder;

/**
 * {@link Operator} that holds for a attribute that has no value assigned.
 * 
 * @see NotEmpty
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(Empty.Display.class)
public interface Empty extends Operator<Empty.Impl> {

	/**
	 * {@link TemplateProvider} for {@link Empty} display.
	 */
	public class Display extends TemplateProvider {
		@Override
		public HTMLTemplateFragment get(ConfigurationItem model) {
			return span(resource(ResKey.forClass(Empty.Impl.class)));
		}
	}

	/**
	 * {@link Operator.Impl} for {@link Empty}.
	 */
	class Impl extends Operator.Impl<Empty> {

		/**
		 * Creates an {@link Impl}.
		 */
		public Impl(InstantiationContext context, Empty config) {
			super(context, config);
		}

		@Override
		public SearchExpression build(ExpressionBuilder builder, SearchExpression contextExpr) {
			return isEmpty(contextExpr);
		}
	}
}
