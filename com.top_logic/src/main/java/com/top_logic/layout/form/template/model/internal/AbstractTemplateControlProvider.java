/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model.internal;

import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link ControlProvider} holding an {@link HTMLTemplateFragment}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractTemplateControlProvider implements ControlProvider {

	private final HTMLTemplateFragment _template;

	/**
	 * Creates a new {@link AbstractTemplateControlProvider}.
	 * 
	 * @param template
	 *        See {@link #getTemplate()}
	 */
	public AbstractTemplateControlProvider(HTMLTemplateFragment template) {
		_template = template;
	}

	/**
	 * The rendering template.
	 */
	public HTMLTemplateFragment getTemplate() {
		return _template;
	}

}

