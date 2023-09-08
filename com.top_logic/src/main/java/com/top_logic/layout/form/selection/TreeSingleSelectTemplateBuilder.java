/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;


/**
 * Algorithm for constructing the template of a single select dialog, which holds an option tree.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Stefan Steinert</a>
 */
public class TreeSingleSelectTemplateBuilder extends SelectTemplateBuilder {

	/**
	 * Creates a {@link SingleSelectTemplateBuilder}.
	 * 
	 * @param config
	 *        See {@link SelectTemplateBuilder#SelectTemplateBuilder(SelectDialogConfig, boolean)}.
	 */
	public TreeSingleSelectTemplateBuilder(SelectDialogConfig config) {
		super(config, false);
	}

	@Override
	public void appendContent() {
		layoutVerticalPercent(100);
		{
			appendHeadline(TreeSelectorContext.TITLE_FIELD_NAME);
			appendCaptions();
			appendFields();
		}
		layoutEnd();
	}

	private void appendCaptions() {
		layoutHorizontalPixel(20);
		{
			appendOptionLabel();
		}
		layoutEnd();
	}

	private void appendFields() {
		layoutHorizontalPercent(100);
		{
			appendOptionPane();
		}
		layoutEnd();
	}

	private void appendOptionPane() {
		layoutVerticalPercent(50);
		{
			appendOptionTree();
		}
		layoutEnd();
	}

	private void appendOptionLabel() {
		layoutVerticalPercent(50);
		{
			label(TreeSelectorContext.OPTIONS_FIELD_NAME);
		}
		layoutEnd();
	}

	private void appendOptionTree() {
		layoutVerticalPercent(50);
		{
			append("<div class='selectbox'>");
			field(TreeSelectorContext.OPTIONS_FIELD_NAME);
			append("</div>");
		}
		layoutEnd();
	}
}
