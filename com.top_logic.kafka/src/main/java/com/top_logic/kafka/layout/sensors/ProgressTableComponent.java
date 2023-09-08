/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.layout.sensors;

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
import com.top_logic.kafka.services.sensors.SensorService;
import com.top_logic.layout.table.component.TableComponent;

/**
 * Table component automatically updating its values via the {@link ProgressTableComponent.Config#getUpdater()}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ProgressTableComponent extends TableComponent implements Runnable {

    /**
     * Configuration options of {@link ProgressTableComponent}.
     */
	public interface Config extends TableComponent.Config {

        /** Whether automatic update is enabled by default. */
        @BooleanDefault(true)
        boolean isDefaultActive();

        /** The time between two UI refreshes in milliseconds. */
        @LongDefault(1000)
        long getUpdateInterval();

        /** Helper class for finding rows to be updated. */
        @Mandatory
        PolymorphicConfiguration<TableComponentValueUpdater> getUpdater();
    }

    private boolean _active;
    private long _updateInterval;
    private ScheduledFuture<?> _timer;
    private TableComponentValueUpdater _updater;

    /** 
     * Creates a {@link ProgressTableComponent}.
     */
    public ProgressTableComponent(InstantiationContext aContext, Config aConfig) throws ConfigurationException {
        super(aContext, aConfig);

        _active = aConfig.isDefaultActive();
        _updateInterval = aConfig.getUpdateInterval();
    }

    @Override
    public void run() {
        this.updateData();
    }

    @Override
    protected void becomingVisible() {
        super.becomingVisible();

        this.updateMonitoringState();
    }

    @Override
    protected void becomingInvisible() {
        super.becomingInvisible();

        this.cancelTimer();
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
                PolymorphicConfiguration<TableComponentValueUpdater> theUpdater = this.getConfig().getUpdater();
                ScheduledExecutorService theService = getMainLayout().getWindowScope().getUIExecutor();

                _timer = theService.scheduleWithFixedDelay(this, _updateInterval, _updateInterval, TimeUnit.MILLISECONDS);
                _updater = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(theUpdater);
            }
        }
        else {
            this.cancelTimer();
        }
    }

    /**
     * Toggle the update mode (automatic versus paused).
     */
    protected void toggleActive() {
        _active = !_active;

        this.updateMonitoringState();

        if (_active) {
            this.updateData();
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
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static interface TableComponentValueUpdater {
        
        /** 
         * Perform the update of the given table rows and return the area of rows changed.
         * Any needed events must be created by the updater
         * 
         * @param    progressTableComponent   The rows to update the data for.
         */
        void update(ProgressTableComponent progressTableComponent);
    }
}
 