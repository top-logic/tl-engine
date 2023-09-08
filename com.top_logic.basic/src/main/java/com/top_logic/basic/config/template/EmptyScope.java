/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;


/**
 * {@link TemplateScope} with no templates defined.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EmptyScope implements TemplateScope {

	/**
	 * Singleton {@link EmptyScope} instance.
	 */
	public static final EmptyScope INSTANCE = new EmptyScope();

	private EmptyScope() {
		// Singleton constructor.
	}

	@Override
	public TemplateExpression getTemplate(String name, boolean optional) {
		return null;
	}

}
