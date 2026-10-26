/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person;

import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.contact.business.AbstractContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * {@link PersonManager} that initializes new accounts with contact information.
 *
 * <p>
 * At startup, each account without contact information is assigned one.
 * </p>
 *
 * @see #repairAccountsWithoutContact()
 */
public class ContactPersonManager extends PersonManager {

	/**
	 * Configuration options for {@link ContactPersonManager}.
	 */
	public interface Config extends PersonManager.Config {
		/**
		 * Whether a contact that was assigned to an account is re-used for a newly created account,
		 * if the login names match.
		 */
		@BooleanDefault(true)
		@Name("reuse-contacts")
		boolean reuseContacts();
	}

	/**
	 * Creates a {@link ContactPersonManager}.
	 */
	public ContactPersonManager(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	protected void startUp() {
		super.startUp();
		repairAccountsWithoutContact();
	}

	/**
	 * Assigns contact information to each account that has none.
	 *
	 * <p>
	 * An account has no contact information, if it was created before the contact module was added
	 * to the application. Each such account is initialized with {@link #initUser(Person)} in a
	 * transaction of its own: an account that cannot be committed (e.g. because it violates another
	 * constraint) is logged and left as it is without affecting the other accounts.
	 * </p>
	 *
	 * @return The number of accounts that were assigned contact information.
	 */
	public int repairAccountsWithoutContact() {
		KnowledgeBase kb = kb();
		List<Person> accounts = Person.all(kb).stream()
			.filter(account -> account.tValid() && account.getUser() == null)
			.toList();
		if (accounts.isEmpty()) {
			return 0;
		}

		int repaired = 0;
		for (Person account : accounts) {
			try (Transaction tx = kb.beginTransaction(I18NConstants.ASSIGNED_CONTACT_TO_ACCOUNT)) {
				initUser(account);
				tx.commit();
				repaired++;
			} catch (RuntimeException ex) {
				Logger.error("Cannot assign contact data to account '" + account.getName() + "'.", ex,
					ContactPersonManager.class);
			}
		}
		Logger.info("Assigned contact data to " + repaired + " of " + accounts.size()
			+ " accounts without contact data.", ContactPersonManager.class);
		return repaired;
	}

	@Override
	public void initUser(Person account) {
		String loginName = account.getName();

		PersonContact user = null;
		if (getConfig().reuseContacts()) {
			for (Object existing : ContactFactory.getInstance().getAllContactsWithAttribute(ContactFactory.PERSON_TYPE,
				AbstractContact.FKEY_ATTRIBUTE, loginName, false)) {
				if (existing instanceof PersonContact existingContact) {
					if (existingContact.getPerson() == null) {
						if (user != null) {
							// Not unique.
							Logger.info("Contact for new account is not unique: " + loginName,
								ContactPersonManager.class);
							user = null;
							break;
						}
						user = existingContact;
					}
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
