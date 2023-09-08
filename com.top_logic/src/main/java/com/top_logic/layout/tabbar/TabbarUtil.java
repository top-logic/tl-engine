/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tabbar;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultPopupMenuModel;
import com.top_logic.layout.basic.PopupMenuModel;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.form.control.PopupMenuButtonControl;
import com.top_logic.layout.form.model.ExecutabilityPolling;
import com.top_logic.layout.toolbar.DefaultToolBar;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Utility methods for {@link TabComponent}s.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class TabbarUtil {

	private static final ComponentName COMPONENT_NAME = ComponentName.newName("mainTabbar.layout.xml", "mainTabber");

	/**
	 * Creates a {@link PopupMenuButtonControl} with the configured commands of the burger menu of
	 * the tabbar. Uses {@link #COMPONENT_NAME} as component name.
	 * 
	 * @see #writePopup(LayoutComponent, ComponentName, DisplayContext, TagWriter)
	 */
	public static void writePopup(LayoutComponent component, DisplayContext context, TagWriter out)
			throws IOException {
		writePopup(component, COMPONENT_NAME, context, out);
	}

	/**
	 * Creates a {@link PopupMenuButtonControl} with the configured commands of the burger menu of
	 * the tabbar.
	 * 
	 * @param component
	 *        {@link LayoutComponent} which contains a {@link TabComponent}.
	 * @param componentName
	 *        The name of the {@link TabComponent}.
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link TagWriter} to write to.
	 */
	public static void writePopup(LayoutComponent component, ComponentName componentName, DisplayContext context,
			TagWriter out) throws IOException {
		Menu burgerMenu = ((TabComponent) component.getComponentByName(componentName)).getTabBar().getBurgerMenu();
		PopupMenuButtonControl popup = createPopupControl(burgerMenu);

		if (popup != null) {
			out.beginBeginTag(HTMLConstants.DIV);
			out.writeAttribute(HTMLConstants.CLASS_ATTR, "tabbarPopup");
			out.endBeginTag();
			popup.write(context, out);
			out.endTag(HTMLConstants.DIV);
		}
	}

	/**
	 * Creates a {@link PopupMenuButtonControl} and updates the executability of every
	 * {@link ExecutabilityPolling} button of every command.
	 * 
	 * @param allCommands
	 *        The configured commands to display in the {@link PopupMenuButtonControl}. Can be
	 *        <code>null</code> if no visible commands are given.
	 */
	public static PopupMenuButtonControl createPopupControl(Menu allCommands) {
		if (allCommands == null) {
			return null;
		}
		allCommands.buttons().forEach(button -> {
			if (button instanceof ExecutabilityPolling) {
				((ExecutabilityPolling) button).updateExecutabilityState();
			}
		});
		if (!allCommands.hasVisibleEntries()) {
			return null;
		}
		PopupMenuModel popupMenu = new DefaultPopupMenuModel(Icons.TAB_BAR_COMMANDS_MENU, allCommands);
		PopupMenuButtonControl popupControl = new PopupMenuButtonControl(popupMenu, DefaultToolBar.BUTTON_RENDERER);
	
		return popupControl;
	}
}