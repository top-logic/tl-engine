/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component.builder;

import java.util.Collection;

import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Simple {@link ListModelBuilder} for chart-details that uses its own model as list.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class ConfiguredChartListModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link ConfiguredChartListModelBuilder} instance.
	 */
	public static final ConfiguredChartListModelBuilder INSTANCE = new ConfiguredChartListModelBuilder();

	private ConfiguredChartListModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return (Collection<?>) businessModel;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null || aModel instanceof Collection;
	}

	@Override
	public boolean supportsListElement(LayoutComponent aComponent, Object anObject) {
		return true;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anObject) {
		return null;
	}
}