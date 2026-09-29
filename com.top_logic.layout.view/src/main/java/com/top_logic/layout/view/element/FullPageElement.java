/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ToolbarControl;
import com.top_logic.layout.react.control.layout.ReactPanelControl;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * UIElement that fills its container with a single main content (typically a table), without any
 * header chrome.
 *
 * <p>
 * Use it as the root content of a view whose enclosing tab or navigation entry already names it: no
 * panel title is rendered, so the label is not duplicated and no vertical space is wasted. The
 * element always fills the available height, so a virtualized content control (a
 * {@link com.top_logic.layout.react.control.table.TableViewControl table}) bounds its scroll
 * viewport and scrolls internally.
 * </p>
 *
 * <p>
 * Unlike a panel, a full page renders no local toolbar: commands are projected to a slot higher in
 * the tree (e.g. the application bar), not into a header bar of their own.
 * </p>
 */
public class FullPageElement extends CommandScopeElement {

	/**
	 * Configuration for {@link FullPageElement}.
	 */
	@TagName("full-page")
	public interface Config extends CommandScopeElement.Config {

		@Override
		@ClassDefault(FullPageElement.class)
		Class<? extends UIElement> getImplementationClass();
	}

	/**
	 * Creates a new {@link FullPageElement} from configuration.
	 */
	@CalledByReflection
	public FullPageElement(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected ToolbarControl createChromeControl(ViewContext context, ReactControl content,
			ReactToolbarControl toolbar, ReactToolbarControl buttonBar) {
		// No title and no local toolbar: the enclosing tab / navigation entry labels the page, and
		// commands are projected to a slot higher in the tree. Passing a null toolbar / button bar
		// keeps the (reused) panel control header-less; the fill flag makes it span the available
		// height so its content scrolls internally.
		ReactPanelControl page = new ReactPanelControl(context, "", content, null, null, false, false, false);
		page.setFill(true);
		return page;
	}
}
