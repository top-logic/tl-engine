/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector;

import java.util.List;

import org.w3c.dom.Document;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProviderWithColon;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplate;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.messagebox.AbstractFormDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.GuiInspectorPlugin;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The gui inspector, which is used to inspect an gui element and create assertions for it.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class GuiInspectorControl extends AbstractFormDialog {

	private static final String PLUGINS_GROUP = "plugins";

	private class FormResetter implements Command {

		@Override
		public HandlerResult executeCommand(DisplayContext displayContext) {
			getFormContext().reset();
			return HandlerResult.DEFAULT_RESULT;
		}

	}

	private static final Document TEMPLATE = DOMUtil.parseThreadSafe(
		"	<div"
		+ "		xmlns='" + HTMLConstants.XHTML_NS + "'"
		+ "		xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
		+ "		xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
		+ "	>"
		+ "		<p:field name='" + PLUGINS_GROUP + "'>"
		+ "			<t:list>"
		+ "				<table>"
		+ "					<t:items>"
		+ "						<p:self />"
		+ "					</t:items>"
		+ "				</table>"
		+ "			</t:list>"
		+ "		</p:field>"
		+ "	</div>");

	private final InspectorModel _model;

	protected GuiInspectorControl(DialogModel dialogModel, InspectorModel model) {
		super(dialogModel);
		_model = model;
	}

	@Override
	protected void fillButtons(List<CommandModel> buttonList) {
		buttonList.add(createResetButton());
		buttonList.add(createCancelButton());
		CommandModel createAssertionsButton = createCreateAssertionsButton();
		buttonList.add(createAssertionsButton);
		getDialogModel().setDefaultCommand(createAssertionsButton);
		annotateButtonsAsDontRecord(buttonList);
	}

	@SuppressWarnings("synthetic-access")
	private CommandModel createResetButton() {
		return MessageBox.button(I18NConstants.RESET, Icons.RESET, Icons.RESET_DISABLED, new FormResetter());
	}

	private CommandModel createCancelButton() {
		CommandModel cancelButton = MessageBox.button(ButtonType.CANCEL, getDiscardClosure());
		return cancelButton;
	}

	private CommandModel createCreateAssertionsButton() {
		Command createAssertionsAndCloseDialogCommand =
			new Command.CommandChain(new RecordAssertionsCommand(_model.getAssertions()), getDiscardClosure());
		return MessageBox.button(I18NConstants.CREATE_ASSERTIONS, Icons.CREATE_ASSERTIONS,
			Icons.CREATE_ASSERTIONS_DISABLED, createAssertionsAndCloseDialogCommand);
	}

	private void annotateButtonsAsDontRecord(List<CommandModel> buttonList) {
		for (CommandModel button : buttonList) {
			ScriptingRecorder.annotateAsDontRecord(button);
		}
	}

	@Override
	protected void fillFormContext(FormContext formContext) {
		formContext.addMember(createFieldsGroup());
		ScriptingRecorder.annotateAsDontRecord(formContext);
	}


	private FormGroup createFieldsGroup() {
		FormGroup fieldsGroup = new FormGroup(PLUGINS_GROUP, I18NConstants.DIALOG);
		for (GuiInspectorPlugin plugin : _model.getAssertions()) {
			plugin.registerFormMembers(fieldsGroup);
		}
		for (GuiInspectorPlugin plugin : _model.getDebugInformations()) {
			plugin.registerFormMembers(fieldsGroup);
		}
		return fieldsGroup;
	}

	@Override
	protected FormTemplate getTemplate() {
		return defaultTemplate(TEMPLATE, true, I18NConstants.DIALOG);
	}

	@Override
	protected ControlProvider getControlProvider() {
		return DefaultFormFieldControlProviderWithColon.INSTANCE;
	}

}
