/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import static com.top_logic.layout.form.values.Fields.*;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.formeditor.builder.FormDefinitionUtil;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.form.values.edit.editor.Editor;
import com.top_logic.layout.form.values.edit.editor.EditorUtils;
import com.top_logic.layout.scripting.action.ValueModelUpdate;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link Editor} for a {@link FormDefinition}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FormDefinitionEditor implements Editor {

	@Override
	public FormMember createUI(EditorFactory editorFactory, FormContainer container, ValueModel model) {
		final PropertyDescriptor property = model.getProperty();
		ConfigurationItem configItem = model.getModel();
		PropertyDescriptor typeProperty = getTypeProperty(configItem, property);
		
		CommandField commandField = new CommandField(normalizeFieldName(property.getPropertyName())) {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				TLStructuredType type = resolveFormType();
				FormDefinition formDefinitionOrigin = (FormDefinition) model.getValue();

				GUIEditorDialog guiEditorDialog = new GUIEditorDialog();
				// setting of a FormDefinition is recorded as block
				ScriptingRecorder.annotateAsDontRecord(guiEditorDialog.getDialogModel());
				guiEditorDialog.setFormDefinitionCopy(formDefinitionOrigin);
				boolean inEditMode = container.isActive();
				guiEditorDialog.setOpenInEditMode(inEditMode);
				guiEditorDialog.setType(type);
				guiEditorDialog.setTemplateProvider(() -> FormDefinitionUtil.getInheritedFormDefinitionTemplates(type));
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
				try {
					Object formType = configItem.value(typeProperty);
					return formType instanceof TLModelPartRef
						? (TLStructuredType) ((TLModelPartRef) formType).resolveType()
						: (TLStructuredType) formType;
				} catch (ConfigurationException ex) {
					throw new ConfigurationError(ex);
				}
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
							model.setValue(newFormDefinition);
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
		};
		// setting of a FormDefinition is recorded as block
		ScriptingRecorder.annotateAsDontRecord(commandField);
		commandField.setImage(Icons.OPEN_GUI_EDITOR);
		commandField.setNotExecutableImage(Icons.OPEN_GUI_EDITOR_DISABLED);
		// Reason key when whole form is not editable.
		commandField.setNotExecutableReasonKey(com.top_logic.common.webfolder.ui.I18NConstants.FIELD_DISABLED);
		commandField.setTooltip(Resources.getInstance().getString(I18NConstants.OPEN_FORM_EDITOR_DIALOG));
		if (configItem.value(typeProperty) == null) {
			setNotExecutable(commandField, typeProperty);
		}
		
		ConfigurationListener typeChangedListener = new ConfigurationListener() {

			@Override
			public void onChange(ConfigurationChange change) {
				Object newValue = change.getNewValue();
				if (newValue == null) {
					setNotExecutable(commandField, typeProperty);
					model.setValue(null);
				} else {
					commandField.setExecutable();
					Object oldValue = change.getOldValue();
					if (oldValue != null && !newValue.equals(oldValue)) {
						TLType oldType;
						if (oldValue instanceof TLModelPartRef) {
							try {
								oldType = ((TLModelPartRef) oldValue).resolveType();
							} catch (ConfigurationException ex) {
								// failure
								model.setValue(null);
								return;
							}
						} else {
							oldType = (TLType) oldValue;
						}
						TLType newType;
						if (newValue instanceof TLModelPartRef) {
							try {
								newType = ((TLModelPartRef) newValue).resolveType();
							} catch (ConfigurationException ex) {
								// failure
								model.setValue(null);
								return;
							}
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

		BooleanField changeField = EditorUtils.addChangeField(container, model);
		ConfigurationListener valueChangedListener = new ConfigurationListener() {
			@Override
			public void onChange(ConfigurationChange change) {
				changeField.setAsBoolean(true);
			}
		};

		commandField.setControlProvider(new DefaultFormFieldControlProvider() {
			
			@Override
			public Control visitCommandField(CommandField member, Void arg) {
				AbstractControlBase result = (AbstractControlBase) super.visitCommandField(member, arg);
				result.addListener(AbstractControlBase.ATTACHED_PROPERTY, new AttachedPropertyListener() {

					@Override
					public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
						if (newValue) {
							configItem.addConfigurationListener(typeProperty, typeChangedListener);
							configItem.addConfigurationListener(property, valueChangedListener);
						} else {
							configItem.removeConfigurationListener(typeProperty, typeChangedListener);
							configItem.removeConfigurationListener(property, valueChangedListener);
						}
					}

				});
				return result;
			}
		});
		container.addMember(commandField);

		/* Ensure that the command can be executed, also when the owner group is in view mode to
		 * allow the user to view the actual form definition. */
		commandField.setInheritDeactivation(false);

		return commandField;

	}

	/**
	 * The property containing the type of the form to edit.
	 */
	private PropertyDescriptor getTypeProperty(ConfigurationItem configItem, PropertyDescriptor property) {
		FormTypeProperty typePropertyAnnotation = property.getAnnotation(FormTypeProperty.class);
		if (typePropertyAnnotation == null) {
			throw new TopLogicException(
				I18NConstants.MISSING_TYPE_ATTRIBUTE_ANNOTATION__ATTRIBUTE.fill(label(property)));
		}
		String typePropertyName = typePropertyAnnotation.value();
		PropertyDescriptor typeProperty = configItem.descriptor().getProperty(typePropertyName);
		if (typeProperty == null) {
			throw new TopLogicException(I18NConstants.MISSING_TYPE_ATTRIBUTE__ATTRIBUTE.fill(typePropertyName));
		}
		Class<?> propertyType = typeProperty.getType();
		if (!TLStructuredType.class.isAssignableFrom(propertyType)
			&& !TLModelPartRef.class.isAssignableFrom(propertyType)) {
			throw new TopLogicException(
				I18NConstants.FORM_TYPE_PROPERTY_HAS_NO_MODEL_TYPE__PROPERTY_TYPE.fill(typePropertyName,
					propertyType.getName()));
		}
		return typeProperty;
	}

	void setNotExecutable(CommandField commandField, PropertyDescriptor typeProperty) {
		commandField.setNotExecutable(I18NConstants.FORM_EDITOR_DIALOG_DISABLED_NO_TYPE_SELECTED__TYPE_ATTRIBUTE
			.fill(label(typeProperty)));
	}

	ResKey label(PropertyDescriptor property) {
		return Labels.propertyLabelKey(property);
	}


}

