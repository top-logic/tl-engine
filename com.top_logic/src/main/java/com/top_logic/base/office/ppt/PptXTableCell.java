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
import org.apache.poi.sl.usermodel.TableCell.BorderEdge;
import org.apache.poi.xslf.usermodel.XSLFSimpleShape;
import org.apache.poi.xslf.usermodel.XSLFTableCell;

/**
 * Represents a {@link XSLFTableCell}
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class PptXTableCell extends PptXTextShape {
	
	private Double _borderBottom;

	private Color _borderBottomColor;

	private Double _borderLeft;

	private Color _borderLeftColor;

	private Double _borderRight;

	private Color _borderRightColor;

	private Double _borderTop;

	private Color _borderTopColor;
	
	/**
	 * See {@link XSLFTableCell#getBorderWidth(BorderEdge)}
	 * 
	 * @return the border size
	 */
	public double getBorderBottom() {
		return _borderBottom == null ? 1.0d : _borderBottom;
	}

	/**
	 * See {@link XSLFTableCell#getBorderColor(BorderEdge)}
	 * 
	 * @return the border color
	 */
	public Color getBorderBottomColor() {
		return _borderBottomColor;
	}

	/**
	 * See {@link XSLFTableCell#getBorderWidth(BorderEdge)}
	 * 
	 * @return the border size
	 */
	public double getBorderLeft() {
		return _borderLeft == null ? 1.0d : _borderLeft;
	}

	/**
	 * See {@link XSLFTableCell#getBorderColor(BorderEdge)}
	 * 
	 * @return the border color
	 */
	public Color getBorderLeftColor() {
		return _borderLeftColor;
	}

	/**
	 * See {@link XSLFTableCell#getBorderWidth(BorderEdge)}
	 * 
	 * @return the border size
	 */
	public double getBorderRight() {
		return _borderRight == null ? 1.0d : _borderRight;
	}

	/**
	 * See {@link XSLFTableCell#getBorderColor(BorderEdge)}
	 * 
	 * @return the border color
	 */
	public Color getBorderRightColor() {
		return _borderRightColor;
	}

	/**
	 * See {@link XSLFTableCell#getBorderWidth(BorderEdge)}
	 * 
	 * @return the border size
	 */
	public double getBorderTop() {
		return _borderTop == null ? 1.0d : _borderTop;
	}

	/**
	 * See {@link XSLFTableCell#getBorderColor(BorderEdge)}
	 * 
	 * @return the border color
	 */
	public Color getBorderTopColor() {
		return _borderTopColor;
	}

	/**
	 * See {@link XSLFTableCell#setBorderWidth(BorderEdge, double)}
	 * 
	 * @param aBorderBottom
	 *        the border size
	 */
	public void setBorderBottom(double aBorderBottom) {
		_borderBottom = aBorderBottom;
	}

	/**
	 * See {@link XSLFTableCell#setBorderColor(BorderEdge, Color)}
	 * 
	 * @param aBorderBottomColor
	 *        the border color
	 */
	public void setBorderBottomColor(Color aBorderBottomColor) {
		_borderBottomColor = aBorderBottomColor;
	}

	/**
	 * See {@link XSLFTableCell#setBorderWidth(BorderEdge, double)}
	 * 
	 * @param aBorderLeft
	 *        the border size
	 */
	public void setBorderLeft(double aBorderLeft) {
		_borderLeft = aBorderLeft;
	}

	/**
	 * See {@link XSLFTableCell#setBorderColor(BorderEdge, Color)}
	 * 
	 * @param aBorderLeftColor
	 *        the border color
	 */
	public void setBorderLeftColor(Color aBorderLeftColor) {
		_borderLeftColor = aBorderLeftColor;
	}

	/**
	 * See {@link XSLFTableCell#setBorderWidth(BorderEdge, double)}
	 * 
	 * @param aBorderRight
	 *        the border size
	 */
	public void setBorderRight(double aBorderRight) {
		_borderRight = aBorderRight;
	}

	/**
	 * See {@link XSLFTableCell#setBorderColor(BorderEdge, Color)}
	 * 
	 * @param aBorderRightColor
	 *        the border color
	 */
	public void setBorderRightColor(Color aBorderRightColor) {
		_borderRightColor = aBorderRightColor;
	}

	/**
	 * See {@link XSLFTableCell#setBorderWidth(BorderEdge, double)}
	 * 
	 * @param aBorderTop
	 *        the border size
	 */
	public void setBorderTop(double aBorderTop) {
		_borderTop = aBorderTop;
	}

	/**
	 * See {@link XSLFTableCell#setBorderColor(BorderEdge, Color)}
	 * 
	 * @param aBorderTopColor
	 *        the border color
	 */
	public void setBorderTopColor(Color aBorderTopColor) {
		_borderTopColor = aBorderTopColor;
	}
	
	/**
	 * Create a new PptXTableCell with a shape name and text value
	 * 
	 * @param aName
	 *        the name
	 * @param aVal
	 *        the text value
	 */
	public PptXTableCell(String aName, String aVal) {
		super(aName, aVal);
	}

	/**
	 * TCreate a new PptXTableCell with a shape name
	 * 
	 * @param aName
	 *        the shape name
	 */
	public PptXTableCell(String aName) {
		super(aName);
	}

	/**
	 * Create a new PptXTableCell from a table cell
	 * 
	 * @param aCell
	 *        the powerpoint cell
	 * @param copyContents
	 *        if true copy the text contents into paragraphs and runs
	 */
	public PptXTableCell(XSLFTableCell aCell, boolean copyContents) {
		super(aCell, copyContents);

		this.setBorderBottom(aCell.getBottomInset());
		this.setBorderBottomColor(aCell.getBorderColor(BorderEdge.bottom));
		this.setBorderLeft(aCell.getLeftInset());
		this.setBorderLeftColor(aCell.getBorderColor(BorderEdge.left));
		this.setBorderRight(aCell.getRightInset());
		this.setBorderRightColor(aCell.getBorderColor(BorderEdge.right));
		this.setBorderTop(aCell.getTopInset());
		this.setBorderTopColor(aCell.getBorderColor(BorderEdge.top));
	}
	
	@Override
	public void copyStyle(XSLFSimpleShape aShape) {
		super.copyStyle(aShape);
		if (aShape instanceof XSLFTableCell) {
			XSLFTableCell tableCell = (XSLFTableCell) aShape;
			if (_borderBottom != null) {
				tableCell.setBottomInset(getBorderBottom());
			}
			if (_borderLeft != null) {
				tableCell.setLeftInset(getBorderLeft());
			}
			if (_borderRight != null) {
				tableCell.setRightInset(getBorderRight());
			}
			if (_borderTop != null) {
				tableCell.setTopInset(getBorderTop());
			}
			Color borderBottomColor = getBorderBottomColor();
			if (borderBottomColor != null) {
				tableCell.setBorderColor(BorderEdge.bottom, borderBottomColor);
			}
			Color borderLeftColor = getBorderLeftColor();
			if (borderLeftColor != null) {
				tableCell.setBorderColor(BorderEdge.left, borderLeftColor);
			}
			Color borderRightColor = this.getBorderRightColor();
			if (borderRightColor != null) {
				tableCell.setBorderColor(BorderEdge.right, borderRightColor);
			}
			Color borderTopColor = getBorderTopColor();
			if (borderTopColor != null) {
				tableCell.setBorderColor(BorderEdge.top, borderTopColor);
			}
        }
	}
	
	@Override
	public boolean getFlipHorizontal() {
		return false;
	}

	@Override
	public void setFlipHorizontal(boolean flipHorizontal) {
		// Not supported. Ignore
	}

	@Override
	public boolean getFlipVertical() {
		return false;
	}

	@Override
	public void setFlipVertical(boolean flipVertical) {
		// Not supported. Ignore
	}

	@Override
	public String getShapeName() {
		// Not supported. Ignore
		return null;
	}

	@Override
	public void setShapeName(String shapeName) {
		// Not supported. Ignore
	}

	@Override
	public LineCap getLineCap() {
		// Not supported. Ignore
		return null;
	}

	@Override
	public void setLineCap(LineCap lineCap) {
		// Not supported. Ignore
	}

	@Override
	public Color getLineColor() {
		// Not supported. Ignore
		return null;
	}

	@Override
	public void setLineColor(Color lineColor) {
		// Not supported. Ignore
	}

	@Override
	public LineDash getLineDash() {
		// Not supported. Ignore
		return null;
	}

	@Override
	public void setLineDash(LineDash lineDash) {
		// Not supported. Ignore
	}

	@Override
	public double getLineWidth() {
		// Not supported. Ignore
		return 1.0d;
	}

	@Override
	public void setLineWidth(double lineWidth) {
		// Not supported. Ignore
	}

	@Override
	public DecorationShape getLineHeadDecoration() {
		// Not supported. Ignore
		return null;
	}

	@Override
	public void setLineHeadDecoration(DecorationShape lineHeadDecoration) {
		// Not supported. Ignore
	}

	@Override
	public DecorationSize getLineHeadLength() {
		// Not supported. Ignore
		return null;
	}

	@Override
	public void setLineHeadLength(DecorationSize lineHeadLength) {
		// Not supported. Ignore
	}

	@Override
	public DecorationSize getLineHeadWidth() {
		// Not supported. Ignore
		return null;
	}

	@Override
	public void setLineHeadWidth(DecorationSize lineHeadWidth) {
		// Not supported. Ignore
	}

	@Override
	public DecorationShape getLineTailDecoration() {
		// Not supported. Ignore
		return null;
	}

	@Override
	public void setLineTailDecoration(DecorationShape lineTailDecoration) {
		// Not supported. Ignore
	}

	@Override
	public DecorationSize getLineTailLength() {
		return null;
	}

	@Override
	public void setLineTailLength(DecorationSize lineTailLength) {
		// Not supported. Ignore
	}

	@Override
	public DecorationSize getLineTailWidth() {
		// Not supported. Ignore
		return null;
	}

	@Override
	public void setLineTailWidth(DecorationSize lineTailWidth) {
		// Not supported. Ignore
	}

}
