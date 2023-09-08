/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model;

import com.top_logic.html.template.HTMLTemplateFragment;

/**
 * Template for some model that has its own {@link #getTemplate()} assigned.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractView implements Template {
	private final HTMLTemplateFragment _template;

	AbstractView(HTMLTemplateFragment template) {
		_template = template;
	}

	/**
	 * The template for the referenced model element.
	 */
	public HTMLTemplateFragment getTemplate() {
		return _template;
	}

}