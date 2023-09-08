/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.awt.Color;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

import com.top_logic.base.office.POIUtil;
import com.top_logic.base.office.ppt.StyledValue;
import com.top_logic.basic.col.TupleFactory;

/**
 * One excel value contains the cell content for one excel cell.
 *
 * IMPORTANT: If you add new style types to this class, you must update the
 * {@link #getStyleKey()}, {@link #clearStyle()} and
 * {@link #hasIndividualStyle()} methods also!
 *
 * This is compatibility Bridge / value holder for the {@link ExcelAccess},
 *
 * @author    <a href="mailto:fma@top-logic.com">Frank Mausz</a>
 */
public class ExcelValue extends StyledValue {
	
	/** Date format of type {@value #DATE_FORMAT_STANDARD}. */
	public static final String DATE_FORMAT_STANDARD = "m/d/yy";
	/** Date format of type {@value #DATE_FORMAT_D_MMM_YY}. */
	public static final String DATE_FORMAT_D_MMM_YY = "d-mmm-yy";
	/** Date format of type {@value #DATE_FORMAT_D_MMM}. */
	public static final String DATE_FORMAT_D_MMM = "d-mmmy";
	/** Date format of type {@value #DATE_FORMAT_MMM_YY}. */
	public static final String DATE_FORMAT_MMM_YY = "mmm-yy";

    /** Normal type offset (not superscript or subscript). */
    public final static Short TYPE_OFFSET_NORMAL = 0;

    /** Superscript type offset. */
    public final static Short TYPE_OFFSET_SUPER = 1;

    /** Subscript type offset. */
    public final static Short TYPE_OFFSET_SUB = 2;

	/**
	 * Possible value for setting dotted border.
	 * 
	 * @see #setBorder(String)
	 */
	public static final String BORDER_DOTTED = "borderDot";

	/**
	 * Possible value for setting thin border.
	 * 
	 * @see #setBorder(String)
	 */
	public static final String BORDER_THIN = "borderNormal";

	/**
	 * Possible value for setting dashed border.
	 * 
	 * @see #setBorder(String)
	 */
	public static final String BORDER_DASHED = "borderDashed";

	/**
	 * Possible value for setting thick border.
	 * 
	 * @see #setBorder(String)
	 */
	public static final String BORDER_THICK = "borderThick";

	/**
	 * Possible value for setting medium border.
	 * 
	 * @see #setBorder(String)
	 */
	public static final String BORDER_MEDIUM = "borderMedium";

	/**
	 * Possible value for setting medium dashed border.
	 * 
	 * @see #setBorder(String)
	 */
	public static final String BORDER_MEDIUM_DASHED = "borderMediumDashed";

	/**
	 * Possible value for setting medium dash dot border.
	 * 
	 * @see #setBorder(String)
	 */
	public static final String BORDER_MEDIUM_DASH_DOT = "borderMediumDashDot";

	/**
	 * Possible value for setting medium dash dot dot border.
	 * 
	 * @see #setBorder(String)
	 */
	public static final String BORDER_MEDIUM_DASH_DOT_DOT = "borderMediumDashDotDot";

	/**
	 * Possible value for setting dashed dotted border.
	 * 
	 * @see #setBorder(String)
	 */
	public static final String BORDER_DASH_DOT = "borderDashedDot";

	/**
	 * Possible value for setting dash, dot, dot border.
	 * 
	 * @see #setBorder(String)
	 */
	public static final String BORDER_DASH_DOT_DOT = "borderDashedDotDot";

	/**
	 * Possible value for setting slanted dashed dotted border.
	 * 
	 * @see #setBorder(String)
	 */
	public static final String BORDER_SLANTED_DASH_DOT = "borderSlantedDashedDotted";

	/**
	 * Possible value for setting hair border.
	 * 
	 * @see #setBorder(String)
	 */
	public static final String BORDER_HAIR = "borderHair";

	/**
	 * Possible value for setting double border.
	 * 
	 * @see #setBorder(String)
	 */
	public static final String BORDER_DOUBLE = "borderDouble";

	/** Constant that indicates that no alignment is set */
	public static final HorizontalAlignment NO_HORIZONTAL_ALIGNMENT_SET = null;

	/** Constant that indicates that no alignment is set */
	public static final VerticalAlignment NO_VERTIVAL_ALIGNMENT_SET = null;

	/** Constant that indicate no format was set. */
	public static final String NO_FORMAT_SET = null;

    /** The sheet the cell is located. */
    private String sheet;

	/** The row number of the cell, starting with <code>0</code>. */
	private int row;

	/** The column number of the cell, starting with <code>0</code>. */
	private int col;

    private String cell;

    /** a comment to be rendered as a tooltip **/
    private String comment = null;
    private MergeRegion commentRegion = null;
    private Color commentFillColor = null;

    /** See {@link #getCellStyleFrom()}. */
    private CellPosition cellStyleFrom;
    /** See {@link #getMergeRegion()}. */
    private MergeRegion mergeRegion;

	/** See {@link #getCellAlignment()} */
	private HorizontalAlignment horizontalAlignment = NO_HORIZONTAL_ALIGNMENT_SET;

	/** See {@link #getVerticalAlignment()} */
	private VerticalAlignment verticalAlignment = NO_VERTIVAL_ALIGNMENT_SET;

    /** See {@link #getBorderTop()}. */
    private String borderTop;
    /** See {@link #getBorderRight()}. */
    private String borderRight;
    /** See {@link #getBorderBottom()}. */
    private String borderBottom;
    /** See {@link #getBorderLeft()}. */
    private String borderLeft;

    /** See {@link #getRotation()}. */
	private short _rotation;

	private boolean autoWidth = true; 
 	private int width;

 	/**
 	 * holds the date format, as default, the standard format is used
 	 */
	private String dataFormat = NO_FORMAT_SET;


	/**
	 * Constructor to create a new excel value with given styles.
	 * 
	 * @see ExcelValue#ExcelValue(int, int, Object)
	 */
    public ExcelValue(int aRow, int aCol, Object aValue, Color aBackgroundColor) {
		this(aRow, aCol, aValue);
        setBackgroundColor(aBackgroundColor);
    }

	/**
	 * Constructor to create a new excel value without sheet name.
	 * 
	 * @see ExcelValue#ExcelValue(String, int, int, Object)
	 */
    public ExcelValue(int aRow, int aCol, Object aValue) {
		this(null, aRow, aCol, aValue);
    }

	/**
	 * Constructor to create a new excel value.
	 * 
	 * @see ExcelValue#ExcelValue(String, String, Object)
	 */
    public ExcelValue(String aCell, Object aValue) {
        this(null, aCell, aValue);
    }

	/**
	 * Constructor to create a new excel value.
	 * 
	 * @param aSheet
	 *        The name of the excel sheet to create value for. May be <code>null</code>.
	 * @param aCol
	 *        Column of the value starting with <code>0</code>.
	 * @param aRow
	 *        Row of the value starting with <code>0</code>.
	 */
    public ExcelValue(String aSheet, int aRow, int aCol, Object aValue) {
		super();
        this.sheet = aSheet;
        this.cell  = POIUtil.convertToCellName(aSheet, aRow, aCol);
        this.col   = aCol;
        this.row   = aRow;
		setValue(aValue);
    }

    /** Constructor to create a new excel value */
    public ExcelValue(String aSheet, String aCell, Object aValue) {
		super();
        this.sheet = aSheet;
        this.cell  = ((aSheet != null) ? aSheet + '!' : "") + aCell;
		setValue(aValue);

        int[] thePos = POIUtil.convertCellName(aCell);

        this.row = thePos[0];
        this.col = thePos[1];
    }

    /**
     * This method returns for a given sheet name the POI name (e.g. 'sheet1!A1').
     * 
     * @param aSheetName A sheet name.
     * @return Returns for a given sheet name the POI name.
     */
    public String getPOIName(String aSheetName) {
        return aSheetName + '!' + this.cell;
    }

    /**
     * @see POIUtil#getExcelColumnName(int)
     */
    public String getColAsString(){
    	return POIUtil.getExcelColumnName(this.col);
    }

    /** 
     * Return the cell identifier.
     */
    public String getCellPos() {
        return (this.cell);
    }

    /** 
     * @see #getSheet()
     */
    public void setSheet(String aSheet) {
    	this.sheet = aSheet;
    }
    
    /** 
     * Return the sheet this value is located in.
     * 
     * @return    The requested sheet.
     */
    public String getSheet() {
        return this.sheet;
    }

    /** 
     * Return the column this value is located in (java counting from 0).
     * 
     * @return    The requested column.
     */
    public int getCol() {
        return this.col;
    }

    /** 
     * Return the row this value is located in (java counting from 0).
     * 
     * @return    The requested row.
     */
    public int getRow() {
        return this.row;
    }

    /**
     * Sets a comment to this excelValue with default size, position and fill color for the
     * comment text area.
     *
     * @see #setComment(String, MergeRegion, Color)
     */
    public void setComment(String aComment){
        setComment(aComment, null, null);
    }

    /**
     * Sets a comment to this excelValue with default size and position for the comment text
     * area.
     *
     * @see #setComment(String, MergeRegion, Color)
     */
    public void setComment(String aComment, Color aFillColor) {
        setComment(aComment, null, aFillColor);
    }

    /**
     * Sets a comment to this excelValue with default fill color for the comment text area.
     *
     * @see #setComment(String, MergeRegion, Color)
     */
    public void setComment(String aComment, MergeRegion aRegion) {
        setComment(aComment, aRegion, null);
    }

    /**
     * Sets a comment to this excelValue. Therefore a comment text area will be created at
     * the given position. The comment will be shown as tooltip right beside the cell in the
     * size of the comment text area, but will be placed on the given position if the "edit
     * comment" option gets selected in excel.
     * 
     * @param aComment
     *        the comment to set
     * @param aRegion
     *        this defines the position and size of the comment text area. The MergeRegion
     *        contains 4 values, the first two to define the cell of the upper left corner
     *        of the comment text area, while the last two define the lower right corner.
     *        The values are 0 based row and cell numbers.
     * @param aFillColor
     *        the fill color of the comment text box
     */
    public void setComment(String aComment, MergeRegion aRegion, Color aFillColor) {
        comment = aComment;
        commentRegion = aRegion;
        commentFillColor = aFillColor;
    }

    /**
     * @see #setComment(String, MergeRegion, Color)
     */
    public String getComment() {
        return comment;
    }

    /**
     * @see #setComment(String, MergeRegion, Color)
     */
    public MergeRegion getCommentRegion() {
        return commentRegion;
    }

    /**
     * @see #setComment(String, MergeRegion, Color)
     */
    public Color getCommentFillColor() {
        return commentFillColor;
    }

	/**
     * Returns the background color of the cell and overwrites the template color.
     */
	@Override
	public Color getBackgroundColor() {
		return super.getBackgroundColor();
    }

    /**
	 * For available cell alignments see constants in {@link CellStyle}.
	 * 
	 * @param alignment
	 *        the cell alignment constant
	 */
	public void setCellAlignment(HorizontalAlignment alignment) {
		this.horizontalAlignment = alignment;
    }

	/**
	 * @see CellStyle#getAlignment()
	 */
	public HorizontalAlignment getCellAlignment() {
		return this.horizontalAlignment;
    }

    /**
	 * For available cell alignments see constants in {@link CellStyle}.
	 * 
	 * @param alignment
	 *        the cell alignment constant
	 */
	public void setVerticalAlignment(VerticalAlignment alignment) {
		this.verticalAlignment = alignment;
	}

	/**
	 * @see CellStyle#getVerticalAlignment()
	 */
	public VerticalAlignment getVerticalAlignment() {
		return this.verticalAlignment;
	}

    /**
     * This method returns the cell position of that cell that contains the style for this
     * excel value or <code>null</code>. <code>Null</code> means that the template
     * style is used.
     */
    public CellPosition getCellStyleFrom() {
        return this.cellStyleFrom;
    }

    /** See {@link #getCellStyleFrom()}. */
    public void setCellStyleFrom(CellPosition cellPosition) {
        this.cellStyleFrom = cellPosition;
    }

	/**
	 * Extracts the position information.
	 */
	public CellPosition position() {
		return new CellPosition(getSheet(), getRow(), getCol());
	}

    /**
     * This method returns the merge region for this excel value or <code>null</code>.
     */
    public MergeRegion getMergeRegion() {
        return this.mergeRegion;
    }

	/**
	 * Sets the {@link MergeRegion} if the given region spans more than one cell.
	 *
	 * @param colSpan
	 *        The number of columns to merge.
	 * @return This value for call-chaining.
	 */
	public ExcelValue setMergeRegion(int colSpan) {
		return setMergeRegion(colSpan, 1);
	}

	/**
	 * Sets the {@link MergeRegion} if the given region spans more than one cell.
	 *
	 * @param colSpan
	 *        The number of columns to merge.
	 * @param rowSpan
	 *        The number of rows to merge.
	 * @return This value for call-chaining.
	 */
	public ExcelValue setMergeRegion(int colSpan, int rowSpan) {
		if (colSpan > 1 || rowSpan > 1) {
			setMergeRegion(new MergeRegion(getRow(), getCol(), getRow() + rowSpan - 1, getCol() + colSpan - 1));
		}
		return this;
	}

    /** Sets the MergeRegion and returns the ExcelValue */
    public ExcelValue setMergeRegion(MergeRegion aMergeRegion) {
        this.mergeRegion = aMergeRegion;
        return this;
    }

	/**
	 * Gets the degree of rotation for the text in the cell.
	 * 
	 * @return Rotation degrees (between -90 and 90 degrees).
	 */
	public short getRotation() {
		return _rotation;
	}

    /**
	 * Sets the degree of rotation for the text in the cell.
	 * 
	 * @param rotation
	 *        Degrees (between -90 and 90 degrees).
	 */
	public void setRotation(short rotation) {
		_rotation = rotation;
	}

    @Override
	public String toString() {
		return "(" + this.cell + ")=" + getValue();
    }

	@Override
	public void clearStyle() {
		super.clearStyle();
        borderTop = null;
        borderLeft = null;
        borderRight = null;
        borderBottom = null;
		_rotation = 0;
		horizontalAlignment = NO_HORIZONTAL_ALIGNMENT_SET;
		verticalAlignment = NO_VERTIVAL_ALIGNMENT_SET;
		dataFormat = NO_FORMAT_SET;
    }

	/**
	 * Gets the style key of this excel value. This is required to identify two values
	 * with the same style.
	 *
	 * @return the cell style key
	 */
	public Object getStyleKey() {
		// Must include the fact whether the value is a date value. In such case the format is never
		// null (see #getDateFormat()).
	    return TupleFactory.newTuple(new Object[] {
			getBackgroundColor(), horizontalAlignment, verticalAlignment,
			Short.valueOf(getRotation()),
			getFontStyle(), isTextWrap(),
			borderTop, borderLeft, borderRight, borderBottom,
			getDataFormat(),
			Boolean.valueOf(hasDateValue())
	    });
	}

	@Override
	public boolean equals(Object aObject) {
		if (aObject == this) {
			return true;
		}
	    if (!(aObject instanceof ExcelValue)) return false;
	    ExcelValue theObject = ((ExcelValue)aObject);
		Object value = getValue();
		return (value == null ? theObject.getValue() == null : value.equals(theObject.getValue()))
	            && this.cell.equals(theObject.cell) && getStyleKey().equals(theObject.getStyleKey());
	}

	@Override
	public int hashCode() {
		Object value = getValue();
	    return value == null ? 0 : value.hashCode() + cell.hashCode() + getStyleKey().hashCode();
	}

    /**
     * Checks whether this excel cell has an reference style.
     *
     * @return <code>true</code>, if the given excel value use the same style like an
     *         other excel value, <code>false</code> otherwise.
     */
    protected boolean hasReferenceStyle() {
        return getCellStyleFrom() != null;
    }

	@Override
	public boolean hasIndividualStyle() {
		return super.hasIndividualStyle() ||
			verticalAlignment != NO_VERTIVAL_ALIGNMENT_SET ||
			horizontalAlignment != NO_HORIZONTAL_ALIGNMENT_SET ||
			hasIndividualBorderStyle() ||
			getRotation() != 0 ||
			hasDataFormat() ||
			hasDateValue();
    }

    /**
     * Checks whether this excel cell has an individual border style.
     *
     * @return <code>true</code> if the excel cell has an individual border style
     */
    public boolean hasIndividualBorderStyle() {
        return getBorderTop() != null ||
               getBorderLeft() != null ||
               getBorderRight() != null ||
               getBorderBottom() != null;
    }

	/**
	 * Define the position of a cell within a sheet (optional with sheet information). 
	 * 
	 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class CellPosition {

	    // Members

		private final int row;
	    private final int column;
		private final String sheet;
        
	    // Constructors
        
        /** 
         * Creates a {@link CellPosition}.
         */
        public CellPosition(int aRow, int aColumn) {
            this(null, aRow, aColumn);
        }

        /**
         * Creates a {@link CellPosition}.
         */
        public CellPosition(String aCell) {
            int[] theCell = POIUtil.convertCellName(aCell);
            int   theDel  = aCell.indexOf('!');

            this.sheet  = (theDel > -1) ? aCell.substring(0, theDel) : null;
            this.row    = theCell[0];
            this.column = theCell[1];
        }

        /** 
         * Creates a {@link CellPosition}.
         */
        public CellPosition(String aSheet, int aRow, int aColumn) {
        	this.sheet  = aSheet;
        	this.row    = aRow;
        	this.column = aColumn;
        }

        // Methods

		/**
		 * This method returns the sheet.
		 * 
		 * @return    Returns the sheet.
		 */
		public String getSheet() {
			return this.sheet;
		}

		/** 
         * Return the requested row (first row will be 0).
         * 
         * @return    The requested row.
         */
        public int getRow() {
            return this.row;
        }

        /** 
         * Return the requested column (first column will be 0).
         * 
         * @return    The requested column.
         */
        public int getColumn() {
            return this.column;
        }

        /** 
         * Return the excel based representation of this cell position.
         * 
         * @return    The string representation.
         * @see       POIUtil#convertToCellName(int, int)
         */
        public String getCell() {
            return POIUtil.convertToCellName(row, column);
        }
	    
	    @Override
		public String toString() {
	    	if (this.sheet != null) {
	    		return "CellPosition [sheet='" + sheet + "', row=" + row + ", column=" + column + "]";
	    	}
	    	else { 
	    		return "CellPosition [row=" + row + ", column=" + column + "]";
	    	}
		}

        @Override
		public boolean equals(Object aObj) {
			if (aObj == this) {
				return true;
			}
            if (aObj instanceof CellPosition) {
                CellPosition thePos = (CellPosition)aObj;
                return sheet == thePos.sheet && this.row == thePos.row && this.column == thePos.column;
            }
            return false;
        }

        @Override
		public int hashCode() {
            return sheet.hashCode() + row + column;
        }

	}

	/**
	 * Define a region where all cells have been merged to one. 
	 * 
	 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class MergeRegion {
	    
	    // Members

		private int fromRow;
	    private int fromCol;
	    private int toRow;
	    private int toCol;
        
	    // Constructors
        
        /** 
         * Creates a {@link MergeRegion}.
         */
        public MergeRegion(int aFromRow, int aFromCol, int aToRow, int aToCol) {
            this.fromRow = aFromRow;
            this.fromCol = aFromCol;
            this.toRow   = aToRow;
            this.toCol   = aToCol;
        }

        // Methods
        
        /** 
         * The start row.
         * 
         * @return    The requested value.
         */
        public int getFromRow() {
            return this.fromRow;
        }

        /** 
         * The start column.
         * 
         * @return    The requested value.
         */
        public int getFromCol() {
            return this.fromCol;
        }

        /** 
         * The end row.
         * 
         * @return    The requested value.
         */
        public int getToRow() {
            return this.toRow;
        }

        /** 
         * The end column.
         * 
         * @return    The requested value.
         */
        public int getToCol() {
            return this.toCol;
        }
	    
	    @Override
		public String toString() {
			return "MergeRegion [fromRow=" + fromRow + ", fromCol=" + fromCol + ", toRow=" + toRow + ", toCol=" + toCol + "]";
		}

        @Override
		public boolean equals(Object aObj) {
			if (aObj == this) {
				return true;
			}
            if (aObj instanceof MergeRegion) {
                MergeRegion theReg = (MergeRegion)aObj;
                return this.fromRow == theReg.fromRow && this.fromCol == theReg.fromCol
                    && this.toRow == theReg.toRow && this.toCol == theReg.toCol;
            }
            return false;
        }

        @Override
		public int hashCode() {
            return fromRow + fromCol + toRow + toCol;
        }

		/**
		 * Creates a {@link CellRangeAddress} from this {@link MergeRegion}.
		 */
		public final CellRangeAddress createCellRangeAddress() {
        	return new CellRangeAddress(getFromRow(), getToRow(), getFromCol(), getToCol());
        }

	}

	/**
	 * Returns the borderTop.
	 */
	public String getBorderTop() {
		return this.borderTop;
	}

	/**
	 * @param borderTop
	 *        The borderTop to set.
	 * @see #setBorder(String)
	 */
	public void setBorderTop(String borderTop) {
		this.borderTop = borderTop;
	}

	/**
	 * Returns the borderRight.
	 */
	public String getBorderRight() {
		return this.borderRight;
	}

	/**
	 * @param borderRight
	 *        The borderRight to set.
	 * 
	 * @see #setBorder(String)
	 */
	public void setBorderRight(String borderRight) {
		this.borderRight = borderRight;
	}

	/**
	 * Returns the borderBottom.
	 */
	public String getBorderBottom() {
		return this.borderBottom;
	}

	/**
	 * @param borderBottom
	 *        The borderBottom to set.
	 * 
	 * @see #setBorder(String)
	 */
	public void setBorderBottom(String borderBottom) {
		this.borderBottom = borderBottom;
	}

	/**
	 * Returns the borderLeft.
	 */
	public String getBorderLeft() {
		return this.borderLeft;
	}

	/**
	 * @param borderLeft
	 *        The borderLeft to set.
	 * 
	 * @see #setBorder(String)
	 */
	public void setBorderLeft(String borderLeft) {
		this.borderLeft = borderLeft;
	}

	/**
	 * Sets all borders.
	 * 
	 * <p>
	 * Possible values are {@link #BORDER_DASHED}, {@link #BORDER_DOTTED}, {@link #BORDER_THICK}, or
	 * {@link #BORDER_THIN}.
	 * </p>
	 */
	public void setBorder(String border) {
	    setBorderTop(border);
	    setBorderLeft(border);
	    setBorderRight(border);
	    setBorderBottom(border);
	}

	/**
	 * @param autoWidth The autoWidth to set.
	 */
	public void setAutoWidth(boolean autoWidth) {
		this.autoWidth = autoWidth;
	}

	/**
	 * Returns the autoWidth.
	 */
	public boolean isAutoWidth() {
		return autoWidth;
	}

	/**
	 * @param width The width to set.
	 */
	public void setCellWidth(int width) {
		this.width = width;
	}

	/**
	 * Returns the width.
	 */
	public int getCellWidth() {
		return width;
	}

	/**
	 * returns the date format to be used. Never <code>null</code>. If none was set
	 *         {@link #DATE_FORMAT_STANDARD} is returned.
	 * 
	 */
	public String getDateFormat() {
		if (!hasDataFormat()) {
			return ExcelValue.DATE_FORMAT_STANDARD;
		}
		return getDataFormat();
	}

	/**
	 * Returns the format for the cell, or {@link #NO_FORMAT_SET} when nothing was set before.
	 */
	public String getDataFormat() {
		return dataFormat;
	}

	/**
	 * Whether a special format was set using {@link #setDataFormat(String)}.
	 */
	public boolean hasDataFormat() {
		return dataFormat != NO_FORMAT_SET;
	}

	/**
	 * Determines whether the value is a date value.
	 * 
	 * <p>
	 * In such case the method {@link #getDateFormat()} must be used to determine format because in
	 * such case always a format must be set to the excel cell style.
	 * </p>
	 */
	public boolean hasDateValue() {
		Object value = getValue();
		return value instanceof Date || value instanceof Calendar;
	}

	/**
	 * Sets the format for the value of this {@link ExcelValue}.
	 * 
	 * @param format
	 *        The new value of {@link #getDataFormat()}.
	 */
	public void setDataFormat(String format) {
		dataFormat = format;
	}

	/**
	 * Returns an array containing the {@link ExcelValue}s in the given collection in
	 * {@link Collection#iterator() iterators} order.
	 * 
	 * @param values
	 *        must not be <code>null</code>.
	 */
	public static ExcelValue[] toArray(Collection<? extends ExcelValue> values) {
		int size = values.size();
		switch (size) {
			case 0:
				return new ExcelValue[0];
			case 1:
				return new ExcelValue[] { values.iterator().next() };
			default:
				return values.toArray(new ExcelValue[size]);
		}
	}

}
