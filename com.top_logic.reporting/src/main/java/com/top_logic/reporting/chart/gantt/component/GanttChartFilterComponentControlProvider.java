/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.component;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.structure.MediaQueryControl;
import com.top_logic.layout.structure.MediaQueryControl.Layout;
import com.top_logic.layout.structure.MediaQueryControl.Layout.Config;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutControlProvider} which configures the sizes where the {@link MediaQueryControl}
 * calculates the number of columns for the given viewport.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class GanttChartFilterComponentControlProvider implements LayoutControlProvider {

	/**
	 * Singleton of {@link GanttChartFilterComponentControlProvider}.
	 */
	public static final LayoutControlProvider INSTANCE = new GanttChartFilterComponentControlProvider();

	private Layout _ganttChartLayout;

	private GanttChartFilterComponentControlProvider() {
		Config config = TypedConfiguration.newConfigItem(Layout.Config.class);
		PropertyDescriptor sizesProperty = config.descriptor().getProperty("sizes");
		List<Integer> sizes = new ArrayList<>();
		sizes.add(340);
		sizes.add(650);
		sizes.add(960);
		config.update(sizesProperty, sizes);

		_ganttChartLayout = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	@Override
	public LayoutControl createLayoutControl(Strategy strategy, LayoutComponent component) {
		return _ganttChartLayout.createLayoutControl(strategy, component);
	}
}
