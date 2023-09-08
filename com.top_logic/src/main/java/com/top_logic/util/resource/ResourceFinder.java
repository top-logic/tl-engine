/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.resource;

import java.util.Collection;
import java.util.Map;

/**
 * This is a little helper class for ResourceEditor
 * to locate all resource files used in the current
 * system.
 *
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface ResourceFinder {
    
    /**
     * Get all resource sets involved in internationalization.
     * The keys to the map are the names ofthe resource sets.
     * The values are Maps from locale names the respective resource name.
     * The map is ordered by priority of lookup.
     * 
     * @return a sorted map of resource sets.
     */
    public Map<String, Map<String, Collection<String>>> getI18NResourceSets();

}
