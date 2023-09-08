/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search.chart;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.model.TLClass;
import com.top_logic.reporting.flex.chart.config.ChartConfig;
import com.top_logic.reporting.flex.chart.config.aggregation.AggregationFunction;
import com.top_logic.reporting.flex.chart.config.datasource.EmptyChartDataSource;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilderUtil;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveChartBuilder;
import com.top_logic.reporting.flex.chart.config.model.AbstractModelPreparationBuilder;
import com.top_logic.reporting.flex.chart.config.model.DefaultModelPreparation;
import com.top_logic.reporting.flex.chart.config.partition.PartitionFunction;
import com.top_logic.reporting.flex.chart.config.util.MetaElementProvider;

/**
 * {@link InteractiveChartBuilder} class for search-result-reports.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class SearchResultInteractiveChartBuilder extends InteractiveChartBuilder {

	private static final HTMLTemplateFragment TEMPLATE;

	static {
		TEMPLATE = div(
				verticalBox(
					fieldBox(path(Config.CHART_BUILDER, SearchChartBuilder.VIEW_GROUP, SearchChartBuilder.CHART_TYPE_FIELD))),
				contentBox(member(path(Config.CHART_BUILDER, SearchChartBuilder.VIEW_GROUP, SearchChartBuilder.CONFIG_GROUP))),
				contentBox(member(Config.MODEL_PREPARATION)),
			contentBox(
				member(path(Config.CHART_BUILDER, SearchChartBuilder.VIEW_GROUP, SearchChartBuilder.COLORS_GROUP))));
	}

	/**
	 * Creates a {@link SearchResultInteractiveChartBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SearchResultInteractiveChartBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver observer) {
		AttributedSearchResultSet model = (AttributedSearchResultSet) observer.getModel();
		if (model == null) {
			return;
		}
		Set<? extends TLClass> types = model.getTypes();
		if (types.isEmpty()) {
			return;
		}
		initMetaElement(types);

		super.createUI(container, observer);

		Templates.template(container, TEMPLATE);
	}

	private void initMetaElement(Set<? extends TLClass> types) {
		AbstractModelPreparationBuilder modelPreparation =
			(AbstractModelPreparationBuilder) getConfig().getModelPreparation();
		modelPreparation.setTypes(types);
	}

	@Override
	public ChartConfig build(FormContainer container) {
		ChartContextObserver observer = ChartContextObserver.getObserver(container.getFormContext());
		AttributedSearchResultSet model = (AttributedSearchResultSet) observer.getModel();
		if (model == null || model.getTypes().isEmpty()) {
			ChartConfig res = TypedConfiguration.newConfigItem(ChartConfig.class);
			res.setDataSource(EmptyChartDataSource.INSTANCE);
			res.setModelPreparation(DefaultModelPreparation.create(Collections.<PartitionFunction.Config> emptyList(),
				Collections.<AggregationFunction.Config> emptyList()));
			res.setChartBuilder(null);
			return res;
		}
		SearchResultChartConfig result =
			InteractiveBuilderUtil.create(container, getConfig(), SearchResultChartConfig.class);
		result.setType(new MetaElementProvider(model.getTypes()));
		result.setColumns(model.getResultColumns());

		return result;
	}

}
