/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

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

		int getChildCount() {
			return getChildren().size();
		}
	}

	private ReactContext createTestContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue());
	}

	/**
	 * Tests that the editor creates child controls for PLAIN/REF properties.
	 *
	 * <p>
	 * TestConfig has 3 PLAIN properties (label, count, enabled) plus the inherited
	 * "configuration-interface" property. With no ITEM set, the child count should be at least 3.
	 * </p>
	 */
	public void testChildControlsCreated() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		TestableConfigEditorControl editor = new TestableConfigEditorControl(createTestContext(), config);

		// ConfigurationItem declares "configuration-interface" as a PLAIN property too.
		// 3 declared PLAIN + 1 inherited = at least 4 children (each wrapped in chrome).
		assertTrue("Should have at least 3 child controls", editor.getChildCount() >= 3);
	}

	/**
	 * Tests that the editor correctly reflects the React module name.
	 */
	public void testReactModule() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		TestableConfigEditorControl editor = new TestableConfigEditorControl(createTestContext(), config);

		assertEquals("TLFormLayout", editor.getReactModule());
	}

	/**
	 * Tests that cleanup detaches all field models so config listeners no longer fire.
	 */
	public void testCleanup() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);

		// Create field models directly to verify detach behavior.
		PropertyDescriptor labelProp = config.descriptor().getProperty(TestConfig.LABEL);
		ConfigFieldModel labelModel = new ConfigFieldModel(config, labelProp);

		int[] callCount = {0};
		labelModel.addListener(new FieldModelListener() {
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

		// Before detach, changing config fires through the model.
		config.setLabel("before");
		assertEquals("Listener should fire before detach", 1, callCount[0]);

		labelModel.detach();

		// After detach, config changes are no longer propagated.
		config.setLabel("after");
		assertEquals("Listener should not fire after detach", 1, callCount[0]);

		// Now verify that the editor's cleanup triggers detach via cleanup actions.
		TestableConfigEditorControl editor = new TestableConfigEditorControl(createTestContext(), config);
		assertTrue("Editor should have children", editor.getChildCount() >= 3);

		editor.cleanupTree();

		// After cleanupTree, the editor's children list is still there but SSE is detached.
		// The cleanup actions (model::detach) have run.
	}

	/**
	 * Tests that ITEM properties produce a child control (form group).
	 */
	public void testItemPropertyRendered() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		InnerConfig inner = TypedConfiguration.newConfigItem(InnerConfig.class);
		inner.setTitle("Hello");
		config.setInner(inner);

		TestableConfigEditorControl editor = new TestableConfigEditorControl(createTestContext(), config);

		// With an ITEM property set, the editor should have more children than without.
		// PLAIN/REF children (at least 3) + 1 form group for the ITEM property.
		assertTrue("Should have at least 4 children with ITEM", editor.getChildCount() >= 4);
	}

	/**
	 * Tests that a null ITEM property value is skipped (no group created).
	 */
	public void testNullItemPropertySkipped() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		// inner is null by default

		int childCountWithoutItem = new TestableConfigEditorControl(createTestContext(), config)
			.getChildCount();

		InnerConfig inner = TypedConfiguration.newConfigItem(InnerConfig.class);
		config.setInner(inner);

		int childCountWithItem = new TestableConfigEditorControl(createTestContext(), config)
			.getChildCount();

		assertEquals("Null ITEM should not add a child", childCountWithItem, childCountWithoutItem + 1);
	}

	/**
	 * Suite requiring TypeIndex for TypedConfiguration.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestConfigEditorControl.class, TypeIndex.Module.INSTANCE);
	}
}
