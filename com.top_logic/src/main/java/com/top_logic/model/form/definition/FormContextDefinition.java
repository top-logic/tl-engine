/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.definition;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Context of a {@link FormDefinition} created by the form editor.
 */
@Abstract
public interface FormContextDefinition extends ConfigurationItem {

	/**
	 * @see #getFormContextType()
	 */
	public static final String FORM_CONTEXT_TYPE = "form-context-type";

	/**
	 * The type of the model object that is being displayed in this form.
	 */
	@Hidden
	@Abstract
	@Name(FORM_CONTEXT_TYPE)
	TLModelPartRef getFormContextType();

}
