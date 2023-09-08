/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.FieldResolver;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;
import com.top_logic.layout.scripting.recorder.ref.ReferenceFactory;
import com.top_logic.layout.scripting.recorder.ref.ui.form.DefaultFormMemberNaming;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * The {@link ModelNamingScheme} for all {@link FormMember}s that don't need any special treatment.
 * 
 * @deprecated Use {@link DefaultFormMemberNaming}.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class FormMemberNamingScheme extends AbstractModelNamingScheme<FormMember, FormMemberBasedName> {

	/**
	 * Singleton {@link FormMemberNamingScheme} instance.
	 */
	public static final FormMemberNamingScheme INSTANCE = new FormMemberNamingScheme();

	private FormMemberNamingScheme() {
		/* Private singleton constructor */
	}

	@Override
	public Class<? extends FormMemberBasedName> getNameClass() {
		return FormMemberName.class;
	}

	@Override
	public Class<FormMember> getModelClass() {
		return FormMember.class;
	}

	@Override
	public FormMember locateModel(ActionContext context, FormMemberBasedName name) {
		ModelName formHandlerName = name.getFormHandlerName();
		FormHandler formHandler =
			(FormHandler) ModelResolver.locateModel(context, formHandlerName);
		if (formHandler == null) {
			throw ApplicationAssertions.fail(name, "Cannot resolve form: " + formHandlerName);
		}
		FormContext formContext = formHandler.getFormContext();
		if (formContext == null) {
			throw ApplicationAssertions.fail(name, "Got no form context from: " + formHandlerName);
		}
		return new FieldResolver(context).resolveFormMember(formContext, name.getPath());
	}

	@Override
	protected void initName(FormMemberBasedName name, FormMember formMember) {
		NamedModel formHandler = formMember.getFormContext().getOwningModel();
		ApplicationAssertions.assertNotNull(name, "Form member '" + formMember.getQualifiedName()
			+ "' without form handler.", formHandler);

		name.setFormHandlerName(formHandler.getModelName());
		name.setPath(ReferenceFactory.referenceField(formMember));
	}

	@Override
	protected boolean isCompatibleModel(FormMember formMember) {
		return FormComponent.formHandlerForMember(formMember) != null;
	}
}
