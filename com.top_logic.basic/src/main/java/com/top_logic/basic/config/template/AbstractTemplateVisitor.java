/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

import com.top_logic.basic.config.template.TemplateExpression.Foreach;
import com.top_logic.basic.config.template.TemplateExpression.Template;
import com.top_logic.basic.config.template.TemplateExpression.TemplateReference;
import com.top_logic.basic.config.template.TemplateExpression.TemplateStructure;

/**
 * Adapter implementation for {@link TemplateVisitor}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTemplateVisitor<R, A, E extends Throwable> implements TemplateVisitor<R, A, E> {

	@Override
	public R visitForeach(Foreach expr, A arg) throws E {
		return visitTemplateStructure(expr, arg);
	}

	@Override
	public R vistTemplateReference(TemplateReference expr, A arg) throws E {
		return visitTemplateStructure(expr, arg);
	}

	@Override
	public R visitTemplate(Template expr, A arg) throws E {
		return visitTemplateStructure(expr, arg);
	}

	/**
	 * Catch-all clause for {@link TemplateStructure} of any type.
	 * 
	 * @param expr
	 *        The visited expression.
	 * @param arg
	 *        The argument to the visit.
	 * @return The visit result.
	 */
	protected abstract R visitTemplateStructure(TemplateStructure expr, A arg) throws E;

}
