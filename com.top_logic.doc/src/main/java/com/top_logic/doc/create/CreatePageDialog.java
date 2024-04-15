/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.create;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.doc.model.Page;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.control.IconControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.messagebox.AbstractFormPageDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;

/**
 * {@link AbstractFormPageDialog} containing {@link StringField}s for the title and help id to
 * create a new {@link Page}.
 * 
 * @author <a href="mailto:dpa@top-logic.com">dpa</a>
 */
public class CreatePageDialog extends AbstractFormPageDialog {

	/** CSS class of the content of the dialog */
	private static final String TL_DOC_CREATE_DIALOG_CSS = "tlDocCreateDialog";

	/** Label of {@link StringField} for the title of the page */
	static final String PAGE_TITLE = "pageTitle";

	/** Label of {@link StringField} for the help id of the page */
	static final String HELP_ID = "helpId";

	private CommandModel _createButton;

	/**
	 * Creates a {@link CreatePageDialog}.
	 * @param selected
	 *        The currently selected {@link Page} of the component.
	 * @param isChild
	 *        Whether the new {@link Page} shall be the child of the selected {@link Page}. If
	 *        <code>false</code> the new {@link Page} will be the next sibling of the selected
	 *        {@link Page}.
	 */
	public CreatePageDialog(LayoutComponent component, DisplayDimension width, DisplayDimension height, Page selected, boolean isChild) {
		super(isChild ? I18NConstants.CREATE_CHILD_PAGE_TITLE : I18NConstants.CREATE_PAGE_TITLE,
			isChild ? I18NConstants.CREATE_CHILD_PAGE_HEADER : I18NConstants.CREATE_PAGE_HEADER,
			I18NConstants.CREATE_PAGE_MESSAGE, width, height);
		Command createCommand =
			new Command.CommandChain(new CreatePageCommand(component, this, selected, isChild), getDiscardClosure());
		CommandModel createButton = MessageBox.forwardStyleButton(I18NConstants.CREATE_BUTTON, createCommand);
		this._createButton = createButton;
		getDialogModel().setDefaultCommand(createCommand);
	}

	@Override
	protected HTMLFragment createBodyContent() {
		HTMLFragment titleInput = input(PAGE_TITLE);
		HTMLFragment helpIdInput = input(HELP_ID);
		return div(TL_DOC_CREATE_DIALOG_CSS, label(PAGE_TITLE), div(titleInput), label(HELP_ID),
			div(helpIdInput));
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(getCreateButton());
		addCancel(buttons);
	}

	@Override
	protected void fillFormContext(FormContext context) {
		StringField titleField = FormFactory.newStringField(PAGE_TITLE);
		titleField.setLabel(Resources.getInstance().getString(I18NConstants.CREATE_PAGE_PAGE_TITLE));
		titleField.setMandatory(true);

		StringField helpIdField = FormFactory.newStringField(HELP_ID);
		helpIdField.setLabel(Resources.getInstance().getString(I18NConstants.CREATE_PAGE_HELP_ID));
		context.addMember(titleField);
		context.addMember(helpIdField);
	}

	@Override
	protected IconControl createTitleIcon() {
		return IconControl.icon(Icons.CREATE_ICON);
	}

	/**
	 * Button with {@link CreatePageCommand}.
	 */
	protected CommandModel getCreateButton() {
		return _createButton;
	}


}
