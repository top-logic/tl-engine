/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.config;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.layout.template.WithProperties;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Creates a command to open a popup displaying the templates source code.
 *
 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
 */
public final class DisplayTemplateCodeCommand extends AbstractCommandModel implements WithProperties {

	/**
	 * Template property that renders the HTML template content to display.
	 */
	public static final String CONTENT_PROPERTY = "content";

	private final HTMLTemplate _template;

	/**
	 * Creates the command model that opens a popup displaying the templates source code.
	 */
	public DisplayTemplateCodeCommand(HTMLTemplate template) {
		_template = template;
		setImage(Icons.POPUP_BUTTON_TEMPLATE);
	}

	@Override
	protected HandlerResult internalExecuteCommand(DisplayContext context) {
		ButtonControl openingButton = context.get(EXECUTING_CONTROL);
		PopupDialogModel popupModel =
			new DefaultPopupDialogModel(
				new DefaultLayoutData(DisplayDimension.px(0), 100, DisplayDimension.px(0), 100, Scrolling.AUTO));
		PopupDialogControl popup =
			new PopupDialogControl(openingButton.getFrameScope(), popupModel, openingButton.getID());

		popup.setContent(Fragments.rendered(Icons.POPUP_CONTENTS_TEMPLATE.get(), this));
		context.getWindowScope().openPopupDialog(popup);
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	public Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
		switch (propertyName) {
			case CONTENT_PROPERTY:
				return _template.getHtml();
		}

		return WithProperties.super.getPropertyValue(propertyName);
	}
}