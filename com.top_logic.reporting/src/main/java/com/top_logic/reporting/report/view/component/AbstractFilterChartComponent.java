/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.component;

import java.util.Set;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

import com.top_logic.base.chart.component.JFreeChartComponent;
import com.top_logic.base.chart.util.ChartConstants;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.reporting.layout.meta.search.ReportingAttributedSearchResultSet;
import com.top_logic.reporting.report.control.post.NoOpPostCreationHandler;
import com.top_logic.reporting.report.control.post.PostChartCreationHandler;
import com.top_logic.reporting.report.control.producer.ChartContext;
import com.top_logic.reporting.report.model.FilterVO;
import com.top_logic.tool.boundsec.securityObjectProvider.NullSecurityObjectProvider;

/**
 * This abstract chart component works together with the {@link AbstractFilterComponent}.
 * Use a {@link AbstractFilterComponent} as master of this component und the event handling
 * is managed for you.
 * 
 * All necessary information to generate the chart is stored into the filter value object.
 * You can get the filter object with the method {@link #getModel()} or 
 * {@link #getModelAsChartContext()}
 * 
 * @see AbstractFilterComponent
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class AbstractFilterChartComponent extends JFreeChartComponent implements
		ChartConstants, Selectable {

    public interface Config extends JFreeChartComponent.Config {
		@Name(LAYOUT_ATTRIBUTE_POST_CREATION_CLASS_NAME)
		@InstanceFormat
		@InstanceDefault(NoOpPostCreationHandler.class)
		PostChartCreationHandler getPostCreationClassName();

		@Override
		@StringDefault(NullSecurityObjectProvider.ALIAS_NAME)
		String getSecurityProviderClass();

	}

	/** The attribute contains the class name of the {@link PostChartCreationHandler}. */
    public static final String LAYOUT_ATTRIBUTE_POST_CREATION_CLASS_NAME = "postCreationClassName";

    private JFreeChart             chart;
    
    private PostChartCreationHandler postHandler;

    /** Creates a {@link AbstractFilterChartComponent}. */
    public AbstractFilterChartComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
        
        this.postHandler = atts.getPostCreationClassName();
    }

    abstract protected JFreeChart createChart();
    
    /**
     * Returns the {@link PostChartCreationHandler} which does somewhat undefined with the chart.
     * Its usage is not encurage because of the broken or unknown concept.
     * 
     * @return the post handler, never <code>null</code>
     */
    protected final PostChartCreationHandler getPostHandler() {
        return this.postHandler;
    }
    
    @Override
	protected JFreeChart createChart(String anImageId) {
        anImageId = anImageId == null? JFreeChartComponent.DEFAULT_IMAGE_ID : anImageId;

        if (this.chart == null) {
            this.chart = createChart();
        }
        
        // Set the image id to the chart context and set the
        // chart in the chart cache
        ChartContext chartContext = getModelAsChartContext();
        if(chartContext != null) {
        	chartContext.setValue(VALUE_IMAGE_ID, anImageId);
        }
        this.charts.put(anImageId, this.chart);
        
        return this.chart;
    }

    @Override
	abstract protected Dataset createDataSet(String anImageId);

    @Override
	public boolean isModelValid() {
		return this.chart != null && super.isModelValid();
    }

    @Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(createModel());
		}
		super.validateModel(context);
        this.chart = createChart();
        this.postHandler.handle(this, getModelAsChartContext(), this.chart, true);

        return true;
    }

    /**
     * Drop model when master does, too.
     */
    @Override
	protected boolean receiveModelDeletedEvent(Set<TLObject> aModel, Object aChangedBy) {
		if (aModel.contains(getModel())) {
            fireSecurityChanged((Object) null);
			setModel(null);
            chart = null;
            this.invalidate();
			super.receiveModelDeletedEvent(aModel, aChangedBy);
            return true;
        }
		return super.receiveModelDeletedEvent(aModel, aChangedBy);
    }

    /**
     * Create the model for this class.
     */
    protected ChartContext createModel() {
        LayoutComponent theMaster = this.getMaster();
        if (theMaster instanceof AbstractFilterComponent) {
            Object theModel = theMaster.getModel();
            if (supportsInternalModel(theModel)) {
                ChartContext theContext = new FilterVO(theModel);
                if (theModel instanceof ReportingAttributedSearchResultSet) {
                    theContext.setReportConfiguration(((ReportingAttributedSearchResultSet) theModel).getReportConfiguration());
                }
                return theContext;
            }
        }
        return new FilterVO();
    }

    /** Returns the model as {@link ChartContext}. */
    public ChartContext getModelAsChartContext() {
		Object model = getModel();
		if (!(model instanceof ChartContext)) {
			return new FilterVO(getModel());
		} else {
			return (ChartContext) model;
		}

    }

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		resetCaches();

		/* Chart must be recreated. To omit multiple chart creation the chart is not created
		 * directly, but when the model is validated. */
		this.chart = null;

		fireSecurityChanged(newModel);
	}

	@Override
	protected void handleNewModel(Object newModel) {
		super.handleNewModel(newModel);
		invalidateButtons();
	}


	@Override
	protected boolean supportsInternalModel(Object anObject) {
        return anObject instanceof ChartContext;
    }

}
