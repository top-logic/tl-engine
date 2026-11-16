/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.operations;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.react.flow.data.Border;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.ClickTarget;
import com.top_logic.react.flow.data.ConnectorSymbol;
import com.top_logic.react.flow.data.ContextMenu;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.EdgeDecoration;
import com.top_logic.react.flow.data.GanttEdge;
import com.top_logic.react.flow.data.GanttEndpoint;
import com.top_logic.react.flow.data.GanttLayout;
import com.top_logic.react.flow.data.GanttLineDecoration;
import com.top_logic.react.flow.data.GanttPoint;
import com.top_logic.react.flow.data.GanttRow;
import com.top_logic.react.flow.data.GraphEdge;
import com.top_logic.react.flow.data.GraphLayout;
import com.top_logic.react.flow.data.Padding;
import com.top_logic.react.flow.data.SelectableBox;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.data.Tooltip;
import com.top_logic.react.flow.operations.Interactivity;
import com.top_logic.react.flow.operations.layout.GanttLayoutOperations;
import com.top_logic.react.flow.server.svg.SvgTagWriter;
import com.top_logic.react.flow.server.ui.AWTContext;

/**
 * Tests that edges and decoration labels of the Gantt and the graph layout receive pointer events
 * only when they are {@link Interactivity interactive}.
 */
public class TestInteractivity extends TestCase {

	private static final String LABEL = "Today";

	private static final String MILESTONE = "Milestone";

	/** The interactive wrappers a label or an edge decoration can be given. */
	private static final List<Function<Box, Box>> INTERACTIVE_WRAPPERS = Arrays.asList(
		content -> SelectableBox.create().setContent(content),
		content -> ClickTarget.create().setContent(content),
		content -> ContextMenu.create().setContent(content),
		content -> Tooltip.create().setText("Info").setContent(content));

	/**
	 * In a chart drawn without viewport, the edges and the label of a decoration let the pointer
	 * pass through, the items receive it.
	 */
	public void testGanttNotInteractiveFlat() throws Exception {
		Document svg = renderGantt(Text.create().setValue(LABEL), false);

		assertPassThrough(textElement(svg, LABEL));
		assertGanttEdgesPassThrough(svg);
		assertReceives(textElement(svg, MILESTONE));
	}

	/**
	 * In a chart drawn with viewport, the edges and the label of a decoration above the frozen rows,
	 * including its backdrop, let the pointer pass through.
	 */
	public void testGanttNotInteractiveViewport() throws Exception {
		Document svg = renderGantt(Text.create().setValue(LABEL), true);

		assertPassThrough(textElement(svg, LABEL));
		List<Element> backdrops = elementsWithClass(svg, GanttLayoutOperations.PINNED_LABEL_BACKGROUND_CSS_CLASS);
		assertEquals("backdrop drawn", 1, backdrops.size());
		assertPassThrough(backdrops.get(0));
		assertGanttEdgesPassThrough(svg);
		assertReceives(textElement(svg, MILESTONE));
	}

	/**
	 * The label of a decoration that is selectable, reacts on a click, offers a context menu, or
	 * shows a tooltip receives pointer events, also within a nested box.
	 */
	public void testGanttInteractiveLabel() throws Exception {
		for (Function<Box, Box> wrapper : INTERACTIVE_WRAPPERS) {
			for (boolean viewport : new boolean[] { false, true }) {
				Box label = Padding.create().setAll(2).setContent(wrapper.apply(Text.create().setValue(LABEL)));
				Document svg = renderGantt(label, viewport);

				Element text = textElement(svg, LABEL);
				assertReceives(text);
				if (viewport) {
					assertReceives(elementsWithClass(svg, GanttLayoutOperations.PINNED_LABEL_BACKGROUND_CSS_CLASS).get(0));
				}
				assertGanttEdgesPassThrough(svg);
			}
		}
	}

	/**
	 * An edge of a graph that is not selectable lets the pointer pass through, also on its
	 * decoration.
	 */
	public void testGraphEdgeNotInteractive() throws Exception {
		GraphEdge edge = graphEdge("not interactive");
		Document svg = renderGraph(edge);

		Element label = textElement(svg, "not interactive");
		for (Element path : descendants(edgeGroup(label), "path")) {
			assertPassThrough(path);
		}
		assertPassThrough(label);
		assertReceives(textElement(svg, "Source"));
	}

	/**
	 * A selectable edge of a graph receives pointer events, its click target only on the stroke.
	 */
	public void testGraphEdgeSelectable() throws Exception {
		GraphEdge edge = graphEdge("selectable").setSelectable(true);
		Document svg = renderGraph(edge);

		Element label = textElement(svg, "selectable");
		List<Element> paths = descendants(edgeGroup(label), "path");
		assertFalse("paths drawn", paths.isEmpty());
		boolean clickTarget = false;
		for (Element path : paths) {
			assertReceives(path);
			clickTarget |= Interactivity.POINTER_EVENTS_STROKE.equals(pointerEvents(path));
		}
		assertTrue("click target on the stroke", clickTarget);
		assertReceives(label);
	}

	/**
	 * The decoration of an edge that reacts on a click, offers a context menu, or shows a tooltip
	 * receives pointer events, the line of the edge does not.
	 */
	public void testGraphEdgeInteractiveDecoration() throws Exception {
		for (Function<Box, Box> wrapper : INTERACTIVE_WRAPPERS) {
			GraphEdge edge = GraphEdge.create()
				.setTargetSymbol(ConnectorSymbol.FILLED_ARROW)
				.addDecoration(EdgeDecoration.create()
					.setLinePosition(0.5)
					.setContent(wrapper.apply(Text.create().setValue("interactive"))));
			Document svg = renderGraph(edge);

			Element label = textElement(svg, "interactive");
			assertReceives(label);
			Element line = descendants(edgeGroup(label), "path").get(0);
			assertPassThrough(line);
		}
	}

	private static GraphEdge graphEdge(String label) {
		return GraphEdge.create()
			.setTargetSymbol(ConnectorSymbol.FILLED_ARROW)
			.addDecoration(EdgeDecoration.create()
				.setLinePosition(0.5)
				.setContent(Text.create().setValue(label)));
	}

	private static Document renderGraph(GraphEdge edge) throws Exception {
		Box source = node("Source");
		Box target = node("Target");
		edge.setSource(source).setTarget(target);

		GraphLayout layout = GraphLayout.create();
		layout.getNodes().add(source);
		layout.getNodes().add(target);
		layout.getEdges().add(edge);

		Diagram diagram = Diagram.create().setRoot(layout);
		diagram.layout(new AWTContext(12f));
		return render(diagram);
	}

	private static Box node(String name) {
		return SelectableBox.create().setContent(
			Border.create().setContent(Padding.create().setAll(5).setContent(Text.create().setValue(name))));
	}

	/**
	 * Draws a chart with a selectable milestone, an edge ending at the milestone with a filled
	 * arrow, and a line decoration spanning all rows with the given label.
	 *
	 * @param viewport
	 *        Whether to lay the chart out in a space smaller than its rows, so that it is drawn in
	 *        a viewport with a frozen row.
	 */
	private static Document renderGantt(Box label, boolean viewport) throws Exception {
		GanttPoint start = GanttPoint.create().setId("start").setRowId("r1").setAt(10)
			.setBox(Text.create().setValue("Start"));
		GanttPoint milestone = GanttPoint.create().setId("milestone").setRowId("r1").setAt(40)
			.setBox(SelectableBox.create().setContent(Text.create().setValue(MILESTONE)));
		GanttEdge edge = GanttEdge.create().setId("e")
			.setSourceItemId("start").setSourceEndpoint(GanttEndpoint.END)
			.setTargetItemId("milestone").setTargetEndpoint(GanttEndpoint.START)
			.setTargetSymbol(ConnectorSymbol.FILLED_ARROW);
		GanttLineDecoration decoration = GanttLineDecoration.create().setId("today").setAt(30)
			.setColor("#c02020").setLabel(label);

		List<GanttRow> rows = new ArrayList<>();
		int rowCount = viewport ? 40 : 2;
		for (int n = 0; n < rowCount; n++) {
			rows.add(GanttRow.create().setId("r" + n).setLabel(Text.create().setValue("Row " + n)));
		}

		GanttLayout layout = GanttLayout.create()
			.setRowLabelMinWidth(200.0)
			.setRangeMin(0).setRangeMax(100).setZoom(1.0)
			.setFrozenRows(viewport ? 1 : 0)
			.setRootRows(rows)
			.setItems(Arrays.asList(start, milestone))
			.setEdges(Arrays.asList(edge))
			.setDecorations(Arrays.asList(decoration));
		for (GanttRow row : rows) {
			layout.addContent(row.getLabel());
		}
		layout.addContent(start.getBox());
		layout.addContent(milestone.getBox());
		layout.addContent(label);

		Diagram diagram = Diagram.create().setRoot(layout);
		if (viewport) {
			diagram.layout(new AWTContext(12f), 2000, 400);
		} else {
			diagram.layout(new AWTContext(12f), 2000, 2000);
		}
		return render(diagram);
	}

	private static void assertGanttEdgesPassThrough(Document svg) {
		List<Element> groups = elementsWithClass(svg, "tl-gantt-edges");
		assertEquals("edge group drawn", 1, groups.size());
		List<Element> paths = descendants(groups.get(0), "path");
		assertEquals("line and arrow drawn", 2, paths.size());
		for (Element path : paths) {
			assertPassThrough(path);
		}
	}

	private static void assertPassThrough(Element element) {
		assertEquals("Pointer passes through <" + element.getTagName() + ">.",
			Interactivity.POINTER_EVENTS_NONE, pointerEvents(element));
	}

	private static void assertReceives(Element element) {
		assertFalse("Pointer is received by <" + element.getTagName() + ">.",
			Interactivity.POINTER_EVENTS_NONE.equals(pointerEvents(element)));
	}

	/**
	 * The effective pointer-events value of the given element: its own or the inherited one, or
	 * <code>null</code> for the default.
	 */
	private static String pointerEvents(Element element) {
		for (Node node = element; node instanceof Element; node = node.getParentNode()) {
			Element current = (Element) node;
			if (current.hasAttribute(Interactivity.POINTER_EVENTS_ATTR)) {
				return current.getAttribute(Interactivity.POINTER_EVENTS_ATTR);
			}
		}
		return null;
	}

	private static Element textElement(Document svg, String value) {
		Element result = null;
		for (Element text : descendants(svg.getDocumentElement(), "text")) {
			if (value.equals(text.getTextContent())) {
				assertNull("Text drawn once: " + value, result);
				result = text;
			}
		}
		assertNotNull("Text drawn: " + value, result);
		return result;
	}

	/** The group drawing the edge the given decoration text belongs to. */
	private static Element edgeGroup(Element decorationText) {
		for (Node node = decorationText.getParentNode(); node instanceof Element; node = node.getParentNode()) {
			Element element = (Element) node;
			if (!descendants(element, "path").isEmpty()) {
				return element;
			}
		}
		fail("No edge drawn around the decoration.");
		return null;
	}

	private static List<Element> elementsWithClass(Document svg, String cssClass) {
		List<Element> result = new ArrayList<>();
		for (Element element : descendants(svg.getDocumentElement(), "*")) {
			if (Arrays.asList(element.getAttribute("class").split("\\s+")).contains(cssClass)) {
				result.add(element);
			}
		}
		return result;
	}

	private static List<Element> descendants(Element root, String tagName) {
		List<Element> result = new ArrayList<>();
		NodeList nodes = root.getElementsByTagName(tagName);
		for (int n = 0; n < nodes.getLength(); n++) {
			result.add((Element) nodes.item(n));
		}
		return result;
	}

	private static Document render(Diagram diagram) throws Exception {
		StringWriter buffer = new StringWriter();
		try (TagWriter tagWriter = new TagWriter(buffer);
				SvgTagWriter svgWriter = new SvgTagWriter(tagWriter)) {
			diagram.draw(svgWriter);
		}
		return DocumentBuilderFactory.newInstance().newDocumentBuilder()
			.parse(new InputSource(new StringReader(buffer.toString())));
	}

}
