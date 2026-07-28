/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.layout;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.server.svg.SvgTagWriter;
import com.top_logic.react.flow.server.ui.AWTContext;
import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.SvgWriter;

import de.haumacher.msgbuf.graph.DefaultScope;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.server.io.ReaderAdapter;

/**
 * Replays a {@link Diagram} captured from a running application and renders it to SVG.
 *
 * <p>
 * The client transfers a diagram as msgbuf JSON and lays it out itself, and the box geometry
 * ({@code x}, {@code y}, {@code width}, {@code height}) is declared {@code transient}. The
 * transferred JSON is therefore exactly the input of {@link Diagram#layout(RenderContext)}: a
 * layout observed in the browser can be reproduced from that JSON alone, without rebuilding the
 * model that produced it.
 * </p>
 *
 * <p>
 * To capture a diagram, wrap the client entry point in the browser console and reload the view:
 * </p>
 *
 * <pre>
 * const mount = window.GWT_FlowDiagram.mount;
 * window.GWT_FlowDiagram.mount = (c, id, w, p, json) =&gt; (copy(json), mount(c, id, w, p, json));
 * </pre>
 *
 * <p>
 * A capture carries the labels and images of the application it came from. Before it is checked in
 * as a test resource it is passed through {@link DiagramScrubber}, which replaces that content
 * without moving the layout. Each replayed diagram is written to
 * {@code target/TestDiagramReplay-*.svg} for visual inspection.
 * </p>
 */
public class TestDiagramReplay extends TestCase {

	private static final String OUT_DIR = "./target";

	/**
	 * Path commands of an SVG {@code d} attribute, as written by {@link SvgTagWriter}.
	 */
	private static final Pattern PATH_COMMAND = Pattern.compile("([MLHV])\\s*([-\\d.eE]+),?([-\\d.eE]*)");

	/**
	 * A subtree bus must not run through the node it belongs to.
	 *
	 * <p>
	 * Minimized from a captured diagram: a node bearing a subtree sits in a row-wise sub-grid, and
	 * its content extends to the right of its connection anchor. The bus that carries its subtree
	 * is placed from the sub-grid's column advance, which follows the anchor rather than the box,
	 * so the bus ends up inside the node's own box and continues down across the siblings below
	 * it.
	 * </p>
	 */
	public void testSubtreeBusOverlapsItsNode() throws IOException {
		String svg = replay("subtree-bus-overlap.json");

		// The nodes along the parent chain are laid out correctly and show that the check passes
		// for a sound layout.
		assertNoBusInsideNode(svg, "K4278");
		assertNoBusInsideNode(svg, "NG80-El.177");
		assertNoBusInsideNode(svg, "S8911");

		assertNoBusInsideNode(svg, "Y3542");
	}

	/**
	 * Asserts that no vertical line starts inside the box of the node carrying the given label and
	 * continues past its lower edge.
	 *
	 * <p>
	 * Verticals that stay within the box are the node's own internal rules; a vertical leaving it
	 * downwards is the bus of the node's subtree, which belongs beside the node, not on top of it.
	 * </p>
	 */
	private void assertNoBusInsideNode(String svg, String label) throws IOException {
		Geometry geometry = Geometry.of(svg);

		double[] text = geometry.text(label);
		assertNotNull("No text '" + label + "' in the rendered diagram.", text);
		double[] node = geometry.enclosingBox(text[0], text[1]);
		assertNotNull("No box around the text '" + label + "'.", node);

		for (double[] line : geometry.verticals()) {
			double top = Math.min(line[1], line[2]);
			double bottom = Math.max(line[1], line[2]);
			boolean insideHorizontally = line[0] > node[0] + 0.5 && line[0] < node[2] - 0.5;
			boolean leavesDownwards = top < node[3] - 0.5 && bottom > node[3] + 0.5;
			assertFalse(
				"The subtree bus at x=" + line[0] + " runs through the box of '" + label + "' ("
					+ node[0] + "," + node[1] + ")-(" + node[2] + "," + node[3] + "), "
					+ (node[2] - line[0]) + "px inside its right edge.",
				insideHorizontally && leavesDownwards);
		}
	}

	/**
	 * Reads the diagram from the given test resource, lays it out and renders it to SVG.
	 *
	 * @param resource
	 *        Name of a captured msgbuf JSON resource, relative to this class.
	 * @return The rendered SVG.
	 */
	private String replay(String resource) throws IOException {
		Diagram diagram = read(resource);

		// Mirror the client, which measures with the canvas default font: the AWT size is given in
		// points, so the context reports (and the SVG declares) DEFAULT_FONT_SIZE_PX pixels.
		RenderContext context = new AWTContext((float) (RenderContext.DEFAULT_FONT_SIZE_PX * AWTContext.PT_PER_PX));
		diagram.layout(context);

		TagWriter out = new TagWriter();
		SvgWriter svgOut = new SvgTagWriter(out);
		diagram.draw(svgOut, context, null);
		String svg = XMLPrettyPrinter.prettyPrint(out.toString());

		String name = "TestDiagramReplay-" + resource.replaceAll("\\.json$", "") + ".svg";
		try (FileWriter w = new FileWriter(new File(OUT_DIR, name), StandardCharsets.UTF_8)) {
			w.write(svg);
		}
		return svg;
	}

	private Diagram read(String resource) throws IOException {
		try (InputStream in = TestDiagramReplay.class.getResourceAsStream(resource)) {
			assertNotNull("Missing test resource '" + resource + "'.", in);
			return Diagram.readDiagram(new DefaultScope(1, 0),
				new JsonReader(new ReaderAdapter(new InputStreamReader(in, StandardCharsets.UTF_8))));
		}
	}

	/**
	 * The absolute geometry of a rendered diagram.
	 *
	 * <p>
	 * Resolves the nested {@code translate} transforms, so that a box can be related to a line
	 * regardless of how the boxes happen to be grouped.
	 * </p>
	 */
	private static final class Geometry {

		private static final Pattern TRANSLATE = Pattern.compile("translate\\(([-\\d.eE]+),([-\\d.eE]+)\\)");

		/** Label, baseline X, baseline Y. */
		private final List<Object[]> _texts = new ArrayList<>();

		/** Left, top, right, bottom of each closed outline. */
		private final List<double[]> _boxes = new ArrayList<>();

		/** X, start Y, end Y of each vertical line segment. */
		private final List<double[]> _verticals = new ArrayList<>();

		static Geometry of(String svg) throws IOException {
			Geometry result = new Geometry();
			try {
				Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)))
					.getDocumentElement();
				result.walk(root, 0, 0);
			} catch (Exception ex) {
				throw new IOException("Cannot parse the rendered SVG.", ex);
			}
			return result;
		}

		private void walk(Element element, double dx, double dy) {
			NodeList children = element.getChildNodes();
			for (int n = 0, size = children.getLength(); n < size; n++) {
				Node child = children.item(n);
				if (!(child instanceof Element)) {
					continue;
				}
				Element el = (Element) child;
				switch (el.getTagName()) {
					case "g": {
						double[] offset = translation(el);
						walk(el, dx + offset[0], dy + offset[1]);
						break;
					}
					case "text": {
						_texts.add(new Object[] { el.getTextContent(),
							Double.parseDouble(el.getAttribute("x")) + dx,
							Double.parseDouble(el.getAttribute("y")) + dy });
						break;
					}
					case "path": {
						addPath(el.getAttribute("d"), dx, dy);
						break;
					}
					default:
						walk(el, dx, dy);
				}
			}
		}

		private static double[] translation(Element el) {
			Matcher matcher = TRANSLATE.matcher(el.getAttribute("transform"));
			return matcher.find()
				? new double[] { Double.parseDouble(matcher.group(1)), Double.parseDouble(matcher.group(2)) }
				: new double[] { 0, 0 };
		}

		private void addPath(String d, double dx, double dy) {
			Matcher matcher = PATH_COMMAND.matcher(d);
			double x = 0, y = 0;
			double left = Double.MAX_VALUE, top = Double.MAX_VALUE;
			double right = -Double.MAX_VALUE, bottom = -Double.MAX_VALUE;
			boolean any = false;
			List<double[]> verticals = new ArrayList<>();
			while (matcher.find()) {
				double a = Double.parseDouble(matcher.group(2));
				switch (matcher.group(1)) {
					case "M":
					case "L":
						x = a + dx;
						y = Double.parseDouble(matcher.group(3)) + dy;
						break;
					case "H":
						x = a + dx;
						break;
					case "V": {
						double next = a + dy;
						verticals.add(new double[] { x, y, next });
						y = next;
						break;
					}
					default:
						continue;
				}
				any = true;
				left = Math.min(left, x);
				top = Math.min(top, y);
				right = Math.max(right, x);
				bottom = Math.max(bottom, y);
			}
			if (!any) {
				return;
			}
			if (d.indexOf('z') >= 0) {
				_boxes.add(new double[] { left, top, right, bottom });
			} else {
				_verticals.addAll(verticals);
			}
		}

		/**
		 * Baseline position of the text with the given content, or {@code null} if there is none.
		 */
		double[] text(String label) {
			for (Object[] text : _texts) {
				if (label.equals(text[0])) {
					return new double[] { (Double) text[1], (Double) text[2] };
				}
			}
			return null;
		}

		/**
		 * The largest closed outline around the given position, or {@code null} if there is none.
		 *
		 * <p>
		 * The largest one is the box of the node as a whole; smaller ones are the outlines of its
		 * inner structure.
		 * </p>
		 */
		double[] enclosingBox(double x, double y) {
			double[] result = null;
			double area = 0;
			for (double[] box : _boxes) {
				// The Y is a text baseline, so the top edge may sit slightly above it.
				if (box[0] <= x && x <= box[2] && box[1] - 16 <= y && y <= box[3]) {
					double size = (box[2] - box[0]) * (box[3] - box[1]);
					if (size > area) {
						area = size;
						result = box;
					}
				}
			}
			return result;
		}

		List<double[]> verticals() {
			return _verticals;
		}
	}
}
