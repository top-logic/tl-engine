/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.ChecklistControl;
import com.top_logic.layout.form.model.SelectField;

/**
 * Tag adding a {@link ChecklistControl} to a page.
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ChecklistTag extends AbstractFormFieldControlTag {

	public ChecklistTag() {
		// nothing special here
	}
    
	@Override
	public Control createControl(FormMember selectField, String displayStyle) {
		return new ChecklistControl((SelectField) selectField);
	}
    
}
