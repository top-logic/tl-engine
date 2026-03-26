/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.view;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.demo.react.DemoReactCounterComponent.DemoCounterControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * {@link UIElement} that wraps the {@link DemoCounterControl}.
 *
 * <p>
 * Demonstrates how application-specific elements integrate with the view system by wrapping an
 * existing {@link com.top_logic.layout.react.control.ReactControl}.
 * </p>
 */
public class DemoCounterElement implements UIElement {

	/**
	 * Configuration for {@link DemoCounterElement}.
	 */
	@TagName("demo-counter")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(DemoCounterElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/**
		 * The display label for the counter, or empty for the default.
		 */
		@Name(LABEL)
		@Nullable
		ResKey getLabel();
	}

	private final ResKey _label;

	/**
	 * Creates a new {@link DemoCounterElement} from configuration.
	 */
	@CalledByReflection
	public DemoCounterElement(InstantiationContext context, Config config) {
		_label = config.getLabel();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		String label = _label != null ? com.top_logic.util.Resources.getInstance().getString(_label) : null;
		return new DemoCounterControl(context, label);
	}
}
