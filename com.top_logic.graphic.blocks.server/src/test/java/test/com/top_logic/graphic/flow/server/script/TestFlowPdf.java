/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.graphic.flow.server.script;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.graphic.flow.data.Border;
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.graphic.flow.data.Text;
import com.top_logic.graphic.flow.server.script.FlowFactory;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.model.ModelService;

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

	/**
	 * The text of a diagram must survive the rasterization that embedding into a PDF performs.
	 */
	public void testDiagramTextSurvivesRasterization() throws Exception {
		Diagram diagram = Diagram.create()
			.setRoot(Border.create().setContent(Text.create().setValue("Hello World")));

		// execute() rather than eval(): the latter evaluates twice and compares, and two equal PDFs
		// are not equal objects.
		BinaryDataSource pdf = (BinaryDataSource) execute(search("html -> pdfFile($html)"),
			html(diagram, "TestFlowPdf-basic"));
		write(pdf, "TestFlowPdf-basic");

		String text = (String) eval("pdf -> pdf2txt($pdf)", pdf);

		assertTrue("Diagram text missing from the rendered PDF: " + text, text.contains("Hello World"));
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
	private static void write(BinaryDataSource pdf, String name) throws IOException {
		try (OutputStream out = new FileOutputStream(new File(OUT_DIR, name + ".pdf"))) {
			pdf.deliverTo(out);
		}
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
		return KBSetup.getSingleKBTest(ServiceTestSetup.createSetup(TestFlowPdf.class,
			SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE));
	}
}
