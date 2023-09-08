/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.partition.function;

import java.util.Date;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.reporting.report.model.partition.function.DatePartitionFunction.DatePartitionConfiguration;


/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public abstract class IntervalPartitionFunction extends AbstractPartitionFunction {
	
	protected IntervalPartitionFunction(InstantiationContext aContext, DatePartitionConfiguration aConfig) {
        super(aContext, aConfig);
    }
	
	protected IntervalPartitionFunction(String anAttributeName, String aLanguage, boolean ignoreNullValues, boolean ignoreEmptyCategories) {
		super(anAttributeName, aLanguage, ignoreNullValues, ignoreEmptyCategories);
    }

	/**
	 * @deprecated obsolete with the use of ConfigItem
	 */
	@Deprecated
	public final Date getBegin() {
	    return ((DatePartitionConfiguration)this.getConfiguration()).getIntervalStart();
	}
	
	/**
     * @deprecated obsolete with the use of ConfigItem
     */
	@Deprecated
	public final Date getEnd() {
	    return ((DatePartitionConfiguration)this.getConfiguration()).getIntervalEnd();
	}
	
	/**
     * @deprecated obsolete with the use of ConfigItem
     */
	@Deprecated
	public final Long getGranularity() {
	    return ((DatePartitionConfiguration)this.getConfiguration()).getSubIntervalLength();
	}
}
