/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.format.ClassFormat;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.layout.Control;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.model.form.implementation.FormEditorMapping;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;

/**
 * {@link ModelBuilder} creating a {@link FormContext} to demonstrate editing of attributes of a
 * given type.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class DisplayFormEditorBuilder {

	/**
	 * {@link ConfigurationItem} for the {@link FormEditorToolboxControl}.
	 * 
	 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
	 */
	public interface GlobalConfig extends ConfigurationItem {

		/** Configuration name for the value of the {@link #getButtons()}. */
		public static final String BUTTONS_NAME = "buttons";

		/**
		 * List of elements to create buttons for.
		 */
		@Name(BUTTONS_NAME)
		@ListBinding(format = ClassFormat.class)
		List<Class<? extends FormElement<? extends FormElementTemplateProvider>>> getButtons();

		/**
		 * Sets a list of classes of {@link FormElement}s to generate buttons out of them.
		 * 
		 * @param buttons
		 *        A list of classes of {@link FormElement}s.
		 */
		void setButtons(List<Class<? extends FormElement<? extends FormElementTemplateProvider>>> buttons);
	}

	private static final String TEMPLATE_SELECTOR_FIELD = "formTemplates";

	/**
	 * Name of the Editor {@link FormContext}, i.e. the {@link FormContext} defining the result
	 * form.
	 */
	public static final String EDITOR_FORM_CONTEXT = "editor_FormContext";

	/**
	 * Name of the attributes {@link FormContext}, i.e. the {@link FormContext} holding the possible
	 * form elements.
	 */
	public static final String ATTRIBUTE_FORM_CONTEXT = "attributes_FormContext";

	private boolean _inEditMode;

	private Supplier<? extends List<FormDefinitionTemplate>> _templateProvider = () -> Collections.emptyList();


	/**
	 * Fills the {@link FormContext} to edit the given {@link FormDefinition} for the given
	 * {@link TLStructuredType}.
	 * 
	 * @param type
	 *        The {@link TLStructuredType} to for which the given {@link FormDefinition} is edited.
	 * @param resPrefix
	 *        {@link ResPrefix} for inner form elements.
	 * @param context
	 *        The {@link FormContext} to fill.
	 * @param formDefinition
	 *        The {@link FormDefinition} to edit.
	 */
	public void fillFormContext(TLStructuredType type, ResPrefix resPrefix, FormContext context,
			FormDefinition formDefinition) {
		FormEditorMapping formEditorMapping = new FormEditorMapping(new HashMap<>());

		FormContext attributeFormContext =
			createAttributeFormContext(type, formDefinition, formEditorMapping, resPrefix);
		FormContext editorFormContext =
			createEditorFormContext(type, formDefinition, resPrefix, formEditorMapping);

		context.addMember(attributeFormContext);
		context.addMember(editorFormContext);
	}


	private FormContext createEditorFormContext(TLStructuredType type, FormDefinition formDefinition,
			ResPrefix resPrefix, FormEditorMapping formEditorMapping) {
		AttributeFormContext editorFormContext = new AttributeFormContext(EDITOR_FORM_CONTEXT, resPrefix);

		editorFormContext.setControlProvider(new ControlProvider() {
			@Override
			public Control createControl(Object model, String style) {
				return new FormEditorPreviewControl(formDefinition, type, resPrefix, isInEditMode(), formEditorMapping);
			}
		});

		return editorFormContext;
	}

	private FormContext createAttributeFormContext(TLStructuredType type, FormDefinition formDefinition,
			FormEditorMapping formEditorMapping, ResPrefix resPrefix) {
		AttributeFormContext innerContext = new AttributeFormContext(ATTRIBUTE_FORM_CONTEXT, resPrefix);

		List<FormDefinitionTemplate> templates = getTemplateProvider().get();
		SelectField templateField;
		if (templates.isEmpty()) {
			templateField = null;
		} else {
			templateField = createTemplateField(templates, formDefinition);
			innerContext.addMember(templateField);
		}

		innerContext.setControlProvider(new ControlProvider() {
			@Override
			public Control createControl(Object model, String style) {
				return new FormEditorToolbarControl(formDefinition, type, resPrefix, isInEditMode(), formEditorMapping,
					templateField, getButtons());
			}
		});

		return innerContext;
	}

	private SelectField createTemplateField(List<FormDefinitionTemplate> formTemplates,
			FormDefinition editedFormDefinition) {

		SelectField result = FormFactory.newSelectField(TEMPLATE_SELECTOR_FIELD, formTemplates);
		result.setOptionLabelProvider(object -> ((FormDefinitionTemplate) object).getName());
		result.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				List<?> newValueList = (List<?>) newValue;
				if (newValueList == null || newValueList.isEmpty()) {
					editedFormDefinition.setContent(Collections.emptyList());
				} else {
					FormDefinition newFormDefinition =
						((FormDefinitionTemplate) newValueList.get(0)).getFormDefinition();
					FormDefinition copy = TypedConfiguration.copy(newFormDefinition);
					editedFormDefinition.setContent(copy.getContent());
				}
			}
		});

		template(result, descriptionBox(fragment(labelWithColon(), error()), direct()));

		return result;
	}

	/**
	 * Returns the global configuration for this builder.
	 * 
	 * @return The global configuration.
	 */
	protected GlobalConfig getConfig() {
		return ApplicationConfig.getInstance().getConfig(GlobalConfig.class);
	}

	/**
	 * Returns the list of classes of {@link FormElement}s for the buttons.
	 * 
	 * @return The configured list of buttons.
	 */
	public List<Class<? extends FormElement<? extends FormElementTemplateProvider>>> getButtons() {
		return getConfig().getButtons();
	}

	/**
	 * Whether the editor must be opened in edit mode.
	 */
	public boolean isInEditMode() {
		return _inEditMode;
	}

	/**
	 * Setter for {@link #isInEditMode()}
	 */
	public void setInEditMode(boolean inEditMode) {
		_inEditMode = inEditMode;
	}

	/**
	 * A supplier for list of templates of {@link FormDefinition}s.
	 */
	public Supplier<? extends List<FormDefinitionTemplate>> getTemplateProvider() {
		return _templateProvider;
	}

	/**
	 * Setter for {@link #getTemplateProvider()}.
	 */
	public void setTemplateProvider(Supplier<? extends List<FormDefinitionTemplate>> templateProvider) {
		_templateProvider = Objects.requireNonNull(templateProvider);
	}
}