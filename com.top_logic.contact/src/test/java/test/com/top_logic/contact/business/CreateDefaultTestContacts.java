/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package test.com.top_logic.contact.business;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.person.CreateDefaultTestPersons;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * Creates some test persons including a contact, often used.
 * 
 * @see CreateDefaultTestPersons
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateDefaultTestContacts extends CreateDefaultTestPersons {

	/**
	 * Creates a new {@link CreateDefaultTestContacts}.
	 */
	public CreateDefaultTestContacts(Test test) {
		super(test);
	}

	@Override
	protected Person createTestUser1() {
		Person testUser1 = super.createTestUser1();
		testUser1.getUser().setFirstName("test-user1");
		testUser1.getUser().setName("test-user1");
		testUser1.getUser().setEMail("user1@develop.local");
		return testUser1;
	}

	@Override
	protected Person createDau() {
		Person dau = super.createDau();
		dau.getUser().setFirstName("Dieter");
		dau.getUser().setName("Aumann");
		dau.getUser().setTitle("Dipl. Ing.");
		dau.getUser().setPhone("+49 1234 111104");
		dau.getUser().setEMail("info3@top-logic.com");
		return dau;
	}

	@Override
	protected Person createDummy() {
		Person dummy = super.createDummy();
		dummy.getUser().setFirstName("Morty");
		dummy.getUser().setName("Dugar");
		dummy.getUser().setPhone("+44 111 2222203");
		dummy.getUser().setEMail("info5@top-logic.co.uk");
		return dummy;
	}

	@Override
	protected Person createGuestDE() {
		Person guestDE = super.createGuestDE();
		guestDE.getUser().setFirstName("Deutscher");
		guestDE.getUser().setName("Gast");
		return guestDE;
	}

	@Override
	protected Person createGuestEN() {
		Person guestEN = super.createGuestEN();
		guestEN.getUser().setFirstName("English");
		guestEN.getUser().setName("Guest");
		return guestEN;
	}

}

