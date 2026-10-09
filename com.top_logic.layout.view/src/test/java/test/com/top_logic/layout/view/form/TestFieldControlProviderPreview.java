/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.configedit.PolymorphicOptions;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.view.form.BinaryControlProvider;
import com.top_logic.layout.view.form.BooleanControlProvider;
import com.top_logic.layout.view.form.ColorInputControlProvider;
import com.top_logic.layout.view.form.ConfigFieldControlProvider;
import com.top_logic.layout.view.form.DatePickerControlProvider;
import com.top_logic.layout.view.form.I18NStringControlProvider;
import com.top_logic.layout.view.form.IconSelectControlProvider;
import com.top_logic.layout.view.form.NumberInputControlProvider;
import com.top_logic.layout.view.form.PasswordInputControlProvider;
import com.top_logic.layout.view.form.SelectControlProvider;
import com.top_logic.layout.view.form.TextInputControlProvider;

/**
 * Tests which field control providers of the view layer need more room than a single line of text,
 * and what stands for their value where they have none.
 *
 * @see ReactFieldControlProvider#isLarge(FieldSpec)
 * @see ReactFieldControlProvider#previewText(FieldSpec, Object)
 */
public class TestFieldControlProviderPreview extends TestCase {

	/** A configuration edited by {@link ConfigFieldControlProvider}. */
	public interface Edited extends ConfigurationItem {
		// No properties needed.
	}

	/**
	 * A text is large where it is displayed on several rows.
	 */
	public void testTextIsLargeWhenMultiline() {
		TextInputControlProvider provider =
			new TextInputControlProvider(null, TypedConfiguration.newConfigItem(TextInputControlProvider.Config.class));

		assertFalse(provider.isLarge(FieldSpec.of(String.class, "Text")));
		assertFalse(provider.isLarge(FieldSpec.of(String.class, "Text").setMultilineRows(1)));
		assertTrue(provider.isLarge(FieldSpec.of(String.class, "Text").setMultilineRows(5)));
		assertEquals("First", provider.previewText(FieldSpec.of(String.class, "Text"), "First\nSecond"));
	}

	/**
	 * An internationalized text is large where it is displayed on several rows, and is previewed in
	 * the user's language.
	 */
	public void testI18NStringIsLargeWhenMultiline() {
		I18NStringControlProvider provider = new I18NStringControlProvider();

		assertFalse(provider.isLarge(FieldSpec.of(ResKey.class, "Text")));
		assertTrue(provider.isLarge(FieldSpec.of(ResKey.class, "Text").setMultilineRows(5)));
		assertEquals("First",
			provider.previewText(FieldSpec.of(ResKey.class, "Text"), ResKey.text("First\nSecond")));
	}

	/**
	 * A configuration is edited in a form of its own, and its kind stands for it.
	 */
	public void testConfigurationIsLarge() {
		ConfigFieldControlProvider provider = new ConfigFieldControlProvider();
		FieldSpec field = FieldSpec.of(Edited.class, "Config");
		String kind = PolymorphicOptions.labelFor(Edited.class);

		assertTrue(provider.isLarge(field));
		assertEquals("", provider.previewText(field, null));
		assertEquals(kind, provider.previewText(field, TypedConfiguration.newConfigItem(Edited.class)));
		assertEquals(kind + ", " + kind, provider.previewText(field,
			List.of(TypedConfiguration.newConfigItem(Edited.class), TypedConfiguration.newConfigItem(Edited.class))));
	}

	/**
	 * The preview of a password never reveals it.
	 */
	public void testPasswordPreviewIsMasked() {
		PasswordInputControlProvider provider = new PasswordInputControlProvider();
		FieldSpec field = FieldSpec.of(String.class, "Password");
		String secret = "s3cr3t";

		assertFalse(provider.isLarge(field));
		assertEquals("", provider.previewText(field, null));
		assertEquals("", provider.previewText(field, ""));
		String preview = provider.previewText(field, secret);
		assertFalse("The password must not be revealed: " + preview, preview.contains(secret));
		assertFalse("The length of the password must not be revealed.",
			preview.length() == secret.length());
		assertFalse(preview.isEmpty());
	}

	/**
	 * Controls taking a single line of text are not large.
	 */
	public void testSingleLineControlsAreNotLarge() {
		FieldSpec field = FieldSpec.of(Object.class, "Value");
		for (ReactFieldControlProvider provider : List.of(
			new BinaryControlProvider(),
			new ColorInputControlProvider(),
			new IconSelectControlProvider(),
			new NumberInputControlProvider(),
			new DatePickerControlProvider(),
			new BooleanControlProvider(),
			new SelectControlProvider())) {
			assertFalse(provider.getClass().getName() + " takes a single line.", provider.isLarge(field));
		}
	}

	/**
	 * Test suite providing the resources and label providers the previews are resolved with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestFieldControlProviderPreview.class, ResourcesModule.Module.INSTANCE,
				LabelProviderService.Module.INSTANCE));
	}

}
