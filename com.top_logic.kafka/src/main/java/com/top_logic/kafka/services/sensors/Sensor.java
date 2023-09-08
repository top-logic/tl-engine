/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.sensors;

import java.util.Date;

import com.top_logic.basic.tools.NameBuilder;

/**
 * Interface for a sensor displayed in the sensor cockpit.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface Sensor<A extends Object, R extends Object> {

    /**
     * Possible states of a sensor. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public enum SensorActivityState {

        /** Has been inactive (or never been started). */
        INACTIVE,

        /** Has been active in the last hour. */
        ACTIVE,

        /** Is currently active or has send a signal in the last minute. */
        RUNNING
    }

    /**
     * The name of the sensor.
     */
    public String getName();

    /**
     * Unique ID of the sensor.
     */
    public String getID();
    
    /**
     * Name of the data source providing the signals.
     */
    public String getDataSource();

    /**
     * Operation this sensor delivers information for, may be <code>null</code>.
     */
	public A getOperation();

    /**
     * Current activity state of the sensor.
     */
    public SensorActivityState getActivityState();

    /**
     * Current state as reported by the sensor, may be <code>null</code>.
     */
	public String getSensorState();

    /**
     * Resource type of the sensor.
     */
	public R getType();

    /**
     * The signal currently send by the sensor, may be <code>null</code> 
     *           when sensor hasn't send a signal at all.
     */
    public Object getSignal();

    /**
     * Date the signal has been send by the sensor, may be <code>null</code> 
     *           when sensor hasn't send a signal at all.
     */
    public Date getSignalDate();

    /**
     * Abstract base implementation of a sensor. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
	public static abstract class AbstractSensor<A extends Object, R extends Object> implements Sensor<A, R> {

        // Attributes

        private final String name;

        private final String id;

		private final R type;

		private A activity;

        // Constructors

        /** 
         * Creates a {@link AbstractSensor}.
         */
        public AbstractSensor(String aName, String anID) {
            this(aName, anID, null, null);
        }

		// Constructors

		/**
		 * Creates a {@link AbstractSensor}.
		 */
		public AbstractSensor(String aName, String anID, A anActivity, R aType) {
            this.name     = aName;
            this.id       = anID;
            this.activity = anActivity;
            this.type     = aType;
        }

        // Implementation of interface Sensor

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getID() {
            return this.id;
        }

        @Override
        public String getDataSource() {
            return "<i>TopLogic</i>";
        }

        @Override
		public A getOperation() {
            return this.activity;
        }

        @Override
		public R getType() {
            return this.type;
        }

        // Overridden methods from Object

        @Override
        public String toString() {
            return new NameBuilder(this)
                    .add("name", this.name)
                    .add("id", this.id)
                    .build();
        }
    }
}
