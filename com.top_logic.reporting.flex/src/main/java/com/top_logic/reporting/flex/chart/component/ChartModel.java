/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component;

import com.top_logic.reporting.flex.chart.config.ChartConfig;

/**
 * Model created by a {@link ChartConfigComponent} and displayed by a {@link AbstractChartComponent}
 * .
 * 
 * <p>
 * To react on changes the slave model combines the component model and the {@link ChartConfig}
 * because the chart config may stay equal even if the model changes and the slave needs an update.
 * </p>
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class ChartModel {

	private final ChartConfig _config;

	private final Object _model;

	/**
	 * Creates a new SlaveModel based on the given config and component-model
	 */
	public ChartModel(ChartConfig config, Object model) {
		_config = config;
		_model = model;
	}

	/**
	 * The config that describes the chart.
	 */
	public ChartConfig getConfig() {
		return _config;
	}

	/**
	 * The component model.
	 */
	public Object getModel() {
		return _model;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_config == null) ? 0 : _config.hashCode());
		result = prime * result + ((_model == null) ? 0 : _model.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChartModel other = (ChartModel) obj;
		if (_config == null) {
			if (other._config != null)
				return false;
		} else if (!_config.equals(other._config))
			return false;
		if (_model == null) {
			if (other._model != null)
				return false;
		} else if (!_model.equals(other._model))
			return false;
		return true;
	}

}