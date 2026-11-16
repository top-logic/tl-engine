/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.script;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.GanttLayout;
import com.top_logic.react.flow.data.GanttRow;
import com.top_logic.react.flow.data.SpaceDistribution;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.data.VerticalLayout;
import com.top_logic.react.flow.server.script.FlowFactory;
import com.top_logic.react.flow.server.svg.SvgTagWriter;
import com.top_logic.react.flow.server.ui.AWTContext;
import com.top_logic.react.flow.svg.SvgConstants;

/**
 * Test case for the IDs a flow diagram writes into its SVG: Each ID is unique in the document,
 * independent of client IDs and CSS classes, and each reference points to exactly one element.
 */
public class TestFlowSvgUniqueIds extends TestCase {

	private static final String FIRST_CHART = "first";

	private static final String SECOND_CHART = "second";

	private static final Pattern URL_REF = Pattern.compile("url\\(#([^)]*)\\)");

	/**
	 * Two clip boxes of different widths without CSS class clip at their own rectangle.
	 */
	public void testClipBoxesWithoutCssClass() throws Exception {
		Diagram diagram = FlowFactory.chart(FlowFactory.horizontal(Arrays.asList(
			sized(FlowFactory.clipbox(text("AAAAAAAAAA"), null, null), 20.0),
			sized(FlowFactory.clipbox(text("BBBBBBBBBB"), null, null), 40.0)), 0, SpaceDistribution.NONE, null, null),
			null, null, false);

		Document svg = export(diagram);

		Map<String, Element> clips = clipGroupsByText(svg);
		String clipA = clipPathId(clips.get("AAAAAAAAAA"));
		String clipB = clipPathId(clips.get("BBBBBBBBBB"));
		assertFalse("distinct clip paths: " + clipA, clipA.equals(clipB));
		assertEquals("first box clipped at its own rectangle", 20.0, clipWidth(svg, clipA), 1e-9);
		assertEquals("second box clipped at its own rectangle", 40.0, clipWidth(svg, clipB), 1e-9);
		assertIdsUniqueAndReferencesResolved(svg);
	}

	/**
	 * Two clip boxes with the same CSS class get different clip paths and keep the class.
	 */
	public void testClipBoxesWithSameCssClass() throws Exception {
		Diagram diagram = FlowFactory.chart(FlowFactory.vertical(Arrays.asList(
			FlowFactory.clipbox(text("AAAA"), "cell", null),
			FlowFactory.clipbox(text("BBBB"), "cell", null)), 0, SpaceDistribution.NONE, null, null),
			null, null, false);

		Document svg = export(diagram);

		Map<String, Element> clips = clipGroupsByText(svg);
		Element groupA = clips.get("AAAA");
		Element groupB = clips.get("BBBB");
		assertFalse("distinct clip paths", clipPathId(groupA).equals(clipPathId(groupB)));
		assertEquals("class kept", "cell", groupA.getAttribute(SvgConstants.CLASS_ATTR));
		assertEquals("class kept", "cell", groupB.getAttribute(SvgConstants.CLASS_ATTR));
		assertFalse("class is no ID", ids(svg).contains("cell"));
		assertIdsUniqueAndReferencesResolved(svg);
	}

	/**
	 * Two Gantt charts in one diagram, both scrolling in a viewport, use disjoint clip paths.
	 */
	public void testTwoGanttsUseDisjointClipPaths() throws Exception {
		GanttLayout first = tallGantt(FIRST_CHART);
		GanttLayout second = tallGantt(SECOND_CHART);
		VerticalLayout column = VerticalLayout.create();
		column.addContent(first);
		column.addContent(second);
		Diagram diagram = Diagram.create().setRoot(column);
		diagram.layout(new AWTContext(12f), 1000, 400);

		Document svg = draw(diagram);

		Set<String> clipsFirst = clipPathIdsWithin(svg, FIRST_CHART, SECOND_CHART);
		Set<String> clipsSecond = clipPathIdsWithin(svg, SECOND_CHART, FIRST_CHART);
		assertEquals("clip paths of the first chart: " + clipsFirst, 5, clipsFirst.size());
		assertEquals("clip paths of the second chart: " + clipsSecond, 5, clipsSecond.size());
		Set<String> common = new HashSet<>(clipsFirst);
		common.retainAll(clipsSecond);
		assertTrue("disjoint: " + common, common.isEmpty());
		assertIdsUniqueAndReferencesResolved(svg);
	}

	/**
	 * Asserts that no ID occurs twice and that each <code>url(#…)</code> reference points to
	 * exactly one element of the document.
	 */
	private static void assertIdsUniqueAndReferencesResolved(Document svg) {
		Map<String, Integer> idCount = new HashMap<>();
		List<String> references = new ArrayList<>();
		collect(svg.getDocumentElement(), idCount, references);

		for (Map.Entry<String, Integer> entry : idCount.entrySet()) {
			assertEquals("occurrences of ID " + entry.getKey(), 1, entry.getValue().intValue());
		}
		assertFalse("references present", references.isEmpty());
		for (String reference : references) {
			assertEquals("elements with referenced ID " + reference, Integer.valueOf(1), idCount.get(reference));
		}
	}

	private static void collect(Element element, Map<String, Integer> idCount, List<String> references) {
		NamedNodeMap attributes = element.getAttributes();
		for (int n = 0; n < attributes.getLength(); n++) {
			Node attribute = attributes.item(n);
			if (SvgConstants.ID_ATTR.equals(attribute.getNodeName())) {
				idCount.merge(attribute.getNodeValue(), 1, Integer::sum);
			}
			Matcher matcher = URL_REF.matcher(attribute.getNodeValue());
			while (matcher.find()) {
				references.add(matcher.group(1));
			}
		}
		for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child instanceof Element childElement) {
				collect(childElement, idCount, references);
			}
		}
	}

	private static Set<String> ids(Document svg) {
		Map<String, Integer> idCount = new HashMap<>();
		collect(svg.getDocumentElement(), idCount, new ArrayList<>());
		return idCount.keySet();
	}

	/**
	 * The groups with a {@value SvgConstants#CLIP_PATH_ATTR} attribute, by the text they contain.
	 */
	private static Map<String, Element> clipGroupsByText(Document svg) {
		Map<String, Element> result = new HashMap<>();
		NodeList groups = svg.getElementsByTagName(SvgConstants.G);
		for (int n = 0; n < groups.getLength(); n++) {
			Element group = (Element) groups.item(n);
			if (group.hasAttribute(SvgConstants.CLIP_PATH_ATTR)) {
				result.put(group.getTextContent().trim(), group);
			}
		}
		return result;
	}

	/** The ID of the clip path the given group references. */
	private static String clipPathId(Element group) {
		assertNotNull("clipped group", group);
		Matcher matcher = URL_REF.matcher(group.getAttribute(SvgConstants.CLIP_PATH_ATTR));
		assertTrue("clip path reference", matcher.matches());
		return matcher.group(1);
	}

	/** The width of the rectangle of the clip path with the given ID. */
	private static double clipWidth(Document svg, String clipPathId) {
		NodeList clipPaths = svg.getElementsByTagName(SvgConstants.CLIP_PATH);
		for (int n = 0; n < clipPaths.getLength(); n++) {
			Element clipPath = (Element) clipPaths.item(n);
			if (clipPathId.equals(clipPath.getAttribute(SvgConstants.ID_ATTR))) {
				Element rect = (Element) clipPath.getElementsByTagName(SvgConstants.RECT).item(0);
				return Double.parseDouble(rect.getAttribute(SvgConstants.WIDTH_ATTR));
			}
		}
		fail("No clip path with ID " + clipPathId);
		return 0;
	}

	/**
	 * The IDs of the clip paths drawn within the chart whose row labels start with the given name.
	 */
	private static Set<String> clipPathIdsWithin(Document svg, String chart, String otherChart) {
		Set<String> result = new HashSet<>();
		NodeList clipPaths = svg.getElementsByTagName(SvgConstants.CLIP_PATH);
		for (int n = 0; n < clipPaths.getLength(); n++) {
			Element clipPath = (Element) clipPaths.item(n);
			if (isWithinChart(clipPath, chart, otherChart)) {
				result.add(clipPath.getAttribute(SvgConstants.ID_ATTR));
			}
		}
		return result;
	}

	/**
	 * Whether the innermost group around the given element that holds row labels holds those of
	 * the given chart only.
	 */
	private static boolean isWithinChart(Element element, String chart, String otherChart) {
		for (Node parent = element.getParentNode(); parent instanceof Element group; parent = group.getParentNode()) {
			String text = group.getTextContent();
			boolean own = text.contains(chart + "-");
			boolean other = text.contains(otherChart + "-");
			if (own || other) {
				return own && !other;
			}
		}
		return false;
	}

	/**
	 * A Gantt chart with rows higher than the space it gets, so that it scrolls in a viewport.
	 *
	 * @param chart
	 *        The start of the row labels.
	 */
	private static GanttLayout tallGantt(String chart) {
		List<GanttRow> rows = new ArrayList<>();
		GanttLayout layout = GanttLayout.create()
			.setRowLabelMinWidth(200.0)
			.setRangeMin(0).setRangeMax(100).setZoom(1.0);
		for (int n = 0; n < 50; n++) {
			Text label = Text.create().setValue(chart + "-" + n);
			rows.add(GanttRow.create().setId("r" + n).setLabel(label));
			layout.addContent(label);
		}
		layout.setRootRows(rows);
		return layout;
	}

	private static Box text(String value) {
		return Text.create().setValue(value);
	}

	private static Box sized(Box content, double width) {
		return FlowFactory.position(content, null, null, null, null, width, null, null, null, false, null);
	}

	private static Document export(Diagram diagram) throws Exception {
		BinaryData data = FlowFactory.toSvg(diagram, "TestFlowSvgUniqueIds.svg", 12.0, null, null);
		try (InputStream in = data.getStream()) {
			return parse(in);
		}
	}

	private static Document draw(Diagram diagram) throws Exception {
		StringWriter buffer = new StringWriter();
		try (TagWriter tagWriter = new TagWriter(buffer);
				SvgTagWriter svgWriter = new SvgTagWriter(tagWriter)) {
			diagram.draw(svgWriter);
		}
		return parse(new ByteArrayInputStream(buffer.toString().getBytes(StandardCharsets.UTF_8)));
	}

	private static Document parse(InputStream in) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		return factory.newDocumentBuilder().parse(in);
	}

}
