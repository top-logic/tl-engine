/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Collections;
import java.util.Map;

/**
 * The MapResourceProvider creates labels provided within a map.
 *
 * @author <a href=mailto:CBR@top-logic.com>CBR</a>
 */
public class MapResourceProvider extends DefaultResourceProvider {

    /** The label map with the objects to label as keys and the labels as value. */
    private Map labelMap;



    /**
     * Creates a new MapResourceProvider with the given label map.
     *
     * @param aLabelMap
     *            a map with the objects to label as keys and the labels as value
     */
    public MapResourceProvider(Map aLabelMap) {
        labelMap = aLabelMap == null ? Collections.EMPTY_MAP : aLabelMap;
    }



    @Override
	public String getLabel(Object aObject) {
        if (aObject == null) {
            return "";
        }
        Object theLabel = labelMap.get(aObject);
        return theLabel == null ? super.getLabel(aObject) : theLabel.toString();
    }

}
