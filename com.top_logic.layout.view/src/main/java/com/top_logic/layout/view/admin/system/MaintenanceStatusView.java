/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.util.Date;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.Resources;

/**
 * Displays the current application maintenance state as a text line and seeds the configured
 * {@link Config#getState() state channel} with the matching {@link MaintenanceModeAction#token(int)
 * token} so the maintenance commands gate correctly from the moment the view opens.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element). The
 * status text reflects global, server-side state held by the {@link MaintenanceWindowManager} (and, while
 * a window is scheduled or active, its message), which no generic value control can express. The view
 * re-renders whenever the state channel changes, i.e. after each maintenance command.
 * </p>
 */
public class MaintenanceStatusView implements UIElement {

	/**
	 * Configuration for {@link MaintenanceStatusView}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getState()}. */
		String STATE = "state";

		@Override
		@ClassDefault(MaintenanceStatusView.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel seeded with the current state token on open and re-read on change; shared with the
		 * commands that gate on it.
		 */
		@Name(STATE)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getState();
	}

	private final ChannelRef _stateRef;

	/**
	 * Creates a new {@link MaintenanceStatusView} from configuration.
	 */
	@CalledByReflection
	public MaintenanceStatusView(InstantiationContext context, Config config) {
		_stateRef = config.getState();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ReactTextControl control = new ReactTextControl(context, statusText(), null);
		if (_stateRef != null) {
			ViewChannel state = context.resolveChannel(_stateRef);
			// Seed the gating channel before listening, so the initial set does not re-render redundantly.
			state.set(MaintenanceModeAction.token(MaintenanceWindowManager.getInstance().getMaintenanceModeState()));
			ChannelListener listener = (sender, oldValue, newValue) -> control.setText(statusText());
			state.addListener(listener);
			control.addCleanupAction(() -> state.removeListener(listener));
		}
		return control;
	}

	/**
	 * The localized one-line status for the current maintenance state, including the scheduled activation
	 * time while a window is pending and the user message while a window is scheduled or active.
	 */
	private static String statusText() {
		MaintenanceWindowManager manager = MaintenanceWindowManager.getInstance();
		Resources resources = Resources.getInstance();
		switch (manager.getMaintenanceModeState()) {
			case MaintenanceWindowManager.IN_MAINTENANCE_MODE:
				return withMessage(resources.getString(I18NConstants.MAINTENANCE_STATUS_ACTIVE), manager.getMessage());
			case MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE:
				String when = HTMLFormatter.getInstance().formatShortDateTime(new Date(manager.getFinishedTime()));
				return withMessage(resources.getString(I18NConstants.MAINTENANCE_STATUS_PENDING__TIME.fill(when)),
					manager.getMessage());
			default:
				return resources.getString(I18NConstants.MAINTENANCE_STATUS_NORMAL);
		}
	}

	/**
	 * The given status text, with the user message appended in parentheses when it is non-empty.
	 */
	private static String withMessage(String text, String message) {
		if (message == null || message.isBlank()) {
			return text;
		}
		return text + " (" + message + ")";
	}
}
