/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.scripting.action.FormAction;
import com.top_logic.layout.scripting.action.FormRawInput;

/**
 * {@link FieldActionOp} setting a value to a {@link FormField}.
 * 
 * @deprecated Use {@link FormRawInput}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public class FormRawInputOp extends FieldActionOp {

	public FormRawInputOp(InstantiationContext context, FormAction config) {
		super(context, config);
	}

	@Override
	protected void processField(FormField field, Object value) {
		try {
			FormFieldInternals.updateField((AbstractFormField) field, value);
		} catch (VetoException ex) {
			// No user interaction.

			ApplicationAssertions.fail(getConfig(), "Field update failed with veto.", ex);
		}
	}

}
