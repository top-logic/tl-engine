/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.themes.modern.layout.sidebar;

import com.top_logic.layout.Control;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.structure.DeckPaneControl;
import com.top_logic.layout.structure.DeckPaneControlProvider;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutControlProvider} for {@link TabComponent}s that renders the {@link TabComponent}
 * using only a {@link DeckPaneControl}.
 * 
 * <p>
 * Switching the tabs must be done externally, e.g. from a surrounding
 * {@link SidebarLayoutControlProvider}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeckpaneOnlyTabControlProvider implements LayoutControlProvider {

	/**
	 * Singleton {@link DeckpaneOnlyTabControlProvider} instance.
	 */
	public static final DeckpaneOnlyTabControlProvider INSTANCE = new DeckpaneOnlyTabControlProvider();

	private DeckpaneOnlyTabControlProvider() {
		// Singleton constructor.
	}

	@Override
	public Control createLayoutControl(Strategy strategy, LayoutComponent component) {
		LayoutControl deckpane =
			LayoutControlAdapter.wrap(DeckPaneControlProvider.INSTANCE.createLayoutControl(strategy, component));
		((AbstractControlBase) deckpane).listenForInvalidation(component);
		return deckpane;
	}

	private LayoutComponent window(LayoutComponent component) {
		LayoutComponent window = component;
		LayoutComponent parent;
		while ((!(window instanceof WindowComponent)) && (parent = window.getParent()) != null) {
			window = parent;
		}
		return window;
	}
}
