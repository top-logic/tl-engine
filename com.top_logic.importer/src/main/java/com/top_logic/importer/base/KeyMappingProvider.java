/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.base;

import java.util.List;
import java.util.Map;

import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Provide unique IDs for an {@link Wrapper} or a map of values read from excel file.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface KeyMappingProvider {

    /** 
     * Return a unique ID from the given values by combining them as described in given {@link List}.
     *
     * <p>This method must return the same ID as {@link #getID(List, Wrapper)} for same data/object combination!</p>
     * 
     * @param    someValues    The values to get the unique ID from.
     * @return   The requested ID.
     */
    String getID(List<String> someKeys, Map<String, Object> someValues);

    /** 
     * Return a unique ID from the given {@link Wrapper} by combining them as described in given {@link List}.
     *
     * <p>This method must return the same ID as {@link #getID(List, Map)} for same data/object combination!</p>
     * 
     * @param    anAttributed    The attributed to get the unique ID from.
     * @return   The requested ID.
     */
    String getID(List<String> someKeys, Wrapper anAttributed);

    /**
     * Default implementation of the key mapping interface. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class DefaultKeyMappingProvider implements KeyMappingProvider {

        // Constructors
        
        /** 
         * Creates a {@link DefaultKeyMappingProvider}.
         */
        public DefaultKeyMappingProvider() {
        }

        // Implementation of interface KeyMappingProvider

        @Override
        public String getID(List<String> someKeys, Map<String, Object> someValues) {
            StringBuffer theResult = new StringBuffer();

            for (String theKey : someKeys) {
                Object theValue = someValues.get(theKey);

                if (theValue != null) {
                    theResult.append(theValue.toString()).append(';');
                }
            }

            return theResult.toString();
        }

        @Override
        public String getID(List<String> someKeys, Wrapper anAttributed) {
            StringBuffer theResult = new StringBuffer();

            for (String theKey : someKeys) {
                Object theValue = anAttributed.getValue(theKey);

                if (theValue != null) {
                    theResult.append(theValue.toString()).append(';');
                }
            }

            return theResult.toString();
        }
    }
}
