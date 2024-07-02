/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.create;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.layout.formeditor.FormEditorUtil;
import com.top_logic.element.layout.formeditor.builder.ConfiguredDynamicFormBuilder;
import com.top_logic.element.layout.formeditor.builder.TypedForm;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.values.edit.FormBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.model.util.TLModelPartRef;

/**
 * {@link FormBuilder} that allows to create a form for objects of a configured concrete type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MonomorphicCreateFormBuilder extends ConfiguredDynamicFormBuilder {

	/**
	 * Configuration options for {@link MonomorphicCreateFormBuilder} that are directly
	 * user-configurable.
	 */
	public interface UIOptions extends ConfigurationItem {
		/**
		 * @see #getFormType()
		 */
		String FORM_TYPE = "formType";

		/**
		 * The concrete type to build the form for.
		 * 
		 * <p>
		 * The displayed form can show all properties of the given type. By default, the form uses
		 * the the form definition annotated to the type.
		 * </p>
		 */
		@Name(FORM_TYPE)
		@Mandatory
		TLModelPartRef getFormType();
	}

	/**
	 * Configuration options for {@link MonomorphicCreateFormBuilder}.
	 */
	public interface Config extends ConfiguredDynamicFormBuilder.Config, UIOptions {
		// Pure sum interface.
	}

	private final TLStructuredType _type;

	/**
	 * Creates a {@link MonomorphicCreateFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MonomorphicCreateFormBuilder(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		_type = config.getFormType().resolveClass();
    }

	@Override
	public Object getModel(Object businessModel, LayoutComponent component) {
		AttributeFormContext formContext = new AttributeFormContext(component.getResPrefix());

		fillFormContext(component, formContext, businessModel);

		return formContext;
	}

	/**
	 * The type of the form being built.
	 */
	protected final TLStructuredType getFormType() {
		return _type;
	}

	/**
	 * Creates the {@link FormContext} contents.
	 * 
	 * @param component
	 *        The context component.
	 * @param formContext
	 *        The {@link FormContext} currently being constructed.
	 * @param businessModel
	 *        The context component's model.
	 * @return The {@link TLFormObject} containing the properties of the created form.
	 */
	protected TLFormObject fillFormContext(LayoutComponent component, AttributeFormContext formContext,
			Object businessModel) {
		TLObject container = businessModel instanceof TLObject ? (TLObject) businessModel : null;
		TLStructuredType type = getFormType();
		TLFormObject newCreation = formContext.createObject(type, null, container);

		FormContainer editorGroup = formContext.createFormContainerForOverlay(newCreation);
		formContext.addMember(editorGroup);
		TypedForm typedForm = TypedForm.lookup(getConfiguredForms(), type);
		setDisplayedTypedForm(typedForm);
		FormEditorContext context = new FormEditorContext.Builder()
			.formMode(FormMode.CREATE)
			.formType(typedForm.getFormType())
			.formContext(formContext)
			.contentGroup(editorGroup)
			.concreteType(type)
			.build();
		FormEditorUtil.createAttributes(context, typedForm.getFormDefinition());

		return newCreation;
	}

	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		return true;
	}
}
