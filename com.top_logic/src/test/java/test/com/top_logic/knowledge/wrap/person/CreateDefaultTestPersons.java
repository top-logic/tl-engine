/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package test.com.top_logic.knowledge.wrap.person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import junit.framework.Test;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.Country;

/**
 * Creates some test persons, often used.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class CreateDefaultTestPersons extends CreateTestPersons {

	/**
	 * Creates a new {@link CreateDefaultTestPersons}.
	 */
	public CreateDefaultTestPersons(Test test) {
		super(test);
	}

	@Override
	protected Collection<? extends Person> createPersons() {
		List<Person> persons = new ArrayList<>();

		persons.add(createTestUser1());
		persons.add(createDau());
		persons.add(createDummy());
		persons.add(createGuestDE());
		persons.add(createGuestEN());

		return persons;
	}

	protected Person createGuestEN() {
		Person guestEN = Person.create(kb(), "guest_en", defaultAuthenticationDevice());
		guestEN.setCountry(new Country(Locale.UK.getCountry()));
		guestEN.setLanguage(Locale.ENGLISH);
		return guestEN;
	}

	protected Person createGuestDE() {
		Person guestDE = Person.create(kb(), "guest_de", defaultAuthenticationDevice());
		guestDE.setCountry(new Country(Locale.GERMANY.getCountry()));
		guestDE.setLanguage(Locale.GERMAN);
		return guestDE;
	}

	protected Person createDummy() {
		Person dummy = Person.create(kb(), "dummy", defaultAuthenticationDevice());
		dummy.setCountry(new Country(Locale.UK.getCountry()));
		dummy.setLanguage(Locale.ENGLISH);
		return dummy;
	}

	protected Person createDau() {
		Person dau = Person.create(kb(), "dau", defaultAuthenticationDevice());
		dau.setCountry(new Country(Locale.GERMANY.getCountry()));
		dau.setLanguage(Locale.GERMAN);
		return dau;
	}

	protected Person createTestUser1() {
		return Person.create(kb(), "test-user1", defaultAuthenticationDevice());
	}

	private String defaultAuthenticationDevice() {
		TLSecurityDeviceManager sdm = TLSecurityDeviceManager.getInstance();
		return sdm.getDefaultAuthenticationDevice().getDeviceID();
	}

}

