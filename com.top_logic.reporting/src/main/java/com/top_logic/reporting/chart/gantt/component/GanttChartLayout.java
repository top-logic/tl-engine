/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.component;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.structure.ContentControl;
import com.top_logic.layout.structure.ContextMenuLayoutControlProvider;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.FixedFlowLayoutControl;
import com.top_logic.layout.structure.FlowLayoutControl;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutControlProvider} that that creates two separate boxes (chart above form) for the
 * same {@link LayoutComponent}.
 */
public class GanttChartLayout extends ContextMenuLayoutControlProvider<GanttChartLayout> {

	/**
	 * Creates a new {@link GanttChartLayout}.
	 */
	public GanttChartLayout(InstantiationContext context, Config<GanttChartLayout> config) {
		super(context, config);
	}

	@Override
	public LayoutControl mkLayout(Strategy strategy, LayoutComponent component) {
		FlowLayoutControl layout = new FixedFlowLayoutControl(Orientation.VERTICAL);

		LayoutControlAdapter chart = new LayoutControlAdapter(createChartDisplay(component));
		chart.listenForInvalidation(component);
		chart.setConstraint(DefaultLayoutData.DEFAULT_CONSTRAINT);
		layout.addChild(chart);

		LayoutControl form = createFormDisplay(component);
		layout.addChild(form);

		return layout;
	}

	/**
	 * Custom rendered (chart) view of the component.
	 */
	protected HTMLFragment createChartDisplay(LayoutComponent businessComponent) {
		return new GanttChartDisplay((GanttComponent) businessComponent);
	}

	/**
	 * Direct (from) view of the component.
	 */
	protected LayoutControl createFormDisplay(LayoutComponent businessComponent) {
		ContentControl contentControl = createContentWithMenu(businessComponent);
		contentControl.setConstraint(new DefaultLayoutData(100, DisplayUnit.PERCENT, 100, 30, DisplayUnit.PIXEL, 100,
			Scrolling.NO));
		return contentControl;
	}
}