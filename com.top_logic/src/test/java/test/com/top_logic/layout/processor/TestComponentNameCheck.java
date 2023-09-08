/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.processor;

import junit.framework.Test;

import test.com.top_logic.ModuleLicenceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.layout.processor.ComponentNameCheck;
import com.top_logic.layout.processor.ResolveFailure;

/**
 * Test case for {@link ComponentNameCheck}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestComponentNameCheck extends LayoutModelTest {

	public TestComponentNameCheck(String name) {
		super(name);
	}

	public void testOk() throws ResolveFailure {
		checkOk("test-duplicate-names-1.xml");
	}

	public void testDuplicateInExpansion() throws ResolveFailure {
		checkFail("test-duplicate-names-2.xml");
	}

	public void testDuplicateInfoComponentNames() throws ResolveFailure {
		checkFail("test-duplicate-names-3.xml");
	}

	protected void checkOk(String rootLayoutName) throws ResolveFailure {
		new ComponentNameCheck(getProtocol()).check(getResolver(), qualifyLayout(rootLayoutName));
	}

	private static String qualifyLayout(String layoutName) {
		return TestComponentNameCheck.class.getName() + '/' + layoutName;
	}

	protected void checkFail(String rootLayoutName) throws ResolveFailure {
		BufferingProtocol log = new BufferingProtocol();
		new ComponentNameCheck(log).check(getResolver(), qualifyLayout(rootLayoutName));
		assertTrue("Expected report of check failure.", log.hasErrors());
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(TestComponentNameCheck.class);
	}

}
