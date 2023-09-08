/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.aggregation;

import java.util.Collection;

import com.top_logic.element.meta.query.CollectionFilter;
import com.top_logic.layout.Accessor;
import com.top_logic.reporting.report.model.ItemVO;

/**
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public interface AggregationFunction extends CollectionFilter {
	
    /**
     * @deprecated use {@link AggregationFunctionConfiguration}
     */
    @Deprecated
	public  static final String IGNORE_NULL             = "ignoreNullValues";
    
    public ItemVO getValueObjectFor(Collection someObjects);

	/**
	 * @deprecated use {@link AggregationFunctionConfiguration}
	 */
	@Deprecated
	public void setIgnoreNullValues(boolean ignoreNullValues);

	/**
     * @deprecated use {@link AggregationFunctionConfiguration}
     */
	@Deprecated
	public void setAttributeAccessor(Accessor anAccessor);

	@Deprecated
	public String getLabel();
	@Deprecated
	public String getLocalizedLabel();

	/**
     * @deprecated use {@link AggregationFunctionConfiguration}
     */
	@Deprecated
	public String getAttributePath();
}
