/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.view.command;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactFieldListControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A demo command that opens a modal dialog via the {@link DialogManager}.
 *
 * <p>
 * Demonstrates how view commands can open dialogs dynamically without pre-wiring overlay controls in
 * the view tree.
 * </p>
 */
public class OpenDemoDialogCommand implements ViewCommand {

	/**
	 * Configuration for {@link OpenDemoDialogCommand}.
	 */
	@TagName("open-demo-dialog")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(OpenDemoDialogCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/** Configuration name for {@link #getDialogTitle()}. */
		String DIALOG_TITLE = "dialog-title";

		/** Configuration name for {@link #getDialogMessage()}. */
		String DIALOG_MESSAGE = "dialog-message";

		/**
		 * The title for the dialog window.
		 */
		@Name(DIALOG_TITLE)
		@Nullable
		String getDialogTitle();

		/**
		 * The message to display inside the dialog body.
		 */
		@Name(DIALOG_MESSAGE)
		@Nullable
		String getDialogMessage();
	}

	private final String _dialogTitle;

	private final String _dialogMessage;

	/**
	 * Creates a new {@link OpenDemoDialogCommand}.
	 */
	@CalledByReflection
	public OpenDemoDialogCommand(InstantiationContext context, Config config) {
		_dialogTitle = config.getDialogTitle() != null ? config.getDialogTitle() : "Dialog";
		_dialogMessage = config.getDialogMessage() != null ? config.getDialogMessage() : "Hello from the dialog!";
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		DialogManager mgr = context.getDialogManager();
		if (mgr == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		ReactWindowControl window = new ReactWindowControl(context, _dialogTitle,
			DisplayDimension.px(420), () -> mgr.closeTopDialog(DialogResult.cancelled()));

		ReactButtonControl messageBtn = new ReactButtonControl(context, _dialogMessage,
			ctx -> HandlerResult.DEFAULT_RESULT);
		window.setChild(new ReactFieldListControl(context, List.of(messageBtn)));

		ReactButtonControl okBtn = new ReactButtonControl(context, "OK", ctx -> {
			mgr.closeTopDialog(DialogResult.ok(null));
			return HandlerResult.DEFAULT_RESULT;
		});
		ReactButtonControl cancelBtn = new ReactButtonControl(context, "Cancel", ctx -> {
			mgr.closeTopDialog(DialogResult.cancelled());
			return HandlerResult.DEFAULT_RESULT;
		});
		window.setActions(List.of(cancelBtn, okBtn));

		mgr.openDialog(true, window, result -> {
			// Result handling is a no-op for this demo.
		});

		return HandlerResult.DEFAULT_RESULT;
	}
}
