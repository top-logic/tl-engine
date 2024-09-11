/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import static com.top_logic.basic.config.TypedConfiguration.*;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.SimpleFormDialog;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.SetGlobalVariableAction;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.LastRevisionNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.util.Resources;

/**
 * {@link AbstractCommandHandler} that stores the {@link HistoryManager#getLastRevision() last
 * revision} into a global variable and creates an action for it.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StoreCurrentRevisionCommand extends AbstractCommandHandler {

	/**
	 * Creates a new {@link StoreCurrentRevisionCommand}.
	 */
	public StoreCurrentRevisionCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
			Object model, Map<String, Object> someArguments) {
		return new VariableNameDialog((ScriptRecorderTree) aComponent).open(aContext);
	}

	static class VariableNameDialog extends SimpleFormDialog {

		final ScriptRecorderTree _scriptRecorderTree;

		public VariableNameDialog(ScriptRecorderTree scriptRecorder) {
			super(I18NConstants.VARIABLE_NAME_DIALOG,
				DisplayDimension.dim(350, DisplayUnit.PIXEL),
				DisplayDimension.dim(250, DisplayUnit.PIXEL));
			_scriptRecorderTree = scriptRecorder;
		}

		@Override
		protected void fillButtons(List<CommandModel> buttons) {
			Command command = new StoreCommand();
			CommandModel ok = MessageBox.button(ButtonType.OK, command);
			ScriptingRecorder.annotateAsDontRecord(ok);
			buttons.add(ok);
			CommandModel cancel = MessageBox.button(ButtonType.CANCEL, getDiscardClosure());
			ScriptingRecorder.annotateAsDontRecord(cancel);
			buttons.add(cancel);
		}

		@Override
		protected void fillFormContext(FormContext context) {
			StringField newStringField = FormFactory.newStringField(INPUT_FIELD);
			newStringField.setMandatory(true);
			newStringField.addConstraint(new AbstractConstraint() {

				private static final String VARIABLE_EXISTS_SUFFIX = "variableExists";

				@Override
				public boolean check(Object value) throws CheckException {
					boolean variableExists =
						GlobalVariableStore.getElseCreateFromSession().has(StringServices.nonNull((String) value));
					if (variableExists) {
						throw new CheckException(Resources.getInstance().getMessage(
							I18NConstants.VARIABLE_NAME_DIALOG.key(VARIABLE_EXISTS_SUFFIX), value));
					}
					return true;
				}
			});
			context.addMember(newStringField);

			ScriptingRecorder.annotateAsDontRecord(context);
		}

		final class StoreCommand implements Command {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				FormContext formContext = getFormContext();
				boolean checkAll = formContext.checkAll();
				if (!checkAll) {
					return AbstractApplyCommandHandler.createErrorResult(formContext);
				}

				String variableName = ((StringField) formContext.getField(INPUT_FIELD)).getAsString();
				resordSetLastRevision(variableName);

				return getDialogModel().getCloseAction().executeCommand(context);
			}

			private void resordSetLastRevision(String variableName) {
				ModelName lastRevisionName = newConfigItem(LastRevisionNamingScheme.Name.class);
				SetGlobalVariableAction action = ActionFactory.setGlobalVariableAction(variableName, lastRevisionName);
				VariableNameDialog.this._scriptRecorderTree.insertAction(action);
			}
		}

	}

	@Override
	public ExecutabilityRule createExecutabilityRule() {
		return EnabledDuringRecording.INSTANCE;
	}
}

