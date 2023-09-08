/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.flexreporting.producer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.PeriodAxisLabelInfo;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.AbstractCategoryItemLabelGenerator;
import org.jfree.chart.labels.AbstractXYItemLabelGenerator;
import org.jfree.chart.labels.BoxAndWhiskerXYToolTipGenerator;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.WaterfallBarRenderer;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYBoxAndWhiskerRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;
import org.jfree.data.xy.XYDataset;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.reporting.chart.renderer.ExtendedClusteredXYBarRenderer;
import com.top_logic.reporting.layout.flexreporting.component.ChartConfigurationFieldHelper;
import com.top_logic.reporting.layout.flexreporting.producer.tooltips.ReportingCategoryToolTipGenerator;
import com.top_logic.reporting.layout.flexreporting.producer.tooltips.ReportingPieToolTipGenerator;
import com.top_logic.reporting.layout.flexreporting.producer.tooltips.ReportingXYToolTipGenerator;
import com.top_logic.reporting.layout.meta.search.ChartDetailCategoryURLGenerator;
import com.top_logic.reporting.layout.meta.search.ChartDetailPieURLGenerator;
import com.top_logic.reporting.layout.meta.search.ChartDetailXYURLGenerator;
import com.top_logic.reporting.layout.meta.search.ReportingSearchDetailComponent.SearchReportDetailChartHandler;
import com.top_logic.reporting.report.control.producer.AbstractChartProducer;
import com.top_logic.reporting.report.control.producer.ChartContext;
import com.top_logic.reporting.report.exception.ReportingException;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.ReportFactory;
import com.top_logic.reporting.report.model.ReportTree.ReportNode;
import com.top_logic.reporting.report.model.RevisedReport;
import com.top_logic.reporting.report.model.aggregation.AggregationFunctionConfiguration;
import com.top_logic.reporting.report.model.aggregation.AggregationFunctionFactory;
import com.top_logic.reporting.report.model.aggregation.AverageFunction;
import com.top_logic.reporting.report.model.aggregation.LowerQuartil;
import com.top_logic.reporting.report.model.aggregation.MaxFunction;
import com.top_logic.reporting.report.model.aggregation.MedianFunction;
import com.top_logic.reporting.report.model.aggregation.MinFunction;
import com.top_logic.reporting.report.model.aggregation.UpperQuartil;
import com.top_logic.reporting.report.model.partition.TimeRangeFactory;
import com.top_logic.reporting.report.model.partition.function.PartitionFunctionFactory;
import com.top_logic.reporting.report.util.DateConstants;
import com.top_logic.reporting.report.util.ReportConstants;
import com.top_logic.reporting.report.util.ReportUtilities;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;

/**
 * The FlexibleReportingProducer represents the connection between a {@link RevisedReport} and a
 * JFreeChart chart. All necessary Configuration of the chart has to be done here.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public class FlexibleReportingProducer extends AbstractChartProducer implements ReportConstants {

	/** The res key for the "error message" if no chart data is available */
	private static final ResKey NO_CHART_DATA = I18NConstants.NO_CHART_DATA;

	private static final double MARGIN = 0.1;
	
	public static interface Config extends AbstractChartProducer.Config {

	}

	private String detailTableName;
	
	/**
	 * The maximum amount of x-axis values. If this number is exceeded, no axis
	 * lables will be drawn.
	 */
	public static final int MAX_AXIS_LABELS = 35;
	
	/** The maximum amount of value labels inside the chart. */
	public static final int MAX_LABELS = 40;
	
	public static FlexibleReportingProducer newFlexibleReportingProducer() {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.setImplementationClass(FlexibleReportingProducer.class);
		return (FlexibleReportingProducer) TypedConfigUtil.createInstance(config);
	}

	/**
	 * Creates a new {@link FlexibleReportingProducer}.
	 */
	public FlexibleReportingProducer(InstantiationContext context, Config someAttrs) {
		super(context, someAttrs);
	}

	@Override
	protected JFreeChart createChart(ChartContext aChartContext) {

	    // a little configuration of the chart
		PlotOrientation orientation = PlotOrientation.VERTICAL;
		
		if (aChartContext == null) {
		    return createEmptyChart();
		}
		
		RevisedReport       theReport = null;
		ReportConfiguration theConf   = aChartContext.getReportConfiguration();
		
		String              theType   = "";
		if (theConf != null) {
			// check whether the given ReportConfiguration is valid. If not so, create an empty chart
			if (!StringServices.isEmpty(RevisedReport.checkReportConfiguration(theConf))) {
				return createEmptyChart();
			}
			
			theType = theConf.getChartType();
			if (ReportConstants.REPORT_TYPE_BOXWHISKER_CHART.equals(theType)) {
				// initialize the proper aggregationfunctions for the box and whisker chart
				List theAggregationFunctions = new ArrayList();
				Class[] theBWFunctions = new Class[] {MinFunction.class, MaxFunction.class, AverageFunction.class, MedianFunction.class, UpperQuartil.class, LowerQuartil.class};
				try {
					for (int i = 0; i < theBWFunctions.length; i++) {
						AggregationFunctionConfiguration theAggrConf = AggregationFunctionFactory.getInstance().createAggregationFunctionConfiguration(theBWFunctions[i]);
						theAggrConf.setIgnoreNullValues(true);
						theAggrConf.setAttributePath(theConf.getAttribute());
//						theAggrConf.setAccessor(AccessorFactory.INSTANCE.getAccessor(theConf.getBusinessObjectProducer().getObjectType(), true));
						theAggregationFunctions.add(theAggrConf);
					}
					theConf.setAggregationConfigurations(theAggregationFunctions);
				}
				catch (ConfigurationException e) {
					throw new ConfigurationError("Invalid configuration for Box-Whisker-Chart detected.", e);
				}
			}
			try {
				theReport = ReportFactory.getInstance().getReport(theConf);
			}
			catch (ReportingException e) {
				return createEmptyChart(Resources.getInstance()
					.decodeMessageFromKeyWithEncodedArguments(e.getMessage()));
			}
		}

		// if the report is null or if nor business objects are present, an empty chart is generated.
		if (theReport == null || CollectionUtil.isEmptyOrNull(((ReportNode)theReport.getReportTree().getRoot()).getObjects())) {
			return createEmptyChart();
		}
		
		String theTitle      = theConf.getTitleLabel();
		String theXaxisLabel = theConf.getXAxisLabel();
		String theYaxisLabel = theConf.getYAxisLabel();
		
		boolean showLegend   = theConf.shouldShowLegend();
		boolean showTooltips = theConf.shouldShowToolTips();
		boolean showUrls     = theConf.shouldShowUrls();
		boolean useBusinessYear = useBusinessYear(theConf);
		
		this.detailTableName = getString(aChartContext, SearchReportDetailChartHandler.CHART_DETAIL_HANDLER, null);
		
		String thePfType = theReport.getPartitionFunction().getType();

		String theAttributeName = theConf.getAttribute();
		if (theAttributeName != null) {
			theAttributeName = Resources.getInstance().getString(ResKey.legacy(theAttributeName), theAttributeName);
		}
		
		Object theGranularity = theReport.getGranularity();
		
		JFreeChart theChart = null;
		
		if (ReportConstants.REPORT_TYPE_BAR_CHART.equals(theType)) {
			if(PartitionFunctionFactory.DATE.equals(thePfType) || PartitionFunctionFactory.PAYMENT.equals(thePfType)) {
				TimeSeriesCollection dataset = ReportUtilities.generateTimeSeriesCollectionFor(theReport);
				theChart = ChartFactory.createTimeSeriesChart(theTitle, theXaxisLabel, theYaxisLabel, dataset, showLegend, showTooltips, showUrls);
				configureXYPlot(theChart, theGranularity, dataset, false, theConf);
			}
			else {
				CategoryDataset dataset = ReportUtilities.generateCategoryDatasetFor(theReport);
				theChart = ChartFactory.createBarChart(theTitle, theXaxisLabel, theYaxisLabel, dataset, orientation, showLegend, showTooltips, showUrls);
				configureCategoryPlot(theChart, dataset, theConf);
			}
		}
		else if (ReportConstants.REPORT_TYPE_BOXWHISKER_CHART.equals(theType)) {
			ValueAxis xAxis;
			
			if (PartitionFunctionFactory.DATE.equals(thePfType)) {
				xAxis = configureDateAxis(new DateAxis(theXaxisLabel), theGranularity, false, theConf);
			}
			else {
				xAxis = new NumberAxis(theXaxisLabel);
			}
			
			NumberAxis yAxis = new NumberAxis(theYaxisLabel);
			yAxis.setUpperMargin(MARGIN);
			yAxis.setAutoRangeIncludesZero(false);
			XYBoxAndWhiskerRenderer theRenderer = new XYBoxAndWhiskerRenderer(10.0);
			String toolTipFormat = "{1}<br/>Mean: {2} min, Median: {3} min, Min: {4} min,<br/>Max: {5} min, Q1: {6} min, Q3: {7} min ";
			//TODO check theGranularity
			if (theGranularity instanceof Number) {
				theRenderer.setDefaultToolTipGenerator(new BoxAndWhiskerXYToolTipGenerator(
    											toolTipFormat,
    											DateConstants.getDateFormat(((Number)theGranularity).intValue(), useBusinessYear),
    											ReportUtilities.getNumberFormat()));
    		}
			theRenderer.setFillBox(true);
			BoxAndWhiskerXYDataset dataset = ReportUtilities.generateBoxAndWhiskerXYDatasetFor(theReport);
			XYPlot thePlot = new XYPlot(dataset, xAxis, yAxis, theRenderer);
			theChart = new JFreeChart(theTitle, null, thePlot, false);
		}
		else if(ReportConstants.REPORT_TYPE_WATERFALL_CHART.equals(theType)) {
			CategoryDataset dataset = ReportUtilities.generateWaterfallDatasetFor(theReport);
			theTitle = (String) CollectionUtil.getFirst(dataset.getRowKeys());
			theChart = ChartFactory.createWaterfallChart(theTitle, theXaxisLabel, theYaxisLabel, dataset, orientation, false, showTooltips, showUrls);
			configureCategoryPlot(theChart, dataset, theConf);
		}
		else if(ReportConstants.REPORT_TYPE_PIE_CHART.equals(theType)) {
		    PieDataset dataset = ReportUtilities.generatePieDatasetFor(theReport);
		    theChart = ChartFactory.createPieChart(theTitle, dataset, false, showTooltips, showUrls);
		    configurePiePlot(theChart, dataset, theConf);
		}
		// so far the only one left is a linechart
		else if (ReportConstants.REPORT_TYPE_LINE_CHART.equals(theType)){
			if(PartitionFunctionFactory.DATE.equals(thePfType)|| PartitionFunctionFactory.PAYMENT.equals(thePfType)) {
				TimeSeriesCollection dataset = ReportUtilities.generateTimeSeriesCollectionFor(theReport);
				theChart = ChartFactory.createTimeSeriesChart(theTitle, theXaxisLabel, theYaxisLabel, dataset, showLegend, showTooltips, showUrls);
				configureXYPlot(theChart, theGranularity, dataset, true, theConf);
			}
			else {
				CategoryDataset dataset = ReportUtilities.generateCategoryDatasetFor(theReport);
				theChart = ChartFactory.createLineChart(theTitle, theXaxisLabel, theYaxisLabel, dataset, orientation, showLegend, showTooltips, showUrls);
				configureCategoryPlot(theChart, dataset, theConf);
			}
		}
		else {
			throw new ReportingException(FlexibleReportingProducer.class, "chart " + theType + " not supported");
		}
		return theChart;
	}
	
	protected boolean useBusinessYear(ReportConfiguration report) {
	    return ReportUtilities.useBusinessYear(report);
	}
	
	private JFreeChart createEmptyChart() {
		String theMsg = Resources.getInstance().getString(NO_CHART_DATA);
	    return createEmptyChart(theMsg);
	}
	
	private JFreeChart createEmptyChart(String aMessage) {
	    JFreeChart theChart = ChartFactory.createPieChart(null, null, false, false, false);
        Plot       plot     = theChart.getPlot();
        
        plot.setNoDataMessage(aMessage);
        plot.setNoDataMessageFont(new Font("Arial", Font.BOLD, 14));
        plot.setNoDataMessagePaint(Color.red);
        
        return theChart;
	}
	
	private void configureXYPlot(JFreeChart aChart, Object aGranularity, TimeSeriesCollection aDataSet, boolean isLine, ReportConfiguration aConf) {
	    XYToolTipGenerator          theToolTipGenerator = new ReportingXYToolTipGenerator();
		XYPlot                      thePlot             = (XYPlot) aChart.getPlot();
		boolean                     middle              = true;
		boolean                     showPeaks           = aConf.isShowPeakValues();
		boolean                     checked             = aConf.shouldShowHighlightArea();
		boolean                     showUrls            = aConf.shouldShowUrls();
		
		thePlot.setRangeZeroBaselineVisible(true);
		
		if (isLine) {
			final XYLineAndShapeRenderer theRenderer = new XYLineAndShapeRenderer(true, true);
			
    		this.setSeriesColors(theRenderer, aDataSet, aConf);
			this.setDotsAndLineVisibility(theRenderer, aDataSet, aConf);

			theRenderer.setDefaultToolTipGenerator(theToolTipGenerator);
			
			if (showUrls && !StringServices.isEmpty(this.detailTableName)) {
				theRenderer.setURLGenerator(new ChartDetailXYURLGenerator(this.detailTableName));
			}
			
			if (showPeaks) {
				theRenderer.setDefaultItemLabelGenerator(new XYLabelGenerator());
				theRenderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12,
					TextAnchor.BASELINE_LEFT, TextAnchor.CENTER_LEFT, -Math.PI / 2));
				theRenderer.setDefaultItemLabelsVisible(true);
			}
			thePlot.setRenderer(theRenderer);
		}
		else {
			ExtendedClusteredXYBarRenderer theRenderer = new ExtendedClusteredXYBarRenderer(0.15, false);
			this.setSeriesColors(theRenderer, aDataSet, aConf);
			// setting tooltips, after destroying them by setting a new
			// renderer.
			theRenderer.setDefaultToolTipGenerator(theToolTipGenerator);
			theRenderer.setMaxBarWidth(0.15);
			theRenderer.setMinimumBarLength(2.0);
			
			if (showUrls && !StringServices.isEmpty(this.detailTableName)) {
				theRenderer.setURLGenerator(new ChartDetailXYURLGenerator(this.detailTableName));
			}
			
			if (showPeaks) {
				theRenderer.setDefaultItemLabelGenerator(new XYLabelGenerator());
				theRenderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12,
					TextAnchor.BASELINE_LEFT, TextAnchor.CENTER_LEFT, -Math.PI / 2));
				theRenderer.setDefaultItemLabelsVisible(true);
			}
			thePlot.setRenderer(theRenderer);
		}
		
		ValueAxis theValueAxis = thePlot.getRangeAxis();
		this.formatValueAxis(theValueAxis);
		
		if (checked) {
			IntervalMarker target = createIntervalMarker(aConf, theValueAxis);
			thePlot.addRangeMarker(target);
		}
		
		DateAxis theAxis =  (DateAxis) thePlot.getDomainAxis();
		// configure the Date Axis so that the granularity defines the 'minimum
		// ticks'
		ValueAxis a = configureDateAxis(theAxis, aGranularity, middle, aConf);
		thePlot.setDomainAxis(a);
	}

	private void configurePiePlot(JFreeChart aChart, PieDataset aDataSet, ReportConfiguration aConf) {
        PiePlot thePlot = (PiePlot) aChart.getPlot();

		thePlot.setLabelGenerator(new StandardPieSectionLabelGenerator(Resources.getInstance().getString(
			I18NConstants.PIE_CHART_LABEL)));
        thePlot.setLabelBackgroundPaint(new Color(220, 220, 220));

        thePlot.setToolTipGenerator(new ReportingPieToolTipGenerator());
        thePlot.setInteriorGap(0.15);

        this.setSeriesColors(thePlot, aDataSet, aConf);

        if (aConf.shouldShowUrls()) { 
            thePlot.setURLGenerator(new ChartDetailPieURLGenerator(this.detailTableName));
        }
    }
	   
	private void configureCategoryPlot(JFreeChart aChart, CategoryDataset aDataSet, ReportConfiguration aConf) {
		CategoryPlot         thePlot     = aChart.getCategoryPlot();
		CategoryItemRenderer theRenderer = thePlot.getRenderer();
		boolean              showUrls    = true;//aConf.shouldShowUrls();
		boolean              showPeaks   = aConf.isShowPeakValues();
		boolean              checked     = aConf.shouldShowHighlightArea();
		// Marker to make y-axis-zero-line visible
		Marker               baseline    = new ValueMarker(0.0D, new Color(0.8F, 0.8F, 0.8F, 0.5F), new BasicStroke(1.0F), new Color(0.85F, 0.85F, 0.95F, 0.5F), new BasicStroke(1.0F), 0.6F);
		
		thePlot.addRangeMarker(baseline, Layer.BACKGROUND);
		
		if (theRenderer instanceof BarRenderer) {
			((BarRenderer)theRenderer).setMinimumBarLength(2.0);
			((BarRenderer)theRenderer).setMaximumBarWidth(0.15);
		}
		
		ReportingCategoryToolTipGenerator theToolTipGenerator = new ReportingCategoryToolTipGenerator();
		theRenderer.setDefaultToolTipGenerator(theToolTipGenerator);
		
		this.setSeriesColors(theRenderer, aDataSet, aConf);
		
		if (theRenderer instanceof WaterfallBarRenderer) {
			setSeriesColor((WaterfallBarRenderer) theRenderer);
		}
		
		if (showUrls && !StringServices.isEmpty(this.detailTableName)) {
			theRenderer.setDefaultItemURLGenerator(new ChartDetailCategoryURLGenerator(this.detailTableName));
		}
		
		final CategoryAxis theDomainAxis = thePlot.getDomainAxis();
		
		if (aDataSet.getColumnCount() > MAX_AXIS_LABELS) {
			theDomainAxis.setVisible(false);
		}
		theDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0));
		
		if (showPeaks) {
			theRenderer.setDefaultItemLabelGenerator(new CategoryLabelGenerator());
			theRenderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12,
				TextAnchor.BASELINE_LEFT, TextAnchor.CENTER_LEFT, -Math.PI / 2));
		}
		theRenderer.setDefaultItemLabelsVisible(true);
		
		ValueAxis theValueAxis = thePlot.getRangeAxis();
		this.formatValueAxis(theValueAxis);
		
		if(checked) {
			IntervalMarker target = createIntervalMarker(aConf, theValueAxis);
			thePlot.addRangeMarker(target);
		}
	}

	/**
	 * Creates an {@link IntervalMarker} to highlight a specified area on the given
	 * {@link ValueAxis}. All necessary information for the marker needs to be configured in the
	 * given {@link ReportConfiguration}.
	 * 
	 * @param aConf        a {@link ReportConfiguration} with all necessary information
	 * @param aValueAxis   a {@link ValueAxis} to be used for the marker
	 * 
	 * @return an {@link IntervalMarker} with the given information, never <code>null</code>.
	 */
	private IntervalMarker createIntervalMarker(ReportConfiguration aConf, ValueAxis aValueAxis) {
		String intervalLabel = aConf.getHighlightAreaLabel();
		String fromVal       = aConf.getHighlightAreaFrom();
		String toVal         = aConf.getHighlightAreaTo();
		
		double upperBound    = aValueAxis.getUpperBound();
		double lowerBound    = aValueAxis.getLowerBound();
		Double from          = Utils.getDoubleValue(fromVal);
		Double to            = Utils.getDoubleValue(toVal);
		
		if (from == null) {
			if (to == null) {
				from = (upperBound - lowerBound) / 2;
				to = (upperBound - lowerBound) / 2;
			}
			else {
				from = to;
			}
		}
		else if (to == null) {
			to = from;
		}

		from = (from < lowerBound) ? lowerBound : from;
		to   = (to > upperBound)   ? upperBound : to;

		IntervalMarker target = new IntervalMarker(from, to);
		
		if (!StringServices.isEmpty(intervalLabel)) {
			target.setLabel(intervalLabel);
		}
		target.setLabelFont(new Font("SansSerif", Font.ITALIC, 11));
		target.setLabelAnchor(RectangleAnchor.LEFT);
		target.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
		target.setPaint(new Color(222, 222, 255, 128));
		
		return target;
	}

	/**
	 * Configures the {@link DateAxis} of this chart.
	 * 
	 * @param anAxis          the ValueAxis which is to be configured
	 * @param aGranularity    the granularity (defines the minimum tickUnits)
	 */
	private ValueAxis configureDateAxis(ValueAxis anAxis, Object aGranularity, boolean middle, ReportConfiguration aConf) {

	    boolean useBusinessYear = useBusinessYear(aConf);
	    
		PeriodAxis theAxis;
		if (anAxis instanceof PeriodAxis) {
			theAxis = (PeriodAxis) anAxis;
		}
		
		if (aGranularity instanceof Double) {
			TickUnits theStandardUnits = new TickUnits();
			if (middle) {
				((DateAxis)anAxis).setTickMarkPosition(DateTickMarkPosition.MIDDLE);
			}
			else {
				((DateAxis)anAxis).setTickMarkPosition(DateTickMarkPosition.START);
			}
			anAxis.setVerticalTickLabels(true);
			int theGranularity = ((Number)aGranularity).intValue();
			if (theGranularity <= DateConstants.YEAR) {
			    theStandardUnits.add(new DateTickUnit(DateTickUnitType.YEAR, 1, DateConstants.getDateFormat(DateConstants.YEAR, useBusinessYear)));
			}
			if (theGranularity <= DateConstants.MONTH) {
				theStandardUnits.add(new DateTickUnit(DateTickUnitType.MONTH, 1, DateConstants.getDateFormat(DateConstants.MONTH, useBusinessYear)));
			}
			if (theGranularity <= DateConstants.WEEK) {
				theStandardUnits.add(new DateTickUnit(DateTickUnitType.DAY, 7, DateConstants.getDateFormat(DateConstants.WEEK, useBusinessYear)));
			}
			if (theGranularity <= DateConstants.DAY) {
				theStandardUnits.add(new DateTickUnit(DateTickUnitType.DAY, 1, DateConstants.getDateFormat(DateConstants.DAY, useBusinessYear)));
			}
			if (theGranularity <= DateConstants.HOUR) {
				theStandardUnits.add(new DateTickUnit(DateTickUnitType.HOUR, 1, DateConstants.getDateFormat(DateConstants.HOUR, useBusinessYear)));
			}
			anAxis.setStandardTickUnits(theStandardUnits);
			return anAxis;
		}
		else if (aGranularity instanceof Long) {
			Long theGranularity = (Long)aGranularity;
			if (TimeRangeFactory.YEAR_INT.equals(theGranularity)) {
			    Class<? extends RegularTimePeriod> year = ReportUtilities.getYearPeriodClass(useBusinessYear);
			    return createPeriodAxisWithLabelInfo(year, year, year);
			}
			else if (TimeRangeFactory.HALFYEAR_INT.equals(theGranularity)) {
			    Class<? extends RegularTimePeriod> halfYear = ReportUtilities.getHalfYearPeriodClass(useBusinessYear);
			    return createPeriodAxisWithLabelInfo(halfYear, halfYear, ReportUtilities.getYearPeriodClass(useBusinessYear));
	        }
			else if (TimeRangeFactory.QUARTER_INT.equals(theGranularity)) {
				new PeriodAxis("");
				Class<? extends RegularTimePeriod> quarter = ReportUtilities.getQuarterPeriodClass(useBusinessYear);
				return createPeriodAxisWithLabelInfo(quarter, quarter, ReportUtilities.getYearPeriodClass(useBusinessYear));
			}
			else if (TimeRangeFactory.MONTH_INT.equals(theGranularity)) {
			    return createPeriodAxisWithLabelInfo(Month.class, Month.class, ReportUtilities.getYearPeriodClass(useBusinessYear));
			}
			else if (TimeRangeFactory.WEEK_INT.equals(theGranularity)) {
			    return createPeriodAxisWithLabelInfo(Week.class, Week.class, Month.class);
			}
			else if (TimeRangeFactory.DAY_INT.equals(theGranularity)) {
			    return createPeriodAxisWithLabelInfo(Day.class, Day.class, Week.class);
			}
			else if (TimeRangeFactory.HOUR_INT.equals(theGranularity)) {
			    return createPeriodAxisWithLabelInfo(Hour.class, Hour.class, Day.class);
			}
			else {
			    throw new IllegalArgumentException("Granularity " + aGranularity + " not supported for a date axis");
			}
		}
		else {
		    throw new IllegalArgumentException("Granularity " + aGranularity + " not supported");
		}
	}
	
	protected PeriodAxis createPeriodAxisWithLabelInfo(Class<? extends RegularTimePeriod> tickUnit, Class<? extends RegularTimePeriod> axis1, Class<? extends RegularTimePeriod> axis2) {
	    PeriodAxis axis = new PeriodAxis("");
	    axis.setMajorTickTimePeriodClass(tickUnit);
	    axis.setAutoRangeTimePeriodClass(tickUnit);
	    PeriodAxisLabelInfo[] info = new PeriodAxisLabelInfo[2];
	    info[0] = new PeriodAxisLabelInfo(axis1, DateConstants.getDateFormat(axis1));
	    info[1] = new PeriodAxisLabelInfo(axis2, DateConstants.getDateFormat(axis2));
	    axis.setLabelInfo(info);
	    return axis;
	}
	
	private void formatValueAxis(ValueAxis anAxis) {
		anAxis.setUpperMargin(MARGIN);
		anAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		double theLower = anAxis.getLowerBound();
		double theMin = anAxis.getUpperBound() / 50 * -1;
		anAxis.setLowerBound((theLower < theMin) ? theLower : theMin);

		double theUpper = anAxis.getUpperBound();
		double theMax = anAxis.getLowerBound() / 50 * -1;
		anAxis.setUpperBound((theUpper > theMax) ? theUpper : theMax);
	}

	private Color getSeriesColor(int aRow, ReportConfiguration aConf) {
		if (aRow < aConf.getAggregationConfigurations().size()) {
			return ((AggregationFunctionConfiguration) aConf.getAggregationConfigurations().get(aRow)).getColor();
		}
		else return null;
	}
	
	private void setSeriesColor(WaterfallBarRenderer aRenderer) {
		Color thePaint = ChartConfigurationFieldHelper.getGradientColor(0);
		aRenderer.setFirstBarPaint(thePaint);
		aRenderer.setPositiveBarPaint(thePaint);
		aRenderer.setNegativeBarPaint(ChartConfigurationFieldHelper.getGradientColor(1));
		aRenderer.setLastBarPaint(ChartConfigurationFieldHelper.getGradientColor(4));
	}
	
	private void setSeriesColors(AbstractXYItemRenderer aRenderer, TimeSeriesCollection aDataSet, ReportConfiguration aConf) {
		int numCols = aDataSet.getSeriesCount();
		for(int i = 0; i < numCols; i++) {
			Color theCol = (this.getSeriesColor(i, aConf));
			if (theCol != null) {
				aRenderer.setSeriesPaint(i, FlexibleReportingProducer.getGradientColor(theCol));
			}
		}
	}
	
	//TODO adapt to new report structure
	private void setSeriesColors(PiePlot aPlot, PieDataset aDataSet, ReportConfiguration aConf) {
	    int numCols = aDataSet.getItemCount();
	    
	    for(int i = 0; i < numCols; i++) {
			Color theCol = ChartConfigurationFieldHelper.getGradientColor(i % 5);
	        if (theCol != null) {
	            aPlot.setSectionPaint(aDataSet.getKey(i), FlexibleReportingProducer.getGradientColor(theCol));
	        }
	    }
	}

	//TODO adapt to new report structure
	private void setSeriesColors(CategoryItemRenderer aRenderer, CategoryDataset aDataSet, ReportConfiguration aConf) {
		int numCols = aDataSet.getRowCount();

		for(int i = 0; i < numCols; i++) {
			Color theCol = (this.getSeriesColor(i, aConf));
			if (theCol != null) {
				aRenderer.setSeriesPaint(i, FlexibleReportingProducer.getGradientColor(theCol));
			}
		}
	}

	private void setDotsAndLineVisibility(XYLineAndShapeRenderer aRenderer, TimeSeriesCollection aDataSet, ReportConfiguration aConf) {
	    // assume a chart has attribute definitions
		int numCols  = aDataSet.getSeriesCount();
		int maxCount = 0;
		boolean showLines = true;
		for(int i = 0; i < numCols; i++) {
			boolean isVisible = ((AggregationFunctionConfiguration) aConf.getAggregationConfigurations().get(i)).isLineVisible();
			maxCount = Math.max(maxCount, aDataSet.getItemCount(i));
			// render lines or dots, not both
			aRenderer.setSeriesLinesVisible(i,   isVisible);
			//aRenderer.setSeriesShapesVisible(i, !isVisible);
			showLines &= isVisible;
		}

		// do not draw dots if too much dots would be drawn and if 
		// dots were not enforced by at least one AggregationFunctionConfiguration
		// 32 is obviously enough for a day-per-month chart to show dots and lines
		if (maxCount > 32) {
			aRenderer.setDefaultShapesVisible(!showLines);
		}
		else {
			aRenderer.setDefaultShapesVisible(true);
		}
    }
	
	/**
	 * Generates a {@link GradientPaint} for the given {@link Color}. The given
	 * color will be used as the base for the gradient.
	 * 
	 * @param aColor   the base color.
	 * 
	 * @return a {@link GradientPaint}
	 */
	public static GradientPaint getGradientColor(Color aColor) {
		int r = aColor.getRed();
		int g = aColor.getGreen();
		int b = aColor.getBlue();
		float[] hsb = Color.RGBtoHSB(r, g, b, null);
		float brightness = hsb[2];
		if(brightness >= 0.75f) {
			hsb[2] = brightness * 0.75f;			
		}
		else if(brightness > 0.4 && brightness < 0.75) {
			hsb[2] = brightness * 0.5f;			
		}
		else {
			hsb[2] += 0.20f;
			return new GradientPaint(0.0f, 0.0f, Color.getHSBColor(hsb[0], hsb[1], hsb[2]), 0.0f, 0.0f, aColor);
		}
		return new GradientPaint(0.0f, 0.0f, aColor, 0.0f, 0.0f, Color.getHSBColor(hsb[0], hsb[1], hsb[2]));
	}

	@Override
	public boolean supports(ChartContext aChartContext) {
		return true;
	}

    /**
	 * Sets the color for the given renderer and dataset. Each series gets a
	 * different color based on the current theme definition.
	 * 
	 * @param aRenderer   the renderer for the chart
	 * @param aDataSet    the dataset to configure.
	 */
    public static void setThemeColors(CategoryItemRenderer aRenderer, CategoryDataset aDataSet) {
        int numCols = aDataSet.getRowCount();

        for (int i = 0; i < numCols; i++) {
			Color theColor = ChartConfigurationFieldHelper.getGradientColor(i % 5);

            aRenderer.setSeriesPaint(i, theColor);
        }
    }
    
    /**
	 * Sets the color for the given renderer and dataset. Each series gets a
	 * different color based on the current theme definition.
	 * 
	 * @param aRenderer   the renderer for the chart
	 * @param aDataSet    the dataset to configure.
	 */
    public static void setThemeColors(AbstractXYItemRenderer aRenderer, TimeSeriesCollection aDataSet) {
        int theSize = aDataSet.getSeriesCount();

        for (int i = 0; i < theSize; i++) {
			Color theColor = ChartConfigurationFieldHelper.getGradientColor(i % 5);
            
            aRenderer.setSeriesPaint(i, theColor);
        }
    }

	private static class CategoryLabelGenerator extends AbstractCategoryItemLabelGenerator implements CategoryItemLabelGenerator {

		public CategoryLabelGenerator() {
            super("", NumberFormat.getInstance());
        }
		
		@Override
		public String generateLabel(CategoryDataset aDataset, int aSeries, int aCategory) {
			String result = null;
			int max = aDataset.getColumnCount();
			int last = (aCategory == 0) ? aCategory : aCategory-1;
			int next = (aCategory == max-1) ? aCategory : aCategory + 1;
			double prevValue = aDataset.getValue(aSeries, last).doubleValue();
			double nextValue = aDataset.getValue(aSeries, next).doubleValue();
            Number value = aDataset.getValue(aSeries, aCategory);
            if (value != null) {
                double v = value.doubleValue();
                if(max <= MAX_LABELS) {
                	result = ReportUtilities.getNumberFormat().format(value);//.toString();  // could apply formatting here
                }
                else if (prevValue <= v && v > nextValue) {
                    result = ReportUtilities.getNumberFormat().format(value);//value.toString();  // could apply formatting here
                }
            }
            return result;
		}
	}
	
	/*package protected*/ static class XYLabelGenerator extends AbstractXYItemLabelGenerator implements XYItemLabelGenerator {
		
		@Override
		public String generateLabel(XYDataset aDataset, int aSeries, int aCategory) {
			String result = "";
			int max = aDataset.getItemCount(aSeries);
			int last = (aCategory == 0) ? aCategory : aCategory-1;
			int next = (aCategory == max-1) ? aCategory : aCategory + 1;
			double prevValue = aDataset.getYValue(aSeries, last);
			double nextValue = aDataset.getYValue(aSeries, next);
            double value = aDataset.getYValue(aSeries, aCategory);
            if(max <= MAX_LABELS) {
            	result += ReportUtilities.getNumberFormat().format(value);//Double.valueOf(value).intValue();  // could apply formatting here
            }
            else if (prevValue <= value && value > nextValue) {
            	result += ReportUtilities.getNumberFormat().format(value);//Double.valueOf(value).intValue();   // could apply formatting here
            }
            return result;
		}
	}
}
