/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component.export;

import java.util.List;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.dsa.DataAccessProxy;

/**
 * Default implementation of {@link AdditionalValuesTemplateProvider} thar returns the template
 * depending on the number of exported columns.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultAdditionalValuesTemplatePathProvider implements AdditionalValuesTemplateProvider,
		ConfiguredInstance<DefaultAdditionalValuesTemplatePathProvider.Config> {

	/**
	 * Configuration for a {@link DefaultAdditionalValuesTemplatePathProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<DefaultAdditionalValuesTemplatePathProvider> {

		/**
		 * The path to the base template.
		 * 
		 * <p>
		 * This path is used as base path to find a template for the correct number of export
		 * columns.
		 * </p>
		 */
		@StringDefault("flex/chart/table.pptx")
		String getBaseName();

	}

	private final Config _config;

	/**
	 * Creates a new {@link DefaultAdditionalValuesTemplatePathProvider} from the given
	 * configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DefaultAdditionalValuesTemplatePathProvider}.
	 */
	public DefaultAdditionalValuesTemplatePathProvider(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public String getTemplate(List<String> columns) {
		return getTemplate(columns.size(),
			ConfiguredChartExportManager.DefaultAdditionalChartValueProvider.indexColumnFirst(columns));
	}

	private String getTemplate(int colCount, boolean indexColumnFirst) {
		String baseName = getConfig().getBaseName();
		try {
			DataAccessProxy dap = new DataAccessProxy("webinf://reportTemplates", "ppt");
			while (colCount > 0) {
				String res = createTemplateName(colCount, indexColumnFirst, baseName);
				if (exists(dap, res)) {
					return res;
				}
				colCount--;
			}
		} catch (Exception ex) {
			// ignore
		}
		return baseName;
	}

	private boolean exists(DataAccessProxy dap, String name) {
		try {
			dap = dap.getChildProxy(name);
			return dap.exists();
		} catch (Exception ex) {
			// ignore
		}
		return false;
	}

	private String createTemplateName(int colCount, boolean indexColumnFirst, String baseName) {
		int pos = baseName.lastIndexOf('.');
		return baseName.substring(0, pos) + colCount + (indexColumnFirst ? "i" : "") + baseName.substring(pos);
	}

}
