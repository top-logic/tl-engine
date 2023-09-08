/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFSheet;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTableRow;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileUtilities;

/**
 * The PptTable represents a table of PptXCells.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class PptXTable {

    private int numRows;
    private int numCols;
    
	private PptXTableCell[][] cells;
    
	private double rowHeight;
    
	private PptXColumnDescription[] colDesc;

    /** 
     * Creates a {@link PptTable}.
     * 
     */
    public PptXTable(int rows, int cols) {
        assert rows > 0 : "row index too small";
        assert cols > 0 : "col index too small";
        
		this.cells = new PptXTableCell[rows][cols];
		this.colDesc = new PptXColumnDescription[cols];
        this.numCols = cols;
        this.numRows = rows;
    }

	/**
	 * the number of columns
	 */
    public int getColCount() {
        return (this.numCols);
    }
    
	/**
	 * the number of rows
	 */
    public int getRowCount() {
        return (this.numRows);
    }
    
	/**
	 * Get a cell
	 * 
	 * @param aRow
	 *        row number starting with 0
	 * @param aCol
	 *        column number starting with 0
	 * @return the cell. May be <code>null</code>
	 */
	public PptXTableCell getCell(int aRow, int aCol) {
        assert aRow >= 0 && aRow < this.numRows : "row index out of range";
        assert aCol >= 0 && aCol < this.numCols : "col index out of range";
        
        return internalGetCell(aRow, aCol);
    }
    
	private PptXTableCell internalGetCell(int aRow, int aCol) {
		PptXTableCell theCell = this.cells[aRow][aCol];
        if (theCell == null) {
			theCell = new PptXTableCell("Cell_x" + aRow + "y" + aCol, StringServices.EMPTY_STRING);
            this.setCell(aRow, aCol, theCell);
        }
        return theCell;
    }
    
	/**
	 * Set a cell
	 * 
	 * @param aRow
	 *        the row number starting with 0
	 * @param aCol
	 *        the column number starting with 0
	 * @param aCell
	 *        the cell
	 */
	public void setCell(int aRow, int aCol, PptXTableCell aCell) {
        assert aRow >= 0 && aRow < this.numRows : "row index out of range";
        assert aCol >= 0 && aCol < this.numCols : "col index out of range";
        assert aCell != null;

        this.cells[aRow][aCol] = aCell;
    }

	/**
	 * the standard row height
	 */
	public double getRowHeight() {
		return (this.rowHeight);
    }
    
	/**
	 * Set the standard row height
	 * 
	 * @param aRowHeight
	 *        the standard row height
	 */
    public void setRowHeight(double aRowHeight) {
        this.rowHeight = aRowHeight;
		// TODO set cell height???
		// for (int i=0; i<this.numRows; i++) {
		// for (int k=0; k<this.numCols; k++) {
		// this.getCell(i, k).setHeight(this.rowHeight);
		// }
		// }
    }
    
    private void internalSetColumnWidth(int aCol, double aWidth, boolean autoFit) {
        assert aCol >= 0 && aCol < this.numCols : "col index out of range";
        
		PptXColumnDescription theDesc = this.getColumnDescription(aCol);
        theDesc.setWidth(aWidth);
        theDesc.setAutoFit(autoFit);
        
		// TODO set cell width??
		// for (int i=0; i<this.numRows; i++) {
		// this.getCell(i, aCol).setWidth(aWidth);
		// }
    }
    
	/**
	 * Get the column description for a column
	 * 
	 * @param aCol
	 *        the column number starting with 0
	 * @return the description
	 */
	public PptXColumnDescription getColumnDescription(int aCol) {
        assert aCol >= 0 && aCol < this.numCols : "col index out of range";
        
		PptXColumnDescription theDesc = this.colDesc[aCol];
		if (theDesc == null) {
			theDesc = new PptXColumnDescription(0, true);
			this.colDesc[aCol] = theDesc;
		}

		return theDesc;
    }

	/**
	 * Resize all columns marked as autofit to fit into the given table width. This method will
	 * fail, if the given width is smaller than the sum of all width of non-autofit columns. TODO
	 * test if autofit works with PPTX
	 */
    /*package protected*/ void autoFitTableWidth(double aWidth) {
        
        double declaredWidth = 0;
        int numDeclared   = this.numCols;
        
        for (int i=0; i<this.numCols; i++) {
			PptXColumnDescription theDesc = this.getColumnDescription(i);
			if (!theDesc.isAutoFit()) {
                declaredWidth += theDesc.getWidth();
                numDeclared--;
            }
        }
        
        if (declaredWidth > aWidth) {
            throw new IllegalArgumentException("aWidth is too small. Dont know how to shrink columns");
        }
        
        //if (numDeclared < 1) { // all columns are fixed, nothing to autoadjust
        //    return;
        //}
        
        double theRest  = aWidth - declaredWidth;
        double eachCell = theRest / numDeclared;
        
        for (int i=0; i<this.numCols; i++) {
			PptXColumnDescription theDesc = this.getColumnDescription(i);
            if (theDesc.isAutoFit()) {
                this.internalSetColumnWidth(i, eachCell, true);
            }
            else {
                this.internalSetColumnWidth(i, theDesc.getWidth(), false);
            }
        }
    }

	/**
	 * Replace aShape with aTable. This will insert a POI {@link PptXTable} into Powerpoint. Each
	 * row of the resulting table will have the same size as aShape. If no specific font and style
	 * is set to a {@link PptXTableCell} of aTable, the style and font settings of aShape will be
	 * applied to the resulting cell.
	 */
	public static void writeAsTable(XSLFSheet aParent, XSLFTextShape aShape, PptXTable aTable) {
        
        Rectangle2D theBox = aShape.getAnchor();
        
        // funny, a shape from Powerpoint with height 0.52cm will sometimes
        // have a box.height=15, in other cases box.height=16 (perhaps 17, i didn't check that).
        // But 0.5cm will always result in box.height=15, that later after the shape/table
        // was rewritten into Powerpoint will result in a height of 0.53cm.
        // this ain't math, this is fortune!
		double theRowHeight = theBox.getHeight();
        
        aTable.autoFitTableWidth(theBox.getWidth());
        aTable.setRowHeight(theRowHeight);

		XSLFTable theTable = aParent.createTable();
		theTable.setAnchor(aShape.getAnchor());
        
		double yPos = theBox.getY();
        for (int x=0; x<aTable.getRowCount(); x++) {
			XSLFTableRow theRow = theTable.addRow();
			theRow.setHeight(aTable.getRowHeight());
            
			double xPos = theBox.getX();
            for (int y=0; y<aTable.getColCount(); y++) {
				PptXTableCell theCell = aTable.getCell(x, y);
				XSLFTableCell theTableCell = theRow.addCell();
                if (theCell == null) {
                    continue;
                }

				Object theData = theCell.getData();
                boolean hasPicture = theData instanceof File;
                String  theText    = theCell.getText();
                boolean hasText    = theText != null;
				theCell.copy(theTableCell, true, false, false);
                
				POIPowerpointXUtil.insertText(theTableCell, theText);
                
                // indent text, if a picture was added
                if (hasPicture) {
					theTableCell.setLeftInset(theCell.getLeftInset() + theRowHeight + theCell.getBorderLeft());
                }

                // if its only a picture, the picture will fill the whole cell
                if (hasPicture) {
                    try {
                        hasPicture      = true;
                        File thePicture = (File) theData;
                        
						XSLFPictureData data =
							aParent.getSlideShow().addPicture(FileUtilities.getBytesFromFile(thePicture),
								PictureType.PNG);
						XSLFPictureShape picShape = aParent.createPicture(data);
                        
                        // if hasText, draw a small square image
                        // else use full size of the cell
						double borderX = theCell.getBorderLeft() + theCell.getBorderRight();
						double borderY = theCell.getBorderBottom() + theCell.getBorderTop();
						Rectangle2D thePicBox = new Rectangle2D.Double(
							xPos + borderX,
							yPos + borderY,
								hasText ? theRowHeight - 2 * borderX : aTable.getColumnDescription(y).getWidth() - 2
									* borderX,
							theRowHeight - 2 * borderY);
                        picShape.setAnchor(thePicBox);
						picShape.setFillColor(theCell.getFillColor());
                    } catch (IOException ioex) {
                        Logger.error("Unable to add picture", ioex, PptXTable.class);
                    }
                }
                
				xPos += aTable.getColumnDescription(y).getWidth();
            }
            yPos += theRowHeight;
        }
        
        aParent.removeShape(aShape);
    }

	/**
	 * Replace a shape with a lot of single {@link TextShape}s, one for each {@link PptXTableCell} of
	 * aTable. Each row of the resulting table will have the same size as aShape. If no specific
	 * font and style is set to a {@link PptXTableCell} of aTable, the style and font settings of aShape
	 * will be applied to the resulting cell.
	 * 
	 * In difference to {@link #writeAsTable(Sheet, TextShape, PptXTable)}, this method will result
	 * in correct size calculation, since all numbers are represented as doubles.
	 */
	// TODO maybe implement table logic with shape group to pass by the problem that currently no
	// pictures are possible in table cells
	// public static void writeAsShapes(XSLFSheet aParent, XSLFTextShape aShape, PptXTable aTable) {
	//
	// Rectangle2D theBox = aShape.getAnchor();
	//
	// aTable.autoFitTableWidth(theBox.getWidth());
	// aTable.setRowHeight(theBox.getHeight());
	//
	// PptXTextRun theFont = new PptXTextRun(aShape);
	//
	// double yPos = theBox.getY();
	//
	// // add a shape for each cell of the table
	// for (int x=0; x<aTable.getRowCount(); x++) {
	// double xPos = theBox.getX();
	// for (int y=0; y<aTable.getColCount(); y++) {
	// PptXTableCell theCell = aTable.getCell(x, y);
	// PptXTextRun theCellFont = theCell.gett();
	// PptXCellStyle theStyle = theCell.getCellStyle();
	//
	// Object theData = theCell.getData();
	// boolean hasPicture = theData instanceof File;
	// String theText = theCell.getValue();
	// boolean hasText = theText != null;
	//
	// double theBorderWidth = theCell.getCellStyle().getBorderWidth();
	// TextBox theTextShape = new TextBox();
	//
	// Rectangle2D theTextBox = new Rectangle2D.Double(xPos, yPos, theCell.getWidth(),
	// theCell.getHeight());
	//
	// theTextShape.setText(StringServices.getEmptyString(theText));
	// RichTextRun theRun = theTextShape.getTextRun().getRichTextRuns()[0];
	//
	// // use font of the cell
	// if (theCellFont != null) {
	// theCellFont.applyFont(theRun);
	// }
	// // or use font of the template shape
	// else {
	// theFont.applyFont(theRun);
	// }
	//
	// // indent text, if a picture was added
	// if (hasPicture) {
	// theStyle.setMarginLeft(theStyle.getMarginLeft() + theCell.getHeight() + theBorderWidth);
	// }
	//
	// theStyle.applyStyle(theTextShape);
	// theTextShape.setAnchor(theTextBox);
	// aParent.addShape(theTextShape);
	//
	// // if its only a picture, the picture will fill the whole cell
	// if (hasPicture) {
	// try {
	// hasPicture = true;
	// File thePicture = (File) theData;
	//
	// int idx = aParent.getSlideShow().addPicture(thePicture, Picture.PNG);
	// Picture picShape = new Picture(idx);
	//
	// // if hasText, draw a small square image
	// // else use full size of the cell
	//
	// Rectangle2D thePicBox = new Rectangle2D.Double(xPos + 2*theBorderWidth, yPos +
	// 2*theBorderWidth, hasText ? theCell.getHeight() -4*theBorderWidth : theCell.getWidth()
	// -4*theBorderWidth, theCell.getHeight() -4*theBorderWidth);
	// picShape.setAnchor(thePicBox);
	// picShape.setFillColor(theStyle.getFillColor());
	//
	// aParent.addShape(picShape);
	// } catch (IOException ioex) {
	// Logger.error("Unable to add picture", ioex, PptXTable.class);
	// }
	// }
	// xPos += theCell.getWidth();
	// }
	// yPos += aTable.getRowHeight();
	// }
	//
	// // remove the original
	// aParent.removeShape(aShape);
	// }

	/**
	 * The PptXColumnDescription contains information about a single column of a {@link PptXTable}
	 */
	public static class PptXColumnDescription {

		private double width;

		private boolean autoFit;

		/**
		 * Create a new PptXColumnDescription
		 * 
		 * @param aWidth
		 *        the width
		 * @param autoFit
		 *        if true try to autofit the column
		 */
		public PptXColumnDescription(double aWidth, boolean autoFit) {
			this.width = aWidth;
			this.autoFit = autoFit;
		}

		/**
		 * Set the width of the column
		 */
		public void setWidth(double aWidth) {
			this.width = aWidth;
		}

		/**
		 * the column width
		 */
		public double getWidth() {
			return (this.width);
		}

		/**
		 * Check if the column should be automatically resized according to the tables width
		 * 
		 * @see PptXTable#autoFitTableWidth(double)
		 */
		public boolean isAutoFit() {
			return (this.autoFit);
		}

		/**
		 * Set the autofit
		 * 
		 * @param aAutoFit
		 *        if true autofit column
		 */
		public void setAutoFit(boolean aAutoFit) {
			this.autoFit = aAutoFit;
		}
	}
}

