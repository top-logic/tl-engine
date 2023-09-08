/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search.chart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.func.Function0;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.reporting.flex.chart.config.aggregation.AbstractAggregationFunction.Operation;
import com.top_logic.reporting.flex.chart.config.aggregation.AggregationFunction;
import com.top_logic.reporting.flex.chart.config.aggregation.AttributeAggregationFunction;
import com.top_logic.reporting.flex.chart.config.aggregation.CountFunction;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.model.DefaultModelPreparation;
import com.top_logic.reporting.flex.chart.config.model.ModelPreparation;
import com.top_logic.reporting.flex.chart.config.partition.AbstractAttributeBasedPartition;
import com.top_logic.reporting.flex.chart.config.partition.BooleanValuePartition;
import com.top_logic.reporting.flex.chart.config.partition.DateAttributePartition;
import com.top_logic.reporting.flex.chart.config.partition.MultiClassificationPartition;
import com.top_logic.reporting.flex.chart.config.partition.MultiValuePartition;
import com.top_logic.reporting.flex.chart.config.partition.NumberIntervalPartition;
import com.top_logic.reporting.flex.chart.config.partition.PartitionFunction;
import com.top_logic.reporting.flex.chart.config.partition.PathValuePartition;
import com.top_logic.reporting.flex.chart.config.partition.SingleClassificationPartition;
import com.top_logic.reporting.flex.chart.config.partition.SingleValuePartition;
import com.top_logic.reporting.flex.chart.config.partition.TLClassPartition;
import com.top_logic.reporting.flex.chart.config.util.MetaAttributeProvider;

/**
 * {@link AbstractSearchModelPreparationBuilder} that uses typed configuration UIs.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GenericModelPreparationBuilder extends AbstractSearchModelPreparationBuilder {

	static final Property<Set<? extends TLClass>> TYPES = TypedAnnotatable.propertySet("type");

	private DefaultModelPreparation.Config _template;

	/**
	 * Creates a {@link GenericModelPreparationBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GenericModelPreparationBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * @see com.top_logic.reporting.flex.chart.config.model.DefaultModelPreparation.Config#getPartitions()
	 */
	public static class PartitionOptions extends Function0<List<PartitionFunction.Config>> {

		private Set<? extends TLClass> _types;

		/**
		 * Creates context-aware {@link PartitionOptions}.
		 */
		@CalledByReflection
		public PartitionOptions(DeclarativeFormOptions options) {
			_types = options.get(TYPES);
		}

		@Override
		public List<PartitionFunction.Config> apply() {
			return getPartitionConfigOptions(_types);
		}

		public static List<PartitionFunction.Config> getPartitionConfigOptions(Set<? extends TLClass> types) {
			List<? extends TLStructuredTypePart> parts = allParts(types);

			List<PartitionFunction.Config> options = new ArrayList<>();
			for (TLStructuredTypePart part : parts) {
				if (DisplayAnnotations.isHidden(part)) {
					continue;
				}
				TLType type = part.getType();
				Map<String, String> init = null;
				Class<? extends AbstractAttributeBasedPartition.Config> config;
				if (type instanceof TLPrimitive) {
					switch (((TLPrimitive) type).getKind()) {
						case BOOLEAN:
						case TRISTATE: {
							config = BooleanValuePartition.Config.class;
							break;
						}
						case DATE: {
							config = DateAttributePartition.Config.class;
							break;
						}
						case FLOAT:
						case INT: {
							config = NumberIntervalPartition.Config.class;
							break;
						}
						default: {
							if (part.isMultiple()) {
								config = MultiValuePartition.Config.class;
							} else {
								config = SingleValuePartition.Config.class;
							}
							break;
						}
					}
				}
				else if (type instanceof TLEnumeration) {
					if (!part.isMandatory()) {
						init = Collections.singletonMap(AbstractAttributeBasedPartition.Config.ADD_EMPTY, "true");
					}
					if (part.isMultiple()) {
						config = MultiClassificationPartition.Config.class;
					} else {
						config = SingleClassificationPartition.Config.class;
					}
				}
				else if (type instanceof TLClass && !part.isMultiple()) {
					config = PathValuePartition.Config.class;
				}
				else {
					if (part.isMultiple()) {
						config = MultiValuePartition.Config.class;
					} else {
						config = SingleValuePartition.Config.class;
					}
				}

				AbstractAttributeBasedPartition.Config model = item(config, init);
				model.setMetaAttribute(toMetaAttribute(part));
				options.add(model);
			}
			if (types.size() > 1) {
				options.add(TypedConfiguration.newConfigItem(TLClassPartition.Config.class));
			}
			return options;
		}

		private static <C extends ConfigurationItem> C item(Class<C> config, Map<String, String> init) {
			if (init == null) {
				return TypedConfiguration.newConfigItem(config);
			} else {
				try {
					return TypedConfiguration.newConfigItem(config, init.entrySet());
				} catch (ConfigurationException ex) {
					throw new ConfigurationError(ex);
				}
			}
		}
	}

	/**
	 * @see com.top_logic.reporting.flex.chart.config.model.DefaultModelPreparation.Config#getAggregations()
	 */
	public static class AggregationOptions extends Function0<List<AggregationFunction.Config>> {

		private Set<? extends TLClass> _types;

		/**
		 * Creates context-aware {@link PartitionOptions}.
		 */
		@CalledByReflection
		public AggregationOptions(DeclarativeFormOptions options) {
			_types = options.get(TYPES);
		}

		@Override
		public List<AggregationFunction.Config> apply() {
			List<? extends TLStructuredTypePart> parts = allParts(_types);

			List<AggregationFunction.Config> options = new ArrayList<>();

			CountFunction.Config countConfig = TypedConfiguration.newConfigItem(CountFunction.Config.class);
			options.add(countConfig);

			for (TLStructuredTypePart part : parts) {
				if (DisplayAnnotations.isHidden(part)) {
					continue;
				}
				TLType type = part.getType();
				
				if (type instanceof TLPrimitive) {
					switch (((TLPrimitive) type).getKind()) {
						case FLOAT:
						case INT: {
							AttributeAggregationFunction.Config config = TypedConfiguration.newConfigItem(AttributeAggregationFunction.Config.class);
							config.setMetaAttribute(toMetaAttribute(part));
							config.setOperation(Operation.SUM);
							options.add(config);
							break;
						}

						default: {
							break;
						}
					}				
				}

				if (part.isMultiple() || !part.isMandatory()) {
					CountFunction.Config valueCount = TypedConfiguration.newConfigItem(CountFunction.Config.class);
					valueCount.setMetaAttribute(toMetaAttribute(part));
					options.add(valueCount);
				}
			}
			return options;
		}

	}

	static MetaAttributeProvider toMetaAttribute(TLStructuredTypePart part) {
		return new MetaAttributeProvider(part);
	}

	/**
	 * Collects all parts of the given set of types.
	 */
	public static List<? extends TLStructuredTypePart> allParts(Set<? extends TLClass> types) {
		List<TLStructuredTypePart> result = new ArrayList<>();
		for (TLClass type : types) {
			result.addAll(type.getAllClassParts());
		}
		return result;
	}

	public void loadModelPreparation(DefaultModelPreparation.Config newModelPreparation) {
		_template = newModelPreparation;
	}

	@Override
	public void createUI(FormContainer container, final ChartContextObserver arg) {
		final DefaultModelPreparation.Config editModel;
		if (_template == null) {
			editModel =
				TypedConfiguration.newConfigItem(DefaultModelPreparation.Config.class);
		} else {
			editModel = TypedConfiguration.copy(_template);
		}
		InitializerIndex context = new InitializerIndex();
		context.set(TYPES, ((AttributedSearchResultSet) arg.getModel()).getTypes());
		EditorFactory.initEditorGroup(container, editModel, context);

		ConfigurationListener updatePartitions = new ConfigurationListener() {
			@Override
			public void onChange(ConfigurationChange change) {
				arg.setPartitions(new ArrayList<>(editModel.getPartitions()));
			}
		};
		editModel.addConfigurationListener(
			editModel.descriptor().getProperty(DefaultModelPreparation.Config.PARTITIONS),
			updatePartitions);
		updatePartitions.onChange(null);

		ConfigurationListener updateAggregations = new ConfigurationListener() {
			@Override
			public void onChange(ConfigurationChange change) {
				arg.setAggregations(new ArrayList<>(editModel.getAggregations()));
			}
		};
		editModel.addConfigurationListener(
			editModel.descriptor().getProperty(DefaultModelPreparation.Config.AGGREGATIONS),
			updateAggregations);
		updateAggregations.onChange(null);
	}

	@Override
	public DefaultModelPreparation build(FormContainer container) {
		DefaultModelPreparation.Config editModel = (DefaultModelPreparation.Config) EditorFactory.getModel(container);

		// Create a copy of the edit model before using it to get rid of all kind of listeners
		// attached to the edit model.
		DefaultModelPreparation.Config configModel = TypedConfiguration.copy(editModel);

		ModelPreparation result =
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(configModel);
		return (DefaultModelPreparation) result;
	}

	@Override
	protected List<PartitionFunction.Config> getPartitions(FormContainer container) {
		throw new UnsupportedOperationException("Useless abstraction, see build(FormContainer).");
	}

	@Override
	protected List<AggregationFunction.Config> getAggregations(FormContainer container) {
		throw new UnsupportedOperationException("Useless abstraction, see build(FormContainer).");
	}

}
