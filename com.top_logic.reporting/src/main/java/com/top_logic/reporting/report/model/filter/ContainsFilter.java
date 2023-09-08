/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.filter;

import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.filter.SetFilter;
import com.top_logic.reporting.report.importer.AbstractXMLImporter;

/**
 * Extend {@link SetFilter} with  AbstractXMLImporter specific features.
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@Deprecated
public class ContainsFilter extends SetFilter {

	public ContainsFilter(String someValues) {
	    super(computeAcceptedValues(someValues));
	}
	
	static Set computeAcceptedValues(String someValues) {
	    String[] theStrings = StringServices.toArray(someValues, ',');
	    HashSet acceptedValues = new HashSet(theStrings.length);
        for (int i = 0; i < theStrings.length; i++) {
            acceptedValues.add(AbstractXMLImporter.extractObject(theStrings[i]));
        }
        return acceptedValues;
	}

}

