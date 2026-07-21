/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.ui;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.PropertyValue;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.search.ui.ScriptContextVariablesProvider;
import com.top_logic.model.search.ui.ScriptContextVariablesResolver;

/**
 * Test for {@link ScriptContextVariablesResolver}.
 */
public class TestScriptContextVariablesResolver extends BasicTestCase {

	/** Provider returning fixed names. */
	public static class FixedProvider implements ScriptContextVariablesProvider {
		@Override
		public List<String> getVariables(ValueModel valueModel) {
			return List.of("alpha", "beta");
		}
	}

	/** Config with an annotated property. */
	public interface Sample extends ConfigurationItem {
		String NAME = "value";

		@Name(NAME)
		@ScriptContextVariables(FixedProvider.class)
		String getValue();
	}

	/** Config with an un-annotated property. */
	public interface Plain extends ConfigurationItem {
		String NAME = "value";

		@Name(NAME)
		String getValue();
	}

	/**
	 * A property annotated with {@link ScriptContextVariables} resolves to the names returned by its
	 * provider.
	 */
	public void testResolvesAnnotatedProperty() {
		assertEquals(List.of("alpha", "beta"), ScriptContextVariablesResolver.resolve(valueModel(Sample.class)));
	}

	/**
	 * A property without the annotation resolves to no context variables.
	 */
	public void testEmptyWhenNoAnnotation() {
		assertEquals(List.of(), ScriptContextVariablesResolver.resolve(valueModel(Plain.class)));
	}

	/**
	 * A <code>null</code> value model (e.g. a field not built by the declarative form) resolves to no
	 * context variables.
	 */
	public void testEmptyWhenNullValueModel() {
		assertEquals(List.of(), ScriptContextVariablesResolver.resolve(null));
	}

	/**
	 * A minimal {@link ValueModel} over the {@code value} property of a freshly created instance of
	 * the given config type, for driving the resolver in tests.
	 */
	private static ValueModel valueModel(Class<? extends ConfigurationItem> configType) {
		ConfigurationItem model = TypedConfiguration.newConfigItem(configType);
		PropertyDescriptor property = model.descriptor().getProperty("value");
		return new ValueModel() {
			@Override
			public ConfigurationItem getModel() {
				return model;
			}

			@Override
			public PropertyDescriptor getProperty() {
				return property;
			}

			@Override
			public Object getValue() {
				return model.value(property);
			}

			@Override
			public void setValue(Object newValue) {
				model.update(property, newValue);
			}

			@Override
			public boolean addValue(Object newElement) {
				return false;
			}

			@Override
			public void removeValue(Object oldElement) {
				// Ignore.
			}

			@Override
			public void clearValue() {
				// Ignore.
			}

			@Override
			public PropertyValue getPropertyValue() {
				return null;
			}
		};
	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestScriptContextVariablesResolver.class));
	}
}
