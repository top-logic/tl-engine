/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;


/**
 * Algorithm for constructing the template of a single select dialog.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SingleSelectTemplateBuilder extends SelectTemplateBuilder {

	/**
	 * Creates a {@link SingleSelectTemplateBuilder}.
	 * 
	 * @param config
	 *        See {@link SelectTemplateBuilder#SelectTemplateBuilder(SelectDialogConfig, boolean)}.
	 * @param isLarge
	 *        See {@link SelectTemplateBuilder#SelectTemplateBuilder(SelectDialogConfig, boolean)}.
	 */
	public SingleSelectTemplateBuilder(SelectDialogConfig config, boolean isLarge) {
		super(config, isLarge);
	}

	@Override
	public void appendContent() {
		layoutVerticalPercent(100);
		{
			appendHeadline(SelectorContext.TITLE_FIELD_NAME);
			appendCaptions();
			appendFields();
		}
		layoutEnd();
	}

	private void appendCaptions() {
		layoutHorizontalPixel(20);
		{
			if (isLeftToRight()) {
				appendOptionLabel();
				appendSpacer();
				appendPatternLabel();
			} else {
				appendPatternLabel();
				appendSpacer();
				appendOptionLabel();
			}
		}
		layoutEnd();
	}

	private void appendFields() {
		layoutHorizontalPercent(100);
		{
			if (isLeftToRight()) {
				appendOptionPane();
				appendSpacer();
				appendPatternField();
			} else {
				appendPatternField();
				appendSpacer();
				appendOptionPane();
			}
		}
		layoutEnd();
	}

	private void appendOptionPane() {
		layoutVerticalPercent(50);
		{
			appendPageField();
			appendOptionList();
		}
		layoutEnd();
	}

	private void appendOptionLabel() {
		layoutVerticalPercent(50);
		{
			label(SelectorContext.OPTIONS_FIELD_NAME);
			space();
			error(SelectorContext.OPTIONS_FIELD_NAME);
		}
		layoutEnd();
	}

	private void appendPatternLabel() {
		layoutVerticalPercent(50);
		{
			label(SelectorContext.PATTERN_FIELD_NAME);

			space();
			error(SelectorContext.PATTERN_FIELD_NAME);
		}
		layoutEnd();
	}

	private void appendOptionList() {
		layoutHorizontalPercent(100);
		{
			field(SelectorContext.OPTIONS_FIELD_NAME);
		}
		layoutEnd();
	}

	private void appendPageField() {
		if (isLarge()) {
			layoutVerticalPixel(25);
			{
				field(SelectorContext.PAGE_FIELD_NAME);
			}
			layoutEnd();
		}
	}

	private void appendSpacer() {
		appendSpacerPixel(10);
	}

	private void appendPatternField() {
		layoutVerticalPercent(50);
		{
			// Anchor for selectFirst()
			append("<form action='#' onsubmit='return false;'>");
			field(SelectorContext.PATTERN_FIELD_NAME);
			append("</form>");
		}
		layoutEnd();
	}

}
