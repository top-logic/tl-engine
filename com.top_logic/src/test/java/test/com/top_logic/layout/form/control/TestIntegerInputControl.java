/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.control;

import junit.framework.Test;

import test.com.top_logic.layout.TestControl;

import com.top_logic.layout.form.control.IntegerInputControl;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.IntField;

/**
 * Test case for {@link IntegerInputControl}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
@SuppressWarnings("javadoc")
public class TestIntegerInputControl extends TestControl {

	public void testCreateControlForFormField() {
		IntField intField = FormFactory.newIntField("testField");
		assertInstanceof(IntegerInputControl.Provider.INSTANCE.createControl(intField),
			IntegerInputControl.class);
	}

	public void testCreateNullForNonFormField() {
		assertNull(IntegerInputControl.Provider.INSTANCE.createControl(17));
	}

	public static Test suite() {
		return TestControl.suite(TestIntegerInputControl.class);
	}

}
