/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.partition.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.NumberComparator;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.reporting.report.model.aggregation.SupportsType;
import com.top_logic.reporting.report.model.filter.Interval;
import com.top_logic.reporting.report.model.filter.NumberIntervalProvider;
import com.top_logic.reporting.report.model.filter.ObjectFilter;
import com.top_logic.reporting.report.model.partition.PartitionFunctionConfiguration;
import com.top_logic.reporting.report.model.partition.criteria.Criteria;
import com.top_logic.reporting.report.model.partition.criteria.interval.NumberIntervalCriteria;


/**
 * The NumberPartitionFunction creates categories from Number values.
 *
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@SupportsType(value = {LegacyTypeCodes.TYPE_FLOAT, LegacyTypeCodes.TYPE_LONG})
@Deprecated
public class NumberPartitionFunction extends AbstractPartitionFunction {

//	private static final String	CRITERIA		= "numberIntervalCriteria";
//	private static final String	BEGIN			= "begin";
//	private static final String	END				= "end";
//	private static final String	GRANULARITY		= "numberGranularity";
//	private static final String	AUTOMATIC		= "automatic";

	/**
	 * The {@link IntervalProvider} creates the {@link Partition}s for this PartitionFunction.
	 */

	/**
	 * Creates a {@link NumberPartitionFunction}.
	 */
	public NumberPartitionFunction(InstantiationContext aContext, NumberPartitionConfiguration aConfig) {
        super(aContext, aConfig);
	    this.setPartitionCriterias(new ArrayList());
        this.setPartitionFilters(new ArrayList());
	}

	public NumberPartitionFunction( String anAttributeName, String aLanguage, boolean ignoreNullValues, boolean ignoreEmptyPartitions, List someFilters ) {
		super(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, TypedConfiguration
			.newConfigItem(NumberPartitionConfiguration.class));
	    
	    this.language = aLanguage;
	    
	    this.getNumberPartitionConfiguration().setIgnoreNullValues(ignoreNullValues);
	    this.getNumberPartitionConfiguration().setIgnoreEmptyPartitions(ignoreEmptyPartitions);
	    
        this.setPartitionCriterias(new ArrayList());
        this.setPartitionFilters(someFilters);
    }
	
	protected NumberPartitionConfiguration getNumberPartitionConfiguration() {
	    return (NumberPartitionConfiguration) super.getConfiguration();
	}
	
	/**
	 * @see com.top_logic.reporting.report.model.partition.function.AbstractPartitionFunction#processObjects(java.util.Collection)
	 */
	@Override
	public List processObjects( Collection someObjects ) {
		
	    Number theStart = null;
	    Number theEnd   = null;
	    
	    NumberPartitionConfiguration theConfig = this.getNumberPartitionConfiguration();
	    Double theGranu = theConfig.getSubIntervalLength();
	    if (theConfig.shouldUseAutomaticRange()) {
			Iterator theIter = someObjects.iterator();
			while(theIter.hasNext()) {
			    // find minium and maximum
				Number val = (Number) this.getAttribute(theIter.next());
				
				if (val == null ) {
					continue;
				}
				
				if(theEnd == null || NumberComparator.INSTANCE.compare(val, theEnd) > 0){
				    theEnd = val;
				}
				
				if(theStart == null || NumberComparator.INSTANCE.compare(val, theStart) < 0) {
				    theStart = val;
				}
			}
			
			
		}
	    else {
	        theStart = theConfig.getIntervalStart();
	        theEnd   = theConfig.getIntervalEnd();
	    }
	    
	    
	    
	    theStart = (theStart == null) ? Double.valueOf(-((Number)theGranu).doubleValue()/2) : theStart;
	    theEnd   = (theEnd == null)   ? Double.valueOf(((Number)theGranu).doubleValue()/2) : theEnd;
		
		NumberIntervalProvider theProvider = new NumberIntervalProvider(theGranu);
		
		List theIntervals = theProvider.getIntervals(theStart, theEnd);
		for (int i = 0; i < theIntervals.size(); i++) {
			Interval theInter = (Interval) theIntervals.get( i );
			Number theInterBegin = (Number) theInter.getBegin();
			Number theInterEnd   = (Number) theInter.getEnd();
			
			ObjectFilter theFilter = new ObjectFilter(theInterBegin, theInterEnd);
			Criteria theCriteria = new NumberIntervalCriteria( theInterBegin, theInterEnd , theGranu);
			this.getPartitionCriterias().add(theCriteria);
			this.getPartitionFilters().add(theFilter);
		}
		
		return this.createPartitions( someObjects, false );
	}

	/**
	 * @see com.top_logic.reporting.report.model.partition.function.PartitionFunction#getType()
	 */
	@Override
	public String getType() {
		return PartitionFunctionFactory.NUMBER;
	}

	/**
	 * This method returns the start of the interval.
	 *
	 * @return Returns the begin.
	 */
	public Number getBegin() {
		return this.getNumberPartitionConfiguration().getIntervalStart();
	}

	/**
	 * This method returns the end.
	 *
	 * @return Returns the end.
	 */
	public Number getEnd() {
		return this.getNumberPartitionConfiguration().getIntervalEnd();
	}

	/**
	 * This method returns the granularity.
	 *
	 * @return Returns the granularity.
	 */
	public Double getGranularity() {
		return this.getNumberPartitionConfiguration().getSubIntervalLength();
	}

	/**
	 * @see com.top_logic.reporting.report.model.partition.function.PartitionFunction#getCriteria()
	 */
	@Override
	public Criteria getCriteria() {
	    return new NumberIntervalCriteria(this.getBegin(), this.getEnd(), this.getGranularity(), this.getAddtionalSettings());
	}
	
	public interface NumberPartitionConfiguration extends PartitionFunctionConfiguration {
	    
	    @Override
		@ClassDefault(NumberPartitionFunction.class)
	    Class getImplementationClass();

	    String USE_AUTOMATIC_RANGE_NAME = "useAutomaticRange";
	    @Name(USE_AUTOMATIC_RANGE_NAME)
	    boolean shouldUseAutomaticRange();
        void setUseAutomaticRange(boolean useAutomaticRange);
        
        String INTERVAL_START_NAME = "intervalStart";
        @Name(INTERVAL_START_NAME)
        Double getIntervalStart();
        void setIntervalStart(Double aStart);
        
        String INTERVAL_END_NAME = "intervalEnd";
        @Name(INTERVAL_END_NAME)
        Double getIntervalEnd();
        void setIntervalEnd(Double anEnd);
        
        String SUB_INTERVAL_LENGTH_NAME = "subIntervalLength";
        @Name(SUB_INTERVAL_LENGTH_NAME)
        Double getSubIntervalLength();
        void setSubIntervalLength(Double aLength);
	}
}
