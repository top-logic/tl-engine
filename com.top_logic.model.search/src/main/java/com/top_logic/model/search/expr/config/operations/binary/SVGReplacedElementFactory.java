/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.print.PrintTranscoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.css.style.CalculatedStyle.Edge;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextReplacedElement;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;
import org.xml.sax.SAXException;

import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link ReplacedElementFactory} replacing references to SVG files by special
 * {@link ITextReplacedElement} finally rendering the SVG.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SVGReplacedElementFactory implements ReplacedElementFactory {

	@Override
	public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box, UserAgentCallback uac, int cssWidth,
			int cssHeight) {
		Element element = box.getElement();
		if (!HTMLConstants.IMG.equals(element.getNodeName())) {
			return null;
		}
		String sourceAttr = element.getAttribute(HTMLConstants.SRC_ATTR);
		// Support both .svg file references and data URI SVG images
		if (!sourceAttr.endsWith(".svg") && !isSvgDataUri(sourceAttr)) {
			return null;
		}

		// Decode data URI to reusable byte array, or keep URI string for file references
		if (isSvgDataUri(sourceAttr)) {
			// Decode data URI to byte array (can be reused multiple times for page wrapping)
			BinaryContent svgData = decodeDataUri(sourceAttr);

			// Replace href attributes with xlink:href attributes Batik insists to use.
			int imageCnt = 0;
			try (InputStream in = svgData.getStream()) {
				Document svg = DOMUtil.newDocumentBuilderNamespaceAware().parse(in);
				NodeList images = svg.getElementsByTagNameNS("http://www.w3.org/2000/svg", "image");
				for (int n = 0, cnt = images.getLength(); n < cnt; n++) {
					Element img = (Element) images.item(n);
					String src = img.getAttributeNS(null, "href");
					if (src != null && !src.isEmpty()) {
						img.removeAttributeNS(null, "href");
						img.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", src);
						imageCnt++;
					}
				}

				if (imageCnt > 0) {
					// Use transformed SVG.
					svgData = DOMUtil.toBinaryContent(svg, svgData.getName());
				}
			} catch (IOException | SAXException | ParserConfigurationException ex) {
				Logger.error("Failed to preprocess SVG image.", ex, SVGReplacedElementFactory.class);
				return null;
			}

			return new SVGReplacedElement(svgData, cssWidth, cssHeight);
		} else {
			// Use URI string for file references (can be read multiple times)
			ImageResource imageResource = uac.getImageResource(sourceAttr);
			return new SVGReplacedElement(imageResource.getImageUri(), cssWidth, cssHeight);
		}
	}

	/**
	 * Checks if the given source attribute is an SVG data URI.
	 *
	 * @param sourceAttr
	 *        The source attribute to check
	 * @return <code>true</code> if the source is an SVG data URI, <code>false</code> otherwise
	 */
	private boolean isSvgDataUri(String sourceAttr) {
		return sourceAttr.startsWith("data:image/svg");
	}

	/**
	 * Decodes an SVG data URI to a byte array.
	 *
	 * @param dataUri
	 *        The data URI (either base64-encoded or URL-encoded)
	 * @return A byte array containing the decoded SVG content
	 */
	private BinaryData decodeDataUri(String dataUri) {
		assert dataUri.startsWith("data:");
		int separatorIndex = dataUri.indexOf(',');
		if (separatorIndex < 0) {
			throw new IllegalArgumentException("Unsupported data URI format: " + dataUri);
		}

		String header = dataUri.substring("data:".length(), separatorIndex);
		String data = dataUri.substring(separatorIndex + 1);
		if (header.endsWith(";base64")) {
			String mediaType = header.substring(0, header.length() - ";base64".length());

			// Decode base64 data URI
			return BinaryDataFactory.createBinaryData(Base64.getDecoder().decode(data), mediaType);
		} else {
			String mediaType = header;

			// Handle non-base64 data URI (URL-encoded)
			try {
				String decoded = URLDecoder.decode(data, "UTF-8");
				return BinaryDataFactory.createBinaryData(decoded.getBytes(StandardCharsets.UTF_8), mediaType);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("Failed to decode SVG data URI", e);
			}
		}
	}

	@Override
	public void reset() {
		// Nothing to do here
	}

	@Override
	public void remove(Element e) {
		// Nothing to do here
	}

	@Override
	public void setFormSubmissionListener(FormSubmissionListener listener) {
		// Nothing to do here
	}

	/**
	 * {@link ITextReplacedElement} rendering an SVG document.
	 *
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class SVGReplacedElement implements ITextReplacedElement {

		private Point location = new Point(0, 0);

		private final BinaryContent _svgData;

		private final String _svgUri;

		private final int _cssWidth;

		private final int _cssHeight;

		/**
		 * Creates a new {@link SVGReplacedElement} from byte array data.
		 *
		 * @param svgData
		 *        The SVG content as byte array (can be reused for multiple paint calls)
		 * @param cssWidth
		 *        The width in CSS pixels
		 * @param cssHeight
		 *        The height in CSS pixels
		 */
		public SVGReplacedElement(BinaryContent svgData, int cssWidth, int cssHeight) {
			_svgUri = null;
			_svgData = svgData;
			_cssWidth = cssWidth;
			_cssHeight = cssHeight;
		}

		/**
		 * Creates a new {@link SVGReplacedElement} from a URI.
		 *
		 * @param svgUri
		 *        The URI to the SVG file (can be re-read for multiple paint calls)
		 * @param cssWidth
		 *        The width in CSS pixels
		 * @param cssHeight
		 *        The height in CSS pixels
		 */
		public SVGReplacedElement(String svgUri, int cssWidth, int cssHeight) {
			_svgData = null;
			_svgUri = svgUri;
			_cssWidth = cssWidth;
			_cssHeight = cssHeight;
		}

		@Override
		public void detach(LayoutContext c) {
			// Nothing to do here
		}

		@Override
		public int getBaseline() {
			return 0;
		}

		@Override
		public int getIntrinsicWidth() {
			return _cssWidth;
		}

		@Override
		public int getIntrinsicHeight() {
			return _cssHeight;
		}

		@Override
		public boolean hasBaseline() {
			return false;
		}

		@Override
		public boolean isRequiresInteractivePaint() {
			return false;
		}

		@Override
		public Point getLocation() {
			return location;
		}

		@Override
		public void setLocation(int x, int y) {
			this.location.x = x;
			this.location.y = y;
		}

		@Override
		public void paint(RenderingContext renderingContext, ITextOutputDevice outputDevice,
				BlockBox blockBox) {
			PdfContentByte cb = outputDevice.getWriter().getDirectContent();
			float width = _cssWidth / outputDevice.getDotsPerPoint();
			float height = _cssHeight / outputDevice.getDotsPerPoint();

			PdfTemplate template = cb.createTemplate(width, height);
			Graphics2D graphics = template.createGraphics(width, height);
			PrintTranscoder printer = new PrintTranscoder();


			// Create a fresh TranscoderInput from stored data (supports multiple paint calls)
			TranscoderInput transcoderInput;
			if (_svgData != null) {
				// Create new InputStream from image data (data URI case)
				try {
					transcoderInput = new TranscoderInput(_svgData.getStream());
				} catch (IOException ex) {
					Logger.error("Cannot access image data.", ex, SVGReplacedElement.class);
					return;
				}
			} else {
				// Create new TranscoderInput from URI (file reference case)
				transcoderInput = new TranscoderInput(_svgUri);
			}
			printer.transcode(transcoderInput, null);

			PageFormat pageFormat = new PageFormat();
			Paper paper = new Paper();
			paper.setSize(width, height);
			paper.setImageableArea(0, 0, width, height);
			pageFormat.setPaper(paper);
			printer.print(graphics, pageFormat, 0);
			graphics.dispose();

			PageBox page = renderingContext.getPage();
			float x = blockBox.getAbsX() + page.getMarginBorderPadding(renderingContext, Edge.LEFT);
			float y = (page.getBottom() - (blockBox.getAbsY() + _cssHeight))
				+ page.getMarginBorderPadding(renderingContext, Edge.BOTTOM);
			x /= outputDevice.getDotsPerPoint();
			y /= outputDevice.getDotsPerPoint();

			cb.addTemplate(template, x, y);
		}
	}

}
