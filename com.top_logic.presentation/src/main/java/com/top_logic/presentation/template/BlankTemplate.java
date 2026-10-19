/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.awt.Dimension;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSheet;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;

/**
 * The template of a presentation created without a template of its own: the blank presentation
 * built into Apache POI in the widescreen format 16:9.
 *
 * <p>
 * The blank presentation offers the standard slide layouts (e.g. "Title Slide", "Title and
 * Content", "Section Header") with the Office theme. Its slide master and layouts are designed for
 * the format 4:3; they are stretched horizontally to the slide width {@link #WIDTH}.
 * </p>
 */
public final class BlankTemplate {

	/**
	 * The width of a slide in points.
	 */
	public static final int WIDTH = 960;

	/**
	 * The height of a slide in points.
	 */
	public static final int HEIGHT = 540;

	private static final String XFRM_PATH =
		"declare namespace a='http://schemas.openxmlformats.org/drawingml/2006/main' .//a:xfrm";

	private BlankTemplate() {
		// Utility class.
	}

	/**
	 * Creates a new blank presentation in the format {@link #WIDTH} x {@link #HEIGHT}.
	 */
	public static XMLSlideShow create() {
		XMLSlideShow result = new XMLSlideShow();
		Dimension size = result.getPageSize();
		double scaleX = (double) WIDTH / size.getWidth();
		double scaleY = (double) HEIGHT / size.getHeight();
		result.setPageSize(new Dimension(WIDTH, HEIGHT));
		for (XSLFSlideMaster master : result.getSlideMasters()) {
			scale(master, scaleX, scaleY);
			for (XSLFSlideLayout layout : master.getSlideLayouts()) {
				scale(layout, scaleX, scaleY);
			}
		}
		return result;
	}

	/**
	 * Scales the explicit positions and sizes of all shapes of the given sheet.
	 *
	 * <p>
	 * The blank presentation has no grouped shapes, whose child coordinates would have to be scaled
	 * as well.
	 * </p>
	 */
	private static void scale(XSLFSheet sheet, double scaleX, double scaleY) {
		for (XmlObject xml : sheet.getXmlObject().selectPath(XFRM_PATH)) {
			if (!(xml instanceof CTTransform2D xfrm)) {
				continue;
			}
			if (xfrm.isSetOff()) {
				CTPoint2D off = xfrm.getOff();
				off.setX(scale(((Number) off.getX()).longValue(), scaleX));
				off.setY(scale(((Number) off.getY()).longValue(), scaleY));
			}
			if (xfrm.isSetExt()) {
				CTPositiveSize2D ext = xfrm.getExt();
				ext.setCx(scale(ext.getCx(), scaleX));
				ext.setCy(scale(ext.getCy(), scaleY));
			}
		}
	}

	private static long scale(long emu, double factor) {
		return Math.round(emu * factor);
	}

}
