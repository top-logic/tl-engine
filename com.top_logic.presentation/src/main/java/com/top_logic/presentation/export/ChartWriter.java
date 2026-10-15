/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.export;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xddf.usermodel.chart.AxisCrossBetween;
import org.apache.poi.xddf.usermodel.chart.AxisCrosses;
import org.apache.poi.xddf.usermodel.chart.AxisOrientation;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.AxisTickMark;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.BarGrouping;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xslf.usermodel.XSLFChart;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartSpace;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDLbl;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDLbls;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumFmt;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.STDLblPos;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;

import com.top_logic.presentation.data.PresentationData;
import com.top_logic.presentation.model.ChartLegend;
import com.top_logic.presentation.model.ChartSpec;
import com.top_logic.presentation.model.ColorSpec;
import com.top_logic.presentation.model.LineSpec;
import com.top_logic.presentation.model.PaintSpec;
import com.top_logic.presentation.model.RunStyle;
import com.top_logic.presentation.template.ColorInfo;

/**
 * Creates native PowerPoint charts with bars or columns.
 *
 * <p>
 * The data of a chart is written into a workbook embedded in the presentation, so that it can be
 * edited in PowerPoint. The bars of a series are filled with the color of the series' role, as
 * reference to the theme where the role refers to a theme color. The texts use the body font of
 * the theme, the color roles {@link PresentationData#ROLE_TEXT} for values and category labels,
 * {@link PresentationData#ROLE_INVERSE} for values inside stacked bars and
 * {@link PresentationData#ROLE_MUTED} for the axis line and the labels of the value axis. The
 * chart has neither border nor background; a value axis and grid lines are only shown if the
 * values are not written at the bars. A segment of stacked bars shorter than
 * {@link #MIN_LABEL_SHARE} of the value axis shows no value.
 * </p>
 */
final class ChartWriter {

	/**
	 * The height of a chart as fraction of its width, if the content decides on the height, e.g.
	 * with automatic height.
	 */
	static final double NATURAL_ASPECT = 0.6;

	/**
	 * The font size of the category labels, the value axis and the legend.
	 */
	static final double TEXT_SIZE = 12;

	/**
	 * The font size of the values written at the bars.
	 */
	static final double LABEL_SIZE = 11;

	/**
	 * The font size of the chart title.
	 */
	static final double TITLE_SIZE = 14;

	/**
	 * The width of the line of the category axis.
	 */
	static final double AXIS_WIDTH = 0.75;

	/**
	 * The width of the grid lines.
	 */
	static final double GRID_WIDTH = 0.5;

	/**
	 * The share of the value axis below which the value of a stacked segment is not written, since
	 * the segment is too narrow for it.
	 */
	static final double MIN_LABEL_SHARE = 0.04;

	/**
	 * The overlap of the bars of stacked series in percent: they lie on top of each other.
	 */
	private static final byte STACKED_OVERLAP = 100;

	/**
	 * The overlap of the bars of series side by side in percent: a small gap between them.
	 */
	private static final byte CLUSTERED_OVERLAP = -10;

	/**
	 * The font of all chart texts: the body font of the theme.
	 */
	private static final String THEME_BODY_FONT = "+mn-lt";

	private static final ColorSpec TEXT = role(PresentationData.ROLE_TEXT);

	private static final ColorSpec INVERSE = role(PresentationData.ROLE_INVERSE);

	private static final ColorSpec MUTED = role(PresentationData.ROLE_MUTED);

	/**
	 * The color of the grid lines.
	 */
	private static final ColorSpec GRID = new ColorSpec.Scheme("bg1", Map.of(ColorInfo.LUM_MOD, 0.85));

	private final XSLFSlide _slide;

	private final DrawingWriter _drawing;

	private final TextWriter _text;

	/**
	 * Creates a {@link ChartWriter}.
	 *
	 * @param slide
	 *        The slide to place the charts on.
	 * @param drawing
	 *        Writes the colors of the color roles.
	 * @param text
	 *        Writes the character formatting.
	 */
	ChartWriter(XSLFSlide slide, DrawingWriter drawing, TextWriter text) {
		_slide = slide;
		_drawing = drawing;
		_text = text;
	}

	/**
	 * Creates the given chart on the slide.
	 *
	 * @param spec
	 *        The chart description.
	 * @param bounds
	 *        The position and size of the chart in points.
	 * @param path
	 *        The path of the chart in the description, for error messages.
	 */
	void write(ChartSpec spec, Rectangle2D bounds, String path) {
		XSLFChart chart = _slide.getSlideShow().createChart();
		_slide.addChart(chart, new Rectangle2D.Double(Units.toEMU(bounds.getX()), Units.toEMU(bounds.getY()),
			Units.toEMU(bounds.getWidth()), Units.toEMU(bounds.getHeight())));

		boolean horizontal = spec.horizontal();
		XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(horizontal ? AxisPosition.LEFT : AxisPosition.BOTTOM);
		XDDFValueAxis valueAxis = chart.createValueAxis(horizontal ? AxisPosition.BOTTOM : AxisPosition.LEFT);
		valueAxis.setCrossBetween(AxisCrossBetween.BETWEEN);
		if (horizontal) {
			// Lists the categories from top to bottom, the value axis below them.
			categoryAxis.setOrientation(AxisOrientation.MAX_MIN);
			valueAxis.setCrosses(AxisCrosses.MAX);
		}

		XDDFBarChartData data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, categoryAxis, valueAxis);
		data.setBarDirection(horizontal ? BarDirection.BAR : BarDirection.COL);
		data.setBarGrouping(spec.stacked() ? BarGrouping.STACKED : BarGrouping.CLUSTERED);
		data.setOverlap(Byte.valueOf(spec.stacked() ? STACKED_OVERLAP : CLUSTERED_OVERLAP));
		data.setGapWidth(Integer.valueOf(spec.gap()));
		data.setVaryColors(Boolean.FALSE);
		addSeries(chart, data, spec);
		chart.plot(data);

		CTPlotArea plotArea = chart.getCTChart().getPlotArea();
		CTBarChart barChart = plotArea.getBarChartArray(0);
		List<ChartSpec.Series> series = spec.series();
		double minLabel = spec.stacked() ? axisLength(spec) * MIN_LABEL_SHARE : 0;
		for (int n = 0; n < series.size(); n++) {
			String seriesPath = path + "." + PresentationData.SERIES + "[" + n + "]";
			writeSeries(barChart.getSerArray(n), series.get(n), spec, minLabel, seriesPath);
		}

		writeCategoryAxis(categoryAxis, plotArea, path);
		writeValueAxis(valueAxis, plotArea.getValAxArray(0), spec, path);
		writeFrame(chart, plotArea, path);
		writeLegend(chart, spec.legend());
		writeTitle(chart, spec.title(), path);
	}

	/**
	 * Adds the series to the chart: the categories in the first column of the embedded workbook,
	 * the values of each series in a further column headed by its name.
	 */
	private static void addSeries(XSLFChart chart, XDDFBarChartData data, ChartSpec spec) {
		List<String> categories = spec.categories();
		int count = categories.size();
		XDDFDataSource<String> categoryData = XDDFDataSourcesFactory.fromArray(categories.toArray(new String[0]),
			chart.formatRange(new CellRangeAddress(1, count, 0, 0)), 0);
		List<ChartSpec.Series> series = spec.series();
		for (int n = 0; n < series.size(); n++) {
			ChartSpec.Series seriesSpec = series.get(n);
			int column = n + 1;
			XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromArray(
				seriesSpec.values().toArray(new Double[0]),
				chart.formatRange(new CellRangeAddress(1, count, column, column)), column);
			XDDFChartData.Series added = data.addSeries(categoryData, values);
			added.setTitle(seriesSpec.name(), chart.setSheetTitle(seriesSpec.name(), column));
		}
	}

	/**
	 * The largest value of the value axis: the given maximum, or the longest bar.
	 */
	private static double axisLength(ChartSpec spec) {
		if (spec.max() != null) {
			return spec.max().doubleValue();
		}
		double result = 0;
		for (int category = 0; category < spec.categories().size(); category++) {
			double total = 0;
			for (ChartSpec.Series series : spec.series()) {
				Double value = series.values().get(category);
				if (value != null) {
					total += Math.abs(value.doubleValue());
				}
			}
			result = Math.max(result, total);
		}
		return result;
	}

	/**
	 * Writes the fill and the value labels of a series.
	 *
	 * @param minLabel
	 *        The smallest value that is written at its bar.
	 */
	private void writeSeries(CTBarSer ser, ChartSpec.Series series, ChartSpec spec, double minLabel,
			String path) {
		CTShapeProperties spPr = ser.isSetSpPr() ? ser.getSpPr() : ser.addNewSpPr();
		CTSolidColorFillProperties fill = spPr.addNewSolidFill();
		_drawing.writeColor(new DrawingWriter.ColorTarget(fill::addNewSchemeClr, fill::addNewSrgbClr),
			role(series.role()), path + "." + PresentationData.ROLE);
		spPr.addNewLn().addNewNoFill();
		if (ser.isSetInvertIfNegative()) {
			ser.getInvertIfNegative().setVal(false);
		} else {
			ser.addNewInvertIfNegative().setVal(false);
		}

		if (!spec.labels()) {
			return;
		}
		CTDLbls labels = ser.isSetDLbls() ? ser.getDLbls() : ser.addNewDLbls();
		List<Double> values = series.values();
		for (int n = 0; n < values.size(); n++) {
			Double value = values.get(n);
			// A stacked bar has no space for the label of an empty or narrow segment.
			if (value == null
				|| spec.stacked() && (value.doubleValue() == 0 || Math.abs(value.doubleValue()) < minLabel)) {
				CTDLbl label = labels.addNewDLbl();
				label.addNewIdx().setVal(n);
				label.addNewDelete().setVal(true);
			}
		}
		if (spec.format() != null) {
			CTNumFmt numFmt = labels.addNewNumFmt();
			numFmt.setFormatCode(spec.format());
			numFmt.setSourceLinked(false);
		}
		labels.addNewSpPr().addNewNoFill();
		writeTextProperties(labels.addNewTxPr(), LABEL_SIZE, true, spec.stacked() ? INVERSE : TEXT, path);
		labels.addNewDLblPos().setVal(spec.stacked() ? STDLblPos.CTR : STDLblPos.OUT_END);
		labels.addNewShowLegendKey().setVal(false);
		labels.addNewShowVal().setVal(true);
		labels.addNewShowCatName().setVal(false);
		labels.addNewShowSerName().setVal(false);
		labels.addNewShowPercent().setVal(false);
		labels.addNewShowBubbleSize().setVal(false);
	}

	private void writeCategoryAxis(XDDFCategoryAxis axis, CTPlotArea plotArea, String path) {
		axis.setMajorTickMark(AxisTickMark.NONE);
		axis.setMinorTickMark(AxisTickMark.NONE);
		CTShapeProperties spPr = axis.getOrAddShapeProperties().getXmlObject();
		_drawing.writeLine(spPr.isSetLn() ? spPr.getLn() : spPr.addNewLn(),
			new LineSpec(new PaintSpec.Solid(MUTED), Double.valueOf(AXIS_WIDTH), null), _slide, path);
		writeTextProperties(plotArea.getCatAxArray(0).addNewTxPr(), TEXT_SIZE, false, TEXT, path);
	}

	/**
	 * Writes the value axis: hidden if the values are written at the bars, otherwise without line,
	 * with muted labels and light grid lines.
	 */
	private void writeValueAxis(XDDFValueAxis axis, CTValAx valAx, ChartSpec spec, String path) {
		if (spec.max() != null) {
			axis.setMaximum(spec.max().doubleValue());
		}
		if (spec.labels()) {
			axis.setVisible(false);
			return;
		}
		axis.setMajorTickMark(AxisTickMark.NONE);
		axis.setMinorTickMark(AxisTickMark.NONE);
		if (spec.format() != null) {
			axis.setNumberFormat(spec.format());
			valAx.getNumFmt().setSourceLinked(false);
		}
		CTShapeProperties spPr = axis.getOrAddShapeProperties().getXmlObject();
		(spPr.isSetLn() ? spPr.getLn() : spPr.addNewLn()).addNewNoFill();
		CTShapeProperties grid = axis.getOrAddMajorGridProperties().getXmlObject();
		_drawing.writeLine(grid.isSetLn() ? grid.getLn() : grid.addNewLn(),
			new LineSpec(new PaintSpec.Solid(GRID), Double.valueOf(GRID_WIDTH), null), _slide, path);
		writeTextProperties(valAx.addNewTxPr(), TEXT_SIZE, false, MUTED, path);
	}

	/**
	 * Removes border and background of the chart and its plot area, and sets the default text
	 * formatting.
	 */
	private void writeFrame(XSLFChart chart, CTPlotArea plotArea, String path) {
		CTChartSpace space = chart.getCTChartSpace();
		(space.isSetRoundedCorners() ? space.getRoundedCorners() : space.addNewRoundedCorners()).setVal(false);
		noFillNoLine(space.isSetSpPr() ? space.getSpPr() : space.addNewSpPr());
		noFillNoLine(plotArea.isSetSpPr() ? plotArea.getSpPr() : plotArea.addNewSpPr());
		writeTextProperties(space.isSetTxPr() ? space.getTxPr() : space.addNewTxPr(), TEXT_SIZE, false, TEXT, path);
	}

	private static void noFillNoLine(CTShapeProperties spPr) {
		spPr.addNewNoFill();
		spPr.addNewLn().addNewNoFill();
	}

	private static void writeLegend(XSLFChart chart, ChartLegend position) {
		if (position == ChartLegend.NONE) {
			chart.deleteLegend();
			return;
		}
		XDDFChartLegend legend = chart.getOrAddLegend();
		legend.setPosition(switch (position) {
			case TOP -> LegendPosition.TOP;
			case LEFT -> LegendPosition.LEFT;
			case RIGHT -> LegendPosition.RIGHT;
			default -> LegendPosition.BOTTOM;
		});
		legend.setOverlay(false);
	}

	private void writeTitle(XSLFChart chart, String title, String path) {
		if (title == null) {
			chart.setAutoTitleDeleted(true);
			return;
		}
		chart.setTitleText(title);
		chart.setTitleOverlay(false);
		chart.setAutoTitleDeleted(false);
		RunStyle style = style(TITLE_SIZE, true, TEXT);
		String titlePath = path + "." + PresentationData.TITLE;
		for (CTTextParagraph paragraph : chart.getCTChart().getTitle().getTx().getRich().getPArray()) {
			_text.writeCharacterProperties(
				paragraph.isSetPPr() && paragraph.getPPr().isSetDefRPr() ? paragraph.getPPr().getDefRPr()
					: (paragraph.isSetPPr() ? paragraph.getPPr() : paragraph.addNewPPr()).addNewDefRPr(),
				style, titlePath);
			for (CTRegularTextRun run : paragraph.getRArray()) {
				_text.writeCharacterProperties(run.isSetRPr() ? run.getRPr() : run.addNewRPr(), style, titlePath);
			}
		}
	}

	/**
	 * Writes the text formatting of a chart part.
	 */
	private void writeTextProperties(CTTextBody txPr, double size, boolean bold, ColorSpec color, String path) {
		txPr.addNewBodyPr();
		txPr.addNewLstStyle();
		CTTextParagraph paragraph = txPr.addNewP();
		_text.writeCharacterProperties(paragraph.addNewPPr().addNewDefRPr(), style(size, bold, color), path);
	}

	private static RunStyle style(double size, boolean bold, ColorSpec color) {
		return new RunStyle(Boolean.valueOf(bold), null, null, null, Double.valueOf(size), color, THEME_BODY_FONT,
			null);
	}

	private static ColorSpec role(String name) {
		return new ColorSpec.Role(name, Map.of());
	}

}
