/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

/**
 * {@link TemplateEnhancer} enhancing a regular tag to a conditional tag.
 */
public class ConditionBuilder implements TemplateEnhancer {

	private final TemplateExpression _test;

	/**
	 * Creates a {@link ConditionBuilder}.
	 */
	public ConditionBuilder(TemplateExpression test) {
		_test = test;
	}

	@Override
	public HTMLTemplateFragment build(HTMLTemplateFragment inner) {
		return new ConditionalTemplate(_test, inner, EmptyTemplate.INSTANCE);
	}

}
