/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.sensors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Providing access to sensor instances.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SensorService extends ConfiguredManagedClass<SensorService.Config>
{

    /**
     * Service Configuration Interface
     */
    public interface Config extends ConfiguredManagedClass.Config<SensorService> {

		/** The builder to create the sensors of this service. */
        @InstanceFormat
        List<SensorBuilder> getBuilders();
    }

	private final Map<String, Sensor<?, ?>> _sensors = new HashMap<>();

    /** 
     * Creates a {@link SensorService}.
     */
	public SensorService(final InstantiationContext context, final Config config) {
        super(context,config);

        this.createSensors();
    }

	/**
	 * The known sensors as copy of the managed sensor list.
	 */
	public List<Sensor<?, ?>> getSensors() {
        synchronized (_sensors) {
			return new ArrayList<>(_sensors.values());
		}
    }

	/**
	 * Add the given sensor to this service.
	 * 
	 * <p>
	 * <b>Note: </b>Has no effect if the given sensor was already added.
	 * </p>
	 * 
	 * @param sensor
	 *            the {@link Sensor} to be added
	 */
	public void addSensor(final Sensor<?, ?> sensor) {
    	synchronized(_sensors) {
    		if(!_sensors.containsKey(sensor.getID())) {
    			_sensors.put(sensor.getID(), sensor);
    		}
    	}
    }
    
	/**
	 * @param id
	 *            the {@link Sensor#getID()} to resolve the sensor for
	 * @return the {@link Sensor} with the given identifier or {@code null} if
	 *         none found
	 */
	public Sensor<?, ?> getSensor(final String id) {
    	synchronized (_sensors) {
    		return _sensors.get(id);
		}
    }

	/**
	 * Create the sensor map out of the service configuration.
	 */
    protected void createSensors() {
		synchronized (_sensors) {
			for (final SensorBuilder builder : this.getConfig().getBuilders()) {
				for (final Sensor<?, ?> sensor : builder.createSensors()) {
					if (sensor != null) {
						_sensors.put(sensor.getID(), sensor);
					}
				}
			}
		}
    }

    /**
     * the singleton {@link SensorService} instance
     */
    public static SensorService getInstance() {
        return Module.INSTANCE.getImplementationInstance();
    }

    /**
     * Module for {@link SensorService}.
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static final class Module extends TypedRuntimeModule<SensorService> {

        /**
         * Singleton {@link Module} instance.
         */
        public static final Module INSTANCE = new Module();

        /**
         * Creates a {@link Module}.
         */
        private Module() {
            // enforce singleton pattern
        }

        @Override
        public Class<SensorService> getImplementation() {
            return SensorService.class;
        }
    }
}

