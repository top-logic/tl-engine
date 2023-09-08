/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.aggregation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.NumberComparator;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.LegacyTypeCodes;

/**
 * The max function returns the maximum number value of a collection.
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@SupportsType(value = {LegacyTypeCodes.TYPE_FLOAT, LegacyTypeCodes.TYPE_LONG})
@NeedsAttribute(value=true)
@Deprecated
public class MaxFunction extends AbstractAggregationFunction {
	private static final String	ID	= AggregationFunctionFactory.AGGREGATION_MAX_FUNCTION;

	/**
	 * Creates a {@link MaxFunction}.
	 */
	public MaxFunction(InstantiationContext aContext, AggregationFunctionConfiguration aConfig)
			throws ConfigurationException {
        super(aContext, aConfig);
	}

	@Override
	protected Number calculateResult(Collection someObjects) {

		if (someObjects.isEmpty()) {
			if (this.ignoreNullValues()) {
				return Double.valueOf(0.0);// return null;
			}
			else {
				return Double.valueOf(0.0);// Double.valueOf(Double.NEGATIVE_INFINITY);
			}
		}

		// filter(...) did the work, we must return just one attribute
		Object theElement = this.getAttribute(CollectionUtil.getFirst(someObjects));  
		
		if (theElement != null && ! (theElement instanceof Number)) {
		    return Double.valueOf(Double.NaN);
		}
		else if (theElement == null) {
		    return Double.valueOf(0.0d);
		}
		else {
		    return (Number) theElement;
		}
	}

	@Override
	public String getLabel() {
		return ID;
	}
	
	@Override
	public Collection filter(Collection aCollection) throws NoSuchAttributeException,
	        AttributeException {
	    
	    
	       if (CollectionUtil.isEmptyOrNull(aCollection)) {
	           return Collections.EMPTY_LIST;
	       }
	       
	       Set    theFiltered = new HashSet();
	       Iterator theIter   = aCollection.iterator();
	       Object theObject   = theIter.next();
	       Object theElement  = this.getAttribute(theObject);
	       
	       Number theMax      = Double.valueOf(Double.NEGATIVE_INFINITY);
	    
	       if (theElement instanceof Number) {
	           theFiltered.add(theObject);
	           theMax = (Number) theElement;
	       }
	       else if (theElement != null) {
	           return Collections.EMPTY_SET;
	       }
	       
	       while (theIter.hasNext()) {
	           theObject  = theIter.next();
	           theElement = this.getAttribute(theObject);
	           if (theElement instanceof Number) {
	               int c = NumberComparator.INSTANCE.compare((Number) theElement, theMax);
	               if (c > 0) {
	                   theMax      = (Number) theElement;
	                   theFiltered = new HashSet();
	                   theFiltered.add(theObject);
	               }
	               else if (c == 0) {
	                   theFiltered.add(theObject);
	               }
	           }
	           else if (theElement != null) {
	               return Collections.EMPTY_SET;
	           }
	       }
	    
	       return theFiltered;
	}
}
