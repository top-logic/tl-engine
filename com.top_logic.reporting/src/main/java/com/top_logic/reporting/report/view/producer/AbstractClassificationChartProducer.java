/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.producer;

import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.util.TableOrder;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.CategoryToPieDataset;
import org.jfree.data.general.PieDataset;

import com.top_logic.base.chart.ChartChoice;
import com.top_logic.base.chart.configurator.BarChartConfigurator;
import com.top_logic.base.chart.util.ChartType;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.gui.layout.list.FastListElementLabelProvider;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.reporting.report.control.producer.AbstractChartProducer;
import com.top_logic.reporting.report.control.producer.ChartContext;
import com.top_logic.reporting.report.control.producer.ChartProducer;
import com.top_logic.reporting.report.model.DataSet;
import com.top_logic.reporting.report.view.component.ChartData;
import com.top_logic.util.Resources;

/**
 * Abstract approach to generate classification charts.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractClassificationChartProducer implements ExtendedChartProducer, ChartProducer, CategoryToolTipGenerator, ChartChoice {

    /** I18N suffix for the personal configuration. */
    public static final String CHART_TYPE = "chartType";

    /** The inner model of this chart producer. */
    protected ClassificationModel model;

    /** The currently selected chart type. */
	private ChartType selectedChartType;

    /** 
     * Create the generator for the needed data set.
     * 
     * @param    aContext    The context containing the needed information for the generator, must not be <code>null</code>.
     * @return   The requested data set generator, never <code>null</code>.
     */
    protected abstract ClassificationDatasetGenerator createGenerator(ChartContext aContext);

    /**
	 * Check, if the given business object is supported by this producer.
	 * 
	 * @param anObject
	 *        The object to be checked, may be <code>null</code>.
	 * @return <code>true</code>, when the component can handle such kinds of business objects.
	 */
    @Override
	public boolean supportsObject(Object anObject) {
        return anObject instanceof ClassificationModel;
    }

    /**
	 * Produce the {@link ChartData} by calling the
	 * {@link #fillChartData(ChartContext, ChartData, CategoryURLGenerator)} method.
	 * 
	 * @param aChartContext
	 *        The chart context contains all necessary information to produce the chart, must NOT be
	 *        <code>null</code>.
	 * @param anURLGenerator
	 *        The generator for URLs, may be <code>null</code>.
	 * @return The chart data object, never <code>null</code>.
	 */
    @Override
	public ChartData produceChartData(ChartContext aChartContext, CategoryURLGenerator anURLGenerator) {
        ChartData theResult = new ChartData();

        this.fillChartData(aChartContext, theResult, anURLGenerator);

        AbstractChartProducer.adaptChart(aChartContext, theResult.getChart());

        return theResult;
    }

    /**
	 * Support the context, when that held model is supported by {@link #supportsObject(Object)}.
	 * 
	 * @param aContext
	 *        The context to be checked, must not be <code>null</code>.
	 * @return The result from {@link #supportsObject(Object)}.
	 */
    @Override
	public boolean supports(ChartContext aContext) {
        return this.supportsObject(aContext.getModel());
    }

    @Override
	public JFreeChart produceChart(ChartContext aContext) {
        return this.produceChartData(aContext, null).getChart();
    }

    @Override
	public String generateToolTip(CategoryDataset aDataset, int aRow, int aColumn) {
        return Resources.getInstance().getMessage(this.getTooltipKey(), this.getTooltipValues(aDataset, aRow, aColumn));
    }

    @Override
	public ChartType getSelection() {
        if (this.selectedChartType == null) {
            this.selectedChartType = this.loadChartTypeFromPersonalConfig();

            if (this.selectedChartType == null) {
                this.selectedChartType = this.getSupportedChartTypes().get(0);
            }
        }

        return this.selectedChartType;
    }

    @Override
	public boolean setSelection(ChartType aChartType) {
        return this.setSelection(aChartType, true);
    }

    @Override
	public boolean isChartTypeSupported(ChartType aChartType) {
        return this.getSupportedChartTypes().contains(aChartType);
    }

    /** 
     * Fill the given chart data object with values from the producers.
     * 
     * @param    aContext          The chart context contains all necessary information to produce the chart, must NOT be <code>null</code>.
     * @param    aResult           The chart data object to be filled, must not be <code>null</code>.
     * @param    anURLGenerator    The generator for URLs, may be <code>null</code>.
     */
    protected void fillChartData(ChartContext aContext, ChartData aResult, CategoryURLGenerator anURLGenerator) {
        this.model = this.getClassificationModel(aContext);

        ClassificationDatasetGenerator theGenerator = this.createGenerator(aContext);
        CategoryDataset                theDataset   = (CategoryDataset) theGenerator.createDataset();

		if (ChartType.PIE_CHART.equals(this.getSelection())) {
            PieURLGenerator theURLGenerator = (PieURLGenerator) ((anURLGenerator instanceof PieURLGenerator) ? anURLGenerator : null);

            aResult.setChart(this.createPieChart(theDataset, theURLGenerator));
        } 
        else {
            aResult.setChart(this.createBarChart(theDataset, anURLGenerator));
        }

        this.getCalculator(aContext).reset();

        this.mapItems(theGenerator.getClassifications(), aResult);
    }

    /** 
     * Map the given items from the map into the given {@link ChartData} object.
     * 
     * @param    aMap       The map to get the values from, must not be <code>null</code>.
     * @param    aResult    The chart data object to be filled, must not be <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    protected void mapItems(Map<String, List<Object>> aMap, ChartData aResult) {
        if (aMap != null) {
            List<String> theList     = this.getFastListElementNames();
            int          theCategory = 0;

            for (String theString : aMap.keySet()) {
                int theSeries = theList.indexOf(theString);
    
                if (theSeries < 0) {
                    // Handle the "unknown" column
                    theSeries = theList.size();
                }
    
                aResult.addItems(theSeries, theCategory, aMap.get(theString));
            }
        }
    }

    /** 
     * Create the bar chart out of the given parameters.
     * 
     * @param    aDataset          The data set to create the chart from, must not be <code>null</code>.
     * @param    anURLGenerator    The URL generator to be used, may be <code>null</code>.
     * @return   The requested bar chart object, never <code>null</code>.
     * @see      #adjustConfigurator(CategoryDataset, BarChartConfigurator)
     */
    protected JFreeChart createBarChart(CategoryDataset aDataset, CategoryURLGenerator anURLGenerator) {
		BarChartConfigurator theConfigurator = this.createChartConfigurator(aDataset);
        final String         theAxisLabel    = this.getRangeAxisLabel();

        theConfigurator.setDefaultValues();
        theConfigurator.useGradientPaint();
        theConfigurator.setItemLabelsVisible(true);
        theConfigurator.setRangeAxisLabel(theAxisLabel);
        theConfigurator.setDomainAxisLabel(this.getDomainAxisLabel());
        theConfigurator.setURLGenerator(anURLGenerator);
        theConfigurator.setToolTipGenerator(this);

        this.adjustConfigurator(aDataset, theConfigurator);

        return theConfigurator.getChart();
    }

	/**
	 * Create the bar chart configuration for producing a JFreeChart.
	 * 
	 * @param aDataset
	 *        The data set to be used for generating the chart, must not be <code>null</code>.
	 * @return The requested configuration, never <code>null</code>.
	 */
	protected BarChartConfigurator createChartConfigurator(CategoryDataset aDataset) {
		return new BarChartConfigurator(LEGEND, aDataset);
	}

    /** 
     * Create the pie chart out of the given parameters.
     * 
     * @param    aDataset          The data set to create the chart from, must not be <code>null</code>.
     * @param    anURLGenerator    The URL generator to be used, may be <code>null</code>.
     * @return   The requested pie chart object, never <code>null</code>.
     */
    protected JFreeChart createPieChart(CategoryDataset aDataset, PieURLGenerator anURLGenerator) {
        CategoryToPieDataset thePieDataset = new CategoryToPieDataset(aDataset, TableOrder.BY_COLUMN, 0);
        JFreeChart           theChart      = ChartFactory.createPieChart("", thePieDataset, !LEGEND, TOOLTIPS, URLS);
        PiePlot              thePiePlot    = (PiePlot) theChart.getPlot();

        thePiePlot.setURLGenerator(anURLGenerator);
        thePiePlot.setLabelGenerator(new PieSectionLabelGenerator() {

            @Override
			public String generateSectionLabel(PieDataset aDataset, Comparable aKey) {
                return aKey.toString();
            }
        
            @Override
			public AttributedString generateAttributedSectionLabel(PieDataset aDataset, Comparable aKey) {
                return new AttributedString(aKey.toString());
            }
        });

        return theChart;
    }

    /** 
     * Adjust the configurator before the {@link #createBarChart(CategoryDataset, CategoryURLGenerator)}
     * method returns.
     * 
     * In this implementation the range for the chart will be changed to start at 0.
     * 
     * @param    aDataset         The data set which has been the source for the configurator, must not be <code>null</code>.
     * @param    aConfigurator    The configurator to be adjusted, must not be <code>null</code>.
     * @see      #createBarChart(CategoryDataset, CategoryURLGenerator)
     */
    protected void adjustConfigurator(CategoryDataset aDataset, BarChartConfigurator aConfigurator) {
        // No negative values in a classification chart.
        Range theRange = aConfigurator.computeRange();

        if (theRange.getLowerBound() >= 0) { 
            aConfigurator.setRange(new Range(0.0d, theRange.getUpperBound()));
        }
    }

    /** 
     * Return the calculator out of information in the given chart context.
     * 
     * @param     aContext    The context containing the requested calculator, must not be <code>null</code>.
     * @return    Returns the requested calculator, never <code>null</code>.
     */
    public CollectionToNumberCalculator getCalculator(ChartContext aContext) {
        ClassificationModel theModel = this.getClassificationModel(aContext);

        return (theModel != null) ? theModel.getCalculator() : new CollectionSizeCalculator();
    }

    /** 
     * Return the classification model out of the given chart context.
     * 
     * @param    aContext    The chart context containing the model, must not be <code>null</code>.
     * @return   The requested classification model, may be <code>null</code>.
     */
    protected ClassificationModel getClassificationModel(ChartContext aContext) {
        Object theModel = aContext.getModel();

        return (theModel instanceof ClassificationModel) ? ((ClassificationModel) theModel) : null;
    }

    /** 
     * Return the I18N code for the generated tool tips.
     * 
     * @return    The requested I18N code.
     */
    protected ResKey getTooltipKey() {
		return I18NConstants.REPORTING_GRAPHICS_CHART_TOOLTIP;
    }

    /** 
     * Return the values to be used in the generated tool tips.
     * 
     * @param    aDataset    The {@link DataSet} to get some information from, must not be <code>null</code>.
     * @param    aRow        The requested row.
     * @param    aColumn     The requested column.
     * @return   The tool tip values, never <code>null</code>.
     */
    protected Object[] getTooltipValues(CategoryDataset aDataset, int aRow, int aColumn) {
        Comparable theRowKey    = aDataset.getRowKey(aRow);
        Comparable theColumnKey = aDataset.getColumnKey(aColumn);
        Number     theCount     = aDataset.getValue(theRowKey, theColumnKey);

        return new Object[] {theRowKey, this.getRangeAxisLabel(), theCount};
    }

    /**
	 * Return the fast list to be used for clustering the data.
	 * 
	 * @return The requested fast list out of the defined attribute.
	 * @see #getMetaAttribute()
	 */
    protected FastList getClassificationList() {
        return AttributeOperations.getClassificationList(this.getMetaAttribute());
    }

    /** 
     * Return the meta attribute to be used for clustering the data.
     * 
     * @return    The requested meta attribute, never <code>null</code>.
     */
    protected TLStructuredTypePart getMetaAttribute() {
		return this.model.getMetaAttribute();
    }

    /** 
     * Return the I18N versions of the fast list elements used for clustering the data.
     * 
     * @return    The requested I18N names of the elements.
     * @see       #getClassificationList()
     */
    protected List<String> getFastListElementNames() {
        List<String> theList = new ArrayList<>();

		{
            List<FastListElement> theElements = this.getClassificationList().elements();
            Resources             theRes      = Resources.getInstance();

            for (FastListElement theFastListElement : theElements) {
				theList.add(theRes.getString(FastListElementLabelProvider.labelKey(theFastListElement)));
            }
        }

        return theList;
    }

    /** 
     * Set the given chart type as currently selected.
     * 
     * @param    aChartType    The chart type to be used now, may be <code>null</code>.
     * @param    shallStore    Flag, if this new type has to be stored in the personal configuration.
     * @return   Flag, if switching the type succeeds.
     */
	protected boolean setSelection(ChartType aChartType, boolean shallStore) {
        if (!this.isChartTypeSupported(aChartType) || aChartType.equals(this.selectedChartType)) {
            return false;
        }
        else {
            this.selectedChartType = aChartType;

            if (shallStore) {
                this.storeChartTypeInPersonalConfig(this.selectedChartType);
            }
            
            return true;
        }
    }

    /**
     * This method returns the chart type from the {@link PersonalConfiguration}
     * of the current person or <code>null</code>.
     * 
     * @return    The chart type to be used, may be <code>null</code>.
     */
	protected ChartType loadChartTypeFromPersonalConfig() {
		PersonalConfiguration personalConfiguration = PersonalConfiguration.getPersonalConfiguration();
		if (personalConfiguration == null) {
			return null;
		}
		return (ChartType) personalConfiguration.getValue(this.getPersonalConfigKey());
    }
    
    /**
     * This method stores the current visible chart type for the person into its
     * {@link PersonalConfiguration}.
     * 
     * @param    aChartType    A chart type, must not be <code>null</code>.
     */
	protected void storeChartTypeInPersonalConfig(ChartType aChartType) {
		PersonalConfiguration personalConfiguration = PersonalConfiguration.getPersonalConfiguration();
		if (personalConfiguration != null) {
			personalConfiguration.setValue(this.getPersonalConfigKey(), aChartType);
        }
    }

    private String getPersonalConfigKey() {
        return this.getClass().getCanonicalName() + '.' + CHART_TYPE;
    }

    private String getRangeAxisLabel() {
        return this.model.getCalculator().getYAxisLabel();
    }

    private String getDomainAxisLabel() {
        CollectionToNumberCalculator theCalculator = this.model.getCalculator();

        return theCalculator.showTotalValue() ? theCalculator.getXAxisLabel() : null;
    }
}

