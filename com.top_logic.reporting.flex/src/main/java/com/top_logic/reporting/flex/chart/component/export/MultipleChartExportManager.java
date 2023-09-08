/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.base.office.ppt.SlideReplacement;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.flex.chart.component.MultipleChartComponent;
import com.top_logic.tool.export.AbstractOfficeExportHandler.OfficeExportValueHolder;

/**
 * {@link AbstractExportManager} that exports multiple charts, one chart per slide.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class MultipleChartExportManager extends AbstractExportManager<MultipleChartExportManager.Config> {

	/**
	 * Config-interface for {@link MultipleChartExportManager}.
	 */
	public interface Config extends AbstractExportManager.Config {

		@Override
		@ClassDefault(MultipleChartExportManager.class)
		public Class<? extends MultipleChartExportManager> getImplementationClass();

		@Override
		@StringDefault("flex/chart/multipleCharts.pptx")
		public String getTemplatePath();
	}

	/**
	 * Config-Constructor for {@link MultipleChartExportManager}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public MultipleChartExportManager(InstantiationContext context, Config config) {
		super(context, config);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void exportAdditionalValues(LayoutComponent caller, Map<String, Object> valueMap, Map arguments) {
		MultipleChartComponent component = (MultipleChartComponent) caller;
		List<OfficeExportValueHolder> list = new ArrayList<>();
		for (BinaryContent content : component.getContentForExport()) {
			Map<String, Object> map = new HashMap<>(valueMap);
			map.put("PICTURE_CHART", content);
			list.add(new OfficeExportValueHolder(null, null, map, false));
		}

		valueMap.put("ADDSLIDES_CHARTS", new SlideReplacement("flex/chart/singleChartReplacement.pptx", list));

	}
}