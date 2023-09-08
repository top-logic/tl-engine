/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap.person;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.knowledge.wrap.person.PersonalConfigurationWrapper;

/**
 * {@link TestPersonalConfigurationImplementation} testing {@link PersonalConfigurationWrapper}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestPersonalConfigurationWrapper extends TestPersonalConfigurationImplementation {

	private Person _testPerson;

	/**
	 * Test {@link Person} is created in {@link #newEmptyPersonConfiguration()}.
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		TestPerson.deletePersonAndUser(_testPerson);
		super.tearDown();
	}

	@Override
	protected PersonalConfiguration newEmptyPersonConfiguration() {
		_testPerson = TestPerson.createPerson("TestPersonalConfigurationWrapper");
		return PersonalConfigurationWrapper.createPersonalConfiguration(_testPerson);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestPersonalConfigurationWrapper}.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(new TestSuite(TestPersonalConfigurationWrapper.class));
	}

}
