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
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactGridControl;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * UIElement that wraps {@link ReactGridControl}.
 *
 * <p>
 * Renders a CSS Grid container with responsive auto-fit columns.
 * </p>
 */
public class GridElement extends ContainerElement {

	/**
	 * Configuration for {@link GridElement}.
	 */
	@TagName("grid")
	public interface Config extends ContainerElement.Config {

		@Override
		@ClassDefault(GridElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getMinColumnWidth()}. */
		String MIN_COLUMN_WIDTH = "min-column-width";

		/** Configuration name for {@link #getGap()}. */
		String GAP = "gap";

		/**
		 * Minimum column width for responsive auto-fit (e.g. "16rem").
		 */
		@Name(MIN_COLUMN_WIDTH)
		@StringDefault("16rem")
		String getMinColumnWidth();

		/**
		 * The gap between grid items: "compact", "default", or "loose".
		 */
		@Name(GAP)
		@StringDefault("default")
		String getGap();
	}

	private final String _minColumnWidth;

	private final String _gap;

	/**
	 * Creates a new {@link GridElement} from configuration.
	 */
	@CalledByReflection
	public GridElement(InstantiationContext context, Config config) {
		super(context, config);
		_minColumnWidth = config.getMinColumnWidth();
		_gap = config.getGap();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ReactControl> children = createChildControls(context).stream()
			.map(c -> (ReactControl) c)
			.collect(Collectors.toList());

		return new ReactGridControl(_minColumnWidth, _gap, children);
	}
}
