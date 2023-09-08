/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.awt.Color;

import org.apache.poi.sl.usermodel.LineDecoration.DecorationShape;
import org.apache.poi.sl.usermodel.LineDecoration.DecorationSize;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCap;
import org.apache.poi.sl.usermodel.StrokeStyle.LineDash;
import org.apache.poi.xslf.usermodel.XSLFSimpleShape;

/**
 * Represents a {@link XSLFSimpleShape}
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class PptXSimpleShape extends PptXShape {
	/**
	 * See {@link XSLFSimpleShape#getFillColor()}
	 * 
	 * @return the file color
	 */
	public Color getFillColor() {
		return (fillColor);
	}

	/**
	 * See {@link XSLFSimpleShape#setFillColor(Color)}
	 * 
	 * @param fillColor
	 *        the fill color
	 */
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	/**
	 * See {@link XSLFSimpleShape#getLineCap()}
	 * 
	 * @return the line cap
	 */
	public LineCap getLineCap() {
		return (lineCap);
	}

	/**
	 * See {@link XSLFSimpleShape#setLineCap(LineCap)}
	 * 
	 * @param lineCap
	 *        the line cap
	 */
	public void setLineCap(LineCap lineCap) {
		this.lineCap = lineCap;
	}

	/**
	 * See {@link XSLFSimpleShape#getLineColor()}
	 * 
	 * @return the line color
	 */
	public Color getLineColor() {
		return (lineColor);
	}

	/**
	 * See {@link XSLFSimpleShape#setLineColor(Color)}
	 * 
	 * @param lineColor
	 *        the line color
	 */
	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	/**
	 * See {@link XSLFSimpleShape#getLineDash()}
	 * 
	 * @return the line dash
	 */
	public LineDash getLineDash() {
		return (lineDash);
	}

	/**
	 * See {@link XSLFSimpleShape#setLineDash(LineDash)}
	 * 
	 * @param lineDash
	 *        the line dash
	 */
	public void setLineDash(LineDash lineDash) {
		this.lineDash = lineDash;
	}

	/**
	 * See {@link XSLFSimpleShape#getLineWidth()}
	 * 
	 * @return the line width
	 */
	public double getLineWidth() {
		return lineWidth == null ? 1.0d : lineWidth;
	}

	/**
	 * See {@link XSLFSimpleShape#setLineWidth(double)}
	 * 
	 * @param lineWidth
	 *        the line width
	 */
	public void setLineWidth(double lineWidth) {
		this.lineWidth = lineWidth;
	}

	/**
	 * See {@link XSLFSimpleShape#getLineHeadDecoration()}
	 * 
	 * @return the line decoration
	 */
	public DecorationShape getLineHeadDecoration() {
		return (lineHeadDecoration);
	}

	/**
	 * See {@link XSLFSimpleShape#setLineHeadDecoration(DecorationShape)}
	 * 
	 * @param decorationShape
	 *        the line decoration
	 */
	public void setLineHeadDecoration(DecorationShape decorationShape) {
		this.lineHeadDecoration = decorationShape;
	}

	/**
	 * See {@link XSLFSimpleShape#getLineHeadLength()}
	 * 
	 * @return the line length
	 */
	public DecorationSize getLineHeadLength() {
		return (lineHeadLength);
	}

	/**
	 * See {@link XSLFSimpleShape#setLineHeadLength(DecorationSize)}
	 * 
	 * @param lineHeadLength
	 *        the line length
	 */
	public void setLineHeadLength(DecorationSize lineHeadLength) {
		this.lineHeadLength = lineHeadLength;
	}

	/**
	 * See {@link XSLFSimpleShape#getLineHeadWidth()}
	 * 
	 * @return the line cap
	 */
	public DecorationSize getLineHeadWidth() {
		return (lineHeadWidth);
	}

	/**
	 * See {@link XSLFSimpleShape#setLineHeadWidth(DecorationSize)}
	 * 
	 * @param lineHeadWidth
	 *        the line width
	 */
	public void setLineHeadWidth(DecorationSize lineHeadWidth) {
		this.lineHeadWidth = lineHeadWidth;
	}

	/**
	 * See {@link XSLFSimpleShape#getLineTailDecoration()}
	 * 
	 * @return the line decoration
	 */
	public DecorationShape getLineTailDecoration() {
		return (lineTailDecoration);
	}

	/**
	 * See {@link XSLFSimpleShape#setLineTailDecoration(DecorationShape)}
	 * 
	 * @param decorationShape
	 *        the line decoration
	 */
	public void setLineTailDecoration(DecorationShape decorationShape) {
		this.lineTailDecoration = decorationShape;
	}

	/**
	 * See {@link XSLFSimpleShape#getLineTailLength()}
	 * 
	 * @return the line length
	 */
	public DecorationSize getLineTailLength() {
		return (lineTailLength);
	}

	/**
	 * See {@link XSLFSimpleShape#setLineTailLength(DecorationSize)}
	 * 
	 * @param lineTailLength
	 *        the line length
	 */
	public void setLineTailLength(DecorationSize lineTailLength) {
		this.lineTailLength = lineTailLength;
	}

	/**
	 * See {@link XSLFSimpleShape#getLineTailWidth()}
	 * 
	 * @return the line width
	 */
	public DecorationSize getLineTailWidth() {
		return (lineTailWidth);
	}

	/**
	 * See {@link XSLFSimpleShape#setLineTailWidth(DecorationSize)}
	 * 
	 * @param lineTailWidth
	 *        the line width
	 */
	public void setLineTailWidth(DecorationSize lineTailWidth) {
		this.lineTailWidth = lineTailWidth;
	}

	@Override
	public void copyStyle(XSLFSimpleShape aShape) {
		super.copyStyle(aShape);

		if (aShape instanceof XSLFSimpleShape) {
			if (fillColor != null) {
				((XSLFSimpleShape) aShape).setFillColor(this.getFillColor());
			}

			if (!(this instanceof PptXTableCell)) {
				if (lineCap != null) {
					((XSLFSimpleShape) aShape).setLineCap(this.getLineCap());
				}
				if (lineColor != null) {
					((XSLFSimpleShape) aShape).setLineColor(this.getLineColor());
				}
				if (lineDash != null) {
					((XSLFSimpleShape) aShape).setLineDash(this.getLineDash());
				}
				if (lineWidth != null) {
					((XSLFSimpleShape) aShape).setLineWidth(this.getLineWidth());
				}

				if (lineHeadDecoration != null) {
					((XSLFSimpleShape) aShape).setLineHeadDecoration(this.getLineHeadDecoration());
				}
				if (lineHeadLength != null) {
					((XSLFSimpleShape) aShape).setLineHeadLength(this.getLineHeadLength());
				}
				if (lineHeadWidth != null) {
					((XSLFSimpleShape) aShape).setLineHeadWidth(this.getLineHeadWidth());
				}

				if (lineTailDecoration != null) {
					((XSLFSimpleShape) aShape).setLineTailDecoration(this.getLineTailDecoration());
				}
				if (lineTailLength != null) {
					((XSLFSimpleShape) aShape).setLineTailLength(this.getLineTailLength());
				}
				if (lineTailWidth != null) {
					((XSLFSimpleShape) aShape).setLineTailWidth(this.getLineTailWidth());
				}
			}
		}
	}

	/**
	 * Create a new PptXSimpleShape from a powerpoint shape
	 * 
	 * @param aShape
	 *        the shape
	 */
	public PptXSimpleShape(XSLFSimpleShape aShape) {
		super(aShape);

		this.setFillColor(aShape.getFillColor());

		if (!(this instanceof PptXTableCell)) {
			this.setLineCap(aShape.getLineCap());
			this.setLineColor(aShape.getLineColor());
			this.setLineDash(aShape.getLineDash());
			this.setLineWidth(aShape.getLineWidth());

			this.setLineHeadDecoration(aShape.getLineHeadDecoration());
			this.setLineHeadLength(aShape.getLineHeadLength());
			this.setLineHeadWidth(aShape.getLineHeadWidth());

			this.setLineTailDecoration(aShape.getLineTailDecoration());
			this.setLineTailLength(aShape.getLineTailLength());
			this.setLineTailWidth(aShape.getLineTailWidth());
		}
	}

	/**
	 * Create a new PptXSimpleShape with a shape name
	 * 
	 * @param aName
	 *        the shape name
	 */
	public PptXSimpleShape(String aName) {
		super(aName);
	}

	private Color fillColor;

	private LineCap lineCap;

	private Color lineColor;

	private LineDash lineDash;

	private Double lineWidth;

	private DecorationShape lineHeadDecoration;

	private DecorationSize lineHeadLength;

	private DecorationSize lineHeadWidth;

	private DecorationShape lineTailDecoration;

	private DecorationSize lineTailLength;

	private DecorationSize lineTailWidth;
}
