/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.contact.layout.scripting;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.UnrecordableNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * Conversion of a {@link Person} to its {@link PersonContact} or from a {@link PersonContact} to
 * its account, if it exists.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ContactAccountConversionNaming
		extends UnrecordableNamingScheme<Object, ContactAccountConversionNaming.Name> {

	/**
	 * Conversion of a {@link Person} to its {@link PersonContact} or from a {@link PersonContact}
	 * to its account, if it exists.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Label("Account vs. Contact conversion: {source}")
	public interface Name extends ModelName {

		/**
		 * Name representing a {@link Person} or {@link PersonContact}.
		 */
		@Mandatory
		ModelName getSource();
	}

	/** 
	 * Creates a new {@link ContactAccountConversionNaming}.
	 */
	public ContactAccountConversionNaming() {
		super(Object.class, Name.class);
	}

	@Override
	public Object locateModel(ActionContext context, Name name) {
		Object source = context.resolve(name.getSource());
		if (source instanceof Person) {
			return ((Person) source).getUser();
		} else if (source instanceof PersonContact) {
			return ((PersonContact) source).getPerson();
		}
		throw ApplicationAssertions.fail(name, "Unable to resolve name to Person or PersonContact.");
	}

}

