/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import java.util.List;
import java.util.function.Supplier;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.element.layout.formeditor.ClearFormCommand;
import com.top_logic.element.layout.formeditor.DiscardChangesCommandModel;
import com.top_logic.element.layout.formeditor.FormDefinitionTemplate;
import com.top_logic.element.layout.formeditor.GUIEditorDialog;
import com.top_logic.element.layout.formeditor.Icons;
import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.ButtonUIModel;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.DisabledPropertyListener;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ImmutablePropertyListener;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.mig.html.layout.VisibilityListener;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link FieldProvider} for attributes with value {@link FormDefinition}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class FormDefinitionFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {
		TLStructuredType guiType = guiType(editContext);
		Supplier<? extends List<FormDefinitionTemplate>> templateProvider =
			templateProvider(editContext);

		HiddenField field = FormFactory.newHiddenField(fieldName, editContext.getCorrectValues());
		CommandModel command = new AbstractCommandModel() {

			@Override
			public HandlerResult internalExecuteCommand(DisplayContext context) {
				FormDefinition formDefinitionOrigin = (FormDefinition) field.getValue();

				GUIEditorDialog guiEditorDialog = new GUIEditorDialog();
				// setting of a FormDefinition is recorded as block
				ScriptingRecorder.annotateAsDontRecord(guiEditorDialog.getDialogModel());
				guiEditorDialog.setFormDefinitionCopy(formDefinitionOrigin);
				guiEditorDialog.setOpenInEditMode(!editContext.isDisabled());
				guiEditorDialog.setTemplateProvider(templateProvider);
				guiEditorDialog.setType(guiType);
				guiEditorDialog.setAcceptCommand(newAcceptCommand(guiEditorDialog));
				FormDefinition formDefinitionGui = guiEditorDialog.getFormDefinition();
				guiEditorDialog.addAdditionalCommand(newDiscardChangesCommand(formDefinitionOrigin, formDefinitionGui));
				guiEditorDialog.addAdditionalCommand(newClearFormCommand(formDefinitionGui));
				return guiEditorDialog.open(context);
			}

			private Command newAcceptCommand(GUIEditorDialog guiEditorDialog) {
				return new Command() {

					@Override
					public HandlerResult executeCommand(DisplayContext context) {
						FormDefinition newFormDefinition;
						FormDefinition currentFormDefinition = guiEditorDialog.getFormDefinition();
						if (currentFormDefinition.getContent().isEmpty()) {
							newFormDefinition = null;
						} else {
							newFormDefinition = currentFormDefinition;
						}
						field.setValue(newFormDefinition);
						/* Recording is turned off by annotation the dialog model. Therefore the
						 * accept command is not recorded. When the dialog is closed, recording is
						 * re-enabled, so it is possible to record in a DialogClosedListener. */
						if (ScriptingRecorder.isEnabled()) {
							guiEditorDialog.getDialogModel().addListener(DialogModel.CLOSED_PROPERTY,
								new DialogClosedListener() {
									@Override
									public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
										if (ScriptingRecorder.isRecordingActive()) {
											ScriptingRecorder.recordFieldInput(field, newFormDefinition);
										}
									}
								});
						}
						return HandlerResult.DEFAULT_RESULT;
					}
				};
			}

			private CommandModel newDiscardChangesCommand(FormDefinition formDefinitionOrigin,
					FormDefinition formDefinitionGui) {
				return new DiscardChangesCommandModel(formDefinitionOrigin, formDefinitionGui);
			}

			private CommandModel newClearFormCommand(FormDefinition formDefintionGui) {
				return ClearFormCommand.newClearFormCommandModel(formDefintionGui);
			}
		};
		// setting of a FormDefinition is recorded as block
		ScriptingRecorder.annotateAsDontRecord(command);
		command.setImage(Icons.OPEN_GUI_EDITOR);
		command.setNotExecutableImage(Icons.OPEN_GUI_EDITOR_DISABLED);
		command.setTooltip(Resources.getInstance()
			.getString(com.top_logic.element.layout.formeditor.I18NConstants.OPEN_FORM_EDITOR_DIALOG));

		if (guiType == null) {
			command.setNotExecutable(I18NConstants.FORM_EDITOR_NO_GUI_TYPE_AVAILABLE);
		} else {
			FormFieldDisabledListener.addListenerForCommand(field, command);
		}
		field.addListener(FormMember.VISIBLE_PROPERTY, new VisibilityListener() {

			@Override
			public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
				command.setVisible(newVisibility.booleanValue());
				return Bubble.BUBBLE;
			}
		});
		// Display the field by displaying the opening command.
		field.setControlProvider(new ControlProvider() {

			@Override
			public Control createControl(Object model, String style) {
				return new ButtonControl(command);
			}
		});
		return field;
	}

	/**
	 * Computation of the {@link FormDefinitionTemplate}.
	 * 
	 * @param editContext
	 *        See {@link #getFormField(EditContext, String)}.
	 * @return Non <code>null</code> {@link Supplier} for the {@link FormDefinitionTemplate} to use.
	 */
	protected abstract Supplier<? extends List<FormDefinitionTemplate>> templateProvider(EditContext editContext);

	/**
	 * The {@link TLStructuredType} to create GUI for.
	 * 
	 * @param editContext
	 *        See {@link #getFormField(EditContext, String)}.
	 * @return May be <code>null</code>. In such case a not executable command is created.
	 */
	protected abstract TLStructuredType guiType(EditContext editContext);

	private static class FormFieldDisabledListener implements DisabledPropertyListener, ImmutablePropertyListener {

		private final ButtonUIModel _command;

		FormFieldDisabledListener(ButtonUIModel command) {
			_command = command;
		}

		public static void addListenerForCommand(FormField field, ButtonUIModel button) {
			FormFieldDisabledListener l = new FormFieldDisabledListener(button);
			field.addListener(FormField.DISABLED_PROPERTY, l);
			field.addListener(FormField.IMMUTABLE_PROPERTY, l);
		}

		@Override
		public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
			updateCommand(newValue.booleanValue());
			return Bubble.BUBBLE;
		}

		@Override
		public Bubble handleDisabledChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
			updateCommand(newValue.booleanValue());
			return Bubble.BUBBLE;
		}

		private void updateCommand(boolean notExecutable) {
			if (notExecutable) {
				_command.setNotExecutable(com.top_logic.common.webfolder.ui.I18NConstants.FIELD_DISABLED);
			} else {
				_command.setExecutable();
			}
		}

	}

}
