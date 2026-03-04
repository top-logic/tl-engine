/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.react.control.ReactButtonControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * UIElement that wraps {@link ReactButtonControl}.
 *
 * <p>
 * Renders a simple button. Since this is a declarative element, the button currently has no
 * server-side action handler; it serves as a visual placeholder in the view demo.
 * </p>
 */
public class ButtonElement implements UIElement {

	/**
	 * Configuration for {@link ButtonElement}.
	 */
	@TagName("button")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(ButtonElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/**
		 * The button label text.
		 */
		@Name(LABEL)
		String getLabel();
	}

	private final String _label;

	/**
	 * Creates a new {@link ButtonElement} from configuration.
	 */
	@CalledByReflection
	public ButtonElement(InstantiationContext context, Config config) {
		_label = config.getLabel();
	}

	@Override
	public ViewControl createControl(ViewContext context) {
		return new ReactButtonControl(_label, displayContext -> HandlerResult.DEFAULT_RESULT);
	}
}
