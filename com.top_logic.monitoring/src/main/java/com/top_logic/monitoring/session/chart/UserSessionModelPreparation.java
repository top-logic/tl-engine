/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.session.chart;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.col.ListBuilder;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.provider.EnumResourceProvider;
import com.top_logic.reporting.flex.chart.config.aggregation.AggregationFunction;
import com.top_logic.reporting.flex.chart.config.dataset.TimeseriesDatasetBuilder.Period;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.model.AbstractModelPreparationBuilder;
import com.top_logic.reporting.flex.chart.config.partition.ObjectValuePartition;
import com.top_logic.reporting.flex.chart.config.partition.ObjectValuePartition.Aggregator;
import com.top_logic.reporting.flex.chart.config.partition.ObjectValuePartition.TimePeriodAggregator;
import com.top_logic.reporting.flex.chart.config.partition.PartitionFunction;

/**
 * Interactive builder for {@link UserSessionModelPreparation}
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class UserSessionModelPreparation extends AbstractModelPreparationBuilder {

	private static final String PERIOD = "period";

	private static final TagTemplate TEMPLATE = div(verticalBox(fieldBox(PERIOD)));

	/**
	 * Config-Constructor for {@link UserSessionModelPreparation}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public UserSessionModelPreparation(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver arg) {
		List<Aggregator> options = new ListBuilder<Aggregator>()
			.add(TimePeriodAggregator.instance(Period.HOUR))
			.add(TimePeriodAggregator.instance(Period.DAY))
			.add(TimePeriodAggregator.instance(Period.WEEK))
			.add(TimePeriodAggregator.instance(Period.MONTH))
			.add(TimePeriodAggregator.instance(Period.YEAR))
			.toList();

		List<Aggregator> init = options.subList(0, 1);
		SelectField periodField = FormFactory.newSelectField(PERIOD, options, false, init, true, false, null);
		periodField.setOptionLabelProvider(new LabelProvider() {

			@Override
			public String getLabel(Object object) {
				Period period = ((TimePeriodAggregator) object).getConfig().getPeriod();
				return EnumResourceProvider.INSTANCE.getLabel(period);
			}
		});
		container.addMember(periodField);

		template(container, TEMPLATE);
	}

	@Override
	protected List<PartitionFunction.Config> getPartitions(FormContainer container) {
		SelectField field = (SelectField) container.getField(PERIOD);
		Aggregator aggr = (Aggregator) field.getSingleSelection();
		ObjectValuePartition.Config partition = ObjectValuePartition.item("date", aggr);
		return Collections.singletonList((PartitionFunction.Config) partition);
	}

	@Override
	protected List<AggregationFunction.Config> getAggregations(FormContainer container) {
		return Collections.emptyList();
	}

}
