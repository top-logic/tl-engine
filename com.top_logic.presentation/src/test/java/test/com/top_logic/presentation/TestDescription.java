/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.presentation;

import static com.top_logic.presentation.data.PresentationData.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyTemplate;
import com.top_logic.presentation.model.Bounds;
import com.top_logic.presentation.model.BulletSpec;
import com.top_logic.presentation.model.CellSpec;
import com.top_logic.presentation.model.ColorSpec;
import com.top_logic.presentation.model.DescriptionReader;
import com.top_logic.presentation.model.ElementSpec;
import com.top_logic.presentation.model.GeometrySpec;
import com.top_logic.presentation.model.I18NConstants;
import com.top_logic.presentation.model.ImageFit;
import com.top_logic.presentation.model.ImageSpec;
import com.top_logic.presentation.model.InsetsSpec;
import com.top_logic.presentation.model.LayoutRef;
import com.top_logic.presentation.model.LayoutRole;
import com.top_logic.presentation.model.LineSpec;
import com.top_logic.presentation.model.Overflow;
import com.top_logic.presentation.model.PaintSpec;
import com.top_logic.presentation.model.ParagraphSpec;
import com.top_logic.presentation.model.PlaceholderKey;
import com.top_logic.presentation.model.RunSpec;
import com.top_logic.presentation.model.RunStyle;
import com.top_logic.presentation.model.ShadowSpec;
import com.top_logic.presentation.model.ShapeSpec;
import com.top_logic.presentation.model.SlideSpec;
import com.top_logic.presentation.model.TableSpec;
import com.top_logic.presentation.model.TextAlign;
import com.top_logic.presentation.model.TextAnchor;
import com.top_logic.presentation.model.TextBoxSpec;
import com.top_logic.presentation.model.TextSpec;
import com.top_logic.presentation.model.Value;
import com.top_logic.presentation.template.ColorInfo;
import com.top_logic.util.error.TopLogicException;

/**
 * Test case for the presentation description: the TL-Script builder functions such as
 * <code>pptShape()</code> and {@link DescriptionReader}.
 */
@SuppressWarnings("javadoc")
public class TestDescription extends AbstractSearchExpressionTest {

	private static final String TEMPLATE = "toplogic-template.pptx";

	/**
	 * A slide using all builder functions except <code>pptImage()</code>.
	 */
	private static final String FULL_SLIDE = """
		pptSlide(
			layout: "content",
			title: "Title",
			subtitle: pptRun("Sub", italic: true),
			placeholders: {"13": "Footer", "body": ["a", "b"]},
			notes: ["Line 1", "Line 2"],
			content: [
				pptShape(geometry: "ellipse", x: 30, y: 130, width: 300, height: 200,
					fill: pptGradient(["accent1", pptColor("accent2", lumMod: 0.75)], angle: 90),
					line: pptLine(color: "#1f4e79", width: 1.5, dash: "dash"),
					shadow: pptShadow(color: pptColor("000000", alpha: 0.3), blur: 8),
					text: [
						pptParagraph([pptRun("Bold ", bold: true), "plain"], align: "center",
							bullet: {"char": "-", "color": "risiko"}, level: 1, spaceBefore: 6),
						"second"
					],
					size: 14, color: "bg1", anchor: "middle", insets: 4, overflow: "ellipsis", split: true),
				pptTextBox("Box", x: 10, y: 10, width: 100, height: 20, font: "Arial", caps: true),
				pptShape(cornerRadius: 8, fill: "none", line: "none", shadow: true, text: 42),
				pptTable([["Name", "Value"], ["A", 1], ["B", pptCell("2", fill: "accent2", bold: true, align: "right")]],
					columnWeights: [2, 1], headerRows: 1, rowHeight: 20)
			])
		""";

	public void testShapeBuilder() throws Exception {
		Object shape = eval("""
			pptShape(geometry: "roundRect", x: 30, y: 130, width: 300, height: 200,
				fill: "accent1", text: "Hallo")
			""");
		assertEquals(map(
			TYPE, TYPE_SHAPE,
			X, 30.0, Y, 130.0, WIDTH, 300.0, HEIGHT, 200.0,
			GEOMETRY, "roundRect",
			FILL, "accent1",
			TEXT, "Hallo"), shape);
	}

	public void testGeometryBuilder() throws Exception {
		assertEquals(map(TYPE, TYPE_SHAPE, GEOMETRY, map(CORNER_RADIUS, 8.0)), eval("pptShape(cornerRadius: 8)"));
		assertEquals(map(TYPE, TYPE_SHAPE, GEOMETRY, map(PRESET, "wedgeRectCallout", ADJUST, map("adj1", 5000.0))),
			eval("pptShape(geometry: 'wedgeRectCallout', adjust: {'adj1': 5000})"));
	}

	public void testSlideBuilder() throws Exception {
		Object slide = eval("""
			pptSlide(layout: 3, title: "T", content: pptTextBox("x", size: 12), notes: "n", hidden: true)
			""");
		assertEquals(map(
			TYPE, TYPE_SLIDE,
			LAYOUT, 3.0,
			TITLE, "T",
			CONTENT, List.of(map(TYPE, TYPE_TEXT_BOX, TEXT, "x", SIZE, 12.0)),
			NOTES, "n",
			HIDDEN, true), slide);
	}

	public void testTextBuilders() throws Exception {
		assertEquals(
			map(RUNS, List.of(map(TEXT, "a", BOLD, true), "b"), ALIGN, "center", LEVEL, 1.0, BULLET, false),
			eval("pptParagraph([pptRun('a', bold: true), 'b'], align: 'center', level: 1, bullet: false)"));
		assertEquals(map(TEXT, "x", COLOR, "accent1", CHARACTER_SPACING, 1.5, HYPERLINK, "https://top-logic.com"),
			eval("pptRun('x', color: 'accent1', spacing: 1.5, hyperlink: 'https://top-logic.com')"));
		assertEquals(map(TEXT, "c", FILL, "accent2", BOLD, true), eval("pptCell('c', fill: 'accent2', bold: true)"));
	}

	public void testStyleBuilders() throws Exception {
		assertEquals(map(SCHEME, "accent1", ColorInfo.LUM_MOD, 0.75), eval("pptColor('accent1', lumMod: 0.75)"));
		assertEquals(map(RGB, "1F4E79"), eval("pptColor('#1f4e79')"));
		assertEquals(map(ROLE, "risiko", ColorInfo.ALPHA, 0.5), eval("pptColor('risiko', alpha: 0.5)"));
		assertEquals(map(SCHEME, "tx1", ColorInfo.LUM_MOD, 0.5, ColorInfo.ALPHA, 0.2),
			eval("pptColor(pptColor('tx1', lumMod: 0.5), alpha: 0.2)"));
		assertEquals(map(TYPE, "gradient", STOPS, List.of("accent1", "accent2"), PATH, "circle"),
			eval("pptGradient(['accent1', 'accent2'], path: 'circle')"));
		assertEquals(map(COLOR, "tx1", WIDTH, 0.75), eval("pptLine('tx1', width: 0.75)"));
		assertEquals(map(BLUR, 4.0, DIRECTION, 45.0), eval("pptShadow(blur: 4, direction: 45)"));

		TopLogicException error = expectError(() -> eval("pptColor('#12345')"));
		assertEquals("color", argument(error, I18NConstants.ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED, 0));
	}

	public void testImageBuilder() throws Exception {
		BinaryData template = template();
		Object image = eval("d -> pptImage($d, x: 1, y: 2, width: 3, height: 4, fit: 'cover')", template);
		assertEquals(map(TYPE, TYPE_IMAGE, X, 1.0, Y, 2.0, WIDTH, 3.0, HEIGHT, 4.0, DATA, template, FIT, "cover"),
			image);
		ImageSpec spec = (ImageSpec) DescriptionReader.readElement(new Value(image, "image"));
		assertEquals(new Bounds(1.0, 2.0, 3.0, 4.0, false, null), spec.bounds());
		assertSame(template, spec.data());
		assertEquals(ImageFit.COVER, spec.fit());
	}

	public void testRoundTrip() throws Exception {
		Object slide = eval(FULL_SLIDE);
		Object json = eval("s -> parseJson(toJson($s))", slide);
		assertEquals(DescriptionReader.readSlides(slide), DescriptionReader.readSlides(json));
	}

	public void testReadFullSlide() throws Exception {
		SlideSpec slide = DescriptionReader.readSlides(eval(FULL_SLIDE)).get(0);

		assertEquals(new LayoutRef.Role(LayoutRole.CONTENT), slide.layout());
		assertEquals(TextSpec.of(List.of(ParagraphSpec.plain("Title"))), slide.title());
		assertEquals(TextSpec.of(List.of(ParagraphSpec.of(List.of(
			new RunSpec("Sub", new RunStyle(null, true, null, null, null, null, null, null), null))))),
			slide.subtitle());
		assertEquals(TextSpec.of(List.of(ParagraphSpec.plain("Footer"))),
			slide.placeholders().get(new PlaceholderKey.Index(13)));
		assertEquals(TextSpec.of(List.of(ParagraphSpec.plain("a"), ParagraphSpec.plain("b"))),
			slide.placeholders().get(new PlaceholderKey.Name("body")));
		assertEquals("Line 1\nLine 2", slide.notes());
		assertFalse(slide.hidden());

		List<ElementSpec> content = slide.content();
		assertEquals(4, content.size());

		ShapeSpec shape = (ShapeSpec) content.get(0);
		assertEquals(new Bounds(30.0, 130.0, 300.0, 200.0, false, null), shape.bounds());
		assertEquals(new GeometrySpec("ellipse", Map.of(), null), shape.geometry());
		assertEquals(new PaintSpec.Gradient(List.of(
			new PaintSpec.Stop(0, new ColorSpec.Scheme("accent1", Map.of())),
			new PaintSpec.Stop(1, new ColorSpec.Scheme("accent2", Map.of(ColorInfo.LUM_MOD, 0.75)))), 90.0, null),
			shape.fill());
		assertEquals(new LineSpec(new PaintSpec.Solid(new ColorSpec.Rgb("1F4E79", Map.of())), 1.5, "dash"),
			shape.line());
		assertEquals(new ShadowSpec(new ColorSpec.Rgb("000000", Map.of(ColorInfo.ALPHA, 0.3)), 8,
			ShadowSpec.DEFAULT_DISTANCE, ShadowSpec.DEFAULT_DIRECTION), shape.shadow());
		TextSpec text = shape.text();
		assertEquals(new RunStyle(null, null, null, null, 14.0, new ColorSpec.Scheme("bg1", Map.of()), null, null),
			text.style());
		assertEquals(TextAnchor.MIDDLE, text.anchor());
		assertEquals(new InsetsSpec(4, 4, 4, 4), text.insets());
		assertEquals(Overflow.ELLIPSIS, text.overflow());
		assertTrue(text.split());
		assertEquals(2, text.paragraphs().size());
		ParagraphSpec first = text.paragraphs().get(0);
		assertEquals(List.of(
			new RunSpec("Bold ", new RunStyle(true, null, null, null, null, null, null, null), null),
			RunSpec.plain("plain")), first.runs());
		assertEquals(TextAlign.CENTER, first.align());
		assertEquals(Integer.valueOf(1), first.level());
		assertEquals(new BulletSpec(true, "-", new ColorSpec.Role("risiko", Map.of())), first.bullet());
		assertEquals(6.0, first.spaceBefore());
		assertEquals(ParagraphSpec.plain("second"), text.paragraphs().get(1));

		TextBoxSpec box = (TextBoxSpec) content.get(1);
		assertEquals(new Bounds(10.0, 10.0, 100.0, 20.0, false, null), box.bounds());
		assertEquals(new TextSpec(List.of(ParagraphSpec.plain("Box")),
			new RunStyle(null, null, null, true, null, null, "Arial", null), null, null, InsetsSpec.ZERO, null, false, null),
			box.text());

		ShapeSpec rounded = (ShapeSpec) content.get(2);
		assertEquals(Bounds.NONE, rounded.bounds());
		assertEquals(new GeometrySpec(GeometrySpec.ROUND_RECT, Map.of(), 8.0), rounded.geometry());
		assertEquals(PaintSpec.NONE, rounded.fill());
		assertEquals(LineSpec.NONE, rounded.line());
		assertEquals(ShadowSpec.DEFAULT_COLOR, rounded.shadow().color());
		assertEquals(TextSpec.of(List.of(ParagraphSpec.plain("42"))), rounded.text());

		TableSpec table = (TableSpec) content.get(3);
		assertEquals(List.of(2.0, 1.0), table.columnWeights());
		assertNull(table.columnWidths());
		assertEquals(1, table.headerRows());
		assertEquals(20.0, table.rowHeight());
		assertEquals(3, table.rows().size());
		assertEquals(2, table.columnCount());
		assertEquals(new CellSpec(TextSpec.of(List.of(ParagraphSpec.plain("1"))), null), table.rows().get(1).get(1));
		assertEquals(new CellSpec(
			new TextSpec(List.of(ParagraphSpec.plain("2")), new RunStyle(true, null, null, null, null, null, null, null),
				TextAlign.RIGHT, null, null, null, false, null),
			new PaintSpec.Solid(new ColorSpec.Scheme("accent2", Map.of()))), table.rows().get(2).get(1));
	}

	public void testTextForms() throws Exception {
		assertEquals(List.of(ParagraphSpec.plain("a"), ParagraphSpec.plain("b"), ParagraphSpec.plain("")),
			DescriptionReader.readParagraphs(new Value("a\r\nb\n", "text")));
		assertEquals(List.of(
			ParagraphSpec.plain("x"),
			ParagraphSpec.plain("y"),
			ParagraphSpec.of(List.of(new RunSpec("r", new RunStyle(null, null, true, null, null, null, null, null),
				"https://top-logic.com"))),
			ParagraphSpec.of(List.of(RunSpec.plain("p1"), RunSpec.plain("p2"))),
			ParagraphSpec.plain("1.5")),
			DescriptionReader.readParagraphs(new Value(List.of(
				"x\ny",
				map(TEXT, "r", UNDERLINE, true, HYPERLINK, "https://top-logic.com"),
				map(RUNS, List.of("p1", map(TEXT, "p2"))),
				1.5), "text")));
		assertEquals(List.of(ParagraphSpec.of(List.of(RunSpec.plain("one run")))),
			DescriptionReader.readParagraphs(new Value(map(RUNS, "one run"), "text")));
	}

	public void testColorForms() throws Exception {
		assertEquals(new ColorSpec.Scheme("dk2", Map.of()), color("dk2"));
		assertEquals(new ColorSpec.Rgb("ABCDEF", Map.of()), color("#abcdef"));
		assertEquals(new ColorSpec.Rgb("ABCDEF", Map.of()), color("abcdef"));
		assertEquals(new ColorSpec.Role("themaA", Map.of()), color("themaA"));
		assertEquals(new ColorSpec.Scheme("accent1", Map.of(ColorInfo.LUM_MOD, 0.6, ColorInfo.LUM_OFF, 0.4)),
			color(map(SCHEME, "accent1", ColorInfo.LUM_MOD, 0.6, ColorInfo.LUM_OFF, 0.4)));
		assertEquals(new ColorSpec.Role("risiko", Map.of(ColorInfo.ALPHA, 0.5)),
			color(map(ROLE, "risiko", ColorInfo.ALPHA, 0.5)));
		// A color of the template analysis keeps the theme reference, the resolved value is ignored.
		assertEquals(new ColorSpec.Scheme("bg1", Map.of(ColorInfo.LUM_MOD, 0.95)),
			color(map(RGB, "F2F2F2", SCHEME, "bg1", ColorInfo.LUM_MOD, 0.95)));
		assertEquals(new ColorSpec.Rgb("FF0000", Map.of()), color(map(RGB, "FF0000")));
	}

	public void testAnalysisStyleAsInput() throws Exception {
		Map<?, ?> analysis = (Map<?, ?>) eval("t -> pptAnalyze($t)", template());
		Map<?, ?> slide = (Map<?, ?>) ((List<?>) analysis.get(SLIDES)).get(1);
		Map<?, ?> card = findShape((List<?>) slide.get(SHAPES), "Abgerundetes Rechteck 89");
		assertNotNull(card);

		// Copy the style of the card from the analysis into a shape description.
		Map<String, Object> shape = map(TYPE, TYPE_SHAPE, TEXT, "Copy");
		for (String key : List.of(X, Y, WIDTH, HEIGHT, GEOMETRY, FILL, LINE, SHADOW)) {
			shape.put(key, card.get(key));
		}
		ShapeSpec spec = (ShapeSpec) DescriptionReader.readElement(new Value(shape, "shape"));

		assertEquals(GeometrySpec.ROUND_RECT, spec.geometry().preset());
		assertTrue(spec.geometry().cornerRadius() > 0);
		PaintSpec.Gradient fill = (PaintSpec.Gradient) spec.fill();
		assertEquals("bg1", ((ColorSpec.Scheme) fill.stops().get(0).color()).name());
		assertEquals(1.0, fill.stops().get(1).position());
		assertTrue(spec.line().paint() instanceof PaintSpec.Gradient);
		assertEquals(1.0, spec.line().width());
		assertEquals(new ShadowSpec(
			new ColorSpec.Scheme("accent1", Map.of(ColorInfo.LUM_MOD, 0.75, ColorInfo.ALPHA, 0.2)), 16, 8, 90),
			spec.shadow());
		assertTrue(spec.bounds().isComplete());
	}

	public void testErrorUnknownType() throws Exception {
		Object slides = List.of(map(CONTENT, List.of(map(TYPE, TYPE_SHAPE), map(TYPE, "chart"))));
		TopLogicException error = expectError(() -> DescriptionReader.readSlides(slides));
		assertEquals("slides[0].content[1]", argument(error, I18NConstants.ERROR_UNKNOWN_TYPE__PATH_TYPE_TYPES, 0));
		assertEquals("chart", argument(error, I18NConstants.ERROR_UNKNOWN_TYPE__PATH_TYPE_TYPES, 1));
		String types = (String) argument(error, I18NConstants.ERROR_UNKNOWN_TYPE__PATH_TYPE_TYPES, 2);
		for (String type : List.of(TYPE_SHAPE, TYPE_TEXT_BOX, TYPE_TABLE, TYPE_IMAGE)) {
			assertTrue(types, types.contains(type));
		}
	}

	public void testErrorUnknownKey() throws Exception {
		Object slides = eval("[pptSlide(title: 'A'), pptSlide(content: [pptShape(x: 1)])]");
		addEntry(slides, 1, "fil", "accent1");
		TopLogicException error = expectError(() -> DescriptionReader.readSlides(slides));
		assertEquals("slides[1].content[0].fil", argument(error, I18NConstants.ERROR_UNKNOWN_ENTRY__PATH_KEYS, 0));
		assertTrue(((String) argument(error, I18NConstants.ERROR_UNKNOWN_ENTRY__PATH_KEYS, 1)).contains(FILL));

		TopLogicException slideError = expectError(() -> DescriptionReader.readSlides(map(TITEL, "x")));
		assertEquals("slides.titel", argument(slideError, I18NConstants.ERROR_UNKNOWN_ENTRY__PATH_KEYS, 0));
	}

	public void testErrorWrongValueType() throws Exception {
		Object slides = List.of(map(CONTENT, List.of(map(TYPE, TYPE_SHAPE, X, "left"))));
		TopLogicException error = expectError(() -> DescriptionReader.readSlides(slides));
		assertEquals("slides[0].content[0].x", argument(error, I18NConstants.ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED, 0));
		assertEquals(I18NConstants.EXPECTED_NUMBER,
			argument(error, I18NConstants.ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED, 2));

		TopLogicException listError =
			expectError(() -> DescriptionReader.readSlides(map(CONTENT, map(TYPE, TYPE_TABLE, ROWS, "no rows"))));
		assertEquals("slides.content.rows",
			argument(listError, I18NConstants.ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED, 0));

		TopLogicException missing = expectError(() -> DescriptionReader.readSlides(map(CONTENT, map(TYPE, TYPE_IMAGE))));
		assertEquals("slides.content.data", argument(missing, I18NConstants.ERROR_MISSING_ENTRY__PATH_EXPECTED, 0));
	}

	public void testErrorBadColor() throws Exception {
		Object cell = map(TEXT, "x", FILL, map(TYPE, "solid", COLOR, "#12"));
		Object slides = List.of(map(), map(CONTENT, List.of(
			map(TYPE, TYPE_TABLE, ROWS, List.of(List.of("a", "b"), List.of("c", cell))))));
		TopLogicException error = expectError(() -> DescriptionReader.readSlides(slides));
		assertEquals("slides[1].content[0].rows[1][1].fill.color",
			argument(error, I18NConstants.ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED, 0));
		assertEquals(I18NConstants.EXPECTED_COLOR,
			argument(error, I18NConstants.ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED, 2));

		TopLogicException modifierError = expectError(
			() -> DescriptionReader.readColor(new Value(map(SCHEME, "accent1", ColorInfo.ALPHA, 2.0), "c")));
		assertEquals("c.alpha", argument(modifierError, I18NConstants.ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED, 0));

		TopLogicException schemeError =
			expectError(() -> DescriptionReader.readColor(new Value(map(SCHEME, "accent9"), "c")));
		assertEquals("c.scheme", argument(schemeError, I18NConstants.ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED, 0));
	}

	public void testErrorNegativeSize() throws Exception {
		Object slides = eval("pptSlide(content: [pptTextBox('a'), pptTextBox('b', width: -5)])");
		TopLogicException error = expectError(() -> DescriptionReader.readSlides(slides));
		assertEquals("slides.content[1].width",
			argument(error, I18NConstants.ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED, 0));
		assertEquals(I18NConstants.EXPECTED_NON_NEGATIVE_NUMBER,
			argument(error, I18NConstants.ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED, 2));
	}

	public void testErrorRowLength() throws Exception {
		Object table = map(TYPE, TYPE_TABLE, COLUMN_WIDTHS, List.of(100, 50), ROWS, List.of(List.of("a", "b", "c")));
		TopLogicException error = expectError(() -> DescriptionReader.readElement(new Value(table, "t")));
		assertEquals("t.rows[0]", argument(error, I18NConstants.ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED, 0));
	}

	public void testLayoutForms() throws Exception {
		assertEquals(new LayoutRef.Index(11), DescriptionReader.readLayout(new Value(11, "layout")));
		assertEquals(new LayoutRef.Index(11), DescriptionReader.readLayout(new Value(11.0, "layout")));
		assertEquals(new LayoutRef.Role(LayoutRole.TITLE), DescriptionReader.readLayout(new Value("title", "layout")));
		assertEquals(new LayoutRef.Name("3_Grid"), DescriptionReader.readLayout(new Value("3_Grid", "layout")));
		expectError(() -> DescriptionReader.readLayout(new Value(1.5, "layout")));
	}

	private static final String TITEL = "titel";

	private static Map<?, ?> findShape(List<?> shapes, String name) {
		for (Object shape : shapes) {
			if (name.equals(((Map<?, ?>) shape).get(NAME))) {
				return (Map<?, ?>) shape;
			}
			Object children = ((Map<?, ?>) shape).get(CHILDREN);
			if (children != null) {
				Map<?, ?> result = findShape((List<?>) children, name);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	private static ColorSpec color(Object data) {
		return DescriptionReader.readColor(new Value(data, "color"));
	}

	@SuppressWarnings("unchecked")
	private static void addEntry(Object slides, int slide, String key, Object value) {
		Map<String, Object> slideData = (Map<String, Object>) ((List<?>) slides).get(slide);
		Map<String, Object> element = (Map<String, Object>) ((List<?>) slideData.get(CONTENT)).get(0);
		element.put(key, value);
	}

	private BinaryData template() {
		return BinaryDataFactory.createBinaryData(TestDescription.class, TEMPLATE);
	}

	/**
	 * The argument of the error message, which must have been created from the given key.
	 */
	private static Object argument(TopLogicException error, ResKeyTemplate expectedKey, int index) {
		ResKey key = error.getErrorKey();
		assertEquals(expectedKey.getKey(), key.getKey());
		return key.arguments()[index];
	}

	private interface Action {
		Object run() throws Exception;
	}

	private static TopLogicException expectError(Action action) throws Exception {
		try {
			action.run();
		} catch (RuntimeException ex) {
			// The error of the description is the innermost one, a script evaluation may wrap it.
			TopLogicException result = null;
			for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
				if (cause instanceof TopLogicException error) {
					result = error;
				}
			}
			if (result == null) {
				throw ex;
			}
			return result;
		}
		fail("Error expected.");
		return null;
	}

	private static Map<String, Object> map(Object... keysAndValues) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (int n = 0; n < keysAndValues.length; n += 2) {
			result.put((String) keysAndValues[n], keysAndValues[n + 1]);
		}
		return result;
	}

	public static Test suite() {
		return suite(TestDescription.class);
	}

}
