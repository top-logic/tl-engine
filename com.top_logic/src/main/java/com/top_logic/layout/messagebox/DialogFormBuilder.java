/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import java.util.function.Consumer;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.values.edit.FormBuilder;

/**
 * Algorithm building the contents of a form based on a {@link ConfigurationItem} model.
 * 
 * @see FormBuilder
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface DialogFormBuilder<C extends ConfigurationItem> {

	/**
	 * Build custom {@link FormContainer} contents based on the given {@link ConfigurationItem}
	 * model.
	 * 
	 * @param form
	 *        The {@link FormContainer} to create additional members in.
	 * @param model
	 *        The model element, the given {@link FormContainer} is built from.
	 * 
	 * @return A callback that is used with <code>model</code> as argument to finish creation of the
	 *         model. The result is typically used to update the given <code>model</code> when this
	 *         builder creates {@link FormField} that do not reflect a property of the
	 *         <code>model</code> directly.
	 */
	Consumer<C> initForm(FormContainer form, C model);

}
