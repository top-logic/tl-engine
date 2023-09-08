/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.Country;

/**
 * This provider takes care of delivering the options (i.e. the possible values) for a
 * {@link TLStructuredTypePart} of custom data type.
 * 
 * @see ListOptionProvider OptionProvider based on a list of options.
 * 
 * @author <a href="mailto:jco@top-logic.com">jco</a>
 */
public interface OptionProvider {
    
    /**
	 * Deliver the possible values for a complex attribute. For example the {@link OptionProvider}
	 * for countries would deliver an {@link OptionModel} containing {@link Country}s.
	 * 
	 * @param editContext
	 *        The current {@link AttributeUpdate} for which the provider must deliver an
	 *        {@link OptionModel}.
	 * 
	 * @return The {@link OptionModel} of all possible values.
	 */
	OptionModel<?> getOptions(EditContext editContext);

}

