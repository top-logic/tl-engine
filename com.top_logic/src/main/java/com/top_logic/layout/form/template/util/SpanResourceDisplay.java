/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.util;

import static com.top_logic.layout.form.template.model.Templates.*;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.form.template.model.ResourceExpr;
import com.top_logic.layout.form.values.edit.TemplateProvider;

/**
 * {@link TemplateProvider} that uses {@link ResourceExpr} template with the model type as resource
 * key.
 */
public class SpanResourceDisplay extends TemplateProvider {

	/**
	 * Singleton {@link SpanResourceDisplay} instance.
	 */
	public static final SpanResourceDisplay INSTANCE = new SpanResourceDisplay();

	private SpanResourceDisplay() {
		// Singleton constructor.
	}

	@Override
	public HTMLTemplateFragment get(ConfigurationItem model) {
		return span(resourceExpr(ResKey.forConfig(model)));
	}
}