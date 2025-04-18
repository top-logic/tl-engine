/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.Comparator;

import com.top_logic.element.config.annotation.TLOptions;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Option provider for {@link TLStructuredTypePart}s.
 * 
 * @see TLOptions
 */
public interface Generator {
    
    /**
	 * Generates a list of options to select from for the attribute being edited.
	 * 
	 * @param editContext
	 *        The current editing context (the abstraction for the attribute being edited).
	 * @return The available options.
	 * 
	 * @see AttributeUpdate#getAttribute()
	 * @see AttributeUpdate#getObject()
	 */
	OptionModel<?> generate(EditContext editContext);

	/**
	 * The order in which the {@link #generate(EditContext) options} must be displayed.
	 * 
	 * @return May be <code>null</code>. In that case the order of the options at the UI is
	 *         unspecified.
	 */
	default Comparator<?> getOptionOrder() {
		return null;
	}

}