/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.chart.flex;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.reporting.flex.chart.component.ChartConfigComponent;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveChartBuilder;

/**
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class DemoInteractiveChartSelector extends FormComponent {

	public static final String BASE_DIR = "/WEB-INF/charts";

	public interface Config extends FormComponent.Config {

		@Mandatory
		ComponentName getTarget();

		public List<ChartRef> getCharts();

		@BooleanDefault(true)
		@Override
		boolean getDisplayWithoutModel();

	}

	public DemoInteractiveChartSelector(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	public FormContext createFormContext() {
		FormContext result = new FormContext(this);

		List<ChartRef> options = getConfig().getCharts();
		SelectField selectField = FormFactory.newSelectField(ChartSelector.CHARTS_FIELD, options, false, null, false, false, null);
		selectField.setOptionLabelProvider(ChartRefLabels.INSTANCE);
		selectField.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				ChartRef ref = (ChartRef) CollectionUtil.getFirst(newValue);
				if (ref != null) {
					InteractiveChartBuilder.Config config = readFile(ref.getName());
					if (config != null) {
						getTarget().setBaseConfig(config);
					}
				}
			}
		});
		result.addMember(selectField);
		return result;
	}

	protected InteractiveChartBuilder.Config readFile(String filename) {
		return readFile(BASE_DIR, filename);
	}

	public static InteractiveChartBuilder.Config readFile(String baseDir, String filename) {
		try {
			BinaryData content = FileManager.getInstance().getData(baseDir + '/' + filename);
			ConfigurationDescriptor desc =
				TypedConfiguration.getConfigurationDescriptor(InteractiveChartBuilder.Config.class);
			InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
			Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap("chart-builder", desc);
			ConfigurationReader reader = new ConfigurationReader(context, descriptors);
			return (InteractiveChartBuilder.Config) reader.setSource(content).read();
		} catch (Exception ex) {
			System.out.println(ex);
			return null;
		}
	}

	public ChartConfigComponent getTarget() {
		return (ChartConfigComponent) getMainLayout().getComponentByName(getConfig().getTarget());
	}

}
