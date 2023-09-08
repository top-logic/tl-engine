/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

import com.top_logic.base.chart.component.JFreeChartComponent;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.layout.flexreporting.producer.FlexibleReportingProducer;
import com.top_logic.reporting.layout.meta.search.ChartComponent;
import com.top_logic.reporting.layout.meta.search.ReportingSearchDetailComponent.SearchReportDetailChartHandler;
import com.top_logic.reporting.report.control.producer.AbstractChartProducer;
import com.top_logic.reporting.report.control.producer.ChartProducer;
import com.top_logic.reporting.report.model.FilterVO;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.ReportFactory;
import com.top_logic.reporting.report.model.RevisedReport;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public abstract class AbstractFixedReportComponent extends JFreeChartComponent implements ChartComponent {

	public interface Config extends JFreeChartComponent.Config {
		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Name(AbstractChartProducer.LABEL_TITLE)
		@StringDefault("")
		String getTitle();

		@Name(AbstractChartProducer.LABEL_X_AXIS)
		@StringDefault("")
		String getXAxisLabel();

		@Name(AbstractChartProducer.LABEL_Y_AXIS)
		@StringDefault("")
		String getYAxisLabel();

		@Name(AbstractChartProducer.VALUE_SHOW_LEGEND)
		@BooleanDefault(true)
		boolean getValueShowLegend();

		@Name(AbstractChartProducer.VALUE_SHOW_TOOLTIPS)
		@BooleanDefault(true)
		boolean getValueShowTooltips();

		@Name(AbstractChartProducer.VALUE_SHOW_URLS)
		@BooleanDefault(false)
		boolean getValueShowUrls();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			JFreeChartComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(RefreshChartCommandHandler.COMMAND_ID);
		}

	}

	public static final String GOTO_HANDLER = "gotoHandler";

	private ReportConfiguration configuration;
	
	private String  title;
	private String  xAxisLabel;
	private String  yAxisLabel;
	private boolean showLegend;
	private boolean showTooltips;
	private boolean showUrls;

	private RevisedReport report;

	/**
	 * C'tor 
	 */
	public AbstractFixedReportComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
		this.title        = atts.getTitle();
		this.xAxisLabel   = atts.getXAxisLabel();
		this.yAxisLabel   = atts.getYAxisLabel();
		this.showLegend   = atts.getValueShowLegend();
		this.showTooltips = atts.getValueShowTooltips();
		this.showUrls     = atts.getValueShowUrls();
	}
	
	@Override
	protected JFreeChart createChart(String anImageId) {
		ChartProducer theChartProducer = FlexibleReportingProducer.newFlexibleReportingProducer();
//		ReportConfiguration theReportConfig  = this.getReportConfiguration();
		FilterVO            theFVO           = (FilterVO) this.getModel();

//		theFVO.setReportConfiguration(theReportConfig);

		return theChartProducer.produceChart(theFVO);
    }

	@Override
	protected Dataset createDataSet(String anImageId) {
		return null;
	}

	@Override
	public RevisedReport getReport() {
		if (this.report == null) {
			this.report = ReportFactory.getInstance().getReport(this.getReportConfiguration()); 
		}
		return this.report;
	}

	/**
	 * This method must be overridden in subclasses because the configuration created here is not
	 * complete.
	 * 
	 * @return a partly configured {@link ReportConfiguration}.
	 * 
	 * @see com.top_logic.reporting.layout.meta.search.ChartComponent#getReportConfiguration()
	 */
	@Override
	public ReportConfiguration getReportConfiguration() {
		if (this.configuration == null) {
			try {
				this.configuration  = ReportFactory.getInstance().createReportConfiguration(RevisedReport.class);
				
				this.configuration.setShowLegend(this.showLegend);
				this.configuration.setShowUrls(this.showUrls);
				this.configuration.setShowToolTips(this.showTooltips);
				this.configuration.setXAxisLabel(this.xAxisLabel);
				this.configuration.setYAxisLabel(this.yAxisLabel);
				this.configuration.setTitleLabel(this.title);
			}
			catch (ConfigurationException e) {
				throw new ConfigurationError("invalid configuration", e);
			}
		}
		return this.configuration;
	}
	
	@Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(initialModel());
		}
		return super.validateModel(context);
	}

	private Object initialModel() {
		FilterVO theVO = new FilterVO();
		theVO.setReportConfiguration(this.getReportConfiguration());
		theVO.setValue("report", this.getReport());
		String theHandlerName = getCommandById(SearchReportDetailChartHandler.OPEN_HANDLER_NAME).getID();
		theVO.setValue(SearchReportDetailChartHandler.CHART_DETAIL_HANDLER, theHandlerName);
		return theVO;
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return true;
	}
	
	public abstract List<String> getResultColumns();

	public static class RefreshChartCommandHandler extends AbstractCommandHandler {
		
		public static String COMMAND_ID = "RefreshChartCommand";
		
		public RefreshChartCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
		}
		
		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			HandlerResult result = new HandlerResult();
			
			aComponent.invalidateButtons();
			aComponent.invalidate();
			
			return result;
		}
	}
}
