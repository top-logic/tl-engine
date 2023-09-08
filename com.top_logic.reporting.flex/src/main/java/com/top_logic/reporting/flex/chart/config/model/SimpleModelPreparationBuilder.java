/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.model;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.reporting.flex.chart.config.aggregation.AggregationFunction;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;
import com.top_logic.reporting.flex.chart.config.partition.MultiClassificationPartition;
import com.top_logic.reporting.flex.chart.config.partition.PartitionFunction;
import com.top_logic.reporting.flex.chart.config.partition.SingleClassificationPartition;
import com.top_logic.reporting.flex.chart.config.util.MetaElementProvider;
import com.top_logic.reporting.flex.search.chart.GenericModelPreparationBuilder;

/**
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class SimpleModelPreparationBuilder implements
		InteractiveBuilder<DefaultModelPreparation, ChartContextObserver>,
		ConfiguredInstance<SimpleModelPreparationBuilder.Config> {

	private static final String PARTITION = "partition";

	private static final String AGGREGATION = "aggregation";

	private final Config _config;

	/**
	 * Config-interface for {@link DefaultModelPreparation}.
	 */
	public interface Config extends PolymorphicConfiguration<SimpleModelPreparationBuilder> {

		/**
		 * provider for the meta-element this {@link ModelPreparation} works with
		 */
		public MetaElementProvider getType();

		/**
		 * the names of attributes that should not be selectable in the UI.
		 */
		@Format(CommaSeparatedStrings.class)
		public List<String> getPartitionExcludes();

		/**
		 * Possible {@link AggregationFunction} configurations to choose from.
		 */
		public List<AggregationFunction.Config> getAggregationOptions();
	}

	/**
	 * Config-Constructor for {@link SimpleModelPreparationBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public SimpleModelPreparationBuilder(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver observer) {
		List<AggregationFunction.Config> aggregations = getConfig().getAggregationOptions();
		SelectField aggregationField =
			FormFactory.newSelectField(AGGREGATION, aggregations, false, null, true, false, null);
		aggregationField.initSingleSelection(CollectionUtil.getFirst(aggregations));
		aggregationField.setOptionLabelProvider(new LabelProvider() {

			@Override
			public String getLabel(Object object) {
				return SimpleInstantiationContext
				.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance((AggregationFunction.Config) object).getLabel();
			}
		});
		aggregationField.setDisabled(aggregations.size() == 1);
		List<TLStructuredTypePart> partitions = getClassificationAttributes();
		SelectField partitionField =
			FormFactory.newSelectField(PARTITION, partitions, false, null, true, false, null);
		partitionField.initSingleSelection(CollectionUtil.getFirst(partitions));

		container.addMember(aggregationField);
		container.addMember(partitionField);

		template(container, div(
			fieldsetBox(
				resource(I18NConstants.DISPLAY_OPTIONS),
				verticalBox(
					fieldBox(AGGREGATION),
					fieldBox(PARTITION)),
				ConfigKey.field(container))));
	}

	private List<TLStructuredTypePart> getClassificationAttributes() {
		List<String> excludes = getConfig().getPartitionExcludes();
		List<TLStructuredTypePart> result = new ArrayList<>();
		List<? extends TLStructuredTypePart> metaAttributes = GenericModelPreparationBuilder.allParts(getTypes());
		for (TLStructuredTypePart ma : metaAttributes) {
			if (!AttributeOperations.isClassificationAttribute(ma)) {
				continue;
			}
			if (excludes.contains(ma.getName())) {
				continue;
			}
			result.add(ma);
		}
		return result;
	}

	private Set<? extends TLClass> getTypes() {
		return _config.getType().get();
	}

	@Override
	public DefaultModelPreparation build(FormContainer container) {

		SelectField aggregationField = (SelectField) container.getField(AGGREGATION);
		SelectField partitionField = (SelectField) container.getField(PARTITION);
		TLStructuredTypePart partition = (TLStructuredTypePart) partitionField.getSingleSelection();
		PartitionFunction.Config item = null;
		if (partition.isMultiple()) {
			item = MultiClassificationPartition.item(partition);
		} else {
			item = SingleClassificationPartition.item(partition);
		}
		AggregationFunction.Config aggregation = (AggregationFunction.Config) aggregationField.getSingleSelection();
		return DefaultModelPreparation.create(Collections.singletonList(item),
			Collections.singletonList(aggregation));
	}

}
