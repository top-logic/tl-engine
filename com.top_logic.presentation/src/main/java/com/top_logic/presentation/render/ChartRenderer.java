/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.common.usermodel.fonts.FontInfo;
import org.apache.poi.sl.draw.DrawFontManager;
import org.apache.poi.sl.draw.DrawFontManagerDefault;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.util.Units;
import org.apache.poi.xslf.usermodel.XSLFChart;
import org.apache.poi.xslf.usermodel.XSLFColor;
import org.apache.poi.xslf.usermodel.XSLFSheet;
import org.apache.poi.xslf.usermodel.XSLFTheme;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCatAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartLines;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDLbl;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDLbls;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLegend;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumVal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScaling;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrVal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.STBarDir;
import org.openxmlformats.schemas.drawingml.x2006.chart.STBarGrouping;
import org.openxmlformats.schemas.drawingml.x2006.chart.STDLblPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.STLegendPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.STOrientation;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;

import com.top_logic.presentation.template.ChartInfo;

/**
 * Draws a chart of a presentation with Java2D from the XML of its chart part.
 *
 * <p>
 * Bar charts (horizontal bars and vertical columns, side by side or stacked) are drawn from the
 * values cached in the chart part: the bars in the colors of their series, the values at the bars
 * if the data labels show them, the category labels, a value axis with grid lines if it is not
 * hidden, the legend and the title. Colors are resolved against the theme of the slide, texts use
 * the theme font the chart text refers to, in the size and color the chart XML defines. All other
 * kinds of charts are drawn as a light frame naming the kind of chart, so that a preview shows
 * that there is a chart.
 * </p>
 */
public final class ChartRenderer {

	/**
	 * The font size of chart texts without size, as defined by the chart XML.
	 */
	private static final double DEFAULT_TEXT_SIZE = 10;

	/**
	 * The distance between the categories in percent of the bar width, if the chart defines none.
	 */
	private static final double DEFAULT_GAP = 150;

	/**
	 * The distance between the parts of a chart (title, plot area, legend, labels) in points.
	 */
	private static final double SPACING = 4;

	/**
	 * The largest share of the chart width taken by the category labels of horizontal bars.
	 */
	private static final double MAX_LABEL_SHARE = 0.35;

	/**
	 * The approximate number of steps of a value axis without given scale.
	 */
	private static final int AXIS_STEPS = 5;

	/**
	 * The width of axis lines without own width in points.
	 */
	private static final double DEFAULT_LINE_WIDTH = 0.75;

	/**
	 * The color of the lines of a chart without own line color.
	 */
	private static final Color DEFAULT_LINE_COLOR = new Color(0xBFBFBF);

	/**
	 * The fill of the frame shown for kinds of charts that are not drawn.
	 */
	private static final Color PLACEHOLDER_FILL = new Color(0, 0, 0, 0x10);

	/**
	 * The color of the frame and the text shown for kinds of charts that are not drawn.
	 */
	private static final Color PLACEHOLDER_COLOR = new Color(0x808080);

	/**
	 * The font size of the text shown for kinds of charts that are not drawn.
	 */
	private static final double PLACEHOLDER_TEXT_SIZE = 14;

	private static final String MINOR_FONT = "+mn-lt";

	private static final String MAJOR_FONT = "+mj-lt";

	private static final String GENERAL_FORMAT = "General";

	private final Graphics2D _graphics;

	private final XSLFChart _chart;

	private final XSLFSheet _sheet;

	private final XSLFTheme _theme;

	private final DataFormatter _formatter = new DataFormatter();

	private ChartRenderer(Graphics2D graphics, XSLFChart chart, XSLFSheet sheet) {
		_graphics = graphics;
		_chart = chart;
		_sheet = sheet;
		_theme = sheet.getTheme();
	}

	/**
	 * Draws the given chart.
	 *
	 * @param graphics
	 *        The graphics to draw on, in points of the slide.
	 * @param chart
	 *        The chart to draw.
	 * @param sheet
	 *        The slide, layout or master the chart is placed on, whose theme resolves the colors
	 *        and fonts.
	 * @param bounds
	 *        The position and size of the chart.
	 */
	public static void draw(Graphics2D graphics, XSLFChart chart, XSLFSheet sheet, Rectangle2D bounds) {
		Graphics2D g = (Graphics2D) graphics.create();
		try {
			g.clip(bounds);
			new ChartRenderer(g, chart, sheet).draw(bounds);
		} finally {
			g.dispose();
		}
	}

	private void draw(Rectangle2D bounds) {
		CTPlotArea plotArea = _chart.getCTChart().getPlotArea();
		if (plotArea.sizeOfBarChartArray() == 0) {
			drawPlaceholder(bounds);
			return;
		}
		new BarChart(plotArea.getBarChartArray(0)).draw(bounds);
	}

	/**
	 * Draws a light frame with the kind of the chart.
	 */
	private void drawPlaceholder(Rectangle2D bounds) {
		_graphics.setColor(PLACEHOLDER_FILL);
		_graphics.fill(bounds);
		_graphics.setColor(PLACEHOLDER_COLOR);
		_graphics.setStroke(new BasicStroke(1f));
		_graphics.draw(new Rectangle2D.Double(bounds.getX() + 0.5, bounds.getY() + 0.5, bounds.getWidth() - 1,
			bounds.getHeight() - 1));
		ChartInfo info = ChartInfo.of(_chart);
		String type = info == null || info.type().isEmpty() ? "?" : info.type();
		TextStyle style = new TextStyle(PLACEHOLDER_TEXT_SIZE, false, PLACEHOLDER_COLOR, _theme.getMinorFont());
		drawText("Chart: " + type, style, bounds.getCenterX(), bounds.getCenterY(), 0.5, 0.5);
	}

	/**
	 * The formatting of a chart text.
	 *
	 * @param size
	 *        The font size in points.
	 * @param bold
	 *        Whether the text is bold.
	 * @param color
	 *        The text color.
	 * @param typeface
	 *        The font family.
	 */
	private record TextStyle(double size, boolean bold, Color color, String typeface) {
		// Pure data.
	}

	/**
	 * A data series of a bar chart.
	 *
	 * @param name
	 *        The name of the series.
	 * @param values
	 *        The values by category, <code>null</code> for missing values.
	 * @param color
	 *        The fill of the bars.
	 * @param labels
	 *        The data labels, <code>null</code> if the values are not shown.
	 */
	private record Series(String name, List<Double> values, Color color, Labels labels) {
		// Pure data.
	}

	/**
	 * The data labels of a series.
	 *
	 * @param deleted
	 *        The indices of the categories whose label is removed.
	 * @param format
	 *        The number format.
	 * @param style
	 *        The text formatting.
	 * @param inside
	 *        Whether the labels are centered in the bars, otherwise beyond their ends.
	 */
	private record Labels(Set<Long> deleted, String format, TextStyle style, boolean inside) {
		// Pure data.
	}

	/**
	 * Draws a bar chart.
	 */
	private final class BarChart {

		private final CTBarChart _bar;

		private final boolean _horizontal;

		private final boolean _stacked;

		private final TextStyle _baseStyle;

		private final List<String> _categories;

		private final List<Series> _series = new ArrayList<>();

		private final CTCatAx _catAx;

		private final CTValAx _valAx;

		private double _min;

		private double _max;

		private double _step;

		/**
		 * The width of the category labels of horizontal bars.
		 */
		private double _labelWidth;

		BarChart(CTBarChart bar) {
			_bar = bar;
			_horizontal = bar.getBarDir().getVal() == STBarDir.BAR;
			STBarGrouping.Enum grouping = bar.isSetGrouping() ? bar.getGrouping().getVal() : STBarGrouping.CLUSTERED;
			_stacked = grouping == STBarGrouping.STACKED || grouping == STBarGrouping.PERCENT_STACKED;
			CTPlotArea plotArea = _chart.getCTChart().getPlotArea();
			_catAx = plotArea.sizeOfCatAxArray() > 0 ? plotArea.getCatAxArray(0) : null;
			_valAx = plotArea.sizeOfValAxArray() > 0 ? plotArea.getValAxArray(0) : null;
			TextStyle defaults = new TextStyle(DEFAULT_TEXT_SIZE, false, Color.BLACK, _theme.getMinorFont());
			_baseStyle = _chart.getCTChartSpace().isSetTxPr()
				? textStyle(_chart.getCTChartSpace().getTxPr(), defaults) : defaults;

			List<String> categories = new ArrayList<>();
			for (CTBarSer ser : bar.getSerArray()) {
				if (categories.isEmpty() && ser.isSetCat()) {
					categories.addAll(categories(ser.getCat()));
				}
			}
			int count = categories.size();
			for (CTBarSer ser : bar.getSerArray()) {
				count = Math.max(count, values(ser.getVal(), 0).size());
			}
			for (int n = categories.size(); n < count; n++) {
				categories.add(Integer.toString(n + 1));
			}
			_categories = categories;
			for (CTBarSer ser : bar.getSerArray()) {
				_series.add(series(ser, count));
			}
		}

		private Series series(CTBarSer ser, int count) {
			String name = "";
			if (ser.isSetTx()) {
				if (ser.getTx().isSetStrRef() && ser.getTx().getStrRef().isSetStrCache()
					&& ser.getTx().getStrRef().getStrCache().sizeOfPtArray() > 0) {
					name = ser.getTx().getStrRef().getStrCache().getPtArray(0).getV();
				} else if (ser.getTx().isSetV()) {
					name = ser.getTx().getV();
				}
			}
			Color color = ser.isSetSpPr() ? fillColor(ser.getSpPr()) : null;
			CTDLbls dLbls = ser.isSetDLbls() ? ser.getDLbls() : _bar.isSetDLbls() ? _bar.getDLbls() : null;
			return new Series(name, values(ser.getVal(), count), color == null ? DEFAULT_LINE_COLOR : color,
				labels(dLbls));
		}

		private Labels labels(CTDLbls dLbls) {
			if (dLbls == null || dLbls.isSetDelete() && dLbls.getDelete().getVal()
				|| !(dLbls.isSetShowVal() && dLbls.getShowVal().getVal())) {
				return null;
			}
			Set<Long> deleted = new HashSet<>();
			for (CTDLbl label : dLbls.getDLblArray()) {
				if (label.isSetDelete() && label.getDelete().getVal()) {
					deleted.add(Long.valueOf(label.getIdx().getVal()));
				}
			}
			String format = dLbls.isSetNumFmt() ? dLbls.getNumFmt().getFormatCode() : null;
			TextStyle style = dLbls.isSetTxPr() ? textStyle(dLbls.getTxPr(), _baseStyle) : _baseStyle;
			boolean inside;
			if (dLbls.isSetDLblPos()) {
				STDLblPos.Enum position = dLbls.getDLblPos().getVal();
				inside = position != STDLblPos.OUT_END;
			} else {
				inside = _stacked;
			}
			return new Labels(deleted, format, style, inside);
		}

		void draw(Rectangle2D bounds) {
			Rectangle2D area = new Rectangle2D.Double(bounds.getX() + SPACING, bounds.getY() + SPACING,
				Math.max(0, bounds.getWidth() - 2 * SPACING), Math.max(0, bounds.getHeight() - 2 * SPACING));
			area = drawTitle(area);
			area = drawLegend(area);
			computeScale();

			boolean showCategories = _catAx != null && !(_catAx.isSetDelete() && _catAx.getDelete().getVal());
			boolean showValues = _valAx != null && !(_valAx.isSetDelete() && _valAx.getDelete().getVal());
			TextStyle categoryStyle = axisStyle(_catAx == null ? null : _catAx.getTxPr());
			TextStyle valueStyle = axisStyle(_valAx == null ? null : _valAx.getTxPr());
			String valueFormat = _valAx != null && _valAx.isSetNumFmt() ? _valAx.getNumFmt().getFormatCode() : null;

			// Room for the labels at the ends of the bars.
			double labelExtent = 0;
			for (Series series : _series) {
				Labels labels = series.labels();
				if (labels != null && !labels.inside()) {
					labelExtent = Math.max(labelExtent, _horizontal
						? maxWidth(series.values(), labels) + SPACING : lineHeight(labels.style()) + SPACING);
				}
			}

			Rectangle2D plot;
			if (_horizontal) {
				double labelWidth = 0;
				if (showCategories) {
					for (String category : _categories) {
						labelWidth = Math.max(labelWidth, textWidth(category, categoryStyle));
					}
					labelWidth = Math.min(labelWidth, area.getWidth() * MAX_LABEL_SHARE);
					_labelWidth = labelWidth;
					labelWidth += SPACING;
				}
				double axisHeight = showValues ? lineHeight(valueStyle) + SPACING : 0;
				plot = new Rectangle2D.Double(area.getX() + labelWidth, area.getY(),
					Math.max(0, area.getWidth() - labelWidth - labelExtent), Math.max(0, area.getHeight() - axisHeight));
			} else {
				double axisWidth = 0;
				if (showValues) {
					for (double value = _min; value <= _max + _step / 2; value += _step) {
						axisWidth = Math.max(axisWidth, textWidth(format(value, valueFormat), valueStyle));
					}
					axisWidth += SPACING;
				}
				double band = (area.getWidth() - axisWidth) / Math.max(1, _categories.size());
				int lines = 1;
				if (showCategories) {
					for (String category : _categories) {
						lines = Math.max(lines, wrap(category, categoryStyle, band).size());
					}
				}
				double labelHeight = showCategories ? lines * lineHeight(categoryStyle) + SPACING : 0;
				plot = new Rectangle2D.Double(area.getX() + axisWidth, area.getY() + labelExtent,
					Math.max(0, area.getWidth() - axisWidth),
					Math.max(0, area.getHeight() - labelHeight - labelExtent));
			}

			if (showValues) {
				drawValueAxis(plot, valueStyle, valueFormat);
			}
			drawBars(plot);
			if (showCategories) {
				drawCategoryAxis(plot, categoryStyle);
			}
		}

		private TextStyle axisStyle(CTTextBody txPr) {
			return txPr == null ? _baseStyle : textStyle(txPr, _baseStyle);
		}

		/**
		 * Draws the title above the plot area.
		 *
		 * @return The area left for the rest of the chart.
		 */
		private Rectangle2D drawTitle(Rectangle2D area) {
			CTChart chart = _chart.getCTChart();
			if (!chart.isSetTitle() || chart.isSetAutoTitleDeleted() && chart.getAutoTitleDeleted().getVal()
				|| !chart.getTitle().isSetTx() || !chart.getTitle().getTx().isSetRich()) {
				return area;
			}
			CTTextBody rich = chart.getTitle().getTx().getRich();
			StringBuilder text = new StringBuilder();
			TextStyle style = textStyle(rich, _baseStyle);
			for (CTTextParagraph paragraph : rich.getPArray()) {
				for (CTRegularTextRun run : paragraph.getRArray()) {
					text.append(run.getT());
					if (run.isSetRPr()) {
						style = runStyle(run.getRPr(), style);
					}
				}
			}
			if (text.length() == 0) {
				return area;
			}
			double height = lineHeight(style);
			drawText(text.toString(), style, area.getCenterX(), area.getY(), 0.5, 0);
			return new Rectangle2D.Double(area.getX(), area.getY() + height + SPACING, area.getWidth(),
				Math.max(0, area.getHeight() - height - SPACING));
		}

		/**
		 * Draws the legend at its position.
		 *
		 * @return The area left for the plot area.
		 */
		private Rectangle2D drawLegend(Rectangle2D area) {
			CTLegend legend = _chart.getCTChart().isSetLegend() ? _chart.getCTChart().getLegend() : null;
			if (legend == null || _series.isEmpty()) {
				return area;
			}
			TextStyle style = legend.isSetTxPr() ? textStyle(legend.getTxPr(), _baseStyle) : _baseStyle;
			STLegendPos.Enum position = legend.isSetLegendPos() ? legend.getLegendPos().getVal() : STLegendPos.R;
			double lineHeight = lineHeight(style);
			double mark = style.size() * 0.7;
			double[] widths = new double[_series.size()];
			double total = 0;
			double widest = 0;
			for (int n = 0; n < _series.size(); n++) {
				widths[n] = mark + SPACING + textWidth(_series.get(n).name(), style);
				total += widths[n] + (n > 0 ? 3 * SPACING : 0);
				widest = Math.max(widest, widths[n]);
			}
			if (position == STLegendPos.B || position == STLegendPos.T) {
				double y = position == STLegendPos.B ? area.getMaxY() - lineHeight : area.getY();
				double x = area.getCenterX() - total / 2;
				for (int n = 0; n < _series.size(); n++) {
					drawLegendEntry(_series.get(n), style, mark, x, y, lineHeight);
					x += widths[n] + 3 * SPACING;
				}
				double rest = Math.max(0, area.getHeight() - lineHeight - SPACING);
				return new Rectangle2D.Double(area.getX(),
					position == STLegendPos.T ? area.getY() + lineHeight + SPACING : area.getY(), area.getWidth(),
					rest);
			}
			double x = position == STLegendPos.L ? area.getX() : area.getMaxX() - widest;
			double y = area.getCenterY() - _series.size() * lineHeight / 2;
			for (Series series : _series) {
				drawLegendEntry(series, style, mark, x, y, lineHeight);
				y += lineHeight;
			}
			double rest = Math.max(0, area.getWidth() - widest - 2 * SPACING);
			return new Rectangle2D.Double(position == STLegendPos.L ? area.getX() + widest + 2 * SPACING : area.getX(),
				area.getY(), rest, area.getHeight());
		}

		private void drawLegendEntry(Series series, TextStyle style, double mark, double x, double y,
				double lineHeight) {
			_graphics.setColor(series.color());
			_graphics.fill(new Rectangle2D.Double(x, y + (lineHeight - mark) / 2, mark, mark));
			drawText(series.name(), style, x + mark + SPACING, y + lineHeight / 2, 0, 0.5);
		}

		/**
		 * Computes the range and the step of the value axis.
		 */
		private void computeScale() {
			double low = 0;
			double high = 0;
			for (int category = 0; category < _categories.size(); category++) {
				double positive = 0;
				double negative = 0;
				for (Series series : _series) {
					Double value = series.values().get(category);
					if (value == null) {
						continue;
					}
					double v = value.doubleValue();
					if (_stacked) {
						if (v >= 0) {
							positive += v;
						} else {
							negative += v;
						}
					} else {
						positive = Math.max(positive, v);
						negative = Math.min(negative, v);
					}
				}
				high = Math.max(high, positive);
				low = Math.min(low, negative);
			}
			CTScaling scaling = _valAx == null ? null : _valAx.getScaling();
			Double givenMax = scaling != null && scaling.isSetMax() ? Double.valueOf(scaling.getMax().getVal()) : null;
			Double givenMin = scaling != null && scaling.isSetMin() ? Double.valueOf(scaling.getMin().getVal()) : null;
			double max = givenMax != null ? givenMax.doubleValue() : high;
			double min = givenMin != null ? givenMin.doubleValue() : low;
			if (max <= min) {
				max = min + 1;
			}
			_step = niceStep((max - min) / AXIS_STEPS);
			_min = givenMin != null ? min : Math.floor(min / _step) * _step;
			_max = givenMax != null ? max : Math.ceil(max / _step) * _step;
			if (_max <= _min) {
				_max = _min + _step;
			}
		}

		/**
		 * Draws the grid lines and the labels of the value axis.
		 */
		private void drawValueAxis(Rectangle2D plot, TextStyle style, String format) {
			CTChartLines grid = _valAx.isSetMajorGridlines() ? _valAx.getMajorGridlines() : null;
			Color gridColor = grid != null && grid.isSetSpPr() ? lineColor(grid.getSpPr()) : DEFAULT_LINE_COLOR;
			double gridWidth = grid != null && grid.isSetSpPr() ? lineWidth(grid.getSpPr()) : DEFAULT_LINE_WIDTH;
			for (double value = _min; value <= _max + _step / 2; value += _step) {
				double position = position(plot, value);
				if (grid != null && gridColor != null) {
					_graphics.setColor(gridColor);
					_graphics.setStroke(new BasicStroke((float) gridWidth));
					_graphics.draw(_horizontal ? new Line2D.Double(position, plot.getY(), position, plot.getMaxY())
						: new Line2D.Double(plot.getX(), position, plot.getMaxX(), position));
				}
				String text = format(value, format);
				if (_horizontal) {
					drawText(text, style, position, plot.getMaxY() + SPACING, 0.5, 0);
				} else {
					drawText(text, style, plot.getX() - SPACING, position, 1, 0.5);
				}
			}
		}

		/**
		 * Draws the category labels and the axis line.
		 */
		private void drawCategoryAxis(Rectangle2D plot, TextStyle style) {
			Color lineColor = _catAx.isSetSpPr() ? lineColor(_catAx.getSpPr()) : DEFAULT_LINE_COLOR;
			if (lineColor != null) {
				double base = position(plot, Math.max(_min, Math.min(_max, 0)));
				_graphics.setColor(lineColor);
				_graphics.setStroke(new BasicStroke((float) (_catAx.isSetSpPr() ? lineWidth(_catAx.getSpPr())
					: DEFAULT_LINE_WIDTH)));
				_graphics.draw(_horizontal ? new Line2D.Double(base, plot.getY(), base, plot.getMaxY())
					: new Line2D.Double(plot.getX(), base, plot.getMaxX(), base));
			}
			double band = band(plot);
			double lineHeight = lineHeight(style);
			for (int n = 0; n < _categories.size(); n++) {
				double start = bandStart(plot, n);
				if (_horizontal) {
					List<String> lines = wrap(_categories.get(n), style, Math.max(1, _labelWidth));
					double y = start + band / 2 - lines.size() * lineHeight / 2;
					for (String line : lines) {
						drawText(line, style, plot.getX() - SPACING, y, 1, 0);
						y += lineHeight;
					}
				} else {
					List<String> lines = wrap(_categories.get(n), style, band);
					double y = plot.getMaxY() + SPACING;
					for (String line : lines) {
						drawText(line, style, start + band / 2, y, 0.5, 0);
						y += lineHeight;
					}
				}
			}
		}

		/**
		 * Draws the bars and their labels.
		 */
		private void drawBars(Rectangle2D plot) {
			int count = _series.size();
			int columns = _stacked ? 1 : count;
			double overlap = percent(_bar.isSetOverlap() ? _bar.getOverlap().getVal() : null, _stacked ? 100 : 0)
				/ 100;
			double gap = percent(_bar.isSetGapWidth() ? _bar.getGapWidth().getVal() : null, DEFAULT_GAP) / 100;
			double band = band(plot);
			double barWidth = band / (columns - (columns - 1) * overlap + gap);
			double groupWidth = barWidth * (columns - (columns - 1) * overlap);
			for (int category = 0; category < _categories.size(); category++) {
				double groupStart = bandStart(plot, category) + (band - groupWidth) / 2;
				double positive = 0;
				double negative = 0;
				for (int n = 0; n < count; n++) {
					Series series = _series.get(n);
					Double value = series.values().get(category);
					if (value == null) {
						continue;
					}
					double v = value.doubleValue();
					double from;
					double to;
					if (_stacked) {
						from = v >= 0 ? positive : negative;
						to = from + v;
						if (v >= 0) {
							positive = to;
						} else {
							negative = to;
						}
					} else {
						from = 0;
						to = v;
					}
					// The first series is next to the origin of the category axis.
					boolean fromStart = _horizontal ? isReversed() : !isReversed();
					int slot = _stacked ? 0 : fromStart ? n : columns - 1 - n;
					double offset = slot * barWidth * (1 - overlap);
					double crossStart = groupStart + offset;
					double p1 = position(plot, clamp(from));
					double p2 = position(plot, clamp(to));
					Rectangle2D rect = _horizontal
						? new Rectangle2D.Double(Math.min(p1, p2), crossStart, Math.abs(p2 - p1), barWidth)
						: new Rectangle2D.Double(crossStart, Math.min(p1, p2), barWidth, Math.abs(p2 - p1));
					_graphics.setColor(series.color());
					_graphics.fill(rect);
					drawLabel(series, category, v, rect);
				}
			}
		}

		private void drawLabel(Series series, int category, double value, Rectangle2D bar) {
			Labels labels = series.labels();
			if (labels == null || labels.deleted().contains(Long.valueOf(category))) {
				return;
			}
			String text = format(value, labels.format());
			if (labels.inside()) {
				drawText(text, labels.style(), bar.getCenterX(), bar.getCenterY(), 0.5, 0.5);
			} else if (_horizontal) {
				drawText(text, labels.style(), bar.getMaxX() + SPACING, bar.getCenterY(), 0, 0.5);
			} else {
				drawText(text, labels.style(), bar.getCenterX(), bar.getY() - SPACING, 0.5, 1);
			}
		}

		private double maxWidth(List<Double> values, Labels labels) {
			double result = 0;
			for (Double value : values) {
				if (value != null) {
					result = Math.max(result, textWidth(format(value.doubleValue(), labels.format()), labels.style()));
				}
			}
			return result;
		}

		private double clamp(double value) {
			return Math.max(_min, Math.min(_max, value));
		}

		/**
		 * The coordinate of the given value along the value axis.
		 */
		private double position(Rectangle2D plot, double value) {
			double share = (value - _min) / (_max - _min);
			return _horizontal ? plot.getX() + share * plot.getWidth() : plot.getMaxY() - share * plot.getHeight();
		}

		private double band(Rectangle2D plot) {
			return (_horizontal ? plot.getHeight() : plot.getWidth()) / Math.max(1, _categories.size());
		}

		/**
		 * The start coordinate of the band of the given category across the value axis.
		 */
		private double bandStart(Rectangle2D plot, int category) {
			int count = _categories.size();
			double band = band(plot);
			// The first category is at the bottom of horizontal bars and at the left of columns,
			// unless the category axis is reversed.
			boolean fromStart = _horizontal ? isReversed() : !isReversed();
			int index = fromStart ? category : count - 1 - category;
			return (_horizontal ? plot.getY() : plot.getX()) + index * band;
		}

		private boolean isReversed() {
			return _catAx != null && _catAx.getScaling().isSetOrientation()
				&& _catAx.getScaling().getOrientation().getVal() == STOrientation.MAX_MIN;
		}

	}

	private static List<String> categories(CTAxDataSource cat) {
		List<String> result = new ArrayList<>();
		CTStrData strings = cat.isSetStrRef() && cat.getStrRef().isSetStrCache() ? cat.getStrRef().getStrCache()
			: cat.isSetStrLit() ? cat.getStrLit() : null;
		if (strings != null) {
			String[] labels = new String[(int) strings.getPtCount().getVal()];
			for (CTStrVal point : strings.getPtArray()) {
				labels[(int) point.getIdx()] = point.getV();
			}
			for (String label : labels) {
				result.add(label == null ? "" : label);
			}
			return result;
		}
		CTNumData numbers = cat.isSetNumRef() && cat.getNumRef().isSetNumCache() ? cat.getNumRef().getNumCache()
			: cat.isSetNumLit() ? cat.getNumLit() : null;
		if (numbers != null) {
			for (Double value : numbers(numbers, 0)) {
				result.add(value == null ? "" : new DataFormatter().formatRawCellContents(value.doubleValue(), 0,
					numbers.isSetFormatCode() ? numbers.getFormatCode() : GENERAL_FORMAT));
			}
		}
		return result;
	}

	private static List<Double> values(CTNumDataSource val, int count) {
		CTNumData data = val == null ? null
			: val.isSetNumRef() && val.getNumRef().isSetNumCache() ? val.getNumRef().getNumCache()
			: val.isSetNumLit() ? val.getNumLit() : null;
		if (data == null) {
			List<Double> result = new ArrayList<>();
			for (int n = 0; n < count; n++) {
				result.add(null);
			}
			return result;
		}
		return numbers(data, count);
	}

	/**
	 * The values of the given number data, at least the given number.
	 */
	private static List<Double> numbers(CTNumData data, int count) {
		int size = Math.max(count, data.isSetPtCount() ? (int) data.getPtCount().getVal() : 0);
		Double[] values = new Double[size];
		for (CTNumVal point : data.getPtArray()) {
			int index = (int) point.getIdx();
			if (index < size) {
				try {
					values[index] = Double.valueOf(Double.parseDouble(point.getV()));
				} catch (NumberFormatException ex) {
					// Not a number, a missing value.
				}
			}
		}
		List<Double> result = new ArrayList<>(size);
		for (Double value : values) {
			result.add(value);
		}
		return result;
	}

	/**
	 * A percentage of the chart XML: a number or a text with percent sign.
	 */
	private static double percent(Object value, double defaultValue) {
		if (value instanceof Number number) {
			return number.doubleValue();
		}
		if (value != null) {
			String text = value.toString().trim();
			if (text.endsWith("%")) {
				text = text.substring(0, text.length() - 1);
			}
			try {
				return Double.parseDouble(text);
			} catch (NumberFormatException ex) {
				// Use the default.
			}
		}
		return defaultValue;
	}

	/**
	 * A step of a value axis near the given one: 1, 2, 2.5 or 5 times a power of ten.
	 */
	private static double niceStep(double raw) {
		if (raw <= 0 || Double.isNaN(raw) || Double.isInfinite(raw)) {
			return 1;
		}
		double magnitude = Math.pow(10, Math.floor(Math.log10(raw)));
		double fraction = raw / magnitude;
		double nice = fraction <= 1 ? 1 : fraction <= 2 ? 2 : fraction <= 2.5 ? 2.5 : fraction <= 5 ? 5 : 10;
		return nice * magnitude;
	}

	private String format(double value, String format) {
		String code = format == null || format.isEmpty() ? GENERAL_FORMAT : format;
		return _formatter.formatRawCellContents(value, 0, code);
	}

	/**
	 * The text formatting defined by the first paragraph of the given text properties.
	 */
	private TextStyle textStyle(CTTextBody txPr, TextStyle parent) {
		if (txPr.sizeOfPArray() == 0) {
			return parent;
		}
		CTTextParagraph paragraph = txPr.getPArray(0);
		if (!paragraph.isSetPPr() || !paragraph.getPPr().isSetDefRPr()) {
			return parent;
		}
		return runStyle(paragraph.getPPr().getDefRPr(), parent);
	}

	private TextStyle runStyle(CTTextCharacterProperties rPr, TextStyle parent) {
		double size = rPr.isSetSz() ? rPr.getSz() / 100.0 : parent.size();
		boolean bold = rPr.isSetB() ? rPr.getB() : parent.bold();
		Color color = rPr.isSetSolidFill() ? new XSLFColor(rPr.getSolidFill(), _theme, null, _sheet).getColor() : null;
		String typeface = rPr.isSetLatin() ? typeface(rPr.getLatin().getTypeface()) : parent.typeface();
		return new TextStyle(size, bold, color != null ? color : parent.color(), typeface);
	}

	private String typeface(String typeface) {
		if (typeface == null || MINOR_FONT.equals(typeface)) {
			return _theme.getMinorFont();
		}
		if (MAJOR_FONT.equals(typeface)) {
			return _theme.getMajorFont();
		}
		return typeface;
	}

	/**
	 * The solid fill color of the given shape properties, <code>null</code> if there is none.
	 */
	private Color fillColor(CTShapeProperties spPr) {
		return spPr.isSetSolidFill() ? new XSLFColor(spPr.getSolidFill(), _theme, null, _sheet).getColor() : null;
	}

	/**
	 * The color of the outline of the given shape properties, <code>null</code> for no outline.
	 */
	private Color lineColor(CTShapeProperties spPr) {
		if (!spPr.isSetLn()) {
			return DEFAULT_LINE_COLOR;
		}
		CTLineProperties ln = spPr.getLn();
		if (ln.isSetNoFill()) {
			return null;
		}
		return ln.isSetSolidFill() ? new XSLFColor(ln.getSolidFill(), _theme, null, _sheet).getColor()
			: DEFAULT_LINE_COLOR;
	}

	private static double lineWidth(CTShapeProperties spPr) {
		return spPr.isSetLn() && spPr.getLn().isSetW() ? Units.toPoints(spPr.getLn().getW()) : DEFAULT_LINE_WIDTH;
	}

	private Font font(TextStyle style) {
		Object handler = _graphics.getRenderingHint(Drawable.FONT_HANDLER);
		DrawFontManager fonts = handler instanceof DrawFontManager manager ? manager : new DrawFontManagerDefault();
		FontInfo info = style::typeface;
		return fonts.createAWTFont(_graphics, info, style.size(), style.bold(), false);
	}

	private double textWidth(String text, TextStyle style) {
		if (text.isEmpty()) {
			return 0;
		}
		return font(style).getStringBounds(text, renderContext()).getWidth();
	}

	private double lineHeight(TextStyle style) {
		LineMetrics metrics = font(style).getLineMetrics("Xg", renderContext());
		return metrics.getAscent() + metrics.getDescent() + metrics.getLeading();
	}

	private FontRenderContext renderContext() {
		// Measures in points, independent of the scale of the graphics.
		FontRenderContext context = _graphics.getFontRenderContext();
		return new FontRenderContext(new AffineTransform(), context.getAntiAliasingHint(),
			context.getFractionalMetricsHint());
	}

	/**
	 * Breaks the given text at spaces into lines of at most the given width.
	 */
	private List<String> wrap(String text, TextStyle style, double width) {
		List<String> lines = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		for (String word : text.split(" ")) {
			if (line.length() > 0 && textWidth(line + " " + word, style) > width) {
				lines.add(line.toString());
				line.setLength(0);
			}
			if (line.length() > 0) {
				line.append(' ');
			}
			line.append(word);
		}
		lines.add(line.toString());
		return lines;
	}

	/**
	 * Draws a single line of text.
	 *
	 * @param x
	 *        The horizontal reference position.
	 * @param y
	 *        The vertical reference position.
	 * @param alignX
	 *        The share of the text width left of the reference position: 0 for left aligned, 0.5
	 *        for centered, 1 for right aligned.
	 * @param alignY
	 *        The share of the line height above the reference position: 0 for the top, 0.5 for the
	 *        middle, 1 for the bottom of the line.
	 */
	private void drawText(String text, TextStyle style, double x, double y, double alignX, double alignY) {
		if (text.isEmpty()) {
			return;
		}
		Font font = font(style);
		LineMetrics metrics = font.getLineMetrics(text, renderContext());
		double height = metrics.getAscent() + metrics.getDescent();
		double width = font.getStringBounds(text, renderContext()).getWidth();
		_graphics.setFont(font);
		_graphics.setColor(style.color());
		_graphics.drawString(text, (float) (x - alignX * width), (float) (y - alignY * height + metrics.getAscent()));
	}

}
