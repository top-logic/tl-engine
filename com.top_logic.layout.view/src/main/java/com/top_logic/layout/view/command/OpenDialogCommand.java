/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.channel.ChannelBindingConfig;
import com.top_logic.tool.boundsec.HandlerResult;

// Note: Outputs config retained for backward compatibility.

/**
 * A {@link ViewCommand} that opens a modal dialog whose content is defined in a separate
 * {@code .view.xml} file.
 *
 * <p>
 * Delegates to {@link OpenDialogAction} for the core dialog-opening logic. This command adds
 * the standard {@link ViewCommand.Config} presentation options (label, image, executability).
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * &lt;button&gt;
 *   &lt;action class="com.top_logic.layout.view.command.OpenDialogCommand"
 *     dialog-view="demo/show-details.view.xml"
 *     close-on-backdrop="true"&gt;
 *     &lt;bind channel="model" to="selection"/&gt;
 *   &lt;/action&gt;
 * &lt;/button&gt;
 * </pre>
 */
public class OpenDialogCommand implements ViewCommand {

	/**
	 * Configuration for {@link OpenDialogCommand}.
	 */
	@TagName("open-dialog")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(OpenDialogCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/** Configuration name for {@link #getDialogView()}. */
		String DIALOG_VIEW = "dialog-view";

		/** Configuration name for {@link #getCloseOnBackdrop()}. */
		String CLOSE_ON_BACKDROP = "close-on-backdrop";

		/** Configuration name for {@link #getBindings()}. */
		String BINDINGS = "bindings";

		/** Configuration name for {@link #getOutputs()}. */
		String OUTPUTS = "outputs";

		/** Configuration name for {@link #getBindInputTo()}. */
		String BIND_INPUT_TO = "bind-input-to";

		/**
		 * Path to the {@code .view.xml} file defining the dialog content, relative to
		 * {@code /WEB-INF/views/}.
		 */
		@Name(DIALOG_VIEW)
		@Mandatory
		String getDialogView();

		/**
		 * Whether clicking the backdrop (outside the dialog) closes it.
		 */
		@Name(CLOSE_ON_BACKDROP)
		@BooleanDefault(false)
		boolean getCloseOnBackdrop();

		/**
		 * Channel bindings from the parent scope to the dialog view's channels.
		 *
		 * <p>
		 * Each binding shares the parent's channel instance with the dialog - both sides read and
		 * write the same object (live binding), identical to the mechanism used by
		 * {@code <view-ref>}.
		 * </p>
		 */
		@Name(BINDINGS)
		@DefaultContainer
		List<ChannelBindingConfig> getBindings();

		/**
		 * Output channel mappings from the dialog back to the parent view.
		 *
		 * <p>
		 * Reserved for future use.
		 * </p>
		 */
		@Name(OUTPUTS)
		@EntryTag("output")
		List<ChannelBindingConfig> getOutputs();

		/**
		 * Dialog channel name to receive the command's input parameter.
		 */
		@Name(BIND_INPUT_TO)
		@Nullable
		String getBindInputTo();
	}

	private final OpenDialogAction _action;

	/**
	 * Creates a new {@link OpenDialogCommand}.
	 */
	@CalledByReflection
	public OpenDialogCommand(InstantiationContext context, Config config) {
		_action = new OpenDialogAction(config.getDialogView(), config.getCloseOnBackdrop(),
			config.getBindInputTo(), config.getBindings());
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		_action.execute(context, input);
		return HandlerResult.DEFAULT_RESULT;
	}
}
