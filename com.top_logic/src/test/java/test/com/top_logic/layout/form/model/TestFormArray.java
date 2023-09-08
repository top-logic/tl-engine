/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.FormArray;

/**
 * Test case for {@link FormArray}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestFormArray extends AbstractFormContainerTest {

	@Override
	protected FormContainer createContainer(String name, ResPrefix i18n) {
		return new FormArray(name, i18n);
	}

	@Override
	public void testForNameUniqueness() {
		// This is not strictly required. The only requirement is that atomic input elements have
		// unique qualified names.
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(new TestSuite(TestFormArray.class));
	}

}
