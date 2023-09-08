/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import com.top_logic.layout.form.model.SelectField;

/**
 * Algorithm for constructing a multi select dialog.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MultiSelectTemplateBuilder extends SelectTemplateBuilder {

	private final boolean _customOrder;

	/**
	 * Creates a {@link MultiSelectTemplateBuilder}.
	 * 
	 * @param config
	 *        See {@link SelectTemplateBuilder#SelectTemplateBuilder(SelectDialogConfig, boolean)}.
	 * @param isLarge
	 *        See {@link SelectTemplateBuilder#SelectTemplateBuilder(SelectDialogConfig, boolean)}.
	 * @param customOrder
	 *        See {@link SelectField#hasCustomOrder()}.
	 */
	public MultiSelectTemplateBuilder(SelectDialogConfig config, boolean isLarge, boolean customOrder) {
		super(config, isLarge);

		_customOrder = customOrder;
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
				appendOptionCaption();
				appendCaptionSpacer();
				appendSelectionCaption();
			} else {
				appendSelectionCaption();
				appendCaptionSpacer();
				appendOptionCaption();
			}
		}
		layoutEnd();
	}

	private void appendOptionCaption() {
		if (showOptions()) {
			layoutVerticalPercent(50);
			{
				label(SelectorContext.PATTERN_FIELD_NAME);
				space();
				error(SelectorContext.PATTERN_FIELD_NAME);
			}
			layoutEnd();
		}
	}

	private void appendSelectionCaption() {
		layoutVerticalPercent(50);
		{
			label(SelectorContext.SELECTION_FIELD_NAME);
			space();
			error(SelectorContext.SELECTION_FIELD_NAME);
		}
		layoutEnd();
	}

	private void appendFields() {
		layoutHorizontalPercent(100);
		{
			if (isLeftToRight()) {
				appendOptionPane();
				appendModifyButtons();
				appendSelectionList();
			} else {
				appendSelectionList();
				appendModifyButtons();
				appendOptionPane();
			}
		}
		layoutEnd();
	}

	private void appendOptionPane() {
		if (showOptions()) {
			layoutVerticalPercent(50);
			{
				appendPatternField();
				appendOptionLabel();
				appendPageField();
				appendOptionList();
			}
			layoutEnd();
		}
	}

	private void appendPatternField() {
		layoutVerticalPixel(25);
		{
			// Anchor for selectFirst()
			append("<form action='#' onsubmit='return false;'>");
			field(SelectorContext.PATTERN_FIELD_NAME);
			append("</form>");
		}
		layoutEnd();
	}

	private void appendOptionLabel() {
		layoutVerticalPixel(20);
		{
			label(SelectorContext.OPTIONS_FIELD_NAME);
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

	private void appendOptionList() {
		layoutVerticalPercent(100);
		{
			field(SelectorContext.OPTIONS_FIELD_NAME);
		}
		layoutEnd();
	}

	private void appendSelectionList() {
		layoutVerticalPercent(50);
		{
			field(SelectorContext.SELECTION_FIELD_NAME);
		}
		layoutEnd();
	}

	private void appendModifyButtons() {
		appendSpacerPixel(10);
		layoutBeginBegin();
		complexWidget();
		verticalPixel(80);
		layoutEndBegin();
		{
			appendListButtonSpacer();

			if (showOptions()) {
				appendAddButton();
				appendAddAllButton();
				appendRemoveButton();
				appendRemoveAllButton();
			}
			appendOrderButtons();

			appendListButtonSpacer();
		}
		layoutEnd();
		appendSpacerPixel(10);
	}

	private void appendAddAllButton() {
		if (!isLarge()) {
			appendButton(SelectorContext.ADD_ALL_TO_SELECTION);
		}
	}

	private void appendAddButton() {
		appendButton(SelectorContext.ADD_TO_SELECTION);
	}

	private void appendRemoveButton() {
		appendButton(SelectorContext.REMOVE_FROM_SELECTION);
	}

	private void appendRemoveAllButton() {
		appendButton(SelectorContext.REMOVE_ALL_FROM_SELECTION);
	}

	private void appendOrderButtons() {
		if (_customOrder) {
			appendButton(SelectorContext.MOVE_UP);
			appendButton(SelectorContext.MOVE_DOWN);
		}
	}

	private void appendButton(String name) {
		layoutVerticalPixel(23);
		{
			field(name);
		}
		layoutEnd();
	}

	private void appendCaptionSpacer() {
		appendSpacerPixel(100);
	}

	private void appendListButtonSpacer() {
		appendSpacerPercent(5);
	}
}
