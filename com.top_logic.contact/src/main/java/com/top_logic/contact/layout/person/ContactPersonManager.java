/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.business.AbstractContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.knowledge.wrap.person.TLPersonManager;

/**
 * {@link PersonManager} that initializes new accounts with contact information.
 */
public class ContactPersonManager extends TLPersonManager {

	/**
	 * Creates a {@link ContactPersonManager}.
	 */
	public ContactPersonManager(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void initUser(Person account) {
		String loginName = account.getName();

		PersonContact user = null;
		for (Object existing : ContactFactory.getInstance().getAllContactsWithAttribute(ContactFactory.PERSON_TYPE,
			AbstractContact.FKEY_ATTRIBUTE, loginName, false)) {
			if (existing instanceof PersonContact existingContact) {
				if (existingContact.getPerson() == null) {
					if (user != null) {
						// Not unique.
						Logger.info("Contact for new account is not unique: " + loginName, ContactPersonManager.class);
						user = null;
						break;
					}
					user = existingContact;
				}
			}
		}

		if (user == null) {
			user = ContactFactory.getInstance().createNewPersonContact(loginName, null);
			user.tUpdateByName(ContactFactory.LOGIN_NAME, account.getName());
		} else {
			Logger.info("Reusing contact for new account: " + loginName, ContactPersonManager.class);
		}
		account.setUser(user);
	}

}
