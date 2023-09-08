/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.values.edit;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.layout.form.values.edit.initializer.InitializerProvider;

/**
 * Test case for {@link EditorFactory}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestEditorFactory extends TestCase {

	public interface A extends ConfigurationItem {
		String getValue();

		void setValue(String value);
	}

	public void testFieldsInitiallyNotChanged() {
		FormGroup group = new FormGroup("group", null);
		InitializerProvider init = new InitializerIndex();
		A model = TypedConfiguration.newConfigItem(A.class);
		model.setValue("Hello world!");
		EditorFactory.initEditorGroup(group, model, init);
		assertFalse("Fields initially changed.", group.isChanged());
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(TestEditorFactory.class);
	}
}
