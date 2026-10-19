/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.presentation;

import static com.top_logic.presentation.data.PresentationData.*;
import static test.com.top_logic.presentation.TestDeck.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKeyTemplate;
import com.top_logic.presentation.model.BulletSpec;
import com.top_logic.presentation.model.Bounds;
import com.top_logic.presentation.model.CellSpec;
import com.top_logic.presentation.model.ColorSpec;
import com.top_logic.presentation.model.ContainerSpec;
import com.top_logic.presentation.model.CrossAlign;
import com.top_logic.presentation.model.DescriptionReader;
import com.top_logic.presentation.model.ElementSpec;
import com.top_logic.presentation.model.ParagraphSpec;
import com.top_logic.presentation.model.ShapeSpec;
import com.top_logic.presentation.model.TextBoxSpec;
import com.top_logic.presentation.model.Value;
import com.top_logic.presentation.profile.TemplateProfile;
import com.top_logic.presentation.render.SlideRenderer;
import com.top_logic.presentation.template.PlaceholderInfo;
import com.top_logic.util.error.TopLogicException;

/**
 * Test case for the building blocks of a presentation (<code>pptCard()</code>,
 * <code>pptBanner()</code>, ...), their expansion into shapes and text boxes, and the color
 * roles, styles and rating styles of the template profile.
 *
 * <p>
 * If the system property {@link TestDeck#PNG_DIR_PROPERTY} names a directory, the slides of the
 * test presentation are rendered into PNG images there, once with the default profile and once
 * with the card style of the template's sample slide.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestBlocks extends AbstractSearchExpressionTest {

	private static final ColorSpec ACCENT = new ColorSpec.Role(ROLE_ACCENT, Map.of());

	/**
	 * The slides showing all building blocks.
	 */
	private static final String DECK_SLIDES = """
		[
			pptTitle("Bausteine", "Eine Beispielpräsentation", notes: "Titelfolie"),
			pptContent("Karten und Kernaussagen", "Drei Karten, eine Kennzahl, eine Bewertung", [
				pptRow([
					pptCard("Erstens", "Eine Karte trägt eine Überschrift und kurzen Text.", role: "topicA"),
					pptCard("Zweitens", "Die Farbe der Rolle rahmt die Karte.", role: "topicB"),
					pptCard("Drittens", ["Absätze", pptParagraph("mit Aufzählung", bullet: true)], role: "topicC")
				], height: 115),
				pptRow([
					pptStat("491", "Tickets", "im Release geschlossen", role: "done", width: 230),
					pptTable([
						["Kriterium", "Option A", "Option B"],
						["Erfüllt die Anforderung", pptRating("met"), pptRating("partial")],
						["Aufwand", pptRating("low"), pptRating("high")],
						["Risiko", pptRating("medium"), pptRating("unmet")]
					], headerRows: 1, columnWeights: [2, 1, 1])
				], height: 140),
				pptBanner(pptParagraph([pptRun("Kernaussage: "),
					pptRun("Ein Banner hebt die eine Botschaft hervor.", bold: false)]), role: "topicA")
			], notes: "Karten, Kennzahl, Bewertung und Banner"),
			pptContent("Ablauf", "Schritte, Ziffern, Aufzählung und Legende", [
				pptRow([
					pptCard("Eingabe", "Daten aus der Anwendung"),
					pptArrow(),
					pptCard("Verarbeitung", "Vorlage und Regeln"),
					pptArrow(),
					pptCard("Ergebnis", "Bericht zur Prüfung", role: "done")
				], align: "center", weights: [1, 1, 1, 1, 1], height: 80),
				pptRow([pptBadge(1, role: "topicA"), pptBadge(2, role: "topicB"), pptBadge(3, role: "topicC"),
					pptArrow("down", role: "risk")], height: 40),
				pptBullets(["Erster Punkt", "Zweiter Punkt mit etwas mehr Text", "Dritter Punkt"], role: "risk",
					height: "auto"),
				pptLegend([["geplant", "muted"], ["erreicht", "done"], ["Risiko", "risk"], ["Warnung", "warning"]],
					width: 600)
			]),
			pptClosing("Vielen Dank", "Fragen?")
		]
		""";

	public void testSlideBuilders() throws Exception {
		assertEquals(map(TYPE, TYPE_SLIDE, LAYOUT, "title", TITLE, "T", SUBTITLE, "S", NOTES, "N"),
			eval("pptTitle('T', 'S', notes: 'N')"));
		assertEquals(map(TYPE, TYPE_SLIDE, LAYOUT, "closing", TITLE, "T"), eval("pptClosing('T')"));
		assertEquals(map(TYPE, TYPE_SLIDE, LAYOUT, "section", TITLE, "T", SUBTITLE, "S"), eval("pptSection('T', 'S')"));
		Map<String, Object> banner = map(TYPE, TYPE_BANNER, TEXT, "B");
		assertEquals(map(TYPE, TYPE_SLIDE, LAYOUT, "content", TITLE, "T", CONTENT, List.of(banner)),
			eval("pptContent('T', content: pptBanner('B'))"));
		assertEquals(map(TYPE, TYPE_SLIDE, LAYOUT, "content", TITLE, "T", SUBTITLE, "S", CONTENT, List.of(banner, banner)),
			eval("pptContent('T', 'S', [pptBanner('B'), pptBanner('B')])"));
	}

	public void testBlockBuilders() throws Exception {
		assertEquals(map(TYPE, TYPE_CARD, HEADING, "H", BODY, "B", ROLE, "topicA", WIDTH, 200.0),
			eval("pptCard('H', 'B', role: 'topicA', width: 200)"));
		assertEquals(map(TYPE, TYPE_BANNER, TEXT, "B", ROLE, "risk", STYLE, "note"),
			eval("pptBanner('B', role: 'risk', style: 'note')"));
		assertEquals(map(TYPE, TYPE_BADGE, LABEL, 1.0, ROLE, "topicB"), eval("pptBadge(1, role: 'topicB')"));
		assertEquals(map(TYPE, TYPE_STAT, VALUE, "491", LABEL, "L", CAPTION, "C", ROLE, "done", HEIGHT, "auto"),
			eval("pptStat('491', 'L', 'C', role: 'done', height: 'auto')"));
		assertEquals(map(TYPE, TYPE_BULLETS, ITEMS, List.of("a", "b"), SPLIT, false, ROLE, "topicC"),
			eval("pptBullets(['a', 'b'], role: 'topicC', split: false)"));
		assertEquals(map(TEXT, "met", RATING, "met"), eval("pptRating('met')"));
		assertEquals(map(TEXT, "yes", RATING, "met"), eval("pptRating('met', 'yes')"));
		assertEquals(map(TYPE, TYPE_LEGEND, ITEMS, List.of(List.of("a", "done")), Y, 10.0),
			eval("pptLegend([['a', 'done']], y: 10)"));
		assertEquals(map(TYPE, TYPE_ARROW, DIRECTION, "down", ROLE, "risk"), eval("pptArrow('down', 'risk')"));
		assertEquals(map(TYPE, TYPE_SHAPE, STYLE, "card", ROLE, "topicA"), eval("pptShape(style: 'card', role: 'topicA')"));
		assertEquals(map(TYPE, TYPE_TEXT_BOX, TEXT, "t", ROLE, "risk"), eval("pptTextBox('t', role: 'risk')"));
	}

	public void testExpansion() throws Exception {
		ShapeSpec card = (ShapeSpec) read("pptCard('H', ['B1', 'B2'], role: 'topicA', width: 200)");
		assertEquals(TYPE_CARD, card.style());
		assertEquals("topicA", card.role());
		assertNull("The style gives the geometry.", card.geometry());
		assertNull(card.fill());
		assertEquals(200.0, card.bounds().width());
		List<ParagraphSpec> paragraphs = card.text().paragraphs();
		assertEquals(3, paragraphs.size());
		assertEquals(Boolean.TRUE, paragraphs.get(0).style().bold());
		assertEquals(16.0, paragraphs.get(0).style().size());
		assertNull("The body has the formatting of the style.", paragraphs.get(1).style().size());
		assertEquals("B2", paragraphs.get(2).runs().get(0).text());

		ShapeSpec ownStyle = (ShapeSpec) read("pptCard('H', style: 'highlight')");
		assertEquals("highlight", ownStyle.style());
		assertNull(ownStyle.role());

		ShapeSpec banner = (ShapeSpec) read("pptBanner('B', role: 'risk')");
		assertEquals(TYPE_BANNER, banner.style());
		assertTrue("A banner is as high as its text.", banner.bounds().autoHeight());

		ShapeSpec badge = (ShapeSpec) read("pptBadge(1, height: 30)");
		assertEquals(new Bounds(null, null, 30.0, 30.0, false, null), badge.bounds());
		assertEquals("1", badge.text().paragraphs().get(0).runs().get(0).text());
		assertEquals(new Bounds(null, null, 40.0, 40.0, false, null), read("pptBadge('A')").bounds());

		ShapeSpec stat = (ShapeSpec) read("pptStat(491, 'Label', 'Caption', role: 'done')");
		assertEquals(TYPE_STAT, stat.style());
		List<ParagraphSpec> statText = stat.text().paragraphs();
		assertEquals(3, statText.size());
		assertEquals("491", statText.get(0).runs().get(0).text());
		assertEquals("The value has the color of the role.", ACCENT, statText.get(0).style().color());
		assertEquals(new ColorSpec.Role(ROLE_MUTED, Map.of()), statText.get(2).style().color());

		TextBoxSpec bullets = (TextBoxSpec) read("pptBullets(['a', pptParagraph('b', bullet: '-')], role: 'risk')");
		assertEquals("risk", bullets.role());
		assertTrue("Bullet lists continue by default.", bullets.text().split());
		assertEquals(new BulletSpec(true, null, ACCENT), bullets.text().paragraphs().get(0).bullet());
		assertEquals("An own bullet wins.", "-", bullets.text().paragraphs().get(1).bullet().character());
		assertFalse(((TextBoxSpec) read("pptBullets(['a'], split: false)")).text().split());

		ContainerSpec legend = (ContainerSpec) read("pptLegend([['planned', 'muted'], ['done', 'done']])");
		assertEquals(ContainerSpec.Direction.ROW, legend.direction());
		assertTrue(legend.bounds().autoHeight());
		assertEquals(2, legend.children().size());
		ContainerSpec entry = (ContainerSpec) legend.children().get(1);
		assertEquals(CrossAlign.CENTER, entry.align());
		ShapeSpec mark = (ShapeSpec) entry.children().get(0);
		assertEquals("done", mark.role());
		assertEquals(12.0, mark.bounds().width());
		assertEquals("done", ((TextBoxSpec) entry.children().get(1)).text().paragraphs().get(0).runs().get(0).text());

		ShapeSpec arrow = (ShapeSpec) read("pptArrow('down', 'risk')");
		assertEquals(TYPE_ARROW, arrow.style());
		assertEquals("downArrow", arrow.geometry().preset());
		assertEquals(new Bounds(null, null, 24.0, 30.0, false, null), arrow.bounds());
		ShapeSpec rightArrow = (ShapeSpec) read("pptArrow(width: 50)");
		assertEquals("rightArrow", rightArrow.geometry().preset());
		assertNull(rightArrow.role());
		assertEquals(new Bounds(null, null, 50.0, 24.0, false, null), rightArrow.bounds());

		CellSpec rating = DescriptionReader.readCell(new Value(eval("pptRating('met')"), "cell"));
		assertEquals("met", rating.rating());
		assertNull(rating.fill());
	}

	public void testCompositeBuilders() throws Exception {
		assertEquals(map(TYPE, TYPE_SLIDE, LAYOUT, "agenda", TITLE, "A", ENTRIES, List.of("x", "y"), NOTES, "N"),
			eval("pptAgenda(['x', 'y'], 'A', 'N')"));
		assertEquals(map(TYPE, TYPE_TIMELINE, MILESTONES, List.of(List.of("d", "t")), ROLE, "topicA"),
			eval("pptTimeline([['d', 't']], 'topicA')"));
		assertEquals(map(TYPE, TYPE_FLOW, STEPS, List.of("a", "b"), HEIGHT, 50.0),
			eval("pptFlow(['a', 'b'], height: 50)"));
		assertEquals(map(TYPE, TYPE_PRO_CON, PROS, List.of("p"), CONS, List.of("c"), PROS_TITLE, "P"),
			eval("pptProCon(['p'], ['c'], 'P')"));
	}

	public void testCompositeExpansion() throws Exception {
		ContainerSpec timeline = (ContainerSpec) read(
			"pptTimeline([['d1', 't1', 's1', 'risk'], {'date': 'd2', 'title': 't2'}], role: 'topicB')");
		assertTrue(timeline.bounds().autoHeight());
		assertEquals(ContainerSpec.Direction.COLUMN, timeline.direction());
		ContainerSpec dates = (ContainerSpec) timeline.children().get(0);
		assertEquals("risk", dates.children().get(0).role());
		assertEquals("The role of the timeline is the default.", "topicB", dates.children().get(1).role());
		ContainerSpec markers = (ContainerSpec) timeline.children().get(1);
		ContainerSpec first = (ContainerSpec) markers.children().get(0);
		assertTrue("No line before the first milestone.", first.children().get(0) instanceof ContainerSpec);
		assertEquals(TYPE_TIMELINE, ((ShapeSpec) first.children().get(2)).style());
		assertEquals(TYPE_BADGE, ((ShapeSpec) first.children().get(1)).style());
		ContainerSpec last = (ContainerSpec) markers.children().get(1);
		assertTrue("No line after the last milestone.", last.children().get(2) instanceof ContainerSpec);
		List<ParagraphSpec> text = ((TextBoxSpec) ((ContainerSpec) timeline.children().get(2)).children().get(0))
			.text().paragraphs();
		assertEquals("t1", text.get(0).runs().get(0).text());
		assertEquals(new ColorSpec.Role(ROLE_MUTED, Map.of()), text.get(1).style().color());

		ContainerSpec flow = (ContainerSpec) read("pptFlow(['a', pptCard('B', role: 'done', weight: 2)], 'topicC')");
		assertTrue(flow.bounds().autoHeight());
		assertEquals(3, flow.children().size());
		assertEquals("topicC", flow.children().get(0).role());
		ShapeSpec arrow = (ShapeSpec) ((ContainerSpec) flow.children().get(1)).children().get(1);
		assertEquals(TYPE_ARROW, arrow.style());
		assertEquals("done", flow.children().get(2).role());
		assertEquals(2.0, flow.children().get(2).bounds().weight());

		ContainerSpec proCon = (ContainerSpec) read("pptProCon(['p1', 'p2'], ['c1'], consTitle: 'Risks')");
		ShapeSpec pros = (ShapeSpec) proCon.children().get(0);
		ShapeSpec cons = (ShapeSpec) proCon.children().get(1);
		assertEquals(ROLE_DONE, pros.role());
		assertEquals(ROLE_RISK, cons.role());
		assertEquals(3, pros.text().paragraphs().size());
		assertEquals(ACCENT, pros.text().paragraphs().get(0).style().color());
		assertEquals("\u2713", pros.text().paragraphs().get(1).bullet().character());
		assertEquals("Risks", cons.text().paragraphs().get(0).runs().get(0).text());
		assertEquals("\u2013", cons.text().paragraphs().get(1).bullet().character());

		TopLogicException milestone = expectError(() -> read("pptTimeline([5])"));
		assertEquals("e.milestones[0]", argument(milestone,
			com.top_logic.presentation.model.I18NConstants.ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED, 0));
		TopLogicException position = expectError(() -> read("pptFlow([pptCard('A', x: 10)])"));
		assertEquals("e.steps[0].x", argument(position,
			com.top_logic.presentation.model.I18NConstants.ERROR_POSITION_IN_CONTAINER__PATH, 0));
	}

	public void testAgendaLayouts() throws Exception {
		Map<?, ?> profile = (Map<?, ?>) eval("t -> pptProfile($t)", template());
		assertEquals(map("3", 3.0, "4", 4.0, "5", 5.0, "6", 7.0, "7", 8.0, "8", 9.0), profile.get(AGENDA_LAYOUTS));
		Map<?, ?> twoColumns = (Map<?, ?>) ((Map<?, ?>) profile.get(SLIDE_LAYOUTS)).get("6");
		assertEquals("Entries are read column by column.", List.of(21.0, 22.0, 23.0, 24.0, 25.0),
			twoColumns.get(ENTRIES));
		assertNull("A list entry is no subtitle.", twoColumns.get(SUBTITLE));

		try (XMLSlideShow deck = open((BinaryData) evalOnce(
			"t -> pptDeck($t, [pptAgenda(['A', 'B', 'C', 'D']), pptAgenda(['A', 'B'], 'Kurz')], "
				+ "profile: {'agendaLayouts': {'4': 6}})",
			template()))) {
			XSLFSlide agenda = deck.getSlides().get(0);
			assertEquals("2_Index (5.1)", agenda.getSlideLayout().getName());
			assertEquals("D", placeholderText(agenda, "body", 24));

			XSLFSlide fallback = deck.getSlides().get(1);
			assertEquals("4_Text volle Breite", fallback.getSlideLayout().getName());
			List<XSLFShape> shapes = contentShapes(fallback);
			assertEquals("A badge and a text for each entry.", 4, shapes.size());
			assertEquals("2", ((XSLFTextShape) shapes.get(2)).getText());
			assertEquals("B", ((XSLFTextShape) shapes.get(3)).getText());
			assertTrue(shapes.get(2).getAnchor().getY() > shapes.get(0).getAnchor().getY());
		}

		TopLogicException tooFew = expectError(() -> evalOnce(
			"t -> pptDeck($t, {'layout': 3, 'title': 'X', 'entries': ['a', 'b', 'c', 'd']})", template()));
		ResKeyTemplate key =
			com.top_logic.presentation.export.I18NConstants.ERROR_TOO_FEW_ENTRY_PLACEHOLDERS__PATH_COUNT_LAYOUT_AVAILABLE;
		assertEquals("slides[0].entries", argument(tooFew, key, 0));
		assertEquals(3, ((Number) argument(tooFew, key, 3)).intValue());
	}

	public void testErrorExpansion() throws Exception {
		TopLogicException legend = expectError(() -> read("pptLegend([['a', 'done'], 'b'])"));
		assertEquals("e.items[1]", argument(legend,
			com.top_logic.presentation.model.I18NConstants.ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED, 0));
		TopLogicException role = expectError(() -> read("pptCard('H', role: 'no role')"));
		assertEquals("e.role", argument(role,
			com.top_logic.presentation.model.I18NConstants.ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED, 0));
		Map<String, Object> card = map(TYPE, TYPE_CARD, HEADING, "H", LABEL, "x");
		TopLogicException unknown = expectError(() -> DescriptionReader.readElement(new Value(card, "e")));
		assertEquals("e.label", argument(unknown,
			com.top_logic.presentation.model.I18NConstants.ERROR_UNKNOWN_ENTRY__PATH_KEYS, 0));
	}

	public void testDefaultProfile() throws Exception {
		Map<?, ?> profile = (Map<?, ?>) eval("t -> pptProfile($t)", template());
		Map<?, ?> roles = (Map<?, ?>) profile.get(ROLES);
		assertEquals(Map.of(SCHEME, "accent1"), roles.get(ROLE_ACCENT));
		assertEquals(Map.of(SCHEME, "accent3"), roles.get(ROLE_TOPIC_C));
		assertEquals(Map.of(SCHEME, "accent6"), roles.get(ROLE_TOPIC_F));
		assertEquals(Map.of(RGB, "FF5733"), roles.get(ROLE_RISK));
		assertEquals(Map.of(SCHEME, "tx1"), roles.get(ROLE_TEXT));
		assertEquals(Map.of(SCHEME, "bg1"), roles.get(ROLE_INVERSE));
		assertEquals("tx1", ((Map<?, ?>) roles.get(ROLE_MUTED)).get(SCHEME));

		Map<?, ?> styles = (Map<?, ?>) profile.get(STYLES);
		assertEquals(List.of(TYPE_CARD, TYPE_STAT, TYPE_BANNER, TYPE_BADGE, TYPE_ARROW, TYPE_TIMELINE), new ArrayList<>(styles.keySet()));
		Map<?, ?> card = (Map<?, ?>) styles.get(TYPE_CARD);
		assertEquals("roundRect", ((Map<?, ?>) card.get(GEOMETRY)).get(PRESET));
		assertEquals(Map.of(ROLE, ROLE_ACCENT), ((Map<?, ?>) card.get(LINE)).get(COLOR));
		assertEquals(ROLE_MUTED, ((Map<?, ?>) styles.get(TYPE_ARROW)).get(ROLE));

		Map<?, ?> ratings = (Map<?, ?>) profile.get(RATINGS);
		assertEquals(List.of(RATING_MET, RATING_PARTIAL, RATING_UNMET, RATING_LOW, RATING_MEDIUM, RATING_HIGH,
			RATING_NONE), new ArrayList<>(ratings.keySet()));
		assertEquals("DCEFE0", ((Map<?, ?>) ratings.get(RATING_MET)).get(FILL));
	}

	public void testDefaultStyles() throws Exception {
		try (XMLSlideShow deck = open((BinaryData) evalOnce("t -> pptDeck($t, pptContent('Styles', content: ["
			+ "pptCard('Card', 'Text', role: 'topicB', height: 100), "
			+ "pptBanner('Banner', role: 'risk'), "
			+ "pptArrow(), "
			+ "pptTable([['A', 'B'], [pptRating('met'), pptRating('none', 'n/a')]], headerRows: 1)]))",
			template()))) {
			List<XSLFShape> shapes = contentShapes(deck.getSlides().get(0));
			String card = xml(shapes.get(0));
			assertTrue(card, card.contains("<a:prstGeom prst=\"roundRect\">"));
			assertTrue("Light gradient: " + card, card.contains("<a:gradFill") && card.contains("val=\"bg1\""));
			assertTrue("Outline in the role color: " + card,
				card.contains("<a:ln w=\"12700\"><a:solidFill><a:schemeClr val=\"accent2\"/>"));
			assertTrue("Shadow in the role color: " + card, card.contains("<a:outerShdw")
				&& card.contains("blurRad=\"152400\"") && card.contains("dist=\"63500\"")
				&& card.contains("<a:schemeClr val=\"accent2\"><a:lumMod val=\"75000\"/><a:alpha val=\"20000\"/>"));
			assertTrue("Text at the top left: " + card, card.contains("anchor=\"t\"") && card.contains("algn=\"l\""));
			assertEquals(100.0, shapes.get(0).getAnchor().getHeight(), 0.01);

			String banner = xml(shapes.get(1));
			assertTrue("Filled with the role color: " + banner,
				banner.contains("<a:solidFill><a:srgbClr val=\"FF5733\"/></a:solidFill>"));
			assertTrue("Text in the inverse color: " + banner, banner.contains("<a:schemeClr val=\"bg1\"/>"));
			assertTrue(banner, banner.contains("b=\"true\""));

			String arrow = xml(shapes.get(2));
			assertTrue(arrow, arrow.contains("<a:prstGeom prst=\"rightArrow\">"));
			assertTrue("Arrow in the muted color: " + arrow, arrow.contains(
				"<a:solidFill><a:schemeClr val=\"tx1\"><a:lumMod val=\"65000\"/><a:lumOff val=\"35000\"/>"));
			assertEquals(30.0, shapes.get(2).getAnchor().getWidth(), 0.01);

			XSLFTable table = (XSLFTable) shapes.get(3);
			String met = table.getCell(1, 0).getXmlObject().xmlText();
			assertTrue(met, met.contains("<a:srgbClr val=\"DCEFE0\"/>"));
			assertTrue(met, met.contains("<a:srgbClr val=\"2E7D43\"/>"));
			assertEquals("n/a", table.getCell(1, 1).getText());
			assertTrue(table.getCell(1, 1).getXmlObject().xmlText().contains("<a:srgbClr val=\"ECEDEF\"/>"));
		}
	}

	public void testApplicationRolesAndRatings() throws Exception {
		try (XMLSlideShow deck = open((BinaryData) evalOnce("""
			t -> pptDeck($t, pptContent('Rollen', content: [
					pptCard('Karte', 'Text', role: 'themaA', height: 100),
					pptTable([['A'], [pptRating('erfüllt')]], headerRows: 1)
				]),
				profile: {
					"roles": {"themaA": "7A3DB8", "topicA": {"rgb": "123456"}},
					"ratings": {"erfüllt": {"fill": "DCEFE0", "color": "2E7D43"}},
					"styles": {"card": {"line": {"color": "accent", "width": 3}}}
				})
			""", template()))) {
			List<XSLFShape> shapes = contentShapes(deck.getSlides().get(0));
			String card = xml(shapes.get(0));
			assertTrue("Overridden line of the style in the application role: " + card,
				card.contains("<a:ln w=\"38100\"><a:solidFill><a:srgbClr val=\"7A3DB8\"/>"));
			assertTrue("The other style properties are kept: " + card, card.contains("<a:outerShdw"));
			String cell = ((XSLFTable) shapes.get(1)).getCell(1, 0).getXmlObject().xmlText();
			assertTrue(cell, cell.contains("<a:srgbClr val=\"DCEFE0\"/>"));
		}

		Map<String, Object> profile = TemplateProfile
			.create(analyze(template()), Map.of(ROLES, Map.of(ROLE_TOPIC_A, Map.of(RGB, "123456")))).toData();
		assertEquals("A role override replaces the color.", Map.of(RGB, "123456"),
			((Map<?, ?>) profile.get(ROLES)).get(ROLE_TOPIC_A));
	}

	public void testErrorUnknownNames() throws Exception {
		BinaryData template = template();

		TopLogicException style = expectError(() -> evalOnce(
			"t -> pptDeck($t, pptContent('X', content: pptCard('H', style: 'fancy')))", template));
		ResKeyTemplate styleKey = com.top_logic.presentation.profile.I18NConstants.ERROR_UNKNOWN_STYLE__PATH_STYLE_STYLES;
		assertEquals("slides[0].content[0].style", argument(style, styleKey, 0));
		assertEquals("fancy", argument(style, styleKey, 1));
		assertTrue(((String) argument(style, styleKey, 2)).contains(TYPE_CARD));

		TopLogicException role = expectError(() -> evalOnce(
			"t -> pptDeck($t, pptContent('X', content: pptCard('H', role: 'chance')))", template));
		ResKeyTemplate roleKey = com.top_logic.presentation.export.I18NConstants.ERROR_UNKNOWN_COLOR_ROLE__PATH_ROLE_ROLES;
		assertEquals("slides[0].content[0].role", argument(role, roleKey, 0));
		assertEquals("chance", argument(role, roleKey, 1));
		assertTrue(((String) argument(role, roleKey, 2)).contains(ROLE_TOPIC_A));

		TopLogicException rating = expectError(() -> evalOnce(
			"t -> pptDeck($t, pptContent('X', content: pptTable([['A'], [pptRating('excellent')]])))", template));
		ResKeyTemplate ratingKey =
			com.top_logic.presentation.profile.I18NConstants.ERROR_UNKNOWN_RATING__PATH_RATING_RATINGS;
		assertEquals("slides[0].content[0].rows[1][0].rating", argument(rating, ratingKey, 0));
		assertEquals("excellent", argument(rating, ratingKey, 1));
		assertTrue(((String) argument(rating, ratingKey, 2)).contains(RATING_PARTIAL));

		TopLogicException badStyle = expectError(() -> evalOnce(
			"t -> pptDeck($t, pptContent('X'), profile: {'styles': {'card': {'fill': 'nonsense!'}}})", template));
		assertEquals("profile.styles.card.fill", argument(badStyle,
			com.top_logic.presentation.model.I18NConstants.ERROR_INVALID_ENTRY__PATH_VALUE_EXPECTED, 0));
	}

	public void testRenderDefault() throws Exception {
		List<BufferedImage> images = render(null);
		writeImages(images, "blocks-default");
	}

	public void testRenderTemplateCardStyle() throws Exception {
		Map<String, Object> card = sampleCardStyle();
		Map<String, Object> override = new LinkedHashMap<>();
		override.put(STYLES, Map.of(TYPE_CARD, card, TYPE_STAT, card));

		try (XMLSlideShow deck = open((BinaryData) evalOnce(
			"t -> p -> pptDeck($t, pptContent('X', content: pptCard('H', 'B', role: 'topicB', height: 100)), profile: $p)",
			template(), override))) {
			String xml = xml(contentShapes(deck.getSlides().get(0)).get(0));
			assertTrue("Gradient of the sample slide: " + xml, xml.contains("<a:path path=\"circle\">"));
			assertTrue("Text formatting of the default style is kept: " + xml, xml.contains("anchor=\"t\""));
		}

		writeImages(render(override), "blocks-toplogic");
	}

	/**
	 * The geometry, fill, outline and shadow of the card on the sample slide of the template, as
	 * reported by <code>pptAnalyze()</code>.
	 */
	static Map<String, Object> sampleCardStyle() throws Exception {
		Map<?, ?> analysis = (Map<?, ?>) evalOnce("t -> pptAnalyze($t)", template());
		Map<?, ?> slide = (Map<?, ?>) ((List<?>) analysis.get(SLIDES)).get(1);
		Map<?, ?> sample = findShape((List<?>) slide.get(SHAPES), "Abgerundetes Rechteck 89");
		assertNotNull(sample);
		Map<String, Object> result = new LinkedHashMap<>();
		for (String key : List.of(GEOMETRY, FILL, LINE, SHADOW)) {
			result.put(key, sample.get(key));
		}
		return result;
	}

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

	private static List<BufferedImage> render(Map<?, ?> profile) throws Exception {
		try (XMLSlideShow deck = open((BinaryData) evalOnce("t -> p -> pptDeck($t, " + DECK_SLIDES
			+ ", footer: 'Bausteine · Test', profile: $p)", template(), profile))) {
			List<XSLFSlide> slides = deck.getSlides();
			assertEquals(4, slides.size());
			for (XSLFShape shape : contentShapes(slides.get(1))) {
				if (shape instanceof XSLFTextShape text) {
					assertFalse("Text must not be cut: " + text.getText(), text.getText().contains("…"));
				}
			}
			SlideRenderer renderer = new SlideRenderer();
			List<BufferedImage> result = new ArrayList<>();
			for (XSLFSlide slide : slides) {
				result.add(renderer.render(slide, 1280));
			}
			return result;
		}
	}

	private ElementSpec read(String script) throws Exception {
		return DescriptionReader.readElement(new Value(eval(script), "e"));
	}

	/**
	 * The shapes of the slide that are no placeholders, in their order.
	 */
	private static List<XSLFShape> contentShapes(XSLFSlide slide) {
		List<XSLFShape> result = new ArrayList<>();
		for (XSLFShape shape : slide.getShapes()) {
			if (PlaceholderInfo.typeOf(shape) == null) {
				result.add(shape);
			}
		}
		return result;
	}

	private static String xml(XSLFShape shape) {
		assertTrue(shape instanceof XSLFAutoShape);
		return shape.getXmlObject().xmlText();
	}

	private static Map<String, Object> map(Object... keysAndValues) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (int n = 0; n < keysAndValues.length; n += 2) {
			result.put((String) keysAndValues[n], keysAndValues[n + 1]);
		}
		return result;
	}

	public static Test suite() {
		return suite(TestBlocks.class);
	}

}
