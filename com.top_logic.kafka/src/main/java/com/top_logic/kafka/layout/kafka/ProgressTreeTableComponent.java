/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.layout.kafka;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.kafka.layout.sensors.ProgressTableComponent;
import com.top_logic.kafka.layout.sensors.ProgressTableComponent.TableComponentValueUpdater;
import com.top_logic.kafka.services.sensors.SensorService;
import com.top_logic.layout.table.tree.TreeTableComponent;

/**
 * Tree based table which can change it's content without user interaction.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ProgressTreeTableComponent extends TreeTableComponent implements Runnable {

	/**
	 * Configuration options of {@link ProgressTableComponent}.
	 */
	public interface Config extends TreeTableComponent.Config {

		/** Whether automatic update is enabled by default. */
		@BooleanDefault(true)
		boolean isDefaultActive();

		/** The time between two UI refreshes in milliseconds. */
		@LongDefault(1000)
		long getUpdateInterval();

		/** Helper class for finding rows to be updated. */
		@Mandatory
		PolymorphicConfiguration<TreeTableComponentValueUpdater> getUpdater();

	}

	private boolean _active;

	private long _updateInterval;

	private ScheduledFuture<?> _timer;

	private TreeTableComponentValueUpdater _updater;

	/**
	 * Creates a {@link ProgressTreeTableComponent}.
	 */
	public ProgressTreeTableComponent(InstantiationContext aContext, Config aConfig) throws ConfigurationException {
		super(aContext, aConfig);

		_active = aConfig.isDefaultActive();
		_updateInterval = aConfig.getUpdateInterval();
	}

	@Override
	public void run() {
		updateData();
	}

	@Override
	protected void becomingVisible() {
		super.becomingVisible();

		updateMonitoringState();
	}

	@Override
	protected void becomingInvisible() {
		super.becomingInvisible();

		cancelTimer();
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return SensorService.Module.INSTANCE.isActive() && super.supportsInternalModel(anObject);
	}

	/**
	 * Start the process at the {@link TableComponentValueUpdater}.
	 */
	protected void updateData() {
		_updater.update(this);
	}

	/**
	 * Update the monitoring state and initialize the timer.
	 */
	protected void updateMonitoringState() {
		if (_active) {
			if (_timer == null) {
				PolymorphicConfiguration<TreeTableComponentValueUpdater> theUpdater = getConfig().getUpdater();
				ScheduledExecutorService theService = getMainLayout().getWindowScope().getUIExecutor();

				_timer =
					theService.scheduleWithFixedDelay(this, _updateInterval, _updateInterval, TimeUnit.MILLISECONDS);
				_updater = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(theUpdater);
			}
		} else {
			cancelTimer();
		}
	}

	/**
	 * Toggle the update mode (automatic versus paused).
	 */
	protected void toggleActive() {
		_active = !_active;

		updateMonitoringState();

		if (_active) {
			updateData();
		}
	}

	/**
	 * Cancel the timer and deactivate reload.
	 */
	protected void cancelTimer() {
		if (_timer != null) {
			_timer.cancel(false);
			_timer = null;
			_updater = null;
		}
	}

	/**
	 * Updater to be asked for changes in the table rows.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static interface TreeTableComponentValueUpdater {

		/**
		 * Perform the update of the given table component.
		 * 
		 * <p>
		 * Any needed events must be created by the updater.
		 * </p>
		 * 
		 * @param progressTableComponent
		 *        Component calling for an update.
		 */
		void update(ProgressTreeTableComponent progressTableComponent);
	}
}
