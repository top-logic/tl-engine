/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.mime.MimeTypesModule;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.OptionLabelsDisplayConfigProvider;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Test case for {@link OptionLabelsDisplayConfigProvider}, as configured in the
 * {@link ConfigControlService}.
 */
@SuppressWarnings("javadoc")
public class TestOptionLabelsDisplayConfigProvider extends TestCase {

	/**
	 * Labels a class by its simple name, and any other value in upper case.
	 */
	public static class SimpleNames implements LabelProvider {
		@Override
		public String getLabel(Object object) {
			if (object instanceof Class<?> type) {
				return type.getSimpleName();
			}
			return object == null ? null : object.toString().toUpperCase();
		}
	}

	public interface LabeledConfig extends ConfigurationItem {

		String SETTABLE = "settable";

		@Override
		@Hidden(false)
		@OptionLabels(SimpleNames.class)
		Class<?> getConfigurationInterface();

		@OptionLabels(SimpleNames.class)
		String getSettable();
	}

	private LabeledConfig _config;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_config = TypedConfiguration.newConfigItem(LabeledConfig.class);
	}

	/**
	 * The configuration interface cannot be set: it is displayed with the label its
	 * {@link OptionLabels} give it.
	 */
	public void testValueThatCannotBeSetIsDisplayedWithItsLabel() {
		ReactControl control = control(ConfigurationItem.CONFIGURATION_INTERFACE_NAME);

		assertTrue("A value that cannot be set is displayed, not edited: " + control,
			control instanceof ReactTextControl);
		assertEquals("The value is labelled by its option labels.", LabeledConfig.class.getSimpleName(),
			((ReactTextControl) control).getText());
	}

	/** A property that can be set keeps its input, the option labels label options only. */
	public void testValueThatCanBeSetKeepsItsInput() {
		assertTrue(control(LabeledConfig.SETTABLE) instanceof ReactTextInputControl);
	}

	private ReactControl control(String propertyName) {
		PropertyDescriptor property = _config.descriptor().getProperty(propertyName);
		ConfigFieldModel model = ConfigControlService.getInstance().createModel(_config, property);
		return ConfigControlService.getInstance().createControl(context(), model);
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * Suite starting the service under test with its shipped configuration.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestOptionLabelsDisplayConfigProvider.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE, FormatterService.Module.INSTANCE,
				MimeTypesModule.Module.INSTANCE, ThemeFactory.Module.INSTANCE, SafeHTML.Module.INSTANCE,
				ConfigControlService.Module.INSTANCE));
	}

}
