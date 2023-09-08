/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap.person;

import java.util.Collections;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;

import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.knowledge.wrap.person.TransientPersonalConfiguration;

/**
 * Test of class {@link TransientPersonalConfiguration}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestTransientPersonalConfiguration extends TestPersonalConfigurationImplementation {

	@Override
	protected PersonalConfiguration newEmptyPersonConfiguration() {
		return new TransientPersonalConfiguration();
	}

	public void testNoRegistration() {
		TransientPersonalConfiguration pc =
			new TransientPersonalConfiguration(Collections.singletonMap("key", "value"));
		pc.setValue("key", "value");
		assertFalse("Setting same value again does not modify.", pc.isModified());
		pc.setValue("notExisting", null);
		assertFalse("Removing not existing value does not modify.", pc.isModified());
		assertEquals("value", pc.getValue("key"));
		assertFalse("Fetching value does not modify.", pc.isModified());
		assertEquals(null, pc.getValue("notExisting"));
		assertFalse("Fetching not existing value does not modify.", pc.isModified());
	}

	public void testRegistration() {
		TransientPersonalConfiguration pc;

		pc = new TransientPersonalConfiguration(Collections.singletonMap("key", "value"));
		pc.setValue("key", null);
		assertTrue("Removing value modifies.", pc.isModified());

		pc = new TransientPersonalConfiguration(Collections.singletonMap("key", "value"));
		pc.setValue("key", "value2");
		assertTrue("Changing value modifies.", pc.isModified());

		pc = new TransientPersonalConfiguration(Collections.singletonMap("key", "value"));
		pc.setValue("key2", "value2");
		assertTrue("Adding value modifies.", pc.isModified());
	}

	/**
	 * a cumulative {@link Test} for all Tests in
	 *         {@link TestPersonalConfigurationImplementation} .
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestTransientPersonalConfiguration.class);
	}

}

