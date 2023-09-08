/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.template.TemplateBuilder;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Base class for algorithms constructing object search dialogs.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class SearchTemplateBuilder extends TemplateBuilder {

	private static final String TITLE_FIELD_NAME = "searchTLObjectTitle";

	private static final String BUTTONS_GROUP_NAME = "buttons";

	private static final String OPTIONS_FIELD_NAME = "tlObjectOptions";

	private static final String SEARCH_FIELD_NAME = "searchTLObject";

	private static final String TITLE_FIELD_STYLE =
		"<div style='font-weight:bold; font-size:20px; padding-top: 15px; padding-bottom: 10px;'><p:field name='%s' style='label' /></div>";

	@Override
	protected void internalBuild() {
		layoutBeginBegin();
		verticalPercent(100);
		appendNameSpaces();
		append(" style='width:100%;height:100%'");
		append(">");
		appendHeadline(TITLE_FIELD_NAME);
		appendContentPane();
		appendButtonBar();
		layoutEnd();
	}

	private void appendContentPane() {
		layoutHorizontalPercent(100);
		appendPaddingSpacer();
		layoutVerticalPercent(100);
		appendPaddingSpacer();
		appendContent();
		appendPaddingSpacer();
		layoutEnd();
		appendPaddingSpacer();
		layoutEnd();
	}

	private void appendContent() {
		layoutVerticalPixel(60);
		appendSearchCaption();
		appendSearchField();
		layoutEnd();

		layoutVerticalPercent(100);
		appendOptionCaption();
		appendOptionField();
		layoutEnd();
	}

	private void appendSearchCaption() {
		layoutHorizontalPixel(20);
		appendPatternLabel();
		layoutEnd();
	}

	private void appendSearchField() {
		layoutHorizontalPercent(100);
		appendPatternField();
		layoutEnd();
	}

	private void appendOptionCaption() {
		layoutHorizontalPixel(20);
		appendOptionLabel();
		layoutEnd();
	}

	private void appendOptionField() {
		layoutHorizontalPercent(100);
		appendOptionList();
		layoutEnd();
	}

	private void appendOptionLabel() {
		label(OPTIONS_FIELD_NAME);
		space();
		error(OPTIONS_FIELD_NAME);
	}

	private void appendOptionList() {
		field(OPTIONS_FIELD_NAME);
	}

	private void appendPatternLabel() {
		label(SEARCH_FIELD_NAME);
		space();
		error(SEARCH_FIELD_NAME);
	}

	private void appendPatternField() {
		append("<form action='#' onsubmit='return false;'>");
		field(SEARCH_FIELD_NAME);
		append("</form>");
	}

	private void appendPaddingSpacer() {
		appendSpacerPixel(8);
	}

	private void appendButtonBar() {
		DisplayDimension buttonBarHeight =
			ThemeFactory.getTheme().getValue(com.top_logic.layout.Icons.BUTTON_COMP_HEIGHT);

		layoutBeginBegin();
		horizontalDimension(buttonBarHeight);
		complexWidget();
		layoutEndBegin();
		append("<p:field name='" + BUTTONS_GROUP_NAME + "'/>");

		append("<script type='text/javascript'>");
		append("focusFirst();");
		append("</script>");
		layoutEnd();
	}

	/**
	 * @param spacerPixel
	 *        Space amount in pixel.
	 */
	protected final void appendSpacerPixel(int spacerPixel) {
		layoutHorizontalPixel(spacerPixel);
		layoutEnd();
	}

	/**
	 * @param spacerPercent
	 *        Space amount in percent.
	 */
	protected final void appendSpacerPercent(int spacerPercent) {
		layoutHorizontalPercent(spacerPercent);
		layoutEnd();
	}

	/**
	 * Appends full headline of dialog.
	 */
	protected final void appendHeadline(String fieldName) {
		layoutBeginBegin();
		verticalPixel(60);
		append(" style='");
		append(HTMLConstants.TEXT_ALIGN_CENTER);
		append("'");
		layoutEndBegin();
		{
			append(String.format(TITLE_FIELD_STYLE, fieldName));
		}
		layoutEnd();
	}
}
