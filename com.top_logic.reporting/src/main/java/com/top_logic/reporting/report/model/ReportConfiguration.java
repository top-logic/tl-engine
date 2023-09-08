/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model;

import java.util.List;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ImplClasses;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.element.meta.MEConfigurationValueProvider;
import com.top_logic.model.TLClass;
import com.top_logic.reporting.report.model.aggregation.AggregationFunctionConfiguration;
import com.top_logic.reporting.report.model.aggregation.AggregationFunctionFactory;
import com.top_logic.reporting.report.model.aggregation.AggregationFunctionLabelProvider;
import com.top_logic.reporting.report.model.aggregation.DefaultAggregationFunctionLabelProvider;
import com.top_logic.reporting.report.model.objectproducer.ObjectProducerConfiguration;
import com.top_logic.reporting.report.model.objectproducer.ObjectProducerFactory;
import com.top_logic.reporting.report.model.objectproducer.WrapperObjectProducer;
import com.top_logic.reporting.report.model.partition.PartitionFunctionConfiguration;
import com.top_logic.reporting.report.model.partition.function.PartitionFunctionFactory;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@Deprecated
public interface ReportConfiguration extends PolymorphicConfiguration<RevisedReport> {
    
	String ATTRIBUTE_NAME = "attribute";
	@Name(ATTRIBUTE_NAME)
	@Nullable
    String getAttribute();
    void setAttribute(String anAtrributePath);
    
    void setTitleLabel(String aTitle);
    String getTitleLabel();
    
    void setXAxisLabel(String anXAxisLabel);
    String getXAxisLabel();

    void setYAxisLabel(String anYAxisLabel);
    String getYAxisLabel();
    
    boolean shouldShowToolTips();
    void setShowToolTips(boolean showTooltips);
    
    boolean shouldShowLegend();
    void setShowLegend(boolean showLegend);
    
    @BooleanDefault(true)
    boolean shouldShowUrls();
    void setShowUrls(boolean showUrls);
    
    /**
     * Type of the chart, e.g. bar-chart, line-chart.
     * This type defines, which chart is drawn and which properties
     * of this class are useful.
     */
    String CHART_TYPE_NAME = "chartType";
    void setChartType(String aChartType);
    @Name(CHART_TYPE_NAME)
    String getChartType();
    
    /**
     * Return the type of the businessobjects, this report is based on
     */
    @Format(MEConfigurationValueProvider.class)
    TLClass getSearchMetaElement();
    void setSearchMetaElement(TLClass aMeta);
    
    public static class BusinessObjectProducer extends DefaultValueProvider {
		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			return ObjectProducerFactory.getInstance().createObjectProducerConfiguration(WrapperObjectProducer.class);
		}
    }
    void setBusinessObjectProducer(ObjectProducerConfiguration aConfig);
    @ImplClasses(ObjectProducerFactory.class)
    @Name("businessObjectProducer")
    @ComplexDefault(BusinessObjectProducer.class)
    ObjectProducerConfiguration getBusinessObjectProducer();
    
    String PARTITION_CONFIGURATION_NAME = "partition";
    void setPartitionConfiguration(PartitionFunctionConfiguration aFunctions);
    @ImplClasses(PartitionFunctionFactory.class)
    @Name(PARTITION_CONFIGURATION_NAME)
    PartitionFunctionConfiguration  getPartitionConfiguration();
    
    String AGGREGATION_CONFIGURATIONS_NAME = "aggregationFunctions";
    // type of a inner list element
    void setAggregationConfigurations(List<AggregationFunctionConfiguration> someFunctions);
    @ImplClasses(AggregationFunctionFactory.class)
    @Name(AGGREGATION_CONFIGURATIONS_NAME)
    List<AggregationFunctionConfiguration> getAggregationConfigurations();
    
    /**
     * If true, labels on local maxima of a dataset wil be drawn
     */
    String SHOW_PEAK_VALUES_NAME = "showPeakValues";
    @BooleanDefault(false)
    @Name(SHOW_PEAK_VALUES_NAME)
    boolean isShowPeakValues();
    void setShowPeakValues(boolean showPeakValues);
    
    // PropertyName _PROPERTY_NAME = new PropertyName
    String SHOW_HIGHLIGHT_AREA_NAME = "showHighlightArea";
    @BooleanDefault(false)
    @Name(SHOW_HIGHLIGHT_AREA_NAME)
    boolean shouldShowHighlightArea();
    void setShowHighlightArea(boolean showHighlightArea);
    
    String HIGHLIGHT_AREA_LABEL_NAME = "highlightAreaLabel";
    @Name(HIGHLIGHT_AREA_LABEL_NAME)
    String getHighlightAreaLabel();
    void setHighlightAreaLabel(String aLabel);
    
    String HIGHLIGHT_AREA_FROM_NAME = "highlightAreaFrom";
    @Name(HIGHLIGHT_AREA_FROM_NAME)
    String getHighlightAreaFrom();
    void setHighlightAreaFrom(String aFrom);
    
    String HIGHLIGHT_AREA_TO_NAME = "highlightAreaTo";
    @Name(HIGHLIGHT_AREA_TO_NAME)
    String getHighlightAreaTo();
    void setHighlightAreaTo(String aTo);
    
	@InstanceFormat()
	@InstanceDefault(DefaultAggregationFunctionLabelProvider.class)
	@Name("aggregationFunctionLabelProvider")
    AggregationFunctionLabelProvider getAggregationFunctionLabelProvider();
    void setAggregationFunctionLabelProvider(AggregationFunctionLabelProvider aProvider);
}

