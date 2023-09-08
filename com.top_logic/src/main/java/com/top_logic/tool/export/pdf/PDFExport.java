/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export.pdf;

import java.awt.Dimension;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

import com.lowagie.text.DocumentException;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractDisplayContext;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.Media;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;

/**
 * Exporter of a {@link FormElementTemplateProvider} to PDF.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PDFExport {

	/**
	 * Default page size.
	 * 
	 * @see #getPageSize()
	 */
	public static final Dimension DEFAULT_PAGE_SIZE = new Dimension(1240, 1754);

	/**
	 * Default page resolution.
	 * 
	 * @see #getPageResolution()
	 */
	public static final float DEFAULT_PAGE_RESOLUTION = 150f;

	/**
	 * Default margins.
	 * 
	 * @see #getMargins()
	 */
	public static final int[] DEFAULT_MARGINS =
		new int[] {
			toPixel(15, DEFAULT_PAGE_RESOLUTION),
			toPixel(15, DEFAULT_PAGE_RESOLUTION),
			toPixel(15, DEFAULT_PAGE_RESOLUTION),
			toPixel(10, DEFAULT_PAGE_RESOLUTION)
		};

	private static final float INCH_MILLIES_FACTOR = 25.4F;

	private static final String STYLE_PDF_EXPORT_CSS = "/style/pdfExport.css";

	private static final String STYLE_TL_ICON_FONT_CSS = "/style/tl_iconFont.css";

	private static final String STYLE_TL_FONT_COLOR_CSS = "/style/fontColor.css";

	private static final String STYLE_FONTAWESOME_ALL_CSS = "/style/fontawesome/all.css";

	private Dimension _pageSize = DEFAULT_PAGE_SIZE;

	private float _pageResolution = DEFAULT_PAGE_RESOLUTION;

	private int[] _margins = DEFAULT_MARGINS;

	private Theme _exportTheme = ThemeFactory.getTheme();

	/**
	 * Writes the PDF export computed from the given export description to the given output.
	 * 
	 * @param context
	 *        Export context to get context informations from.
	 * @param out
	 *        {@link OutputStream} to write PDF to.
	 * @param exportDescription
	 *        Description of the PDF export.
	 * @param element
	 *        Context object for the export.
	 */
	public final void createPDFExport(DisplayContext context, OutputStream out,
			FormElementTemplateProvider exportDescription, TLObject element) throws IOException, DocumentException {
		createPDFExport(context, out, exportDescription, element, (TLClass) element.tType());
	}

	/**
	 * Writes the PDF export computed from the given export description to the given output.
	 * 
	 * @param context
	 *        Export context to get context informations from.
	 * @param out
	 *        {@link OutputStream} to write PDF to.
	 * @param exportDescription
	 *        Description of the PDF export.
	 * @param element
	 *        Context object for the export.
	 * @param type
	 *        Context type in which the export description is evaluated.
	 */
	public final void createPDFExport(DisplayContext context, OutputStream out,
			FormElementTemplateProvider exportDescription, TLObject element, TLClass type)
			throws IOException, DocumentException {
		FormEditorContext renderContext = new FormEditorContext.Builder()
			.model(element)
			.formType(type)
			.build();
		createPDFExport(context, out, exportDescription, renderContext);

	}

	/**
	 * Writes the PDF export computed from the given export description to the given output.
	 * 
	 * @param context
	 *        Export context to get context informations from.
	 * @param out
	 *        {@link OutputStream} to write PDF to.
	 * @param exportDescription
	 *        Description of the PDF export.
	 * @param renderContext
	 *        Context information about the exported object.
	 */
	public void createPDFExport(DisplayContext context, OutputStream out, FormElementTemplateProvider exportDescription,
			FormEditorContext renderContext) throws IOException, DocumentException {
		StringWriter w = new StringWriter();
		try (TagWriter tagWriter = new TagWriter(w)) {
			Media outputMedia = context.getOutputMedia();
			if (outputMedia == Media.PDF) {
				writeHTML(context, tagWriter, exportDescription, renderContext);
			} else if (!(context instanceof AbstractDisplayContext)) {
				// try with default media.
				writeHTML(context, tagWriter, exportDescription, renderContext);
			} else {
				AbstractDisplayContext dc = (AbstractDisplayContext) context;
				dc.setOutputMedia(Media.PDF);
				try {
					writeHTML(context, tagWriter, exportDescription, renderContext);
				} finally {
					dc.setOutputMedia(outputMedia);
				}
			}
		}
		writeToPDF(context, out, w.toString());
	}

	/**
	 * Writes the HTML to export.
	 */
	protected void writeHTML(DisplayContext context, TagWriter out, FormElementTemplateProvider exportDescription,
			FormEditorContext renderContext) throws IOException {
		out.beginTag(HTMLConstants.HTML);

		writeHead(context, out, exportDescription, renderContext);
		writeBody(context, out, exportDescription, renderContext);

		out.endTag(HTMLConstants.HTML);
	}

	/**
	 * Writes the HTML header to export.
	 * 
	 * @see #writeHeadContent(DisplayContext, TagWriter)
	 * @see #writeBody(DisplayContext, TagWriter, FormElementTemplateProvider, FormEditorContext)
	 */
	protected void writeHead(DisplayContext context, TagWriter out, FormElementTemplateProvider exportDescription,
			FormEditorContext renderContext) throws IOException {
		out.beginTag(HTMLConstants.HEAD);
		writeHeadContent(context, out);
		out.endTag(HTMLConstants.HEAD);
	}

	/**
	 * Writes content within the {@link HTMLConstants#HEAD} tag.
	 * 
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 * 
	 * @see #writeHead(DisplayContext, TagWriter, FormElementTemplateProvider, FormEditorContext)
	 */
	protected void writeHeadContent(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(HTMLConstants.META);
		out.writeAttribute("charset", StringServices.UTF8);
		out.endEmptyTag();

		writePDFStyles(context, out);
	}

	/**
	 * Writes style informations for the PDF.
	 * 
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	protected void writePDFStyles(DisplayContext context, TagWriter out) throws IOException {
		writePageStyles(context, out);

		Theme currentTheme = getExportTheme();
		String mergedCSS = currentTheme.getStyleSheet(
			"pdfExport.css",
			new String[] {
				currentTheme.getFileLink(STYLE_FONTAWESOME_ALL_CSS),
				currentTheme.getFileLink(STYLE_TL_ICON_FONT_CSS),
				currentTheme.getFileLink(STYLE_TL_FONT_COLOR_CSS),
				currentTheme.getFileLink(STYLE_PDF_EXPORT_CSS),

			});
		HTMLUtil.writeStylesheetRef(out, context.getContextPath(), mergedCSS);
	}

	/**
	 * Writes the inline page style attribute.
	 */
	protected void writePageStyles(DisplayContext context, TagWriter out) {
		out.beginBeginTag(HTMLConstants.STYLE_ATTR);
		out.writeAttribute(HTMLConstants.TYPE_ATTR, "text/css");
		out.endBeginTag();

		// Add page size infos and fonts
		Dimension size = getPageSize();
		int[] margins = getMargins();
		out.writeText(" @page {size: " + (int) size.getWidth() + "px " + (int) size.getHeight()
			+ "px; margin-left: " + margins[0] + "px; margin-right: " + margins[1] + "px; margin-top: "
			+ margins[2] + "px; margin-bottom: " + margins[3] + "px; marks:crop;}");

		out.endTag(HTMLConstants.STYLE_ATTR);
	}

	/**
	 * Writes the HTML body to export.
	 * 
	 * @see #writeBodyContent(DisplayContext, TagWriter, FormElementTemplateProvider,
	 *      FormEditorContext)
	 * @see #writeHead(DisplayContext, TagWriter, FormElementTemplateProvider, FormEditorContext)
	 */
	protected void writeBody(DisplayContext context, TagWriter out, FormElementTemplateProvider exportDescription,
			FormEditorContext renderContext) throws IOException {
		out.beginBeginTag(HTMLConstants.BODY);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, "pdfExport");
		out.endBeginTag();

		writeBodyContent(context, out, exportDescription, renderContext);

		out.endTag(HTMLConstants.BODY);
	}

	/**
	 * Writes content within the {@link HTMLConstants#BODY} tag.
	 * 
	 * @see #writeBody(DisplayContext, TagWriter, FormElementTemplateProvider, FormEditorContext)
	 */
	protected void writeBodyContent(DisplayContext context, TagWriter out,
			FormElementTemplateProvider exportDescription, FormEditorContext renderContext) throws IOException {
		renderContent(context, out, exportDescription, renderContext);
	}

	/**
	 * Writes content created from given {@link FormElementTemplateProvider}.
	 */
	protected void renderContent(DisplayContext context, TagWriter out, FormElementTemplateProvider exportDescription,
			FormEditorContext renderContext) throws IOException {
		exportDescription.renderPDFExport(context, out, renderContext);
	}

	/**
	 * Writes the created HTML as PDF to given {@link OutputStream}.
	 */
	protected void writeToPDF(DisplayContext context, OutputStream out, String html) throws DocumentException {
		ITextRenderer renderer = new ITextRenderer(150f / 72f, 1);
		setCustomizedUserAgentCallback(context, renderer);
		renderer.setDocumentFromString(html);
		renderer.layout();
		/* Creating PDF with parameter "finish", closes the underlying output stream. This is
		 * unwanted, e.g. when the output is a ZIP stream. */
		renderer.createPDF(out, false);
		renderer.getWriter().setCloseStream(false);
		renderer.finishPDF();
	}

	/**
	 * Sets own {@link UserAgentCallback} to transform image and CSS resource-paths.
	 */
	protected void setCustomizedUserAgentCallback(DisplayContext context, ITextRenderer renderer) {
		// get the UserAgentCallback of the renderer
		UserAgentCallback callback = renderer.getSharedContext().getUserAgentCallback();
		// create custom UserAgentCallback
		UserAgentCallback fmUserAgentCallback = new FileManagerUserAgentCallback(context.getContextPath(), callback);
		fmUserAgentCallback.setBaseURL(context.asServletContext().getRealPath("/"));
		renderer.getSharedContext().setUserAgentCallback(fmUserAgentCallback);

		ChainingReplacedElementFactory chainingFactory = new ChainingReplacedElementFactory();
		SharedContext sharedContext = renderer.getSharedContext();
		chainingFactory.getFactories().add(new SVGReplacedElementFactory());
		chainingFactory.getFactories().add(sharedContext.getReplacedElementFactory());
		sharedContext.setReplacedElementFactory(chainingFactory);
	}

	/**
	 * Theme that is used for the export.
	 */
	public Theme getExportTheme() {
		return _exportTheme;
	}

	/**
	 * Setter for {@link #getExportTheme()}.
	 */
	public void setExportTheme(Theme exportTheme) {
		_exportTheme = Objects.requireNonNull(exportTheme);
	}

	/**
	 * Gets the desired export resolution in DPI.
	 *
	 * @see #getPageSize()
	 */
	public float getPageResolution() {
		return _pageResolution;
	}

	/**
	 * Setter for {@link #getPageResolution()}.
	 */
	public void setPageResolution(float pageResolution) {
		_pageResolution = pageResolution;
	}

	/**
	 * Gets the page size in pixel.
	 *
	 * <pre>
	 * DIN A4 = 21,0 x 29,70 cm
	 *        = 8,27 x 11,69 inch
	 *        = 827 x 1169 px at 100 DPI
	 *        = 595,44 x 841,68 px at 72 DPI
	 *        = 1.240,5 x 1.753,5 px at 150 DPI
	 * </pre>
	 *
	 * @see #getPageResolution()
	 */
	public Dimension getPageSize() {
		return _pageSize;
	}

	/**
	 * Setter for {@link #getPageSize()}.
	 */
	public void setPageSize(Dimension pageSize) {
		_pageSize = Objects.requireNonNull(pageSize);
	}

	/**
	 * Computes the number of pixel from the resolution (DPI) and the number of millimeters.
	 */
	public static int toPixel(int millimeter, float resolution) {
		return (int) (millimeter * (resolution / INCH_MILLIES_FACTOR));
	}

	/**
	 * Gets the page margins in pixel in order left, right, top, bottom.
	 *
	 * @return an array with 4 values, in order left, right, top, bottom
	 */
	public int[] getMargins() {
		return _margins;
	}

	/**
	 * Setter for {@link #getMargins()}.
	 */
	public void setMargins(int[] margins) {
		Objects.requireNonNull(margins);
		if (margins.length != 4) {
			throw new IllegalArgumentException("Expected 4 coordinates: " + Arrays.toString(margins));
		}
		_margins = margins;
	}

	/**
	 * Ripped straight out of POS. Adjusted misleading Comments.
	 *
	 * The {@link UserAgentCallback} used by this component.
	 */
	public static class FileManagerUserAgentCallback implements UserAgentCallback {

		private String contextPath;

		private UserAgentCallback callback;

		/**
		 * Creates a new {@link FileManagerUserAgentCallback}.
		 */
		public FileManagerUserAgentCallback(String contextPath, UserAgentCallback callback) {
			this.contextPath = contextPath;
			this.callback = callback;
		}

		/**
		 * Transform the given uri (e.g. "/aema/image.png") into an absolute file path (e.g.
		 * "file:/D:/aema/image.png")
		 *
		 * @param uri
		 *        The uri to transform
		 * @return The transformed uri
		 */
		private String getTLPath(String uri) {
			if (uri.startsWith(contextPath)) {
				uri = uri.substring(contextPath.length());
			}
			int parameter = uri.lastIndexOf("?");
			if (parameter > 0) {
				uri = uri.substring(0, parameter);
			}
			if (uri.startsWith("data:")) {
				return uri;
			}
			if (!uri.startsWith("file:")) {
				try {
					URL resourceURL = FileManager.getInstance().getResourceUrl(uri);
					if (resourceURL != null) {
						return resourceURL.toExternalForm();
					} else {
						Logger.error("Unable to get file for " + uri, FileManagerUserAgentCallback.class);
					}
				} catch (Exception e) {
					Logger.error("Unable to get file for " + uri, e, FileManagerUserAgentCallback.class);
				}
			}
			return uri;
		}

		/**
		 * leave the URI as it is at this point.
		 *
		 * This is called internally to resolve given URIs.
		 *
		 * We just want to return the relative URIi as given for being able of resolving tl paths
		 * later on
		 *
		 * @see org.xhtmlrenderer.extend.UserAgentCallback#resolveURI(java.lang.String)
		 */
		@Override
		public String resolveURI(String uri) {
			return uri;
		}

		@Override
		public String getBaseURL() {
			return callback.getBaseURL();
		}

		@Override
		public void setBaseURL(String url) {
			callback.setBaseURL(url);
		}

		@Override
		public boolean isVisited(String uri) {
			return callback.isVisited(uri);
		}

		@Override
		public XMLResource getXMLResource(String uri) {
			return callback.getXMLResource(uri);
		}

		@Override
		public ImageResource getImageResource(String uri) {
			uri = getTLPath(uri); // transform the URI
			return callback.getImageResource(uri);
		}

		@Override
		public CSSResource getCSSResource(String uri) {
			uri = getTLPath(uri); // transform the URI
			return callback.getCSSResource(uri);
		}

		@Override
		public byte[] getBinaryResource(String uri) {
			uri = getTLPath(uri); // transform the URI
			return callback.getBinaryResource(uri);
		}

	}

	/**
	 * {@link ReplacedElementFactory} holding a sequence of {@link ReplacedElementFactory} and
	 * returning the first possible replaced element.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class ChainingReplacedElementFactory implements ReplacedElementFactory {

		private List<ReplacedElementFactory> _factories = new ArrayList<>();

		/**
		 * Modifiable sequence of the factories to call.
		 */
		public List<ReplacedElementFactory> getFactories() {
			return _factories;
		}

		@Override
		public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box,
				UserAgentCallback uac, int cssWidth, int cssHeight) {
			for (ReplacedElementFactory factory : _factories) {
				ReplacedElement element = factory.createReplacedElement(c, box, uac, cssWidth, cssHeight);
				if (element != null) {
					return element;
				}
			}
			return null;
		}

		@Override
		public void reset() {
			for (ReplacedElementFactory replacedElementFactory : _factories) {
				replacedElementFactory.reset();
			}
		}

		@Override
		public void remove(Element e) {
			for (ReplacedElementFactory replacedElementFactory : _factories) {
				replacedElementFactory.remove(e);
			}
		}

		@Override
		public void setFormSubmissionListener(FormSubmissionListener listener) {
			for (ReplacedElementFactory replacedElementFactory : _factories) {
				replacedElementFactory.setFormSubmissionListener(listener);
			}
		}
	}

}

