/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.ui;

import java.awt.Color;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FloatDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.gui.Theme;
import com.top_logic.layout.Icons;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.form.format.ColorConfigFormat;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.reporting.chart.gantt.I18NConstants;

/**
 * Configuration options for {@link AbstractGanttChartCreator}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface GanttChartConfig extends ConfigurationItem {

	/**
	 * Default font name, if not customized by the {@link Theme}.
	 * 
	 * @see Icons#TEXT_FAMILY
	 */
	@StringDefault("Arial")
	String getFont();

	/**
	 * Default font size, if not customized by the {@link Theme}.
	 * 
	 * @see Icons#FONT_SIZE_IMAGE
	 */
	@IntDefault(12)
	int getFontSize();

	/**
	 * Returns the mapping of element type names to {@link ColorConfig}s that should be used for
	 * node date ranges in gantt chart.
	 */
	@Key(ColorConfig.NAME_ATTRIBUTE)
	@EntryTag("color-config")
	Map<String, GanttChartConfig.ColorConfig> getElementTypeColors();

	/**
	 * {@link ResPrefix} for I18N.
	 * 
	 * @see I18NConstants#GENERATOR Default used, if nothing is set.
	 */
	@Nullable
	@InstanceFormat
	ResPrefix getResourcePrefix();

	/**
	 * {@link ResourceProvider} for generating links, labels and tooltips for displayed business
	 * objects.
	 */
	@InstanceFormat
	@InstanceDefault(MetaResourceProvider.class)
	ResourceProvider getResourceProvider();

	/**
	 * Factor of the row height in multiples of the font height.
	 */
	@FloatDefault(2)
	float getRowHeightFactor();

	/**
	 * Maximum width of the generated image in pixels
	 */
	@IntDefault(13000)
	// Enough for a year in day granularity inclusive additional columns
	int getMaxImageWidth();

	/**
	 * Maximum image height of the generated image in pixels.
	 */
	@IntDefault(Integer.MAX_VALUE)
	int getMaxImageHeight();

	/**
	 * Default width of the generated image in pixels, if no width can be determined neither
	 * from content (error image) nor form the environment (window widh).
	 */
	@IntDefault(650)
	int getDefaultImageWidth();

	/**
	 * Minimum height of in image generated for error display.
	 */
	@IntDefault(250)
	int getMinErrorImageHeight();

	/**
	 * Minimum column width used for chart columns (quarters, months, weeks, days)
	 */
	@IntDefault(34)
	int getMinColumnWidth();

	/**
	 * Color for current date line
	 */
	@Format(ColorConfigFormat.class)
	@FormattedDefault("#FFC800")
	Color getCurrentDateColor();

	/**
	 * Default background color.
	 */
	@FormattedDefault("#FFFFFF")
	@Format(ColorConfigFormat.class)
	Color getBackgroundColor();

	/**
	 * Default foreground color.
	 */
	@FormattedDefault("#000000")
	@Format(ColorConfigFormat.class)
	Color getForegroundColor();

	/**
	 * Background color for even rows.
	 */
	@FormattedDefault("#FFFFFF")
	@Format(ColorConfigFormat.class)
	Color getEvenRowColor();

	/**
	 * Background color for odd rows.
	 */
	@FormattedDefault("#DEE9F2")
	@Format(ColorConfigFormat.class)
	Color getOddRowColor();

	/**
	 * Color for dependency arrows.
	 */
	@FormattedDefault("#000000")
	@Format(ColorConfigFormat.class)
	Color getDependencyColor();

	/**
	 * Color for dependency arrows with conflicts.
	 */
	@FormattedDefault("#FF0000")
	@Format(ColorConfigFormat.class)
	Color getDependencyConflictColor();

	/**
	 * Color for disabled nodes.
	 */
	@FormattedDefault("#808080")
	@Format(ColorConfigFormat.class)
	Color getDisabledTextColor();

	/**
	 * Color for navigation nodes.
	 */
	@FormattedDefault("#C0C0C0")
	@Format(ColorConfigFormat.class)
	Color getNavigationTextColor();

	/**
	 * Color of period separator lines.
	 */
	@FormattedDefault("#C0C0C0")
	@Format(ColorConfigFormat.class)
	Color getPeriodSeparatorLineColor();

	/**
	 * Option whether to show grid lines (period separator lines) or not.
	 */
	GridLineType getGridLineType();

	/**
	 * Width of date lines.
	 */
	@IntDefault(3)
	int getDateLineWidth();

	/**
	 * Thickness of frames.
	 */
	@IntDefault(1)
	int getFrameSize();

	/**
	 * Horizontal space between texts.
	 */
	@IntDefault(2)
	int getHorizontalTextSpace();

	/**
	 * Vertical space between texts.
	 */
	@IntDefault(3)
	int getVerticalTextSpace();

	/**
	 * Height of the date ranges of nodes.
	 */
	@IntDefault(10)
	int getNodeDateRangeHeight();

	/**
	 * Indent with per depth in tree.
	 */
	@IntDefault(20)
	int getIndentWidthPerDepth();

	/**
	 * Maximum additional rows for collision avoiding.
	 */
	@IntDefault(10)
	int getMaxCollisionAvoidingRows();

	/**
	 * Indent with per depth in tree.
	 */
	@BooleanDefault(false)
	boolean getAddBlockingInfoToLabels();


	/**
	 * Configuration of colors for elements in Gantt chart.
	 */
	public static interface ColorConfig extends ConfigurationItem {
		/** The name option of this {@link ColorConfig}. */
		String NAME_ATTRIBUTE = "elementName";

		/**
		 * Returns the name of this {@link ColorConfig} which represents the element type this
		 * {@link ColorConfig} is used for.
		 */
		@Name(NAME_ATTRIBUTE)
		String getName();

		/**
		 * Returns the start color of this {@link ColorConfig}.
		 */
		@Format(ColorConfigFormat.class)
		Color getStartColor();

		/**
		 * Returns the end color of this {@link ColorConfig}.
		 */
		@Format(ColorConfigFormat.class)
		Color getEndColor();

		/**
		 * Returns the border color of this {@link ColorConfig}.
		 */
		@Format(ColorConfigFormat.class)
		Color getBorderColor();
	}

	/**
	 * Supported grid line types.
	 * 
	 * @see GanttChartConfig#getGridLineType()
	 */
	public static enum GridLineType implements ExternallyNamed {

		/**
		 * Show grid lines for first interval.
		 */
		INTERVAL("interval"),

		/**
		 * Show grid lines for sub interval.
		 */
		SUBINTERVAL("sub-interval"),

		/**
		 * Don't show grid lines.
		 */
		NONE("none");

		private final String _externalName;

		private GridLineType(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

}