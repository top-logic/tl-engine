/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.flexreporting.component;

import org.w3c.dom.Document;

import com.top_logic.base.time.BusinessYearConfiguration;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.Control;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.control.SelectControl;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.aggregation.AggregationFunctionConfiguration;
import com.top_logic.reporting.report.model.partition.function.DatePartitionFunction.DatePartitionConfiguration;
import com.top_logic.reporting.report.model.partition.function.NumberPartitionFunction.NumberPartitionConfiguration;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@Deprecated
public class ChartConfigurationControlProvider extends ConfigurationControlProvider {

    private static final String DATE_PARTITION_TEMPLATE_STRING = 
//            +       "<tr>"
//            +           "<td class='label'>"   + getLabelTagString(DatePartitionConfiguration.ATTRIBUTE_PROPERTY_NAME.getName()) + "</td>"
//            +           "<td class='content'>" + getFieldTagString(DatePartitionConfiguration.ATTRIBUTE_PROPERTY_NAME.getName()) + "</td>"
//            +       "</tr>"
	        "<tr>"
	    +           "<td class='label'>"   + getLabelTagString(DatePartitionConfiguration.DATE_RANGE_NAME) + "</td>"
	    +           "<td class='content'>" + getFieldTagString(DatePartitionConfiguration.DATE_RANGE_NAME) + "</td>"
	    +       "</tr>"
	    +       "<tr>"
	    +           "<td class='label'>"   + getLabelTagString(DatePartitionConfiguration.INTERVAL_START_NAME) + "</td>"
	    +           "<td class='content'>" + getFieldTagString(DatePartitionConfiguration.INTERVAL_START_NAME) + "</td>"
	    +       "</tr>"
	    +       "<tr>"
	    +           "<td class='label'>"   + getLabelTagString(DatePartitionConfiguration.INTERVAL_END_NAME) + "</td>"
	    +           "<td class='content'>" + getFieldTagString(DatePartitionConfiguration.INTERVAL_END_NAME) + "</td>"
	    +       "</tr>"
	    +       "<tr>"
	    +           "<td class='label'>"   + getLabelTagString(DatePartitionConfiguration.SUB_INTERVAL_LENGTH_NAME) + "</td>"
	    +           "<td class='content'>" + getFieldTagString(DatePartitionConfiguration.SUB_INTERVAL_LENGTH_NAME) + "</td>"
	    +       "</tr>";

    private static final Document REPORT_TEMPLATE = DOMUtil.parseThreadSafe(
            "<table"
        +   " xmlns='" + HTMLConstants.XHTML_NS + "'"
        +   " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
        +   " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
        +   ">"
        +       "<tr>"
        +           "<td class='label'>"   + getLabelTagString(ReportConfiguration.ATTRIBUTE_NAME) + "</td>"
        +           "<td class='content'>" + getFieldTagString(ReportConfiguration.ATTRIBUTE_NAME) + "</td>"
        +       "</tr>"
        +       "<tr>"
        +           "<td class='label'>"   + getLabelTagString(ReportConfiguration.CHART_TYPE_NAME) + "</td>"
        +           "<td class='content'>" + getFieldTagString(ReportConfiguration.CHART_TYPE_NAME) + "</td>"
        +       "</tr>"
        +       "<tr>"
        +           "<td class='label' colspan='2'><p:field name='" + ReportConfiguration.PARTITION_CONFIGURATION_NAME + "' style='label'/></td>"
        +       "</tr>"
        +       "<tr>"
        +           "<td class='content' colspan='2'><p:field name='" + ReportConfiguration.PARTITION_CONFIGURATION_NAME + "' /></td>"
        +       "</tr>"
        +       "<tr>"
        +           "<td class='label' colspan='2'><p:field name='" +   ConfigurationFormFieldHelper.TABLE_PREFIX + ReportConfiguration.AGGREGATION_CONFIGURATIONS_NAME + "' style='label'/></td>"
        +       "</tr>"
        +       "<tr>"
        +           "<td class='content' colspan='2'><p:field name='" + ConfigurationFormFieldHelper.TABLE_PREFIX + ReportConfiguration.AGGREGATION_CONFIGURATIONS_NAME + "' /></td>"
        +       "</tr>"
        +       "<tr>"
        +           "<td class='label'>"   + getLabelTagString(ReportConfiguration.SHOW_PEAK_VALUES_NAME) + "</td>"
        +           "<td class='content'>" + getFieldTagString(ReportConfiguration.SHOW_PEAK_VALUES_NAME) + "</td>"
        +       "</tr>"
        +       "<tr>"
        +           "<td class='label'>"     + getLabelTagString(ReportConfiguration.SHOW_HIGHLIGHT_AREA_NAME) + "</td>"
        +           "<td class='content'>"   + getFieldTagString(ReportConfiguration.SHOW_HIGHLIGHT_AREA_NAME) + "</td>"
        +       "</tr>"
        +       "<tr>"
        +           "<td class='label'>"     + getLabelTagString(ReportConfiguration.HIGHLIGHT_AREA_LABEL_NAME) + "</td>"
        +           "<td class='content'>"   + getFieldTagString(ReportConfiguration.HIGHLIGHT_AREA_LABEL_NAME) + "</td>"
        +       "</tr>"
        +       "<tr>"
        +           "<td class='label'>"     + getLabelTagString(ReportConfiguration.HIGHLIGHT_AREA_FROM_NAME) + "</td>"
        +           "<td class='content'>"   + getFieldTagString(ReportConfiguration.HIGHLIGHT_AREA_FROM_NAME) + "</td>"
        +       "</tr>"
        +       "<tr>"
        +           "<td class='label'>"     + getLabelTagString(ReportConfiguration.HIGHLIGHT_AREA_TO_NAME) + "</td>"
        +           "<td class='content'>"   + getFieldTagString(ReportConfiguration.HIGHLIGHT_AREA_TO_NAME) + "</td>"
        +       "</tr>"
        +   "</table>"
    		);

    private static final Document AGGREGATION_FUNCTION_TEMPLATE = DOMUtil.parseThreadSafe(
            "<table style='margin:0px; border:0px; spacing:0px;'"
        +   " xmlns='" + HTMLConstants.XHTML_NS + "'"
        +   " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
        +   " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
        +   ">"
        +       "<tr>"
        +           "<td class='content'>" + getFieldTagString(AggregationFunctionConfiguration.ATTRIBUTE_PATH_NAME) + "</td>"
        +           "<td class='content'>" + getFieldTagString(AggregationFunctionConfiguration.COLOR_PROPERTY_NAME, false) + "</td>"
        +       "</tr>"
        +       "<tr>"
        +           "<td class='label'>" + getLabelTagString(AggregationFunctionConfiguration.IGNORE_NULL_VALUES_PROPERTY_NAME) + "</td>"
        +           "<td class='content'>" + getFieldTagString(AggregationFunctionConfiguration.IGNORE_NULL_VALUES_PROPERTY_NAME) + "</td>"
        +       "</tr>"
        +   "</table>"
    );

    private static final Document DATE_PARTITION_TEMPLATE = DOMUtil.parseThreadSafe(
    		"<table"
		    +   " xmlns='" + HTMLConstants.XHTML_NS + "'"
		    +   " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
		    +   " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
		    +   ">"
		    + DATE_PARTITION_TEMPLATE_STRING
		    +  "</table>");

    private static final Document DATE_YEAR_PARTITION_TEMPLATE = DOMUtil.parseThreadSafe(
    		"<table"
		    +   " xmlns='" + HTMLConstants.XHTML_NS + "'"
		    +   " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
		    +   " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
		    +   ">"
		    + DATE_PARTITION_TEMPLATE_STRING 
		    + "<tr>" 
		    + 	"<td class='label'>" + getLabelTagString(DatePartitionConfiguration.USE_BUSINESS_YEAR_NAME) + "</td>" 
		    + 	"<td class='content'>" + getFieldTagString(DatePartitionConfiguration.USE_BUSINESS_YEAR_NAME) + "</td>" 
		    + "</tr>" + 
		    "</table>");

    private static final Document NUMBER_PARTITION_TEMPLATE = DOMUtil.parseThreadSafe(
            "<table"
            +   " xmlns='" + HTMLConstants.XHTML_NS + "'"
            +   " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
            +   " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
            +   ">"
//            +       "<tr>"
//            +           "<td class='label'>"   + getLabelTagString(NumberPartitionConfiguration.ATTRIBUTE_PROPERTY_NAME.getName()) + "</td>"
//            +           "<td class='content'>" + getFieldTagString(NumberPartitionConfiguration.ATTRIBUTE_PROPERTY_NAME.getName()) + "</td>"
//            +       "</tr>"
            +       "<tr>"
            +           "<td class='label'>"   + getLabelTagString(NumberPartitionConfiguration.USE_AUTOMATIC_RANGE_NAME) + "</td>"
            +           "<td class='content'>" + getFieldTagString(NumberPartitionConfiguration.USE_AUTOMATIC_RANGE_NAME) + "</td>"
            +       "</tr>"
            +       "<tr>"
            +           "<td class='label'>"   + getLabelTagString(NumberPartitionConfiguration.INTERVAL_START_NAME) + "</td>"
            +           "<td class='content'>" + getFieldTagString(NumberPartitionConfiguration.INTERVAL_START_NAME) + "</td>"
            +       "</tr>"
            +       "<tr>"
            +           "<td class='label'>"   + getLabelTagString(NumberPartitionConfiguration.INTERVAL_END_NAME) + "</td>"
            +           "<td class='content'>" + getFieldTagString(NumberPartitionConfiguration.INTERVAL_END_NAME) + "</td>"
            +       "</tr>"
            +       "<tr>"
            +           "<td class='label'>"   + getLabelTagString(NumberPartitionConfiguration.SUB_INTERVAL_LENGTH_NAME) + "</td>"
            +           "<td class='content'>" + getFieldTagString(NumberPartitionConfiguration.SUB_INTERVAL_LENGTH_NAME) + "</td>"
            +       "</tr>"
            +   "</table>"
    );
    
    private static final Document PARTITION_GROUP_TEMPLATE = DOMUtil.parseThreadSafe(
            "<table"
        +   " xmlns='" + HTMLConstants.XHTML_NS + "'"
        +   " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
        +   " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
        +   ">"
        +       "<tr>"
        +           "<td class='content'><p:field name='select' /></td>"
        +       "</tr>"
        +       "<tr>"
        +           "<td class='content'><p:field name='groupContainer' /></td>"
        +       "</tr>"
        +   "</table>"
        );

	public ChartConfigurationControlProvider(ResPrefix aResPrefix) {
        super(aResPrefix);
    }

    @Override
	public Control visitFormContainer(FormContainer aMember, Void arg) {
        if (ReportConfiguration.PARTITION_CONFIGURATION_NAME.equals(aMember.getName())) {
            return new FormGroupControl((FormGroup) aMember, this, this.getPartitionGroupTemplate(null), this.getResPrefix());
        }
		return super.visitFormContainer(aMember, arg);
    }

    @Override
	protected Control getFormGroupControl(FormContainer aGroup, ConfigurationDescriptor aConfDesc) {
        Class<?> theIFace = aConfDesc.getConfigurationInterface();
        if (ReportConfiguration.class.isAssignableFrom(theIFace)) {
            return new FormGroupControl((FormGroup) aGroup, this, this.getReportTemplate(aConfDesc), this.getResPrefix());
        }
        else if (DatePartitionConfiguration.class.isAssignableFrom(theIFace)) {
            return new FormGroupControl((FormGroup) aGroup, this, this.getDatePartitionTemplate(aConfDesc), this.getResPrefix());
        }
        else if (NumberPartitionConfiguration.class.isAssignableFrom(theIFace)) {
            return new FormGroupControl((FormGroup) aGroup, this, this.getNumberPartitionTemplate(aConfDesc), this.getResPrefix());
        }
        else if (AggregationFunctionConfiguration.class.isAssignableFrom(theIFace)) {
            return new FormGroupControl((FormGroup) aGroup, this, this.getAggregationFunctionTemplate(aConfDesc), this.getResPrefix());
        }
        return super.getFormGroupControl(aGroup, aConfDesc);
    }

    @Override
	public Control visitSelectField(SelectField aMember, Void arg) {
		Control theControl = (Control) super.visitSelectField(aMember, arg);

        if (theControl instanceof SelectControl) {
            ((SelectControl) theControl).setInputStyle("width:125px;");
        }

        return theControl;
    }

    protected Document getNumberPartitionTemplate(ConfigurationDescriptor aConfDesc) {
        return NUMBER_PARTITION_TEMPLATE;
    }
    protected Document getDatePartitionTemplate(ConfigurationDescriptor aConfDesc) {
    	if (BusinessYearConfiguration.isConfigured()) {
    		return DATE_YEAR_PARTITION_TEMPLATE;
    	} else {
    		return DATE_PARTITION_TEMPLATE;
    	}
    }

    protected Document getAggregationFunctionTemplate(ConfigurationDescriptor aConfDesc) {
        return AGGREGATION_FUNCTION_TEMPLATE;
    }

    protected Document getPartitionGroupTemplate(ConfigurationDescriptor aConfDesc) {
        return PARTITION_GROUP_TEMPLATE;
    }

    protected Document getReportTemplate(ConfigurationDescriptor aConfDesc) {
        return REPORT_TEMPLATE;
    }

    protected static String getLabelTagString(String aFieldname) {
        return "<p:field name='" + aFieldname + "' style='label'/>";
    }

    protected static String getFieldTagString(String aFieldname) {
        return getFieldTagString(aFieldname, true);
    }

    protected static String getFieldTagString(String aFieldname, boolean withError) {
    	String theResult = "<p:field name='" + aFieldname + "' />";

    	if (withError) {
    		theResult += "<p:field name='" + aFieldname + "' style='error'/>";
    	}
    	return theResult;
    }
}

