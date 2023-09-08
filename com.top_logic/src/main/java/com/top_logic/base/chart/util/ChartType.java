/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.util;

import com.top_logic.layout.basic.ThemeImage;

/**
 * A type for charts with its icons.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public enum ChartType {
	/** The bar chart type. */
	ACHIEVEMENT_CHART {
		@Override
		public String getName() {
			return ChartConstants.ACHIEVEMENT_CHART_TYPE;
		}

		@Override
		public ThemeImage getIcon() {
			return Icons.ARCHIEVEMENT_CHART;
		}
	},
	/** The bar chart type. */
	BAR_CHART {
		@Override
		public String getName() {
			return ChartConstants.BAR_CHART_TYPE;
		}

		@Override
		public ThemeImage getIcon() {
			return Icons.BAR_CHART;
		}
	},
	/** The default chart type. */
	DEFAULT_CHART {
		@Override
		public String getName() {
			return ChartConstants.DEFAULT_CHART_TYPE;
		}

		@Override
		public ThemeImage getIcon() {
			return Icons.DEFAULT_CHART;
		}
	},
	/** The kpitracing chart type. */
	KPITRACING_CHART {
		@Override
		public String getName() {
			return ChartConstants.KPITRACING_CHART_TYPE;
		}

		@Override
		public ThemeImage getIcon() {
			return Icons.KPITRACING_CHART;
		}
	},
	/** The matrix chart type. */
	MATRIX_CHART {
		@Override
		public String getName() {
			return ChartConstants.MATRIX_CHART_TYPE;
		}

		@Override
		public ThemeImage getIcon() {
			return Icons.MATRIX_CHART;
		}
	},
	/** The pie chart type. */
	PIE_CHART {
		@Override
		public String getName() {
			return ChartConstants.PIE_CHART_TYPE;
		}

		@Override
		public ThemeImage getIcon() {
			return Icons.PIE_CHART;
		}
	},
	/** The spider chart type. */
	SPIDER_CHART {
		@Override
		public String getName() {
			return ChartConstants.SPIDER_CHART_TYPE;
		}

		@Override
		public ThemeImage getIcon() {
			return Icons.SPIDER_CHART;
		}
	},
	/** The swing chart type. */
	SWING_CHART {
		@Override
		public String getName() {
			return ChartConstants.SWING_CHART_TYPE;
		}

		@Override
		public ThemeImage getIcon() {
			return Icons.SWING_CHART;
		}
	},
	/** The default chart type. */
	TREND_CHART {
		@Override
		public String getName() {
			return ChartConstants.TREND_CHART_TYPE;
		}

		@Override
		public ThemeImage getIcon() {
			return Icons.TREND_CHART;
		}
	};

	/** The name of this chart type. */
	public abstract String getName();

	/** The icon for this chart type. */
	public abstract ThemeImage getIcon();
}
