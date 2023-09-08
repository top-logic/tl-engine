/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.model;

import java.util.Map;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.layout.ResPrefix;


/**
 * Constants for the Gantt chart.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public interface GanttChartConstants {

	/** Resource prefix for additional columns. */
	public static final ResPrefix RES_PREFIX_FOR_COLUMNS = I18NConstants.GANTT_COLUMNS;


	/** Filter property for start date. */
	public static final String PROPERTY_START_DATE = "propertyStartDate";

	/** Filter property for end date. */
	public static final String PROPERTY_END_DATE = "propertyEndDate";

	/** Filter property for using dates from context element */
	public static final String PROPERTY_USE_CONTEXT_DATES = "propertyUseContextDates";

	/** Filter property for show forecast flag. */
	public static final String PROPERTY_SHOW_FORECAST = "propertyShowForecast";

	/** Filter property for show root of the tree. */
	public static final String PROPERTY_SHOW_ROOT = "propertyShowRoot";

	/** Filter property for depth. */
	public static final String PROPERTY_DEPTH = "propertyDepth";

	/** Filter property for show-parent-elements. */
	public static final String PROPERTY_SHOW_PARENT_ELEMENTS = "showParentElements";

	/** Filter property for show-report-lines. */
	public static final String PROPERTY_SHOW_REPORT_LINES = "showReportLines";

	/** Filter property for show-dependencies. */
	public static final String PROPERTY_SHOW_DEPENDENCIES = "showDependencies";
	
	/** Filter property for show-meetings. */
	public static final String PROPERTY_SHOW_MEETINGS = "showMeetings";
	
	/** Filter property for additional-columns. */
	public static final String PROPERTY_COMMITTEES = "committees";

	/** Filter property for show-additional-columns. */
	public static final String PROPERTY_SHOW_ADDITIONAL_COLUMNS = "showAdditionalColumns";

	/** Filter property for show-duration-to-milestone. */
	public static final String PROPERTY_SHOW_DURATION_TO_MS = "showDurationToMS";

	/** Filter property for hide-node-date-ranges. */
	public static final String PROPERTY_HIDE_NODE_DATE_RANGES = "hideNodeDateRanges";

	/** Filter property for hide-start-end-label. */
	public static final String PROPERTY_HIDE_START_END_LABEL = "hideStartEndLabel";

	/** Filter property for hide-finished-elements. */
	public static final String PROPERTY_HIDE_FINISHED_ELEMENTS = "hideFinishedElements";

	/** Filter property for hide-finished-elements. */
	public static final String PROPERTY_DISABLE_FINISHED_ELEMENTS = "disableFinishedElements";

	/** Filter property for add-collision-avoiding-rows. */
	public static final String PROPERTY_ADD_COLLISION_AVOIDING_ROWS = "addCollisionAvoidingRows";

	/** Filter property for additional-columns. */
	public static final String PROPERTY_ADDITIONAL_COLUMNS = "additionalColumns";

	/** Filter property for additional-columns. */
	public static final String PROPERTY_TARGET_MILESTONE = "targetMilestone";

	/** Filter property for scaling-option. */
	public static final String PROPERTY_SCALING_OPTION = "scalingOption";

	/** Filter property for scaling-granularity. */
	public static final String PROPERTY_SCALING_GRANULARITY = "scalingGranularity";


	/** Name of the scaling option fit to window */
	public static final String SCALING_OPTION_WINDOW = "scalingOptionWindow";

	/** Name of the scaling option auto */
	public static final String SCALING_OPTION_AUTO = "scalingOptionAuto";

	/** Name of the scaling option manual */
	public static final String SCALING_OPTION_MANUAL = "scalingOptionManual";


	/** Additional column start date. */
	public static final String COLUMN_START_DATE = "startDate";

	/** Additional column end date. */
	public static final String COLUMN_END_DATE = "endDate";

	/** Additional column responsible. */
	public static final String COLUMN_RESPONSIBLE = "responsible";

	/** Additional column responsible. */
	public static final String COLUMN_STATE = "state";


	/** Map containing the width of each column. */
	public static final Map<String, Integer> COLUMN_MAP = new MapBuilder<String, Integer>(true)
		.put(COLUMN_START_DATE, 85)
		.put(COLUMN_END_DATE, 85)
		.put(COLUMN_RESPONSIBLE, 155)
		.put(COLUMN_STATE, 50)
	.toMap();

}
