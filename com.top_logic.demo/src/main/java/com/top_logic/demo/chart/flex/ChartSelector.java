/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.chart.flex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.reporting.flex.chart.component.ChartModel;
import com.top_logic.reporting.flex.chart.component.MultiChartModel;
import com.top_logic.reporting.flex.chart.config.ChartConfig;
import com.top_logic.reporting.flex.search.chart.SearchResultChartConfig;

/**
 * Component allowing to select one or many configured charts.
 * 
 * <p>
 * When a chart is selected, this component propagates the chart to a display component through a
 * {@link com.top_logic.mig.html.layout.LayoutComponent.Config#getModelSpec() selection relation}.
 * </p>
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class ChartSelector extends FormComponent implements Selectable {

	public static final String CHARTS_FIELD = "charts";

	public static final String CONTENT_FIELD = "content";

	/**
	 * The path relative to which configured chart XML configurations are resolved.
	 */
	public static final String BASE_DIR = "/WEB-INF/charts";

	/**
	 * Configuration options for {@link ChartSelector}.
	 */
	public interface Config extends FormComponent.Config {

		/**
		 * Whether to allow selecting multiple charts.
		 * 
		 * <p>
		 * With multi selection enabled, a {@link MultiChartModel} is accepted instead of a
		 * {@link ChartModel}.
		 * </p>
		 */
		boolean isMulti();

		/**
		 * The chart XML configurations files relative to {@link ChartSelector#BASE_DIR}.
		 */
		public List<ChartRef> getCharts();

		@BooleanDefault(true)
		@Override
		boolean getDisplayWithoutModel();

	}

	final boolean _multi;

	/**
	 * Creates a {@link ChartSelector} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ChartSelector(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		_multi = config.isMulti();
	}

	@Override
	public FormContext createFormContext() {
		FormContext result = new FormContext(this);

		Config config = (Config) getConfig();
		List<? extends NamedConfiguration> options = config.getCharts();

		SelectField selectField = FormFactory.newSelectField(CHARTS_FIELD, options, _multi, null, false, false, null);
		selectField.setOptionLabelProvider(ChartRefLabels.INSTANCE);

		selectField.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				if (_multi) {
					List<ChartConfig> configs = new ArrayList<>();
					@SuppressWarnings("unchecked")
					Collection<ChartRef> newChartRefs = (Collection<ChartRef>) newValue;
					for (ChartRef ref : newChartRefs) {
						configs.add(readFile(ref.getName()));
					}
					ChartSelector.this.setSelected(new MultiChartModel(configs));
				} else {
					NamedConfiguration option = (NamedConfiguration) CollectionUtil.getFirst(newValue);
					if (option != null) {
						String filename = option.getName();
						SearchResultChartConfig chartConfig = readFile(filename);
						if (chartConfig != null) {
							ChartSelector.this.setSelected(new ChartModel(chartConfig, null));
						}
					}
				}
			}
		});

		result.addMember(selectField);

		if (!_multi) {
			final StringField contentField = FormFactory.newStringField(CONTENT_FIELD, "", true);
			selectField.addValueListener(new ValueListener() {
				@Override
				public void valueChanged(FormField field, Object oldValue, Object newValue) {
					String content = "";

					NamedConfiguration option = (NamedConfiguration) CollectionUtil.getFirst(newValue);
					if (option != null) {
						String filename = option.getName();
						content = readContent(filename);
					}

					contentField.setValue(content);
				}
			});
			result.addMember(contentField);
		}
		return result;
	}

	static SearchResultChartConfig readFile(String filename) {
		return readFile(BASE_DIR, filename);
	}

	static String readContent(String filename) {
		try {
			BinaryData file = FileManager.getInstance().getData(BASE_DIR + '/' + filename);
			return StreamUtilities.readAllFromStream(file);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static SearchResultChartConfig readFile(String dir, String filename) {
		try {
			BinaryData content = FileManager.getInstance().getData(dir + '/' + filename);
			ConfigurationDescriptor desc = TypedConfiguration.getConfigurationDescriptor(SearchResultChartConfig.class);
			InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
			Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap("chart", desc);
			ConfigurationReader reader = new ConfigurationReader(context, descriptors);
			return (SearchResultChartConfig) reader.setSource(content).read();
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Invalid chart configuration.", ex);
		}
	}

}
