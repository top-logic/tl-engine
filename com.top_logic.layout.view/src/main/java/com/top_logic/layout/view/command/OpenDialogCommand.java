/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
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
import com.top_logic.layout.view.channel.ChannelMappingConfig;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} that opens a modal dialog whose content is defined in a separate
 * {@code .view.xml} file.
 *
 * <p>
 * The dialog gets its own isolated {@link ViewContext}. Input channel values from the parent context
 * are copied once at open time (no live binding). The dialog view XML is fully self-contained and
 * defines its own chrome, layout, and action buttons.
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
 *     &lt;inputs&gt;
 *       &lt;input from="selection" to="model"/&gt;
 *     &lt;/inputs&gt;
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

		/** Configuration name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		/** Configuration name for {@link #getOutputs()}. */
		String OUTPUTS = "outputs";

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
		 * Input channel mappings from the parent view to the dialog.
		 *
		 * <p>
		 * Each mapping copies the current value of a parent channel into a dialog channel at open
		 * time. This is a one-shot copy, not a live binding.
		 * </p>
		 */
		@Name(INPUTS)
		@EntryTag("input")
		List<ChannelMappingConfig> getInputs();

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
		List<ChannelMappingConfig> getOutputs();
	}

	private final String _dialogViewPath;

	private final boolean _closeOnBackdrop;

	private final List<ChannelMappingConfig> _inputs;

	/**
	 * Creates a new {@link OpenDialogCommand}.
	 */
	@CalledByReflection
	public OpenDialogCommand(InstantiationContext context, Config config) {
		_dialogViewPath = ViewLoader.VIEW_BASE_PATH + config.getDialogView();
		_closeOnBackdrop = config.getCloseOnBackdrop();
		_inputs = config.getInputs();
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

		// Propagate error sink from parent (same pattern as ReferenceElement).
		if (context instanceof ViewContext) {
			ViewContext parentViewContext = (ViewContext) context;
			ErrorSink parentErrorSink = parentViewContext.getErrorSink();
			if (parentErrorSink != null) {
				dialogContext = dialogContext.withErrorSink(parentErrorSink);
			}

			// One-shot copy of input channel values.
			for (ChannelMappingConfig mapping : _inputs) {
				String fromName = mapping.getFrom();
				if (!parentViewContext.hasChannel(fromName)) {
					Logger.warn("Input channel '" + fromName + "' not found in parent context, skipping.",
						OpenDialogCommand.class);
					continue;
				}
				ViewChannel parentChannel = parentViewContext.resolveChannel(new ChannelRef(fromName));
				DefaultViewChannel dialogChannel = new DefaultViewChannel(mapping.getTo());
				dialogChannel.set(parentChannel.get());
				dialogContext.registerChannel(mapping.getTo(), dialogChannel);
			}
		}

		// Build the dialog control tree.
		ReactControl dialogControl = (ReactControl) dialogView.createControl(dialogContext);

		mgr.openDialog(_closeOnBackdrop, dialogControl, result -> {
			// Output channel propagation: deferred to future implementation.
		});

		return HandlerResult.DEFAULT_RESULT;
	}
}
