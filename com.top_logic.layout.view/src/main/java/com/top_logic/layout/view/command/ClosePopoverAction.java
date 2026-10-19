/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.ReactPopoverControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.element.PopoverElement;

/**
 * {@link ViewAction} that closes the {@link PopoverElement &lt;popover&gt;} whose content the
 * command belongs to.
 *
 * <p>
 * Of nested popovers, the innermost one is closed. Outside the content of a popover, the action does
 * nothing, just as closing a dialog does nothing where no dialog is open.
 * </p>
 *
 * @implNote The popover is found in the {@link ViewContext#getScope(Class) scope}
 *           {@link ReactPopoverControl} that the popover establishes for its content.
 */
@InApp
public class ClosePopoverAction implements ViewAction {

	/**
	 * Configuration for {@link ClosePopoverAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<ClosePopoverAction> {

		/** Configuration tag of a {@link ClosePopoverAction}. */
		String TAG_NAME = "close-popover";

		@Override
		@ClassDefault(ClosePopoverAction.class)
		Class<? extends ClosePopoverAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link ClosePopoverAction} from configuration.
	 */
	@CalledByReflection
	public ClosePopoverAction(InstantiationContext context, Config config) {
		// No configuration.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (context instanceof ViewContext viewContext) {
			ReactPopoverControl popover = viewContext.getScope(ReactPopoverControl.class);
			if (popover != null) {
				popover.close();
			}
		}
		return input;
	}
}
