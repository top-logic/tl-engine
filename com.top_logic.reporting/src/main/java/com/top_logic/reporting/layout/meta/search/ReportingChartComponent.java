/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.meta.search;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;

import com.top_logic.base.chart.dataset.ExtendedTimeSeries;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.model.TLClass;
import com.top_logic.reporting.layout.meta.search.ReportingSearchDetailComponent.SearchReportDetailChartHandler;
import com.top_logic.reporting.report.control.producer.ChartContext;
import com.top_logic.reporting.report.model.FilterVO;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.ReportFactory;
import com.top_logic.reporting.report.model.RevisedReport;
import com.top_logic.reporting.report.model.partition.function.PartitionFunctionFactory;
import com.top_logic.reporting.report.util.ReportConstants;
import com.top_logic.reporting.report.util.ReportUtilities;
import com.top_logic.reporting.report.view.component.DefaultProducerChartComponent;
import com.top_logic.tool.export.AbstractOfficeExportHandler.OfficeExportValueHolder;
import com.top_logic.tool.export.WordExportHandler;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * The ReportingChartComponent takes the information from the SearchInput and the objects from the
 * SearchResultSet and creates a suitable {@link RevisedReport}. This report is then used to create the
 * chart.
 *
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public class ReportingChartComponent extends DefaultProducerChartComponent implements ReportConstants, ChartComponent {

	private ReportConfiguration configuration;
	private RevisedReport       report;
	private String              handlerName;

	/**
	 * Creates a {@link ReportingChartComponent}.
	 */
	public ReportingChartComponent(InstantiationContext context, Config aAtts) throws ConfigurationException {
		super(context, aAtts);
		
		try {
		    this.configuration = ReportFactory.getInstance().createReportConfiguration(RevisedReport.class);
		} catch (ConfigurationException c) {
		    throw new ConfigurationError("Invalid report configuration", c);
		}
	}

	@Override
	protected boolean observeAllTypes() {
		return true;
	}

	@Override
	protected boolean receiveModelChangedEvent(Object model, Object changedBy) {
		if ((model instanceof ReportConfiguration)) {
			// this event should only occur, if something actually happened...
			this.configuration = (ReportConfiguration) model;
			this.report = null;
			ChartContext context = getChartContextModel();
			List theList = new ArrayList(context.getFilteredObjects());
			FilterVO theVO = new FilterVO(context.getModel(), theList, this.configuration);
			
			theVO.setValue(SearchReportDetailChartHandler.CHART_DETAIL_HANDLER, this.getHandlerName());
			
			for (Iterator theIter = context.getAllKeys(); theIter.hasNext();) {
				Object theKey = theIter.next();
				theVO.setValue(theKey, context.getValue(theKey));
			}
			return super.receiveModelChangedEvent(theVO, changedBy);
		}
		return false;
	}

	private ChartContext getChartContextModel() {
		return (ChartContext) getModel();
	}

	/**
	 * Returns the name of the dialog opener used for <code>this</code> component. The returned Name
	 * will start with "displayDialog_".
	 */
	private String getHandlerName() {
		if (this.handlerName == null) {
			this.handlerName = getCommandById(SearchReportDetailChartHandler.OPEN_HANDLER_NAME).getID();
		}
		return this.handlerName;
	}
	
	@Override
	protected JFreeChart createChart() {
		ChartContext theChartContext = getModelAsChartContext();
		theChartContext.setValue(SearchReportDetailChartHandler.CHART_DETAIL_HANDLER, this.getHandlerName());
		return createChart(theChartContext);
	}
	
	@Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(initialModel());
		}
		return super.validateModel(context);
	}

	private Object initialModel() {
		ChartContext context = new FilterVO();
		if (configuration != null) {
			context.setReportConfiguration(configuration);
		}
		return context;
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject == null || super.supportsInternalModel(anObject);
	}

	@Override
	public RevisedReport getReport() {
		if (this.report == null) {
			this.report = ReportFactory.getInstance().getReport(this.configuration);
		}
    	return (report);
    }

	/**
     * Returns the active {@link ReportConfiguration}.
     */
    @Override
	public ReportConfiguration getReportConfiguration() {
    	return (configuration);
    }
    
	@Override
	public String getTemplatePath() {
		String templateName = super.getTemplatePath();
		int pos = templateName.lastIndexOf(".");
		templateName =
			templateName.substring(0, pos) + "_" + TLContext.getLocale().getLanguage() + templateName.substring(pos);
		return templateName;
	}

	@Override
	public OfficeExportValueHolder getExportValues(DefaultProgressInfo progressInfo, Map arguments) {
    	Resources               theRes          = Resources.getInstance();
        OfficeExportValueHolder theValueHolder  =  super.getExportValues(progressInfo, arguments);
        @SuppressWarnings("unchecked")
        Map<String, Object> theExportData = (Map<String, Object>) theValueHolder.exportData;

        // Export following only if word handler is used as these tables values are only supported by word, yet. 
        if (!WordExportHandler.COMMAND_ID.equals(getExportHandlerId())) {
        	return theValueHolder;
        }

		// if the report configuration is not valid, delete all table tokens
		// from the template and export the empty chart.
        if (!StringServices.isEmpty(RevisedReport.checkReportConfiguration(this.configuration))) {
        	for (int i = 1; i <= 5; i++) {
        		String tableKey = "TABLE_CHART_" + i;
        		theExportData.put(tableKey, null);
			}
        	return theValueHolder;
        }
        TLClass       theME       = this.configuration.getSearchMetaElement();
        List<Object>      someVals    = new ArrayList<>();
		String            mePrefix    = theME.getName();
		String            thePfType   = this.configuration.getChartType(); //thePF.getType();
		String            theAttrName = this.configuration.getAttribute();//thePF.getAttributeName();
		String pfName = theRes.getString(RES_PREFIX.key(thePfType));
		String attrName = theRes.getString(ResPrefix.legacyString(mePrefix).key(theAttrName));
		DecimalFormat     format      = new DecimalFormat("###,###,###,###,##0.##");
		
		theExportData.put("VALUE_NAME_OF_ATTRIBUTE", attrName);
		someVals.add(pfName + " (" + attrName + ")");
		
		if (PartitionFunctionFactory.DATE.equals(thePfType)) {
			TimeSeriesCollection dataset     = ReportUtilities.generateTimeSeriesCollectionFor(getReport());//, theAggregationDefinitions, theProvider, theME);
			List                 theSeries   = dataset.getSeries();
			int                  seriesCount = theSeries.size();
			
			for (int i = 1; i <= 5; i++) {
				if (i != seriesCount) {
					String tableKey = "TABLE_CHART_" + i;
					theExportData.put(tableKey, null);
				}
			}
			String       tableKey = "TABLE_CHART_" + seriesCount;
			List<String> theKeys  = new ArrayList<>(seriesCount);
			int    max      = 0;
			
			for (int i = 0; i < seriesCount; i++) {
				String seriesKey = (String) dataset.getSeriesKey(i);
				someVals.add(seriesKey);
				theKeys.add(seriesKey);
				int count = dataset.getItemCount(i);
				max = count > max ? count : max;
			}
			for (int i = 0; i < max; i++) {
				ExtendedTimeSeries aSerie  = (ExtendedTimeSeries) theSeries.get(0);
				TimeSeriesDataItem anItem  = aSerie.getDataItem(i);
				TimePeriod         aPeriod = anItem.getPeriod();
				
				someVals.add(aPeriod);
				
				for (int j = 0; j < seriesCount;j++) {
					aSerie       = (ExtendedTimeSeries) theSeries.get(j);
					anItem       = aSerie.getDataItem(i);
					Number value = anItem.getValue();
					someVals.add(format.format(value));
				}
			}
			theExportData.put(tableKey, someVals);
		}
		else {
			CategoryDataset dataset = ReportUtilities.generateCategoryDatasetFor(getReport());//, theAggregationDefinitions, theProvider, theME);
			List<String>    rowKeys = dataset.getRowKeys();
			int             rows    = rowKeys.size();
			int             colums  = dataset.getColumnKeys().size();
			
			for (int i = 0; i < rows; i++) {
				someVals.add(rowKeys.get(i));
			}
			for (int i = 0; i < colums; i++ ) {
				String cKey = (String) dataset.getColumnKey(i);
				someVals.add(cKey);
				
				for (int j = 0; j < rows; j++) {
					Number theVal = dataset.getValue(j, i);
					someVals.add(format.format(theVal));
				}
			}
			for (int i = 1; i <= 5; i++) {
				if (i != rows) {
					String tableKey = "TABLE_CHART_" + i;
					theExportData.put(tableKey, null);
				}
			}
			String tableKey = "TABLE_CHART_" + rows;
			
			theExportData.put(tableKey, someVals);
		}

        return theValueHolder;
    }

	/** 
	 * The configured result columns of the masters {@link AttributedSearchResultSet}
	 */
	public List<String> getSearchResultColumns() {
    	AttributedSearchResultSet theResultSet = (AttributedSearchResultSet) this.getMaster().getModel();
    	return theResultSet.getResultColumns();
	}
}
