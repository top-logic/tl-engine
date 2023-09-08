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
 * Calculates the statistical median of a collection of {@link Number}s.
 * <p>
 * The <code>median</code> is the middle value in a list of sorted numbers. If the list has an odd
 * number of values, the <code>median</code> is the value at the middle position (size + 1) / 2. If it
 * has an even number of values the <code>median</code> is the average of the two middle values.
 * </p>
 * <b>Example:</b>
 * <ul>
 * <li>List: [13, 13, 13, 13, 14, 14, 16, 18, 21] --> size: 9 --> <code>median</code> = (9 + 1) / 2 =
 * 5 --> 5th element is 14 </li>
 * <li> List: [8, 9, 10, 10, 10, 11, 11, 11, 12, 13] --> size: 10 --> <code>median</code> = (10 + 1) /
 * 2 = 5.5 --> the average of the 5th and 6th element is (10 + 11) / 2 = 10.5</li>
 * </ul>
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@SupportsType(value = {LegacyTypeCodes.TYPE_FLOAT, LegacyTypeCodes.TYPE_LONG})
@NeedsAttribute(value=true)
@Deprecated
public class MedianFunction extends AbstractAggregationFunction {
	private static final String	ID	= AggregationFunctionFactory.AGGREGATION_MEDIAN_FUNCTION;

	/**
	 * Creates a {@link MedianFunction}.
	 */
	public MedianFunction(InstantiationContext aContext, AggregationFunctionConfiguration aConfig)
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
		if(size == 0) {
		    return Double.valueOf(0.0);
		}
		if (size % 2 == 1) {
			int pos = ((size + 1) / 2) - 1;
			Object theElement = this.getAttribute(theObjects.get(pos));
			if (theElement instanceof Number) {
				return (Number) theElement;
			}
			else {
				return Double.valueOf(0.0);
			}
		}
		else {
			int posA = (size / 2) - 1;
			int posB = size / 2;
			Object theFirstElement = this.getAttribute(theObjects.get(posA));
			Object theSecondElement = this.getAttribute(theObjects.get(posB));
			if (theFirstElement instanceof Number && theSecondElement instanceof Number) {
				double first = ((Number) theFirstElement).doubleValue();
				double second = ((Number) theSecondElement).doubleValue();
				return Double.valueOf((first + second) / 2);
			}
			else {
				return Double.valueOf(0.0);
			}
		}
		
	}

	@Override
	public String getLabel() {
		return ID;
	}
}
