/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.sensors;

import java.util.List;

/**
 * Build a new sensor out of the configuration.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface SensorBuilder {

    /** 
     * Create new sensors.
     * 
     * <p>When the builder cannot create the sensor, it has to return an empty list.</p>
     * 
     * @return    The new sensors created.
     */
	List<? extends Sensor<?, ?>> createSensors();
}
