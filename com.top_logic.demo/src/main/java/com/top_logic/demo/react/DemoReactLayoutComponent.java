/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactDeckPaneControl;
import com.top_logic.layout.react.control.layout.ReactMaximizeRootControl;
import com.top_logic.layout.react.control.layout.ReactPanelControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl.ChildConstraint;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Demo {@link LayoutComponent} that showcases the React layout controls.
 *
 * <p>
 * Demonstrates {@link ReactSplitPanelControl}, {@link ReactPanelControl},
 * {@link ReactMaximizeRootControl}, and {@link ReactDeckPaneControl} with all supported features:
 * horizontal and vertical splits, drag resizing, minimize, maximize, cascading collapse, and deck
 * pane switching.
 * </p>
 */
public class DemoReactLayoutComponent extends LayoutComponent {

	/**
	 * Configuration for {@link DemoReactLayoutComponent}.
	 */
	public interface Config extends LayoutComponent.Config {
		// No additional configuration needed.
	}

	private ReactControl _rootControl;

	/**
	 * Creates a new {@link DemoReactLayoutComponent}.
	 */
	public DemoReactLayoutComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public void writeBody(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response,
			TagWriter out) throws IOException, ServletException {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

		if (_rootControl == null) {
			_rootControl = createLayout();
		}

		_rootControl.write(displayContext, out);
	}

	private ReactControl createLayout() {
		// Build a nested layout to demonstrate all features:
		//
		// MaximizeRoot
		// +-- Horizontal Split [30% | 70%]
		//     +-- Left Panel ("Navigation", minimize+maximize)
		//     |   +-- DeckPane with 3 children
		//     +-- Vertical Split [60% | 40%]
		//         +-- Top Panel ("Editor", minimize+maximize)
		//         |   +-- Counter A
		//         +-- Bottom Panel ("Console", minimize+maximize)
		//             +-- Counter B

		// Leaf content: counters for visible interactive content.
		DemoReactCounterComponent.DemoCounterControl counterA =
			new DemoReactCounterComponent.DemoCounterControl("Editor Content");
		DemoReactCounterComponent.DemoCounterControl counterB =
			new DemoReactCounterComponent.DemoCounterControl("Console Content");

		// Deck pane children for the navigation panel.
		DemoReactCounterComponent.DemoCounterControl deckChild1 =
			new DemoReactCounterComponent.DemoCounterControl("Page 1");
		DemoReactCounterComponent.DemoCounterControl deckChild2 =
			new DemoReactCounterComponent.DemoCounterControl("Page 2");
		DemoReactCounterComponent.DemoCounterControl deckChild3 =
			new DemoReactCounterComponent.DemoCounterControl("Page 3");

		// Deck pane with 3 pages.
		ReactDeckPaneControl deckPane = new ReactDeckPaneControl(
			List.of(
				() -> deckChild1,
				() -> deckChild2,
				() -> deckChild3
			),
			0
		);

		// Panels with toolbar buttons.
		ReactPanelControl navPanel = new ReactPanelControl("Navigation", deckPane, true, true, false);
		ReactPanelControl editorPanel = new ReactPanelControl("Editor", counterA, true, true, false);
		ReactPanelControl consolePanel = new ReactPanelControl("Console", counterB, true, true, false);

		// Inner vertical split: Editor (top) + Console (bottom).
		ReactSplitPanelControl innerSplit = new ReactSplitPanelControl(Orientation.VERTICAL, true);
		innerSplit.addChild(editorPanel, ChildConstraint.percent(60));
		innerSplit.addChild(consolePanel, ChildConstraint.percent(40));

		// Outer horizontal split: Navigation (left) + Inner split (right).
		ReactSplitPanelControl outerSplit = new ReactSplitPanelControl(Orientation.HORIZONTAL, true);
		outerSplit.addChild(navPanel, ChildConstraint.percent(30));
		outerSplit.addChild(innerSplit, ChildConstraint.percent(70));

		// Maximize root wrapping the entire layout.
		ReactMaximizeRootControl maximizeRoot = new ReactMaximizeRootControl(outerSplit);

		// Wire up maximize root references so panels can maximize.
		navPanel.setMaximizeRoot(maximizeRoot);
		editorPanel.setMaximizeRoot(maximizeRoot);
		consolePanel.setMaximizeRoot(maximizeRoot);

		return maximizeRoot;
	}

}
