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
import com.top_logic.reporting.report.model.partition.function.PartitionFunction;

/**
 * A DataSeries represents a {@link List} of {@link ItemVO}s. It can be used by a
 * renderer to produce a new Report.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public class DataSeries {
	/**
	 * A List of {@link ItemVO}s.
	 * 
	 * @see ItemVO
	 */
	private List values;

	/**
	 * The label used for this DataSeries. It is normally the label of the
	 * {@link AggregationFunction} used to create this DataSerie.
	 */
	private String label;

	public DataSeries(String aLabel) {
		this.label = aLabel;
		this.values = new ArrayList();
	}
	/**
	 * Creates a {@link DataSeries}.
	 * 
	 * @param aLabel
	 *            the label for this DataSeries.
	 * @Param someItemVOs
	 *            a {@link List} of {@link ItemVO}s filterd by a {@link PartitionFunction}
	 *            and evaluated by a {@link AggregationFunction}.
	 */
	public DataSeries(String aLabel, List someItemVOs) {
		this(aLabel);
		this.values = someItemVOs;
	}
	
	/**
	 * Returns the label of this DataSeries.
	 * 
	 * @return the name of this DataSeries
	 */
	public String getLabel() {
		return this.label;
	}

	public void addItemVO(ItemVO anItem) {
		this.values.add(anItem);
	}
	/**
	 * Returns the {@link List} of {@link ItemVO}s this DataSeries consists of.
	 * 
	 * @return an unmodifiable {@link List} of {@link ItemVO}
	 */
	public List getItemVOs() {
		return Collections.unmodifiableList(this.values);
	}

	/**
	 * Returns the ItemVO with the given index.
	 * 
	 * @param anIndex
	 *            the index of the ItemVO
	 * @return an ItemVO
	 */
	public ItemVO getItemVOAt(int anIndex) {
		return (ItemVO) this.values.get(anIndex);
	}

}
