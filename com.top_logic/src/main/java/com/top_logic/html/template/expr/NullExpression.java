/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.expr;

import com.top_logic.html.template.EmptyTemplate;
import com.top_logic.html.template.RawTemplateFragment;
import com.top_logic.html.template.TemplateExpression;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.WithProperties;

/**
 * {@link TemplateExpression} for the <code>null</code> literal.
 */
public class NullExpression implements TemplateExpression {

	/**
	 * Singleton {@link NullExpression} instance.
	 */
	public static final NullExpression INSTANCE = new NullExpression();

	private NullExpression() {
		// Singleton constructor.
	}

	@Override
	public Object eval(DisplayContext context, WithProperties properties) {
		return null;
	}

	@Override
	public RawTemplateFragment toFragment() {
		return EmptyTemplate.INSTANCE;
	}

}
