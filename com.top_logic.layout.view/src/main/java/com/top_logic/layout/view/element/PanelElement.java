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
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ToolbarControl;
import com.top_logic.layout.react.control.layout.ReactPanelControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * UIElement that wraps {@link ReactPanelControl}.
 *
 * <p>
 * Renders a titled panel with a toolbar header. Commands configured in the {@code <commands>}
 * section are available to child elements via the command scope and toolbar-placed commands are
 * automatically rendered as toolbar buttons.
 * </p>
 */
public class PanelElement extends CommandScopeElement {

	/**
	 * Configuration for {@link PanelElement}.
	 */
	@TagName("panel")
	public interface Config extends CommandScopeElement.Config {

		@Override
		@ClassDefault(PanelElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getTitle()}. */
		String TITLE = "title";

		/**
		 * The panel title displayed in the toolbar header.
		 */
		@Name(TITLE)
		String getTitle();
	}

	private final String _title;

	/**
	 * Creates a new {@link PanelElement} from configuration.
	 */
	@CalledByReflection
	public PanelElement(InstantiationContext context, Config config) {
		super(context, config);
		_title = config.getTitle();
	}

	@Override
	protected ToolbarControl createChromeControl(ViewContext context, ReactControl content) {
		return new ReactPanelControl(context, _title, content, false, false, false);
	}
}
