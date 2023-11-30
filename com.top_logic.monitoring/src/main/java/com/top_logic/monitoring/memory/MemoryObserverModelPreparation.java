/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.memory;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.data.time.Second;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.control.ChoiceControl;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.reporting.flex.chart.config.aggregation.AggregationFunction;
import com.top_logic.reporting.flex.chart.config.aggregation.ObjectAsValueFunction;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.model.AbstractModelPreparationBuilder;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.ConfiguredKeysPartition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.TimePeriodCriterion;
import com.top_logic.reporting.flex.chart.config.partition.PartitionFunction;
import com.top_logic.reporting.flex.chart.config.util.ToStringText;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.sched.MemoryObserverThread.MemoryUsageEntry;
import com.top_logic.util.sched.MemoryObserverThread.MemoryUsageEntryAccessor;

/**
 * Interactive builder for {@link MemoryObserverModelPreparation}
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class MemoryObserverModelPreparation extends AbstractModelPreparationBuilder {

	private static final TagTemplate TEMPLATE = div(verticalBox(fieldBox(Config.ATTRIBUTES)));

	/**
	 * Config-interface for {@link MemoryObserverModelPreparation}.
	 */
	public interface Config extends AbstractModelPreparationBuilder.Config {

		public static final String ATTRIBUTES = "attributes";

		public static final String INITIAL = "initial";

		@Override
		@ClassDefault(MemoryObserverModelPreparation.class)
		public Class<? extends MemoryObserverModelPreparation> getImplementationClass();

		@Name(ATTRIBUTES)
		@Format(CommaSeparatedStrings.class)
		public List<String> getAttributes();

		@Name(INITIAL)
		@Format(CommaSeparatedStrings.class)
		public List<String> getInitial();

	}

	/**
	 * Config-Constructor for {@link MemoryObserverModelPreparation}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public MemoryObserverModelPreparation(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver arg) {
		List<String> options = getConfig().getAttributes();
		List<String> init = getConfig().getInitial();

		SelectField attrField = FormFactory.newSelectField(Config.ATTRIBUTES, options, true, init, true, false, null);
		final Resources resources = Resources.getInstance();
		attrField.setOptionLabelProvider(new AbstractResourceProvider() {
			@Override
			public String getLabel(Object object) {
				return resources.getString(key(object));
			}

			@Override
			public String getTooltip(Object object) {
				return resources.getString(key(object).tooltipOptional());
			}

			private ResKey key(Object object) {
				return I18NConstants.MEMORY_OBSERVER.key(suffix(object));
			}

			private String suffix(Object object) {
				return String.valueOf(object);
			}
		});
		attrField.setControlProvider(new ControlProvider() {

			@Override
			public Control createControl(Object model, String style) {
				return new ChoiceControl((SelectField) model);
			}
		});
		container.addMember(attrField);

		template(container, TEMPLATE);
	}

	@Override
	protected List<PartitionFunction.Config> getPartitions(FormContainer container) {
		SelectField field = (SelectField) container.getField(Config.ATTRIBUTES);
		List<String> attr = CollectionUtil.dynamicCastView(String.class, field.getSelection());
		PartitionFunction.Config attrPartition =
			ConfiguredKeysPartition.item(I18NConstants.MEMORY_OBSERVER, attr);
		PartitionFunction.Config valuePartition = MemoryUsageEntryPartition.item();
		return CollectionUtil.createList(attrPartition, valuePartition);
	}

	@Override
	protected List<AggregationFunction.Config> getAggregations(FormContainer container) {
		return Collections.singletonList((AggregationFunction.Config) ObjectAsValueFunction.item());
	}

	public static class MemoryUsageEntryPartition implements PartitionFunction, ConfiguredInstance<MemoryUsageEntryPartition.Config> {

		private final Config _config;

		/**
		 * Config-interface for {@link MemoryUsageEntryPartition}.
		 */
		public interface Config extends PartitionFunction.Config {

			@Override
			@ClassDefault(MemoryUsageEntryPartition.class)
			public Class<? extends MemoryUsageEntryPartition> getImplementationClass();
		}

		/**
		 * Config-Constructor for {@link MemoryUsageEntryPartition}.
		 * 
		 * @param context
		 *        - default config-constructor
		 * @param config
		 *        - default config-constructor
		 */
		public MemoryUsageEntryPartition(InstantiationContext context, Config config) {
			_config = config;
		}

		@Override
		public Config getConfig() {
			return _config;
		}

		@Override
		public List<Partition> createPartitions(Partition aParent) {
			List<Partition> result = new ArrayList<>();
			
			String attr = ((ToStringText) aParent.getKey()).getKeyName();
			List<MemoryUsageEntry> entries =
				CollectionUtil.dynamicCastView(MemoryUsageEntry.class, aParent.getObjects());

	        for (MemoryUsageEntry entry : entries) {
				Second period = new Second(entry.getDate(), TimeZones.systemTimeZone(), TLContext.getLocale());
				double value = ((Number) MemoryUsageEntryAccessor.INSTANCE.getValue(entry, attr)).doubleValue();
				result.add(new Partition(aParent, period, Collections.singletonList(value)));
	        }
	        
			return result;
		}

		@Override
		public Criterion getCriterion() {
			return TimePeriodCriterion.TIME_PERIOD_INSTANCE;
		}

		/**
		 * Factory method to create an initialized {@link Config}.
		 * 
		 * @return a new ConfigItem.
		 */
		public static Config item() {
			Config item = TypedConfiguration.newConfigItem(Config.class);
			return item;
		}
	}

}
