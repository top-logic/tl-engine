/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.aggregation;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.layout.Accessor;
import com.top_logic.reporting.layout.flexreporting.component.ConfigurationFormFieldHelper.ConfigurationDescriptorResourceProvider;
import com.top_logic.reporting.report.model.ItemVO;
import com.top_logic.reporting.report.util.ReportConstants;
import com.top_logic.util.error.TopLogicException;

/**
 * Any range function operates on a given set of objects and calculates an {@link ItemVO} for this set.
 * Therefor it accesses the objects through a configured accessor
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@Deprecated
public abstract class AbstractAggregationFunction implements AggregationFunction, ReportConstants {

	// private boolean checked = false;

	private String			suffix	= "";
	private String			prefix	= "";
	
	private final AggregationFunctionConfiguration config;

	public AbstractAggregationFunction(InstantiationContext aContext, AggregationFunctionConfiguration aConfig)
			throws ConfigurationException {
        this.config = aConfig;
		checkAttributePath();
    }

	private void checkAttributePath() throws ConfigurationException {
		Class<?> runtimeClass = getClass();
		while (true) {
			NeedsAttribute needsAttributePath = runtimeClass.getAnnotation(NeedsAttribute.class);
			if (needsAttributePath != null) {
				if (needsAttributePath.value()) {
					if (StringServices.isEmpty(config.getAttributePath())) {
						throw new ConfigurationException("Configuration '"
							+ AggregationFunctionConfiguration.ATTRIBUTE_PATH_NAME + "' must be set.");
					}
				}
				break;
			}

			// check whether super class defines NeedsAttribute
			runtimeClass = runtimeClass.getSuperclass();
			if (!AbstractAggregationFunction.class.isAssignableFrom(runtimeClass)) {
				break;
			}
				
		}
	}

	@Override
	public ItemVO getValueObjectFor(Collection someObjects) {
	    try {
	        Collection theObjects = this.filter(someObjects);
	        return new ItemVO(calculateResult(theObjects), this.prefix, this.suffix, null);
	    } catch (NoSuchAttributeException nsax) {
	        Logger.error("Cannot calculate result", nsax, AbstractAggregationFunction.class);
	        throw new TopLogicException(AbstractAggregationFunction.class, "");
	    }
	}

	protected AggregationFunctionConfiguration getConfiguraion() {
        return (this.config);
    }
	
	protected final Object getAttribute(Object anObject) {
		return this.config.getAccessor().getValue(anObject, this.getAttributePath());
	}

	public final boolean ignoreNullValues() {
		return this.getConfiguraion().isIgnoreNullValues();
	}

	@Override
	public final String getAttributePath() {
		return this.getConfiguraion().getAttributePath();
	}

	@Override
	public final void setIgnoreNullValues(boolean ignoreNullvalues) {
		this.getConfiguraion().setIgnoreNullValues(ignoreNullvalues);
	}
	
	@Override
	public void setAttributeAccessor(Accessor anAccessor) {
	    this.getConfiguraion().setAccessor(anAccessor);
	}

	protected final double getDivisor() {
	    return this.getConfiguraion().getDivisor();
	}
	
	protected abstract Number calculateResult(Collection someObjects);

	@Override
	public Collection filter(Collection aCollection) throws NoSuchAttributeException,
	        AttributeException {
	    return aCollection;
	}
	
	@Override
	public final Map<String, Object> getValueMap() {
	    return Collections.EMPTY_MAP;
	}
	
	@Override
	public final int compareTo(Object aO) {
	    return 0;
	}
	
	@Override
	public final void setUpFromValueMap(Map aValueMap) throws IllegalArgumentException {
	}
	
	/**
	 * @deprecated The label for an {@link AggregationFunction} is provided via an
	 *             {@link AggregationFunctionLabelProvider}.
	 */
	@Override
	@Deprecated
	public String getLocalizedLabel() {
		return new ConfigurationDescriptorResourceProvider().getLabel(this.getClass());
	}
	
}
