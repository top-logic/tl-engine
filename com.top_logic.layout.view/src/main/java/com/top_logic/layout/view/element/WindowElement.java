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
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ToolbarControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.util.Resources;

/**
 * A {@link UIElement} that provides dialog window chrome using {@link ReactWindowControl}.
 *
 * <p>
 * Renders a window with title bar (including close button and toolbar buttons), scrollable body, and
 * optional footer action buttons. Intended for use as the root element inside dialog view XML files.
 * </p>
 *
 * <p>
 * The close button (X) in the title bar cancels the dialog via {@link DialogManager}. Footer
 * actions are declared as child elements inside the {@code <actions>} section. Toolbar commands from
 * child elements (e.g. form edit/save/cancel) are automatically rendered in the title bar.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * &lt;window title="Details" width="500px"&gt;
 *   &lt;form input="model"&gt;
 *     &lt;field attribute="name"/&gt;
 *   &lt;/form&gt;
 *   &lt;actions&gt;
 *     &lt;button&gt;
 *       &lt;action class="...CancelDialogCommand" label="Close"/&gt;
 *     &lt;/button&gt;
 *   &lt;/actions&gt;
 * &lt;/window&gt;
 * </pre>
 */
public class WindowElement extends CommandScopeElement {

	/**
	 * Configuration for {@link WindowElement}.
	 */
	@TagName("window")
	public interface Config extends CommandScopeElement.Config {

		@Override
		@ClassDefault(WindowElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration name for {@link #getWidth()}. */
		String WIDTH = "width";

		/** Configuration name for {@link #getActions()}. */
		String ACTIONS = "actions";

		/** Configuration name for {@link #getResizable()}. */
		String RESIZABLE = "resizable";

		/**
		 * The window title displayed in the title bar.
		 */
		@Name(TITLE)
		@Nullable
		ResKey getTitle();

		/**
		 * The window width as a CSS pixel value (e.g. "500px").
		 *
		 * <p>
		 * Defaults to "500px" if not specified.
		 * </p>
		 */
		@Name(WIDTH)
		@StringDefault("500px")
		String getWidth();

		/**
		 * Whether the window can be resized by the user.
		 *
		 * <p>
		 * Defaults to {@code true}. When enabled, resize handles are shown at all edges and corners.
		 * </p>
		 */
		@Name(RESIZABLE)
		@BooleanDefault(true)
		boolean getResizable();

		/**
		 * Footer action elements (typically buttons).
		 *
		 * <p>
		 * These are rendered in the window's footer bar below the body content.
		 * </p>
		 */
		@Name(ACTIONS)
		List<PolymorphicConfiguration<? extends UIElement>> getActions();
	}

	private final ResKey _title;

	private final String _width;

	private final boolean _resizable;

	private final List<UIElement> _actions;

	/**
	 * Creates a new {@link WindowElement} from configuration.
	 */
	@CalledByReflection
	public WindowElement(InstantiationContext context, Config config) {
		super(context, config);
		_title = config.getTitle();
		_width = config.getWidth();
		_resizable = config.getResizable();
		_actions = config.getActions().stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
	}

	@Override
	protected ToolbarControl createChromeControl(ViewContext context, ReactControl content) {
		DialogManager mgr = context.getDialogManager();
		Runnable closeHandler = () -> {
			if (mgr != null) {
				mgr.closeTopDialog(DialogResult.cancelled());
			}
		};

		DisplayDimension width = parseWidth(_width);
		String title = _title != null ? Resources.getInstance().getString(_title) : "";

		ReactWindowControl window = new ReactWindowControl(context, title, width, closeHandler);
		window.setResizable(_resizable);
		window.setChild(content);

		if (!_actions.isEmpty()) {
			List<ReactControl> actionControls = _actions.stream()
				.map(a -> (ReactControl) a.createControl(context))
				.collect(Collectors.toList());
			window.setActions(actionControls);
		}

		return window;
	}

	private DisplayDimension parseWidth(String widthStr) {
		if (widthStr.endsWith("px")) {
			try {
				int value = Integer.parseInt(widthStr.substring(0, widthStr.length() - 2).trim());
				return DisplayDimension.px(value);
			} catch (NumberFormatException ex) {
				// Fall through to default.
			}
		}
		return DisplayDimension.px(500);
	}
}
