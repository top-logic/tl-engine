/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;

/**
 * Provider of a {@link HTMLTemplateFragment} for rendering {@link ConfigurationItem}s.
 * 
 * @see UseTemplate
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TemplateProvider {

	/**
	 * Constructs the template for rendering the annotated model.
	 * 
	 * @param model
	 *        The model instance to get the template for.
	 */
	public abstract HTMLTemplateFragment get(ConfigurationItem model);

}
