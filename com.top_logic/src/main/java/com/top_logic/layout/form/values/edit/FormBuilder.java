/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.form.FormContainer;

/**
 * Algorithm building the contents of a form based on a {@link ConfigurationItem} model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FormBuilder<C extends ConfigurationItem> {

	/**
	 * Build custom {@link FormContainer} contents based on the given {@link ConfigurationItem}
	 * model.
	 * 
	 * @param form
	 *        The {@link FormContainer} to create additional members in.
	 * @param model
	 *        The model element, the given {@link FormContainer} is built from.
	 */
	void initForm(FormContainer form, C model);

}
