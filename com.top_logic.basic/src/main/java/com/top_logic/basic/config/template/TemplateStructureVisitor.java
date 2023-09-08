/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

import com.top_logic.basic.config.template.TemplateExpression.Foreach;
import com.top_logic.basic.config.template.TemplateExpression.Tag;
import com.top_logic.basic.config.template.TemplateExpression.Template;
import com.top_logic.basic.config.template.TemplateExpression.TemplateReference;

/**
 * Visitor interface for template structure {@link TemplateExpression}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TemplateStructureVisitor<R, A, E extends Throwable> {

	/**
	 * Visit case for {@link Foreach}.
	 */
	R visitForeach(Foreach expr, A arg) throws E;

	/**
	 * Visit case for {@link TemplateReference}.
	 */
	R vistTemplateReference(TemplateReference expr, A arg) throws E;

	/**
	 * Visit case for {@link Template}.
	 */
	R visitTemplate(Template expr, A arg) throws E;

	/**
	 * Visit case for {@link Tag}.
	 */
	R visitTag(Tag expr, A arg) throws E;

}
