/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.structure;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.structure.Expandable.ExpansionState;
import com.top_logic.layout.structure.PersonalizingExpandable;

/**
 * Test for {@link PersonalizingExpandable}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestPersonalizingExpandable extends BasicTestCase {

	public void testPersonalizing() {
		String personalizedKey = getClass().getName() + "#testPersonalizing";
		PersonalizingExpandable expandable = new PersonalizingExpandable(personalizedKey);
		expandable.setExpansionState(ExpansionState.MINIMIZED);
		assertEquals(expandable.getExpansionState(), ExpansionState.MINIMIZED);

		expandable = new PersonalizingExpandable(personalizedKey);
		assertEquals(expandable.getExpansionState(), ExpansionState.MINIMIZED);

		expandable.setExpansionState(ExpansionState.NORMALIZED);
		assertEquals(expandable.getExpansionState(), ExpansionState.NORMALIZED);

		expandable = new PersonalizingExpandable(personalizedKey);
		assertEquals(expandable.getExpansionState(), ExpansionState.NORMALIZED);
	}

	public void testInitialMinimized() {
		String personalizedKey = getClass().getName() + "#testInitialMinimized";
		PersonalizingExpandable expandable = new PersonalizingExpandable(personalizedKey, true);
		assertEquals(expandable.getExpansionState(), ExpansionState.MINIMIZED);

		expandable.setExpansionState(ExpansionState.NORMALIZED);
		assertEquals(expandable.getExpansionState(), ExpansionState.NORMALIZED);

		expandable = new PersonalizingExpandable(personalizedKey, true);
		assertEquals("InitialMinimized must be overridden by personal config.", expandable.getExpansionState(),
			ExpansionState.NORMALIZED);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestPersonalizingExpandable}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestPersonalizingExpandable.class);
	}

}
