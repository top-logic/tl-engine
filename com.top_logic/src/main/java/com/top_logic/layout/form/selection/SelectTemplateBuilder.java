/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.template.TemplateBuilder;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Base class for algorithms constructing select dialogs.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class SelectTemplateBuilder extends TemplateBuilder {

	private final SelectDialogConfig _config;

	private final boolean _isLarge;

	/**
	 * Creates a {@link SelectTemplateBuilder}.
	 * 
	 * @param config
	 *        Configuration of the dialog to generate.
	 * @param isLarge
	 *        See {@link #isLarge()}.
	 */
	public SelectTemplateBuilder(SelectDialogConfig config, boolean isLarge) {
		super();
		_config = config;
		_isLarge = isLarge;
	}

	/**
	 * @see SelectDialogConfig#isLeftToRight()
	 */
	protected final boolean isLeftToRight() {
		return _config.isLeftToRight();
	}

	/**
	 * @see SelectDialogConfig#getShowOptions()
	 */
	protected final boolean showOptions() {
		return _config.getShowOptions();
	}

	/**
	 * Whether variant for large option size should be generated.
	 */
	protected final boolean isLarge() {
		return _isLarge;
	}

	@Override
	protected void internalBuild() {
		layoutBeginBegin();
		verticalPercent(100);
		appendNameSpaces();
		append(" style='width:100%;height:100%'");
		append(">");
		{
			appendContentPane();
			appendButtonBar();
		}
		layoutEnd();
	}

	private void appendContentPane() {
		layoutHorizontalPercent(100);
		{
			appendPaddingSpacer();
			layoutVerticalPercent(100);
			{
				appendPaddingSpacer();
				appendContent();
				appendPaddingSpacer();
			}
			layoutEnd();
			appendPaddingSpacer();
		}
		layoutEnd();
	}

	private void appendPaddingSpacer() {
		appendSpacerPixel(4);
	}

	private void appendButtonBar() {
		DisplayDimension buttonBarHeight =
			ThemeFactory.getTheme().getValue(com.top_logic.layout.Icons.BUTTON_COMP_HEIGHT);
		
		layoutBeginBegin();
		horizontalDimension(buttonBarHeight);
		complexWidget();
		layoutEndBegin();
		{
			append("<p:field name='" + SelectorContext.BUTTONS + "'/>");

			append("<script type='text/javascript'>");
			append("focusFirst();");
			append("</script>");
		}
		layoutEnd();
	}

	/**
	 * Hook for generating the dialog content.
	 */
	protected abstract void appendContent();

	/**
	 * @param spacerPixel
	 *        - space amount in pixel
	 */
	protected final void appendSpacerPixel(int spacerPixel) {
		layoutHorizontalPixel(spacerPixel);
		layoutEnd();
	}

	/**
	 * @param spacerPercent
	 *        - space amount in percent
	 */
	protected final void appendSpacerPercent(int spacerPercent) {
		layoutHorizontalPercent(spacerPercent);
		layoutEnd();
	}

	/**
	 * Appends full headline of dialog
	 */
	protected final void appendHeadline(String fieldName) {
		layoutBeginBegin();
		verticalPixel(60);
		append(" style='");
		append(HTMLConstants.TEXT_ALIGN_CENTER);
		append("'");
		layoutEndBegin();
		{
			append("<div style='font-weight:bold; font-size:20px; padding-top: 15px; padding-bottom: 10px;'><p:field name='"
				+ fieldName + "' style='label' /></div>");
		}
		layoutEnd();
	}
}
