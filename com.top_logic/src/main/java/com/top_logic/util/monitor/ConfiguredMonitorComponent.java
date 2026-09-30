/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedPolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.util.monitor.MonitorMessage.Status;

/**
 * A {@link MonitorComponent} which is a {@link ConfiguredInstance}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public abstract class ConfiguredMonitorComponent<C extends ConfiguredMonitorComponent.Config>
		extends AbstractMonitorComponent implements ConfiguredInstance<C> {

	/** {@link ConfigurationItem} for the {@link ConfiguredMonitorComponent}. */
	public interface Config extends NamedPolymorphicConfiguration<ConfiguredMonitorComponent<?>> {

		/** A description of what is being monitored. */
		String getDescription();

	}

	private final C _config;

	/** {@link TypedConfiguration} constructor for {@link ConfiguredMonitorComponent}. */
	public ConfiguredMonitorComponent(@SuppressWarnings("unused") InstantiationContext context, C config) {
		_config = config;
	}

	@Override
	public C getConfig() {
		return _config;
	}

	@Override
	public String getName() {
		return getConfig().getName();
	}

	@Override
	public String getDescription() {
		return getConfig().getDescription();
	}

	@Override
	public final void checkState(MonitorResult result) {
		try {
			checkStateInTryCatch(result);
		} catch (Throwable exception) {
			String message = "Failed to determine state. Cause:  " + exception.getMessage();
			result.addMessage(createMessage(Status.ERROR, message));
			logError(exception);
		}
	}

	/**
	 * Check the state.
	 * <p>
	 * Is called in {@link #checkState(MonitorResult)}. It is surrounded with a try-catch block to
	 * catch any exception, log it and write it into the result.
	 * </p>
	 */
	protected abstract void checkStateInTryCatch(MonitorResult result);

	/** Creates a {@link MonitorMessage} for this {@link MonitorComponent}. */
	protected MonitorMessage createMessage(Status statusType, String message) {
		return new MonitorMessage(statusType, message, getName());
	}

	private void logError(Throwable exception) {
		Logger.error(exception.getMessage(), exception, ConfiguredMonitorComponent.class);
	}

}
