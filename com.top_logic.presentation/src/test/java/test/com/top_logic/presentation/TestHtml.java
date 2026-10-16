/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.presentation;

import static test.com.top_logic.presentation.TestArrangement.*;
import static test.com.top_logic.presentation.TestDeck.*;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.apache.poi.sl.usermodel.AutoNumberingScheme;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextAutonumberBullet;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.presentation.html.HtmlConverter;
import com.top_logic.presentation.text.TextMeasure;
import com.top_logic.util.error.TopLogicException;

/**
 * Test case for slide content created from HTML with <code>pptHtml()</code>, and for columns
 * continuing child by child on further slides.
 *
 * <p>
 * If the system property {@link TestDeck#PNG_DIR_PROPERTY} names a directory, the slides of the
 * created presentations are rendered into PNG images there.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestHtml extends AbstractSearchExpressionTest {

	/** The content area of the content layout of the test template. */
	private static final Rectangle2D AREA = new Rectangle2D.Double(28.35, 127.64, 907.09, 369.76);

	private static final double DELTA = 0.01;

	private static final String CONTENT_DECK = """
		t -> h -> pptDeck($t, pptContent("Beschreibung", null, pptHtml($h)),
			profile: {"continuationSuffix": "(Forts.)"})
		""";

	public void testFormatting() throws Exception {
		XMLSlideShow deck = open((BinaryData) evalOnce(CONTENT_DECK, template(), """
			<h1>Überschrift</h1>
			<p>Normal <b>fett</b> <i>kursiv</i> <u>unterstrichen</u>
			   <a href="https://www.top-logic.com">Link</a> und <code>x = 1</code>.</p>
			<ul>
				<li>Eins
					<ul><li>Verschachtelt</li></ul>
				</li>
				<li>Zwei</li>
			</ul>
			<ol start="3"><li>Drei</li><li>Vier</li></ol>
			<p>Zeile<br>Umbruch</p>
			<pre>a = 1
b = 2</pre>
			"""));
		assertEquals(1, deck.getSlides().size());
		XSLFTextShape box = (XSLFTextShape) contentShapes(deck, 0).get(0);
		List<XSLFTextParagraph> paragraphs = box.getTextParagraphs();
		List<String> texts = new ArrayList<>();
		for (XSLFTextParagraph paragraph : paragraphs) {
			texts.add(paragraph.getText());
		}
		assertEquals(List.of("Überschrift", "Normal fett kursiv unterstrichen Link und x = 1.", "Eins", "Verschachtelt",
			"Zwei", "Drei", "Vier", "Zeile\nUmbruch", "a = 1", "b = 2"), texts);

		XSLFTextParagraph heading = paragraphs.get(0);
		XSLFTextRun headingRun = heading.getTextRuns().get(0);
		assertTrue(headingRun.isBold());
		assertEquals(HtmlConverter.DEFAULT_SIZE * 1.6, headingRun.getFontSize().doubleValue(), DELTA);

		XSLFTextParagraph formatted = paragraphs.get(1);
		assertEquals(HtmlConverter.DEFAULT_SIZE, run(formatted, "Normal ").getFontSize().doubleValue(), DELTA);
		assertFalse(run(formatted, "Normal ").isBold());
		assertTrue(run(formatted, "fett").isBold());
		assertTrue(run(formatted, "kursiv").isItalic());
		assertTrue(run(formatted, "unterstrichen").isUnderlined());
		assertEquals("https://www.top-logic.com", run(formatted, "Link").getHyperlink().getAddress());
		assertEquals(HtmlConverter.MONOSPACE_FONT, run(formatted, "x = 1").getFontFamily());

		assertEquals("•", paragraphs.get(2).getBulletCharacter());
		assertEquals(0, paragraphs.get(2).getIndentLevel());
		assertEquals("–", paragraphs.get(3).getBulletCharacter());
		assertEquals(1, paragraphs.get(3).getIndentLevel());
		assertEquals(0, paragraphs.get(4).getIndentLevel());

		assertAutoNumber(paragraphs.get(5), AutoNumberingScheme.arabicPeriod, 3);
		assertAutoNumber(paragraphs.get(6), AutoNumberingScheme.arabicPeriod, 4);
		assertFalse(paragraphs.get(7).isBullet());
		assertEquals(HtmlConverter.MONOSPACE_FONT, paragraphs.get(8).getTextRuns().get(0).getFontFamily());

		assertFits(box);
		renderDeck(deck, "html-formatting");
	}

	public void testAutoNumberParagraph() throws Exception {
		XMLSlideShow deck = open((BinaryData) evalOnce("""
			t -> pptDeck($t, pptContent("Nummern", null, pptTextBox([
				pptParagraph("Zweites", bullet: {"autoNumber": "alphaLcPeriod", "start": 2}),
				pptParagraph("Weiter", bullet: {"autoNumber": "alphaLcPeriod"})])))
			""", template()));
		List<XSLFTextParagraph> paragraphs = ((XSLFTextShape) contentShapes(deck, 0).get(0)).getTextParagraphs();
		assertAutoNumber(paragraphs.get(0), AutoNumberingScheme.alphaLcPeriod, 2);
		CTTextAutonumberBullet continued = paragraphs.get(1).getXmlObject().getPPr().getBuAutoNum();
		assertEquals("alphaLcPeriod", continued.getType().toString());
		assertFalse("Continues the numbering.", continued.isSetStartAt());

		TopLogicException error = expectError(() -> evalOnce("""
			t -> pptDeck($t, pptContent("Nummern", null, pptTextBox(
				pptParagraph("X", bullet: {"autoNumber": "arabicPeriod", "char": "-"}))))
			""", template()));
		assertNotNull(error);
	}

	public void testTable() throws Exception {
		StringBuilder html = new StringBuilder("<p>Vorher</p><table><thead><tr><th>Nr</th><th>Name</th></tr></thead><tbody>");
		for (int n = 1; n <= 30; n++) {
			html.append("<tr><td>").append(n).append("</td><td>Eintrag <b>").append(n).append("</b></td></tr>");
		}
		html.append("<tr><td colspan=\"2\">Summe</td></tr></tbody></table>");
		XMLSlideShow deck = open((BinaryData) evalOnce(CONTENT_DECK, template(), html.toString()));
		List<XSLFSlide> slides = deck.getSlides();
		assertTrue("Table must be continued: " + slides.size(), slides.size() > 1);
		List<String> numbers = new ArrayList<>();
		for (int n = 0; n < slides.size(); n++) {
			assertEquals(n == 0 ? "Beschreibung" : "Beschreibung (Forts.)", placeholderText(slides.get(n), "title", 0));
			List<XSLFShape> shapes = contentShapes(deck, n);
			XSLFTable table = (XSLFTable) shapes.get(shapes.size() - 1);
			assertEquals("Header repeated.", "Nr", table.getCell(0, 0).getText());
			assertTrue(table.getCell(0, 0).getTextParagraphs().get(0).getTextRuns().get(0).isBold());
			assertTrue("Table must fit.", table.getAnchor().getMaxY() <= AREA.getMaxY() + DELTA);
			for (int row = 1; row < table.getNumberOfRows(); row++) {
				numbers.add(table.getCell(row, 0).getText());
			}
		}
		List<String> expected = new ArrayList<>();
		for (int n = 1; n <= 30; n++) {
			expected.add(Integer.toString(n));
		}
		expected.add("Summe");
		assertEquals(expected, numbers);
		XSLFTable last = (XSLFTable) contentShapes(deck, slides.size() - 1).get(0);
		assertEquals("Spanned cell stays empty.", "", last.getCell(last.getNumberOfRows() - 1, 1).getText());
	}

	public void testImages() throws Exception {
		BinaryData stored = image(200, 100);
		String dataUri = "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes(image(80, 40)));
		StructuredText text = new StructuredText("<p>Vorher</p><p><img src=\"ref:bild.png\"></p>"
			+ "<img src=\"" + dataUri + "\" width=\"100\"><img src=\"fehlt.png\"><p>Nachher</p>",
			Map.of("bild.png", stored));
		XMLSlideShow deck = open((BinaryData) evalOnce(CONTENT_DECK, template(), text));
		assertEquals(1, deck.getSlides().size());
		List<XSLFShape> shapes = contentShapes(deck, 0);
		assertEquals(4, shapes.size());
		assertEquals("Vorher", ((XSLFTextShape) shapes.get(0)).getText());
		Rectangle2D first = ((XSLFPictureShape) shapes.get(1)).getAnchor();
		assertEquals(150, first.getWidth(), DELTA);
		assertEquals(75, first.getHeight(), DELTA);
		Rectangle2D second = ((XSLFPictureShape) shapes.get(2)).getAnchor();
		assertEquals(75, second.getWidth(), DELTA);
		assertEquals(37.5, second.getHeight(), DELTA);
		assertEquals("Nachher", ((XSLFTextShape) shapes.get(3)).getText());
		for (int n = 1; n < shapes.size(); n++) {
			assertTrue("Order kept.", shapes.get(n - 1).getAnchor().getMaxY() <= shapes.get(n).getAnchor().getY());
		}
	}

	public void testWideImageScaledToColumn() throws Exception {
		StructuredText text = new StructuredText("<img src=\"breit.png\">", Map.of("breit.png", image(2000, 500)));
		XMLSlideShow deck = open((BinaryData) evalOnce(CONTENT_DECK, template(), text));
		Rectangle2D anchor = contentShapes(deck, 0).get(0).getAnchor();
		assertEquals(AREA.getWidth(), anchor.getWidth(), DELTA);
		assertEquals(AREA.getWidth() / 4, anchor.getHeight(), DELTA);
	}

	/**
	 * A long description with paragraphs, a table and an image continues on further slides, each
	 * showing its part within the content area and in the order of the description.
	 */
	public void testLongHtml() throws Exception {
		StringBuilder html = new StringBuilder("<h2>Ausgangslage</h2>");
		for (int n = 1; n <= 12; n++) {
			html.append("<p>Absatz ").append(n)
				.append(": Die Abnahme prüft die erzeugte Präsentation auf Vollständigkeit, korrekte Zahlen und ")
				.append("lesbare Diagramme.</p>");
		}
		html.append("<ol>");
		for (int n = 1; n <= 6; n++) {
			html.append("<li>Schritt ").append(n).append("</li>");
		}
		html.append("</ol><table><tr><th>Prüfung</th><th>Ergebnis</th></tr>");
		for (int n = 1; n <= 14; n++) {
			html.append("<tr><td>Prüfung ").append(n).append("</td><td>bestanden</td></tr>");
		}
		html.append("</table><p><img src=\"diagramm.png\" width=\"600\"></p><h2>Ergebnis</h2>");
		for (int n = 13; n <= 20; n++) {
			html.append("<p>Absatz ").append(n).append(": Die Folien sind vollständig.</p>");
		}
		StructuredText text = new StructuredText(html.toString(), Map.of("diagramm.png", image(600, 300)));
		BinaryData data = (BinaryData) evalOnce(CONTENT_DECK, template(), text);
		writeDeck(data, "html-long.pptx");
		XMLSlideShow deck = open(data);
		List<XSLFSlide> slides = deck.getSlides();
		assertTrue("Content must be continued: " + slides.size(), slides.size() >= 3);

		List<String> order = new ArrayList<>();
		int pictures = 0;
		for (int n = 0; n < slides.size(); n++) {
			assertEquals(n == 0 ? "Beschreibung" : "Beschreibung (Forts.)", placeholderText(slides.get(n), "title", 0));
			double y = AREA.getY() - DELTA;
			List<XSLFShape> shapes = contentShapes(deck, n);
			assertFalse("Slide " + n + " must not be empty.", shapes.isEmpty());
			for (XSLFShape shape : shapes) {
				Rectangle2D anchor = shape.getAnchor();
				assertTrue("Shape on slide " + n + " starts below the previous one: " + anchor, anchor.getY() >= y);
				assertTrue("Shape on slide " + n + " within the content area: " + anchor,
					anchor.getMaxY() <= AREA.getMaxY() + DELTA && anchor.getX() >= AREA.getX() - DELTA
						&& anchor.getMaxX() <= AREA.getMaxX() + DELTA);
				y = anchor.getMaxY();
				if (shape instanceof XSLFTable table) {
					for (int row = 1; row < table.getNumberOfRows(); row++) {
						order.add(table.getCell(row, 0).getText());
					}
				} else if (shape instanceof XSLFTextShape box) {
					assertFits(box);
					double height = TextMeasure.measure(box).height();
					assertTrue("Text within its box.", height <= anchor.getHeight() + DELTA);
					for (XSLFTextParagraph paragraph : box.getTextParagraphs()) {
						order.add(paragraph.getText());
					}
				} else if (shape instanceof XSLFPictureShape) {
					order.add("Bild");
					pictures++;
					assertEquals(2, anchor.getWidth() / anchor.getHeight(), DELTA);
				}
			}
		}
		assertEquals(1, pictures);
		List<String> expected = new ArrayList<>();
		expected.add("Ausgangslage");
		for (int n = 1; n <= 12; n++) {
			expected.add("Absatz " + n + ": Die Abnahme prüft die erzeugte Präsentation auf Vollständigkeit, "
				+ "korrekte Zahlen und lesbare Diagramme.");
		}
		for (int n = 1; n <= 6; n++) {
			expected.add("Schritt " + n);
		}
		for (int n = 1; n <= 14; n++) {
			expected.add("Prüfung " + n);
		}
		expected.add("Bild");
		expected.add("Ergebnis");
		for (int n = 13; n <= 20; n++) {
			expected.add("Absatz " + n + ": Die Folien sind vollständig.");
		}
		assertEquals(expected, order);

		renderDeck(deck, "html-long");
	}

	/**
	 * Numbered list items keep their numbers on a continuation slide.
	 */
	public void testNumbersContinued() throws Exception {
		StringBuilder html = new StringBuilder("<ol>");
		for (int n = 1; n <= 30; n++) {
			html.append("<li>Punkt ").append(n).append("</li>");
		}
		html.append("</ol>");
		XMLSlideShow deck = open((BinaryData) evalOnce(CONTENT_DECK, template(), html.toString()));
		assertTrue(deck.getSlides().size() > 1);
		XSLFTextShape box = (XSLFTextShape) contentShapes(deck, 1).get(0);
		XSLFTextParagraph first = box.getTextParagraphs().get(0);
		int number = Integer.parseInt(first.getText().substring("Punkt ".length()));
		assertTrue(number > 1);
		assertAutoNumber(first, AutoNumberingScheme.arabicPeriod, number);
	}

	/**
	 * In a split column, an image that does not fit moves to the next slide, where it is reduced
	 * to the height of the content area if it is higher.
	 */
	public void testSplitColumnTallImage() throws Exception {
		XMLSlideShow deck = open((BinaryData) evalOnce("""
			t -> i -> pptDeck($t, pptContent("Bild", null, pptColumn([
				pptTextBox("Text davor", size: 20, height: "auto"),
				pptImage($i, width: 400, height: "auto"),
				pptTextBox("Text danach", size: 20, height: "auto")
			], split: true)))
			""", template(), image(100, 400)));
		assertEquals(3, deck.getSlides().size());
		assertEquals(1, contentShapes(deck, 0).size());
		List<XSLFShape> second = contentShapes(deck, 1);
		assertEquals(1, second.size());
		Rectangle2D picture = second.get(0).getAnchor();
		assertEquals(AREA.getHeight(), picture.getHeight(), DELTA);
		assertEquals(AREA.getHeight() / 4, picture.getWidth(), DELTA);
		assertEquals("Text danach", ((XSLFTextShape) contentShapes(deck, 2).get(0)).getText());
	}

	public void testEmpty() throws Exception {
		for (Object html : new Object[] { null, "", "  ", new StructuredText("") }) {
			XMLSlideShow deck = open((BinaryData) evalOnce(CONTENT_DECK, template(), html));
			assertEquals(1, deck.getSlides().size());
			assertEquals(List.of(), contentShapes(deck, 0));
		}
	}

	public void testUnknownTags() throws Exception {
		XMLSlideShow deck = open((BinaryData) evalOnce(CONTENT_DECK, template(),
			"<custom-block>Hallo <x-tag>Welt</x-tag></custom-block><script>verborgen()</script>"));
		XSLFTextShape box = (XSLFTextShape) contentShapes(deck, 0).get(0);
		assertEquals("Hallo Welt", box.getText());
	}

	public void testNoHtml() throws Exception {
		TopLogicException error = expectError(() -> evalOnce("t -> pptDeck($t, pptContent('X', null, pptHtml(42)))",
			template()));
		assertEquals(com.top_logic.presentation.script.I18NConstants.ERROR_NO_HTML__VALUE.getKey(),
			error.getErrorKey().getKey());
	}

	private static XSLFTextRun run(XSLFTextParagraph paragraph, String text) {
		for (XSLFTextRun run : paragraph.getTextRuns()) {
			if (run.getRawText().equals(text)) {
				return run;
			}
		}
		fail("No run '" + text + "' in: " + paragraph.getText());
		return null;
	}

	private static void assertAutoNumber(XSLFTextParagraph paragraph, AutoNumberingScheme scheme, int number) {
		CTTextParagraphProperties pPr = paragraph.getXmlObject().getPPr();
		assertTrue(paragraph.getText(), pPr.isSetBuAutoNum());
		assertEquals(scheme.name(), pPr.getBuAutoNum().getType().toString());
		assertEquals(number, pPr.getBuAutoNum().getStartAt());
		assertEquals(scheme, paragraph.getAutoNumberingScheme());
	}

	private static byte[] bytes(BinaryData data) throws IOException {
		try (InputStream in = data.getStream()) {
			return in.readAllBytes();
		}
	}

	public static Test suite() {
		return suite(TestHtml.class);
	}

}
