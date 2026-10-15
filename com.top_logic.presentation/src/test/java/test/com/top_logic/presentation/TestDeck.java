/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.presentation;

import static com.top_logic.presentation.data.PresentationData.*;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import junit.framework.Test;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFNotes;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFRelation;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyTemplate;
import com.top_logic.presentation.export.DeckWriter;
import com.top_logic.presentation.export.PackagePruner;
import com.top_logic.presentation.render.SlideRenderer;
import com.top_logic.presentation.template.LayoutInfo;
import com.top_logic.presentation.template.PlaceholderInfo;
import com.top_logic.presentation.template.TemplateAnalysis;
import com.top_logic.util.error.TopLogicException;

/**
 * Test case for the creation of presentations with <code>pptDeck()</code> and the template
 * profile reported by <code>pptProfile()</code>.
 *
 * <p>
 * If the system property {@link #PNG_DIR_PROPERTY} names a directory, the slides of the created
 * test presentation are rendered into PNG images there.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestDeck extends AbstractSearchExpressionTest {

	/**
	 * System property naming a directory for PNG images of the created test presentation.
	 */
	public static final String PNG_DIR_PROPERTY = "tl.presentation.pngDir";

	private static final String TEMPLATE = "toplogic-template.pptx";

	private static final int TITLE_LAYOUT = 0;

	private static final int GRID_LAYOUT = 10;

	private static final int CONTENT_LAYOUT = 11;

	private static final long FOOTER_IDX = 4;

	private static final long SUBTITLE_IDX = 13;

	private static final long CONTENT_IDX = 21;

	private static final String FOOTER_TEXT = "TopLogic – vertraulich";

	private static final double DELTA = 0.01;

	private static final String DECK = """
		t -> img -> pptDeck($t, [
			pptSlide(layout: "title", title: "Quartalsbericht", subtitle: "Q3 2026"),
			pptSlide(title: "Ergebnisse", subtitle: "Alles im Plan", notes: ["Zeile 1", "Zeile 2"], content: [
				pptShape(x: 40, y: 140, width: 260, height: 150, cornerRadius: 16,
					fill: pptGradient(["accent1", pptColor("accent2", lumMod: 0.75)], angle: 90),
					line: "none", shadow: true, text: "Abgerundet", size: 20, bold: true, color: "bg1"),
				pptTable([["Region", "Umsatz"], ["Nord", 12], ["Süd", pptCell("7", fill: "accent2", align: "right")]],
					x: 330, y: 140, width: 360, columnWeights: [2, 1], headerRows: 1),
				pptImage($img, x: 710, y: 140, width: 220, height: 220),
				pptTextBox([pptParagraph(["Mehr unter ", pptRun("top-logic.com", hyperlink: "https://top-logic.com")],
					bullet: true), "Zweite Zeile"], x: 40, y: 410, width: 600, height: 50)
			])
		], footer: "TopLogic – vertraulich", name: "bericht.pptx")
		""";

	public void testDeck() throws Exception {
		BinaryData template = template();
		BinaryData deck = (BinaryData) evalOnce(DECK, template, image(400, 200));

		assertEquals(DeckWriter.PPTX_CONTENT_TYPE, deck.getContentType());
		assertEquals("bericht.pptx", deck.getName());

		TemplateAnalysis analysis = analyze(template);
		try (XMLSlideShow result = open(deck)) {
			List<XSLFSlide> slides = result.getSlides();
			assertEquals("Sample slides of the template must be removed.", 2, slides.size());

			XSLFSlide title = slides.get(0);
			assertEquals(analysis.layouts().get(TITLE_LAYOUT).name(), title.getSlideLayout().getName());
			assertEquals("Only filled placeholders are created.", 2, title.getShapes().size());
			assertEquals("Quartalsbericht", placeholderText(title, "ctrTitle", 0));
			assertEquals("Q3 2026", placeholderText(title, "subTitle", 1));

			XSLFSlide content = slides.get(1);
			assertEquals(analysis.layouts().get(CONTENT_LAYOUT).name(), content.getSlideLayout().getName());
			assertEquals("Ergebnisse", placeholderText(content, "title", 0));
			assertEquals("Alles im Plan", placeholderText(content, "body", SUBTITLE_IDX));
			assertEquals(FOOTER_TEXT, placeholderText(content, "body", FOOTER_IDX));
			assertNull("No placeholder for the unused content area.", placeholder(content, CONTENT_IDX));

			// Placeholders inherit their position from the layout.
			PlaceholderInfo layoutSubtitle = analysis.layouts().get(CONTENT_LAYOUT).placeholder("body", SUBTITLE_IDX);
			assertEquals(layoutSubtitle.bounds().getY(), placeholder(content, SUBTITLE_IDX).getAnchor().getY(), DELTA);

			XSLFNotes notes = content.getNotes();
			assertNotNull(notes);
			assertEquals("Zeile 1\nZeile 2", notesText(notes));
			assertNull(title.getNotes());

			XSLFAutoShape shape = (XSLFAutoShape) content.getShapes().stream()
				.filter(s -> s instanceof XSLFAutoShape && PlaceholderInfo.typeOf(s) == null)
				.findFirst().get();
			String shapeXml = shape.getXmlObject().xmlText();
			assertTrue(shapeXml, shapeXml.contains("prst=\"roundRect\""));
			assertTrue("Corner radius 16pt of a 150pt high shape.", shapeXml.contains("fmla=\"val 10667\""));
			assertTrue(shapeXml, shapeXml.contains("<a:gradFill"));
			assertTrue(shapeXml, shapeXml.contains("<a:schemeClr val=\"accent2\"><a:lumMod val=\"75000\"/>"));
			assertTrue(shapeXml, shapeXml.contains("<a:outerShdw"));
			assertBounds(new Rectangle2D.Double(40, 140, 260, 150), shape.getAnchor());
			assertEquals("Abgerundet", shape.getText());

			XSLFTable table = findShape(content, XSLFTable.class);
			assertEquals(3, table.getNumberOfRows());
			assertEquals(2, table.getNumberOfColumns());
			assertEquals(240, table.getColumnWidth(0), DELTA);
			assertEquals(120, table.getColumnWidth(1), DELTA);
			assertTrue(table.getCell(0, 0).getTextParagraphs().get(0).getTextRuns().get(0).isBold());
			assertTrue("Rows grow with their text.", table.getRowHeight(0) > 10);

			XSLFPictureShape picture = findShape(content, XSLFPictureShape.class);
			Rectangle2D anchor = picture.getAnchor();
			assertEquals("Contained image keeps its aspect ratio.", 2.0, anchor.getWidth() / anchor.getHeight(), DELTA);
			assertEquals(220, anchor.getWidth(), DELTA);
			assertEquals(250, anchor.getCenterY(), DELTA);

			SlideRenderer renderer = new SlideRenderer();
			List<BufferedImage> images = new ArrayList<>();
			for (XSLFSlide slide : slides) {
				images.add(renderer.render(slide, 1280));
			}
			writeImages(images, "deck");
		}

		assertTrue("Result must be smaller than the template.", deck.getSize() < size(template));
		try (OPCPackage pkg = OPCPackage.open(deck.getStream())) {
			Set<PackagePartName> reachable = PackagePruner.reachableParts(pkg);
			Set<String> names = new TreeSet<>();
			for (PackagePart part : pkg.getParts()) {
				String name = part.getPartName().getName();
				names.add(name);
				if (!part.isRelationshipPart()) {
					assertTrue("Unreachable part: " + name, reachable.contains(part.getPartName()));
				}
			}
			assertEquals(XSLFRelation.MAIN.getContentType(),
				pkg.getPart(pkg.getRelationshipsByType(
					"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument").getRelationship(0))
					.getContentType());
			for (String name : names) {
				assertFalse("Part of a removed sample slide: " + name,
					name.startsWith("/ppt/charts/") || name.startsWith("/ppt/embeddings/"));
			}
			// The background images of the layouts stay.
			assertTrue(names.toString(), names.contains("/ppt/media/image1.png"));
			assertTrue(names.toString(), names.contains("/ppt/media/image4.png"));
			// The icons of the sample slides are removed.
			assertFalse(names.toString(), names.contains("/ppt/media/image43.svg"));
			pkg.revert();
		}
	}

	public void testProfileOfTemplate() throws Exception {
		BinaryData template = template();
		Map<?, ?> profile = (Map<?, ?>) eval("t -> pptProfile($t)", template);

		Map<?, ?> layouts = (Map<?, ?>) profile.get(LAYOUTS);
		assertEquals(0.0, layouts.get("title"));
		assertEquals(11.0, layouts.get("content"));
		assertEquals(0.0, layouts.get("closing"));
		assertNull("The template has no section layout.", layouts.get("section"));
		assertEquals(Map.of(SCHEME, "accent1"), ((Map<?, ?>) profile.get(ROLES)).get(ROLE_TOPIC_A));

		Map<?, ?> slideLayouts = (Map<?, ?>) profile.get(SLIDE_LAYOUTS);
		Map<?, ?> content = (Map<?, ?>) slideLayouts.get("11");
		assertEquals((double) SUBTITLE_IDX, content.get(SUBTITLE));
		assertEquals((double) FOOTER_IDX, content.get(FOOTER));
		PlaceholderInfo body = analyze(template).layouts().get(CONTENT_LAYOUT).placeholder("body", CONTENT_IDX);
		assertEquals(bounds(body.bounds()), content.get(CONTENT_AREA));
		assertEquals(Map.of(X, 28.35, Y, 127.64, WIDTH, 907.09, HEIGHT, 369.76), content.get(CONTENT_AREA));

		Map<?, ?> title = (Map<?, ?>) slideLayouts.get("0");
		assertEquals(1.0, title.get(SUBTITLE));
		assertNull(title.get(FOOTER));
	}

	public void testProfileOfDefaultTemplate() throws Exception {
		BinaryData template;
		try (XMLSlideShow slideShow = new XMLSlideShow()) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			slideShow.write(buffer);
			template = BinaryDataFactory.createBinaryData(buffer.toByteArray(), DeckWriter.PPTX_CONTENT_TYPE, "poi.pptx");
		}
		Map<?, ?> profile = (Map<?, ?>) eval("t -> pptProfile($t)", template);
		Map<?, ?> layouts = (Map<?, ?>) profile.get(LAYOUTS);
		assertEquals(0.0, layouts.get("title"));
		assertEquals(1.0, layouts.get("content"));
		assertEquals(2.0, layouts.get("section"));
		assertEquals(0.0, layouts.get("closing"));

		LayoutInfo contentLayout = analyze(template).layouts().get(1);
		Map<?, ?> content = (Map<?, ?>) ((Map<?, ?>) profile.get(SLIDE_LAYOUTS)).get("1");
		assertNull("The content placeholder is no subtitle.", content.get(SUBTITLE));
		assertEquals((double) contentLayout.placeholder("ftr").idx(), content.get(FOOTER));
		assertEquals(bounds(contentLayout.placeholder("body", 1).bounds()), content.get(CONTENT_AREA));
		assertEquals(1.0, ((Map<?, ?>) ((Map<?, ?>) profile.get(SLIDE_LAYOUTS)).get("0")).get(SUBTITLE));

		BinaryData deck = (BinaryData) evalOnce("""
			t -> pptDeck($t, [pptSlide(title: "Hallo", content: pptTextBox("Inhalt"))], footer: "Fuß")
			""", template);
		try (XMLSlideShow result = open(deck)) {
			XSLFSlide slide = result.getSlides().get(0);
			assertEquals("Title and Content", slide.getSlideLayout().getName());
			assertEquals("Fuß", placeholderText(slide, "ftr", 11));
			XSLFTextShape box = findShape(slide, XSLFTextShape.class, s -> PlaceholderInfo.typeOf(s) == null);
			assertBounds(contentLayout.placeholder("body", 1).bounds(), box.getAnchor());
		}
	}

	public void testProfileOverride() throws Exception {
		BinaryData template = template();
		BinaryData deck = (BinaryData) evalOnce("""
			t -> pptDeck($t, pptSlide(title: "Raster", subtitle: "Sub", content: pptShape(fill: "accent1")),
				profile: {"layouts": {"content": 10}})
			""", template);
		LayoutInfo grid = analyze(template).layouts().get(GRID_LAYOUT);
		try (XMLSlideShow result = open(deck)) {
			XSLFSlide slide = result.getSlides().get(0);
			assertEquals(grid.name(), slide.getSlideLayout().getName());

			// Without a content placeholder, the content fills the area between subtitle and footer.
			Rectangle2D title = grid.placeholder("title").bounds();
			Rectangle2D subtitle = grid.placeholder("body", SUBTITLE_IDX).bounds();
			Rectangle2D footer = grid.placeholder("body", FOOTER_IDX).bounds();
			XSLFAutoShape shape = findShape(slide, XSLFAutoShape.class, s -> PlaceholderInfo.typeOf(s) == null);
			Rectangle2D anchor = shape.getAnchor();
			assertEquals(title.getX(), anchor.getX(), DELTA);
			assertEquals(subtitle.getMaxY(), anchor.getY(), DELTA);
			assertEquals(960 - 2 * title.getX(), anchor.getWidth(), DELTA);
			assertEquals(footer.getY(), anchor.getMaxY(), DELTA);
		}
	}

	public void testNeutralShape() throws Exception {
		BinaryData deck = (BinaryData) evalOnce("""
			t -> pptDeck($t, pptSlide(content: [
				pptShape(x: 100, y: 200, width: 300, height: 100, text: "Nur Text", size: 40),
				pptShape(x: 500, y: 200, width: 300, height: 100, line: pptLine(width: 2))]))
			""", template());
		try (XMLSlideShow result = open(deck)) {
			XSLFSlide slide = result.getSlides().get(0);
			List<XSLFAutoShape> shapes = new ArrayList<>();
			for (XSLFShape shape : slide.getShapes()) {
				if (shape instanceof XSLFAutoShape autoShape && PlaceholderInfo.typeOf(shape) == null) {
					shapes.add(autoShape);
				}
			}
			assertEquals(2, shapes.size());

			CTShape text = (CTShape) shapes.get(0).getXmlObject();
			assertFalse("No implicit shape style.", text.isSetStyle());
			assertFalse(text.getSpPr().isSetSolidFill() || text.getSpPr().isSetGradFill());
			assertFalse("No outline without description.", text.getSpPr().isSetLn());
			assertFalse("Text color is inherited.", text.xmlText().contains("solidFill"));

			CTShape outlined = (CTShape) shapes.get(1).getXmlObject();
			assertFalse(outlined.isSetStyle());
			assertTrue("Outline without color uses tx1.",
				outlined.getSpPr().getLn().xmlText().contains("<a:schemeClr val=\"tx1\"/>"));

			// The text is dark on the light content background.
			BufferedImage image = new SlideRenderer().render(slide, 960);
			int darkest = 255;
			for (int x = 100; x < 400; x++) {
				for (int y = 200; y < 300; y++) {
					Color pixel = new Color(image.getRGB(x, y));
					darkest = Math.min(darkest, (pixel.getRed() + pixel.getGreen() + pixel.getBlue()) / 3);
				}
			}
			assertTrue("Text must be readable, darkest pixel: " + darkest, darkest < 80);
		}
	}

	public void testRoleColors() throws Exception {
		BinaryData template = template();
		BinaryData deck = (BinaryData) evalOnce("""
			t -> pptDeck($t, pptSlide(content: [pptShape(x: 10, y: 10, width: 100, height: 50,
					fill: pptColor("risk", alpha: 0.5), text: "Risiko", color: "risk")]),
				profile: {"roles": {"risk": "FF5733"}})
			""", template);
		try (XMLSlideShow result = open(deck)) {
			XSLFAutoShape shape = findShape(result.getSlides().get(0), XSLFAutoShape.class, s -> true);
			String xml = shape.getXmlObject().xmlText();
			assertTrue(xml, xml.contains("<a:srgbClr val=\"FF5733\"><a:alpha val=\"50000\"/></a:srgbClr>"));
			assertTrue(xml, xml.contains("<a:rPr><a:solidFill><a:srgbClr val=\"FF5733\"/>"));
		}

		TopLogicException error = expectError(() -> evalOnce("""
			t -> pptDeck($t, pptSlide(content: [pptShape(x: 10, y: 10, width: 100, height: 50, fill: "chance")]),
				profile: {"roles": {"risk": "FF5733"}})
			""", template));
		ResKeyTemplate key = com.top_logic.presentation.export.I18NConstants.ERROR_UNKNOWN_COLOR_ROLE__PATH_ROLE_ROLES;
		assertEquals("slides[0].content[0].fill.color", argument(error, key, 0));
		assertEquals("chance", argument(error, key, 1));
		assertTrue(((String) argument(error, key, 2)).contains("risk"));
	}

	public void testErrorBadLayout() throws Exception {
		BinaryData template = template();

		TopLogicException index = expectError(() -> evalOnce("t -> pptDeck($t, [pptSlide(layout: 99)])", template));
		assertEquals("slides[0].layout", argument(index,
			com.top_logic.presentation.profile.I18NConstants.ERROR_NO_SUCH_LAYOUT_INDEX__PATH_INDEX_COUNT, 0));

		TopLogicException name =
			expectError(() -> evalOnce("t -> pptDeck($t, [pptSlide(), pptSlide(layout: 'Gibt es nicht')])", template));
		assertEquals("slides[1].layout", argument(name,
			com.top_logic.presentation.profile.I18NConstants.ERROR_NO_SUCH_LAYOUT_NAME__PATH_NAME_NAMES, 0));

		TopLogicException role = expectError(() -> evalOnce("t -> pptDeck($t, pptSlide(layout: 'section'))", template));
		assertEquals("section", argument(role,
			com.top_logic.presentation.profile.I18NConstants.ERROR_NO_LAYOUT_FOR_ROLE__PATH_ROLE_ROLES, 1));

		TopLogicException placeholder =
			expectError(() -> evalOnce("t -> pptDeck($t, pptSlide(layout: 'title', placeholders: {'99': 'x'}))", template));
		assertEquals("slides[0].placeholders.99", argument(placeholder,
			com.top_logic.presentation.export.I18NConstants.ERROR_NO_PLACEHOLDER__PATH_PLACEHOLDER_LAYOUT, 0));
	}

	public void testTemplateFile() throws Exception {
		BinaryData potx;
		try (OPCPackage pkg = OPCPackage.open(template().getStream())) {
			pkg.replaceContentType(XSLFRelation.MAIN.getContentType(),
				XSLFRelation.PRESENTATIONML_TEMPLATE.getContentType());
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			pkg.save(buffer);
			pkg.revert();
			potx = BinaryDataFactory.createBinaryData(buffer.toByteArray(), "application/octet-stream", "t.potx");
		}
		BinaryData deck = (BinaryData) evalOnce("t -> pptDeck($t, pptSlide(title: 'Aus Vorlage'))", potx);
		try (OPCPackage pkg = OPCPackage.open(deck.getStream())) {
			assertEquals(1, pkg.getPartsByContentType(XSLFRelation.MAIN.getContentType()).size());
			assertEquals(0, pkg.getPartsByContentType(XSLFRelation.PRESENTATIONML_TEMPLATE.getContentType()).size());
			pkg.revert();
		}
		try (XMLSlideShow result = open(deck)) {
			assertEquals("Aus Vorlage", placeholderText(result.getSlides().get(0), "title", 0));
		}
	}

	/**
	 * Writes the given slide images into the directory given by {@link #PNG_DIR_PROPERTY}, if
	 * set.
	 */
	static void writeImages(List<BufferedImage> images, String prefix) throws IOException {
		String dir = System.getProperty(PNG_DIR_PROPERTY);
		if (dir == null || dir.isEmpty()) {
			return;
		}
		File directory = new File(dir);
		directory.mkdirs();
		for (int n = 0; n < images.size(); n++) {
			ImageIO.write(images.get(n), "png", new File(directory, String.format("%s-slide-%02d.png", prefix, n)));
		}
	}

	static void assertBounds(Rectangle2D expected, Rectangle2D actual) {
		assertEquals(expected.getX(), actual.getX(), DELTA);
		assertEquals(expected.getY(), actual.getY(), DELTA);
		assertEquals(expected.getWidth(), actual.getWidth(), DELTA);
		assertEquals(expected.getHeight(), actual.getHeight(), DELTA);
	}

	private static Map<String, Object> bounds(Rectangle2D bounds) {
		Map<String, Object> result = map();
		putBounds(result, bounds);
		return result;
	}

	static String placeholderText(XSLFSlide slide, String type, long idx) {
		XSLFTextShape shape = placeholder(slide, idx);
		assertNotNull("No placeholder " + type + " " + idx, shape);
		assertEquals(type, PlaceholderInfo.typeOf(shape));
		assertTrue("Placeholder inherits its geometry.", ((CTShape) shape.getXmlObject()).getSpPr().getXfrm() == null);
		return shape.getText();
	}

	private static XSLFTextShape placeholder(XSLFSlide slide, long idx) {
		for (XSLFShape shape : slide.getShapes()) {
			CTPlaceholder ph = PlaceholderInfo.placeholderOf(shape);
			if (ph != null && PlaceholderInfo.idxOf(ph) == idx) {
				return (XSLFTextShape) shape;
			}
		}
		return null;
	}

	static String notesText(XSLFNotes notes) {
		for (XSLFTextShape shape : notes.getPlaceholders()) {
			if (shape.getTextType() == org.apache.poi.sl.usermodel.Placeholder.BODY) {
				return shape.getText();
			}
		}
		return null;
	}

	private static <T> T findShape(XSLFSlide slide, Class<T> type) {
		return findShape(slide, type, s -> true);
	}

	static <T> T findShape(XSLFSlide slide, Class<T> type, java.util.function.Predicate<XSLFShape> filter) {
		for (XSLFShape shape : slide.getShapes()) {
			if (type.isInstance(shape) && filter.test(shape)) {
				return type.cast(shape);
			}
		}
		fail("No " + type.getSimpleName() + " on slide.");
		return null;
	}

	static TemplateAnalysis analyze(BinaryData data) throws IOException {
		try (XMLSlideShow slideShow = open(data)) {
			return TemplateAnalysis.analyze(slideShow);
		}
	}

	static XMLSlideShow open(BinaryData data) throws IOException {
		try (InputStream in = data.getStream()) {
			return new XMLSlideShow(in);
		}
	}

	private static BinaryData image(int width, int height) throws IOException {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setPaint(new GradientPaint(0, 0, Color.ORANGE, width, height, Color.BLUE));
		graphics.fillRect(0, 0, width, height);
		graphics.setColor(Color.WHITE);
		graphics.fillOval(width / 2 - height / 4, height / 4, height / 2, height / 2);
		graphics.dispose();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ImageIO.write(image, "png", buffer);
		return BinaryDataFactory.createBinaryData(buffer.toByteArray(), "image/png", "image.png");
	}

	/**
	 * Evaluates a script creating a presentation.
	 *
	 * <p>
	 * The result is not compared with the result of the compiled script, since the files differ in
	 * their time stamps.
	 * </p>
	 */
	static Object evalOnce(String script, Object... args) throws Exception {
		return execute(search(script), args);
	}

	private static long size(BinaryData data) throws IOException {
		try (InputStream in = data.getStream()) {
			return in.readAllBytes().length;
		}
	}

	static BinaryData template() {
		return BinaryDataFactory.createBinaryData(TestDeck.class, TEMPLATE);
	}

	static Object argument(TopLogicException error, ResKeyTemplate expectedKey, int index) {
		ResKey key = error.getErrorKey();
		assertEquals(expectedKey.getKey(), key.getKey());
		return key.arguments()[index];
	}

	interface Action {
		Object run() throws Exception;
	}

	static TopLogicException expectError(Action action) throws Exception {
		try {
			action.run();
		} catch (RuntimeException ex) {
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

	public static Test suite() {
		return suite(TestDeck.class);
	}

}
