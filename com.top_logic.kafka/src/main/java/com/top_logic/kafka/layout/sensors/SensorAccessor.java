/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.layout.sensors;

import com.top_logic.kafka.services.sensors.Sensor;
import com.top_logic.layout.ReadOnlyAccessor;

/**
 * Accessor to the sensor interface.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SensorAccessor extends ReadOnlyAccessor<Sensor<?, ?>> {

    /** Special access to see, if sensor is active. */
    public static final String KEY_ACTIVE = "active";

    /** Access to {@link Sensor#getName()} . */
    public static final String KEY_NAME = "name";

    /** Access to {@link Sensor#getOperation()} . */
    public static final String KEY_OPERATION = "operation";

    /** Access to {@link Sensor#getActivityState()} . */
    public static final String KEY_STATE = "state";

    /** Access to {@link Sensor#getID()} . */
    public static final String KEY_ID = "id";
    
    /** Access to {@link Sensor#getDataSource()} . */
    public static final String SOURCE_ID = "source";

    /** Access to {@link Sensor#getType()} . */
    public static final String KEY_TYPE = "type";

    /** Access to {@link Sensor#getSignal()} . */
    public static final String KEY_SIGNAL = "signal";

    /** Access to {@link Sensor#getSignalDate()} . */
    public static final String KEY_DATE = "date";

    @Override
	public Object getValue(Sensor<?, ?> aSensor, String aKey) {
        switch (aKey) {
            case KEY_ACTIVE:
                return aSensor.getActivityState();
            case KEY_NAME:
                return aSensor.getName();
            case KEY_OPERATION:
                return aSensor.getOperation();
            case KEY_STATE:
				return aSensor.getSensorState();
            case KEY_ID:
                return aSensor.getID();
            case SOURCE_ID:
                return aSensor.getDataSource();
            case KEY_TYPE:
                return aSensor.getType();
            case KEY_SIGNAL:
                return aSensor.getSignal();
            case KEY_DATE:
                return aSensor.getSignalDate();
            default:
                return null;
        }
    }
}

