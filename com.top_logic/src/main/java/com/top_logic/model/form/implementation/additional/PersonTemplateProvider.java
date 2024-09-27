/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.form.implementation.additional;

import java.util.function.Consumer;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.knowledge.wrap.person.PersonalConfigurationWrapper;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * {@link FormElementTemplateProvider} to additionally add {@link HTMLTemplateFragment} to display
 * non-model based aspects of a Person.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class PersonTemplateProvider implements FormElementTemplateProvider {

	@Override
	public HTMLTemplateFragment createTemplate(FormEditorContext context) {
		TLObject model = context.getModel();
		String fieldName = fieldName();
		FormField field;
		if (model instanceof Person) {
			field = createField(context, fieldName, asPerson(model));
		} else {
			StringField noPersonField = FormFactory.newStringField(fieldName, FormFactory.IMMUTABLE);
			noPersonField.initializeField(Resources.getInstance().getString(I18NConstants.NO_ACCOUNT_AVAILABLE));
			field = noPersonField;
		}
		field.setLabel(fieldLabel());
		context.getContentGroup().addMember(field);
		if (field instanceof BooleanField) {
			return Templates.fieldBoxInputFirst(field.getName());
		} else {
			return Templates.fieldBox(field.getName());
		}
	}

	private static Person asPerson(TLObject model) {
		return (Person) model;
	}

	/**
	 * Creates the field fo display.
	 * 
	 * @param context
	 *        Context in of the template provider evaluation.
	 * @param fieldName
	 *        name for the result field.
	 * @param account
	 *        The edited {@link Person}.
	 */
	protected abstract FormField createField(FormEditorContext context, String fieldName, Person account);

	/**
	 * Name of the field to display.
	 */
	protected abstract String fieldName();

	/**
	 * Label for the new field.
	 */
	protected abstract ResKey fieldLabel();

	@Override
	public boolean getWholeLine(TLStructuredType modelType) {
		return false;
	}

	/**
	 * The {@link PersonalConfiguration} of the given {@link Person}.
	 * 
	 * <p>
	 * If the given {@link Person} is the current user, the transient configuration is returned.
	 * </p>
	 * 
	 * @param account
	 *        The {@link Person} to get {@link PersonalConfiguration} for.
	 * 
	 * @return May be <code>null</code>.
	 */
	protected PersonalConfiguration getPersonalConfiguration(Person account) {
		PersonalConfiguration pc;
		if (Utils.equals(account, currentPerson())) {
			pc = PersonalConfiguration.getPersonalConfiguration();
		} else {
			pc = PersonalConfigurationWrapper.getPersonalConfiguration(account);
		}
		return pc;
	}

	/**
	 * Executes the given store algorithm on the personal configuration of the given account. If the
	 * current {@link Person} is edited, the store algorithm is additionally executed on the
	 * transient {@link PersonalConfiguration} of the session.
	 */
	protected void storeToPersonalConfiguration(Person account, Consumer<PersonalConfiguration> storeAlgorithm) {
		PersonalConfigurationWrapper pcw = PersonalConfigurationWrapper.createPersonalConfiguration(account);
		storeAlgorithm.accept(pcw);
		if (Utils.equals(account, currentPerson())) {
			PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
			storeAlgorithm.accept(pc);
		}
	}

	private Person currentPerson() {
		return TLContext.getContext().getCurrentPersonWrapper();
	}

}

