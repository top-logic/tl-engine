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
 * The sun function sums up number values from a collection.
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@SupportsType(value = {LegacyTypeCodes.TYPE_FLOAT, LegacyTypeCodes.TYPE_LONG})
@NeedsAttribute(value=true)
@Deprecated
public class SumFunction extends AbstractAggregationFunction {
	private static final String	ID	= AggregationFunctionFactory.AGGREGATION_SUM_FUNCTION;

	/**
	 * Creates a {@link SumFunction}.
	 */
	public SumFunction(InstantiationContext aContext, AggregationFunctionConfiguration aConfig)
			throws ConfigurationException {
        super(aContext, aConfig);
	}

	@Override
	protected Number calculateResult(Collection someObjects) {

	    double theDivisor = this.getDivisor();
		if (someObjects.isEmpty() && this.ignoreNullValues()) {
			return Double.valueOf(0.0);// return null;
		}

		double theResult = 0;
		Iterator theIt = someObjects.iterator();
		while (theIt.hasNext()) {
			Object theElement = this.getAttribute(theIt.next());
			if (theElement instanceof Number) {
				theResult += ((Number) theElement).doubleValue();
			}
			else if (theElement != null && !(theElement instanceof Number)) {
				return Double.valueOf(Double.NaN);
			}
		}

		return Double.valueOf(theResult / theDivisor);
	}

	@Override
	public String getLabel() {
		return ID;
	}
}
