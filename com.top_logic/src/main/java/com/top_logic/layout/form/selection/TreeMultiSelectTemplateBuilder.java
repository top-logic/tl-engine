/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;


/**
 * {@link SelectTemplateBuilder} to construct a tree selector dialog, which supports multiple
 * selections.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeMultiSelectTemplateBuilder extends SelectTemplateBuilder {

	public TreeMultiSelectTemplateBuilder(SelectDialogConfig config) {
		super(config, !config.getAllowTreeAddAll());
	}

	@Override
	protected void appendContent() {
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
			if (isLeftToRight()) {
				appendOptionLabel();
				appendCaptionSpacer();
				appendSelectionLabel();
			} else {
				appendSelectionLabel();
				appendCaptionSpacer();
				appendOptionLabel();
			}
		}
		layoutEnd();
	}

	private void appendFields() {
		layoutHorizontalPercent(100);
		{
			if (isLeftToRight()) {
				appendOptions();
				appendButtons();
				appendSelection();
			} else {
				appendSelection();
				appendButtons();
				appendOptions();
			}
		}
		layoutEnd();
	}

	private void appendSelectionLabel() {
		appendFieldLabel(TreeSelectorContext.SELECTION_FIELD_NAME);
	}

	private void appendFieldLabel(String fieldId) {
		layoutVerticalPercent(50);
		{
			label(fieldId);
		}
		layoutEnd();
	}

	/**
	 * Add the view of the current selection to the given
	 */
	private void appendSelection() {
		layoutVerticalPercent(50);
		{
			field(TreeSelectorContext.SELECTION_FIELD_NAME);
		}
		layoutEnd();
	}

	/**
	 * Add the view of the buttons to the given
	 */
	private void appendButtons() {
		appendSpacerPixel(10);
		layoutVerticalPixel(80);
		{
			appendButtonSpacer();

			appendButton(TreeSelectorContext.ADD_TO_SELECTION);

			if (!this.isLarge()) {
				appendButton(TreeSelectorContext.ADD_ALL_TO_SELECTION);
			}

			appendButton(TreeSelectorContext.REMOVE_ALL_FROM_SELECTION);
			appendButton(TreeSelectorContext.REMOVE_FROM_SELECTION);

			appendButtonSpacer();
		}
		layoutEnd();
		appendSpacerPixel(10);
	}

	private void appendButton(String buttonId) {
		layoutVerticalPixel(23);
		{
			field(buttonId);
		}
		layoutEnd();
	}

	private void appendOptionLabel() {
		appendFieldLabel(TreeSelectorContext.OPTIONS_FIELD_NAME);
	}

	/**
	 * Add the view of the options to the given
	 */
	private void appendOptions() {
		layoutVerticalPercent(50);
		{
			append("<div class='selectbox'>");
			{
				field(TreeSelectorContext.OPTIONS_FIELD_NAME);
			}
			append("</div>");
		}
		layoutEnd();
	}

	private void appendCaptionSpacer() {
		appendSpacerPixel(100);
	}

	private void appendButtonSpacer() {
		appendSpacerPercent(5);
	}
}
