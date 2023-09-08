/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.model;

import java.util.Collection;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.reporting.flex.chart.config.ChartConfig;

/**
 * Algorithm for creating a generic {@link ChartTree} model from raw data.
 * 
 * @see #prepare(Collection)
 * @see ChartConfig#getModelPreparation()
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public interface ModelPreparation {

	/**
	 * Base config-interface for {@link ModelPreparation}.
	 */
	public interface Config extends PolymorphicConfiguration<ModelPreparation>, ConfigPart {
		// base interface
	}

	/**
	 * @param rawData
	 *        the input the {@link ChartTree} is about.
	 * @return a new ChartTree as base-model for charts.
	 */
	public ChartTree prepare(Collection<? extends Object> rawData);

}