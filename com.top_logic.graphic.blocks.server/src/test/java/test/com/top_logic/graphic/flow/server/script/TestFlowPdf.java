/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.graphic.flow.server.script;

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
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.graphic.flow.data.Border;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.graphic.flow.data.SpaceDistribution;
import com.top_logic.graphic.flow.data.Text;
import com.top_logic.graphic.flow.server.script.FlowFactory;

/**
 * Test case for embedding a flow diagram into a PDF.
 *
 * <p>
 * The production use of an exported diagram is an {@code img} inside a document handed to
 * {@code pdfFile}, where it is rasterized. Rasterizing resolves the fonts a second time, so it is
 * the step at which a diagram whose text no longer matches the boxes measured for it becomes
 * visible - the SVG source alone cannot show that.
 * </p>
 *
 * <p>
 * The generated PDF is written to {@code target/TestFlowPdf-*.pdf} for visual inspection.
 * </p>
 */
public class TestFlowPdf extends AbstractSearchExpressionTest {

	private static final String OUT_DIR = "./target";

	private static final String PORTABLE = "TestFlowPdf-portable";

	/**
	 * Prefix of the messages the default error handler of Batik prints to {@link System#err}.
	 */
	private static final String[] BATIK_MESSAGE_PREFIXES = { "ERROR: ", "WARNING: " };

	private static final String TRANSLUCENT_LABEL = "Translucent";

	/**
	 * A diagram embedded into a PDF is drawn, and its text is text.
	 *
	 * <p>
	 * Do not assert through {@code pdf2txt}: it falls back to OCR, so it reports the text of a
	 * diagram whose glyphs were drawn as outlines just as readily as one drawn as text. Only
	 * reading the text operators tells the two apart.
	 * </p>
	 */
	public void testDiagramIsDrawn() throws Exception {
		Diagram diagram = Diagram.create()
			.setRoot(Border.create().setContent(Text.create().setValue("Hello World")));

		// execute() rather than eval(): the latter evaluates twice and compares, and two equal PDFs
		// are not equal objects.
		BinaryDataSource pdf = (BinaryDataSource) execute(search("html -> pdfFile($html)"),
			html(diagram, "TestFlowPdf-basic"));
		File file = write(pdf, "TestFlowPdf-basic");

		try (PDDocument document = Loader.loadPDF(file)) {
			assertTrue("Nothing was drawn into the PDF.", drawn(document));

			assertEquals("Diagram text is not text in the PDF, so it cannot be selected.",
				"Hello World", new PDFTextStripper().getText(document).trim());
		}
	}

	/**
	 * A diagram embedded without an explicit size takes the size of its {@code viewBox}.
	 */
	public void testDiagramWithoutExplicitSize() throws Exception {
		Diagram diagram = Diagram.create()
			.setRoot(Border.create().setContent(Text.create().setValue("Hello World")));

		BinaryData svg = FlowFactory.toSvg(diagram, "TestFlowPdf-unsized.svg", 12.0, null, null);
		String dataUri = "data:image/svg+xml;base64,"
			+ Base64.getEncoder().encodeToString(StreamUtilities.readAllFromStream(svg)
				.getBytes(StandardCharsets.UTF_8));
		String html = "<html><head></head><body><img src=\"" + dataUri + "\"/></body></html>";

		BinaryDataSource pdf = (BinaryDataSource) execute(search("html -> pdfFile($html)"), html);
		File file = write(pdf, "TestFlowPdf-unsized");

		try (PDDocument document = Loader.loadPDF(file)) {
			assertTrue("Nothing was drawn into the PDF.", drawn(document));
			assertEquals("Diagram text is not text in the PDF, so it cannot be selected.",
				"Hello World", new PDFTextStripper().getText(document).trim());
		}
	}

	/**
	 * The exported SVG uses SVG 1.1 and CSS2 colors only.
	 *
	 * <p>
	 * An exported diagram is rendered by Apache Batik, which supports neither colors with alpha
	 * value nor the color keyword {@code transparent} nor CSS custom properties.
	 * </p>
	 */
	public void testSvgIsPortable() throws Exception {
		String svg = exportPortableSvg();

		assertFalse("Transparent color keyword in export: " + svg, svg.contains("transparent"));
		assertFalse("CSS color with alpha value in export: " + svg, svg.contains("rgba("));
		assertFalse("CSS custom property in export: " + svg, svg.contains("var("));

		// A duplicate attribute makes the document not well-formed.
		Document document = parseXml(svg);

		assertTrue("Fill color with its opacity: " + svg,
			hasPaint(document, "fill", "#0000FF", "fill-opacity", "0.4"));
		assertTrue("Stroke color with its opacity: " + svg,
			hasPaint(document, "stroke", "#FF0000", "stroke-opacity", "0.6"));
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
	public void testPortablePdfRendersWithoutErrors() throws Exception {
		String svg = exportPortableSvg();

		List<String> messages = new ArrayList<>();
		BinaryDataSource pdf = renderPdf(svg, messages);
		assertEquals("Batik reported problems rendering the exported SVG.", List.of(), messages);

		File file = write(pdf, PORTABLE);
		try (PDDocument document = Loader.loadPDF(file)) {
			String text = new PDFTextStripper().getText(document);
			assertTrue("Diagram text not drawn into the PDF: " + text, text.contains(TRANSLUCENT_LABEL));
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

	private String exportPortableSvg() throws IOException {
		BinaryData svg = FlowFactory.toSvg(portableDiagram(), PORTABLE + ".svg", 12.0, null, null);
		String result = StreamUtilities.readAllFromStream(svg);

		new File(OUT_DIR).mkdirs();
		try (OutputStream out = new FileOutputStream(new File(OUT_DIR, PORTABLE + ".svg"))) {
			out.write(result.getBytes(StandardCharsets.UTF_8));
		}
		return result;
	}

	/**
	 * A diagram built through the {@link FlowFactory} script functions with colors that are not
	 * portable as given.
	 */
	private static Diagram portableDiagram() {
		Box translucent = FlowFactory.fill(text(TRANSLUCENT_LABEL), "rgba(0, 0, 255, 0.4)", null, null);
		Box transparent = FlowFactory.fill(text("Transparent"), "transparent", null, null);
		Box stroke = FlowFactory.border(text("Stroke"), true, true, true, true, 1.0, "rgba(255, 0, 0, 0.6)",
			null, null, null);
		Box selectable = FlowFactory.selection(text("Selectable"), null, "selectable");

		return FlowFactory.chart(FlowFactory.vertical(List.of(translucent, transparent, stroke, selectable), 10,
			SpaceDistribution.NONE, null, null), null, null);
	}

	private static Box text(String value) {
		return FlowFactory.text(value, null, null, null, null, null, null, null);
	}

	/**
	 * Renders the given SVG embedded into a PDF and collects the messages Batik reports.
	 */
	private BinaryDataSource renderPdf(String svg, List<String> messages) throws Exception {
		String dataUri = "data:image/svg+xml;base64,"
			+ Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
		String html = "<html><head></head><body><img src=\"" + dataUri
			+ "\" width=\"400\" height=\"300\"/></body></html>";

		PrintStream err = System.err;
		ByteArrayOutputStream captured = new ByteArrayOutputStream();
		System.setErr(new PrintStream(captured, true, StandardCharsets.UTF_8));
		BinaryDataSource result;
		try {
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
	 * A document embedding the given diagram as an SVG image.
	 */
	private String html(Diagram diagram, String name) throws IOException {
		BinaryData svg = FlowFactory.toSvg(diagram, name + ".svg", 12.0, null, null);
		String dataUri = "data:image/svg+xml;base64,"
			+ Base64.getEncoder().encodeToString(StreamUtilities.readAllFromStream(svg)
				.getBytes(StandardCharsets.UTF_8));

		return "<html><head></head><body><img src=\"" + dataUri + "\" width=\"400\" height=\"100\"/></body></html>";
	}

	/**
	 * Writes the given PDF for visual inspection.
	 */
	private static File write(BinaryDataSource pdf, String name) throws IOException {
		File result = new File(OUT_DIR, name + ".pdf");
		try (OutputStream out = new FileOutputStream(result)) {
			pdf.deliverTo(out);
		}
		return result;
	}

	/**
	 * Whether the document holds a drawn diagram.
	 */
	private static boolean drawn(PDDocument document) throws IOException {
		for (PDPage page : document.getPages()) {
			if (page.getResources().getXObjectNames().iterator().hasNext()) {
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
			ServiceTestSetup.createSetup(TestFlowPdf.class, getModules()));
	}
}
