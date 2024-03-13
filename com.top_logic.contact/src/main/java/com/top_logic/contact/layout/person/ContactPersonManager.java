/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person;

import com.top_logic.basic.config.InstantiationContext;
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
		PersonContact user = ContactFactory.getInstance().createNewPersonContact(loginName, null);
		account.setUser(user);
	}

}
