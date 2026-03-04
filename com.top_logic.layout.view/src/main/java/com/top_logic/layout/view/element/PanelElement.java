/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.react.control.layout.ReactPanelControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * UIElement that wraps {@link ReactPanelControl}.
 *
 * <p>
 * Renders a titled panel with a toolbar header. If multiple children are configured, they are
 * wrapped in a {@link ReactStackControl}.
 * </p>
 */
public class PanelElement extends ContainerElement {

	/**
	 * Configuration for {@link PanelElement}.
	 */
	@TagName("panel")
	public interface Config extends ContainerElement.Config {

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
	public ViewControl createControl(ViewContext context) {
		List<ViewControl> childControls = createChildControls(context);

		ReactControl content;
		if (childControls.size() == 1) {
			content = (ReactControl) childControls.get(0);
		} else {
			List<ReactControl> reactChildren = childControls.stream()
				.map(c -> (ReactControl) c)
				.collect(Collectors.toList());
			content = new ReactStackControl(reactChildren);
		}

		return new ReactPanelControl(_title, content, false, false, false);
	}
}
