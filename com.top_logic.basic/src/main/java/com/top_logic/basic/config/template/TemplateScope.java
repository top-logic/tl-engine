/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

/**
 * Resolver for template references.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TemplateScope {

	/**
	 * Lookup a {@link TemplateExpression} with the given name.
	 * 
	 * @param name
	 *        The name of the template to resolve.
	 * @param optional
	 *        Whether the caller can legally handle a missing template.
	 * @return The parsed template, or <code>null</code>, if no template was found.
	 */
	TemplateExpression getTemplate(String name, boolean optional);

}