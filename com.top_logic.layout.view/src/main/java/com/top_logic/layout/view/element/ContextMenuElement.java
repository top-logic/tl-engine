/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.layout.react.control.overlay.ContextMenuContribution;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.command.MenuTrigger;
import com.top_logic.layout.view.command.ViewCommandModel;

/**
 * A menu over this element's children that opens on right-click.
 *
 * <p>
 * Commands declared inside a {@code <context-menu>} element with
 * {@link CommandPlacement#CONTEXT_MENU placement="CONTEXT_MENU"} are collected into a single
 * {@link ContextMenuContribution} bound to an implicit
 * {@link AbstractMenuElement#DEFAULT_TARGET_CHANNEL} channel (or to the channel referenced via the
 * optional {@code input} attribute). The children are the region the user right-clicks, and they
 * may carry commands of their own placements, which is why only the context-menu ones become
 * entries.
 * </p>
 *
 * @see MenuElement
 */
@InApp
public class ContextMenuElement extends AbstractMenuElement {

	/**
	 * Configuration for {@link ContextMenuElement}.
	 */
	@TagName("context-menu")
	public interface Config extends AbstractMenuElement.Config {

		@Override
		@ClassDefault(ContextMenuElement.class)
		Class<? extends UIElement> getImplementationClass();
	}

	/**
	 * Creates a new {@link ContextMenuElement}.
	 */
	public ContextMenuElement(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected MenuTrigger getTrigger() {
		return MenuTrigger.CONTEXT_MENU;
	}

	@Override
	protected List<CommandModel> menuCommands(List<ViewCommandModel> models) {
		List<CommandModel> result = new ArrayList<>();
		for (ViewCommandModel model : models) {
			if (model.getPlacement() == CommandPlacement.CONTEXT_MENU) {
				result.add(model);
			}
		}
		return result;
	}
}
