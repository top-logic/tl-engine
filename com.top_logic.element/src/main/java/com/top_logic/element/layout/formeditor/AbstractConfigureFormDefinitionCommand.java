/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import java.util.Map;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.formeditor.builder.ConfiguredDynamicFormBuilder;
import com.top_logic.element.layout.formeditor.builder.FormDefinitionUtil;
import com.top_logic.element.layout.formeditor.builder.FormsTemplateParameter;
import com.top_logic.element.layout.formeditor.builder.TypedForm;
import com.top_logic.element.layout.formeditor.builder.TypedFormDefinition;
import com.top_logic.element.layout.formeditor.definition.TLFormDefinition;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Command to change the view of a {@link FormComponent} by editing the underlying
 * {@link FormDefinition}.
 * 
 * <p>
 * This command is only usable by {@link FormComponent}s instantiated from typed layout templates
 * using the {@link ConfiguredDynamicFormBuilder}.
 * </p>
 * 
 * @see ConfiguredDynamicFormBuilder
 * @see FormComponent
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public abstract class AbstractConfigureFormDefinitionCommand extends AbstractCommandHandler {

	/**
	 * Creates an instance of {@link AbstractConfigureFormDefinitionCommand}.
	 */
	public AbstractConfigureFormDefinitionCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		if (component instanceof FormComponent) {
			FormComponent form = (FormComponent) component;
			ModelBuilder modelBuilder = form.getBuilder();

			if (modelBuilder instanceof ConfiguredDynamicFormBuilder) {
				ConfiguredDynamicFormBuilder builder = (ConfiguredDynamicFormBuilder) modelBuilder;
				TypedForm typedForm = builder.getDisplayedTypedForm();
				TLStructuredType displayedType = typedForm.getDisplayedType();

				GUIEditorDialog guiEditorDialog =
					createEditorDialog(component, typedForm.getFormDefinition(), displayedType);
				// setting of a FormDefinition is recorded as block
				ScriptingRecorder.annotateAsDontRecord(guiEditorDialog.getDialogModel());

				return guiEditorDialog.open(context);
			} else {
				return HandlerResult.error(com.top_logic.mig.html.layout.I18NConstants.NO_TEMPLATE_COMPONENT_ERROR);
			}
		}

		return HandlerResult.error(com.top_logic.element.layout.formeditor.I18NConstants.NO_FORM_COMPONENT_ERROR);
	}

	private GUIEditorDialog createEditorDialog(LayoutComponent component, FormDefinition definition,
			TLStructuredType displayedType) {
		GUIEditorDialog guiEditorDialog = new GUIEditorDialog();

		guiEditorDialog.setFormDefinitionCopy(definition);
		guiEditorDialog.setOpenInEditMode(true);
		guiEditorDialog.setType(displayedType);
		guiEditorDialog
			.setTemplateProvider(() -> FormDefinitionUtil.getInheritedFormDefinitionTemplates(displayedType));
		guiEditorDialog
			.setAcceptCommand(context -> acceptChange(context, guiEditorDialog, component, displayedType));

		return guiEditorDialog;
	}

	private HandlerResult acceptChange(DisplayContext context, GUIEditorDialog guiEditorDialog,
			LayoutComponent component, TLStructuredType displayedType) {
		FormDefinition formDefinition = guiEditorDialog.getFormDefinition();
		FormComponent form = (FormComponent) component;
		ConfiguredDynamicFormBuilder builder = ((ConfiguredDynamicFormBuilder) form.getBuilder());

		TLStructuredType currentType = WrapperHistoryUtils.getCurrent(displayedType);
		if (currentType != null && builder.displaysStandardForm()) {
			return updateStandardForm(context, guiEditorDialog, component, currentType, form);
		} else {
			HandlerResult result = storeCustomForm(formDefinition, component, TLModelUtil.qualifiedName(displayedType));
			if (result.isSuccess() && ScriptingRecorder.isRecordingActive()) {
				/* Recording is turned off by annotation the dialog model. Therefore the accept
				 * command is not recorded. When the dialog is closed, recording is re-enabled, so
				 * it is possible to record in a DialogClosedListener. */
				String typeName = TLModelUtil.qualifiedName(displayedType);
				guiEditorDialog.getDialogModel().addListener(DialogModel.CLOSED_PROPERTY,
					new DialogClosedListener() {

						@Override
						public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
							ScriptingRecorder.recordAction(createAction(form, formDefinition, typeName, false));
						}
					});

			}
			return result;
		}
	}

	private HandlerResult updateStandardForm(DisplayContext context, GUIEditorDialog guiEditorDialog,
			LayoutComponent component, TLStructuredType type, FormComponent form) {
		String message = context.getResources().getString(com.top_logic.mig.html.layout.I18NConstants.STORE_FOR_MODEL);
		FormDefinition formDefinition = guiEditorDialog.getFormDefinition();

		CommandModel yes =
			MessageBox.button(ButtonType.YES, confirmContext -> confirmUpdateStandardForm(form, formDefinition, type));
		// Do not record click on button
		ScriptingRecorder.annotateAsDontRecord(yes);
		CommandModel no =
			MessageBox.button(ButtonType.NO, denyContext -> denyUpdateStandardForm(component, formDefinition, type));
		// Do not record click on button
		ScriptingRecorder.annotateAsDontRecord(no);

		return MessageBox.confirm(context, MessageType.CONFIRM, message, yes, no);
	}

	private HandlerResult confirmUpdateStandardForm(FormComponent component, FormDefinition formDefinition,
			TLStructuredType type) {
		updateStandardForm(component, formDefinition, type);
		if (ScriptingRecorder.isRecordingActive()) {
			String typeName = TLModelUtil.qualifiedName(type);
			ScriptingRecorder.recordAction(createAction(component, formDefinition, typeName, true));
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	private HandlerResult denyUpdateStandardForm(LayoutComponent component, FormDefinition formDefinition,
			TLStructuredType type) {
		String typeName = TLModelUtil.qualifiedName(type);
		HandlerResult result = storeCustomForm(formDefinition, component, typeName);
		if (result.isSuccess() && ScriptingRecorder.isRecordingActive()) {
			ScriptingRecorder.recordAction(createAction(component, formDefinition, typeName, false));
		}
		return result;
	}

	private HandlerResult storeCustomForm(FormDefinition formDefintion, LayoutComponent component, String typeName) {
		boolean success = replaceAndStoreFormDefinition(component, formDefintion, typeName);
		if (success) {
			return HandlerResult.DEFAULT_RESULT;
		} else {
			return HandlerResult.error(com.top_logic.mig.html.layout.I18NConstants.NO_TEMPLATE_COMPONENT_ERROR);
		}
	}

	/**
	 * Sets the given {@link FormDefinition} as standard form for the given type.
	 * 
	 * @param component
	 *        A component to invalidate and update.
	 * @param formDefintion
	 *        The form to set for the given type.
	 * @param type
	 *        The {@link TLStructuredType} for which the given form should be standard.
	 */
	protected static void updateStandardForm(FormComponent component, FormDefinition formDefintion,
			TLStructuredType type) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction();

		TLFormDefinition formAnnotation = TypedConfiguration.newConfigItem(TLFormDefinition.class);
		formAnnotation.setForm(formDefintion);
		type.setAnnotation(formAnnotation);

		tx.commit();

		component.invalidate();
		component.removeFormContext();
	}

	/**
	 * Stores the form for the given type.
	 * 
	 * <p>
	 * If the type has already a form it will be replaced by the given one.
	 * </p>
	 * <p>
	 * Replaces the property
	 * {@link com.top_logic.element.layout.formeditor.builder.ConfiguredDynamicFormBuilder.Config#FORMS}
	 * and stores the changed layout.
	 * </p>
	 * 
	 * @param component
	 *        The component the form shall be configured for.
	 * @param formDefinition
	 *        The form to add for the given type.
	 * @param type
	 *        The name of the {@link TLStructuredType} of the form.
	 */
	protected static boolean replaceAndStoreFormDefinition(LayoutComponent component, FormDefinition formDefinition,
			String type) {
		String scope = LayoutTemplateUtils.getNonNullNameScope(component);
		TLLayout layout = LayoutTemplateUtils.getOrCreateLayout(scope);

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		try (Transaction tx = kb.beginTransaction()) {
			FormsTemplateParameter arguments = (FormsTemplateParameter) layout.getArguments();
			Map<TLModelPartRef, TypedFormDefinition> forms = arguments.getForms();

			TypedFormDefinition typedForm = TypedConfiguration.newConfigItem(TypedFormDefinition.class);
			TLModelPartRef typeRef = TLModelPartRef.ref(type);
			typedForm.setType(typeRef);
			typedForm.setFormDefinition(formDefinition);

			forms.put(typeRef, typedForm);

			LayoutTemplateUtils.storeLayout(scope, layout.getTemplateName(), arguments);

			tx.commit();
		} catch (ConfigurationException exception) {
			throw new ConfigurationError(exception);
		}

		LayoutTemplateUtils.replaceComponent(scope, component);

		return true;
	}

	/**
	 * Creates an {@link ConfigureFormDefinitionAction} filled with the given parameters.
	 * 
	 * @param component
	 *        The component the form is configured for.
	 * @param formDefinition
	 *        The form for the given type to display.
	 * @param type
	 *        The name of the {@link TLStructuredType} of the form.
	 * @param standardForm
	 *        Whether the standard form for the type is configured.
	 */
	protected ApplicationAction createAction(LayoutComponent component, FormDefinition formDefinition,
			String type, boolean standardForm) {
		ConfigureFormDefinitionAction action = TypedConfiguration.newConfigItem(ConfigureFormDefinitionAction.class);
		action.setFormDefinition(formDefinition);
		action.setLayoutComponent(component.getName());
		action.setType(type);
		action.setStandardForm(standardForm);

		return action;
	}

	@Override
	protected boolean mustNotRecord(DisplayContext context, LayoutComponent component,
			Map<String, Object> someArguments) {
		// setting of a FormDefinition is recorded as block
		return true;
	}

	@Override
	public ResKey getDefaultI18NKey() {
		return I18NConstants.CONFIGURE_FORM_DEFINITION_COMMAND;
	}
}
