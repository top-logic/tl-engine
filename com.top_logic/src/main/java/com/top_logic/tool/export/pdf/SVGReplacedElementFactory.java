/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export.pdf;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.print.PageFormat;
import java.awt.print.Paper;

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
		if (!sourceAttr.endsWith(".svg")) {
			return null;
		}

		ImageResource imageResource = uac.getImageResource(sourceAttr);
		return new SVGReplacedElement(imageResource, cssWidth, cssHeight);
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

		private ImageResource _svg;

		/**
		 * Creates a new {@link SVGReplacedElement}.
		 */
		public SVGReplacedElement(ImageResource svg, int cssWidth, int cssHeight) {
			this.cssWidth = cssWidth;
			this.cssHeight = cssHeight;
			_svg = svg;
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
			TranscoderInput ti = new TranscoderInput(_svg.getImageUri());
			prm.transcode(ti, null);
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
