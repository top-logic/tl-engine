/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.aggregation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.LegacyTypeCodes;

/**
 * The UpperQuartil is the middle of the upper half of a sorted list of numbers. It is used to calculate
 * the values for a box-and-whisker chart.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@SupportsType(value = {LegacyTypeCodes.TYPE_FLOAT, LegacyTypeCodes.TYPE_LONG})
@NeedsAttribute(value=true)
@Deprecated
public class UpperQuartil extends MedianFunction {
	private static final String	ID	= AggregationFunctionFactory.AGGREGATION_UPPER_FUNCTION;

	/**
	 * Creates a {@link UpperQuartil}.
	 */
	public UpperQuartil(InstantiationContext aContext, AggregationFunctionConfiguration aConfig)
			throws ConfigurationException {
        super(aContext, aConfig);
	}

	@Override
	protected Number calculateResult(Collection someObjects) {
		if (someObjects.isEmpty() && this.ignoreNullValues()) {
			return Double.valueOf(0.0);// return null;
		}
		List theObjects = new ArrayList();
		theObjects.addAll(someObjects);
		Collections.sort(theObjects);
		int size = theObjects.size();
		int index;
		if (size % 2 == 1) {
			index = ((size + 1) / 2);
		}
		else {
			index = size / 2;
		}
		theObjects = theObjects.subList(index, theObjects.size());
		return super.calculateResult(theObjects);
	}

	@Override
	public String getLabel() {
		return ID;
	}
}
