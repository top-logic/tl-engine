/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.themes.modern.layout.sidebar;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.gui.layout.ButtonComponent;
import com.top_logic.layout.Control;
import com.top_logic.layout.structure.LayoutControlFactory;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.structure.LayoutControlProvider.Strategy;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutControlFactory} that removes button components and maximizable buttons.
 * 
 * <p>
 * When component buttons are displayed in a global header area, view-local button bars make no
 * sense. With component buttons in the global header are, maximizing components makes no sense,
 * since the buttons would be hidden.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SidebarLayoutControlFactory extends LayoutControlFactory {

	/**
	 * Creates a {@link SidebarLayoutControlFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SidebarLayoutControlFactory(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Control createSpecificLayout(LayoutComponent component, LayoutControlProvider customProvider) {
		if (component instanceof ButtonComponent) {
			if (component.getDialogParent() == null) {
				// Only outside a dialog - dialogs have no global button bar.
				return null;
			}
		}
		return super.createSpecificLayout(component, customProvider);
	}

	@Override
	public Control mkLayout(Strategy strategy, LayoutComponent component) {
		if (component instanceof ButtonComponent) {
			if (component.getDialogParent() == null) {
				// Only outside a dialog - dialogs have no global button bar.
				return null;
			}
		}
		return super.mkLayout(strategy, component);
	}

	@Override
	protected boolean clearShowMaximize(LayoutComponent component) {
		// Prevent showing the maximize button since this would maximize over the global buttons
		// rendering the view unusable.
		super.clearShowMaximize(component);
		return false;
	}

}
