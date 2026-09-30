/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.form.tag.TextInputTag;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.model.annotate.DisplayAnnotations;

/**
 * {@link DisplayProvider} for {@link String} attributes.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StringSetTagProvider extends IndirectDisplayProvider {

	/**
	 * Singleton {@link StringSetTagProvider} instance.
	 */
	public static final StringSetTagProvider INSTANCE = new StringSetTagProvider();

	private StringSetTagProvider() {
		// Singleton constructor.
	}

	@Override
	public ControlProvider getControlProvider(EditContext editContext) {
		TextInputTag result = new TextInputTag();
		result.setColumns(DisplayAnnotations.inputSize(editContext, TextInputTag.NO_COLUMNS));
		return result;
	}

}
