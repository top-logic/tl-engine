/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
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
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link FormBuilder} that allows to create a form for objects of a configured concrete type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
@Label("Generic form")
public class GenericFormBuilderByExpression extends ConfiguredDynamicFormBuilder {

	/**
	 * Configuration options for {@link GenericFormBuilderByExpression} that are directly
	 * user-configurable.
	 */
	public interface UIOptions extends ConfigurationItem {
		/**
		 * @see #getFormCreation()
		 */
		String FORM_CREATION = "formCreation";

		/**
		 * Function creating the form model.
		 * 
		 * <p>
		 * The function can expect the component's model as single argument and must return the
		 * object that should be edited in the form.
		 * </p>
		 * 
		 * <p>
		 * The form model may either be a transient object that only exists while the form is
		 * displayed or an existing persistent object that should be edited. However, the function
		 * must neither allocate a new persistent object nor perform any other operations requiring
		 * a transaction.
		 * </p>
		 * 
		 * <p>
		 * An object creation can be modeled by creating a transient object as form model in this
		 * function and creating a deep persistent copy of the form data during apply. As
		 * alternative, another form builder can be used that creates a form implicitly creating an
		 * object during apply. In both situations, it is advisable to enable auto-apply in the form
		 * transaction handler to copy form data to the form model.
		 * </p>
		 */
		@Name(FORM_CREATION)
		@Mandatory
		Expr getFormCreation();
	}

	/**
	 * Configuration options for {@link GenericFormBuilderByExpression}.
	 */
	public interface Config extends ConfiguredDynamicFormBuilder.Config, UIOptions {
		// Pure sum interface.
	}

	private final QueryExecutor _formCreation;

	/**
	 * Creates a {@link GenericFormBuilderByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GenericFormBuilderByExpression(InstantiationContext context, Config config) {
		super(context, config);

		_formCreation = QueryExecutor.compile(config.getFormCreation());
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent component) {
		AttributeFormContext formContext = new AttributeFormContext(component.getResPrefix());

		fillFormContext(component, formContext, businessModel);

		return formContext;
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
		Object scriptResult = _formCreation.execute(businessModel);
		if (scriptResult == null) {
			return null;
		}
		
		if (!(scriptResult instanceof TLObject)) {
			throw new TopLogicException(I18NConstants.ERROR_NO_TLOBJECT_FORM_MODEL__FORM.fill(scriptResult));
		}
		
		TLObject formObject = (TLObject) scriptResult;
		
		TLStructuredType type = formObject.tType();
		TLFormObject formOverlay = formContext.editObject(formObject);

		FormContainer editorGroup = formContext.createFormContainerForOverlay(formOverlay);
		formContext.addMember(editorGroup);
		TypedForm typedForm = TypedForm.lookup(getConfiguredForms(), type);
		setDisplayedTypedForm(typedForm);
		FormEditorContext context = new FormEditorContext.Builder()
			.formMode(FormMode.CREATE)
			.formType(typedForm.getFormType())
			.formContext(formContext)
			.contentGroup(editorGroup)
			.model(formObject)
			.concreteType(formObject.tType())
			.build();
		FormEditorUtil.createAttributes(context, typedForm.getFormDefinition());

		return formOverlay;
	}

	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		return true;
	}
}
