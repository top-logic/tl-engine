/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model;

import java.text.NumberFormat;

import com.top_logic.basic.StringServices;
import com.top_logic.reporting.report.model.aggregation.AggregationFunction;
import com.top_logic.reporting.report.model.partition.Partition;

/**
 * The ItemVO represents one item in a {@link Report}.
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class ItemVO {

	/** See {@link #getValue()}. */
	private Number value;
	/** See {@link #getSuffix()}. */
	private String suffix;

	private String prefix;
	/** See {@link #getFormat()}. */
	private NumberFormat format;
	/** See {@link #getTooltip()}. */
	private String tooltip;
	/** See {@link #getUrl()}. */
	private String url;

	private AggregationFunction aggregationFunction;
	
	/**
	 * The {@link Partition} this ItemVO belongs to. It can be used to request the criteria
	 * used to partition the set of <code>BusinessObjects</code>.
	 */
	private Partition firstPartition;
//	private Partition secondPartition;

	/**
	 * See {@link #ItemVO(Number, String, String, NumberFormat, String, String)}.
	 */
	public ItemVO(Number aValue, String aPrefix, String aSuffix) {
		this(aValue, aPrefix, aSuffix, null);
	}

	/**
	 * See {@link #ItemVO(Number, String, String, NumberFormat, String, String)}.
	 */
	public ItemVO(Number aValue, String aPrefix, String aSuffix, NumberFormat aFormat) {
		this(aValue, aPrefix, aSuffix, aFormat, /* Tooltip */"", /* Url */"");
	}

	/**
	 * Creates a {@link ItemVO}.
	 * 
	 * @param aValue
	 *            See {@link #getValue()}.
	 * @param aSuffix
	 *            See {@link #getSuffix()}.
	 * @param aFormat
	 *            See {@link #getFormat()}.
	 * @param aTooltip
	 *            See {@link #getTooltip()}.
	 * @param aUrl
	 *            See {@link #getUrl()}.
	 */
	public ItemVO(Number aValue, String aPrefix, String aSuffix, NumberFormat aFormat,
			String aTooltip, String aUrl) {
		this.value = aValue;
		this.prefix = aPrefix;
		this.suffix = aSuffix;
		this.format = aFormat;
		this.tooltip = aTooltip;
		this.url = aUrl;
	}

	/**
	 * This method returns the item suffix. The suffix is added after the value (e.g. 5.0
	 * EUR - suffix = EUR). The suffix must never <code>null</code>. Maybe an empty
	 * string.
	 */
	public String getSuffix() {
		return this.suffix;
	}

	public String getPrefix() {
		return this.prefix;
	}

	/**
	 * This method returns the tooltip of this item. Maybe <code>null</code> or empty.
	 */
	public String getTooltip() {
		return this.tooltip;
	}

	/**
	 * This method returns the url or url fragment (this depends on the tag fragment
	 * generator). Maybe <code>null</code> or empty.
	 */
	public String getUrl() {
		return this.url;
	}

	/**
	 * This method the value of this item.
	 */
	public Number getValue() {
		return this.value;
	}

	/**
	 * This method returns the value as string. If a format is set the format is used to
	 * formate the value and if a suffix is set the suffix is added after the value (e.g.
	 * 5.000 EUR).
	 */
	public String getValueAsString() {
        String theValueAsString = "";
        
        if (!StringServices.isEmpty(getPrefix())) {
            theValueAsString = getPrefix();
        }
        
        if (getValue() == null) {
            theValueAsString = "n.a.";
        } else {
            if (getFormat() != null) {
                theValueAsString += getFormat().format(getValue());
            } else {
                theValueAsString += String.valueOf(getValue());
            }
        }
        
        if (!StringServices.isEmpty(getSuffix())) {
            theValueAsString += getSuffix();
        }
        
        return theValueAsString;
    }

	/**
	 * This method returns the format for the value. The format is used to format the value.
	 */
	public NumberFormat getFormat() {
		return this.format;
	}

	/**
	 * Returns the {@link Partition} this ItemVO belongs to.
	 * 
	 * @return a {@link Partition}
	 */
	public Partition getFirstPartition() {
		return this.firstPartition;
	}

	/**
	 * This method sets the partition.
	 *
	 * @param    aPartition    The partition to set.
	 */
	public void setFirstPartition( Partition aPartition ) {
		this.firstPartition = aPartition;
	}

//	public Partition getSecondPartition() {
//    	return (secondPartition);
//    }
//
//	public void setSecondPartition(Partition secondPartition) {
//    	this.secondPartition = secondPartition;
//    }

	public void setAggregationFunction(AggregationFunction aFunction) {
	    this.aggregationFunction = aFunction;
	}

	public AggregationFunction getAggregationFunction() {
	    return this.aggregationFunction;
	}
}
