/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.producer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartColor;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.top_logic.base.chart.util.ChartType;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.layout.list.FastListElementLabelProvider;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.reporting.chart.renderer.TemplateRenderer;
import com.top_logic.reporting.report.control.producer.AbstractChartProducer;
import com.top_logic.reporting.report.control.producer.ChartContext;
import com.top_logic.reporting.report.control.producer.ChartProducer;
import com.top_logic.reporting.report.model.DataSet;
import com.top_logic.reporting.report.view.component.ChartData;
import com.top_logic.util.Resources;

/**
 * Abstract producer for matrix charts.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractMatrixChartProducer implements ExtendedChartProducer, ChartProducer, CategoryToolTipGenerator {

	public static final List<ChartType> MATRIX_CHART_TYPE = Collections.singletonList(ChartType.MATRIX_CHART);

    /** 
     * Return the values to be used by this chart producer.
     * 
     * @param    aContext    The context holding information about the objects to return, must not be <code>null</code>. 
     * @return   The requested collection of objects, never <code>null</code>.
     */
    protected abstract Collection<Object> getValues(ChartContext aContext);

    /** 
     * The name of the meta element to get the classifications from.
     * 
     * @return    The name of the requested meta element, never <code>null</code>.
     */
    protected abstract String getQualifiedTypeNameDotted();

    /** 
     * Return the name of the meta attribute to be used on the X axis.
     * 
     * @return    The requested name of the meta attribute.
     */
    protected abstract String getMetaAttribute4XAxis();

    /** 
     * Return the name of the meta attribute to be used on the Y axis.
     * 
     * @return    The requested name of the meta attribute.
     */
    protected abstract String getMetaAttribute4YAxis();

    @Override
	public ChartData produceChartData(ChartContext aChartContext, CategoryURLGenerator anURLGenerator) {
        ChartData theResult = new ChartData();

        this.fillChartData(aChartContext, theResult, anURLGenerator);

        return theResult;
    }

    @Override
	public JFreeChart produceChart(ChartContext aChartContext) {
        return this.produceChartData(aChartContext, null).getChart();
    }

    @Override
	public boolean supports(ChartContext aContext) {
        return this.supportsObject(aContext.getModel());
    }

    @Override
	public List<ChartType> getSupportedChartTypes() {
        return AbstractMatrixChartProducer.MATRIX_CHART_TYPE;
    }

    @Override
	public String generateToolTip(CategoryDataset aDataset, int aRow, int aColumn) {
        return Resources.getInstance().getMessage(this.getTooltipKey(), this.getTooltipValues(aDataset, aRow, aColumn));
    }

    /** 
     * Fill the given chart data object out of the given chart context.
     * 
     * @param aChartContext     The chart context holding the information to get the values, must not be <code>null</code>.
     * @param aChartData        The chart data object to be filled, must not be <code>null</code>.
     * @param anURLGenerator    The URL generator to be used in here, may be <code>null</code>.
     */
    protected void fillChartData(ChartContext aChartContext, ChartData aChartData, CategoryURLGenerator anURLGenerator) {
        Resources        theRes          = Resources.getInstance();
        List<String>     theRowNames     = this.getLocalizedEntries(theRes, this.getRowList());
        List<String>     theColumnNames  = this.getLocalizedEntries(theRes, this.getColumnList());
		SymbolAxis theRangeAxis =
			new SymbolAxis(theRes.getString(this.getRowAxisNameKey()), theRowNames.toArray(new String[0]));
		CategoryAxis theCategoryAxis = new CategoryAxis(theRes.getString(this.getColumnAxisNameKey()));
        CategoryMatrix   theMatrix       = this.createCategoryMatrix(theRowNames, theColumnNames, aChartContext);
        CategoryDataset  theDataset      = this.createDataSetFromMatrix(theMatrix, aChartData);

        theRangeAxis.setGridBandsVisible(true);

        TemplateRenderer theRenderer = this.getRenderer(aChartContext, aChartData);
		theRenderer.setDefaultToolTipGenerator(this);
        if (anURLGenerator != null) { 
			theRenderer.setDefaultItemURLGenerator(anURLGenerator);
        }

        CategoryPlot thePlot = new CategoryPlot(theDataset, theCategoryAxis, theRangeAxis, theRenderer);

        thePlot.setRangeGridlinesVisible(true);
        thePlot.setDomainGridlinesVisible(true);
        thePlot.setDomainGridlinePaint(ChartColor.DARK_GRAY);

        JFreeChart theChart = this.customizeChart(new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, thePlot, false));

        AbstractChartProducer.adaptChart(aChartContext, theChart, true);

        aChartData.setChart(theChart);
    }

    /** 
     * Return the requested axis value from the given object.
     * 
     * This implementation will try to use the {@link Wrapper#getValue(String)} method from the given object.
     * 
     * @param    anObject    The object to get the value from, must not be <code>null</code>.
     * @param    aString     The requested axis name (took from {@link #getMetaAttribute4XAxis()} and {@link #getMetaAttribute4YAxis()}). 
     * @return   The requested value as {@link FastListElement} or <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    protected FastListElement getFastListElement(Object anObject, String aString) {
        if (anObject instanceof Wrapper) { 
            return (FastListElement)CollectionUtil.getSingleValueFromCollection((Collection) ((Wrapper) anObject).getValue(aString));
        }
        else {
            return null;
        }
    }

    /** 
     * Return the I18N code for the generated tool tips.
     * 
     * @return    The requested I18N code.
     */
    protected ResKey getTooltipKey() {
		return I18NConstants.MATRIX_GRAPHICS_CHART_TOOLTIP;
    }

    /** 
     * Return the template renderer for the bubbles and lines in the chart.
     * @return    The requested renderer, never <code>null</code>.
     */
    protected TemplateRenderer getRenderer(ChartContext aChartContext, ChartData aChartData) {
    	return new MatrixTemplateRenderer();
    }

    /** 
     * Create the category matrix out of the given row and column keys by using the values from {@link #getValues(ChartContext)}.
     * 
     * @param    aRowNames       The name of the supported rows, never <code>null</code>.
     * @param    aColumnNames    The name of the supported columns, never <code>null</code>.
     * @param    aContext       The chart context to be used for getting values, must not be <code>null</code>.
     * @return   The requested matrix, never <code>null</code>.
     */
    protected CategoryMatrix createCategoryMatrix(List<String> aRowNames, List<String> aColumnNames, ChartContext aContext) {
        Resources              theRes       = Resources.getInstance();
        CategoryMatrix<Object> theResult    = new CategoryMatrix<>(aRowNames, aColumnNames);
        String                 theXAxisName = this.getMetaAttribute4XAxis();
        String                 theYAxisName = this.getMetaAttribute4YAxis();

        for (Object theObject : this.getValues(aContext)) {
            FastListElement theXAxis = this.getFastListElement(theObject, theXAxisName);
            FastListElement theYAxis = this.getFastListElement(theObject, theYAxisName);

            if ((theXAxis != null) && (theYAxis != null)) { 
				theResult.addItem(
					FastListElementLabelProvider.INSTANCE.getLabel(theXAxis),
					FastListElementLabelProvider.INSTANCE.getLabel(theYAxis), theObject);
            }
        }

        return theResult;
    }

	protected String getRowListName() {
		return this.getRowAxisNameKey().getKey();
    }

	protected ResKey getRowAxisNameKey() {
		return TLModelNamingConvention.modelPartNameKey(getQualifiedTypeNameDotted())
			.suffix(this.getMetaAttribute4XAxis());
    }

    protected String getColumnListName() {
		return this.getColumnAxisNameKey().getKey();
    }

    protected ResKey getColumnAxisNameKey() {
		return TLModelNamingConvention.modelPartNameKey(getQualifiedTypeNameDotted())
			.suffix(this.getMetaAttribute4YAxis());
    }

    /** 
     * Create the list of row categories out of {@link #getRowListName()}.
     * 
     * @return    The values from {@link #createFastListElementNames(String)}, never <code>null</code>.
     */
    protected List<String> getRowList() {
		return this.createFastListElementNames(this.getRowListName());
    }

    /** 
     * Create the list of row categories out of {@link #getColumnListName()}.
     * 
     * @return    The values from {@link #createFastListElementNames(String)}, never <code>null</code>.
     */
    protected List<String> getColumnList() {
		return this.createFastListElementNames(this.getColumnListName());
    }

    /** 
     * Create the data set and fill up the chart data out of the given matrix.
     * 
     * @param    aMatrix       The data provider for this method, must not be <code>null</code>.
     * @param    aChartData    The chart data to be filled, must not be <code>null</code>.
     * @return   The new created data set, never <code>null</code>. 
     */
    @SuppressWarnings("unchecked")
    protected CategoryDataset createDataSetFromMatrix(CategoryMatrix aMatrix, ChartData aChartData) {
    	ChartDataCategoryDataset theDataset = new ChartDataCategoryDataset(aChartData);
        int theOuterDim = aMatrix.getRowSize();
        int theInnerDim = aMatrix.getColumnSize();

        for (int i = 0; i < theOuterDim; i++) {
            String theRowKey = aMatrix.getRowKey(i);

            for (int j = 0; j < theInnerDim; j++) {
                List theItems = aMatrix.getItems(i, j);

                // Fill the CategoryDataset
                theDataset.addValue(theItems.size(), theRowKey, aMatrix.getColumnKey(j));

                // Fill the ChartData
                aChartData.addItems(i, j, theItems);
            }
        }
        return theDataset;
    }

    /**
	 * Return the translated names of the given I18N keys.
	 * 
	 * @param aRes
	 *        The resources to be used for I18N, must not be <code>null</code>.
	 * @param someI18NKeys
	 *        The keys to be translated, must not be <code>null</code>.
	 * @return The list of translated values, never <code>null</code>.
	 */
    protected List<String> getLocalizedEntries(Resources aRes, List<String> someI18NKeys) {
        List<String> theResult = new ArrayList<>(someI18NKeys.size());

        for (String theString : someI18NKeys) {
			theResult.add(aRes.decodeMessageFromKeyWithEncodedArguments(theString));
        }

        return theResult;
    }

    /** 
     * Create a list of I18N keys from a fast list identified by the given name.
     * 
     * @param    aListName    The name of the requested fast list, must not be <code>null</code>.
     * @return   The requested names of the {@link FastListElement elements}.
     */
    protected List<String> createFastListElementNames(String aListName) {
        List<String> theResult = new ArrayList<>();

        for (FastListElement theElement : FastList.getFastList(aListName).elements()) {
            theResult.add(theElement.getName());
        }

        return theResult;
    }

    /** 
     * Hook method for modifying the chart after creation.
     * 
     * @param    aChart    The chart to be modified, never <code>null</code>.
     * @return   The requested chart, never <code>null</code>.
     */
    protected JFreeChart customizeChart(JFreeChart aChart) {
        return aChart;
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
        Resources  theRes       = Resources.getInstance();
        Comparable theRowKey    = aDataset.getRowKey(aRow);
        Comparable theColumnKey = aDataset.getColumnKey(aColumn);
        Number     theCount     = aDataset.getValue(theRowKey, theColumnKey);
        String     theRow       = theRes.getString(this.getRowAxisNameKey());
        String     theColumn    = theRes.getString(this.getColumnAxisNameKey());
		String theType = theRes.getString(TLModelNamingConvention.modelPartNameKey(getQualifiedTypeNameDotted()));

        return new Object[] {theRowKey, theColumnKey, theCount, theRow, theColumn, theType};
    }

    /**
     * A matrix representation which can be accessed via integer and return a list of objects in every position. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    protected class CategoryMatrix<T> {

        // Attributes

        private Map<String, Map<String, List<T>>> internalMatrix;
        private List<String> rowKeys;
        private List<String> columnKeys;

        // Constructors
        
        /** 
         * Creates a {@link CategoryMatrix}.
         */
        public CategoryMatrix(List<String> someRowNames, List<String> someColumnNames) {
            this.rowKeys        = someRowNames;
            this.columnKeys     = someColumnNames;
            this.internalMatrix = new HashMap<>();

            for (String theRow : someRowNames) {
                Map<String, List<T>> theMap = new HashMap<>();

                for (String theColumn : someColumnNames) {
					theMap.put(theColumn, new ArrayList<>());
                }

                this.internalMatrix.put(theRow, theMap);
            }
        }

        // Public methods

        public int getRowSize() {
            return this.rowKeys.size();
        }

        public int getColumnSize() {
            return this.columnKeys.size();
        }

        public void addItem(String aRowKey, String aColumnKey, T anObject) {
            List<T> theList = this.getList(aRowKey, aColumnKey);

            if (theList != null) {
                theList.add(anObject);
            }
        }

        public List<T> getItems(int aRow, int aColumn) {
            return this.getList(this.getRowKey(aRow), this.getColumnKey(aColumn));
        }

        public String getRowKey(int aRow) {
            return this.rowKeys.get(aRow);
        }

        public String getColumnKey(int aColumn) {
            return this.columnKeys.get(aColumn);
        }

        // Protected methods

        protected List<T> getList(String aRowKey, String aColumnKey) {
            Map<String, List<T>> theRow = this.internalMatrix.get(aRowKey);

            if (theRow != null) { 
                return theRow.get(aColumnKey);
            }
            else {
                return null;
            }
        }
    }

    /**
     * Standard template renderer for matrix charts. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    protected class MatrixTemplateRenderer extends TemplateRenderer {

        public MatrixTemplateRenderer() {
            super(MatrixChartInfo.RED_YELLOW_GREEN);

            this.setShapeGradientValue(40);
        }
    }



    /**
     * Special CategoryDataSet providing access to the ChartData.
     *
     * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
     */
    protected static class ChartDataCategoryDataset<T> extends DefaultCategoryDataset {

    	private ChartData<T> chartData;

    	public ChartDataCategoryDataset() {
    		this(null);
    	}

    	public ChartDataCategoryDataset(ChartData<T> chartData) {
    		this.chartData = chartData;
    	}

    	public ChartData<T> getChartData() {
    		return chartData;
    	}

    	public void setChartData(ChartData<T> chartData) {
    		this.chartData = chartData;
    	}

    }

}
