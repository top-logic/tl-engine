/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.dataset;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

import com.top_logic.reporting.flex.chart.config.model.ChartTree;

/**
 * A {@link DatasetBuilder} creates a {@link Dataset} for a given {@link ChartTree}
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public interface DatasetBuilder<D extends Dataset> {

	/**
	 * the type of the {@link Dataset} created by this
	 */
	public Class<D> getDatasetType();

	/**
	 * @param tree
	 *        the {@link ChartTree} to create the {@link Dataset} for.
	 * @return the {@link Dataset} to be used in a {@link JFreeChart}.
	 */
	public D getDataset(ChartTree tree);

}
