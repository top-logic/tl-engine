/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.sensors;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.kafka.services.sensors.Sensor.AbstractSensor;

/**
 * Build dummy sensors which will generate random values.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class DummySensorBuilder implements SensorBuilder {

    /**
     * Configuration of this dummy sensor builder 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public interface Config extends PolymorphicConfiguration<DummySensorBuilder> {

		/** Name of this dummy sensor. */
        @Mandatory
        String getName();
        
		/** Unique ID of this dummy sensor. */
        @Mandatory
        String getID();

		/** Delay between {@link DummySensor#getSignal() signal requests}. */
        @Mandatory
        Integer getDuration();
    }

    private DummySensorBuilder.Config _config;

    /** 
     * Creates a {@link DummySensorBuilder}.
     */
    public DummySensorBuilder(@SuppressWarnings("unused") final InstantiationContext context, final DummySensorBuilder.Config config) {
        _config = config;
    }

    @Override
	public List<? extends Sensor<?, ?>> createSensors() {
        Config theConfig = this._config;

        return Collections.singletonList(new DummySensor<>(theConfig.getName(), theConfig.getID(), theConfig.getDuration()));
    }

    /**
     * Dummy implementation of a sensor to providing some randomized sensor data. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
	public static class DummySensor<A extends Object, R extends Object> extends AbstractSensor<A, R> {

        // Attributes

        /** The last signal provided by this sensor. */
        private Object signal;

        /** The time stamp the last signal has been produced. */
        private Date timestamp;

        /** Randomizer to generate new signals. */
        private Random random;

        /** Delay in calls to {@link #getSignal()} to pause before new values provided. */
        private int requestedDelay;

        private int delay;

        // Constructors
        
        /** 
         * Creates a {@link DummySensor}.
         */
        public DummySensor(String aName, String anID, int aDelay) {
            this(aName, anID, null, null, aDelay);
        }

        /** 
         * Creates a {@link DummySensor}.
         */
		public DummySensor(String aName, String anID, A anActivity, R aType, int aDelay) {
            super(aName, anID, anActivity, aType);

            this.requestedDelay = aDelay;
            this.delay = 0;
            this.random = new Random();
        }

        // Overridden methods from AbstractSensor

        @Override
        public SensorActivityState getActivityState() {
            long theAge = this.getSignalAge();

            if (theAge < 2) {
                return SensorActivityState.RUNNING;
            }
            else if (theAge < 10) {
                return SensorActivityState.ACTIVE;
            }
            else {
                return SensorActivityState.INACTIVE;
            }
        }

        @Override
        public Object getSignal() {
            if (this.needsSignal()) {
                this.signal = this.createSignal();
            }

            return this.signal;
        }

        @Override
        public Date getSignalDate() {
            return this.timestamp;
        }

        @Override
		public String getSensorState() {
            return "OK";
        }

        // Protected methods

        /** 
         * Create a new signal for this sensor.
         * 
         * <p>Calling this method will update the time stamp 
         * and create a new random value.</p>
         * 
         * @return    The new signal from the sensor.
         */
        protected Object createSignal() {
            this.timestamp = new Date();

            return this.random.nextLong();
        }

        /** 
         * Calculate if we need to create a new signal.
         * 
         * @return   <code>true</code> when new signal is needed.
         * @see      #createSignal()
         */
        protected boolean needsSignal() {
            if (this.timestamp != null) {
                if (this.delay++ >= this.requestedDelay) {
                    this.delay = 0;

                    return true;
                }
                else {
                    return false;
                }
            }
            else {
                return true;
            }
        }

        /** 
         * Return the age of the last signal received in seconds.
         * 
         * @return    The age of the last signal in seconds.
         */
        protected long getSignalAge() {
            if (this.timestamp == null) {
                return Long.MAX_VALUE;
            }
            else {
                long theMillis = new Date().getTime() - this.timestamp.getTime();

                return theMillis / 1000l;
            }
        }
    }
}
