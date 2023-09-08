/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.chart.chartjs.component;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.chart.chartjs.control.ChartJsControl;
import com.top_logic.layout.Control;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.util.css.CssUtil;

/**
 * {@link BoundComponent} displaying a <a href="https://www.chartjs.org/">chart.js</a> chart based
 * on the component's model.
 * 
 * <p>
 * The chart data is computed by a TL-Script expression, see {@link Config#getData()}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ChartJsComponent extends BoundComponent implements ControlRepresentable {

	/**
	 * Configuration options for {@link ChartJsComponent}.
	 */
	@TagName("chartjs")
	public interface Config extends BoundComponent.Config {

		@Override
		@ClassDefault(ChartJsComponent.class)
		public Class<? extends ChartJsComponent> getImplementationClass();

		/**
		 * Function creating the chart configuration data <code>{ type: ..., data: ..., options:
		 * ... }</code>.
		 * 
		 * <p>
		 * The function takes the component model as single argument and returns a dictionary with
		 * the three keys "type", "data", and "options".
		 * </p>
		 */
		@Mandatory
		@Name("data")
		Expr getData();

		/**
		 * The CSS class of the control element.
		 */
		@Name("cssClass")
		@Nullable
		String getCSSClass();

	}

	private QueryExecutor _dataFun;

	private ChartJsControl _chart;

	/**
	 * Creates a {@link ChartJsComponent}.
	 */
	public ChartJsComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		_dataFun = QueryExecutor.compile(config.getData());
	}

	@Override
	public Control getRenderingControl() {
		ChartJsControl chart = createChartControl();
		return _chart = chart;
	}

	/**
	 * Creates a {@link ChartJsControl} that renders the chart configuration data of the current
	 * model.
	 */
	public ChartJsControl createChartControl() {
		ChartJsControl chart = new ChartJsControl(createChartData());
		chart.setCssClass(CssUtil.joinCssClasses("cjsComp", ((Config) getConfig()).getCSSClass()));
		return chart;
	}

	/**
	 * Produces a new version of the displayed chart configuration.
	 */
	private Object createChartData() {
		return _dataFun.execute(getModel());
	}

	@Override
	protected void becomingInvisible() {
		super.becomingInvisible();

		_chart = null;
	}

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		if (_chart != null) {
			// Do not invalidate, but animate to the new version.
			_chart.setModel(createChartData());
		} else {
			super.afterModelSet(oldModel, newModel);
		}
	}

}
