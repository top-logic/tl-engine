/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import static com.top_logic.layout.form.values.Fields.*;

import java.util.List;
import java.util.function.Supplier;

import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.element.layout.formeditor.builder.FormDefinitionUtil;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.fieldprovider.ConfigurationFieldProvider;
import com.top_logic.element.meta.form.fieldprovider.form.TLFormTemplates;
import com.top_logic.element.meta.form.fieldprovider.form.TLFormType;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.editor.ComponentConfigurationDialogBuilder;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.form.values.edit.editor.Editor;
import com.top_logic.layout.form.values.edit.editor.EditorUtils;
import com.top_logic.layout.scripting.action.ValueModelUpdate;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.form.definition.FormContextDefinition;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link Editor} for a {@link FormDefinition} property of a typed configuration item.
 * 
 * <p>
 * The form is displayed as button opening a form editor that displays the form in a dialog and
 * allows to edit the form.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FormDefinitionEditor implements Editor {

	@Override
	public FormMember createUI(EditorFactory editorFactory, FormContainer container, ValueModel model) {
		final PropertyDescriptor property = model.getProperty();
		
		String fieldName = normalizeFieldName(property.getPropertyName());
	
		BooleanField changeField = EditorUtils.addChangeField(container, model);
		CommandField commandField = new OpenEditor(fieldName, container, model, editorFactory, changeField);
		container.addMember(commandField);
	
		return commandField;
	}

	private final class OpenEditor extends CommandField {
		private final FormContainer _container;

		private final ValueModel _model;

		private final EditorFactory _editorFactory;

		/**
		 * Creates a {@link OpenEditor}.
		 */
		private OpenEditor(String name, FormContainer container,
				ValueModel model, EditorFactory editorFactory, BooleanField changeField) {
			super(name);

			_container = container;
			_model = model;
			_editorFactory = editorFactory;

			// setting of a FormDefinition is recorded as block
			ScriptingRecorder.annotateAsDontRecord(this);
			setImage(Icons.OPEN_GUI_EDITOR);
			setNotExecutableImage(Icons.OPEN_GUI_EDITOR_DISABLED);
			// Reason key when whole form is not editable.
			setNotExecutableReasonKey(com.top_logic.common.webfolder.ui.I18NConstants.FIELD_DISABLED);
			setTooltip(Resources.getInstance().getString(I18NConstants.OPEN_FORM_EDITOR_DIALOG));
			/* Ensure that the command can be executed, also when the owner group is in view mode to
			 * allow the user to view the actual form definition. */
			setInheritDeactivation(false);

			if (resolveFormType() == null) {
				setNotExecutable(I18NConstants.FORM_EDITOR_DIALOG_DISABLED_NO_TYPE_SELECTED);
			}

			ConfigurationListener typeChangedListener = new ConfigurationListener() {
				@Override
				public void onChange(ConfigurationChange change) {
					Object newValue = change.getNewValue();
					if (newValue == null) {
						setNotExecutable(I18NConstants.FORM_EDITOR_DIALOG_DISABLED_NO_TYPE_SELECTED);
						model.setValue(null);
					} else {
						setExecutable();
						Object oldValue = change.getOldValue();
						if (oldValue != null && !newValue.equals(oldValue)) {
							TLType oldType;
							if (oldValue instanceof TLModelPartRef) {
								oldType = ((TLModelPartRef) oldValue).resolveType();
							} else {
								oldType = (TLType) oldValue;
							}
							TLType newType;
							if (newValue instanceof TLModelPartRef) {
								newType = ((TLModelPartRef) newValue).resolveType();
							} else {
								newType = (TLType) newValue;
							}
							if (!TLModelUtil.isCompatibleType(oldType, newType)) {
								// type changed incompatible: definition is invalid
								model.setValue(null);
							}
						}
					}
				}
			};

			ConfigurationListener valueChangedListener = new ConfigurationListener() {
				@Override
				public void onChange(ConfigurationChange change) {
					changeField.setAsBoolean(true);
				}
			};

			final PropertyDescriptor property = model.getProperty();
			ConfigurationItem owner = _model.getModel();
			PropertyDescriptor typeProperty =
				owner instanceof FormContextDefinition
					? owner.descriptor().getProperty(FormContextDefinition.FORM_CONTEXT_TYPE)
					: null;

			setControlProvider(new DefaultFormFieldControlProvider() {
				@Override
				public Control visitCommandField(CommandField member, Void arg) {
					AbstractControlBase result = (AbstractControlBase) super.visitCommandField(member, arg);
					result.addListener(AbstractControlBase.ATTACHED_PROPERTY, new AttachedPropertyListener() {
						@Override
						public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
							if (newValue) {
								if (typeProperty != null) {
									owner.addConfigurationListener(typeProperty, typeChangedListener);
								}
								owner.addConfigurationListener(property, valueChangedListener);
							} else {
								if (typeProperty != null) {
									owner.removeConfigurationListener(typeProperty, typeChangedListener);
								}
								owner.removeConfigurationListener(property, valueChangedListener);
							}
						}

					});
					return result;
				}
			});
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			FormDefinition formDefinitionOrigin = (FormDefinition) _model.getValue();

			LayoutComponent contextComponent =
				_editorFactory.getSettings().get(ComponentConfigurationDialogBuilder.COMPONENT);
			GUIEditorDialog guiEditorDialog = new GUIEditorDialog(contextComponent);
			// setting of a FormDefinition is recorded as block
			ScriptingRecorder.annotateAsDontRecord(guiEditorDialog.getDialogModel());
			guiEditorDialog.setFormDefinitionCopy(formDefinitionOrigin);
			boolean inEditMode = _container.isActive();
			guiEditorDialog.setOpenInEditMode(inEditMode);
			initTemplates(guiEditorDialog);
			guiEditorDialog.setAcceptCommand(newAcceptCommand(formDefinitionOrigin, guiEditorDialog));
			if (inEditMode) {
				FormDefinition formDefinitionGui = guiEditorDialog.getFormDefinition();
				guiEditorDialog
					.addAdditionalCommand(newDiscardChangesCommand(formDefinitionOrigin, formDefinitionGui));
				guiEditorDialog.addAdditionalCommand(newClearFormCommand(formDefinitionGui));
			}
			return guiEditorDialog.open(context);
		}

		private TLStructuredType resolveFormType() {
			ConfigurationItem owner = _model.getModel();
			if (owner instanceof FormContextDefinition contextDef) {
				TLStructuredType contextType = contextType(contextDef);
				if (contextType != null) {
					return contextType;
				}
			}

			// Try to resolve from edit context.
			EditContext editContext =
				ConfigurationFieldProvider.editContext(_editorFactory.getInitializerProvider());

			TLFormType typeAnnotation = editContext.getAnnotation(TLFormType.class);
			return TLFormType.resolve(typeAnnotation, editContext.getObject(), editContext.getDescriptionKey());
		}

		private void initTemplates(GUIEditorDialog guiEditorDialog) {
			ConfigurationItem owner = _model.getModel();

			Supplier<? extends List<FormDefinitionTemplate>> templateProvider;

			if (owner instanceof FormContextDefinition contextDef) {
				TLStructuredType type = contextType(contextDef);
				if (type != null) {
					templateProvider = () -> FormDefinitionUtil.getInheritedFormDefinitionTemplates(type);

					guiEditorDialog.setType(type);
					guiEditorDialog.setTemplateProvider(templateProvider);
					return;
				}
			}

			EditContext editContext =
				ConfigurationFieldProvider.editContext(_editorFactory.getInitializerProvider());

			TLFormType typeAnnotation = editContext.getAnnotation(TLFormType.class);
			TLFormTemplates templatesAnnotation = editContext.getAnnotation(TLFormTemplates.class);

			TLStructuredType type =
				TLFormType.resolve(typeAnnotation, editContext.getObject(), editContext.getDescriptionKey());
			templateProvider = TLFormTemplates.resolve(templatesAnnotation, editContext.getObject());

			guiEditorDialog.setType(type);
			guiEditorDialog.setTemplateProvider(templateProvider);
		}

		private TLStructuredType contextType(FormContextDefinition contextDef) {
			TLModelPartRef formType = contextDef.getFormContextType();
			if (formType == null) {
				return null;
			}
			return (TLStructuredType) formType.resolveType();
		}

		private Command newAcceptCommand(FormDefinition formDefinitionOrigin, GUIEditorDialog guiEditorDialog) {
			FormMember member = this;
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
					if (!ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(newFormDefinition, formDefinitionOrigin)) {
						_model.setValue(newFormDefinition);
					}

					if (ScriptingRecorder.isEnabled()) {
						/* Recording is turned off by annotation the dialog model. Therefore the
						 * accept command is not recorded. When the dialog is closed, recording
						 * is re-enabled, so it is possible to record in a
						 * DialogClosedListener. */
						guiEditorDialog.getDialogModel().addListener(DialogModel.CLOSED_PROPERTY,
							new DialogClosedListener() {

								@Override
								public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
									if (ScriptingRecorder.isRecordingActive()) {
										ValueModelUpdate.recordValueModelUpdate(member, newFormDefinition);
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
			return new ClearFormCommandModel(formDefintionGui);
		}
	}
}

