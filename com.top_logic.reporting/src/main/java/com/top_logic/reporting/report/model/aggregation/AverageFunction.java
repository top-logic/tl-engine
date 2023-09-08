/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.aggregation;

import java.util.Collection;
import java.util.Iterator;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.LegacyTypeCodes;

/**
 * Calculates the average number value of some attributes.
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@SupportsType(value = {LegacyTypeCodes.TYPE_FLOAT, LegacyTypeCodes.TYPE_LONG})
@NeedsAttribute(value=true)
@Deprecated
public class AverageFunction extends AbstractAggregationFunction {
	private static final String	ID	= AggregationFunctionFactory.AGGREGATION_AVG_FUNCTION;

	/**
	 * Creates a {@link AverageFunction}.
	 */
	public AverageFunction(InstantiationContext aContext, AggregationFunctionConfiguration aConfig)
			throws ConfigurationException {
	    super(aContext, aConfig);
	}

	@Override
	protected Number calculateResult(Collection someObjects) {

		// empty sets don't have an average value
		if (someObjects.isEmpty() && this.ignoreNullValues()) {
			return Double.valueOf(0.0);// return null;
		}

		double theResult = 0;
		long theCountNotNull = 0;
		Iterator theIt = someObjects.iterator();
		while (theIt.hasNext()) {

			Object theElement = this.getAttribute(theIt.next());

			if (theElement instanceof Number) {
				theResult += ((Number) theElement).doubleValue();
				theCountNotNull++;
			}
			else if (theElement != null && !(theElement instanceof Number)) {
				return Double.valueOf(Double.NaN);
			}
		}

		Double theRetVal;
		if (this.ignoreNullValues()) {
		    theRetVal = (theCountNotNull != 0 ? Double.valueOf(theResult / theCountNotNull) : Double.valueOf(0));
//			return Double.valueOf(theResult / theCountNotNull);
		}
		else {
		    theRetVal = (someObjects.size() != 0 ? Double.valueOf(theResult / someObjects.size()) : Double.valueOf(0));
//			return Double.valueOf(theResult / someObjects.size());
		}
		return theRetVal;
	}

	@Override
	public String getLabel() {
		return ID;
	}
}
