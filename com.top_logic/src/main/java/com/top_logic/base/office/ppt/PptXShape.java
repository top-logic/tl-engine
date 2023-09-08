/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.awt.geom.Rectangle2D;

import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSimpleShape;

/**
 * Represents a {@link XSLFShape}
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class PptXShape {
	/**
	 * See {@link XSLFSimpleShape#getAnchor()}
	 * 
	 * @return the anchor
	 */
	public Rectangle2D getAnchor() {
		return (anchor);
	}

	/**
	 * See {@link XSLFSimpleShape#setAnchor(Rectangle2D)}
	 * 
	 * @param anchor
	 *        the new anchor
	 */
	public void setAnchor(Rectangle2D anchor) {
		this.anchor = anchor;
	}

	/**
	 * See {@link XSLFSimpleShape#getFlipHorizontal()}
	 * 
	 * @return true if flipped
	 */
	public boolean getFlipHorizontal() {
		return flipHorizontal == null ? false : flipHorizontal;
	}

	/**
	 * See {@link XSLFSimpleShape#setFlipHorizontal(boolean)}
	 * 
	 * @param flipHorizontal
	 *        if true flip it
	 */
	public void setFlipHorizontal(boolean flipHorizontal) {
		this.flipHorizontal = flipHorizontal;
	}

	/**
	 * See {@link XSLFSimpleShape#getFlipVertical()}
	 * 
	 * @return true if flipped
	 */
	public boolean getFlipVertical() {
		return flipVertical == null ? false : flipVertical;
	}

	/**
	 * See {@link XSLFSimpleShape#setFlipVertical(boolean)}
	 * 
	 * @param flipVertical
	 *        if true flip it
	 */
	public void setFlipVertical(boolean flipVertical) {
		this.flipVertical = flipVertical;
	}

	/**
	 * See {@link XSLFSimpleShape#getRotation()}
	 * 
	 * @return the rotation angle
	 */
	public double getRotation() {
		return rotation == null ? 0.0d : rotation;
	}

	/**
	 * See {@link XSLFSimpleShape#setRotation(double)}
	 * 
	 * @param rotation
	 *        the rotation angle
	 */
	public void setRotation(double rotation) {
		this.rotation = rotation;
	}

	/**
	 * See {@link XSLFShape#getShapeName()}
	 * 
	 * @return the shape name
	 */
	public String getShapeName() {
		return (shapeName);
	}

	/**
	 * Setter to store shape name for use with {@link #getShapeName()}
	 * 
	 * @param shapeName
	 *        the shape name
	 */
	public void setShapeName(String shapeName) {
		this.shapeName = shapeName;
	}

	/**
	 * Copy the style setting to a given shape
	 * 
	 * @param aShape
	 *        the shape
	 */
	public void copyStyle(XSLFSimpleShape aShape) {
		if (!(this instanceof PptXTableCell)) {
			if (anchor != null)
				aShape.setAnchor(this.getAnchor());
			if (flipHorizontal != null)
				aShape.setFlipHorizontal(this.getFlipHorizontal());
			if (flipVertical != null)
				aShape.setFlipVertical(this.getFlipVertical());
			if (rotation != null)
				aShape.setRotation(this.getRotation());
		}
	}

	/**
	 * Create a new PptXShape from a shape
	 * 
	 * @param aShape
	 *        the shape
	 */
	public PptXShape(XSLFSimpleShape aShape) {
		if (!(this instanceof PptXTableCell)) {
			this.setAnchor(aShape.getAnchor());
			this.setFlipHorizontal(aShape.getFlipHorizontal());
			this.setFlipVertical(aShape.getFlipVertical());
			this.setRotation(aShape.getRotation());
			this.setShapeName(aShape.getShapeName());
		}
	}

	/**
	 * Create a new PptXShape with a shape name
	 * 
	 * @param aName
	 *        the shape name
	 */
	public PptXShape(String aName) {
		this.setShapeName(aName);
	}

	private Rectangle2D anchor;

	private Boolean flipHorizontal;

	private Boolean flipVertical;

	private Double rotation;

	private String shapeName;
}
