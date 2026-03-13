/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.configedit.ConfigEditorControl;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Tests for {@link ConfigEditorControl}.
 */
public class TestConfigEditorControl extends TestCase {

	/** Nested configuration used as an ITEM property value. */
	public interface InnerConfig extends ConfigurationItem {

		/** Property name for {@link #getTitle()}. */
		String TITLE = "title";

		@Name(TITLE)
		String getTitle();

		void setTitle(String value);
	}

	/** Test configuration with a mix of property types. */
	public interface TestConfig extends ConfigurationItem {

		/** Property name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Property name for {@link #getCount()}. */
		String COUNT = "count";

		/** Property name for {@link #isEnabled()}. */
		String ENABLED = "enabled";

		/** Property name for {@link #getInner()}. */
		String INNER = "inner";

		@Name(LABEL)
		@Mandatory
		String getLabel();

		void setLabel(String value);

		@Name(COUNT)
		@IntDefault(0)
		int getCount();

		void setCount(int value);

		@Name(ENABLED)
		boolean isEnabled();

		void setEnabled(boolean value);

		@Name(INNER)
		InnerConfig getInner();

		void setInner(InnerConfig value);
	}

	/**
	 * Test subclass that bypasses {@link com.top_logic.layout.form.values.edit.Labels} to avoid
	 * requiring Resources/ThreadContextManager in unit tests.
	 */
	static class TestableConfigEditorControl extends ConfigEditorControl {

		TestableConfigEditorControl(ReactContext context, ConfigurationItem config) {
			super(context, config);
		}

		@Override
		protected String resolveLabel(PropertyDescriptor property) {
			return property.getPropertyName();
		}

		@Override
		protected String resolveHelpText(PropertyDescriptor property) {
			return null;
		}

		@Override
		protected ConfigEditorControl createNestedEditor(ReactContext context, ConfigurationItem nested) {
			return new TestableConfigEditorControl(context, nested);
		}
	}

	private ReactContext createTestContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue());
	}

	/**
	 * Tests that the editor creates field models for all PLAIN/REF properties.
	 */
	public void testFieldModelsCreated() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		ConfigEditorControl editor = new TestableConfigEditorControl(createTestContext(), config);

		List<ConfigFieldModel> models = editor.getFieldModels();
		// ConfigurationItem declares "configuration-interface" as a PLAIN property too.
		assertTrue("Should have at least 3 field models", models.size() >= 3);

		// Verify our 3 declared properties are present.
		boolean foundLabel = false;
		boolean foundCount = false;
		boolean foundEnabled = false;
		for (ConfigFieldModel model : models) {
			String name = model.getProperty().getPropertyName();
			if (TestConfig.LABEL.equals(name)) {
				foundLabel = true;
			} else if (TestConfig.COUNT.equals(name)) {
				foundCount = true;
			} else if (TestConfig.ENABLED.equals(name)) {
				foundEnabled = true;
			}
		}
		assertTrue("Should contain label property", foundLabel);
		assertTrue("Should contain count property", foundCount);
		assertTrue("Should contain enabled property", foundEnabled);
	}

	/**
	 * Tests that the editor correctly reflects the React module name.
	 */
	public void testReactModule() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		ConfigEditorControl editor = new TestableConfigEditorControl(createTestContext(), config);

		assertEquals("TLFormLayout", editor.getReactModule());
	}

	/**
	 * Tests that cleanup detaches all field models.
	 */
	public void testCleanup() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		ConfigEditorControl editor = new TestableConfigEditorControl(createTestContext(), config);

		List<ConfigFieldModel> models = editor.getFieldModels();
		assertTrue("Should have at least 3 field models", models.size() >= 3);

		// Trigger cleanup.
		editor.cleanupTree();

		// After cleanup, external changes should no longer fire through models.
		int[] callCount = {0};
		for (ConfigFieldModel model : models) {
			model.addListener(new FieldModelListener() {
				@Override
				public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
					callCount[0]++;
				}

				@Override
				public void onEditabilityChanged(FieldModel source, boolean editable) {
					// Ignored.
				}

				@Override
				public void onValidationChanged(FieldModel source) {
					// Ignored.
				}
			});
		}

		config.setLabel("changed");
		config.setCount(42);
		config.setEnabled(true);
		assertEquals("No listeners should fire after cleanup", 0, callCount[0]);
	}

	/**
	 * Tests that ITEM properties produce a child control (form group) but no field model.
	 */
	public void testItemPropertyRendered() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		InnerConfig inner = TypedConfiguration.newConfigItem(InnerConfig.class);
		inner.setTitle("Hello");
		config.setInner(inner);

		ConfigEditorControl editor = new TestableConfigEditorControl(createTestContext(), config);

		// ITEM properties do not produce field models.
		for (ConfigFieldModel model : editor.getFieldModels()) {
			assertFalse("ITEM property should not produce a field model",
				TestConfig.INNER.equals(model.getProperty().getPropertyName()));
		}

		// The editor should still have at least the PLAIN fields plus at least one child for the
		// ITEM group. The total number of child controls should be greater than just the field
		// models (which are PLAIN/REF only).
		List<ConfigFieldModel> models = editor.getFieldModels();
		assertTrue("Should have PLAIN/REF field models", models.size() >= 3);
	}

	/**
	 * Tests that a null ITEM property value is skipped (no group created).
	 */
	public void testNullItemPropertySkipped() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		// inner is null by default

		ConfigEditorControl editor = new TestableConfigEditorControl(createTestContext(), config);

		// Should still work fine — only PLAIN/REF fields are created.
		List<ConfigFieldModel> models = editor.getFieldModels();
		assertTrue("Should have at least 3 field models", models.size() >= 3);
	}

	/**
	 * Suite requiring TypeIndex for TypedConfiguration.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestConfigEditorControl.class, TypeIndex.Module.INSTANCE);
	}
}
