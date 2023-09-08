/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.PictureShape;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.sl.usermodel.TableCell;
import org.apache.poi.sl.usermodel.TableShape;
import org.apache.poi.sl.usermodel.TextBox;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.sl.usermodel.TextShape;

import com.top_logic.base.office.ppt.PptCell.PptCellStyle;
import com.top_logic.base.office.ppt.PptCell.PptFont;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.binary.BinaryData;


/**
 * The PptTable represents a table of PptCells.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class PptTable {

    private int numRows;
    private int numCols;
    
    private PptCell[][] cells;
    
    private double rowHeight;
    
    private PptColumnDescription[] colDesc;

    /** 
     * Creates a {@link PptTable}.
     * 
     */
    public PptTable(int rows, int cols) {
        assert rows > 0 : "row index too small";
        assert cols > 0 : "col index too small";
        
        this.cells   = new PptCell[rows][cols];
        this.colDesc = new PptColumnDescription[cols];
        this.numCols = cols;
        this.numRows = rows;
    }

    public int getColCount() {
        return (this.numCols);
    }
    
    public int getRowCount() {
        return (this.numRows);
    }
    
    public PptCell getCell(int aRow, int aCol) {
        assert aRow >= 0 && aRow < this.numRows : "row index out of range";
        assert aCol >= 0 && aCol < this.numCols : "col index out of range";
        
        return internalGetCell(aRow, aCol);
    }
    
    private PptCell internalGetCell(int aRow, int aCol) {
        PptCell theCell = this.cells[aRow][aCol];
        if (theCell == null) {
            theCell = new PptCell("Cell_x"+aRow+"y"+aCol, StringServices.EMPTY_STRING);
            this.setCell(aRow, aCol, theCell);
        }
        return theCell;
    }
    
    public void setCell(int aRow, int aCol, PptCell aCell) {
        assert aRow >= 0 && aRow < this.numRows : "row index out of range";
        assert aCol >= 0 && aCol < this.numCols : "col index out of range";
        assert aCell != null;

        aCell.setHeight(this.getRowHeight());
        aCell.setWidth(this.getColumnDescription(aCol).getWidth());
        
        this.cells[aRow][aCol] = aCell;
    }
    
    public double getRowHeight() {
        return (this.rowHeight);
    }
    
    public void setRowHeight(double aRowHeight) {
        this.rowHeight = aRowHeight;
        
        for (int i=0; i<this.numRows; i++) {
            for (int k=0; k<this.numCols; k++) {
                this.getCell(i, k).setHeight(this.rowHeight);
            }
        }
    }
    
    private void internalSetColumnWidth(int aCol, double aWidth, boolean autoFit) {
        assert aCol >= 0 && aCol < this.numCols : "col index out of range";
        
        PptColumnDescription theDesc = this.getColumnDescription(aCol);
        theDesc.setWidth(aWidth);
        theDesc.setAutoFit(autoFit);
        
        for (int i=0; i<this.numRows; i++) {
            this.getCell(i, aCol).setWidth(aWidth);
        }
    }
    
    public PptColumnDescription getColumnDescription(int aCol) {
        assert aCol >= 0 && aCol < this.numCols : "col index out of range";
        
        PptColumnDescription theDesc = this.colDesc[aCol];
        if (theDesc == null) {
            theDesc = new PptColumnDescription(0, true);
            this.colDesc[aCol] = theDesc;
        }
        
        return theDesc;
    }
    
    /**
     * Resize all columns marked as autofit to fit into the given table width.
     * This method will fail, if the given width is smaller than the sum of all 
     * width of non-autofit columns.
     */
    /*package protected*/ void autoFitTableWidth(double aWidth) {
        
        double declaredWidth = 0;
        int numDeclared   = this.numCols;
        
        for (int i=0; i<this.numCols; i++) {
            PptColumnDescription theDesc = this.getColumnDescription(i);
            if (! theDesc.isAutoFit()) {
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
            PptColumnDescription theDesc = this.getColumnDescription(i);
            if (theDesc.isAutoFit()) {
                this.internalSetColumnWidth(i, eachCell, true);
            }
            else {
                this.internalSetColumnWidth(i, theDesc.getWidth(), false);
            }
        }
    }
    
    /**
	 * Replace aShape with aTable. This will insert a POI {@link PptTable} into Powerpoint. Each row
	 * of the resulting table will have the same size as aShape. If no specific font and style is
	 * set to a {@link PptCell} of aTable, the style and font settings of aShape will be applied to
	 * the resulting cell.
	 */
    public static void writeAsTable(Sheet aParent, TextShape aShape, PptTable aTable) {
        
		Rectangle2D theBox = aShape.getAnchor();
        
        // funny, a shape from Powerpoint with height 0.52cm will sometimes
        // have a box.height=15, in other cases box.height=16 (perhaps 17, i didn't check that).
        // But 0.5cm will always result in box.height=15, that later after the shape/table
        // was rewritten into Powerpoint will result in a height of 0.53cm.
        // this ain't math, this is fortune!
		double theRowHeight = theBox.getWidth();
        
        aTable.autoFitTableWidth(theBox.getWidth());
        aTable.setRowHeight(theRowHeight);

		PptFont theFont = PptFont.createFont(aShape);
        
		TableShape<?, ?> theTable = aParent.createTable(aTable.getRowCount(), aTable.getColCount());
        aParent.addShape(theTable);
        
		theTable.setAnchor(new Rectangle2D.Double(theBox.getX(), theBox.getY(), theBox.getWidth(),
			theBox.getHeight() * aTable.getRowCount()));
        
		double yPos = theBox.getY();
        for (int x=0; x<aTable.getRowCount(); x++) {
            
			double xPos = theBox.getX();
            theTable.setRowHeight(x, theRowHeight);
            
            for (int y=0; y<aTable.getColCount(); y++) {
                PptCell      theCell     = aTable.getCell(x, y);
                
                if (theCell == null) {
                    continue;
                }

                Object  theData    = theCell.getData();
				boolean hasPicture = theData instanceof File || theData instanceof BinaryData;
                String  theText    = theCell.getValue();
                boolean hasText    = theText != null;
                int theColWidth    = (int) theCell.getWidth(); // possible loss of precicion
                
                theTable.setColumnWidth(y, theColWidth);
                
                PptFont      theCellFont = theCell.getFont();
                PptCellStyle theStyle    = theCell.getCellStyle();
                
                TableCell theTableCell = theTable.getCell(x, y);
                
                // setting the anchor will result in ordinary shape groups and not in a table, but only if(!) at least one cell has a 
                // text input. If all cells of the table do not have a text, the result will be a table object, which will autoresize
                // if an input is done. wtf?
                //theTableCell.setAnchor(new Rectangle2D.Double(xPos, yPos, theCell.getWidth(), theCell.getHeight()));
                
                // readding the cell as shape will result will always result in an ordinary shape group and not in a table, 
                // independent from cell content. second wtf?
                //theTable.addShape(theTableCell);
                
				TextRun newRun = theTableCell.setText(StringServices.getEmptyString(theText));
                
                // use font of the cell
                if (theCellFont != null) {
					theCellFont.applyFont(newRun);
                }
                // or use font of the template shape
                else {
					theFont.applyFont(newRun);
                }
                
				double theBorderWidth = theCell.getCellStyle().getLineWidth();

                // indent text, if a picture was added
                if (hasPicture) {
                    theStyle.setMarginLeft(theStyle.getMarginLeft() + theRowHeight + theBorderWidth);
                }
                
                theStyle.applyStyle(theTableCell);

                // if its only a picture, the picture will fill the whole cell
                if (hasPicture) {
                    try {
                        hasPicture      = true;
                        
						PictureData data =
							aParent.getSlideShow().addPicture(POIPowerpointXUtil.getImageBytes(theData),
								PictureType.PNG);
						PictureShape picShape = aParent.createPicture(data);
                        
                        // if hasText, draw a small square image
                        // else use full size of the cell
                        Rectangle2D thePicBox = new Rectangle2D.Double(xPos + 2*theBorderWidth, yPos + 2*theBorderWidth, hasText ? theRowHeight -4*theBorderWidth : theColWidth -4*theBorderWidth, theRowHeight  -4*theBorderWidth);
                        picShape.setAnchor(thePicBox);
                        picShape.setFillColor(theStyle.getFillColor());
                        
                        aParent.addShape(picShape);
                    } catch (IOException ioex) {
                        Logger.error("Unable to add picture", ioex, PptTable.class);
                    }
                }
                
                xPos += theColWidth;
            }
            yPos += theRowHeight;
        }
        
        aParent.removeShape(aShape);
    }
    
    /**
     * Replace a shape with a lot of single {@link TextShape}s, one for each {@link PptCell} of aTable.
     * Each row of the resulting table will have the same size as aShape. 
     * If no specific font and style is set to a {@link PptCell} of aTable, the style and 
     * font settings of aShape will be applied to the resulting cell.
     * 
     * In difference to {@link #writeAsTable(Sheet, TextShape, PptTable)}, this method will result in 
     * correct size calculation, since all numbers are represented as doubles. 
     */
	public static void writeAsShapes(Sheet aParent, TextShape aShape, PptTable aTable) {
        
		Rectangle2D theBox = aShape.getAnchor();
        
        aTable.autoFitTableWidth(theBox.getWidth());
        aTable.setRowHeight(theBox.getHeight());

		PptFont theFont = PptFont.createFont(aShape);
        
        double yPos = theBox.getY();
        
        // add a shape for each cell of the table
        for (int x=0; x<aTable.getRowCount(); x++) {
            double xPos = theBox.getX();
            for (int y=0; y<aTable.getColCount(); y++) {
                PptCell      theCell     = aTable.getCell(x, y);
                PptFont      theCellFont = theCell.getFont();
                PptCellStyle theStyle    = theCell.getCellStyle();
                
                Object  theData = theCell.getData();
				boolean hasPicture = theData instanceof File || theData instanceof BinaryData;
                String  theText    = theCell.getValue();
                boolean hasText    = theText != null;
                
				double theBorderWidth = theCell.getCellStyle().getLineWidth();
				TextBox<?, ?> theTextShape = aParent.createTextBox();
                
                Rectangle2D theTextBox  =  new Rectangle2D.Double(xPos, yPos, theCell.getWidth(), theCell.getHeight());
                
				TextRun newRun = theTextShape.setText(StringServices.getEmptyString(theText));
                
                // use font of the cell
                if (theCellFont != null) {
					theCellFont.applyFont(newRun);
                }
                // or use font of the template shape
                else {
					theFont.applyFont(newRun);
                }
                
                // indent text, if a picture was added
                if (hasPicture) {
                    theStyle.setMarginLeft(theStyle.getMarginLeft() + theCell.getHeight() + theBorderWidth);
                }
                
                theStyle.applyStyle(theTextShape);
                theTextShape.setAnchor(theTextBox);
                aParent.addShape(theTextShape);

                // if its only a picture, the picture will fill the whole cell
                if (hasPicture) {
                    try {
                        hasPicture      = true;
                        
						PictureData data =
							aParent.getSlideShow().addPicture(POIPowerpointXUtil.getImageBytes(theData),
								PictureType.PNG);
						PictureShape picShape = aParent.createPicture(data);
                        
                        // if hasText, draw a small square image
                        // else use full size of the cell
                        
                        Rectangle2D thePicBox = new Rectangle2D.Double(xPos + 2*theBorderWidth, yPos + 2*theBorderWidth, hasText ? theCell.getHeight() -4*theBorderWidth : theCell.getWidth() -4*theBorderWidth, theCell.getHeight()  -4*theBorderWidth);
                        picShape.setAnchor(thePicBox);
                        picShape.setFillColor(theStyle.getFillColor());
                        
                        aParent.addShape(picShape);
                    } catch (IOException ioex) {
                        Logger.error("Unable to add picture", ioex, PptTable.class);
                    }
                }
                xPos += theCell.getWidth();
            }
            yPos += aTable.getRowHeight();
        }
        
        // remove the original
        aParent.removeShape(aShape);
    }
    
    
    /**
     * The PptColumnDescription contains information about a single column of a 
     * {@link PptTable}
     */
    public static class PptColumnDescription {
        
        private double  width;
        private boolean autoFit;
        
        public PptColumnDescription(double aWidth, boolean autoFit) {
            this.width      = aWidth;
            this.autoFit = autoFit;
        }
        
        /**
         * Set the width of the column
         */
        public void setWidth(double aWidth) {
            this.width = aWidth;
        }
        
        public double getWidth() {
            return (this.width);
        }
        
        /**
         * Check if the column should be automatically resized according to the tables width
         * @see PptTable#autoFitTableWidth(double)
         */
        public boolean isAutoFit() {
            return (this.autoFit);
        }
        
        public void setAutoFit(boolean aAutoExtend) {
            this.autoFit = aAutoExtend;
        }
    }
}

