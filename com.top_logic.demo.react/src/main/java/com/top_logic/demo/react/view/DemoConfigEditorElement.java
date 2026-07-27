/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.view;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.configedit.ConfigEditorControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * {@link UIElement} that renders the React configuration editor for a fresh
 * {@link DemoEditorConfig}.
 *
 * <p>
 * Demonstrates the config editor control over a self-contained sample configuration covering a
 * spread of property kinds.
 * </p>
 */
public class DemoConfigEditorElement implements UIElement {

	/**
	 * Configuration for {@link DemoConfigEditorElement}.
	 */
	@TagName("config-editor-demo")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(DemoConfigEditorElement.class)
		Class<? extends UIElement> getImplementationClass();
	}

	/**
	 * Creates a new {@link DemoConfigEditorElement} from configuration.
	 */
	@CalledByReflection
	public DemoConfigEditorElement(InstantiationContext context, Config config) {
		// No additional state needed.
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		DemoEditorConfig config = TypedConfiguration.newConfigItem(DemoEditorConfig.class);
		return new ConfigEditorControl(context, config);
	}
}
