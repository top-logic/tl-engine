/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.util;

import static com.top_logic.layout.form.template.model.Templates.*;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.values.edit.TemplateProvider;

/**
 * {@link TemplateProvider} rendering a {@link FormContainer} inline.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SpanGroup extends TemplateProvider {

	/**
	 * Singleton {@link SpanGroup} instance.
	 */
	public static final SpanGroup INSTANCE = new SpanGroup();

	private SpanGroup() {
		// Singleton constructor.
	}

	@Override
	public HTMLTemplateFragment get(ConfigurationItem model) {
		return span(items(self()));
	}

}
