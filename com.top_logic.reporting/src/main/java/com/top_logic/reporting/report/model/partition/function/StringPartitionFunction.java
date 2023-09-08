/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.partition.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.reporting.report.model.aggregation.SupportsType;
import com.top_logic.reporting.report.model.partition.PartitionFunctionConfiguration;
import com.top_logic.reporting.report.model.partition.criteria.Criteria;

/**
 * The StringPartitionFunction creates partitions from String values.
 *
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@SupportsType(value = {LegacyTypeCodes.TYPE_STRING, LegacyTypeCodes.TYPE_STRING_SET})
@Deprecated
public class StringPartitionFunction extends AbstractPartitionFunction {

	/**
	 * Creates a {@link StringPartitionFunction}.
	 */
	public StringPartitionFunction(InstantiationContext aContext, PartitionFunctionConfiguration aConfig) {
        super(aContext, aConfig);
	}

	/**
	 * Creates a {@link StringPartitionFunction}.
	 *
	 * @param aLanguage
	 *            A language (e.g. 'de' or 'en').
	 */
	public StringPartitionFunction(String anAttributeName, String aLanguage, boolean ignoreNullValues,
			boolean ignoreEmptyPartitions, List someFilters) {
		super(anAttributeName, aLanguage, ignoreNullValues, ignoreEmptyPartitions);
		this.setPartitionCriterias(new ArrayList());
	    this.setPartitionFilters(someFilters);
	}

	@Override
	public List processObjects(Collection someObjects) {
		List theResult = new ArrayList();
		return theResult;
	}

	/**
	 * @see com.top_logic.reporting.report.model.partition.function.PartitionFunction#getType()
	 */
	@Override
	public String getType() {
		return PartitionFunctionFactory.STRING;
	}

	/**
	 * @see com.top_logic.reporting.report.model.partition.function.PartitionFunction#getCriteria()
	 */
	@Override
	public Criteria getCriteria() {
		return null;
	}
}
