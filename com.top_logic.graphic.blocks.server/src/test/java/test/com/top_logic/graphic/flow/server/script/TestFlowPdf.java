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

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

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
	 * A diagram embedded into a PDF is drawn.
	 *
	 * <p>
	 * Its text is drawn as glyph outlines rather than as text, so it cannot be selected; ticket
	 * #29426 tracks that. The assertion is prepared below.
	 * </p>
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

			// Ticket #29426: enable once the diagram is drawn into the PDF directly rather than
			// through Batik, which fills glyph outlines instead of drawing text.
			// assertEquals("Diagram text is not text in the PDF, so it cannot be selected.",
			// "Hello World", new PDFTextStripper().getText(document).trim());
		}
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
		return KBSetup.getSingleKBTest(ServiceTestSetup.createSetup(TestFlowPdf.class,
			SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE));
	}
}
