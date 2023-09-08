/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.reporting.layout.meta.search.ReportingAttributedSearchResultSet;
import com.top_logic.reporting.report.control.checker.Checker;
import com.top_logic.reporting.report.control.checker.TrueChecker;
import com.top_logic.reporting.report.control.producer.ChartContext;
import com.top_logic.reporting.report.model.FilterVO;
import com.top_logic.reporting.report.view.component.configuration.ProducerFilterConfiguration;
import com.top_logic.util.error.TopLogicException;

/**
 * Use this component together with the {@link DefaultProducerChartComponent} if
 * you haven't additional filters. If you want to use the {@link DefaultProducerChartComponent}
 * you need a {@link AbstractFilterComponent} as parent. The DefaultProducerFilterComponent can
 * be used if the chart needs no additional information but the model of the parent. This
 * component wraps only the model in {@link FilterVO} which can be used for the
 * {@link DefaultProducerChartComponent}. If you have additional filter, you must implement
 * a new sub class of {@link AbstractFilterComponent}.
 *
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class DefaultProducerFilterComponent extends AbstractFilterComponent {

    public interface Config extends AbstractFilterComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Name(LAYOUT_ATTRIBUTE_CHART_CONTEXT_CLASS_NAME)
		String getChartContextClass();

		@Name(LAYOUT_ATTRIBUTE_CHECKER_CLASS_NAME)
		String getCheckerClass();

		@Name(LAYOUT_ATTRIBUTE_FILTER_CONFIGURATION_NAME)
		String getConfigurationClass();

		@Override
		default void addUpdateCommand(CommandRegistry registry) {
			if (!getConfigurationClass().isEmpty()) {
				AbstractFilterComponent.Config.super.addUpdateCommand(registry);
			}
		}

	}

	/** The name of the chart context class. The class must have a default
     *  constructor. */
    public static final String LAYOUT_ATTRIBUTE_CHART_CONTEXT_CLASS_NAME  = "chartContextClass";
    /** The name of the {@link Checker} class. The checker validates if the
     *  given object is supported for this filter component. */
    public static final String LAYOUT_ATTRIBUTE_CHECKER_CLASS_NAME        = "checkerClass";
    /** The layout xml attribute for the producer filter configuration class name. */
    public static final String LAYOUT_ATTRIBUTE_FILTER_CONFIGURATION_NAME = "configurationClass";

    /** The name of the chart context class or <code>null</code> if no class is configured. */
    private String chartContextClassName;

    /** The checker validates the received models. */
    private Checker modelChecker;

    /** The producer filter configuration should avoids to many sub classes. */
    private ProducerFilterConfiguration filterConfiguration;

    /** Creates a {@link DefaultProducerFilterComponent}. */
    public DefaultProducerFilterComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);

        this.chartContextClassName = StringServices.nonEmpty(atts.getChartContextClass());

        String checkerClassName    = StringServices.nonEmpty(atts.getCheckerClass());
        if (StringServices.isEmpty(checkerClassName)) {
            this.modelChecker = new TrueChecker();
        } else {
            try {
                this.modelChecker = (Checker) Class.forName(checkerClassName).newInstance();
            }
            catch (Exception e) {
                throw new TopLogicException(this.getClass(), "Could not create the checker for the component ('" + this.getName() + "').");
            }
        }

		String filterConfigurationName = atts.getConfigurationClass();
        if (!StringServices.isEmpty(filterConfigurationName)) {
            try {
                this.filterConfiguration = (ProducerFilterConfiguration) Class.forName(filterConfigurationName).newInstance();
            }
            catch (Exception e) {
                throw new TopLogicException(this.getClass(), "Could not create the ProducerFilterConfiguration for the component ('" + this.getName() + "').");
            }
        }
    }

	@Override
	protected boolean observeAllTypes() {
		return true;
	}

    @Override
	protected void handleEvent() {
        ChartContext chartContext = getInitialChartContext();
        if (supportsInternalModel(getModel())) {
            fill(chartContext);
			setSelected(chartContext);
        }
    }

	/**
	 * This method returns the initial {@link ChartContext} of this component or
	 * <code>null</code>. The returned {@link ChartContext} can be used to
	 * read default values. But note, every call produces a new instance of the
	 * chart context and returned never the same context object.
	 */
	protected ChartContext getInitialChartContext() {
		ChartContext chartContext;
		Object       theModel = this.getModel();
		if (StringServices.isEmpty(this.chartContextClassName)) {
            chartContext = new FilterVO(theModel);
        } else {
            try {
                chartContext = (ChartContext) Class.forName(this.chartContextClassName).newInstance();
                chartContext.setModel(theModel);
            }
            catch (Exception e) {
                throw new TopLogicException(this.getClass(), "Could not create the chart context for the component ('" + this.getName() + "').");
            }
        }
		if (theModel instanceof ReportingAttributedSearchResultSet) {
		    chartContext.setReportConfiguration(((ReportingAttributedSearchResultSet) theModel).getReportConfiguration());
		}
		
		return chartContext;
	}

    @Override
	protected void fill(FormContext aContext) {
        if (this.filterConfiguration != null) {
            this.filterConfiguration.fill(getModel(), aContext);
        }
    }

    @Override
	protected void fill(ChartContext aChartContext) {
        if (this.filterConfiguration != null) {
            this.filterConfiguration.fill(getFormContext(), aChartContext);
        }
    }

    @Override
	protected boolean resetFormContext(Object oldModel, Object aModel) {
    	if (this.filterConfiguration != null) {
            return this.filterConfiguration.resetFormContext(aModel);
        }
    	return super.resetFormContext(oldModel,aModel);
    }
    
    public String getFieldsetLabel() {
        if (this.filterConfiguration != null) {
            return this.filterConfiguration.getFieldsetLabel();
        }

        return null;
    }

    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return this.modelChecker.check(anObject);
    }

}
