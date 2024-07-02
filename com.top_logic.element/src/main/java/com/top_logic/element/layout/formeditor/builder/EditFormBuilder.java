/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.builder;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.element.layout.formeditor.FormEditorUtil;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.values.edit.FormBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link FormBuilder} that displays a configured {@link FormDefinition} to a
 * {@link TLStructuredType}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class EditFormBuilder extends ConfiguredDynamicFormBuilder {

	private TLType _type;

	private boolean _showNoModel;

	/**
	 * Configuration for a dynamic {@link FormBuilder}.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	public interface Config extends ConfiguredDynamicFormBuilder.Config, UIOptions {
		// Pure sum interface.
	}

	/**
	 * Options of {@link EditFormBuilder} that can directly customized in the UI.
	 */
	public interface UIOptions extends ConfigurationItem {
		/**
		 * @see #getType()
		 */
		String TYPE = "type";

		/**
		 * @see #getShowNoModel()
		 */
		String SHOW_NO_MODEL = "showNoModel";

		/**
		 * The required model type of the displayed object.
		 * 
		 * <p>
		 * No value means that objects of all types are acceptable.
		 * </p>
		 * 
		 * <p>
		 * If a value of an incompatible type is received, the component hides itself. The
		 * <code>null</code> value is handled especially. A component stays visible when it's model
		 * is <code>null</code>, only if the {@link #getShowNoModel()} option is set.
		 * </p>
		 */
		@Name(TYPE)
		@Nullable
		TLModelPartRef getType();

		/**
		 * Whether the component should display, even if it's model is <code>null</code>. In this
		 * case, a "no model" message is displayed instead of hiding the component.
		 */
		@Name(SHOW_NO_MODEL)
		@Label("Show without model")
		@BooleanDefault(true)
		boolean getShowNoModel();
	}

	/**
	 * Creates a {@link ConfiguredDynamicFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public EditFormBuilder(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		_type = resolveOptional(config.getType());
		_showNoModel = config.getShowNoModel();
	}

	private TLType resolveOptional(TLModelPartRef type) throws ConfigurationException {
		return type == null ? null : type.resolveClass();
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent component) {
		if (businessModel == null) {
			return null;
		}

		AttributeFormContext formContext = new AttributeFormContext(component.getResPrefix());

		// Note: Early initialize the form context to allow access to the component while building
		// the form.
		FormComponent.initFormContext(component, (FormHandler) component, formContext);

		TLObject object = (TLObject) businessModel;
		setDisplayedTypedForm(TypedForm.lookup(getConfiguredForms(), object));

		TypedForm typedForm = getDisplayedTypedForm();
		FormEditorContext context = new FormEditorContext.Builder()
			.formMode(FormMode.EDIT)
			.formType(typedForm.getFormType())
			.concreteType(typedForm.getDisplayedType())
			.model(object)
			.formContext(formContext)
			.contentGroup(formContext)
			.build();
		
		FormEditorUtil.createAttributes(context, typedForm.getFormDefinition());
		return formContext;
	}

	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		if (model == null) {
			return _showNoModel;
		}

		return isTypeInstance(model);
	}

	private boolean isTypeInstance(Object model) {
		return _type == null
			|| ((model instanceof TLObject) && TLModelUtil.isCompatibleInstance(_type, model));
	}
}
