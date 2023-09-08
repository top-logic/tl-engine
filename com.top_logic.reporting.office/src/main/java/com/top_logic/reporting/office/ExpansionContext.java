/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office;

import java.util.Locale;
import java.util.Map;


/**
 * The context an expansion engine needs to operate in.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public interface ExpansionContext {
    
    /**
     * The locale this report is to render. This may influence the template the report handler
     * uses for generation.
     * @return a Locale. In case nothing was specified the @see Locale.getDefault() is returned.
     */
    public Locale getReportLocale();

    /**
     * The objects relevant for a report are stored in a map.
     * The keys to the map are all of type string!
     * These objects are used for example by the script runner as variables.
     * 
     * @return the map of business objects or an empty map.
     */
    public Map getBusinessObjects();
}
