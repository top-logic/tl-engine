/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.ui;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link FlowChartBuilder} that can be implemented by TL-Script.
 */
@InApp
public class ScriptFlowChartBuilder extends AbstractConfiguredInstance<ScriptFlowChartBuilder.Config<?>>
		implements FlowChartBuilder {

	/**
	 * Configuration options for {@link ScriptFlowChartBuilder}.
	 */
	public interface Config<I extends ScriptFlowChartBuilder> extends PolymorphicConfiguration<I> {
		/**
		 * Predicate that decides, whether a given model can be displayed as flow chart.
		 */
		@NonNullable
		@FormattedDefault("true")
		Expr getSupportsModel();

		/**
		 * Function creating a flow chart form a given model.
		 */
		@NonNullable
		@FormattedDefault("model -> flowChart()")
		Expr getCreateChart();
	}

	private QueryExecutor _supportModel;

	private QueryExecutor _createChart;

	/**
	 * Creates a {@link ScriptFlowChartBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ScriptFlowChartBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
		_supportModel = QueryExecutor.compile(config.getSupportsModel());
		_createChart = QueryExecutor.compile(config.getCreateChart());
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return SearchExpression.asBoolean(_supportModel.execute(aModel));
	}

	@Override
	public Diagram getModel(Object businessModel, LayoutComponent aComponent) {
		return (Diagram) _createChart.execute(businessModel);
	}

}
