/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.util;

import com.top_logic.base.chart.util.ChartConstants;
import com.top_logic.layout.ResPrefix;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.partition.PartitionFunctionConfiguration;


/**
 * The {@link ReportConstants} contains only useful report constants.
 *
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public interface ReportConstants {

	/** Resource key of the X axis options. */
	public static final String X_AXIS_OPTIONS_I18N_KEY  = "reporting.chart.xaxis.options";
    /** Resource key of the Y axis options. */
    public static final String Y_AXIS_OPTIONS_I18N_KEY  = "reporting.chart.yaxis.options";
    /** Resource key of the display options. */
    public static final String DISPLAY_OPTIONS_I18N_KEY = "reporting.chart.display.options";
    
	/** Resource prefix of the aggregation functions. */
	public static final String AGGREGATION_PREFIX = "reporting.aggregation.";

	/** Resource prefix of the chart components. */
	public static final ResPrefix RES_PREFIX = I18NConstants.CHART;
	/** Name of the configuration option for the type of the reported objects. */
	public static final String REPORT_OBJECT_TYPE = "reportObjects";
	/** Name of the argument holding the ID of the goto handler. */
	public static final String GOTO_HANDLER_ID    = "gotoId";
	
	// Report type constants

	/** Indicates that a report is rendered as a bar chart. */
	public static final String	REPORT_TYPE_BAR_CHART			= "bar-chart";
	/** Indicates that a report is rendered as a pie chart. */
	public static final String	REPORT_TYPE_PIE_CHART			= "pie-chart";
	/** Indicates that a report is rendered as a line chart. */
	public static final String	REPORT_TYPE_LINE_CHART			= "line-chart";
	/** Indicates that a report is rendered as a box and whisker chart. */
	public static final String	REPORT_TYPE_BOXWHISKER_CHART	= "box-chart";
	/** Indicates that a report is rendered as an area chart. */
	public static final String	REPORT_TYPE_AREA_CHART			= "area-chart";
	/** Indicates that a report is rendered as a water fall chart. */
	public static final String	REPORT_TYPE_WATERFALL_CHART		= "waterfall-chart";
	/** Indicates that a report is rendered as a table. */
	public static final String	REPORT_TYPE_TABLE				= "table";
	/** Indicates that a report is rendered as a chart. */
	public static final String	REPORT_TYPE_CHART				= "chart";

	/** The name of the start date. */
	public static final String	START_DATE_FIELD				= "startDate";
	/** The name of the end date. */
	public static final String	END_DATE_FIELD					= "endDate";
	/** The name of the granularity for dates. */
	public static final String	DATE_GRANULARITY_FIELD			= "dateGranularity";
	/** The name of the granularity for numbers. */
	public static final String	NUMBER_GRANULARITY_FIELD		= "numberGranularity";
	
	/** @deprecated The name of the chart type selectField. */
	@Deprecated
	public static final String	CHART_TYPE_FIELD				= ChartConstants.CHART_TYPE;
	
	/** The Name of the time range selection field */
	public static final String	TIME_RANGE_FIELD				= "timeRange";
	/** The name of the aggregation selection field */
	//private static final String	AGGREGATION_FIELD				= "aggregationSelection";
	/** The name of the smartinput field. */
	public static final String	SMART_INPUT_FIELD				= "smartInput";
	/** The name of the checkbox for fixed or relative timeranges */
	public static final String	RELATIVE_RANGES_FIELD			= "relativeRanges";
	/** Field name for additional settings, e.g. relative time ranges */
	public static final String	ADDITIONAL_SETTINGS				= "additionalSettings";
	/** The name of the stored report selector Field */
	public static final String	STORED_REPORT_FIELD				= "storedReports";
	
	/** @deprecated use {@link ReportConfiguration#setPartitionConfiguration(PartitionFunctionConfiguration)} Field name for PartitionFunctionfield */
	@Deprecated
	public static final String	PARTITION_FUNCTIONS_FIELD		= "partitionFunctions";
	
	/** @deprecated use {@link PartitionFunctionConfiguration#setAttribute} Field name for Attribute Field */
	@Deprecated
	public static final String	ATTRIBUTE_FIELD					= "attributes";

	@Deprecated
	public static final String  ATTRIBUTE_SEARCH_META_ELEMENT   = "searchMetaElement";

	/** Name of the field for showing labels. */
	public static final String  SHOW_LABELS_FIELD				= "showLabels";

	@Deprecated
	public static final String  SHOW_HIGHLIGHT_CECK_FIELD		= "showHighlightCheck";
	@Deprecated
	public static final String  SHOW_HIGHLIGHT_FROM_FIELD		= "showHighlightFrom";
	@Deprecated
	public static final String  SHOW_HIGHLIGHT_TO_FIELD			= "showHighlightTo";
	@Deprecated
	public static final String  SHOW_HIGHLIGHT_LABEL_FIELD		= "showHighlightLabel";
	
	
	/** Name of the field selecting the stored query. */
	public static final String  QUERY_SELECTION_FIELD           = "querySelection";
	/** Name of the field selecting the stored report. */
	public static final String  REPORT_SELECTION_FIELD          = "reportSelection";
	
	/** Name of the provider option. */
	public static final String PROVIDER                         = "provider";
	
	// Contract Payment Analysis
	//public static final String PAYMENT_RESPONSIBLE             = "paymentResponsible";
	/** Name of the field for accumulated payments. */
	public static final String PAYMENT_ACCUMULATED             = "paymentAccumulated";
	
	
	
	// Visibility constants
	/** Indicates that something is visible. */
	public static final boolean	VISIBLE							= true;
	/** Indicates that something is invisible. */
	public static final boolean	INVISIBLE						= false;

	// Align constants

	/** Indicates that something is aligned left. */
	public static final String	ALIGN_LEFT						= "left";
	/** Indicates that something is aligned center. */
	public static final String	ALIGN_CENTER					= "center";
	/** Indicates that something is aligned right. */
	public static final String	ALIGN_RIGHT						= "right";
	/** Indicates that something is aligned top. */
	public static final String	ALIGN_TOP						= "top";
	/** Indicates that something is aligned bottom. */
	public static final String	ALIGN_BOTTOM					= "bottom";

	// Orientation constants

	/** Indicates that something is horizontally oriented. */
	public static final String	ORIENTATION_HORIZONTAL			= "horizontal";
	/** Indicates that something is vertically oriented. */
	public static final String	ORIENTATION_VERTICAL			= "vertical";

	// Font style constants

	/** Indicates that a font is displayed plain. */
	public static final String	FONT_STYLE_PLAIN				= "plain";
	/** Indicates that a font is displayed bold. */
	public static final String	FONT_STYLE_BOLD					= "bold";
	/** Indicates that a font is displayed italic. */
	public static final String	FONT_STYLE_ITALIC				= "italic";
	
	
	// attribute definition fields
	/** Name of the group of attribute definitions. */
	public static final String ATTR_DEFINITION_GROUP   = "attributeDefinitions";
	/** Name of the field choosing an attribute. */
	public static final String ATTR_CHOOSER_FIELD      = "attributeChooser";
	/** Name of the table of attribute definitions. */
	public static final String ATTR_DEFINITION_TABLE   = "attributeTable";
	/** Index of the color column in the attribute table. */
	public static final int    ATTR_COLUMN_COLOR       = 0;
	/** Index of the function column in the attribute table. */
	public static final int    ATTR_COLUMN_FUNCTION    = 1;
	/** Index of the attribute column in the attribute table. */
	public static final int    ATTR_COLUMN_ATTR        = 2;

	/** Name of the color column. */
	public static final String   COLUMN_COLOR                            = "column_color";
	/** Name of the function column. */
	public static final String   COLUMN_FUNCTION                         = "column_function";
	/** Name of the attribute column. */
	public static final String   COLUMN_ATTR                             = "column_attr";
	/** Name prefix of fields. */
	public static final String   FIELD_PREFIX                            = "field_";
	/** Name of the field table. */
	public static final String   FIELD_TABLE                             = "field_table";
	/** The columns of the aggregation table. */
	public static final String[] AGGREGATION_COLUMNS                     = {COLUMN_COLOR, COLUMN_FUNCTION, COLUMN_ATTR};
	
	// color definition
	/** Name of the field choosing a color. */
	public static final String COLOR_CHOOSER_FIELD     = "colorChooser";
	
	/** Name prefix of gradient colors. */
	public static final String COLOR_PREFIX = "grad";
	
	/**
	 * The name to be used for creating the new
	 * {@link com.top_logic.reporting.report.wrap.StoredReport}.
	 */
	public static final String	NAME_ATTRIBUTE	 = "name";
	/** Name of the argument holding the report. */
	public static final String	REPORT_ATTRIBUTE = "report";
}
