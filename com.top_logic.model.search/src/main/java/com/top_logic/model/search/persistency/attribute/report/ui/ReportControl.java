/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.persistency.attribute.report.ui;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.AbstractFormFieldControlBase;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.persistency.attribute.macro.ui.MacroDisplayControl;

/**
 * {@link Control} displaying a {@link FormField} with a {@link SearchExpression} value as source
 * code input field in edit mode and as macro expansion in view mode.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ReportControl extends AbstractFormFieldControlBase {

	private final TLObject _self;

	private int _rows = 10;

	/**
	 * Creates a {@link ReportControl}.
	 */
	public ReportControl(TLObject self, FormField member) {
		super(member);
		_self = self;
	}

	/**
	 * The number of rows to display in edit mode.
	 */
	public int getRows() {
		return _rows;
	}

	/**
	 * @see #getRows()
	 */
	public void setRows(int rows) {
		_rows = rows;
		requestRepaint();
	}

	@Override
	protected String getTypeCssClass() {
		return "cReport";
	}

	@Override
	protected void doInternalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			writeContents(context, out);
		}
		out.endTag(DIV);
	}

	private void writeContents(DisplayContext context, TagWriter out) throws IOException {
		FormField model = getFieldModel();
		if (model.isImmutable()) {
			new MacroDisplayControl(_self, model).write(context, out);
		} else {
			writeEditable(context, out);
		}
	}

	/**
	 * The view to edit the report definition.
	 */
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		FormField model = getFieldModel();
		TextInputControl result = new TextInputControl(model);
		result.setMultiLine(true);
		result.setRows(_rows);
		result.write(context, out);
	}

	@Override
	protected void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		// Ignore, content control cares about disabled state.
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		// Ignore, content control cares about value.
	}
}
