/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.ConfigTextFieldModel;

/**
 * Tests editing a configuration property whose values are not strings through its textual
 * specification.
 */
public class TestConfigTextFieldModel extends TestCase {

	/**
	 * A value with a textual specification, like a channel reference in a view.
	 *
	 * @param name
	 *        The reference target.
	 */
	public record Ref(String name) {

		/** Converts a {@link Ref} to and from its specification. */
		public static class ValueFormat extends com.top_logic.basic.config.AbstractConfigurationValueProvider<Ref> {

			/** Singleton {@link ValueFormat} instance. */
			public static final ValueFormat INSTANCE = new ValueFormat();

			private ValueFormat() {
				super(Ref.class);
			}

			@Override
			protected Ref getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws com.top_logic.basic.config.ConfigurationException {
				String text = propertyValue.toString();
				if (text.contains(" ")) {
					throw new com.top_logic.basic.config.ConfigurationException(
						com.top_logic.basic.util.ResKey.text("A reference must not contain a space."),
						propertyName, propertyValue);
				}
				return new Ref(text);
			}

			@Override
			protected String getSpecificationNonNull(Ref configValue) {
				return configValue.name();
			}
		}
	}

	/**
	 * Configuration with a formatted and a plain property.
	 */
	public interface Config extends ConfigurationItem {

		/** Property name for {@link #getInput()}. */
		String INPUT = "input";

		/** Property name for {@link #getLabel()}. */
		String LABEL = "label";

		@Name(INPUT)
		@Format(Ref.ValueFormat.class)
		Ref getInput();

		/** @see #getInput() */
		void setInput(Ref value);

		@Name(LABEL)
		String getLabel();
	}

	private Config _config;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_config = TypedConfiguration.newConfigItem(Config.class);
	}

	private PropertyDescriptor property(String name) {
		return _config.descriptor().getProperty(name);
	}

	/**
	 * Only a property whose values are not strings needs the textual detour.
	 */
	public void testFormattedProperties() {
		assertTrue("A formatted property is edited as text", ConfigTextFieldModel.isFormatted(property(Config.INPUT)));
		assertFalse("A string property is edited directly", ConfigTextFieldModel.isFormatted(property(Config.LABEL)));
	}

	/**
	 * Entering a specification stores the parsed value, which is what the configuration can hold.
	 */
	public void testSetSpecification() {
		ConfigTextFieldModel model = new ConfigTextFieldModel(_config, property(Config.INPUT));

		model.setValue("model");

		assertEquals("The parsed value reached the configuration", new Ref("model"), _config.getInput());
		assertEquals("The specification is displayed", "model", model.getValue());
		assertFalse(model.hasError());
	}

	/**
	 * Without the conversion the property would be handed a string it cannot store.
	 */
	public void testPlainModelCannotStoreText() {
		ConfigFieldModel plain = new ConfigFieldModel(_config, property(Config.INPUT));
		try {
			plain.setValue("model");
		} catch (RuntimeException ex) {
			// Rejected outright, which is the failure the text model avoids.
			return;
		}
		assertFalse("A string must not end up in a Ref-typed property",
			_config.value(property(Config.INPUT)) instanceof String);
	}

	/**
	 * Text the format cannot parse is reported on the field and leaves the configuration unchanged.
	 */
	public void testInvalidSpecificationIsReported() {
		_config.setInput(new Ref("kept"));
		ConfigTextFieldModel model = new ConfigTextFieldModel(_config, property(Config.INPUT));

		model.setValue("not a reference");

		assertTrue("The problem is reported on the field", model.hasError());
		assertEquals("The stored value is unchanged", new Ref("kept"), _config.getInput());
	}

	/**
	 * Clearing the input unsets the property.
	 */
	public void testEmptyClearsValue() {
		_config.setInput(new Ref("model"));
		ConfigTextFieldModel model = new ConfigTextFieldModel(_config, property(Config.INPUT));

		model.setValue("");

		assertNull("The property is cleared", _config.getInput());
		assertEquals("An unset property displays as empty text", "", model.getValue());
	}

}
