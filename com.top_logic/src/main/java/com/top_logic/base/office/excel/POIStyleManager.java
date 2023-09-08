/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.formula.CollaboratingWorkbooksEnvironment.WorkbookNotFoundException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;

import com.top_logic.basic.col.TupleFactory;

/**
 * Instances of this class are responsible for caching {@link CellStyle}
 * instance for a POI workbook.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public class POIStyleManager {

    /**
     * This class serves as a temporary style dummy which can be configured
     * before registering it with a cache.
     * 
     * @author <a href=mailto:wta@top-logic.com>wta</a>
     */
	private static final class TempCellStyle implements CellStyle {
		
		/**
		 * @see #getAlignment()
		 */
		private HorizontalAlignment _alignHorizontal = HorizontalAlignment.LEFT;

		/**
		 * @see #getBorderBottom()
		 */
		private BorderStyle _borderBottom = BorderStyle.NONE;

		/**
		 * @see #getBorderLeft()
		 */
		private BorderStyle _borderLeft = BorderStyle.NONE;

		/**
		 * @see #getBorderRight()
		 */
		private BorderStyle _borderRight = BorderStyle.NONE;

		/**
		 * @see #getBorderTop()
		 */
		private BorderStyle _borderTop = BorderStyle.NONE;

		/**
		 * @see #getDataFormat()
		 */
		private short _dataFormat = 0;

		/**
		 * @see #getFillBackgroundColor()
		 */
		private short _bgColor = IndexedColors.AUTOMATIC.getIndex();

		/**
		 * @see #getFillForegroundColor()
		 */
		private short _fgColor = IndexedColors.AUTOMATIC.getIndex();

		/**
		 * @see #getFillPattern()
		 */
		private FillPatternType _fillPattern = FillPatternType.NO_FILL;

		/**
		 * @see #getFontIndex()
		 */
		private int _fontIndex = 0;

		/**
		 * @see #getHidden()
		 */
		private boolean _hidden = false;

		/**
		 * @see #getIndention()
		 */
		private short _indention = 0;

		/**
		 * @see #getIndex()
		 */
		private short _index = -1;

		/**
		 * @see #getTopBorderColor()
		 */
		private short _borderColorTop = -1;

		/**
		 * @see #getBottomBorderColor()
		 */
		private short _borderColorBottom = -1;

		/**
		 * @see #getLeftBorderColor()
		 */
		private short _borderColorLeft = -1;

		/**
		 * @see #getRightBorderColor()
		 */
		private short _borderColorRight = -1;

		/**
		 * @see #getLocked()
		 */
		private boolean _locked = false;

		/**
		 * @see #getRotation()
		 */
		private short _rotation = 0;

		/**
		 * @see #getVerticalAlignment()
		 */
		private VerticalAlignment _alignVertical = VerticalAlignment.TOP;

		/**
		 * @see #getWrapText()
		 */
		private boolean _wrapText = false;

		private boolean _shrinkToFit;

		private boolean _quotePrefix;

		/**
		 * Create a new {@link TempCellStyle}.
		 */
		/* package private */public TempCellStyle() {
		}

        @Override
		public void cloneStyleFrom(final CellStyle style) {
			_alignHorizontal = style.getAlignment();
			_borderBottom = style.getBorderBottom();
			_borderLeft = style.getBorderLeft();
			_borderRight = style.getBorderRight();
			_borderTop = style.getBorderTop();
			_borderColorBottom = style.getBottomBorderColor();
			_dataFormat = style.getDataFormat();
			_bgColor = style.getFillBackgroundColor();
			_fgColor = style.getFillForegroundColor();
			_fillPattern = style.getFillPattern();
			_fontIndex = style.getFontIndex();
			_hidden = style.getHidden();
			_indention = style.getIndention();
			_borderColorLeft = style.getLeftBorderColor();
			_locked = style.getLocked();
			_borderColorRight = style.getRightBorderColor();
			_rotation = style.getRotation();
			_borderColorTop = style.getTopBorderColor();
			_alignVertical = style.getVerticalAlignment();
			_wrapText = style.getWrapText();
        }

        @Override
		public HorizontalAlignment getAlignment() {
			return _alignHorizontal;
        }

        @Override
		public BorderStyle getBorderBottom() {
			return _borderBottom;
        }

        @Override
		public BorderStyle getBorderLeft() {
			return _borderLeft;
        }

        @Override
		public BorderStyle getBorderRight() {
			return _borderRight;
        }

        @Override
		public BorderStyle getBorderTop() {
			return _borderTop;
        }

        @Override
        public short getBottomBorderColor() {
			return _borderColorBottom;
        }

        @Override
        public short getDataFormat() {
			return _dataFormat;
        }

        @Override
        public String getDataFormatString() {
			throw new UnsupportedOperationException();
        }
        
        @Override
        public short getFillBackgroundColor() {
			return _bgColor;
        }

        @Override
        public short getFillForegroundColor() {
			return _fgColor;
        }
        
        @Override
        public Color getFillBackgroundColorColor() {
			return getIndexedColor(_bgColor);
        }
        
        @Override
        public Color getFillForegroundColorColor() {
			return getIndexedColor(_fgColor);
        }

		private HSSFColor getIndexedColor(short index) {
			/* index by integer objects. Using with Short objects won't find anything. */
			Integer indexAsInteger = Integer.valueOf(index);
			return HSSFColor.getIndexHash().get(indexAsInteger);
		}

        @Override
		public FillPatternType getFillPattern() {
			return _fillPattern;
        }

        @Override
		public int getFontIndex() {
			return _fontIndex;
        }

        @Override
        public boolean getHidden() {
			return _hidden;
        }

        @Override
        public short getIndention() {
			return _indention;
        }

        @Override
        public short getIndex() {
			return _index;
        }

        @Override
        public short getLeftBorderColor() {
			return _borderColorLeft;
        }

        @Override
        public boolean getLocked() {
			return _locked;
        }

        @Override
        public short getRightBorderColor() {
			return _borderColorRight;
        }

        @Override
        public short getRotation() {
			return _rotation;
        }

        @Override
        public short getTopBorderColor() {
			return _borderColorTop;
        }

        @Override
		public VerticalAlignment getVerticalAlignment() {
			return _alignVertical;
        }

        @Override
        public boolean getWrapText() {
			return _wrapText;
        }

        @Override
		public void setAlignment(final HorizontalAlignment alignHorizontal) {
			_alignHorizontal = alignHorizontal;
        }

        @Override
		public void setBorderBottom(final BorderStyle borderBottom) {
			_borderBottom = borderBottom;
        }

        @Override
		public void setBorderLeft(final BorderStyle borderLeft) {
			_borderLeft = borderLeft;
        }

        @Override
		public void setBorderRight(final BorderStyle borderRight) {
			_borderRight = borderRight;
        }

        @Override
		public void setBorderTop(final BorderStyle borderTop) {
			_borderTop = borderTop;
        }

        @Override
		public void setBottomBorderColor(final short borderColorBottom) {
			_borderColorBottom = borderColorBottom;
        }

        @Override
		public void setDataFormat(final short dataFormat) {
			_dataFormat = dataFormat;
        }

        @Override
		public void setFillBackgroundColor(final short bgColor) {
			_bgColor = bgColor;
        }

        @Override
		public void setFillBackgroundColor(Color color) {
			setFillBackgroundColor(index(color));
		}

		@Override
		public void setFillForegroundColor(final short fgColor) {
			_fgColor = fgColor;
        }

        @Override
		public void setFillForegroundColor(Color color) {
			setFillForegroundColor(index(color));
		}

		private short index(Color color) {
			short index;
			if (color instanceof HSSFColor) {
				index = ((HSSFColor) color).getIndex2();
			} else {
				index = color == null ? IndexedColors.AUTOMATIC.getIndex() : ((XSSFColor) color).getIndexed();
			}
			return index;
		}

		@Override
		public void setFillPattern(final FillPatternType fillPattern) {
			_fillPattern = fillPattern;
        }

        @Override
		public void setFont(final Font font) {
			_fontIndex = font.getIndex();
        }

        @Override
		public void setHidden(final boolean hidden) {
			_hidden = hidden;
        }

        @Override
		public void setIndention(final short indention) {
			_indention = indention;
        }

        @Override
		public void setLeftBorderColor(final short borderColorLeft) {
			_borderColorLeft = borderColorLeft;
        }

        @Override
		public void setLocked(final boolean locked) {
			_locked = locked;
        }

        @Override
		public void setRightBorderColor(final short color) {
			_borderColorRight = color;
        }

        @Override
		public void setRotation(final short rotation) {
			_rotation = rotation;
        }

        @Override
		public void setTopBorderColor(final short color) {
			_borderColorTop = color;
        }

        @Override
		public void setVerticalAlignment(final VerticalAlignment alignVertical) {
			_alignVertical = alignVertical;
        }

        @Override
		public void setWrapText(final boolean wrapText) {
			_wrapText = wrapText;
        }

		@Override
		public void setQuotePrefixed(boolean quotePrefix) {
			_quotePrefix = quotePrefix;
		}

		@Override
		public boolean getQuotePrefixed() {
			return _quotePrefix;
		}

		@Override
		public void setShrinkToFit(boolean shrinkToFit) {
			_shrinkToFit = shrinkToFit;
		}

		@Override
		public boolean getShrinkToFit() {
			return _shrinkToFit;
		}

		/**
		 * @see org.apache.poi.ss.usermodel.CellStyle#getFontIndexAsInt()
		 * @deprecated Use {@link #getFontIndex()} instead.
		 */
		@SuppressWarnings("deprecation")
		@Deprecated
		@Override
		public int getFontIndexAsInt() {
			return getFontIndex();
		}

    }

    /**
	 * Create a key which reflects the specified cell style properties in a way that
	 * <code>createKey(A).equals(createKey(B))</code> will return <code>true</code> for equal styles
	 * even if <code>A.equals(B)</code> is not <code>true</code>.
	 * 
	 * @param style
	 *        the style to create a key for
	 * @return the key for the specified style
	 */
	public static Object createKey(final CellStyle style) {
        return TupleFactory.newTuple(
			style.getAlignment(),
			style.getBorderBottom(),
			style.getBorderLeft(),
			style.getBorderRight(),
			style.getBorderTop(),
			style.getBottomBorderColor(),
			style.getDataFormat(),
			style.getFillBackgroundColor(),
			style.getFillForegroundColor(),
			style.getFillPattern(),
			style.getFontIndex(),
			style.getHidden(),
			style.getIndention(),
			style.getLeftBorderColor(),
			style.getLocked(),
			style.getRightBorderColor(),
			style.getRotation(),
			style.getTopBorderColor(),
			style.getVerticalAlignment(),
			style.getWrapText(),
			style.getQuotePrefixed(),
			style.getShrinkToFit());
    }

    /**
     * This is the actual monster that will be fed with appropriate style
     * coordinates.
     * 
     * @see #putStyle(Object, CellStyle)
     */
	private final Map<Object, CellStyle> _cache = new HashMap<>();

    /**
     * The workbook to create the style cache for.
     */
	final Workbook _workbook;
    
	/**
	 * Creates an instance of this class to cache the cell styles for the specified workbook.
	 * 
	 * @param book
	 *        the {@link WorkbookNotFoundException} to manage the cell style cache for
	 * @throws IllegalArgumentException
	 *         if the argument is {@code null}
	 */
	public POIStyleManager(final Workbook book) throws IllegalArgumentException {
		if (book == null) {
			throw new IllegalArgumentException("cannot create style cache for workbook: " + book);
        }

		_workbook = book;
    }
    
    /**
	 * Returns the cached {@link CellStyle} instance which has been registered using the specified
	 * key.
	 * 
	 * @param key
	 *        the key to retrieve the cell style for
	 * @return the {@link CellStyle} registered using the specified key or {@code null} if no style
	 *         has been registered yet
	 * @see #putStyle(Object, CellStyle)
	 */
	public CellStyle getStyle(final Object key) {
		return _cache.get(key);
    }

    /**
     * the {@link Workbook} instance the receiver manages cached values
     *         for
     */
    public Workbook getWorkbook() {
		return _workbook;
    }
    
    /**
     * This method creates a temporary style which can be freely modified. The
     * newly created style is NOT registered and thus will not be reflected in
     * the receiver's workbook at all.
     * 
     * <p>
     * Once the returned instance is configured properly, it can be registered
     * using the {@link #putStyle(Object, CellStyle)} method.
     * </p>
     * 
     * @return the temporary style
     * @see #putStyle(Object, CellStyle)
     */
    public CellStyle newStyle() {
        return new TempCellStyle();
    }

    /**
     * This method creates a temporary style as a copy of the specified one. The
     * new style can be freely modified. The newly created style is NOT
     * registered and thus will not be reflected in the receiver's workbook at
     * all.
     * 
     * <p>
     * Once the returned instance is configured properly, it can be registered
     * using the {@link #putStyle(Object, CellStyle)} method.
     * </p>
     * 
     * @return the temporary style
     * @see #putStyle(Object, CellStyle)
     */
	public CellStyle newStyle(final CellStyle style) {
		final TempCellStyle copy = new TempCellStyle();
		copy.cloneStyleFrom(style);
		return copy;
    }
    
	/**
	 * Registers a <strong>copy</strong> of the specified cell style using the specified key.
	 * 
	 * @param key
	 *        the key to register the specified style for
	 * @param style
	 *        the {@link CellStyle} to be registered
	 * @return the registered {@link CellStyle} which can be used in the workbook
	 * @throws IllegalArgumentException
	 *         if the specified style is not supported
	 * @see #getStyle(Object)
	 */
	public CellStyle putStyle(final Object key, final CellStyle style) throws IllegalArgumentException {
		if (style == null) {
			throw new IllegalArgumentException("unsupported style: " + style);
        }
        
        // do NOT store the specified style directly but
        // create a new one and register it with the workbook.
		final CellStyle clone = _workbook.createCellStyle();

		cloneBorder(style, clone);
		cloneBorderColor(style, clone);

		clone.setAlignment(style.getAlignment());
		clone.setDataFormat(style.getDataFormat());
		clone.setFillBackgroundColor(style.getFillBackgroundColor());
		clone.setFillForegroundColor(style.getFillForegroundColor());
		clone.setFillPattern(style.getFillPattern());
		clone.setFont(_workbook.getFontAt(style.getFontIndex()));
		clone.setHidden(style.getHidden());
		clone.setIndention(style.getIndention());
		clone.setLocked(style.getLocked());
		clone.setRotation(style.getRotation());
		clone.setVerticalAlignment(style.getVerticalAlignment());
		clone.setWrapText(style.getWrapText());
		clone.setQuotePrefixed(style.getQuotePrefixed());
		clone.setShrinkToFit(style.getShrinkToFit());
        
		_cache.put(key, clone);
		return clone;
    }

	/**
	 * Create a new cell style, adapt the given color to it and store this in the given style
	 * manager.
	 * 
	 * @param key
	 *        The accessing key for the new created style, must not be {@code null}.
	 * @param color
	 *        The {@link IndexedColors} to be used, must not be {@code null}.
	 * @param style
	 *        The {@link CellStyle} to create a new one for, must not be {@code null}.
	 * @return The new created style, never {@code null}.
	 */
	public CellStyle adaptCellStyle(final String key, final IndexedColors color, final CellStyle style) {
		final CellStyle tmpStyle = newStyle(style);

		tmpStyle.setFillBackgroundColor(color.getIndex());
		tmpStyle.setFillForegroundColor(color.getIndex());
		tmpStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		return putStyle(key, tmpStyle);
	}

	/**
	 * Clone border colors (need this due poor performance in POI implementation, see
	 * https://issues.apache.org/bugzilla/show_bug.cgi?id=54593).
	 * 
	 * @param source
	 *        The {@link CellStyle} to get the values from
	 * @param target
	 *        The {@link CellStyle} to put the values to
	 */
	protected void cloneBorderColor(final CellStyle source, final CellStyle target) {
		short color;

		color = source.getBottomBorderColor();
		if (target.getBottomBorderColor() != color) {
			target.setBottomBorderColor(color);
        }
		color = source.getLeftBorderColor();
		if (target.getLeftBorderColor() != color) {
			target.setLeftBorderColor(color);
        }        
		color = source.getRightBorderColor();
		if (target.getRightBorderColor() != color) {
			target.setRightBorderColor(color);
        }
		color = source.getTopBorderColor();
		if (target.getTopBorderColor() != color) {
			target.setTopBorderColor(color);
        }
    }

	/**
	 * Clone border sizes (need this due poor performance in POI implementation, see
	 * https://issues.apache.org/bugzilla/show_bug.cgi?id=54593).
	 * 
	 * @param source
	 *        The {@link CellStyle} to get the values from
	 * @param target
	 *        The {@link CellStyle} to put the values to
	 */
	protected void cloneBorder(final CellStyle source, final CellStyle target) {
		BorderStyle border;

		border = source.getBorderBottom();
		if (target.getBorderBottom() != border) {
			target.setBorderBottom(border);
        }
		border = source.getBorderLeft();
		if (target.getBorderLeft() != border) {
			target.setBorderLeft(border);
        }
		border = source.getBorderRight();
		if (target.getBorderRight() != border) {
			target.setBorderRight(border);
        }
		border = source.getBorderTop();
		if (target.getBorderTop() != border) {
			target.setBorderTop(border);
        }
    }
}