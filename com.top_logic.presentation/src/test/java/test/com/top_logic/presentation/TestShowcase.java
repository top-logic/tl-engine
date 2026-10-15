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
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.presentation.render.SlideRenderer;
import com.top_logic.presentation.template.PlaceholderInfo;

/**
 * Showcase of the building blocks: a presentation with one slide for each kind of building block,
 * created on the TopLogic corporate template with a profile adjusted to the corporate design.
 *
 * <p>
 * The script {@link #SHOWCASE} serves as an example of a complete presentation. If the system
 * property {@link TestDeck#PNG_DIR_PROPERTY} names a directory, the slides are rendered into PNG
 * images there. If moreover the system property {@link #REFERENCE_PROPERTY} names a presentation
 * file, its slides are rendered there as well, for a comparison.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestShowcase extends AbstractSearchExpressionTest {

	/**
	 * Name of the system property naming a presentation to render for comparison.
	 */
	public static final String REFERENCE_PROPERTY = "tl.presentation.reference";

	/**
	 * Index of the agenda layout with five entries in a single column.
	 */
	private static final int AGENDA_5_LAYOUT = 5;

	/**
	 * The presentation: a function of the template and the style of the cards.
	 */
	private static final String SHOWCASE = """
		template -> card -> pptDeck($template, [
			pptTitle("Bausteine im Überblick", "Eine Beispielpräsentation",
				notes: "Titelfolie: Titel und eine Untertitelzeile auf dem dunklen Hintergrund der Vorlage."),

			pptAgenda(["Karten und Kernaussagen", "Ablauf und Abwägung", "Tabelle mit Bewertung",
					"Kennzahlen", "Zeitplan"],
				notes: "Agenda: Das Layout mit der passenden Zahl von Einträgen wird gewählt."),

			pptContent("Karten und Kernaussagen", "Drei Karten mit Ziffern, darunter eine Kernaussage", [
				pptRow([
					pptColumn([pptBadge(1, role: "themaA"),
						pptCard("Erstens", "Eine Karte trägt eine Überschrift und kurzen Text.", role: "themaA")],
						gap: 8),
					pptColumn([pptBadge(2, role: "themaB"),
						pptCard("Zweitens", "Die Farbe des Rahmens ordnet die Karte einem Thema zu.", role: "themaB")],
						gap: 8),
					pptColumn([pptBadge(3, role: "themaC"),
						pptCard("Drittens", "Ziffern in Kreisen führen durch eine Reihenfolge.", role: "themaC")],
						gap: 8)
				]),
				pptBanner(pptParagraph([pptRun("Kernaussage: "),
					pptRun("Ein Banner hebt die eine Botschaft der Folie hervor.", bold: false)]), role: "themaA")
			], notes: "Karten nebeneinander in einer Zeile, Ziffern als Badges, die Kernaussage als Banner."),

			pptContent("Ablauf und Abwägung", "Eine Kette von Schritten, darunter Stärken und Grenzen", [
				pptFlow([
					pptCard("Eingabe", "Daten aus der Anwendung", role: "themaA"),
					pptCard("Verarbeitung", "Vorlage und Regeln", role: "themaA"),
					pptCard("Ergebnis", "Bericht zur Prüfung", role: "themaB")
				]),
				pptProCon(["Erste Stärke", "Zweite Stärke"], ["Erste Grenze", "Zweite Grenze"],
					"Stärken", "Grenzen")
			], notes: "pptFlow für Abläufe, pptProCon für die Abwägung zweier Seiten."),

			pptContent("Tabelle mit Bewertung", "Farbige Bewertungszellen für einen Vergleich", [
				pptTable([
					[pptCell("Kriterium", fill: "themaA", color: "inverse"),
						pptCell("Option A", fill: "themaA", color: "inverse", align: "center"),
						pptCell("Option B", fill: "themaB", color: "inverse", align: "center")],
					["Erfüllt die Anforderung", pptRating("met", "erfüllt"), pptRating("partial", "bedingt")],
					["Aufwand", pptRating("low", "gering"), pptRating("high", "hoch")],
					["Risiko", pptRating("medium", "mittel"), pptRating("unmet", "nein")]
				], headerRows: 1, columnWeights: [6.6, 3, 3], rowHeight: 36)
			], notes: "Tabellen: Kopfzeilen mit pptCell, Bewertungen mit pptRating."),

			pptContent("Kennzahlen", "Kennzahlen mit Legende", [
				pptRow([
					pptStat("17", "Thema A", "Meldungen im Quartal", role: "themaA"),
					pptStat("12", "Thema B", "davon behoben", role: "erledigt"),
					pptStat("5", "Thema C", "offene Fehler", role: "risiko")
				], height: "auto"),
				pptLegend([["neu", "themaA"], ["behoben", "erledigt"], ["Fehler", "risiko"]], width: 450)
			], notes: "pptStat für Kennzahlen; pptLegend erklärt die Farben."),

			pptContent("Zeitplan", "Meilensteine auf einer Zeitachse", [
				pptTimeline([
					["01.02.", "Start", "Umfang fest", "themaA"],
					["01.03.", "Prototyp", "erste Erprobung", "themaA"],
					{"date": "01.04.", "title": "Entscheidung", "role": "risiko"},
					["01.05.", "Lieferung", "Version 1", "themaB"]
				]),
				pptCard("Hinweis", "Die Zeitachse verteilt die Meilensteine gleichmäßig.")
			], notes: "pptTimeline für Fahrpläne; Datumsangaben farbig, Meilensteine mit Titel und Unterzeile.")
		],
		footer: "Beispiel · TopLogic-Präsentation",
		profile: {
			"roles": {
				"themaA": "accent1",
				"themaB": "accent3",
				"themaC": "7A3DB8",
				"risiko": "FF5733",
				"erledigt": "4BA861"
			},
			"styles": {"card": $card},
			"agendaLayouts": {"3": 3, "4": 4, "5": 5, "6": 7, "7": 8, "8": 9}
		})
		""";

	public void testShowcase() throws Exception {
		BinaryData deck = (BinaryData) evalOnce(SHOWCASE, template(), cardStyle());
		try (XMLSlideShow slideShow = open(deck)) {
			List<XSLFSlide> slides = slideShow.getSlides();
			assertEquals(7, slides.size());

			assertEquals("1_Titel / Abschluss", slides.get(0).getSlideLayout().getName());
			XSLFSlide agenda = slides.get(1);
			assertEquals(analyze(template()).layouts().get(AGENDA_5_LAYOUT).name(),
				agenda.getSlideLayout().getName());
			assertEquals("Karten und Kernaussagen", placeholderText(agenda, "body", 21));
			assertEquals("Zeitplan", placeholderText(agenda, "body", 25));
			for (int n = 2; n < slides.size(); n++) {
				assertEquals("4_Text volle Breite", slides.get(n).getSlideLayout().getName());
			}

			Rectangle2D area = analyze(template()).layouts().get(CONTENT_LAYOUT).placeholder("body", CONTENT_IDX)
				.bounds();
			for (int n = 0; n < slides.size(); n++) {
				XSLFSlide slide = slides.get(n);
				assertFalse("Notes of slide " + n, notesText(slideShow.getNotesSlide(slide)).isBlank());
				for (XSLFShape shape : slide.getShapes()) {
					if (PlaceholderInfo.typeOf(shape) != null) {
						continue;
					}
					Rectangle2D bounds = shape.getAnchor();
					assertTrue("Shape " + shape.getShapeName() + " of slide " + n + " outside the content area: "
						+ bounds, contains(area, bounds));
					if (shape instanceof XSLFTextShape text) {
						assertFalse("Text must not be cut: " + text.getText(), text.getText().contains("…"));
					}
				}
			}

			List<BufferedImage> images = render(slides);
			writeImages(images, "showcase");
		}

		String reference = System.getProperty(REFERENCE_PROPERTY);
		if (reference != null && !reference.isEmpty()) {
			try (XMLSlideShow slideShow = open(BinaryDataFactory.createBinaryData(new File(reference)))) {
				writeImages(render(slideShow.getSlides()), "reference");
			}
		}
	}

	private static boolean contains(Rectangle2D area, Rectangle2D bounds) {
		double tolerance = 0.5;
		return bounds.getX() >= area.getX() - tolerance && bounds.getY() >= area.getY() - tolerance
			&& bounds.getMaxX() <= area.getMaxX() + tolerance && bounds.getMaxY() <= area.getMaxY() + tolerance;
	}

	private static List<BufferedImage> render(List<XSLFSlide> slides) {
		SlideRenderer renderer = new SlideRenderer();
		List<BufferedImage> result = new ArrayList<>();
		for (XSLFSlide slide : slides) {
			result.add(renderer.render(slide, 1280));
		}
		return result;
	}

	/**
	 * The style of the cards: the geometry, fill and shadow of the card on the sample slide of the
	 * template, as reported by <code>pptAnalyze()</code>, with an outline in the color of the
	 * card's role.
	 */
	private static Map<String, Object> cardStyle() throws Exception {
		Map<String, Object> result = new LinkedHashMap<>(TestBlocks.sampleCardStyle());
		result.put(LINE, Map.of(COLOR, Map.of(ROLE, ROLE_ACCENT), WIDTH, 1.0));
		return result;
	}

	public static Test suite() {
		return suite(TestShowcase.class);
	}

}
