/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.values.edit;

import java.awt.Color;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.StringValueProvider;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.layout.form.control.ColorControlProvider;
import com.top_logic.layout.form.control.PasswordInputControlProvider;
import com.top_logic.layout.form.format.ColorConfigFormat;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
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

	public interface ColorProperties extends ConfigurationItem {
		@Format(ColorConfigFormat.class)
		Color getColor();

		@Format(ColorConfigFormat.class)
		@ControlProvider(PasswordInputControlProvider.class)
		Color getAnnotatedColor();

		@Format(StringValueProvider.class)
		String getText();
	}

	@Abstract
	public interface AnyValue extends ConfigurationItem {
		@Abstract
		Object getValue();
	}

	public interface ColorValue extends AnyValue {
		@Override
		@Format(ColorConfigFormat.class)
		Color getValue();
	}

	public void testColorControlProvider() {
		FormGroup group = new FormGroup("group", null);
		ColorProperties model = TypedConfiguration.newConfigItem(ColorProperties.class);
		EditorFactory.initEditorGroup(group, model, new InitializerIndex());

		assertSame("Color property is edited with the color chooser.", ColorControlProvider.INSTANCE,
			group.getField("color").getControlProvider());
		assertSame("Annotated control provider wins over the type default.",
			PasswordInputControlProvider.INSTANCE, group.getField("annotated-color").getControlProvider());
		assertNull("Formatted string property has no special control.",
			group.getField("text").getControlProvider());
	}

	public void testColorControlProviderForOverriddenProperty() {
		FormGroup group = new FormGroup("group", null);
		ColorValue model = TypedConfiguration.newConfigItem(ColorValue.class);
		EditorFactory.initEditorGroup(group, model, new InitializerIndex());

		assertSame("Property overridden with a color type is edited with the color chooser.",
			ColorControlProvider.INSTANCE, group.getField("value").getControlProvider());
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
