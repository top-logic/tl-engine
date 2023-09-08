/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import com.top_logic.layout.form.values.edit.OptionMapping;

/**
 * {@link OptionMapping} transforming {@link DynamicComponentDefinition} options to
 * {@link LayoutTemplate} values.
 * 
 * @see DynamicComponentOptionMapping
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DynamicComponentOptionMapping implements OptionMapping {

	@Override
	public Object toSelection(Object option) {
		if (option == null) {
			return null;
		}
		return ((DynamicComponentDefinition) option).createTemplate();
	}

	@Override
	public Object asOption(Iterable<?> allOptions, Object selection) {
		if (selection == null) {
			return null;
		}
		LayoutTemplate selectedTemplate = (LayoutTemplate) selection;
		return DynamicComponentService.getInstance().getComponentDefinition(selectedTemplate.getTemplatePath());
	}


}

