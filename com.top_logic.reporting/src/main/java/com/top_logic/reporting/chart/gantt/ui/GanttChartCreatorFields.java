/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.ui;

import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.reporting.chart.gantt.model.GanttChartConstants;
import com.top_logic.reporting.chart.gantt.model.GanttChartSettings;
import com.top_logic.reporting.chart.gantt.model.GanttEvent;
import com.top_logic.reporting.chart.gantt.model.GanttObject;
import com.top_logic.reporting.chart.gantt.model.GanttRow;
import com.top_logic.reporting.chart.gantt.model.TimeGranularity;
import com.top_logic.reporting.view.component.property.FilterProperty;

/**
 * Holds variables required while computing the gantt chart.
 *
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class GanttChartCreatorFields implements GanttChartConstants {

	/** Holds the filter settings of the filter component. */
	private GanttChartSettings _filterSettings;

	/** Flag indicating whether the currently selected scaling width is accepted or not */
	private boolean _scalingWidthAccepted = true;

	/** Flag indicating whether the currently selected scaling height is accepted or not */
	private boolean _scalingHeightAccepted = true;


	/** Mandatory start time */
	private Date _timeFrom;

	/** Mandatory end time */
	private Date _timeTo;

	/** Mandatory time interval for calendar */
	private TimeGranularity _tInterval;

	/** Optional sub interval for calendar. */
	private TimeGranularity _tSubInterval;

	/** Target date for the duration headline. */
	private Date _targetDate;

	/** Window / image width. */
	private int _windowWidth;


	/** Node list (row list). */
	private List<GanttRow> _rows;

	/** DateLine (meetings) set sorted by date. */
	private TreeSet<GanttEvent> _dateLines;

	/** Holds the chart entities (link and tooltip information) for additional objects e.g. the objects in additional columns. */
	private List<GanttObject> _entities;

	/** List of blocking infos. */
	private List<BlockingInfo> _blockingInfo;

	/** Date line descriptions to draw into the chart footer. */
	private InstructionGraphics2D _dateLineDescriptions;


	/** Image width */
	private int _chartWidth;

	/** Computed tree column width */
	private int _treeColumnWidth = 0;

	/** Computed additional columns width */
	private int _additionalColumnsWidth = 0;

	/** Computed tree width */
	private int _treeWidth = 0;

	/** Used to avoid long runtime by skipping collision detection */
	private int _pathCounter;

	/** Computed number of nodes fitting vertically on one page */
	private int _nodesPerPage = 0;

	/** Computed additional depth caused by the {@link #showParentElements()} option. */
	private int _parentElementCount = 0;


	// Options

	/** Show MS forecast date instead of planned date. */
	public int depth() {
		return _filterSettings.getInteger(PROPERTY_DEPTH);
	}

	/** Show MS forecast date instead of planned date. */
	public boolean showMSForecast() {
		return _filterSettings.getBoolean(PROPERTY_SHOW_FORECAST);
	}

	/** Show parent elements of the start element also. */
	public boolean showParentElements() {
		return _filterSettings.getBoolean(PROPERTY_SHOW_PARENT_ELEMENTS);
	}

	/** Show duration to end in header row. */
	public boolean showDurationToMS() {
		return _filterSettings.getBoolean(PROPERTY_SHOW_DURATION_TO_MS);
	}

	/** Show Report lines for milestones. */
	public boolean showReportLines() {
		return _filterSettings.getBoolean(PROPERTY_SHOW_REPORT_LINES);
	}

	/** Show dependencies */
	public boolean showDependencies() {
		return _filterSettings.getBoolean(PROPERTY_SHOW_DEPENDENCIES);
	}

	/** Hide node date ranges. */
	public boolean hideNodeDateRanges() {
		return _filterSettings.getBoolean(PROPERTY_HIDE_NODE_DATE_RANGES);
	}

	/** Hide labels of start and end milestones. */
	public boolean hideStartEndLabel() {
		return _filterSettings.getBoolean(PROPERTY_HIDE_START_END_LABEL);
	}

	/** Hide finished elements. */
	public boolean hideFinishedElements() {
		return _filterSettings.getBoolean(PROPERTY_HIDE_FINISHED_ELEMENTS);
	}

	/** Disable finished elements. */
	public boolean disableFinishedElements() {
		return _filterSettings.getBoolean(PROPERTY_DISABLE_FINISHED_ELEMENTS);
	}

	/** Add additional rows when there are overlapping NodeDatas (milestones). */
	public boolean addCollisionAvoidingRows() {
		return _filterSettings.getBoolean(PROPERTY_ADD_COLLISION_AVOIDING_ROWS);
	}

	/** Show additional columns. */
	public boolean showAdditionalColumns() {
		return _filterSettings.getBoolean(PROPERTY_SHOW_ADDITIONAL_COLUMNS);
	}

	/** Selected additional columns. */
	@SuppressWarnings("unchecked")
	public List<String> additionalColumns() {
		return (List<String>) _filterSettings.getValue(PROPERTY_ADDITIONAL_COLUMNS);
	}

	/** The selected scaling option. */
	public String scalingOption() {
		return (String) CollectionUtil.getFirst(_filterSettings.getValue(PROPERTY_SCALING_OPTION));
	}

	/** The selected scaling granularity. */
	public TimeGranularity scalingGranularity() {
		return (TimeGranularity) CollectionUtil.getFirst(_filterSettings.getValue(PROPERTY_SCALING_GRANULARITY));
	}

	/** Show milestones as icons (text rectangles otherwise). */
	public boolean showMilestoneIcons() {
		return true;
	}

	/** Draw blocking infos as rectangles (use for debugging purposes). */
	public boolean drawBlockingInfos() {
		return false;
	}

	/** Show a row for the root element in the chart or not. */
	public boolean showRoot() {
		FilterProperty property = _filterSettings.getProperty(PROPERTY_SHOW_ROOT);
		if (property == null) {
			// property not set, default value is "show root"
			return true;
		}
		Object value = property.getValue();
		return !(value instanceof Boolean) || ((Boolean) value).booleanValue();
	}

	/**
	 * Gets the maximum tree depth to show.
	 */
	public int getMaxDepth() {
		return depth() + _parentElementCount;
	}


	/**
	 * @see #_filterSettings
	 */
	public GanttChartSettings getFilterSettings() {
		return _filterSettings;
	}

	/**
	 * @see #_filterSettings
	 */
	public void setFilterSettings(GanttChartSettings filterSettings) {
		_filterSettings = filterSettings;
	}

	/**
	 * @see #_scalingWidthAccepted
	 */
	public boolean isScalingWidthAccepted() {
		return _scalingWidthAccepted;
	}

	/**
	 * @see #_scalingWidthAccepted
	 */
	public void setScalingWidthAccepted(boolean scalingWidthAccepted) {
		_scalingWidthAccepted = scalingWidthAccepted;
	}

	/**
	 * @see #_scalingHeightAccepted
	 */
	public boolean isScalingHeightAccepted() {
		return _scalingHeightAccepted;
	}

	/**
	 * @see #_scalingHeightAccepted
	 */
	public void setScalingHeightAccepted(boolean scalingHeightAccepted) {
		_scalingHeightAccepted = scalingHeightAccepted;
	}

	/**
	 * @see #_timeFrom
	 */
	public Date getTimeFrom() {
		return _timeFrom;
	}

	/**
	 * @see #_timeFrom
	 */
	public void setTimeFrom(Date timeFrom) {
		_timeFrom = timeFrom;
	}

	/**
	 * @see #_timeTo
	 */
	public Date getTimeTo() {
		return _timeTo;
	}

	/**
	 * @see #_timeTo
	 */
	public void setTimeTo(Date timeTo) {
		_timeTo = timeTo;
	}

	/**
	 * @see #_tInterval
	 */
	public TimeGranularity gettInterval() {
		return _tInterval;
	}

	/**
	 * @see #_tInterval
	 */
	public void settInterval(TimeGranularity tInterval) {
		_tInterval = tInterval;
	}

	/**
	 * @see #_tSubInterval
	 */
	public TimeGranularity gettSubInterval() {
		return _tSubInterval;
	}

	/**
	 * @see #_tSubInterval
	 */
	public void settSubInterval(TimeGranularity tSubInterval) {
		_tSubInterval = tSubInterval;
	}

	/**
	 * @see #_targetDate
	 */
	public Date getTargetDate() {
		return _targetDate;
	}

	/**
	 * @see #_targetDate
	 */
	public void setTargetDate(Date targetDate) {
		_targetDate = targetDate;
	}

	/**
	 * @see #_windowWidth
	 */
	public int getWindowWidth() {
		return _windowWidth;
	}

	/**
	 * @see #_windowWidth
	 */
	public void setWindowWidth(int windowWidth) {
		_windowWidth = windowWidth;
	}

	/**
	 * @see #_rows
	 */
	public List<GanttRow> getRows() {
		return _rows;
	}

	/**
	 * @see #_rows
	 */
	public void setRows(List<GanttRow> rows) {
		_rows = rows;
	}

	/**
	 * @see #_dateLines
	 */
	public TreeSet<GanttEvent> getDateLines() {
		return _dateLines;
	}

	/**
	 * @see #_dateLines
	 */
	public void setDateLines(TreeSet<GanttEvent> dateLines) {
		_dateLines = dateLines;
	}

	/**
	 * @see #_entities
	 */
	public List<GanttObject> getEntities() {
		return _entities;
	}

	/**
	 * @see #_entities
	 */
	public void setEntities(List<GanttObject> entities) {
		_entities = entities;
	}

	/**
	 * @see #_blockingInfo
	 */
	public List<BlockingInfo> getBlockingInfo() {
		return _blockingInfo;
	}

	/**
	 * @see #_blockingInfo
	 */
	public void setBlockingInfo(List<BlockingInfo> blockingInfo) {
		_blockingInfo = blockingInfo;
	}

	/**
	 * @see #_dateLineDescriptions
	 */
	public InstructionGraphics2D getDateLineDescriptions() {
		return _dateLineDescriptions;
	}

	/**
	 * @see #_dateLineDescriptions
	 */
	public void setDateLineDescriptions(InstructionGraphics2D dateLineDescriptions) {
		_dateLineDescriptions = dateLineDescriptions;
	}

	/**
	 * @see #_chartWidth
	 */
	public int getChartWidth() {
		return _chartWidth;
	}

	/**
	 * @see #_chartWidth
	 */
	public void setChartWidth(int chartWidth) {
		_chartWidth = chartWidth;
	}

	/**
	 * @see #_treeColumnWidth
	 */
	public int getTreeColumnWidth() {
		return _treeColumnWidth;
	}

	/**
	 * @see #_treeColumnWidth
	 */
	public void setTreeColumnWidth(int treeColumnWidth) {
		_treeColumnWidth = treeColumnWidth;
	}

	/**
	 * @see #_additionalColumnsWidth
	 */
	public int getAdditionalColumnsWidth() {
		return _additionalColumnsWidth;
	}

	/**
	 * @see #_additionalColumnsWidth
	 */
	public void setAdditionalColumnsWidth(int additionalColumnsWidth) {
		_additionalColumnsWidth = additionalColumnsWidth;
	}

	/**
	 * @see #_treeWidth
	 */
	public int getTreeWidth() {
		return _treeWidth;
	}

	/**
	 * @see #_treeWidth
	 */
	public void setTreeWidth(int treeWidth) {
		_treeWidth = treeWidth;
	}

	/**
	 * @see #_pathCounter
	 */
	public int getPathCounter() {
		return _pathCounter;
	}

	/**
	 * @see #_pathCounter
	 */
	public void setPathCounter(int pathCounter) {
		_pathCounter = pathCounter;
	}

	/**
	 * @see #_nodesPerPage
	 */
	public int getNodesPerPage() {
		return _nodesPerPage;
	}

	/**
	 * @see #_nodesPerPage
	 */
	public void setNodesPerPage(int nodesPerPage) {
		_nodesPerPage = nodesPerPage;
	}

	/**
	 * @see #_parentElementCount
	 */
	public int getParentElementCount() {
		return _parentElementCount;
	}

	/**
	 * @see #_parentElementCount
	 */
	public void setParentElementCount(int parentElementCount) {
		_parentElementCount = parentElementCount;
	}

}
