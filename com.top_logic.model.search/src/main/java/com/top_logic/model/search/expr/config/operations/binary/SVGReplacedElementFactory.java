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
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.print.PrintTranscoder;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.style.CalculatedStyle;
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

import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;

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
			byte[] svgData = decodeSvgDataUri(sourceAttr);
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
	private byte[] decodeSvgDataUri(String dataUri) {
		if (dataUri.startsWith("data:image/svg+xml;base64,")) {
			// Decode base64 data URI
			String base64Data = dataUri.substring("data:image/svg+xml;base64,".length());
			return Base64.getDecoder().decode(base64Data);
		} else if (dataUri.startsWith("data:image/svg+xml,")) {
			// Handle non-base64 data URI (URL-encoded)
			String svgData = dataUri.substring("data:image/svg+xml,".length());
			try {
				String decoded = URLDecoder.decode(svgData, "UTF-8");
				return decoded.getBytes(StandardCharsets.UTF_8);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("Failed to decode SVG data URI", e);
			}
		} else {
			throw new IllegalArgumentException("Unsupported data URI format: " + dataUri);
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

		private int cssWidth;

		private int cssHeight;

		private byte[] _svgData;

		private String _svgUri;

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
		public SVGReplacedElement(byte[] svgData, int cssWidth, int cssHeight) {
			this.cssWidth = cssWidth;
			this.cssHeight = cssHeight;
			_svgData = svgData;
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
			this.cssWidth = cssWidth;
			this.cssHeight = cssHeight;
			_svgUri = svgUri;
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
			return cssWidth;
		}

		@Override
		public int getIntrinsicHeight() {
			return cssHeight;
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
			float width = cssWidth / outputDevice.getDotsPerPoint();
			float height = cssHeight / outputDevice.getDotsPerPoint();

			PdfTemplate template = cb.createTemplate(width, height);
			Graphics2D g2d = template.createGraphics(width, height);
			PrintTranscoder prm = new PrintTranscoder();

			// Create a fresh TranscoderInput from stored data (supports multiple paint calls)
			TranscoderInput transcoderInput;
			if (_svgData != null) {
				// Create new InputStream from byte array (data URI case)
				transcoderInput = new TranscoderInput(new ByteArrayInputStream(_svgData));
			} else {
				// Create new TranscoderInput from URI (file reference case)
				transcoderInput = new TranscoderInput(_svgUri);
			}
			prm.transcode(transcoderInput, null);

			PageFormat pg = new PageFormat();
			Paper pp = new Paper();
			pp.setSize(width, height);
			pp.setImageableArea(0, 0, width, height);
			pg.setPaper(pp);
			prm.print(g2d, pg, 0);
			g2d.dispose();

			PageBox page = renderingContext.getPage();
			float x = blockBox.getAbsX() + page.getMarginBorderPadding(renderingContext, CalculatedStyle.LEFT);
			float y = (page.getBottom() - (blockBox.getAbsY() + cssHeight))
				+ page.getMarginBorderPadding(renderingContext, CalculatedStyle.BOTTOM);
			x /= outputDevice.getDotsPerPoint();
			y /= outputDevice.getDotsPerPoint();

			cb.addTemplate(template, x, y);
		}
	}

}
