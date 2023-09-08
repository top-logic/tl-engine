/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.util;

import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.RectangleEdge;

/**
 * The ChartConstants contains constants.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public interface ChartConstants {

    /** The img-tag has "chart" as default value. If you
     *  use the default chart.jsp with the default img-tag 
     *  you can get the image control over the id 
     *  (component name + CHART_CONTROL_SUFFIX. */
    public static final String CHART_CONTROL_SUFFIX = "chart";
    
    public static final String NO_DATA = "reporting.chart.noData";
    
    /** @deprecated This constant can be used as key for the chart type.  */
    @Deprecated
	public static final String CHART_TYPE     = "chartType";
    /** The bar chart type. */
    public static final String BAR_CHART_TYPE = "barChartType";
    /** The pie chart type. */
    public static final String PIE_CHART_TYPE = "pieChartType";
	/** The achievement chart type. */
	public static final String ACHIEVEMENT_CHART_TYPE = "achievement";
	/** The default chart type. */
	public static final String DEFAULT_CHART_TYPE = "default";
	/** The kpitracing chart type. */
	public static final String KPITRACING_CHART_TYPE = "kpitracing";
	/** The matrix chart type. */
	public static final String MATRIX_CHART_TYPE = "matrix";
	/** The spider chart type. */
	public static final String SPIDER_CHART_TYPE = "spider";
	/** The swing chart type. */
	public static final String SWING_CHART_TYPE = "swing";
	/** The trend chart type. */
	public static final String TREND_CHART_TYPE = "trend";
    
    /** Indicates whether the tooltips are shown. */
    public static final boolean TOOLTIPS    = true;
    /** Indicates whether the legend is shown. */
    public static final boolean LEGEND      = true;
    /** Indicates whether the urls are shown. */
    public static final boolean URLS        = true;
    /** Indicates whether the item labels are shown. */
    public static final boolean ITEM_LABELS = true;
    
    /** Legend position. */
    public static final RectangleEdge LEGEND_TOP    = RectangleEdge.TOP;
    /** Legend position. */
    public static final RectangleEdge LEGEND_BOTTOM = RectangleEdge.BOTTOM;
    /** Legend position. */
    public static final RectangleEdge LEGEND_LEFT   = RectangleEdge.LEFT;
    /** Legend position. */
    public static final RectangleEdge LEGEND_RIGHT  = RectangleEdge.RIGHT;
    
    /** Plot orientation. */
    public static final PlotOrientation PLOT_VERTICAL   = PlotOrientation.VERTICAL;
    /** Plot orientation. */
    public static final PlotOrientation PLOT_HORIZONTAL = PlotOrientation.HORIZONTAL;
    

    /** The image id can be used to distinguish charts. Default is the
     *  image id form the tag of the jsp-page. */
    public static final String VALUE_IMAGE_ID    = "imageId";
    
    /** A value for the creation of a chart. */
    public static final String VALUE_LABEL_TITLE  = "labelTitle";
    /** A value for the creation of a chart. */
    public static final String VALUE_LABEL_AXIS_X = "labelXAxis";
    /** A value for the creation of a chart. */
    public static final String VALUE_LABEL_AXIS_Y = "labelYAxis";
    
    /** A value that indicates that the chart WITHOUT the legend is rendered as square. */
    public static final String VALUE_WRITE_SQUARE_CHART = "value.write.square.chart";
}

