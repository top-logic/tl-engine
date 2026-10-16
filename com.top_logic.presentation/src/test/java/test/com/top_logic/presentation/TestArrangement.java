/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.presentation;

import static test.com.top_logic.presentation.TestDeck.*;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.presentation.data.PresentationData;
import com.top_logic.presentation.profile.TemplateProfile;
import com.top_logic.presentation.render.SlideRenderer;
import com.top_logic.presentation.template.PlaceholderInfo;
import com.top_logic.presentation.text.TextMeasure;
import com.top_logic.presentation.text.TextMetrics;
import com.top_logic.util.error.TopLogicException;

/**
 * Test case for the arrangement of slide content in rows and columns, the fitting of texts into
 * their elements and the continuation of tables and texts on further slides.
 *
 * <p>
 * If the system property {@link TestDeck#PNG_DIR_PROPERTY} names a directory, the slides of the
 * created presentations are rendered into PNG images there.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestArrangement extends AbstractSearchExpressionTest {

	/** The content area of the content layout of the test template. */
	private static final Rectangle2D AREA = new Rectangle2D.Double(28.35, 127.64, 907.09, 369.76);

	private static final double GAP = TemplateProfile.DEFAULT_GAP;

	private static final double DELTA = 0.01;

	private static final String LONG_TEXT = "Die Präsentation beschreibt den Stand des Projekts, die erreichten Ziele "
		+ "des Quartals, die offenen Risiken und die nächsten Schritte bis zum Ende des Jahres. "
		+ "Jeder Abschnitt fasst die wichtigsten Kennzahlen zusammen und nennt die Verantwortlichen.";

	/** A ticket description of about 300 characters. */
	private static final String TICKET_DESCRIPTION = "Die Präsentation wird aus den Projektdaten erzeugt und "
		+ "muss vor der Freigabe auf Vollständigkeit, korrekte Zahlen, lesbare Diagramme und die richtige "
		+ "Firmenvorlage geprüft werden. Dabei sind auch die Sprechernotizen und die Fortsetzungsfolien für "
		+ "lange Tabellen zu kontrollieren, bevor sie an die Geschäftsleitung geht.";

	public void testRowWeightsAndWidths() throws Exception {
		List<XSLFShape> shapes = contentShapes(deck("""
			t -> pptDeck($t, pptSlide(title: "Zeile", content: pptRow([
				pptShape(fill: "accent1"), pptShape(fill: "accent2", weight: 2), pptShape(fill: "accent3", width: 200)
			], gap: 10)))
			"""), 0);
		assertEquals(3, shapes.size());
		double rest = AREA.getWidth() - 2 * 10 - 200;
		double x = AREA.getX();
		assertBounds(new Rectangle2D.Double(x, AREA.getY(), rest / 3, AREA.getHeight()), anchor(shapes, 0));
		x += rest / 3 + 10;
		assertBounds(new Rectangle2D.Double(x, AREA.getY(), rest * 2 / 3, AREA.getHeight()), anchor(shapes, 1));
		x += rest * 2 / 3 + 10;
		assertBounds(new Rectangle2D.Double(x, AREA.getY(), 200, AREA.getHeight()), anchor(shapes, 2));
	}

	public void testRowWeightsList() throws Exception {
		List<XSLFShape> shapes = contentShapes(deck("""
			t -> pptDeck($t, pptSlide(content: pptRow([pptShape(fill: "accent1"), pptShape(fill: "accent2", height: 50)],
				weights: [1, 3], align: "end", gap: 0)))
			"""), 0);
		assertBounds(new Rectangle2D.Double(AREA.getX(), AREA.getMaxY(), AREA.getWidth() / 4, 0), anchor(shapes, 0));
		assertBounds(new Rectangle2D.Double(AREA.getX() + AREA.getWidth() / 4, AREA.getMaxY() - 50,
			AREA.getWidth() * 3 / 4, 50), anchor(shapes, 1));
	}

	public void testColumnAutoHeight() throws Exception {
		List<XSLFShape> shapes = contentShapes(deck("""
			t -> pptDeck($t, pptSlide(content: pptColumn([
				pptTextBox("Ein Text mit seiner natürlichen Höhe", height: "auto", size: 24),
				pptShape(fill: "accent1"),
				pptShape(fill: "accent2", weight: 3)
			])))
			"""), 0);
		XSLFTextShape box = (XSLFTextShape) shapes.get(0);
		double textHeight = TextMeasure.measure(box).height();
		assertTrue(textHeight > 20);
		assertBounds(new Rectangle2D.Double(AREA.getX(), AREA.getY(), AREA.getWidth(), textHeight), box.getAnchor());

		double rest = AREA.getHeight() - 2 * GAP - textHeight;
		double y = AREA.getY() + textHeight + GAP;
		assertBounds(new Rectangle2D.Double(AREA.getX(), y, AREA.getWidth(), rest / 4), anchor(shapes, 1));
		y += rest / 4 + GAP;
		assertBounds(new Rectangle2D.Double(AREA.getX(), y, AREA.getWidth(), rest * 3 / 4), anchor(shapes, 2));
	}

	public void testNesting() throws Exception {
		List<XSLFShape> shapes = contentShapes(deck("""
			t -> pptDeck($t, pptSlide(content: pptColumn([
				pptRow([pptShape(fill: "accent1"), pptShape(fill: "accent2"), pptShape(fill: "accent3")], height: 200),
				pptShape(fill: "accent4", text: "Banner")
			])))
			"""), 0);
		assertEquals(4, shapes.size());
		double cardWidth = (AREA.getWidth() - 2 * GAP) / 3;
		for (int n = 0; n < 3; n++) {
			assertBounds(new Rectangle2D.Double(AREA.getX() + n * (cardWidth + GAP), AREA.getY(), cardWidth, 200),
				anchor(shapes, n));
		}
		assertBounds(new Rectangle2D.Double(AREA.getX(), AREA.getY() + 200 + GAP, AREA.getWidth(),
			AREA.getHeight() - 200 - GAP), anchor(shapes, 3));
	}

	public void testGapFromProfile() throws Exception {
		List<XSLFShape> shapes = contentShapes(deck("""
			t -> pptDeck($t, pptSlide(content: pptRow([pptShape(fill: "accent1"), pptShape(fill: "accent2")])),
				profile: {"gap": 5})
			"""), 0);
		double width = (AREA.getWidth() - 5) / 2;
		assertBounds(new Rectangle2D.Double(AREA.getX() + width + 5, AREA.getY(), width, AREA.getHeight()),
			anchor(shapes, 1));
	}

	public void testImplicitColumn() throws Exception {
		List<XSLFShape> shapes = contentShapes(deck("""
			t -> pptDeck($t, pptSlide(content: [
				pptShape(fill: "accent1", height: 100),
				pptShape(fill: "accent2", x: 500, y: 20, width: 50, height: 50),
				pptTextBox("Rest", width: 100)
			]))
			"""), 0);
		assertBounds(new Rectangle2D.Double(AREA.getX(), AREA.getY(), AREA.getWidth(), 100), anchor(shapes, 0));
		assertBounds(new Rectangle2D.Double(500, 20, 50, 50), anchor(shapes, 1));
		assertBounds(new Rectangle2D.Double(AREA.getX(), AREA.getY() + 100 + GAP, 100, AREA.getHeight() - 100 - GAP),
			anchor(shapes, 2));
	}

	public void testErrorPositionInContainer() throws Exception {
		TopLogicException error = expectError(() -> evalOnce("""
			t -> pptDeck($t, [pptSlide(), pptSlide(content: pptColumn([pptShape(), pptRow([pptShape(y: 10)])]))])
			""", template()));
		assertEquals("slides[1].content[0].children[1].children[0].y", argument(error,
			com.top_logic.presentation.model.I18NConstants.ERROR_POSITION_IN_CONTAINER__PATH, 0));

		TopLogicException weights = expectError(() -> evalOnce("""
			t -> pptDeck($t, pptSlide(content: pptRow([pptShape(), pptShape()], weights: [1])))
			""", template()));
		assertEquals("slides.content[0].weights", argument(weights,
			com.top_logic.presentation.model.I18NConstants.ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED, 0));
	}

	public void testShrink() throws Exception {
		XMLSlideShow deck = deck("t -> pptDeck($t, pptSlide(content: pptShape(x: 100, y: 150, width: 300, height: 90, "
			+ "fill: 'accent1', size: 28, text: '" + LONG_TEXT + "')))");
		XSLFTextShape shape = (XSLFTextShape) contentShapes(deck, 0).get(0);
		double size = fontSize(shape);
		assertTrue("Font must be shrunk: " + size, size < 28);
		assertTrue("Font must not be shrunk below the minimum: " + size, size >= TemplateProfile.DEFAULT_MIN_FONT_SIZE);
		assertFits(shape);
		assertEquals("The text is complete.", LONG_TEXT, shape.getText());
		assertNoAutofit(shape);
	}

	public void testShrinkToMinimumAndCut() throws Exception {
		XMLSlideShow deck = deck("t -> pptDeck($t, pptSlide(content: pptShape(x: 100, y: 150, width: 120, height: 40, "
			+ "size: 28, text: '" + LONG_TEXT + "')), profile: {'minFontSize': 12})");
		XSLFTextShape shape = (XSLFTextShape) contentShapes(deck, 0).get(0);
		assertEquals(12.0, fontSize(shape), DELTA);
		assertTrue(shape.getText(), shape.getText().endsWith("…"));
		assertTrue(LONG_TEXT.startsWith(shape.getText().substring(0, shape.getText().length() - 1)));
		assertFits(shape);
	}

	public void testEllipsis() throws Exception {
		XMLSlideShow deck = deck("t -> pptDeck($t, pptSlide(content: pptTextBox(x: 100, y: 150, width: 250, height: 90, "
			+ "size: 20, overflow: 'ellipsis', text: ['Erster Absatz', '" + LONG_TEXT + "'])))");
		XSLFTextShape box = (XSLFTextShape) contentShapes(deck, 0).get(0);
		assertEquals("Font size is kept.", 20.0, fontSize(box), DELTA);
		List<XSLFTextParagraph> paragraphs = box.getTextParagraphs();
		assertEquals(2, paragraphs.size());
		assertEquals("Erster Absatz", paragraphs.get(0).getText());
		String cut = paragraphs.get(1).getText();
		assertTrue(cut, cut.endsWith("…"));
		assertTrue(cut, cut.length() > 1);
		assertFits(box);
	}

	public void testVisible() throws Exception {
		XMLSlideShow deck = deck("t -> pptDeck($t, pptSlide(content: pptTextBox(x: 100, y: 150, width: 250, height: 30, "
			+ "size: 20, overflow: 'visible', text: '" + LONG_TEXT + "')))");
		XSLFTextShape box = (XSLFTextShape) contentShapes(deck, 0).get(0);
		assertEquals(20.0, fontSize(box), DELTA);
		assertEquals(LONG_TEXT, box.getText());
		assertTrue(TextMeasure.measure(box).height() > 30);
	}

	public void testOverflowError() throws Exception {
		TopLogicException error = expectError(() -> evalOnce("t -> pptDeck($t, pptSlide(content: ["
			+ "pptShape(x: 10, y: 10, width: 10, height: 10), pptShape(x: 100, y: 150, width: 200, height: 30, "
			+ "overflow: 'error', text: '" + LONG_TEXT + "')]))", template()));
		assertEquals("slides[0].content[1].text",
			argument(error, com.top_logic.presentation.export.I18NConstants.ERROR_TEXT_OVERFLOW__PATH, 0));
	}

	public void testCellLongWord() throws Exception {
		XMLSlideShow deck = deck("""
			t -> pptDeck($t, pptSlide(content: pptTable([["Donaudampfschifffahrtsgesellschaft", "kurz"]],
				x: 100, y: 150, columnWidths: [250, 120])))
			""");
		XSLFTable table = TestDeck.findShape(deck.getSlides().get(0), XSLFTable.class, s -> true);
		XSLFTableCell cell = table.getCell(0, 0);
		double size = fontSize(cell);
		assertTrue("Font must be shrunk: " + size, size < 18);
		assertFalse(TextMeasure.measure(cell).wordBroken());
		assertEquals("Donaudampfschifffahrtsgesellschaft", cell.getText());
		assertEquals("Other cells keep their size.", 18.0, fontSize(table.getCell(0, 1)), DELTA);
	}

	public void testTableContinuation() throws Exception {
		XMLSlideShow deck = deck("""
			t -> pptDeck($t, [
				pptSlide(title: "Liste", subtitle: "Alle Einträge", notes: "Notiz", content: [
					pptTextBox("Einleitung", height: "auto", size: 20),
					pptTable(concat([["Nr", "Name"]], count(1, 41).map(n -> [$n, toString("Eintrag ", $n)])), headerRows: 1)
				]),
				pptSlide(title: "Danach")
			], profile: {"continuationSuffix": "(Forts.)"})
			""");
		List<XSLFSlide> slides = deck.getSlides();
		int tableSlides = slides.size() - 1;
		assertTrue("Table must be continued: " + tableSlides, tableSlides > 2);
		assertEquals("Danach", placeholderText(slides.get(tableSlides), "title", 0));

		List<String> numbers = new ArrayList<>();
		for (int n = 0; n < tableSlides; n++) {
			XSLFSlide slide = slides.get(n);
			assertEquals(n == 0 ? "Liste" : "Liste (Forts.)", placeholderText(slide, "title", 0));
			assertEquals("Alle Einträge", placeholderText(slide, "body", 13));
			assertEquals("Notes only on the first slide.", n == 0, slide.getNotes() != null);

			List<XSLFShape> shapes = contentShapes(deck, n);
			assertEquals("Other content only on the first slide.", n == 0 ? 2 : 1, shapes.size());
			XSLFTable table = (XSLFTable) shapes.get(shapes.size() - 1);
			assertEquals("Header repeated.", "Nr", table.getCell(0, 0).getText());
			Rectangle2D anchor = table.getAnchor();
			assertTrue("Table must fit: " + anchor, anchor.getMaxY() <= AREA.getMaxY() + DELTA);
			if (n > 0) {
				assertEquals("Continued table fills the content area.", AREA.getY(), anchor.getY(), DELTA);
			}
			for (int row = 1; row < table.getNumberOfRows(); row++) {
				numbers.add(table.getCell(row, 0).getText());
			}
		}
		List<String> expected = new ArrayList<>();
		for (int n = 1; n <= 40; n++) {
			expected.add(Integer.toString(n));
		}
		assertEquals(expected, numbers);
	}

	/**
	 * A table with a long description in one row and short rows otherwise, as the list of open
	 * tickets of a status report: Each row is exactly as high as its cell texts, and each slide
	 * shows as many rows as fit.
	 */
	public void testTableRowsPacked() throws Exception {
		BinaryData data = (BinaryData) evalOnce("""
			t -> long -> pptDeck($t, pptSlide(title: "Offene Tickets", content: pptTable(concat(
				[["Ticket", "Beschreibung", "Meilenstein", "Fällig", "Bearbeiter", "Dringlichkeit"]
					.map(h -> pptCell($h, fill: "accent1", color: "bg1", bold: true))],
				[["T1 Kurz", "Kurz", "M1", "01.10.", "Anna", pptRating("high", "überfällig")],
				 ["T2 Lang", $long, "M2", "–", "–", pptRating("none", "ohne Termin")]],
				count(3, 13).map(n -> [toString("T", $n), "Kurz", "M3", "–", "Bert", pptRating("low", "im Plan")])),
				headerRows: 1, columnWeights: [3, 5, 2, 1.6, 2, 2])), profile: {"continuationSuffix": "(Forts.)"})
			""", template(), TICKET_DESCRIPTION);
		writeDeck(data, "table-packing.pptx");
		XMLSlideShow deck = open(data);

		List<XSLFTable> tables = new ArrayList<>();
		for (int n = 0; n < deck.getSlides().size(); n++) {
			tables.add((XSLFTable) contentShapes(deck, n).get(0));
		}
		List<String> tickets = new ArrayList<>();
		for (int n = 0; n < tables.size(); n++) {
			XSLFTable table = tables.get(n);
			double tableHeight = 0;
			for (int row = 0; row < table.getNumberOfRows(); row++) {
				double content = 0;
				for (int column = 0; column < table.getNumberOfColumns(); column++) {
					XSLFTableCell cell = table.getCell(row, column);
					cell.setAnchor(new Rectangle2D.Double(0, 0, table.getColumnWidth(column), 0));
					Insets2D insets = cell.getInsets();
					content = Math.max(content, TextMeasure.measure(cell).height() + insets.top + insets.bottom);
				}
				assertEquals("Row " + row + " of slide " + n + " must be as high as its content.", content,
					table.getRowHeight(row), DELTA);
				tableHeight += table.getRowHeight(row);
				if (row > 0) {
					tickets.add(table.getCell(row, 0).getText());
				}
			}
			assertEquals(tableHeight, table.getAnchor().getHeight(), DELTA);
			assertTrue("Table must fit: " + tableHeight, tableHeight <= AREA.getHeight() + DELTA);
			if (n + 1 < tables.size()) {
				double nextRow = tables.get(n + 1).getRowHeight(1);
				assertTrue("The first row of the next slide must not fit onto slide " + n + ": " + tableHeight + " + "
					+ nextRow, tableHeight + nextRow > AREA.getHeight() + DELTA);
			}
		}
		List<String> expected = new ArrayList<>(List.of("T1 Kurz", "T2 Lang"));
		for (int n = 3; n <= 12; n++) {
			expected.add("T" + n);
		}
		assertEquals(expected, tickets);

		renderDeck(deck, "table-packing");
	}

	/**
	 * Header cells with long words in narrow columns are shrunk until no word is broken at the
	 * exact width of the cell.
	 */
	public void testHeaderWordsNotBroken() throws Exception {
		BinaryData data = (BinaryData) evalOnce("""
			t -> pptDeck($t, pptSlide(content: pptTable([
				["Ticket", "Beschreibung", "Meilenstein", "Fällig", "Bearbeiter", "Dringlichkeit"],
				["T1", "Erste Zeile\\nZweite Zeile\\nDritte Zeile", "M1", "–", "Anna", "hoch"]],
				headerRows: 1, columnWeights: [3, 5, 2, 1.6, 2, 2])))
			""", template());
		writeDeck(data, "table-header.pptx");
		XMLSlideShow deck = open(data);
		XSLFTable table = (XSLFTable) contentShapes(deck, 0).get(0);
		boolean shrunk = false;
		for (int column = 0; column < table.getNumberOfColumns(); column++) {
			XSLFTableCell cell = table.getCell(0, column);
			double width = table.getColumnWidth(column);
			cell.setAnchor(new Rectangle2D.Double(0, 0, width, 0));
			Insets2D insets = cell.getInsets();
			TextMetrics metrics = TextMeasure.measure(cell);
			String text = cell.getText();
			assertFalse("Word broken in header '" + text + "'.", metrics.wordBroken());
			assertEquals("Single line header '" + text + "'.", 1, metrics.lines());
			assertTrue("Header '" + text + "' wider than its cell: " + metrics.width(),
				metrics.width() <= width - insets.left - insets.right + DELTA);
			shrunk |= fontSize(cell) < 18;
		}
		assertTrue("Long header words must be shrunk.", shrunk);

		renderDeck(deck, "table-header");
	}

	static void renderDeck(XMLSlideShow deck, String prefix) throws Exception {
		SlideRenderer renderer = new SlideRenderer();
		List<BufferedImage> images = new ArrayList<>();
		for (XSLFSlide slide : deck.getSlides()) {
			images.add(renderer.render(slide, 1280));
		}
		writeImages(images, prefix);
	}

	public void testDefaultContinuationSuffix() throws Exception {
		Map<?, ?> profile = (Map<?, ?>) eval("t -> pptProfile($t)", template());
		assertEquals(TemplateProfile.defaultContinuationSuffix(), profile.get(PresentationData.CONTINUATION_SUFFIX));
		assertEquals(GAP, profile.get(PresentationData.GAP));
		assertEquals(TemplateProfile.DEFAULT_MIN_FONT_SIZE, profile.get(PresentationData.MIN_FONT_SIZE));
	}

	public void testSplitText() throws Exception {
		XMLSlideShow deck = deck("""
			t -> pptDeck($t, pptSlide(title: "Punkte", content: pptTextBox(
				count(1, 31).map(n -> pptParagraph(toString("Punkt ", $n), bullet: true)), size: 20, split: true)))
			""");
		List<XSLFSlide> slides = deck.getSlides();
		assertTrue("Text must be continued: " + slides.size(), slides.size() > 1);
		List<String> paragraphs = new ArrayList<>();
		for (int n = 0; n < slides.size(); n++) {
			XSLFTextShape box = (XSLFTextShape) contentShapes(deck, n).get(0);
			assertEquals("Paragraphs keep their size.", 20.0, fontSize(box), DELTA);
			assertFits(box);
			for (XSLFTextParagraph paragraph : box.getTextParagraphs()) {
				paragraphs.add(paragraph.getText());
			}
		}
		List<String> expected = new ArrayList<>();
		for (int n = 1; n <= 30; n++) {
			expected.add("Punkt " + n);
		}
		assertEquals(expected, paragraphs);
	}

	public void testErrorSeveralContinued() throws Exception {
		TopLogicException error = expectError(() -> evalOnce("""
			t -> pptDeck($t, pptSlide(content: pptRow([
				pptTable(concat([["Nr"]], count(1, 41).map(n -> [$n])), headerRows: 1),
				pptTable(concat([["Nr"]], count(1, 41).map(n -> [$n])), headerRows: 1)])))
			""", template()));
		assertEquals("slides[0].content[0].children[1]", argument(error,
			com.top_logic.presentation.export.I18NConstants.ERROR_SEVERAL_CONTINUED__PATH_OTHER, 0));
		assertEquals("slides[0].content[0].children[0]", argument(error,
			com.top_logic.presentation.export.I18NConstants.ERROR_SEVERAL_CONTINUED__PATH_OTHER, 1));
	}

	/**
	 * Creates a presentation showing the arrangement, fitting and continuation features and
	 * renders its slides.
	 */
	public void testShowcase() throws Exception {
		XMLSlideShow deck = deck("""
			t -> pptDeck($t, [
				pptSlide(title: "Karten und Banner", subtitle: "Zeile in Spalte", content: pptColumn([
					pptRow([
						pptShape(geometry: "roundRect", fill: "accent1", color: "bg1", size: 24, text: "Erste Karte"),
						pptShape(geometry: "roundRect", fill: "accent2", color: "bg1", size: 24, text: "Zweite Karte"),
						pptShape(geometry: "roundRect", fill: "accent3", color: "bg1", size: 24, text: "Dritte Karte")
					]),
					pptShape(fill: pptColor("accent1", lumMod: 0.5), color: "bg1", size: 20, height: 70, text: "Banner über die ganze Breite")
				])),
				pptSlide(title: "Langer Text", content: pptRow([
					pptShape(fill: "accent1", color: "bg1", size: 32, text: $long),
					pptTextBox($long, size: 32, overflow: "ellipsis"),
					pptTextBox($long, size: 14, height: "auto")
				], align: "start", weights: [1, 1, 1])),
				pptSlide(title: "Tabelle", notes: "Nur auf der ersten Folie", content: [
					pptTextBox("Die Tabelle wird auf weiteren Folien fortgesetzt.", height: "auto", size: 18),
					pptTable(concat([["Nr", "Name", "Beschreibung"]],
						count(1, 31).map(n -> [$n, toString("Eintrag ", $n), toString("Beschreibung des Eintrags ", $n)])),
						headerRows: 1, columnWeights: [1, 3, 6])
				]),
				pptSlide(title: "Aufzählung", content: pptTextBox(
					count(1, 21).map(n -> pptParagraph(toString("Punkt ", $n, " der fortgesetzten Aufzählung"), bullet: true)),
					size: 24, split: true))
			], profile: {"continuationSuffix": "(Forts.)"})
			""".replace("$long", "'" + LONG_TEXT + "'"));
		List<XSLFSlide> slides = deck.getSlides();
		assertTrue(slides.size() >= 6);

		SlideRenderer renderer = new SlideRenderer();
		List<BufferedImage> images = new ArrayList<>();
		for (XSLFSlide slide : slides) {
			images.add(renderer.render(slide, 1280));
		}
		writeImages(images, "arrangement");
	}

	private static XMLSlideShow deck(String script) throws Exception {
		return open((BinaryData) evalOnce(script, template()));
	}

	/**
	 * The shapes of the slide with the given index that are no placeholders, in their order.
	 */
	static List<XSLFShape> contentShapes(XMLSlideShow deck, int slide) {
		List<XSLFShape> result = new ArrayList<>();
		for (XSLFShape shape : deck.getSlides().get(slide).getShapes()) {
			if (PlaceholderInfo.typeOf(shape) == null) {
				result.add(shape);
			}
		}
		return result;
	}

	private static Rectangle2D anchor(List<XSLFShape> shapes, int index) {
		return shapes.get(index).getAnchor();
	}

	/**
	 * The font size of the first text run, all runs of the shape having the same size.
	 */
	private static double fontSize(XSLFTextShape shape) {
		Double result = null;
		for (XSLFTextParagraph paragraph : shape.getTextParagraphs()) {
			for (XSLFTextRun run : paragraph.getTextRuns()) {
				Double size = run.getFontSize();
				double value = size == null ? paragraph.getDefaultFontSize().doubleValue() : size.doubleValue();
				if (result == null) {
					result = Double.valueOf(value);
				} else {
					assertEquals("All runs have the same size.", result.doubleValue(), value, DELTA);
				}
			}
		}
		return result.doubleValue();
	}

	static void assertFits(XSLFTextShape shape) {
		TextMetrics metrics = TextMeasure.measure(shape);
		Insets2D insets = shape.getInsets();
		double available = shape.getAnchor().getHeight() - insets.top - insets.bottom;
		assertTrue("Text must fit: " + metrics.height() + " > " + available, metrics.height() <= available + DELTA);
		assertFalse("No word may be broken.", metrics.wordBroken());
	}

	private static void assertNoAutofit(XSLFTextShape shape) {
		String xml = shape.getXmlObject().xmlText();
		assertFalse(xml, xml.contains("normAutofit"));
	}

	public static Test suite() {
		return suite(TestArrangement.class);
	}

}
