/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.template;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;

/**
 * Test case for {@link DefaultFormFieldControlProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDefaultFormFieldControlProvider extends TestCase {

	public void testDispatch() {
		ControlProvider cp = new ControlProvider() {
			@Override
			public Control createControl(Object model, String style) {
				BlockControl result = new BlockControl();
				result.addChild(DefaultFormFieldControlProvider.INSTANCE.createControl(model, style));
				return result;
			}
		};

		StringField field = FormFactory.newStringField("foo");

		field.setControlProvider(cp);

		Control control = DefaultFormFieldControlProvider.INSTANCE.createControl(field);
		assertNotNull(control);
		BasicTestCase.assertInstanceof(control, BlockControl.class);
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(TestDefaultFormFieldControlProvider.class);
	}
}
