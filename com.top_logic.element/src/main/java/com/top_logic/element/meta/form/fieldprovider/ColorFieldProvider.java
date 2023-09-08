/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import java.awt.Color;

import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.ColorControlProvider;
import com.top_logic.layout.form.format.ColorFormat;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;

/**
 * {@link FieldProvider} for {@link Color} fields.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ColorFieldProvider extends AbstractFieldProvider {

	/**
	 * Singleton {@link ColorFieldProvider} instance.
	 */
	public static final ColorFieldProvider INSTANCE = new ColorFieldProvider();

	private ColorFieldProvider() {
		// Singleton constructor.
	}

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {
		ComplexField result = FormFactory.newComplexField(fieldName, ColorFormat.INSTANCE);
		result.setControlProvider(ColorControlProvider.INSTANCE);
		return result;
	}

}
