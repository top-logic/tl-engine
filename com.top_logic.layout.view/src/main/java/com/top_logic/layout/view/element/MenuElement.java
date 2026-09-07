/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackAlign;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.MenuTrigger;
import com.top_logic.layout.view.command.ViewCommandModel;

/**
 * A drop-down menu whose children are its trigger.
 *
 * <p>
 * The children render the trigger - an avatar, an icon, a label, or any combination - and clicking
 * them opens a menu of the commands declared on this element, anchored below. Use this instead of a
 * row of buttons wherever a group of commands belongs to one thing rather than to the surface they
 * sit on: the account menu of an app bar is the canonical case.
 * </p>
 *
 * <p>
 * Every declared command becomes an entry, whatever its
 * {@link com.top_logic.layout.react.control.button.CommandPlacement placement}: a command written
 * inside a menu is an entry of that menu by construction. Entries the current user must not see
 * drop out on their own, since a command hidden by its executability rule is not offered.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * &lt;menu&gt;
 *   &lt;avatar input="currentUser"/&gt;
 *   &lt;commands&gt;
 *     &lt;logout-command image="css:bi bi-box-arrow-right"&gt;
 *       &lt;label&gt;&lt;en&gt;Log out&lt;/en&gt;&lt;/label&gt;
 *     &lt;/logout-command&gt;
 *   &lt;/commands&gt;
 * &lt;/menu&gt;
 * </pre>
 *
 * @see ContextMenuElement
 */
@InApp
public class MenuElement extends AbstractMenuElement {

	/**
	 * Configuration for {@link MenuElement}.
	 */
	@TagName("menu")
	public interface Config extends AbstractMenuElement.Config {

		@Override
		@ClassDefault(MenuElement.class)
		Class<? extends UIElement> getImplementationClass();
	}

	/**
	 * Creates a new {@link MenuElement}.
	 */
	public MenuElement(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected MenuTrigger getTrigger() {
		return MenuTrigger.CLICK;
	}

	/**
	 * A trigger reads as one control, so its parts sit side by side - an avatar next to the name it
	 * belongs to, not above it.
	 */
	@Override
	protected ReactControl createRegionContent(ViewContext context) {
		List<IReactControl> children = createChildControls(context);
		if (children.size() == 1) {
			return (ReactControl) children.get(0);
		}
		return new ReactStackControl(context, StackDirection.ROW, StackGap.COMPACT, StackAlign.CENTER, false,
			children.stream().map(child -> (ReactControl) child).collect(Collectors.toList()));
	}

	@Override
	protected List<CommandModel> menuCommands(List<ViewCommandModel> models) {
		return allCommands(models);
	}
}
