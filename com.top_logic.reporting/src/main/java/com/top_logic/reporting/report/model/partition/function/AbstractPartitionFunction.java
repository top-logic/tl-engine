/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.partition.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.Accessor;
import com.top_logic.reporting.report.model.aggregation.SupportsType;
import com.top_logic.reporting.report.model.partition.DefaultPartition;
import com.top_logic.reporting.report.model.partition.Partition;
import com.top_logic.reporting.report.model.partition.PartitionFunctionConfiguration;
import com.top_logic.reporting.report.model.partition.criteria.Criteria;
import com.top_logic.reporting.report.util.ReportConstants;
import com.top_logic.util.TLContext;

/**
 * This class provides basic fields and methods for all {@link PartitionFunction}s.
 *
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@SupportsType(value = {})
@Deprecated
public abstract class AbstractPartitionFunction implements PartitionFunction, ReportConstants {

	protected static final String ATTRIBUTE   = "attribute";
	
	private Map				addtionalSettings;

	protected String			language;
	
	/**
	 * The Filter is used to sort objects into specific partitions.
	 */
	private   List				partitionFilters;
	private   List				partitionCriterias;

	private PartitionFunctionConfiguration config;

	private PartitionFunction partitionFunction;
	
	/**
	 * If this constructor is used a call to init is mandatory!
	 */
	public AbstractPartitionFunction(InstantiationContext aContext, PartitionFunctionConfiguration aConfig) {
	    this.update(aConfig);
	}
	
	protected PartitionFunctionConfiguration getConfiguration() {
	    return this.config;
	}
	
	private final void update(PartitionFunctionConfiguration aSomeOptions) {
	    this.config   = aSomeOptions;
	    Locale theLoc = TLContext.getContext().getCurrentLocale();
	    if (theLoc != null) {
	        this.language = theLoc.getLanguage();
	    }
	    
	    // TODO TBE/FSC: whats the intention behind this code? Why is the partitionfunction declared by an inner configuration?
	    PartitionFunctionConfiguration theInnerPartitionConfig = this.config.getPartitionConfiguration();
        
        if (theInnerPartitionConfig != null) {
            PartitionFunctionFactory       theFac  = PartitionFunctionFactory.getInstance();
            PartitionFunction              theFunc = theFac.getPartitionFunction(theInnerPartitionConfig);

            this.partitionFunction = theFunc;
        }
	}

	@Deprecated
	protected AbstractPartitionFunction( String anAttributeName, String aLanguage,
			boolean ignoreNullValues, boolean ignoreEmptyPartitions ) {
		this.language = aLanguage;
	}

	/**
	 * This method creates a list of partitions.
	 *
	 * @return list of partitions, never <code>null</code>
	 *
	 * @see com.top_logic.reporting.report.model.partition.function.PartitionFunction#processObjects(java.util.Collection)
	 */
	@Override
	public abstract List<Partition> processObjects( Collection<?> someObjects );

	/**
	 * Returns the attribute value of an object obtained through the accessor.
	 *
	 * @return the value of the attribute, may be <code>null</code>
	 */
	protected final Object getAttribute(Object anObject) {
		return this.getConfiguration().getAccessor().getValue(anObject, this.getConfiguration().getAttribute());
	}

	/**
	 * @see #partitionFilters
	 */
	protected final void setPartitionFilters( List someFilters ) {
		this.partitionFilters = someFilters;
	}

	/**
	 * @see #partitionFilters
	 */
	protected final List getPartitionFilters() {
		return this.partitionFilters;
	}

	@Override
	public final void setAttributeAccessor( Accessor anAccessor ) {
		if (anAccessor != null) {
		    this.getConfiguration().setAccessor(anAccessor);
		}
	}

	protected final boolean ignoreNullValues() {
	    return this.getConfiguration().isIgnoreNullValues();
	}
	
	@Override
	public final boolean ignoreEmptyPartitions() {
	    return this.getConfiguration().isIgnoreEmptyPartitions();
	}
	
	protected final List getPartitionCriterias() {
        return (this.partitionCriterias);
    }
	
	protected final void setPartitionCriterias(List aPartitionCriterias) {
        this.partitionCriterias = aPartitionCriterias;
    }

	/**
	 * This method returns the addtionalSettings.
	 *
	 * @return Returns the addtionalSettings.
	 */
	protected final Map getAddtionalSettings() {
		// IGNORE FindBugs(UWF_UNWRITTEN_FIELD): Deprecated class. Is removed when class removed.
		return ( this.addtionalSettings );
	}

	/**
	 * Uses the Filter that defines this PartitionFunction and partitions the Collection of Objects.
	 *
	 * @param someObjects
	 *            the Objects to "sort" into partitions
	 * @return a {@link List} of created Partitions each containing some BusinessObjects (an
	 *         <code>empty</code> List is possible. The {@link List} itself is never <code>null</code>.
	 */
	protected List createPartitions( Collection someObjects, boolean multiple ) {
       // first, create an empty list to store created partitions in.
        // If there are no filters, we can simply return the empty list.
        final int partCount = partitionFilters.size();
        final List thePartitions = new ArrayList(partCount);
        if(partCount < 1) {
            return thePartitions;
        }
        
        // now initialize the partition list using the receiver's partition
        // criteria
        for(int i = 0; i < partCount; i++) {
            final Criteria theCriteria = (Criteria)partitionCriterias.get(i);
            final String thePFName = theCriteria.toString();
            thePartitions.add(new DefaultPartition(thePFName, this.language, theCriteria));
        }
        
        // time to go through all the objects to filter
        final Iterator theIterator = someObjects.iterator();
        while(theIterator.hasNext()) {
            final Object theObject = theIterator.next();
            final Object theValue = getAttribute(theObject);
            
            // invalid value, we can't apply any filters here...
            if(theObject == null || theValue == null) {
                continue;
            }
            
            // we iterate over the entire list of filters and assign the objects
            // to partitions which accept them.
            // NOTE: since the filter and criteria lists have exactly the same
            //       order, we can safely use the filter list order here, even
            //       though we used the criteria list to initialize partitions.
            for(int i = 0; i < partCount; i++) {
                final Filter theFilter = (Filter)partitionFilters.get(i);
                if(theFilter.accept(theValue)) {
                    ((Partition)thePartitions.get(i)).add(theObject);
                }
            }
        }
        return thePartitions;
    }
	
	/** 
	 * Returns the inner {@link PartitionFunction}.
	 * 
	 * @return a {@link PartitionFunction}, can be <code>null</code>.
	 */
	public PartitionFunction getSubPartitionFunction() {
		return this.partitionFunction;
	}
}
