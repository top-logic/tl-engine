/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.layout.sensors;

import com.top_logic.basic.xml.TagUtil;
import com.top_logic.kafka.services.sensors.Sensor.SensorActivityState;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.EnumResourceProvider;

/**
 * Provide matching images for the requested {@link SensorActivityState}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SensorActivityStateResourceProvider extends AbstractResourceProvider {

    @Override
    public String getLabel(Object object) {
        return null;
    }

    @Override
    public ThemeImage getImage(Object anObject, Flavor aFlavor) {
        if (anObject instanceof SensorActivityState) {
            SensorActivityState theState = (SensorActivityState) anObject;
            String              theType  = "SensorActivityState." + theState.name();

            if (aFlavor == Flavor.ENLARGED) {
                theType += ".large";
            }

			return ThemeImage.typeIcon(theType);
        }

        return null;
    }

    @Override
    public String getTooltip(Object anObject) {
		return (anObject instanceof Enum) ? TagUtil.encodeXML(EnumResourceProvider.INSTANCE.getLabel(anObject)) : null;
    }
}
