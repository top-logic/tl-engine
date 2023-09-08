/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.IconSelectControl;
import com.top_logic.layout.form.control.IconSelectControl.DefaultBooleanTristateResourceProvider;
import com.top_logic.layout.form.model.BooleanField;

/**
 * View of a {@link BooleanField} rendered as a checkbox with 3 possible states:
 * "checked", "unchecked", and "not decided".
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TristateCheckboxTag extends AbstractFormFieldControlTag {

	private boolean resettable = true;
	
	public void setResettable(boolean resettable) {
		this.resettable = resettable;
	}
	
	@Override
	public Control createControl(FormMember member, String displayStyle) {
		IconSelectControl result = new IconSelectControl((FormField) member, DefaultBooleanTristateResourceProvider.INSTANCE);
		result.setResetable(resettable);
		return result;
	}
	
	@Override
	protected void teardown() {
		this.resettable = false;
		super.teardown();
	}
	
}
