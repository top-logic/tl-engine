/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.module.services.ServletContextService;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DummyControlScope;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.mig.html.Media;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLContext;

/**
 * A {@link GenericMethod} implementation that generates a PDF from HTML content.
 *
 * <p>
 * This function converts HTML content (either as String or HTMLFragment) into PDF binary data
 * using the Flying Saucer library (ITextRenderer). The HTML must be well-formed XHTML for proper
 * PDF generation.
 * </p>
 *
 * <p>
 * Usage examples:
 * </p>
 *
 * <pre>
 * // String literal with default filename "document.pdf"
 * pdfFile("""
 *   &lt;html&gt;
 *     &lt;body&gt;
 *       &lt;h1&gt;My PDF Document&lt;/h1&gt;
 *       &lt;p&gt;This is generated from HTML.&lt;/p&gt;
 *     &lt;/body&gt;
 *   &lt;/html&gt;
 * """)
 *
 * // HTMLFragment literal with custom filename
 * pdfFile({{{
 *   &lt;html&gt;
 *     &lt;body&gt;
 *       &lt;h1&gt;Invoice&lt;/h1&gt;
 *       &lt;p&gt;Amount: ${$invoice.get('amount')}&lt;/p&gt;
 *     &lt;/body&gt;
 *   &lt;/html&gt;
 * }}}, "invoice-2024-001")
 * </pre>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PdfFile extends GenericMethod {

	/**
	 * Default resolution for PDF rendering in DPI (dots per inch).
	 */
	private static final float DEFAULT_PDF_RESOLUTION = 150f;

	/**
	 * Conversion factor from DPI to points (72 points per inch).
	 */
	private static final float DPI_TO_POINTS_FACTOR = 72f;

	/**
	 * Number of anti-aliasing passes for better rendering quality.
	 */
	private static final int ANTI_ALIASING_PASSES = 1;

	/**
	 * Default page width in pixels at 150 DPI (A4 width: 210mm ≈ 1240px).
	 */
	private static final int DEFAULT_PAGE_WIDTH = 1240;

	/**
	 * Default page height in pixels at 150 DPI (A4 height: 297mm ≈ 1754px).
	 */
	private static final int DEFAULT_PAGE_HEIGHT = 1754;

	/**
	 * Default margin in millimeters.
	 */
	private static final int DEFAULT_MARGIN_MM = 15;

	/**
	 * Conversion factor from millimeters to inches.
	 */
	private static final float INCH_MILLIES_FACTOR = 25.4F;

	/**
	 * Creates a {@link PdfFile} expression.
	 */
	protected PdfFile(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new PdfFile(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.BINARY_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		// Extract the HTML content from the first argument (can be String or HTMLFragment)
		Object htmlArg = arguments[0];

		// Return null if no HTML content provided
		if (htmlArg == null) {
			return null;
		}

		// Convert to HTML string
		String html;
		try {
			html = toHtmlString(htmlArg);
		} catch (IOException ex) {
			throw new RuntimeException("Failed to render HTML content: " + ex.getMessage(), ex);
		}

		// Return null if HTML is empty after conversion
		if (html == null || html.trim().isEmpty()) {
			return null;
		}

		// Extract the optional filename from the second argument, default to "document.pdf"
		String filename = asString(arguments[1]);
		if (filename == null || filename.trim().isEmpty()) {
			filename = "document.pdf";
		}

		// Ensure the filename has .pdf extension
		if (!filename.toLowerCase().endsWith(".pdf")) {
			filename = filename + ".pdf";
		}

		// Extract optional rendering parameters
		int pageWidth = asInt(arguments[2], DEFAULT_PAGE_WIDTH);
		int pageHeight = asInt(arguments[3], DEFAULT_PAGE_HEIGHT);
		float resolution = asFloat(arguments[4], DEFAULT_PDF_RESOLUTION);
		int marginLeft = asInt(arguments[5], toPixel(DEFAULT_MARGIN_MM, resolution));
		int marginRight = asInt(arguments[6], toPixel(DEFAULT_MARGIN_MM, resolution));
		int marginTop = asInt(arguments[7], toPixel(DEFAULT_MARGIN_MM, resolution));
		int marginBottom = asInt(arguments[8], toPixel(10, resolution)); // Default bottom margin is 10mm

		try {
			// Convert HTML to PDF with specified parameters and return as binary data
			byte[] pdfBytes = convertHtmlToPdf(html, pageWidth, pageHeight, resolution,
				marginLeft, marginRight, marginTop, marginBottom);

			// Wrap as BinaryData with appropriate content type and filename
			return BinaryDataFactory.createBinaryData(pdfBytes, "application/pdf", filename);

		} catch (DocumentException | IOException ex) {
			// Log error and return null on conversion failure
			throw new RuntimeException("Failed to generate PDF from HTML: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Computes the number of pixels from the resolution (DPI) and the number of millimeters.
	 */
	private static int toPixel(int millimeter, float resolution) {
		return (int) (millimeter * (resolution / INCH_MILLIES_FACTOR));
	}

	/**
	 * Converts the input argument to an HTML string.
	 *
	 * @param htmlArg
	 *        Either a String or an HTMLFragment.
	 * @return The HTML content as a string.
	 * @throws IOException
	 *         If rendering the HTMLFragment fails.
	 */
	private String toHtmlString(Object htmlArg) throws IOException {
		if (htmlArg instanceof String) {
			// Direct string input
			return (String) htmlArg;
		} else if (htmlArg instanceof HTMLFragment) {
			// Render HTMLFragment to string
			return renderFragmentToString((HTMLFragment) htmlArg);
		} else {
			// Try to convert to string as fallback
			return asString(htmlArg);
		}
	}

	/**
	 * Renders an {@link HTMLFragment} to a string.
	 *
	 * @param fragment
	 *        The fragment to render.
	 * @return The rendered HTML as a string.
	 * @throws IOException
	 *         If rendering fails.
	 */
	private String renderFragmentToString(HTMLFragment fragment) throws IOException {
		StringWriter htmlBuffer = new StringWriter();
		DisplayContext context = createDisplayContext();

		try (TagWriter tagWriter = new TagWriter(htmlBuffer)) {
			fragment.write(context, tagWriter);
		}

		return htmlBuffer.toString();
	}

	/**
	 * Creates a display context for rendering HTMLFragment.
	 */
	private DisplayContext createDisplayContext() {
		// Note: Must set up a separate display context to allow rendering of fragments.
		// The "current" display context is not available for control rendering,
		// since the current session is not in rendering mode.
		DisplayContext context = new DummyDisplayContext(
			ServletContextService.getInstance().getServletContext())
				.initOutputMedia(Media.PDF)
				.initContextPath(
					ServletContextService.getInstance().getServletContext().getContextPath());
		context.initScope(new DummyControlScope());
		context.installSubSessionContext(TLContext.getContext());
		return context;
	}

	/**
	 * Converts HTML string to PDF bytes using Flying Saucer (ITextRenderer).
	 *
	 * @param html
	 *        The HTML content to convert to PDF. Must be well-formed XHTML.
	 * @param pageWidth
	 *        The page width in pixels.
	 * @param pageHeight
	 *        The page height in pixels.
	 * @param resolution
	 *        The resolution in DPI.
	 * @param marginLeft
	 *        The left margin in pixels.
	 * @param marginRight
	 *        The right margin in pixels.
	 * @param marginTop
	 *        The top margin in pixels.
	 * @param marginBottom
	 *        The bottom margin in pixels.
	 * @return The PDF content as byte array.
	 * @throws DocumentException
	 *         If PDF generation fails.
	 * @throws IOException
	 *         If I/O operations fail.
	 */
	private byte[] convertHtmlToPdf(String html, int pageWidth, int pageHeight, float resolution,
			int marginLeft, int marginRight, int marginTop, int marginBottom)
			throws DocumentException, IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			// Create ITextRenderer with resolution conversion factor
			// Factor converts DPI to points: resolution DPI / 72 points per inch
			float resolutionFactor = resolution / DPI_TO_POINTS_FACTOR;
			ITextRenderer renderer = new ITextRenderer(resolutionFactor, ANTI_ALIASING_PASSES);

			// Inject page size and margins into HTML if not already present
			String htmlWithPageStyles = injectPageStyles(html, pageWidth, pageHeight,
				marginLeft, marginRight, marginTop, marginBottom);

			// Set the HTML document from the string
			renderer.setDocumentFromString(htmlWithPageStyles);

			// Layout the document for rendering
			renderer.layout();

			// Create the PDF without closing the output stream
			// (finish=false means don't close the stream)
			renderer.createPDF(out, false);

			// Ensure the stream isn't closed by the renderer
			renderer.getWriter().setCloseStream(false);

			// Finalize the PDF generation
			renderer.finishPDF();

			return out.toByteArray();

		} finally {
			// Ensure output stream is closed
			out.close();
		}
	}

	/**
	 * Injects CSS @page styles into the HTML to define page size and margins.
	 *
	 * @param html
	 *        The original HTML content.
	 * @param pageWidth
	 *        The page width in pixels.
	 * @param pageHeight
	 *        The page height in pixels.
	 * @param marginLeft
	 *        The left margin in pixels.
	 * @param marginRight
	 *        The right margin in pixels.
	 * @param marginTop
	 *        The top margin in pixels.
	 * @param marginBottom
	 *        The bottom margin in pixels.
	 * @return HTML with injected page styles.
	 */
	private String injectPageStyles(String html, int pageWidth, int pageHeight,
			int marginLeft, int marginRight, int marginTop, int marginBottom) {
		String pageStyle = "<style>@page {size: " + pageWidth + "px " + pageHeight
			+ "px; margin-left: " + marginLeft + "px; margin-right: " + marginRight
			+ "px; margin-top: " + marginTop + "px; margin-bottom: " + marginBottom
			+ "px; marks:crop;}</style>";

		// Try to inject before </head> if present
		if (html.contains("</head>")) {
			return html.replace("</head>", pageStyle + "</head>");
		}

		// Otherwise inject a complete head section after <html> if present
		if (html.contains("<html>")) {
			return html.replace("<html>", "<html><head>" + pageStyle + "</head>");
		}

		// As a fallback, construct valid HTML from content-only input
		// This happens when the user provides only content without HTML structure
		return "<html><head>" + pageStyle + "</head><body>" + html + "</body></html>";
	}

	@Override
	public boolean isSideEffectFree() {
		// PDF generation is a pure function with no side effects
		return true;
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating a {@link PdfFile} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<PdfFile> {

		/** Description of parameters for a {@link PdfFile}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("html")
			.optional("name")
			.optional("pageWidth")
			.optional("pageHeight")
			.optional("resolution")
			.optional("marginLeft")
			.optional("marginRight")
			.optional("marginTop")
			.optional("marginBottom")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public PdfFile build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new PdfFile(getConfig().getName(), args);
		}

	}

}
