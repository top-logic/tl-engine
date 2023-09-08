/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder;

import org.jfree.chart.plot.PlotOrientation;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * @author     <a href="mailto:cca@top-logic.com>cca</a>
 */
public enum Orientation implements ExternallyNamed {

	/**
	 * <code>VERTICAL</code> corresponding to {@link PlotOrientation#VERTICAL}
	 */
	VERTICAL("vertical", PlotOrientation.VERTICAL),
	/**
	 * <code>HORIZONTAL</code> corresponding to {@link PlotOrientation#HORIZONTAL}
	 */
	HORIZONTAL("horizontal", PlotOrientation.HORIZONTAL);

	private final String _name;

	private final PlotOrientation _orientation;

	private Orientation(String name, PlotOrientation orientation) {
		_name = name;
		_orientation = orientation;
	}

	@Override
	public String getExternalName() {
		return _name;
	}

	/**
	 * Returns the orientation.
	 */
	public PlotOrientation getOrientation() {
		return _orientation;
	}
}