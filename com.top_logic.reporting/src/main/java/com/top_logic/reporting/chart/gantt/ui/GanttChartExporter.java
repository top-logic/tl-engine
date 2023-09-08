/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.resource.XMLResource;

import com.lowagie.text.DocumentException;

import com.top_logic.base.chart.util.ChartUtil;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.reporting.chart.gantt.model.GanttChartSettings;

/**
 * Helper class to export the Gantt chart.
 * 
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class GanttChartExporter implements HTMLConstants {

	/**
	 * Singleton {@link GanttChartExporter} instance.
	 */
	public static final GanttChartExporter INSTANCE = new GanttChartExporter();

	/**
	 * Creates a {@link GanttChartExporter}.
	 */
	protected GanttChartExporter() {
		// Singleton constructor.
	}

	/**
	 * Extracts relevant infos from the given display context which are needed for PDF export.
	 */
	public static Pair<String, String> extractContextInfoForExport(DisplayContext context) {
		return new Pair<>(context.getContextPath(), context.asServletContext().getRealPath("/"));
	}

	/**
	 * Generates the PDF file representing the export of the gantt chart.
	 */
	public File generateChartPDF(Pair<String, String> contextInfo, List<ImageData> imageDatas,
			GanttChartSettings filterSettings, boolean fitToOnePage, int chartHeight, int chartWidth,
			boolean isNativeFormat) throws IOException, DocumentException {
		boolean portraitMode = usePortraitMode(fitToOnePage, chartHeight, chartWidth);

		// create the html with the image
		String inputHTML = createHTMLDocument(imageDatas, filterSettings, portraitMode, fitToOnePage, isNativeFormat, chartHeight, chartWidth);

		// create the PDF from the html
		File outputPDF = convertToPDF(contextInfo, inputHTML, portraitMode, fitToOnePage, chartHeight, chartWidth, isNativeFormat);
		return outputPDF;
	}

	/**
	 * Decide whether to use "landscape" or "portrait" mode
	 */
	protected boolean usePortraitMode(boolean fitToOnePage, int chartHeight, int chartWidth) {
		if (fitToOnePage) {
			return chartHeight > chartWidth;
		} else {
			return usePortraitModeMultiPageExport(chartWidth);
		}
	}

	/**
	 * For multi page export the decision if using portrait or landscape mode is made by comparing
	 * chart width to horizontally available pixel on one page in portrait or landscape mode:
	 * 
	 * <p>
	 * Chart-width is smaller than portrait-page-width --> use portrait
	 * </p>
	 * 
	 * <p>
	 * Chart-width is between portrait-page-width and landscape-page-width --> use portrait
	 * </p>
	 * 
	 * <p>
	 * Chart-width is greater than landscape-page-width --> use landscape
	 * </p>
	 * 
	 * @param chartWidth
	 * 		Width of the gantt chart.
	 * @return Returns <code>true</code> or <code>false</code> to indicate which orientation to use.
	 */
	public boolean usePortraitModeMultiPageExport(int chartWidth) {
		boolean usePortraitMode = false;
		if (chartWidth < getMaxWidthPortrait()
			|| (chartWidth > getMaxWidthPortrait() && chartWidth < getMaxWidthLandscape())) {
			usePortraitMode = true;
		}
		return usePortraitMode;
	}

	/**
	 * Creates the HTML document that has to be converted to PDF.
	 *
	 * @return The created HTML string.
	 */
	protected String createHTMLDocument(List<ImageData> imageDatas, GanttChartSettings filterSettings,
			boolean portraitMode, boolean fitToOnePage, boolean isNativeFormat, int chartHeight, int chartWidth) throws IOException {
		StringWriter stringWriter = new StringWriter();
		writeHTMLPart(stringWriter, imageDatas, filterSettings, portraitMode, fitToOnePage, isNativeFormat, chartHeight,
			chartWidth);
		return stringWriter.toString();
	}

	/**
	 * Adds CSS information to the given {@link StringWriter}.
	 *
	 * @param stringWriter
	 *        The {@link StringWriter} to add information to.
	 */
	protected void writeCSSPart(StringWriter stringWriter, GanttChartSettings filterSettings, boolean portraitMode,
			boolean isNativeFormat, int chartHeight, int chartWidth) {
		TagUtil.beginBeginTag(stringWriter, STYLE_ATTR);
		TagUtil.writeAttribute(stringWriter, TYPE_ATTR, "text/css");
		TagUtil.endBeginTag(stringWriter);
		if(isNativeFormat){
			int margin = 20;// for margin/padding of main table containing header and footer
			int chartExportWidth = computeExportChartWidth(chartWidth, filterSettings) + margin;
			int chartExportHeight = computeExportChartHeight(chartHeight, filterSettings) + margin;
			TagUtil.writeText(stringWriter, "@page {size: " + chartExportWidth + "px " + chartExportHeight + "px;");
		}else{
			TagUtil.writeText(stringWriter, "@page {size: A3 ");
		}
		if (portraitMode) {
			TagUtil.writeText(stringWriter, "portrait;");
		}
		else {
			TagUtil.writeText(stringWriter, "landscape;");
		}
		if (isNativeFormat) {
			TagUtil.writeText(stringWriter, "margin: 0px; marks:crop;}");
		} else {
			TagUtil.writeText(stringWriter, "margin: 20px; marks:crop;}");
		}
		TagUtil.endTag(stringWriter, STYLE_ATTR);
	}

	/**
	 * Computes the width of the export chart in respect to the header and footer.
	 */
	protected int computeExportChartWidth(int chartWidth, GanttChartSettings filterSettings) {
		return chartWidth;
	}

	/**
	 * Computes the height of the export chart in respect to the header and footer.
	 */
	protected int computeExportChartHeight(int chartHeight, GanttChartSettings filterSettings) {
		return chartHeight;
	}

	/**
	 * Adds HTML information to the given {@link StringWriter}.
	 * 
	 * @param stringWriter
	 *        The {@link StringWriter} to add information to.
	 */
	protected void writeHTMLPart(StringWriter stringWriter, List<ImageData> imageDatas,
			GanttChartSettings filterSettings, boolean portraitMode, boolean fitToOnePage, boolean isNativeFormat,
			int chartHeight, int chartWidth) throws IOException {
		TagUtil.writeXMLHeader(stringWriter, StringServices.UTF8);
		TagUtil.beginTag(stringWriter, HTML);
		TagUtil.beginTag(stringWriter, HEAD);
		writeCSSPart(stringWriter, filterSettings, portraitMode, isNativeFormat, chartHeight, chartWidth);
		TagUtil.endTag(stringWriter, HEAD);
		TagUtil.beginTag(stringWriter, BODY);
		writeHTMLContent(stringWriter, imageDatas, filterSettings, portraitMode, fitToOnePage);
		TagUtil.endTag(stringWriter, BODY);
		TagUtil.endTag(stringWriter, HTML);
	}

	/**
	 * Adds a html table containing a centered tempImage-tag to the given {@link StringWriter}.
	 *
	 * @param stringWriter
	 *        The {@link StringWriter} to add information to.
	 */
	protected void writeHTMLContent(StringWriter stringWriter, List<ImageData> imageDatas,
			GanttChartSettings filterSettings, boolean portraitMode, boolean fitToOnePage) throws IOException {
		TagUtil.beginBeginTag(stringWriter, TABLE);
		TagUtil.writeAttribute(stringWriter, WIDTH_ATTR, "100%");
		TagUtil.writeAttribute(stringWriter, HEIGHT_ATTR, "100%");
		TagUtil.writeAttribute(stringWriter, BORDER_ATTR, 0);
		TagUtil.writeAttribute(stringWriter, CELLPADDING_ATTR, 0);
		TagUtil.writeAttribute(stringWriter, CELLSPACING_ATTR, 0);
		TagUtil.writeAttribute(stringWriter, SUMMARY_ATTR, "");
		TagUtil.endBeginTag(stringWriter);

		for (Iterator<ImageData> it = imageDatas.iterator(); it.hasNext();) {
			ImageData imageData = it.next();
			TagUtil.beginBeginTag(stringWriter, TR);
			if (it.hasNext() && !fitToOnePage) {
				TagUtil.writeAttribute(stringWriter, STYLE_ATTR, "page-break-after:always");
			}
			TagUtil.writeAttribute(stringWriter, ALIGN_ATTR, "center");
			TagUtil.writeAttribute(stringWriter, VALIGN_ATTR, "middle");
			TagUtil.endBeginTag(stringWriter);
			TagUtil.beginTag(stringWriter, TD);

			TagUtil.beginBeginTag(stringWriter, IMG);
			TagUtil.writeAttribute(stringWriter, SRC_ATTR, ChartUtil.getFilePath(imageData.toImageFile()));
			TagUtil.writeAttribute(stringWriter, ALT_ATTR, "");
			TagUtil.endBeginTag(stringWriter);
			TagUtil.endTag(stringWriter, IMG);

			TagUtil.endTag(stringWriter, TD);
			TagUtil.endTag(stringWriter, TR);
		}

		TagUtil.endTag(stringWriter, TABLE);
	}


	private File convertToPDF(Pair<String, String> contextInfo, String inputHTML, boolean portraitMode, boolean fitToOnePage,
			int chartHeight, int chartWidth, boolean isNativeFormat) throws IOException, DocumentException {
		File outputPDF = File.createTempFile("GanttChart", ".pdf", Settings.getInstance().getTempDir());
		htmlToPdf(inputHTML, outputPDF, contextInfo, computeScaling(portraitMode, fitToOnePage, chartHeight, chartWidth, isNativeFormat),	1);
		return outputPDF;
	}

	/**
	 * Computes a scaling for the given {@link ImageData} so that it will fit exactly into one page.
	 * 
	 * @param portraitMode
	 *        Flag indicating whether to use page in portrait mode or not.
	 * @param fitToOnePage
	 *        Flag indicating whether all imageDatas have to fit on one page or each of it has its
	 *        own page.
	 * @return A {@link Float} that can be used as first parameter of a ":1" scaling (e.g. 2:1 which
	 *         means that 2 pixel of the image are mapped to one pixel in the page, so the image is
	 *         scaled down).
	 */
	protected float computeScaling(boolean portraitMode, boolean fitToOnePage,
			int chartHeight, int chartWidth, boolean isNativeFormat) {
		float scale = 1;

		if(!isNativeFormat){
			float horScale = 1;
			float verScale = 1;
			if (portraitMode) {
				// assuming that horizontally 786px fit to A3 page in portrait mode and 1:1 scaling
				horScale = chartWidth / getMaxWidthPortrait();
				// assuming that vertically 1136px fit to A3 page in portrait mode and 1:1 scaling
				verScale = chartHeight / getMaxHeightPortrait();
			} else {
				// assuming that horizontally 1136px fit to A3 page in landscape mode and 1:1 scaling
				horScale = chartWidth / getMaxWidthLandscape();
				// assuming that vertically 786px fit to A3 page in landscape mode and 1:1 scaling
				verScale = chartHeight / getMaxHeightLandscape();
			}
			
			scale = Math.max(horScale, verScale);
			
			if (scale < 1) {
				// don't become bigger than scaling 1:1
				scale = 1;
			}
		}
		
		return scale;
	}

	/**
	 * Returns the maximum with a picture may have to fit to A3 page in portrait mode and 1:1 scaling.
	 */
	public float getMaxWidthPortrait() {
		return 786f;
	}

	/**
	 * Returns the maximum height a picture may have to fit to A3 page in portrait mode and 1:1 scaling.
	 */
	public float getMaxHeightPortrait() {
		return 1136f;
	}

	/**
	 * Returns the maximum width a picture may have to fit to A3 page in landscape mode and 1:1 scaling.
	 */
	public float getMaxWidthLandscape() {
		return 1136f;
	}

	/**
	 * Returns the maximum height a picture may have to fit to A3 page in landscape mode and 1:1 scaling.
	 */
	public float getMaxHeightLandscape() {
		return 786f;
	}

	private void htmlToPdf(String html, File output, Pair<String, String> contextInfo, float dotsPerPoint, int dotsPerPixel) throws DocumentException, IOException {
		OutputStream os = new FileOutputStream(output);
		try {
			ITextRenderer renderer = new ITextRenderer(dotsPerPoint, dotsPerPixel);

			// use own UserAgentCallback to transform image and CSS resource-paths
			String contextPath = contextInfo.getFirst();
			useCustomizedUserAgentCallback(renderer, contextPath);

			String baseURL = contextInfo.getSecond();
			renderer.getSharedContext().getUserAgentCallback().setBaseURL(baseURL);

			renderer.setDocumentFromString(html);
			renderer.layout();
			renderer.createPDF(os);
		}
		finally {
			os.flush();
			os = StreamUtilities.close(os);
		}
	}

	private void useCustomizedUserAgentCallback(ITextRenderer renderer, final String contextPath) {
		// get the UserAgentCallback of the renderer
		final UserAgentCallback callback = renderer.getSharedContext().getUserAgentCallback();

		// create custom UserAgentCallback
		renderer.getSharedContext().setUserAgentCallback(new UserAgentCallback() {

			/**
			 * leave the uri as it is at this point.
			 * 
			 * This is called internally to resolve given uris.
			 * 
			 * We just want to return the relative uri as given for being able of resolving tl paths later on
			 * 
			 * @return the given uri
			 * 
			 * @see org.xhtmlrenderer.extend.UserAgentCallback#resolveURI(java.lang.String)
			 */
			@Override
			public String resolveURI(String uri) {
				return uri;
			}

			/**
			 * Transform the given uri (e.g. "/pmt/image.png") into an absolute file path (e.g.
			 * "file:/D:/pmt/image.png")
			 *
			 * @param uri
			 *        The uri to transform
			 * @return The transformed uri
			 */
			private String getTLPath(String uri) {
				// only transform if not already done
				if (!uri.startsWith("file:")) {
					if (uri.startsWith(contextPath)) {
						uri = uri.substring(contextPath.length());
					}
					if (uri.startsWith("/")) {
						uri = uri.substring(1);
					}
					int parameterStart = uri.lastIndexOf("?");
					if (parameterStart >= 0) {
						uri = uri.substring(0, parameterStart);
					}
					try {
						URL file = FileManager.getInstance().getResourceUrl(uri);
						return file == null ? uri : file.toExternalForm();
					} catch (IOException e) {
						Logger.error("Unable to get file", e, this);
					}
				}
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
				uri = getTLPath(uri); // transform the uri
				return callback.getImageResource(uri);
			}

			@Override
			public CSSResource getCSSResource(String uri) {
				uri = getTLPath(uri); // transform the uri
				return callback.getCSSResource(uri);
			}

			@Override
			public byte[] getBinaryResource(String uri) {
				uri = getTLPath(uri); // transform the uri
				return callback.getBinaryResource(uri);
			}
		});
	}

}
