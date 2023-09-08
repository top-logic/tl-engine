/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.debuginfo;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.recorder.gui.FieldCopier;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;

/**
 * Shows the {@link FormField#getDefaultValue() default value} of the {@link FormField}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class FieldDefaultValueInfoPlugin extends DebugInfoPlugin<FormField> {

	public FieldDefaultValueInfoPlugin(FormField model) {
		super(model, I18NConstants.DEFAULT_VALUE, "defaultValue");
	}

	@Override
	protected FormMember createInformationField(String fieldName) {
		FormField defaultField = FieldCopier.copyField(getModel(), fieldName);
		if (defaultField == null) {
			throw new RuntimeException("Unable to copy such a field: " + getModel().getClass().getCanonicalName());
		}
		defaultField.initializeField(getModel().getDefaultValue());
		defaultField.setImmutable(true);
		return defaultField;
	}

}