/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

/**
 * {@link SelectTemplateBuilder} to construct a table based selector dialog.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TableSelectTemplateBuilder extends SelectTemplateBuilder {

	/**
	 * Create a new {@link TableSelectTemplateBuilder}
	 */
	public TableSelectTemplateBuilder(SelectDialogConfig config) {
		super(config, false);
	}

	@Override
	protected void appendContent() {
		layoutVerticalPercent(100);
		{
			appendHeadline(SelectorContext.TITLE_FIELD_NAME);
			appendTableField();
		}
		layoutEnd();
	}

	private void appendTableField() {
		layoutHorizontalPercent(100);
		{
			field(TableSelectorContext.SELECTION_TABLE);
		}
		layoutEnd();
	}

}
