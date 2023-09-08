/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.aggregation;

import java.util.Collection;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;

/**
 * The count function just counts the size of a collection.
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@SupportsType(value = {})
@NeedsAttribute(value=false)
@Deprecated
public class CountFunction extends AbstractAggregationFunction {
	private static final String	ID	= AggregationFunctionFactory.AGGREGATION_COUNT_FUNCTION;

	/**
	 * Creates a {@link CountFunction}.
	 */
	public CountFunction(InstantiationContext aContext, AggregationFunctionConfiguration aConfig)
			throws ConfigurationException {
        super(aContext, aConfig);
	}

	@Override
	protected Number calculateResult(Collection someObjects) {

		if (someObjects.isEmpty() && this.ignoreNullValues()) {
			return 0d;// return null;
		}

		if (StringServices.isEmpty(getAttributePath()) || ! this.ignoreNullValues()) {
			return someObjects.size();
		}
		else {
			int theCount = 0;
			for (Object object : someObjects) {
                if (this.getAttribute(object) != null) {
                    theCount++;
                }
            }
			
			return theCount;
		}
	}

	@Override
	public String getLabel() {
		return ID;
	}
}
