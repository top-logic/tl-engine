/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ReloadableControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelBindingConfig;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * {@link ViewAction} that opens a modal dialog.
 *
 * <p>
 * The action's input is injected into a named dialog channel ({@link Config#getBindInputTo()},
 * default: {@code "model"}). Additional parent channels can be shared via live bindings.
 * </p>
 *
 * <p>
 * This action contains the core dialog-opening logic. {@link OpenDialogCommand} extends this with
 * command presentation options (label, image, executability).
 * </p>
 */
public class OpenDialogAction implements ViewAction {

	/**
	 * Configuration for {@link OpenDialogAction}.
	 */
	@TagName("open-dialog")
	public interface Config extends PolymorphicConfiguration<OpenDialogAction> {

		@Override
		@ClassDefault(OpenDialogAction.class)
		Class<? extends OpenDialogAction> getImplementationClass();

		/**
		 * Path to the dialog view file, relative to {@code /WEB-INF/views/}.
		 */
		@Name("dialog-view")
		@Mandatory
		String getDialogView();

		/**
		 * Whether clicking the backdrop closes the dialog.
		 */
		@Name("close-on-backdrop")
		@BooleanDefault(false)
		boolean getCloseOnBackdrop();

		/**
		 * Dialog channel name to receive the action's input object.
		 *
		 * <p>
		 * Defaults to {@code "model"} if not specified.
		 * </p>
		 */
		@Name("bind-input-to")
		@Nullable
		String getBindInputTo();

		/**
		 * Channel bindings from the parent scope to the dialog.
		 */
		@Name("bindings")
		@DefaultContainer
		List<ChannelBindingConfig> getBindings();
	}

	private final String _dialogViewPath;

	private final boolean _closeOnBackdrop;

	private final String _bindInputTo;

	private final List<ChannelBindingConfig> _bindings;

	/**
	 * Creates a new {@link OpenDialogAction} from configuration.
	 */
	@CalledByReflection
	public OpenDialogAction(InstantiationContext context, Config config) {
		this(config.getDialogView(), config.getCloseOnBackdrop(), config.getBindInputTo(),
			config.getBindings());
	}

	/**
	 * Creates a new {@link OpenDialogAction} with explicit parameters.
	 */
	public OpenDialogAction(String dialogView, boolean closeOnBackdrop, String bindInputTo,
			List<ChannelBindingConfig> bindings) {
		_dialogViewPath = ViewLoader.VIEW_BASE_PATH + dialogView;
		_closeOnBackdrop = closeOnBackdrop;
		_bindInputTo = bindInputTo != null ? bindInputTo : "model";
		_bindings = bindings != null ? bindings : Collections.emptyList();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		// Inject the chain's input into the configured dialog channel.
		Map<String, ?> channelValues =
			input != null ? Collections.singletonMap(_bindInputTo, input) : Collections.emptyMap();
		openDialog(context, _dialogViewPath, _closeOnBackdrop, channelValues, _bindings);
		return input;
	}

	/**
	 * Opens a modal dialog loading the given view, seeding it with the given channel values and live
	 * bindings inherited from the parent context.
	 *
	 * <p>
	 * This is the reusable core of {@link OpenDialogAction#execute(ReactContext, Object)}; it is also
	 * used to chain follow-up dialogs from Java {@link ViewAction}s (e.g. the forced change-password
	 * step of the login flow) where more than one named channel must be transferred to the dialog.
	 * </p>
	 *
	 * @param context
	 *        The current context; must provide a {@link DialogManager} (otherwise this is a no-op).
	 * @param dialogViewPath
	 *        The full view path (including {@link ViewLoader#VIEW_BASE_PATH}).
	 * @param closeOnBackdrop
	 *        Whether clicking the backdrop closes the dialog.
	 * @param channelValues
	 *        Channel name to value; each entry is registered as a fresh channel on the dialog
	 *        context, carrying the given value.
	 * @param bindings
	 *        Live bindings inherited from the parent context (may be empty).
	 */
	public static void openDialog(ReactContext context, String dialogViewPath, boolean closeOnBackdrop,
			Map<String, ?> channelValues, List<ChannelBindingConfig> bindings) {
		DialogManager mgr = context.getDialogManager();
		if (mgr == null) {
			return;
		}

		ViewElement dialogView;
		try {
			dialogView = ViewLoader.getOrLoadView(dialogViewPath);
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to load dialog view: " + dialogViewPath, ex);
		}

		ViewContext dialogContext = new DefaultViewContext(context);

		if (context instanceof ViewContext) {
			ViewContext parentViewContext = (ViewContext) context;
			ErrorSink parentErrorSink = parentViewContext.getErrorSink();
			if (parentErrorSink != null) {
				dialogContext = dialogContext.withErrorSink(parentErrorSink);
			}

			for (ChannelBindingConfig binding : bindings) {
				String parentChannelName = binding.getTo().getChannelName();
				if (!parentViewContext.hasChannel(parentChannelName)) {
					Logger.warn("Channel '" + parentChannelName + "' not found in parent context, skipping.",
						OpenDialogAction.class);
					continue;
				}
				ViewChannel parentChannel = parentViewContext.resolveChannel(binding.getTo());
				dialogContext.registerChannel(binding.getChannel(), parentChannel);
			}
		}

		// Seed the dialog with the requested channel values.
		for (Map.Entry<String, ?> entry : channelValues.entrySet()) {
			DefaultViewChannel channel = new DefaultViewChannel(entry.getKey());
			channel.set(entry.getValue());
			dialogContext.registerChannel(entry.getKey(), channel);
		}

		ReactControl dialogControl = new ReloadableControl(dialogViewPath, dialogContext,
			(ReactControl) dialogView.createControl(dialogContext));

		mgr.openDialog(closeOnBackdrop, dialogControl, result -> {
			// Dialog closed.
		});
	}
}
