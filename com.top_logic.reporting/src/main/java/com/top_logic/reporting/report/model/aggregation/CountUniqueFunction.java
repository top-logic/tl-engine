/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.aggregation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;

/**
 * This function counts the number of different items in a collection. The difference refers only to the
 * given attribute.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@SupportsType(value = {})
@NeedsAttribute(value=true)
@Deprecated
public class CountUniqueFunction extends AbstractAggregationFunction {
	private static final String	ID	= AggregationFunctionFactory.AGGREGATION_COUNT_U_FUNCTION;
	private Set uniqueItems;

	/**
	 * Creates a {@link CountUniqueFunction}.
	 */
	public CountUniqueFunction(InstantiationContext aContext, AggregationFunctionConfiguration aConfig)
			throws ConfigurationException {
        super(aContext, aConfig);
	}

	@Override
	protected Number calculateResult(Collection someObjects) {
		this.uniqueItems = new HashSet();
		if (someObjects.isEmpty() && this.ignoreNullValues()) {
			return Double.valueOf(0.0);// return null;
		}

//		if (!this.ignoreNullValues()) {
//			return Integer.valueOf(someObjects.size());
//		}
//		else {
			int theCount = 0;
			for (Iterator theIter = someObjects.iterator(); theIter.hasNext();) {
				Object theAttribute = this.getAttribute(theIter.next());
				if (theAttribute != null) {
					boolean added = this.uniqueItems.add(theAttribute);
					if (added) {
						theCount++;
					}
				}
			}
			return Integer.valueOf(theCount);
//		}
	}

	@Override
	public String getLabel() {
		return ID;
	}

}
