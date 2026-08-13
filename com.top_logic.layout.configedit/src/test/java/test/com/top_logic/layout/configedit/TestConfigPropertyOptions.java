/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.configedit.ConfigPropertyOptions;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Tests for {@link ConfigPropertyOptions}.
 */
public class TestConfigPropertyOptions extends TestCase {

	/** Options function offering three fixed colours. */
	public static class Colors extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			return Arrays.asList("red", "green", "blue");
		}
	}

	/** Configuration with an annotated option list and a property without options. */
	public interface TestConfig extends ConfigurationItem {

		/** Property name for {@link #getColor()}. */
		String COLOR = "color";

		/** Property name for {@link #getText()}. */
		String TEXT = "text";

		/**
		 * One of the offered colours.
		 */
		@Name(COLOR)
		@Options(fun = Colors.class)
		String getColor();

		/** @see #getColor() */
		void setColor(String value);

		/**
		 * Free text without options.
		 */
		@Name(TEXT)
		String getText();

		/** @see #getText() */
		void setText(String value);
	}

	/**
	 * A property annotated with {@link Options} offers the function's values.
	 */
	public void testResolvesAnnotatedOptions() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COLOR);

		List<?> options = ConfigPropertyOptions.optionsFor(config, property);

		assertEquals(Arrays.asList("red", "green", "blue"), options);
	}

	/**
	 * A property without options offers none, rather than failing.
	 */
	public void testPlainPropertyHasNoOptions() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.TEXT);

		assertTrue("A property without options must offer none.",
			ConfigPropertyOptions.optionsFor(config, property).isEmpty());
	}

	/**
	 * Suite requiring {@link TypeIndex}: the option resolution looks up specializations.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestConfigPropertyOptions.class, TypeIndex.Module.INSTANCE);
	}
}
