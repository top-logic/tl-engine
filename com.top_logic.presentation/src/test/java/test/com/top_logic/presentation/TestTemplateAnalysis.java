/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.presentation;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.xslf.usermodel.SlideLayout;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.presentation.template.ColorInfo;
import com.top_logic.presentation.template.LayoutInfo;
import com.top_logic.presentation.template.TemplateAnalysis;
import com.top_logic.presentation.template.TemplateData;
import com.top_logic.util.error.TopLogicException;

/**
 * Test case for the TL-Script function <code>pptAnalyze()</code> and {@link TemplateAnalysis}.
 *
 * <p>
 * The fixture is the TopLogic corporate template: 16:9 slides, three masters (title, agenda,
 * content), 14 layouts and sample slides with cards.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestTemplateAnalysis extends AbstractSearchExpressionTest {

	private static final String TEMPLATE = "toplogic-template.pptx";

	/** Layout with dark background, centered title and subtitle. */
	private static final int TITLE_LAYOUT = 0;

	/** Content layout with title, subtitle, content area and footer. */
	private static final int CONTENT_LAYOUT = 11;

	/** Layout with a helper raster. */
	private static final int GRID_LAYOUT = 10;

	private static Map<?, ?> _analysis;

	private Map<?, ?> analysis() throws Exception {
		if (_analysis == null) {
			_analysis = (Map<?, ?>) eval("t -> pptAnalyze($t)", template());
		}
		return _analysis;
	}

	private BinaryData template() {
		return BinaryDataFactory.createBinaryData(TestTemplateAnalysis.class, TEMPLATE);
	}

	public void testSlideSize() throws Exception {
		assertEquals(960.0, get(analysis(), TemplateData.SLIDE_SIZE, TemplateData.WIDTH));
		assertEquals(540.0, get(analysis(), TemplateData.SLIDE_SIZE, TemplateData.HEIGHT));
	}

	public void testLayoutOrder() throws Exception {
		List<?> layouts = list(analysis(), TemplateData.LAYOUTS);
		assertEquals(14, layouts.size());
		for (int n = 0; n < layouts.size(); n++) {
			assertEquals(Double.valueOf(n), get(layouts.get(n), TemplateData.INDEX));
		}
		assertEquals("1_Titel / Abschluss", get(layouts.get(TITLE_LAYOUT), TemplateData.NAME));
		assertEquals("3_Grid", get(layouts.get(GRID_LAYOUT), TemplateData.NAME));
		assertEquals("4_Text volle Breite", get(layouts.get(CONTENT_LAYOUT), TemplateData.NAME));

		// The layouts reference their masters.
		assertEquals(0.0, get(layouts.get(TITLE_LAYOUT), TemplateData.MASTER, TemplateData.INDEX));
		assertEquals(2.0, get(layouts.get(CONTENT_LAYOUT), TemplateData.MASTER, TemplateData.INDEX));
		assertEquals("3_Inhalt", get(layouts.get(CONTENT_LAYOUT), TemplateData.MASTER, TemplateData.NAME));
	}

	public void testScriptAccess() throws Exception {
		assertEquals("4_Text volle Breite", eval("t -> pptAnalyze($t)['layouts'][11]['name']", template()));
	}

	public void testTheme() throws Exception {
		Map<?, ?> contentTheme = (Map<?, ?>) get(analysis(), TemplateData.MASTERS, 2, TemplateData.THEME);
		assertEquals("2968C8", get(contentTheme, TemplateData.COLORS, "accent1"));
		assertEquals("101113", get(contentTheme, TemplateData.COLORS, "dk1"));
		assertEquals("FFFFFF", get(contentTheme, TemplateData.COLORS, "lt1"));
		assertEquals("Open Sans", get(contentTheme, TemplateData.FONTS, TemplateData.MAJOR));
		assertEquals("Open Sans", get(contentTheme, TemplateData.FONTS, TemplateData.MINOR));

		// The title master swaps dark and light text colors for its dark background.
		assertEquals("FFFFFF", get(analysis(), TemplateData.MASTERS, 0, TemplateData.THEME, TemplateData.COLORS, "dk1"));
	}

	public void testTitleLayout() throws Exception {
		Map<?, ?> layout = layout(TITLE_LAYOUT);
		assertEquals(TemplateData.BACKGROUND + " of the title layout", "picture",
			get(layout, TemplateData.BACKGROUND, TemplateData.TYPE));

		Map<?, ?> title = placeholder(layout, "ctrTitle", 0);
		assertEquals("center", get(title, TemplateData.TEXT, TemplateData.ALIGN));
		assertEquals("FFFFFF", get(title, TemplateData.TEXT, TemplateData.COLOR, TemplateData.RGB));
		assertEquals("tx1", get(title, TemplateData.TEXT, TemplateData.COLOR, TemplateData.SCHEME));
		assertEquals(Boolean.TRUE, get(title, TemplateData.TEXT, TemplateData.CAPS));

		Map<?, ?> subtitle = placeholder(layout, "subTitle", 1);
		assertEquals("center", get(subtitle, TemplateData.TEXT, TemplateData.ALIGN));
		assertEquals("FFFFFF", get(subtitle, TemplateData.TEXT, TemplateData.COLOR, TemplateData.RGB));
		assertTrue(number(subtitle, TemplateData.Y) > number(title, TemplateData.Y));
	}

	public void testContentLayout() throws Exception {
		Map<?, ?> layout = layout(CONTENT_LAYOUT);
		assertEquals(List.of(), get(layout, TemplateData.DECORATIONS));

		Map<?, ?> title = placeholder(layout, "title", 0);
		assertEquals("Open Sans", get(title, TemplateData.TEXT, TemplateData.FONT));
		assertEquals(28.0, get(title, TemplateData.TEXT, TemplateData.SIZE));
		assertEquals(Boolean.TRUE, get(title, TemplateData.TEXT, TemplateData.CAPS));
		assertEquals(Boolean.TRUE, get(title, TemplateData.TEXT, TemplateData.BOLD));
		assertEquals("accent1", get(title, TemplateData.TEXT, TemplateData.COLOR, TemplateData.SCHEME));
		assertEquals("<Titel hinzufügen>", get(title, TemplateData.PROMPT));

		Map<?, ?> subtitle = placeholder(layout, "body", 13);
		Map<?, ?> footer = placeholder(layout, "body", 4);
		Map<?, ?> content = placeholder(layout, "body", 21);
		assertEquals("Subtitel 1", get(subtitle, TemplateData.NAME));
		assertEquals("Footer", get(footer, TemplateData.NAME));

		// Title, subtitle, content area and footer are stacked from top to bottom.
		double subtitleBottom = number(subtitle, TemplateData.Y) + number(subtitle, TemplateData.HEIGHT);
		assertTrue(number(subtitle, TemplateData.Y) >= number(title, TemplateData.Y) + number(title, TemplateData.HEIGHT) - 0.1);
		assertTrue(number(content, TemplateData.Y) >= subtitleBottom - 0.1);
		assertTrue(number(footer, TemplateData.Y) > subtitleBottom);
		assertTrue(number(footer, TemplateData.Y) + number(footer, TemplateData.HEIGHT) <= 540);

		// The subtitle color is a darker variant of the accent color.
		Map<?, ?> subtitleColor = (Map<?, ?>) get(subtitle, TemplateData.TEXT, TemplateData.COLOR);
		assertEquals("accent1", subtitleColor.get(TemplateData.SCHEME));
		assertEquals(0.75, subtitleColor.get(ColorInfo.LUM_MOD));
		assertEquals(10.0, get(footer, TemplateData.TEXT, TemplateData.SIZE));
	}

	public void testAgendaLayouts() throws Exception {
		int[] agendaLayouts = { 3, 4, 5, 7, 8, 9 };
		for (int n = 0; n < agendaLayouts.length; n++) {
			int entries = n + 3;
			Map<?, ?> layout = layout(agendaLayouts[n]);
			assertTrue((String) get(layout, TemplateData.NAME), ((String) get(layout, TemplateData.NAME))
				.endsWith("Index (" + entries + ")"));
			int entryPlaceholders = 0;
			for (Object placeholder : list(layout, TemplateData.PLACEHOLDERS)) {
				if ("body".equals(get(placeholder, TemplateData.TYPE)) && !Double.valueOf(4).equals(get(placeholder, TemplateData.IDX))) {
					entryPlaceholders++;
				}
			}
			assertEquals(entries, entryPlaceholders);
		}
	}

	public void testGridDecorations() throws Exception {
		List<?> decorations = list(layout(GRID_LAYOUT), TemplateData.DECORATIONS);
		assertEquals(84, decorations.size());
		for (Object decoration : decorations) {
			assertEquals("shape", get(decoration, TemplateData.KIND));
			assertEquals("rect", get(decoration, TemplateData.GEOMETRY, TemplateData.PRESET));
			Map<?, ?> color = (Map<?, ?>) get(decoration, TemplateData.FILL, TemplateData.COLOR);
			assertEquals("solid", get(decoration, TemplateData.FILL, TemplateData.TYPE));
			assertEquals("FFBCAD", color.get(TemplateData.RGB));
			assertEquals("accent2", color.get(TemplateData.SCHEME));
			assertEquals(0.4, color.get(ColorInfo.LUM_MOD));
			assertEquals(0.6, color.get(ColorInfo.LUM_OFF));
		}
	}

	public void testCard() throws Exception {
		Map<?, ?> slide = (Map<?, ?>) get(analysis(), TemplateData.SLIDES, 1);
		assertEquals(Double.valueOf(CONTENT_LAYOUT), get(slide, TemplateData.LAYOUT, TemplateData.INDEX));

		Map<?, ?> card = shape(list(slide, TemplateData.SHAPES), "Abgerundetes Rechteck 89");
		assertEquals("shape", get(card, TemplateData.KIND));
		assertEquals("roundRect", get(card, TemplateData.GEOMETRY, TemplateData.PRESET));
		assertTrue(number((Map<?, ?>) get(card, TemplateData.GEOMETRY), TemplateData.CORNER_RADIUS) > 0);

		assertEquals("gradient", get(card, TemplateData.FILL, TemplateData.TYPE));
		List<?> stops = list((Map<?, ?>) get(card, TemplateData.FILL), TemplateData.STOPS);
		assertEquals(2, stops.size());
		assertEquals("bg1", get(stops.get(0), TemplateData.COLOR, TemplateData.SCHEME));
		assertEquals("F2F2F2", get(stops.get(0), TemplateData.COLOR, TemplateData.RGB));
		assertEquals(1.0, get(stops.get(1), TemplateData.POSITION));

		assertEquals("accent1", get(card, TemplateData.LINE, TemplateData.STOPS, 1, TemplateData.COLOR, TemplateData.SCHEME));
		assertEquals(1.0, get(card, TemplateData.LINE, TemplateData.WIDTH));

		Map<?, ?> shadow = (Map<?, ?>) get(card, TemplateData.SHADOW);
		assertEquals("accent1", get(shadow, TemplateData.COLOR, TemplateData.SCHEME));
		assertEquals(0.75, get(shadow, TemplateData.COLOR, ColorInfo.LUM_MOD));
		assertEquals(0.2, get(shadow, TemplateData.COLOR, ColorInfo.ALPHA));
		assertEquals(16.0, get(shadow, TemplateData.BLUR));
		assertEquals(8.0, get(shadow, TemplateData.DISTANCE));
		assertEquals(90.0, get(shadow, TemplateData.DIRECTION));

		assertEquals("Beispiel", get(card, TemplateData.TEXT, TemplateData.CONTENT));
		assertEquals(14.0, get(card, TemplateData.TEXT, TemplateData.SIZE));
	}

	public void testGroupChildrenOnSlideCoordinates() throws Exception {
		for (Object slide : list(analysis(), TemplateData.SLIDES)) {
			for (Object shape : list((Map<?, ?>) slide, TemplateData.SHAPES)) {
				if ("group".equals(get(shape, TemplateData.KIND))) {
					Rectangle2D group = bounds((Map<?, ?>) shape);
					for (Object child : list((Map<?, ?>) shape, TemplateData.CHILDREN)) {
						Rectangle2D childBounds = bounds((Map<?, ?>) child);
						assertTrue(child + " outside " + group,
							group.createUnion(childBounds).getWidth() <= group.getWidth() + 0.1
								&& group.createUnion(childBounds).getHeight() <= group.getHeight() + 0.1);
					}
				}
			}
		}
	}

	public void testJson() throws Exception {
		Object json = eval("t -> toJson(pptAnalyze($t))", template());
		assertTrue(json instanceof String);
		assertTrue(((String) json).contains("\"4_Text volle Breite\""));
		assertEquals("Result is not deterministic.", json, eval("t -> toJson(pptAnalyze($t))", template()));
	}

	public void testJavaApi() throws Exception {
		try (XMLSlideShow slideShow = new XMLSlideShow(template().getStream())) {
			TemplateAnalysis analysis = TemplateAnalysis.analyze(slideShow);
			assertEquals(TemplateAnalysis.layouts(slideShow).size(), analysis.layouts().size());

			LayoutInfo content = analysis.layout("4_Text volle Breite");
			assertEquals(CONTENT_LAYOUT, content.index());
			assertEquals(TemplateAnalysis.layouts(slideShow).get(CONTENT_LAYOUT).getName(), content.name());
			assertEquals("Footer", content.placeholder("body", 4).name());
			assertEquals("title", content.placeholder("title").type());
		}
	}

	public void testDefaultPresentation() throws Exception {
		BinaryData data;
		try (XMLSlideShow slideShow = new XMLSlideShow()) {
			XSLFSlide slide = slideShow.createSlide();
			XSLFAutoShape box = slide.createAutoShape();
			box.setShapeType(ShapeType.ROUND_RECT);
			box.setAnchor(new Rectangle2D.Double(10, 20, 100, 50));
			box.setFillColor(Color.RED);
			box.setText("Hello");
			XSLFTable table = slide.createTable(2, 3);
			table.setAnchor(new Rectangle2D.Double(10, 100, 300, 60));
			table.getCell(0, 0).setText("Head");
			table.getCell(1, 0).setText("Body");

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			slideShow.write(buffer);
			data = BinaryDataFactory.createBinaryData(buffer.toByteArray(),
				"application/vnd.openxmlformats-officedocument.presentationml.presentation", "default.pptx");
		}

		Map<?, ?> analysis = (Map<?, ?>) eval("t -> pptAnalyze($t)", data);
		assertEquals(720.0, get(analysis, TemplateData.SLIDE_SIZE, TemplateData.WIDTH));
		assertEquals(1, list(analysis, TemplateData.MASTERS).size());

		List<?> layouts = list(analysis, TemplateData.LAYOUTS);
		List<Object> types = new ArrayList<>();
		for (Object layout : layouts) {
			types.add(get(layout, TemplateData.TYPE));
		}
		assertTrue(types.toString(), types.contains(SlideLayout.TITLE.name()));
		assertTrue(types.toString(), types.contains(SlideLayout.TITLE_AND_CONTENT.name()));

		List<?> shapes = list((Map<?, ?>) get(analysis, TemplateData.SLIDES, 0), TemplateData.SHAPES);
		Map<?, ?> shape = (Map<?, ?>) shapes.get(0);
		assertEquals("shape", get(shape, TemplateData.KIND));
		assertEquals("roundRect", get(shape, TemplateData.GEOMETRY, TemplateData.PRESET));
		assertEquals(10.0, get(shape, TemplateData.X));
		assertEquals("FF0000", get(shape, TemplateData.FILL, TemplateData.COLOR, TemplateData.RGB));
		assertNull(get(shape, TemplateData.FILL, TemplateData.COLOR, TemplateData.SCHEME));
		assertEquals("Hello", get(shape, TemplateData.TEXT, TemplateData.CONTENT));

		Map<?, ?> table = (Map<?, ?>) shapes.get(1);
		assertEquals("table", get(table, TemplateData.KIND));
		assertEquals(2.0, get(table, TemplateData.TABLE, TemplateData.ROWS));
		assertEquals(3.0, get(table, TemplateData.TABLE, TemplateData.COLUMNS));
		assertEquals("Head", get(table, TemplateData.TABLE, TemplateData.HEADER_CELL, TemplateData.TEXT, TemplateData.CONTENT));
		assertEquals("Body", get(table, TemplateData.TABLE, TemplateData.BODY_CELL, TemplateData.TEXT, TemplateData.CONTENT));
	}

	public void testNotAPresentation() throws Exception {
		try {
			eval("pptAnalyze(binary(name: 'notes.txt', data: 'Hello world', contentType: 'text/plain'))");
			fail("A text file is not a presentation.");
		} catch (TopLogicException ex) {
			// Expected.
		}
	}

	private Map<?, ?> layout(int index) throws Exception {
		return (Map<?, ?>) get(analysis(), TemplateData.LAYOUTS, index);
	}

	private static Map<?, ?> placeholder(Map<?, ?> layout, String type, int idx) {
		for (Object placeholder : list(layout, TemplateData.PLACEHOLDERS)) {
			if (type.equals(get(placeholder, TemplateData.TYPE))
				&& Double.valueOf(idx).equals(get(placeholder, TemplateData.IDX))) {
				return (Map<?, ?>) placeholder;
			}
		}
		fail("No placeholder " + type + "/" + idx + " in " + layout.get(TemplateData.NAME));
		return null;
	}

	private static Map<?, ?> shape(List<?> shapes, String name) {
		for (Object shape : shapes) {
			if (name.equals(get(shape, TemplateData.NAME))) {
				return (Map<?, ?>) shape;
			}
			Object children = ((Map<?, ?>) shape).get(TemplateData.CHILDREN);
			if (children != null) {
				Map<?, ?> result = shape((List<?>) children, name);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	private static Rectangle2D bounds(Map<?, ?> shape) {
		return new Rectangle2D.Double(number(shape, TemplateData.X), number(shape, TemplateData.Y),
			number(shape, TemplateData.WIDTH), number(shape, TemplateData.HEIGHT));
	}

	private static List<?> list(Map<?, ?> data, String key) {
		Object value = data.get(key);
		assertTrue("Not a list: " + key + "=" + value, value instanceof List<?>);
		return (List<?>) value;
	}

	private static double number(Map<?, ?> data, String key) {
		Object value = data.get(key);
		assertTrue("Not a Double: " + key + "=" + value, value instanceof Double);
		return ((Double) value).doubleValue();
	}

	/**
	 * Navigates the given data along the given dictionary keys and list indices.
	 */
	private static Object get(Object data, Object... path) {
		Object result = data;
		for (Object step : path) {
			if (result == null) {
				return null;
			}
			if (step instanceof Integer index) {
				result = ((List<?>) result).get(index.intValue());
			} else {
				result = ((Map<?, ?>) result).get(step);
			}
		}
		return result;
	}

	public static Test suite() {
		return suite(TestTemplateAnalysis.class);
	}

}
