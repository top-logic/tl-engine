/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.contact.layout.scripting;

import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.scripting.recorder.ref.GlobalModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link GlobalModelNamingScheme} to identity a contact by the user name of its account.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ContactByAccountNamingScheme
		extends GlobalModelNamingScheme<PersonContact, ContactByAccountNamingScheme.Name> {

	/**
	 * {@link ModelName} to identity a contact by the user name of its account.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Label("Contact with user name '{name}'")
	public interface Name extends ModelName, NamedConfigMandatory {

		/**
		 * The user name of the desired contact.
		 */

		@Override
		String getName();

	}

	/**
	 * Creates a new {@link ContactByAccountNamingScheme}.
	 */
	public ContactByAccountNamingScheme() {
		super(PersonContact.class, Name.class);
	}

	@Override
	public Maybe<Name> buildName(PersonContact model) {
		Person person = model.getPerson();
		if (person == null) {
			return Maybe.none();
		}
		Name name = TypedConfiguration.newConfigItem(Name.class);
		name.setName(person.getUser().getUserName());
		return Maybe.some(name);
	}

	@Override
	public PersonContact locateModel(ActionContext context, Name name) {
		Person person = Person.byName(name.getName());
		if (person == null) {
			throw ApplicationAssertions.fail(name, "No Person with name '" + name.getName() + "' available.");
		}
		UserInterface user = person.getUser();
		if (user == null) {
			throw ApplicationAssertions.fail(name, "Person with name '" + name.getName() + "' without contact.");
		}
		return (PersonContact) user;
	}

}

