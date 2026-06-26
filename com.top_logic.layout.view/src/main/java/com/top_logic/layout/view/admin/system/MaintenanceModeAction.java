/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLObject;

/**
 * {@link ViewAction} that switches the application maintenance mode and returns the resulting state as
 * a {@link #STATE_NORMAL} / {@link #STATE_PENDING} / {@link #STATE_ACTIVE} token for command gating and
 * status display.
 *
 * <p>
 * The token is meant to be written to the channel that the {@code maintenance.view.xml} commands gate on
 * (via {@code <visible-if>}) and that the {@link MaintenanceStatusView} re-renders from. For
 * {@link Mode#ENTER} the delay and message are read from the configured {@link Config#getModel() model
 * channel} (the transient {@code tl.admin:MaintenanceMode} bound to the enter dialog's form); an empty or
 * zero delay enters maintenance mode immediately.
 * </p>
 *
 * @implNote Delegates to {@link MaintenanceWindowManager#enterMaintenanceWindow(long)},
 *           {@link MaintenanceWindowManager#abortEnterMaintenanceWindow()} and
 *           {@link MaintenanceWindowManager#leaveMaintenanceWindow()}; the resulting state comes from
 *           {@link MaintenanceWindowManager#getMaintenanceModeState()}.
 */
public class MaintenanceModeAction implements ViewAction {

	/** State token for normal (non-maintenance) operation. */
	public static final String STATE_NORMAL = "NORMAL";

	/** State token for a scheduled, not-yet-active maintenance window. */
	public static final String STATE_PENDING = "PENDING";

	/** State token for an active maintenance window. */
	public static final String STATE_ACTIVE = "ACTIVE";

	/** Model attribute holding the delay in minutes before entering maintenance mode. */
	private static final String DELAY_MINUTES = "delayMinutes";

	/** Model attribute holding the message shown to logged-in users. */
	private static final String MESSAGE = "message";

	/**
	 * What a {@link MaintenanceModeAction} does before returning the state token.
	 */
	public enum Mode {
		/** Just return the current maintenance state. */
		REFRESH,

		/** Enter (or schedule entering) maintenance mode using the model channel's delay and message. */
		ENTER,

		/** Abort a scheduled, not-yet-active maintenance window. */
		ABORT,

		/** Leave maintenance mode and return to normal operation. */
		LEAVE;
	}

	/**
	 * Configuration for {@link MaintenanceModeAction}.
	 *
	 * <p>
	 * App-specific action, referenced by {@code class=} in the maintenance view rather than claiming a
	 * global {@code @TagName}.
	 * </p>
	 */
	public interface Config extends PolymorphicConfiguration<MaintenanceModeAction> {

		/** Configuration name for {@link #getMode()}. */
		String MODE = "mode";

		/** Configuration name for {@link #getModel()}. */
		String MODEL = "model";

		@Override
		@ClassDefault(MaintenanceModeAction.class)
		Class<? extends MaintenanceModeAction> getImplementationClass();

		/**
		 * What the action does before returning the state token.
		 */
		@Name(MODE)
		@Mandatory
		Mode getMode();

		/**
		 * Name of the channel holding the transient {@code tl.admin:MaintenanceMode} input; required only
		 * for {@link Mode#ENTER}.
		 */
		@Name(MODEL)
		@Nullable
		String getModel();
	}

	private final Mode _mode;

	private final String _modelChannel;

	/**
	 * Creates a new {@link MaintenanceModeAction} from configuration.
	 */
	@CalledByReflection
	public MaintenanceModeAction(InstantiationContext context, Config config) {
		_mode = config.getMode();
		_modelChannel = config.getModel();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		MaintenanceWindowManager manager = MaintenanceWindowManager.getInstance();
		switch (_mode) {
			case ENTER:
				enter(context, manager);
				break;
			case ABORT:
				manager.abortEnterMaintenanceWindow();
				break;
			case LEAVE:
				manager.leaveMaintenanceWindow();
				break;
			case REFRESH:
				break;
		}
		return token(manager.getMaintenanceModeState());
	}

	/**
	 * Enters (or schedules entering) maintenance mode using the delay and message from the model channel.
	 */
	private void enter(ReactContext context, MaintenanceWindowManager manager) {
		long minutes = 0;
		TLObject model = model(context);
		if (model != null) {
			Object delay = model.tValueByName(DELAY_MINUTES);
			if (delay instanceof Number number) {
				minutes = Math.max(0, number.longValue());
			}
			Object message = model.tValueByName(MESSAGE);
			if (message instanceof String text && !text.isBlank()) {
				manager.setMessage(text);
			}
		}
		manager.enterMaintenanceWindow(minutes * 60 * 1000);
	}

	/**
	 * The transient input object held by the configured model channel, or {@code null} when unavailable.
	 */
	private TLObject model(ReactContext context) {
		if (_modelChannel == null || !(context instanceof ViewContext viewContext)) {
			return null;
		}
		if (!viewContext.hasChannel(_modelChannel)) {
			return null;
		}
		Object value = viewContext.resolveChannel(new ChannelRef(_modelChannel)).get();
		return value instanceof TLObject object ? object : null;
	}

	/**
	 * The {@link #STATE_NORMAL} / {@link #STATE_PENDING} / {@link #STATE_ACTIVE} token for the given
	 * {@link MaintenanceWindowManager#getMaintenanceModeState() manager state}.
	 */
	public static String token(int state) {
		switch (state) {
			case MaintenanceWindowManager.IN_MAINTENANCE_MODE:
				return STATE_ACTIVE;
			case MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE:
				return STATE_PENDING;
			default:
				return STATE_NORMAL;
		}
	}
}
