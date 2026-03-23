/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelBindingConfig;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} that opens a modal dialog whose content is defined in a separate
 * {@code .view.xml} file.
 *
 * <p>
 * The dialog gets its own isolated {@link ViewContext}. Parent channels are shared with the dialog
 * via live bindings (same mechanism as {@code <view-ref>}). Additionally, the command's input
 * parameter can be injected into a named dialog channel via {@code bind-input-to}.
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
		 * Reserved for future use. When a result propagation mechanism is implemented, these
		 * mappings will define which dialog channel values are copied back to the parent on close.
		 * </p>
		 */
		@Name(OUTPUTS)
		@EntryTag("output")
		List<ChannelBindingConfig> getOutputs();

		/**
		 * Dialog channel name to receive the command's input parameter.
		 *
		 * <p>
		 * When set, the {@code input} argument from {@link ViewCommand#execute} is written into the
		 * specified dialog channel before the dialog view is created. This allows chart click
		 * handlers, table row actions, etc. to pass their target object to the dialog.
		 * </p>
		 */
		@Name(BIND_INPUT_TO)
		@Nullable
		String getBindInputTo();
	}

	private final String _dialogViewPath;

	private final boolean _closeOnBackdrop;

	private final List<ChannelBindingConfig> _bindings;

	private final String _bindInputTo;

	/**
	 * Creates a new {@link OpenDialogCommand}.
	 */
	@CalledByReflection
	public OpenDialogCommand(InstantiationContext context, Config config) {
		_dialogViewPath = ViewLoader.VIEW_BASE_PATH + config.getDialogView();
		_closeOnBackdrop = config.getCloseOnBackdrop();
		_bindings = config.getBindings();
		_bindInputTo = config.getBindInputTo();
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		DialogManager mgr = context.getDialogManager();
		if (mgr == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		ViewElement dialogView;
		try {
			dialogView = ViewLoader.getOrLoadView(_dialogViewPath);
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to load dialog view: " + _dialogViewPath, ex);
		}

		// Create isolated ViewContext for the dialog.
		ViewContext dialogContext = new DefaultViewContext(context);

		// Propagate error sink from parent.
		if (context instanceof ViewContext) {
			ViewContext parentViewContext = (ViewContext) context;
			ErrorSink parentErrorSink = parentViewContext.getErrorSink();
			if (parentErrorSink != null) {
				dialogContext = dialogContext.withErrorSink(parentErrorSink);
			}

			// Bind parent channels to dialog channels (same mechanism as ReferenceElement).
			for (ChannelBindingConfig binding : _bindings) {
				String parentChannelName = binding.getTo().getChannelName();
				if (!parentViewContext.hasChannel(parentChannelName)) {
					Logger.warn("Channel '" + parentChannelName + "' not found in parent context, skipping.",
						OpenDialogCommand.class);
					continue;
				}
				ViewChannel parentChannel = parentViewContext.resolveChannel(binding.getTo());
				dialogContext.registerChannel(binding.getChannel(), parentChannel);
			}
		}

		// Bind command input to dialog channel.
		if (_bindInputTo != null) {
			DefaultViewChannel inputChannel = new DefaultViewChannel(_bindInputTo);
			inputChannel.set(input);
			dialogContext.registerChannel(_bindInputTo, inputChannel);
		}

		// Build the dialog control tree.
		ReactControl dialogControl = (ReactControl) dialogView.createControl(dialogContext);

		mgr.openDialog(_closeOnBackdrop, dialogControl, result -> {
			// Output channel propagation: deferred to future implementation.
		});

		return HandlerResult.DEFAULT_RESULT;
	}
}
