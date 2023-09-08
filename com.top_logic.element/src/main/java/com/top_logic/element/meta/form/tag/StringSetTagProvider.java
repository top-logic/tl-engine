/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.element.meta.AttributeUpdateFactory;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.form.tag.PopupSelectTag;
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
		if (!editContext.isSearchUpdate() && AttributeUpdateFactory.isRestricted(editContext)) {
			PopupSelectTag result = new PopupSelectTag();

			result.setColumns(DisplayAnnotations.inputSize(editContext, PopupSelectTag.NO_COLUMNS));
			result.clearButton.setAsBoolean(!editContext.isMandatory());
			return result;
		} else {
			TextInputTag result = new TextInputTag();
			result.setColumns(DisplayAnnotations.inputSize(editContext, TextInputTag.NO_COLUMNS));
			return result;
		}
	}

}
