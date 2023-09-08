/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.util.Date;

import javax.servlet.jsp.JspException;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.DateInputControl;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.tag.util.IntAttribute;

/**
 * View of a {@link ComplexField} with a {@link Date} format rendered as input
 * field with calendar button.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DateTag extends AbstractFormFieldControlTag {
	

	public final IntAttribute columns = new IntAttribute();
	
	public void setColumns(int value) {
		this.columns.setAsInt(value);
	}

	@Override
	protected void setup() throws JspException {
		super.setup();

		if (! columns.isSet()) {
			columns.setAsInt(DateInputControl.DEFAULT_COLUMN_SIZE);
		}
	}

	@Override
	protected void teardown() {
		super.teardown();

		columns.reset();
	}

	@Override
	public Control createControl(FormMember member, String displayStyle) {
		DateInputControl result = new DateInputControl((ComplexField) member);
		
		result.setColumns(columns.get());
		if (style.isSet()) result.setInputStyle(style.get());
		if (tabindex.isSet()) result.setTabIndex(tabindex.get());
		
		return result;
	}
}
