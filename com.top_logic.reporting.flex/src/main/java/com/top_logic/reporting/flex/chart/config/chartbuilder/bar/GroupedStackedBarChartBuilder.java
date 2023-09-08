/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.bar;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.SubCategoryAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
import org.jfree.chart.text.TextBlock;
import org.jfree.chart.text.TextBlockAnchor;
import org.jfree.chart.text.TextLine;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.category.CategoryDataset;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.reporting.flex.chart.config.UniqueName;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.dataset.AbstractDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.CategoryDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.GenericCategoryDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.tooltip.CategoryToolTipGeneratorProvider;
import com.top_logic.reporting.flex.chart.config.util.KeyCompare;
import com.top_logic.util.TLContext;

/**
 * {@link BarChartBuilder} that builds grouped-stacked-bar-charts.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class GroupedStackedBarChartBuilder extends BarChartBuilder {

	/**
	 * Config-interface for {@link GroupedStackedBarChartBuilder}.
	 */
	public interface Config extends BarChartBuilder.Config {

		@Override
		@ClassDefault(GroupedStackedBarChartBuilder.class)
		public Class<? extends GroupedStackedBarChartBuilder> getImplementationClass();

		@Override
		@BooleanDefault(false)
		public boolean getShowPeaks();

		/**
		 * the additional label at the x-axis
		 */
		@InstanceFormat
		public ResKey getSubCategoryLabelKey();

		/**
		 * the indexes to build the column-key, see {@link GenericCategoryDatasetBuilder}
		 */
		@Format(CommaSeparatedStrings.class)
		@Name("column-keys")
		@FormattedDefault("0")
		public List<String> getColKeyIndexes();

		/**
		 * the indexes to build the row-key, see {@link GenericCategoryDatasetBuilder}
		 */
		@Format(CommaSeparatedStrings.class)
		@Name("row-keys")
		@FormattedDefault("1,2")
		public List<String> getRowKeyIndexes();

		/**
		 * the index in the row-key-tuple that is used for getting the color
		 */
		@IntDefault(1)
		public int getColorKeyIndex();

		/**
		 * the index in the row-key-tuple that is used for grouping
		 */
		@IntDefault(0)
		public int getGroupKeyIndex();

		@Override
		@InstanceDefault(GroupedStackedBarToolTipGeneratorProvider.class)
		public CategoryToolTipGeneratorProvider getTooltipGeneratorProvider();

		/**
		 * true if a line should mark the sub-categories
		 */
		@BooleanDefault(true)
		public boolean getShowSubCategoryMarker();
	}

	/**
	 * Config-Constructor for {@link GroupedStackedBarChartBuilder}.
	 * 
	 * @param aContext
	 *        - default config-constructor
	 * @param aConfig
	 *        - default config-constructor
	 */
	public GroupedStackedBarChartBuilder(InstantiationContext aContext, Config aConfig) {
		super(aContext, aConfig);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	protected JFreeChart internalCreateChart(ChartContext context, ChartData<CategoryDataset> chartData) {
		PlotOrientation orientation = getOrientation();
		JFreeChart result = ChartFactory.createStackedBarChart(getTitle(), getXAxisLabel(),
			getYAxisLabel(), chartData.getDataset(), orientation, getConfig().getShowLegend(),
			getConfig().getShowTooltips(), false);
		return result;
	}

	private UniqueName getRowKey(Comparable<?>[] keys) {
		return GenericCategoryDatasetBuilder.getKey(getConfig().getRowKeyIndexes(), keys,
			(AbstractDatasetBuilder<?>) getDatasetBuilder());
	}

	@Override
	public int getMaxDimensions() {
		return 3;
	}

	@Override
	public int getMinDimensions() {
		return 3;
	}

	@Override
	protected void setPeakLabelGenerator(JFreeChart model) {
		CategoryPlot plot = (CategoryPlot) model.getPlot();
		GroupedStackedBarRenderer renderer = (GroupedStackedBarRenderer) plot.getRenderer();
		renderer.setDefaultItemLabelGenerator(newGroupedStackedBarCategoryItemLabelGenerator());
		renderer.setDefaultItemLabelsVisible(true);
		renderer.setPositiveItemLabelPositionFallback(new ItemLabelPosition(
			ItemLabelAnchor.CENTER, TextAnchor.CENTER));
		renderer.setItemMargin(0.10);
	}

	private void parseCategoryChildren(List<ChartNode> children, int level, Comparable<?>[] keys, KeyToGroupMap[] maps) {
		for (int i = 0; i < children.size(); i++) {
			ChartNode child = children.get(i);
			Comparable<?> key = child.getKey();
			keys[level] = key;
			if (child.isLeaf()) {
				UniqueName rowKey = getRowKey(keys);

				Tuple tuple = (Tuple) rowKey.getKey();
				Comparable<?> part0 = (Comparable<?>) tuple.get(getConfig().getGroupKeyIndex());
				Comparable<?> part1 = (Comparable<?>) tuple.get(getConfig().getColorKeyIndex());
				
				if (maps[0] == null) {
					maps[0] = new KeyToGroupMap(part0);
				}
				if (maps[1] == null) {
					maps[1] = new KeyToGroupMap(part1);
				}
				maps[0].mapKeyToGroup(rowKey, part0);
				maps[1].mapKeyToGroup(rowKey, part1);

			} else {
				List<ChartNode> theChildren = child.getChildren();
				parseCategoryChildren(theChildren, level + 1, keys, maps);
			}
		}
	}

	@Override
	protected void adaptChart(JFreeChart model, ChartContext context, ChartData<CategoryDataset> chartData) {
		super.adaptChart(model, context, chartData);
		GroupedStackedBarRenderer renderer = new GroupedStackedBarRenderer();
		CategoryPlot plot = (CategoryPlot) model.getPlot();
		plot.setRenderer(renderer);
	}

	private String getSubCategoryLabel() {
		return getTranslationNotEmpty(getConfig().getSubCategoryLabelKey());
	}

	@Override
	public void modifyPlot(JFreeChart chart, ChartContext context, ChartData<CategoryDataset> chartData) {
		if (chartData.getModel().getDepth() < 3) {
			return;
		}
		super.modifyPlot(chart, context, chartData);
		CategoryPlot catPlot = (CategoryPlot) chart.getPlot();

		CategoryDataset dataset = catPlot.getDataset();

		GroupedStackedBarRenderer renderer = (GroupedStackedBarRenderer) catPlot.getRenderer();

		setLabelProvider(getLabelProvider(0), dataset.getColumnKeys());

		SubCategoryAxis domainAxis = getSubCategoryAxis();
		domainAxis.setCategoryMargin(0.05);

		ChartTree tree = chartData.getModel();
		ChartNode root = tree.getRoot();
		List<ChartNode> children = root.getChildren();
		KeyToGroupMap[] maps = new KeyToGroupMap[2];

		Comparable<?>[] array = new Comparable[tree.getDepth()];

		parseCategoryChildren(children, 0, array, maps);
		KeyToGroupMap map = maps[0];
		KeyToGroupMap colorMap = maps[1];

		Set<Object> set = new LinkedHashSet<>();
		for (Object rowKey : dataset.getRowKeys()) {
			UniqueName name = (UniqueName) rowKey;
			Tuple pair = (Tuple) name.getKey();
			set.add(pair.get(0));
			Object group = colorMap.getGroup(name);
			Paint color = getColor(group);
			int rowIndex = dataset.getRowIndex((Comparable<?>) rowKey);
			renderer.setSeriesPaint(rowIndex, color);
			renderer.setSeriesItemLabelPaint(rowIndex, getLabelPaint(color));
		}
		for (Object obj : set) {
			UniqueName key = new UniqueName((Comparable<?>) obj);
			key.setProvider(getLabelProvider(1));
			domainAxis.addSubCategory(key);
		}

		renderer.setSeriesToGroupMap(map);
		renderer.setItemMargin(0.1);

		catPlot.setDomainAxis(domainAxis);
		catPlot.setFixedLegendItems(createLegendItems(colorMap));

	}

	/**
	 * Heuristic method to compute a human readable text color for a given background color.
	 */
	protected Paint getLabelPaint(Paint bgColor) {
		if (bgColor instanceof GradientPaint) {
			bgColor = ((GradientPaint) bgColor).getColor1();
		}
		if (bgColor instanceof Color) {
			Color color = (Color) bgColor;
			if (color.getRed() > 175 || color.getGreen() > 125 /* || bgColor.getBlue() > 200 */) {
				return Color.BLACK;
			}
			return Color.WHITE;
		}
		return Color.BLACK;
	}

	private SubCategoryAxis getSubCategoryAxis() {
		final boolean showSubCategoryMarker = getConfig().getShowSubCategoryMarker();
		SubCategoryAxis domainAxis = new SubCategoryAxis(getSubCategoryLabel()) {

			@SuppressWarnings("rawtypes")
			@Override
			protected TextBlock createLabel(Comparable category, final float width, RectangleEdge edge, Graphics2D g2) {
				if (showSubCategoryMarker) {
					final TextBlock theBlock = super.createLabel(category, width, edge, g2);
					final Paint thePaint = this.getPaint(category);
					TextBlock theResult = new TextBlock() {

						@Override
						public void draw(final Graphics2D g2d, float anchorX, float anchorY, TextBlockAnchor anchor,
								float rotateX, float rotateY, double angle) {
							int theX = (int) (anchorX - width / 2);
							int theY = (int) anchorY - 2;

							Color oldColor = g2d.getColor();
							g2d.setColor((Color) thePaint);
							g2d.fillRect(theX, theY, (int) width, 2);
							g2d.setColor(oldColor);
							theBlock.draw(g2d, anchorX, anchorY, anchor, rotateX, rotateY, angle);
						}
					};

					for (Object theLine : theBlock.getLines()) {
						theResult.addLine((TextLine) theLine);
					}

					return theResult;
				}
				else {
					return super.createLabel(category, width, edge, g2);
				}
			}

			/**
			 * @param aCategory
			 *        the category to get the color for
			 */
			private Paint getPaint(Comparable<?> aCategory) {
				return Color.BLACK; // getColor(aCategory);
			}

		};
		return domainAxis;
	}

	@Override
	protected void initColors(JFreeChart model, ChartData<CategoryDataset> chartData) {
		// no default-handling - needs special handling for grouped keys, will be done in modifyPlot
	}

	private LegendItemCollection createLegendItems(KeyToGroupMap colorMap) {
		LegendItemCollection result = new LegendItemCollection();
		List groups = colorMap.getGroups();
		Collections.sort(groups, KeyCompare.INSTANCE);
		for (Object group : groups) {
			result.add(new LegendItem(getLabelProvider(2).getLabel(group), getColor(group)));
		}
		return result;
	}

	public static Config item() {
		return TypedConfiguration.newConfigItem(Config.class);
	}

	public static GroupedStackedBarChartBuilder instance() {
		return (GroupedStackedBarChartBuilder) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(item());
	}

	/**
	 * Suppresses labels of 0.0 values to avoid text overlapping for empty bars.
	 * 
	 * @see #newStandardCategoryItemLabelGenerator()
	 */
	public static StandardCategoryItemLabelGenerator newGroupedStackedBarCategoryItemLabelGenerator() {
		Locale userLocale = TLContext.getContext().getCurrentLocale();
		NumberFormat format = NumberFormat.getInstance(userLocale);
		NumberFormat percentFormat = NumberFormat.getPercentInstance(userLocale);
		String labelFormat = StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING;
		return new StandardCategoryItemLabelGenerator(labelFormat, format, percentFormat) {
			@Override
			public String generateLabel(CategoryDataset dataset, int row, int column) {
				/* IGNORE FindBugs(FE_FLOATING_POINT_EQUALITY): special handling only if this is
				 * exactly 0. */
				if (Utils.getdoubleValue(dataset.getValue(row, column)) == 0.0) {
					return StringServices.EMPTY_STRING;
				} else {
					return super.generateLabel(dataset, row, column);
				}
			}
		};
	}

	/**
	 * {@link CategoryToolTipGeneratorProvider} for grouped-stacked-bar-charts.
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	public static class GroupedStackedBarToolTipGeneratorProvider implements CategoryToolTipGeneratorProvider {

		/**
		 * Static <code>INSTANCE</code>
		 */
		public static final GroupedStackedBarToolTipGeneratorProvider INSTANCE =
			new GroupedStackedBarToolTipGeneratorProvider();

		/**
		 * Creates a new {@link GroupedStackedBarToolTipGeneratorProvider} - use {@link #INSTANCE}
		 * instead.
		 */
		private GroupedStackedBarToolTipGeneratorProvider() {
		}

		@Override
		public CategoryToolTipGenerator getCategoryTooltipGenerator(JFreeChart model, ChartContext context,
				ChartData<? extends CategoryDataset> chartData) {
			return GroupedStackedBarToolTipGenerator.INSTANCE;
		}
	}

	/**
	 * Default tooltip-generator for grouped-stacked-bar-charts. Creates tooltips containing all
	 * keys and the resulting number-value.
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	public static class GroupedStackedBarToolTipGenerator implements CategoryToolTipGenerator {

		/**
		 * Static <code>INSTANCE</code>
		 */
		public static final GroupedStackedBarToolTipGenerator INSTANCE = new GroupedStackedBarToolTipGenerator();

		@Override
		public String generateToolTip(CategoryDataset dataset, int row, int column) {
			Object dataKey = CategoryDatasetBuilder.toDataKey(dataset, row, column);
			return String.valueOf(dataKey + ": " + dataset.getValue(row, column));
		}
	}
}
