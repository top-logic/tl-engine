/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.presentation;

import static com.top_logic.presentation.data.PresentationData.*;
import static test.com.top_logic.presentation.TestDeck.*;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFChart;
import org.apache.poi.xslf.usermodel.XSLFGraphicFrame;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumVal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrVal;
import org.openxmlformats.schemas.drawingml.x2006.chart.STBarDir;
import org.openxmlformats.schemas.drawingml.x2006.chart.STBarGrouping;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKeyTemplate;
import com.top_logic.presentation.model.ChartLegend;
import com.top_logic.presentation.model.ChartSpec;
import com.top_logic.presentation.model.DescriptionReader;
import com.top_logic.presentation.model.Value;
import com.top_logic.presentation.render.SlideRenderer;
import com.top_logic.presentation.template.ShapeInfo;
import com.top_logic.presentation.template.SlideInfo;
import com.top_logic.presentation.template.TemplateAnalysis;
import com.top_logic.util.error.TopLogicException;

/**
 * Test case for the native charts of a presentation (<code>pptBarChart()</code>,
 * <code>pptColumnChart()</code>): their description, the written chart parts with the embedded
 * workbook, and their analysis.
 */
@SuppressWarnings("javadoc")
public class TestCharts extends AbstractSearchExpressionTest {

	private static final int RENDER_WIDTH = 1280;

	private static final int MIN_PIXELS = 200;

	private static final int COLOR_TOLERANCE = 4;

	/**
	 * The color of the frame and text of a chart that is not drawn.
	 */
	private static final String PLACEHOLDER_COLOR = "808080";

	/**
	 * A slide with a column chart next to a card and a stacked bar chart below.
	 */
	private static final String CHART_SLIDE = """
		pptContent("Diagramme", content: [
			pptRow([
				pptColumnChart(["Q1", "Q2", "Q3"], [
					pptSeries("neu", [27, 19, null], "topicA"),
					["erledigt", [24, 24, 15], "risk"]
				], weight: 2),
				pptCard("Hinweis", "Daten sind in PowerPoint bearbeitbar.")
			], height: 220),
			pptBarChart(["React", "Sicherheit"], [
				{"name": "Funktionen", "values": [59, 3]},
				{"name": "Fehler", "values": [75, 0], "role": "topicC"}
			], stacked: true, max: 140, format: "0")
		])
		""";

	public void testBuilders() throws Exception {
		Map<String, Object> series = map(NAME, "S", VALUES, List.of(1.0, 2.0), ROLE, "topicA");
		assertEquals(series, eval("pptSeries('S', [1, 2], 'topicA')"));
		assertEquals(map(TYPE, TYPE_BAR_CHART, CATEGORIES, List.of("a", "b"), SERIES, List.of(series),
			STACKED, true, LEGEND, "top", MAX, 10.0, GAP, 80.0, FORMAT, "0.0", WIDTH, 300.0),
			eval("pptBarChart(['a', 'b'], [pptSeries('S', [1, 2], 'topicA')], stacked: true, legend: 'top', "
				+ "max: 10, gap: 80, format: '0.0', width: 300)"));
		assertEquals(map(TYPE, TYPE_COLUMN_CHART, CATEGORIES, List.of("a"), SERIES, List.of(List.of("S", List.of(1.0))),
			LABELS, false, TITLE, "T", WEIGHT, 2.0),
			eval("pptColumnChart(['a'], [['S', [1]]], labels: false, title: 'T', weight: 2)"));
	}

	public void testReader() throws Exception {
		ChartSpec bar = (ChartSpec) read("pptBarChart(['a', 'b'], [['S', [1, null]], {'name': 'T', 'values': [3, 4], "
			+ "'role': 'risk'}], stacked: true, gap: 20)");
		assertTrue(bar.horizontal());
		assertEquals(List.of("a", "b"), bar.categories());
		assertEquals(2, bar.series().size());
		assertEquals("S", bar.series().get(0).name());
		assertEquals(Arrays.asList(1.0, null), bar.series().get(0).values());
		assertEquals("A series without role gets the topic role of its position.", ROLE_TOPIC_A,
			bar.series().get(0).role());
		assertEquals("risk", bar.series().get(1).role());
		assertTrue(bar.stacked());
		assertTrue(bar.labels());
		assertEquals(ChartLegend.BOTTOM, bar.legend());
		assertEquals(20, bar.gap());

		ChartSpec column = (ChartSpec) read("pptColumnChart([1, 2], [pptSeries(values: [1, 2])], legend: 'right')");
		assertFalse(column.horizontal());
		assertEquals("Numbers are accepted as category labels.", List.of("1", "2"), column.categories());
		assertEquals(ChartLegend.RIGHT, column.legend());
		assertFalse(column.stacked());
		assertNull(column.max());

		ChartSpec single = (ChartSpec) read("pptColumnChart(['a'], [['S', [1]]])");
		assertEquals("A single series needs no legend.", ChartLegend.NONE, single.legend());
	}

	public void testReaderErrors() throws Exception {
		ResKeyTemplate invalid = com.top_logic.presentation.model.I18NConstants.ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED;

		TopLogicException length = expectError(() -> read("pptBarChart(['a', 'b'], [['S', [1, 2]], ['T', [1]]])"));
		assertEquals("e.series[1][1]", argument(length, invalid, 0));

		TopLogicException mapLength = expectError(() -> read("pptBarChart(['a'], [pptSeries('S', [1, 2])])"));
		assertEquals("e.series[0].values", argument(mapLength, invalid, 0));

		TopLogicException number = expectError(() -> read("pptColumnChart(['a', 'b'], [pptSeries('S', [1, 'x'])])"));
		assertEquals("e.series[0].values[1]", argument(number, invalid, 0));

		TopLogicException categories = expectError(() -> read("pptBarChart([], [])"));
		assertEquals("e.categories", argument(categories, invalid, 0));

		TopLogicException series = expectError(() -> read("pptColumnChart(['a'], [5])"));
		assertEquals("e.series[0]", argument(series, invalid, 0));

		TopLogicException legend = expectError(() -> read("pptColumnChart(['a'], [['S', [1]]], legend: 'middle')"));
		assertEquals("e.legend", argument(legend, invalid, 0));

		TopLogicException role = expectError(() -> read("pptColumnChart(['a'], [['S', [1], 'no role']])"));
		assertEquals("e.series[0][2]", argument(role, invalid, 0));

		TopLogicException unknownRole = expectError(() -> evalOnce(
			"t -> pptDeck($t, pptContent('X', content: pptColumnChart(['a'], [['S', [1], 'chance']])))", template()));
		ResKeyTemplate roleKey = com.top_logic.presentation.export.I18NConstants.ERROR_UNKNOWN_COLOR_ROLE__PATH_ROLE_ROLES;
		assertEquals("slides[0].content[0].series[0].role", argument(unknownRole, roleKey, 0));
		assertEquals("chance", argument(unknownRole, roleKey, 1));
	}

	public void testWrittenCharts() throws Exception {
		BinaryData deck = (BinaryData) evalOnce("t -> pptDeck($t, " + CHART_SLIDE + ")", template());
		writeDeck(deck, "charts.pptx");
		try (XMLSlideShow slideShow = open(deck)) {
			XSLFSlide slide = slideShow.getSlides().get(0);
			List<XSLFGraphicFrame> frames = new ArrayList<>();
			XSLFAutoShape card = null;
			for (XSLFShape shape : slide.getShapes()) {
				if (shape instanceof XSLFGraphicFrame frame && frame.hasChart()) {
					frames.add(frame);
				} else if (shape instanceof XSLFAutoShape auto && auto.getText().startsWith("Hinweis")) {
					card = auto;
				}
			}
			assertEquals(2, frames.size());
			assertNotNull(card);

			Rectangle2D area = analyze(template()).layouts().get(CONTENT_LAYOUT).placeholder("body", CONTENT_IDX)
				.bounds();
			Rectangle2D columnBounds = frames.get(0).getAnchor();
			Rectangle2D cardBounds = card.getAnchor();
			assertTrue("Chart inside the content area: " + columnBounds, contains(area, columnBounds));
			assertTrue("Chart inside the content area: " + frames.get(1).getAnchor(),
				contains(area, frames.get(1).getAnchor()));
			assertEquals(area.getX(), columnBounds.getX(), 0.5);
			assertEquals(220, columnBounds.getHeight(), 0.5);
			assertEquals(cardBounds.getY(), columnBounds.getY(), 0.5);
			assertTrue("Chart left of the card.", columnBounds.getMaxX() < cardBounds.getX());
			assertEquals("The chart has twice the weight of the card.", 2 * cardBounds.getWidth(),
				columnBounds.getWidth(), 0.5);

			XSLFChart column = frames.get(0).getChart();
			CTBarChart columnChart = column.getCTChart().getPlotArea().getBarChartArray(0);
			assertEquals(STBarDir.COL, columnChart.getBarDir().getVal());
			assertEquals(STBarGrouping.CLUSTERED, columnChart.getGrouping().getVal());
			assertEquals(2, columnChart.sizeOfSerArray());
			CTBarSer created = columnChart.getSerArray(0);
			assertEquals("neu", seriesName(created));
			assertEquals(List.of("Q1", "Q2", "Q3"), categories(created));
			assertEquals(Arrays.asList(27.0, 19.0, null), values(created.getVal().getNumRef().getNumCache(), 3));
			assertEquals("accent1", created.getSpPr().getSolidFill().getSchemeClr().getVal().toString());
			CTBarSer closed = columnChart.getSerArray(1);
			assertEquals("erledigt", seriesName(closed));
			assertEquals("FF5733", hex(closed.getSpPr().getSolidFill().getSrgbClr().getVal()));
			assertTrue("Values shown at the columns.", closed.getDLbls().getShowVal().getVal());
			assertTrue("Value axis hidden.", column.getCTChart().getPlotArea().getValAxArray(0).getDelete().getVal());
			assertFalse("No rounded chart border.", column.getCTChartSpace().getRoundedCorners().getVal());

			try (XSSFWorkbook workbook = column.getWorkbook()) {
				XSSFSheet sheet = workbook.getSheetAt(0);
				assertEquals("neu", sheet.getRow(0).getCell(1).getStringCellValue());
				assertEquals("erledigt", sheet.getRow(0).getCell(2).getStringCellValue());
				assertEquals("Q2", sheet.getRow(2).getCell(0).getStringCellValue());
				assertEquals(19.0, sheet.getRow(2).getCell(1).getNumericCellValue());
				assertEquals(15.0, sheet.getRow(3).getCell(2).getNumericCellValue());
				Cell missing = sheet.getRow(3).getCell(1);
				assertTrue("A missing value has no cell value.", missing == null || missing.getCellType() == CellType.BLANK);
			}

			XSLFChart bar = frames.get(1).getChart();
			CTBarChart barChart = bar.getCTChart().getPlotArea().getBarChartArray(0);
			assertEquals(STBarDir.BAR, barChart.getBarDir().getVal());
			assertEquals(STBarGrouping.STACKED, barChart.getGrouping().getVal());
			assertEquals(100, ((Number) barChart.getOverlap().getVal()).intValue());
			CTBarSer errors = barChart.getSerArray(1);
			assertEquals("Fehler", seriesName(errors));
			assertEquals(List.of(75.0, 0.0), values(errors.getVal().getNumRef().getNumCache(), 2));
			assertEquals("Default role of the first series.", "accent1",
				barChart.getSerArray(0).getSpPr().getSolidFill().getSchemeClr().getVal().toString());
			assertEquals("accent3", errors.getSpPr().getSolidFill().getSchemeClr().getVal().toString());
			assertEquals("The label of an empty stacked segment is removed.", 1, errors.getDLbls().sizeOfDLblArray());
			assertEquals(1, errors.getDLbls().getDLblArray(0).getIdx().getVal());
			CTBarSer features = barChart.getSerArray(0);
			assertEquals("The label of a narrow stacked segment is removed.", 1, features.getDLbls().sizeOfDLblArray());
			assertEquals(1, features.getDLbls().getDLblArray(0).getIdx().getVal());
			assertEquals("0", errors.getDLbls().getNumFmt().getFormatCode());
			assertEquals(140.0, bar.getCTChart().getPlotArea().getValAxArray(0).getScaling().getMax().getVal());
			try (XSSFWorkbook workbook = bar.getWorkbook()) {
				assertEquals(75.0, workbook.getSheetAt(0).getRow(1).getCell(2).getNumericCellValue());
			}

			TemplateAnalysis analysis = TemplateAnalysis.analyze(slideShow);
			List<ShapeInfo> charts = new ArrayList<>();
			for (ShapeInfo shape : analysis.slides().get(0).shapes()) {
				if (shape.kind() == ShapeInfo.Kind.CHART) {
					charts.add(shape);
				}
			}
			assertEquals(2, charts.size());
			assertEquals(map(TYPE, "column", SERIES, 2.0, CATEGORIES, 3.0), charts.get(0).toData().get(CHART));
			assertEquals(map(TYPE, "bar", SERIES, 2.0, CATEGORIES, 2.0), charts.get(1).toData().get(CHART));
		}

		try (InputStream in = deck.getStream(); OPCPackage pkg = OPCPackage.open(in)) {
			int charts = 0;
			int workbooks = 0;
			for (PackagePart part : pkg.getParts()) {
				String contentType = part.getContentType();
				if (contentType.equals("application/vnd.openxmlformats-officedocument.drawingml.chart+xml")) {
					charts++;
				} else if (contentType
					.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
					workbooks++;
				}
			}
			assertEquals("Chart parts survive the pruning.", 2, charts);
			assertEquals("Embedded workbooks survive the pruning.", 2, workbooks);
		}

		// The deck is complete: written again by POI, it opens unchanged.
		try (XMLSlideShow slideShow = open(deck)) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			slideShow.write(buffer);
			try (XMLSlideShow reopened = new XMLSlideShow(new ByteArrayInputStream(buffer.toByteArray()))) {
				assertEquals(2, reopened.getCharts().size());
			}
		}
	}

	public void testRenderCharts() throws Exception {
		BinaryData deck = (BinaryData) evalOnce("t -> pptDeck($t, " + CHART_SLIDE + ")", template());
		try (XMLSlideShow slideShow = open(deck)) {
			XSLFSlide slide = slideShow.getSlides().get(0);
			// The theme colors of the slide master of the slide.
			Map<String, String> colors = TemplateAnalysis.analyze(slideShow).masters()
				.get(slideShow.getSlideMasters().indexOf(slide.getSlideMaster())).theme().colors();
			String accent1 = colors.get("accent1");
			String accent3 = colors.get("accent3");
			BufferedImage image = new SlideRenderer().render(slide, RENDER_WIDTH);
			writeImages(List.of(image), "charts-poi");
			double scale = RENDER_WIDTH / slideShow.getPageSize().getWidth();
			List<XSLFGraphicFrame> frames = new ArrayList<>();
			for (XSLFShape shape : slide.getShapes()) {
				if (shape instanceof XSLFGraphicFrame frame && frame.hasChart()) {
					frames.add(frame);
				}
			}
			Rectangle2D column = frames.get(0).getAnchor();
			assertTrue("Columns of topicA drawn.", countPixels(image, column, scale, accent1) > MIN_PIXELS);
			assertTrue("Columns of the role risk drawn.", countPixels(image, column, scale, "FF5733") > MIN_PIXELS);
			Rectangle2D bar = frames.get(1).getAnchor();
			assertTrue("Bars of topicA drawn.", countPixels(image, bar, scale, accent1) > MIN_PIXELS);
			assertTrue("Bars of topicC drawn.", countPixels(image, bar, scale, accent3) > MIN_PIXELS);
			assertEquals("No foreign colors in the bar chart.", 0, countPixels(image, bar, scale, "FF5733"));
		}
	}

	public void testRenderChartPlaceholder() throws Exception {
		try (XMLSlideShow slideShow = open(template())) {
			TemplateAnalysis analysis = TemplateAnalysis.analyze(slideShow);
			for (SlideInfo slideInfo : analysis.slides()) {
				List<ShapeInfo> charts = new ArrayList<>();
				collectCharts(charts, slideInfo.shapes());
				if (charts.isEmpty()) {
					continue;
				}
				XSLFSlide slide = slideShow.getSlides().get(slideInfo.index());
				BufferedImage image = new SlideRenderer().render(slide, RENDER_WIDTH);
				writeImages(List.of(image), "template-chart");
				double scale = RENDER_WIDTH / slideShow.getPageSize().getWidth();
				assertTrue("The line chart is drawn as placeholder.",
					countPixels(image, charts.get(0).bounds(), scale, PLACEHOLDER_COLOR) > MIN_PIXELS);
				return;
			}
			fail("The template has a sample chart.");
		}
	}

	public void testTemplateChartAnalysis() throws Exception {
		List<Object> charts = new ArrayList<>();
		for (SlideInfo slide : analyze(template()).slides()) {
			addCharts(charts, slide.shapes());
		}
		assertEquals("The sample line chart of the template.",
			List.of(map(TYPE, "line", SERIES, 3.0, CATEGORIES, 5.0)), charts);
	}

	private static void collectCharts(List<ShapeInfo> charts, List<ShapeInfo> shapes) {
		for (ShapeInfo shape : shapes) {
			if (shape.kind() == ShapeInfo.Kind.CHART) {
				charts.add(shape);
			}
			collectCharts(charts, shape.children());
		}
	}

	/**
	 * The number of pixels in the given slide area of the image that have the given color, up to a
	 * small deviation for anti-aliasing.
	 */
	private static int countPixels(BufferedImage image, Rectangle2D area, double scale, String rgb) {
		int expected = Integer.parseInt(rgb, 16);
		int result = 0;
		int maxX = Math.min(image.getWidth(), (int) (area.getMaxX() * scale));
		int maxY = Math.min(image.getHeight(), (int) (area.getMaxY() * scale));
		for (int y = Math.max(0, (int) (area.getY() * scale)); y < maxY; y++) {
			for (int x = Math.max(0, (int) (area.getX() * scale)); x < maxX; x++) {
				int actual = image.getRGB(x, y);
				if (Math.abs(((actual >> 16) & 0xFF) - ((expected >> 16) & 0xFF)) <= COLOR_TOLERANCE
					&& Math.abs(((actual >> 8) & 0xFF) - ((expected >> 8) & 0xFF)) <= COLOR_TOLERANCE
					&& Math.abs((actual & 0xFF) - (expected & 0xFF)) <= COLOR_TOLERANCE) {
					result++;
				}
			}
		}
		return result;
	}

	private static void addCharts(List<Object> charts, List<ShapeInfo> shapes) {
		for (ShapeInfo shape : shapes) {
			if (shape.kind() == ShapeInfo.Kind.CHART) {
				charts.add(shape.toData().get(CHART));
			}
			addCharts(charts, shape.children());
		}
	}

	private static String seriesName(CTBarSer ser) {
		return ser.getTx().getStrRef().getStrCache().getPtArray(0).getV();
	}

	private static List<String> categories(CTBarSer ser) {
		List<String> result = new ArrayList<>();
		for (CTStrVal point : ser.getCat().getStrRef().getStrCache().getPtArray()) {
			result.add(point.getV());
		}
		return result;
	}

	private static List<Double> values(CTNumData data, int count) {
		assertEquals(count, data.getPtCount().getVal());
		Double[] result = new Double[count];
		for (CTNumVal point : data.getPtArray()) {
			result[(int) point.getIdx()] = Double.valueOf(point.getV());
		}
		return Arrays.asList(result);
	}

	private static String hex(byte[] rgb) {
		return String.format("%02X%02X%02X", rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF);
	}

	private static boolean contains(Rectangle2D area, Rectangle2D bounds) {
		double tolerance = 0.5;
		return bounds.getX() >= area.getX() - tolerance && bounds.getY() >= area.getY() - tolerance
			&& bounds.getMaxX() <= area.getMaxX() + tolerance && bounds.getMaxY() <= area.getMaxY() + tolerance;
	}

	private Object read(String script) throws Exception {
		return DescriptionReader.readElement(new Value(eval(script), "e"));
	}

	private static Map<String, Object> map(Object... keysAndValues) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (int n = 0; n < keysAndValues.length; n += 2) {
			result.put((String) keysAndValues[n], keysAndValues[n + 1]);
		}
		return result;
	}

	public static Test suite() {
		return suite(TestCharts.class);
	}

}
