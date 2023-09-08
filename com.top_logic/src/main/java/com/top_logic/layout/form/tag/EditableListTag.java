/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.awt.event.ActionListener;

import javax.servlet.jsp.tagext.Tag;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.EditableListControl;
import com.top_logic.layout.form.model.ListField;

/**
 * {@link Tag} creating {@link EditableListControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EditableListTag extends AbstractFormFieldControlTag {

	private ActionListener onEdit;
	
	public void setOnEdit(ActionListener onEdit) {
		this.onEdit = onEdit;
	}
	
	public ActionListener getOnEdit() {
		return onEdit;
	}
	
	@Override
	public Control createControl(FormMember selectField, String displayStyle) {
		EditableListControl control = new EditableListControl((ListField) selectField);
		control.setOnEdit(onEdit);
		return control;
	}

}
