/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.ui;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.graphic.flow.operations.FlowDiagram;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.table.component.BuilderComponent;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutComponent} displaying a flow chart.
 */
public class FlowChartComponent extends BuilderComponent implements ControlRepresentable {

	private FlowChartControl _control = new FlowChartControl();

	/**
	 * Configuration options for {@link FlowChartComponent}.
	 */
	@TagName("flowChart")
	public interface Config extends BuilderComponent.Config {

		@Override
		PolymorphicConfiguration<? extends FlowChartBuilder> getModelBuilder();

		@Override
		@ClassDefault(FlowChartComponent.class)
		Class<? extends LayoutComponent> getImplementationClass();
	}

	/**
	 * Creates a {@link FlowChartComponent}.
	 */
	public FlowChartComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	protected boolean doValidateModel(DisplayContext context) {
		FlowDiagram diagram = (FlowDiagram) getBuilder().getModel(getModel(), this);
		if (diagram != null) {
			diagram.layout(new AWTContext(14));

			_control.setModel(diagram);
		}
		return super.doValidateModel(context);
	}

	@Override
	public Control getRenderingControl() {
		return _control;
	}

}
