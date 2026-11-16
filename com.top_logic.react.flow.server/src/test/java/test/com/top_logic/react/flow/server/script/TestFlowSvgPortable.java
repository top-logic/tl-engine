/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.script;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.GanttDecoration;
import com.top_logic.react.flow.data.GanttItem;
import com.top_logic.react.flow.data.GanttRow;
import com.top_logic.react.flow.data.GraphEdge;
import com.top_logic.react.flow.data.SpaceDistribution;
import com.top_logic.react.flow.server.script.FlowFactory;

/**
 * Test case for the portability of a diagram exported by
 * {@link FlowFactory#toSvg(Diagram, String, double, Double, Double)}.
 *
 * <p>
 * An exported diagram is embedded into documents rendered by Apache Batik (e.g. through
 * {@code pdfFile}), which supports SVG 1.1 and CSS2 only. The diagram of this test combines the
 * colors the drawing code produces on its own (the default color of a Gantt range decoration, the
 * transparent click target of a selectable graph edge) with colors given by a script.
 * </p>
 *
 * <p>
 * The generated files are written to {@code target/TestFlowSvgPortable.svg} and
 * {@code target/TestFlowSvgPortable.pdf} for visual inspection.
 * </p>
 */
public class TestFlowSvgPortable extends AbstractSearchExpressionTest {

	private static final String OUT_DIR = "./target";

	private static final String NAME = "TestFlowSvgPortable";

	/**
	 * Prefix of the messages the default error handler of Batik prints to {@link System#err}.
	 */
	private static final String[] BATIK_MESSAGE_PREFIXES = { "ERROR: ", "WARNING: " };

	private static final String BOX_LABEL = "Translucent";

	/**
	 * The exported SVG uses SVG 1.1 and CSS2 colors only.
	 */
	public void testSvgIsPortable() throws Exception {
		String svg = exportSvg();

		assertFalse("Transparent color keyword in export: " + svg, svg.contains("transparent"));
		assertFalse("CSS color with alpha value in export: " + svg, svg.contains("rgba("));
		assertFalse("CSS custom property in export: " + svg, svg.contains("var("));

		// A duplicate attribute makes the document not well-formed.
		Document document = parseXml(svg);

		assertTrue("Default range decoration color with its opacity: " + svg,
			hasPaint(document, "fill", "#FFDC78", "fill-opacity", "0.349"));
		assertTrue("Script range decoration color with its opacity: " + svg,
			hasPaint(document, "fill", "#FF0000", "fill-opacity", "0.6"));
		assertTrue("Box fill with its opacity: " + svg,
			hasPaint(document, "fill", "#0000FF", "fill-opacity", "0.4"));
	}

	/**
	 * The exported SVG renders through {@code pdfFile} without errors reported by Batik.
	 *
	 * <p>
	 * Batik does not fail for an SVG it cannot render: its default error handler prints the problem
	 * to {@link System#err} and the image stays empty. The test therefore watches the error output
	 * and additionally checks that the diagram text is drawn.
	 * </p>
	 */
	public void testPdfRendersWithoutErrors() throws Exception {
		String svg = exportSvg();

		List<String> messages = new ArrayList<>();
		BinaryDataSource pdf = renderPdf(svg, messages);
		assertEquals("Batik reported problems rendering the exported SVG.", List.of(), messages);

		File file = write(pdf);
		try (PDDocument document = Loader.loadPDF(file)) {
			String text = new PDFTextStripper().getText(document);
			assertTrue("Diagram text not drawn into the PDF: " + text, text.contains(BOX_LABEL));
		}
	}

	/**
	 * Proves that {@link #renderPdf(String, List)} detects an SVG that Batik cannot render.
	 */
	public void testDetectsBatikProblems() throws Exception {
		String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"10\" height=\"10\""
			+ " viewBox=\"0 0 10 10\"><rect x=\"0\" y=\"0\" width=\"10\" height=\"10\""
			+ " fill=\"rgba(1, 2, 3, 0.5)\"/></svg>";

		List<String> messages = new ArrayList<>();
		renderPdf(svg, messages);
		assertFalse("A color with alpha value is not reported by Batik.", messages.isEmpty());
	}

	private String exportSvg() throws IOException {
		BinaryData svg = FlowFactory.toSvg(diagram(), NAME + ".svg", 12.0, null, null);
		String result = StreamUtilities.readAllFromStream(svg);

		new File(OUT_DIR).mkdirs();
		try (OutputStream out = new FileOutputStream(new File(OUT_DIR, NAME + ".svg"))) {
			out.write(result.getBytes(StandardCharsets.UTF_8));
		}
		return result;
	}

	/**
	 * A diagram built through the {@link FlowFactory} script functions.
	 */
	private static Diagram diagram() {
		String row = "row";
		GanttRow ganttRow = FlowFactory.ganttRow(row, text("Row"), null, null, null, null, null, null);
		GanttDecoration defaultColor =
			FlowFactory.ganttRangeDeco("default", 10, 30, null, text("Default"), null, null, null);
		GanttDecoration scriptColor =
			FlowFactory.ganttRangeDeco("script", 50, 70, "rgba(255, 0, 0, 0.6)", text("Script"), null, null, null);
		GanttItem span = FlowFactory.ganttSpan("span", row, text("Span"), 20, 60,
			null, null, null, null, null, null, null, null, null, null);
		Box gantt = FlowFactory.gantt(List.of(ganttRow), List.of(span), null,
			List.of(defaultColor, scriptColor), 0, 100, null, null, null);

		Box source = FlowFactory.border(text("Source"), true, true, true, true, 1.0, "black", null, null, null);
		Box target = FlowFactory.border(text("Target"), true, true, true, true, 1.0, "black", null, null, null);
		GraphEdge edge =
			FlowFactory.graphEdge(source, target, 0, null, null, "black", 1.0, null, null, true, "edge");
		Box graph = FlowFactory.graphLayout(List.of(source, target), List.of(edge), 60, 30, null, null);

		Box translucent = FlowFactory.fill(text(BOX_LABEL), "rgba(0, 0, 255, 0.4)", null, null);

		return FlowFactory.chart(FlowFactory.vertical(List.of(gantt, graph, translucent), 10, SpaceDistribution.NONE, null,
			null),
			null, null, false);
	}

	private static Box text(String value) {
		return FlowFactory.text(value, null, null, null, 0, null, null, null, false);
	}

	/**
	 * Renders the given SVG embedded into a PDF and collects the messages Batik reports.
	 */
	private BinaryDataSource renderPdf(String svg, List<String> messages) throws Exception {
		String dataUri = "data:image/svg+xml;base64,"
			+ Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
		String html = "<html><head></head><body><img src=\"" + dataUri
			+ "\" width=\"600\" height=\"400\"/></body></html>";

		PrintStream err = System.err;
		ByteArrayOutputStream captured = new ByteArrayOutputStream();
		System.setErr(new PrintStream(captured, true, StandardCharsets.UTF_8));
		BinaryDataSource result;
		try {
			// execute() rather than eval(): the latter evaluates twice and compares, and two equal
			// PDFs are not equal objects.
			result = (BinaryDataSource) execute(search("html -> pdfFile($html)"), html);
		} finally {
			System.setErr(err);
		}

		String output = captured.toString(StandardCharsets.UTF_8);
		err.print(output);
		for (String line : output.split("\\R")) {
			for (String prefix : BATIK_MESSAGE_PREFIXES) {
				if (line.startsWith(prefix)) {
					messages.add(line);
				}
			}
		}
		return result;
	}

	private static File write(BinaryDataSource pdf) throws IOException {
		File result = new File(OUT_DIR, NAME + ".pdf");
		try (OutputStream out = new FileOutputStream(result)) {
			pdf.deliverTo(out);
		}
		return result;
	}

	private static Document parseXml(String svg) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		return factory.newDocumentBuilder().parse(new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * Whether some element of the document carries the given paint with the given opacity.
	 */
	private static boolean hasPaint(Document document, String paintAttr, String paint, String opacityAttr,
			String opacity) {
		NodeList elements = document.getElementsByTagNameNS("*", "*");
		for (int n = 0, cnt = elements.getLength(); n < cnt; n++) {
			Element element = (Element) elements.item(n);
			if (paint.equals(element.getAttribute(paintAttr)) && opacity.equals(element.getAttribute(opacityAttr))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The {@link Test} suite of this test case.
	 *
	 * <p>
	 * Rendering does not touch the database, so the default one is enough; the inherited suite
	 * would repeat the test for every configured database.
	 * </p>
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(TestFlowSvgPortable.class, getModules()));
	}
}
