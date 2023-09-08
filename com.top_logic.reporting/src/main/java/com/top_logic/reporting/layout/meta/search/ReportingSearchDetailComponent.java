/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.meta.search;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.meta.search.AttributedSearchResultComponent;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.DialogInfo;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.reporting.chart.component.AbstractFixedReportComponent;
import com.top_logic.reporting.layout.flexreporting.component.ChartConfigurationComponent;
import com.top_logic.reporting.report.model.FilterVO;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.ReportFactory;
import com.top_logic.reporting.report.model.RevisedReport;
import com.top_logic.tool.boundsec.BoundLayout;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;


/**
 * {@link AttributedSearchResultComponent} with undocumented semantics.
 * 
 * <p>
 * TODO: Explain or remove.
 * </p>
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
@Deprecated
public class ReportingSearchDetailComponent extends AttributedSearchResultComponent {

	private static final String XML_CONFIG_RESULT_COLUMNS = "columns";

    public ReportingSearchDetailComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
        super(context, someAttrs);
    }

    public String getTitle(Resources someResources) {
		return someResources.getString(I18NConstants.CHART_DETAILS_TAB);
    }

    @Override
    public ResKey hideReason(Object potentialModel) {
		return null;
    }

    public static void showDetailDialog(ReportingSearchDetailComponent aDetail, AttributedSearchResultSet aResult) {
        aDetail.setModel(aResult);
    }

    /**
     * Handler for clicking on one bar in the reporting chart.
     */
    public static class SearchReportDetailChartHandler extends OpenModalDialogCommandHandler{

        // Constants
        public static final String COMMAND_ID = "showSearchReportDetailChart";
        /** The name of the chart from which we are called */
        public static final String CHART_NAME = "chartName";
        /** Constant for the category. */
        public static final String CATEGORY = "category";
        /** Constant for the series. */
        public static final String SERIES   = "series";

		/**
		 * @see Config#getDetailTable()
		 */
        public static final String DETAIL_TABLE = "detailTable";

        public static final String OPEN_HANDLER_NAME    = "showChartDetails";
        public static final String CHART_DETAIL_HANDLER = "detailHandler";

		private ComponentName _detailTable;

		public interface Config extends OpenModalDialogCommandHandler.Config {
			@Name(DETAIL_TABLE)
			ComponentName getDetailTable();
		}

        public SearchReportDetailChartHandler(InstantiationContext context, Config config) {
			super(context, config);
			_detailTable = config.getDetailTable();
        }

        @Override
		public ResKey createDialogTitle(LayoutComponent aDialogOpener, LayoutComponent aDialogContent,
				DialogInfo aDialogInfo) {
			return I18NConstants.CHART_DETAILS_TAB;
        }

        /** 
         * Return the component for displaying the details from a swing chart.
         * 
         * @return    The requested component, may be <code>null</code>.
         */
        protected ReportingSearchDetailComponent getDetailComponent(LayoutComponent aComponent) {
			BoundLayout theBoundComponent = (BoundLayout) aComponent.getMainLayout().getComponentByName(_detailTable);
            for (Object child : theBoundComponent.getChildList()) {
                if (child instanceof ReportingSearchDetailComponent) {
                    return (ReportingSearchDetailComponent)child;
                }
            }
            return null;
        }


        @Override
        protected void beforeOpening(DisplayContext aContext, LayoutComponent aComponent, Map someArguments, LayoutComponent aDialog) {
            super.beforeOpening(aContext, aComponent, someArguments, aDialog);
            int    theSeries = -1;
            int    theCat    = -1;
            ReportingSearchDetailComponent detailComponent = getDetailComponent(aComponent);
			if (detailComponent == null) {
				Logger.error("No detail component found.", ReportingSearchDetailComponent.class);
				return;
			}

            if ((aComponent instanceof ChartComponent)) {

                try{
                    ChartComponent              theComponent = (ChartComponent)aComponent;
                    String                      theSeriesStr = LayoutComponent.getParameter(someArguments, SERIES);
                    String                      theCatStr    = LayoutComponent.getParameter(someArguments, CATEGORY);
                    FilterVO                    theVO        = (FilterVO) theComponent.getModel();
                    RevisedReport               theReport    = null;

                    theSeries    = Integer.valueOf(theSeriesStr).intValue();
                    theCat       = Integer.valueOf(theCatStr).intValue();

                    // fall back in case the report was not set yet
                    if (theReport == null) {
                        theReport = ReportFactory.getInstance().getReport(theVO.getReportConfiguration());
                    }

                    Collection<?>                      theBOs       = theReport.getRelevantObjectsForItemVO(theSeries, theCat);
                    ReportConfiguration                reportConfig = theComponent.getReportConfiguration();
                    ReportingAttributedSearchResultSet theNewSet;

                    // if the calling component is an AbstractFixedReportComponent there is no CCComp because its 
                    // used outside the search. So the necessary information for the details view has to be collected
                    // in a different way.
                    if (aComponent instanceof AbstractFixedReportComponent ) {
                        List<String> theCols = ((AbstractFixedReportComponent) aComponent).getResultColumns();
                        TLClass  theME   = reportConfig.getSearchMetaElement();

                        theNewSet = new ReportingAttributedSearchResultSet(theBOs, theME, theCols, Collections.EMPTY_LIST, reportConfig);
                    }
                    else {
                        ChartConfigurationComponent theCCComp = (ChartConfigurationComponent) theComponent.getMaster();
                        theNewSet = this.createSearchResultSet(theBOs, theCCComp.getSearchResult(), reportConfig);
                    }

					ReportingSearchDetailComponent.showDetailDialog(detailComponent, theNewSet);
                }
                catch(Exception e){
                    throw new TopLogicException(SearchReportDetailChartHandler.class, " Could not generate detailed report. ", e);
                }

            }
            else {
                Logger.warn("aComponent is not ChartComponent " + aComponent.getClass(), this);

            }

        }



        protected ReportingAttributedSearchResultSet createSearchResultSet(Collection<?> someSearchElements, AttributedSearchResultSet anOldSearchResult, ReportConfiguration aConfiguration) {
			return new ReportingAttributedSearchResultSet(someSearchElements, anOldSearchResult.getTypes(),
				anOldSearchResult.getResultColumns(), anOldSearchResult.getSearchMessages(), aConfiguration);
        }

        @Override
        public String[] getAttributeNames() {
            String[] theAttrNames = super.getAttributeNames();

            if (theAttrNames == null) {
                return new String[] {CHART_NAME, SERIES, CATEGORY};
            }
            else {
                String[] theTmp = new String[theAttrNames.length + 3];
                System.arraycopy(theAttrNames, 0, theTmp, 0, theAttrNames.length);
                theTmp[theAttrNames.length]     = CHART_NAME;
                theTmp[theAttrNames.length + 1] = SERIES;
                theTmp[theAttrNames.length + 2] = CATEGORY;

                return theTmp;
            }
        }
    }

    /**
     * Simple model builder just taking the model from the given component. 
     * 
     */
    public static class SearchDetailModelBuilder implements ModelBuilder {

		/**
		 * Singleton {@link SearchDetailModelBuilder} instance.
		 */
		public static final SearchDetailModelBuilder INSTANCE = new SearchDetailModelBuilder();

		private SearchDetailModelBuilder() {
			// Singleton constructor.
		}

        /**
         * Return the model of the given component.
         * 
         * @see com.top_logic.mig.html.ModelBuilder#getModel(Object, com.top_logic.mig.html.layout.LayoutComponent)
         */
        @Override
		public Object getModel(Object businessModel, LayoutComponent aComponent) {
			return businessModel;
        }

        /**
         * This builder will support {@link Collection collections}.
         * 
         * @see com.top_logic.mig.html.ModelBuilder#supportsModel(java.lang.Object, com.top_logic.mig.html.layout.LayoutComponent)
         */
        @Override
		public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
            return aModel instanceof Collection<?>;
        }
    }
}
