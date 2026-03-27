/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Factory for creating consistently styled {@link ReactButtonControl}s for use in dialogs.
 *
 * <p>
 * Each button type provides a standard I18N label and a theme icon, matching the legacy
 * {@link ButtonType} definitions. This ensures that dialog buttons across the application have a
 * uniform appearance.
 * </p>
 *
 * @see ButtonType
 */
public class MessageButtons {

	/**
	 * Creates a {@link ReactButtonControl} with the standard label and icon for the given
	 * {@link ButtonType}.
	 *
	 * @param context
	 *        The React context.
	 * @param type
	 *        The button type defining label and icon.
	 * @param action
	 *        The action to execute on click.
	 * @return A new button control with label and icon set.
	 */
	public static ReactButtonControl button(ReactContext context, ButtonType type, ButtonAction action) {
		String label = Resources.getInstance().getString(type.getButtonLabelKey());
		ReactButtonControl button = new ReactButtonControl(context, label, action);
		button.setImage(type.getButtonImage());
		return button;
	}

	/**
	 * Creates a cancel button.
	 *
	 * @param context
	 *        The React context.
	 * @param action
	 *        The cancel action.
	 * @return A new cancel button with standard label and icon.
	 */
	public static ReactButtonControl cancel(ReactContext context, ButtonAction action) {
		return button(context, ButtonType.CANCEL, action);
	}

	/**
	 * Creates an OK button.
	 *
	 * @param context
	 *        The React context.
	 * @param action
	 *        The OK action.
	 * @return A new OK button with standard label and icon.
	 */
	public static ReactButtonControl ok(ReactContext context, ButtonAction action) {
		return button(context, ButtonType.OK, action);
	}

	/**
	 * Creates a Yes button.
	 *
	 * @param context
	 *        The React context.
	 * @param action
	 *        The yes action.
	 * @return A new yes button with standard label and icon.
	 */
	public static ReactButtonControl yes(ReactContext context, ButtonAction action) {
		return button(context, ButtonType.YES, action);
	}

	/**
	 * Creates a No button.
	 *
	 * @param context
	 *        The React context.
	 * @param action
	 *        The no action.
	 * @return A new no button with standard label and icon.
	 */
	public static ReactButtonControl no(ReactContext context, ButtonAction action) {
		return button(context, ButtonType.NO, action);
	}

	/**
	 * Creates a Close button.
	 *
	 * @param context
	 *        The React context.
	 * @param action
	 *        The close action.
	 * @return A new close button with standard label and icon.
	 */
	public static ReactButtonControl close(ReactContext context, ButtonAction action) {
		return button(context, ButtonType.CLOSE, action);
	}

	/**
	 * Creates a Continue button.
	 *
	 * @param context
	 *        The React context.
	 * @param action
	 *        The continue action.
	 * @return A new continue button with standard label and icon.
	 */
	public static ReactButtonControl continueButton(ReactContext context, ButtonAction action) {
		return button(context, ButtonType.CONTINUE, action);
	}
}
