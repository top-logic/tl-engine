/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.reporting.report.model.aggregation.AggregationFunction;
import com.top_logic.reporting.report.model.partition.Partition;

/**
 * A {@link DataSet} is the result of a report. It contains a {@link List} of {@link DataSeries}
 * each representing the result of an evaluation of a {@link AggregationFunction} on {@link List} of
 * {@link Partition}s.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public class DataSet {
	/**
	 * The {@link List} of {@link DataSeries} this {@link DataSet} is composed of.
	 */
	private List	dataSeries;

	/**
	 * A name.
	 */
	private String	label;

	public DataSet() {
		this.dataSeries = new ArrayList();
	}

	/**
	 * Creates a {@link DataSet}.
	 * 
	 */
	public DataSet(String aName, List someDataSeries) {
		this.dataSeries = someDataSeries;
		this.label = aName;
	}

	/**
	 * Adds a new {@link DataSeries} to this {@link DataSet}.
	 */
	public void addDataSeries(DataSeries aDataSerie) {
		this.dataSeries.add(aDataSerie);
	}

	/**
	 * Removes all {@link DataSeries} from this {@link DataSet}.
	 * 
	 */
	public void resetDataSet() {
		this.dataSeries.clear();
	}

	/**
	 * This method returns the data series.
	 * 
	 * @return Returns the (unmodifiable) data series.
	 */
	public List getDataSeries() {
		return (Collections.unmodifiableList(this.dataSeries));
	}

	/** The item of the given series in the given category. */
	public ItemVO getItemVOAt(int aSeries, int aCategory) {
	    int size = this.dataSeries.size();
	    if (aCategory > size) {
	        throw new IllegalArgumentException("This set contains only " +size+ " categories");
	    }
	    
	    DataSeries theSeries = (DataSeries) this.dataSeries.get(aCategory);
	    return theSeries.getItemVOAt(aSeries);
	}
	
	/**
	 * This method returns the label.
	 * 
	 * @return Returns the label.
	 */
	public String getLabel() {
		return (this.label);
	}

	/** Setter for the label of this data set. */
	public void setLabel(String aName) {
		this.label = aName;
	}

}
