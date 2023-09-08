/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import java.util.Date;

import com.top_logic.basic.func.Identity;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.DateTimeField;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;

/**
 * {@link Editor} for displaying {@link Date} properties with date and time.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DateTimeEditor extends AbstractEditor {

	@Override
	protected FormField addField(EditorFactory editorFactory, FormContainer container, ValueModel model,
			String fieldName) {
		DateTimeField field =
			new DateTimeField(fieldName, (Date) model.getValue(), false);
		container.addMember(field);

		init(editorFactory, model, field, Identity.getInstance(), Identity.getInstance());

		return field;
	}

}