/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.configedit.TestConfigEditorControl.NoFormatBinding;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.mime.MimeTypesModule;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigEditorControl;
import com.top_logic.layout.configedit.ConfigFieldIndex;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.ConfigFormatFieldModel;
import com.top_logic.layout.form.values.edit.annotation.Html;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests for {@link ConfigEditorControl} over {@link ResKey} properties holding a literal key with a
 * text per language, the form an internationalized text takes in a configuration file.
 *
 * <p>
 * Such a value has no text form - {@link ResKey.ValueFormat} writes only a non-literal key as
 * text - but the per-language editor claiming {@link ResKey.ValueFormat} edits it in its typed
 * form, so it is displayed and edited in a field, top-level as well as in a nested item.
 * </p>
 */
public class TestConfigEditorControlResKey extends TestCase {

	/** Item with a plain and an {@link Html} {@link ResKey}, the shape of a mail text. */
	public interface TextConfig extends ConfigurationItem {

		/** Property name for {@link #getSubject()}. */
		String SUBJECT = "subject";

		/** Property name for {@link #getBody()}. */
		String BODY = "body";

		/** The subject line. */
		@Name(SUBJECT)
		@Mandatory
		ResKey getSubject();

		/** @see #getSubject() */
		void setSubject(ResKey value);

		/** The HTML body. */
		@Name(BODY)
		@Mandatory
		@Html
		ResKey getBody();

		/** @see #getBody() */
		void setBody(ResKey value);
	}

	/** Configuration with a non-polymorphic mandatory item of {@link TextConfig}. */
	public interface OuterConfig extends ConfigurationItem {

		/** Property name for {@link #getText()}. */
		String TEXT = "text";

		/** The nested text. */
		@Name(TEXT)
		@Mandatory
		TextConfig getText();

		/** @see #getText() */
		void setText(TextConfig value);
	}

	/**
	 * A parsing-only format over a list of strings that no control of the
	 * {@link ConfigControlService} claims: it accepts no value, so a property using it has no text
	 * form.
	 */
	public static class ParseOnlyListFormat extends AbstractConfigurationValueProvider<List<String>> {

		/** Creates a {@link ParseOnlyListFormat}. */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public ParseOnlyListFormat() {
			super((Class) List.class);
		}

		@Override
		protected List<String> getValueNonEmpty(String propertyName, CharSequence propertyValue) {
			return List.of(propertyValue.toString());
		}

		@Override
		protected String getSpecificationNonNull(List<String> configValue) {
			throw new UnsupportedOperationException("There is no normative way to serialize this value.");
		}

		@Override
		public boolean isLegalValue(Object value) {
			return false;
		}
	}

	/** Configuration with {@link PropertyKind#COMPLEX} properties no control claims. */
	public interface UnclaimedConfig extends ConfigurationItem {

		/** Property name for {@link #getParseOnly()}. */
		String PARSE_ONLY = "parse-only";

		/** Property name for {@link #getBindingOnly()}. */
		String BINDING_ONLY = "binding-only";

		/** A value with an unclaimed format that writes no value as text. */
		@Name(PARSE_ONLY)
		@Format(ParseOnlyListFormat.class)
		@Binding(NoFormatBinding.class)
		List<String> getParseOnly();

		/** A value with a binding only. */
		@Name(BINDING_ONLY)
		@Binding(NoFormatBinding.class)
		List<String> getBindingOnly();
	}

	private static ResKey literal(String en, String de) {
		return ResKey.builder().add(Locale.ENGLISH, en).add(Locale.GERMAN, de).build();
	}

	private static TextConfig textConfig() {
		TextConfig text = TypedConfiguration.newConfigItem(TextConfig.class);
		text.setSubject(literal("Subject", "Betreff"));
		text.setBody(literal("<p>Body</p>", "<p>Inhalt</p>"));
		return text;
	}

	private static ReactContext createTestContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	private static PropertyDescriptor property(ConfigurationItem config, String name) {
		return config.descriptor().getProperty(name);
	}

	/**
	 * A literal key has no text form, but is edited in a field in its typed form.
	 */
	public void testLiteralResKeyIsEditedTypedInAField() {
		TextConfig text = textConfig();
		PropertyDescriptor subject = property(text, TextConfig.SUBJECT);

		assertEquals(PropertyKind.COMPLEX, subject.kind());
		assertTrue(text.getSubject().isLiteral());
		assertFalse("A literal key has no text form.", ConfigControlService.hasTextForm(text, subject));
		assertTrue(ConfigControlService.isEditedTyped(subject));
		assertFalse("A claimed key is never edited as its encoded text.",
			ConfigControlService.isEditedAsText(text, subject));
		assertTrue(ConfigControlService.isEditedInField(text, subject));

		ConfigFieldModel model = ConfigControlService.getInstance().createModel(text, subject);
		assertFalse("The literal key must be edited in its typed form, not as its encoded text.",
			model instanceof ConfigFormatFieldModel);
		assertEquals(text.getSubject(), model.getValue());
	}

	/**
	 * A top-level {@link ResKey} property with a literal value is displayed, also with {@link Html}.
	 */
	public void testTopLevelLiteralResKeyIsDisplayed() {
		TextConfig text = textConfig();
		assertTrue(ConfigEditorControl.isDisplayed(text, property(text, TextConfig.SUBJECT)));
		assertTrue(ConfigEditorControl.isDisplayed(text, property(text, TextConfig.BODY)));
		assertTrue(ConfigEditorControl.hasDisplayedProperty(text));

		List<ReactControl> fields = fields(new ConfigEditorControl(createTestContext(), text));
		assertEquals("Fields of the editor: " + fields, 2, fields.size());
	}

	/**
	 * The fields of a nested, non-polymorphic item with literal keys are part of the outer editor.
	 */
	public void testNestedItemWithLiteralResKeysHasItsFields() {
		OuterConfig outer = TypedConfiguration.newConfigItem(OuterConfig.class);
		outer.setText(textConfig());

		List<ReactControl> fields = fields(new ConfigEditorControl(createTestContext(), outer, Set.of(), false));
		assertEquals("Fields in the outer editor's tree: " + fields, 2, fields.size());
	}

	/**
	 * A literal key changed through its field is stored in the item as the new literal key.
	 */
	public void testEditedLiteralResKeyIsWrittenBack() {
		OuterConfig outer = TypedConfiguration.newConfigItem(OuterConfig.class);
		TextConfig text = textConfig();
		outer.setText(text);

		ConfigFieldIndex index = new ConfigFieldIndex();
		new ConfigEditorControl(createTestContext(), outer, Set.of(), false, index, true);

		ConfigFieldModel model = index.lookup(text, property(text, TextConfig.SUBJECT));
		assertNotNull("The subject has no field.", model);
		assertTrue(model.isEditable());

		ResKey changed = literal("Subject", "Neuer Betreff");
		model.setValue(changed);

		ResKey stored = text.getSubject();
		assertTrue(stored.isLiteral());
		assertEquals(changed, stored);
	}

	/**
	 * A {@link PropertyKind#COMPLEX} property no control claims and whose value has no text form
	 * is not displayed.
	 */
	public void testUnclaimedComplexWithoutTextFormIsHidden() {
		UnclaimedConfig config = TypedConfiguration.newConfigItem(UnclaimedConfig.class);
		for (String name : List.of(UnclaimedConfig.PARSE_ONLY, UnclaimedConfig.BINDING_ONLY)) {
			PropertyDescriptor property = property(config, name);
			assertEquals(name, PropertyKind.COMPLEX, property.kind());
			assertFalse(name, ConfigControlService.isEditedTyped(property));
			assertFalse(name, ConfigControlService.isEditedInField(config, property));
			assertFalse(name, ConfigEditorControl.isDisplayed(config, property));
		}
		assertFalse(ConfigEditorControl.hasDisplayedProperty(config));
	}

	private static List<ReactControl> fields(ReactControl control) {
		List<ReactControl> fields = new ArrayList<>();
		collectFields(control, fields);
		return fields;
	}

	private static void collectFields(ReactControl control, List<ReactControl> fields) {
		for (ReactControl child : control.displayedChildren()) {
			if (child instanceof ReactFormFieldChromeControl) {
				fields.add(child);
			}
			collectFields(child, fields);
		}
	}

	/**
	 * Suite with the services the editor and the per-language field need.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigEditorControlResKey.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE, FormatterService.Module.INSTANCE,
				MimeTypesModule.Module.INSTANCE, ThemeFactory.Module.INSTANCE, SafeHTML.Module.INSTANCE,
				ConfigControlService.Module.INSTANCE));
	}
}
