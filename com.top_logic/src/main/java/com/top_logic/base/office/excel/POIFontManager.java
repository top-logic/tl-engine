/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.basic.col.TupleFactory;

/**
 * Instances of this class are responsible for managing {@link Font}s for an
 * excel workbook.
 * 
 * @author <a href="mailto:wta@top-logic.com">wta</a>
 */
public class POIFontManager {

    /**
	 * A {@link Map} of {@link Font}s by an {@link Object} key.
	 * 
	 * @see #putFont(Object, Font)
	 */
	private final Map<Object, Font> _cache = new HashMap<>();

    /**
	 * The {@link Workbook} to create the style cache for.
	 */
	private final Workbook _workbook;

    /**
	 * Creates an instance of this class to cache the fonts for the specified workbook.
	 * 
	 * @param book
	 *        the {@link Workbook} to manage the font cache for
	 * @throws IllegalArgumentException
	 *         if the argument is {@code null}
	 */
	public POIFontManager(final Workbook book) throws IllegalArgumentException {
		if (book == null) {
			throw new IllegalArgumentException("cannot create font cache for workbook: " + book);
        }

		_workbook = book;
    }

    /**
	 * Returns the cached {@link Font} instance which has been registered using the specified key.
	 * 
	 * @param key
	 *        the key to retrieve the font for
	 * @return the {@link Font} registered using the specified key or {@code null} if no font has
	 *         been registered yet
	 * @see #putFont(Object, Font)
	 */
	public Font getFont(final Object key) {
		return _cache.get(key);
    }

    /**
     * Returns the {@link Workbook} instance the receiver manages cached values
     * for.
     * 
     * @return the {@link Workbook} instance the receiver manages cached values
     *         for
     */
    public Workbook getWorkbook() {
		return _workbook;
    }
    
    /**
	 * Registers a <strong>copy</strong> of the specified font using the specified key.
	 * 
	 * @param key
	 *        the key to register the specified font for
	 * @param font
	 *        the {@link Font} to be registered
	 * @return the registered font
	 * @see #getFont(Object)
	 */
	public Font putFont(final Object key, final Font font) throws IllegalArgumentException {
		if (font == null) {
			throw new IllegalArgumentException("unsupported font: " + font);
        }

        // do NOT store the specified style directly but
        // create a new one and register it with the workbook.
		final Font clone = _workbook.createFont();
        
		clone.setBold(font.getBold());
		clone.setCharSet(font.getCharSet());
		clone.setColor(font.getColor());
		clone.setFontHeight(font.getFontHeight());
		clone.setFontHeightInPoints(font.getFontHeightInPoints());
		clone.setFontName(font.getFontName());
		clone.setItalic(font.getItalic());
		clone.setStrikeout(font.getStrikeout());
		clone.setTypeOffset(font.getTypeOffset());
		clone.setUnderline(font.getUnderline());
        
		_cache.put(key, clone);
		return clone;
    }

    /**
	 * This method creates a temporary font which can be freely modified. The newly created font is
	 * NOT registered and thus will not be reflected in the receiver's workbook at all.
	 * 
	 * <p>
	 * Once the returned instance is configured properly, it can be registered using the
	 * {@link #putFont(Object, Font)} method.
	 * </p>
	 * 
	 * @return the temporary {@link Font}
	 * @see #putFont(Object, Font)
	 */
    public Font createFont() {
        return new TempFont();
    }

	/**
	 * This method creates a temporary copy of the specified font which can be freely modified. The
	 * newly created font is NOT registered and thus will not be reflected in the receiver's
	 * workbook at all.
	 * 
	 * <p>
	 * Once the returned instance is configured properly, it can be registered using the
	 * {@link #putFont(Object, Font)} method.
	 * </p>
	 * 
	 * @return the temporary copy {@link Font}
	 * @see #putFont(Object, Font)
	 */
	public Font createFont(final Font font) {
		return new TempFont(font);
    }
    
    /**
	 * Create a key which reflects the specified font properties in a way that
	 * <code>createKey(A).equals(createKey(B))</code> will return <code>true</code> for equal fonts
	 * even if <code>A.equals(B)</code> is not <code>true</code>.
	 * 
	 * @param font
	 *        the {@link Font} to create a key for
	 * @return the key for the specified font
	 */
	public static Object createKey(final Font font) {
		final Object[] props = new Object[] {
			Boolean.valueOf(font.getBold()),
			font.getCharSet(),
			font.getColor(),
			font.getFontHeightInPoints(),
			font.getFontName(),
			font.getItalic(),
			font.getStrikeout(),
			font.getTypeOffset(),
			font.getUnderline(),
        };
        
		return TupleFactory.newTuple(props);
    }
    
    /**
     * This class serves as a temporary font dummy which can be configured
     * before registering it with a cache.
     * 
     * @author <a href=mailto:wta@top-logic.com>wta</a>
     */
    private static final class TempFont implements Font {

		/**
		 * @see #getBold()
		 */
		private boolean _bold = false;

		/**
		 * @see #getCharSet()
		 */
		private int _charSet = Font.DEFAULT_CHARSET;

		/**
		 * @see #getColor()
		 */
		private short _color = Font.COLOR_NORMAL;

		/**
		 * @see #getFontHeightInPoints()
		 */
		private short _fontHeightPoints = 10;

		/**
		 * @see #getFontName()
		 */
		private String _fontName = "Arial";

		/**
		 * @see #getIndex()
		 */
		private short _index = -1;

		/**
		 * @see #getItalic()
		 */
		private boolean _italic = false;

		/**
		 * @see #getStrikeout()
		 */
		private boolean _strikeout = false;

		/**
		 * @see #getTypeOffset()
		 */
		private short _typeOffset = Font.SS_NONE;

		/**
		 * @see #getUnderline()
		 */
		private byte _underline = Font.U_NONE;

        /**
         * Creates a default instance of this class.
         */
		/* package protected */TempFont() {
        	
        }
        
        /**
		 * Creates an instance of this class as a copy of the specified one.
		 * 
		 * @param font
		 *        the {@link Font} to copy the values from.
		 */
		/* package protected */TempFont(final Font font) {
			_bold = font.getBold();
			_charSet = font.getCharSet();
			_color = font.getColor();
			_fontHeightPoints = font.getFontHeightInPoints();
			_fontName = font.getFontName();
			_italic = font.getItalic();
			_strikeout = font.getStrikeout();
			_typeOffset = font.getTypeOffset();
			_underline = font.getUnderline();
		}

        @Override
		public boolean getBold() {
			return _bold;
        }

        @Override
        public int getCharSet() {
			return _charSet;
        }

        @Override
        public short getColor() {
			return _color;
        }

        @Override
        public short getFontHeight() {
			return (short) (_fontHeightPoints * 20);
        }

        @Override
        public short getFontHeightInPoints() {
			return _fontHeightPoints;
        }

        @Override
        public String getFontName() {
			return _fontName;
        }

        @Override
		public int getIndex() {
			return _index;
        }

        @Override
        public boolean getItalic() {
			return _italic;
        }

        @Override
        public boolean getStrikeout() {
			return _strikeout;
        }

        @Override
        public short getTypeOffset() {
			return _typeOffset;
        }

        @Override
        public byte getUnderline() {
			return _underline;
        }

        @Override
		public void setBold(boolean bold) {
			_bold = bold;
        }

        @Override
		public void setCharSet(final int charSet) {
			_charSet = charSet;
        }

        @Override
		public void setCharSet(final byte charSet) {
			_charSet = charSet;
        }
        
        @Override
		public void setColor(final short color) {
			_color = color;
        }

        @Override
		public void setFontHeight(final short fontHeight) {
			_fontHeightPoints = (short) (fontHeight / 20);
        }

        @Override
		public void setFontHeightInPoints(final short fontHeightInPoints) {
			_fontHeightPoints = fontHeightInPoints;
        }

        @Override
		public void setFontName(final String fontName) {
			_fontName = fontName;
        }

        @Override
		public void setItalic(final boolean italic) {
			_italic = italic;
        }

        @Override
		public void setStrikeout(final boolean strikeout) {
			_strikeout = strikeout;
        }

        @Override
		public void setTypeOffset(final short typeOffset) {
			_typeOffset = typeOffset;
        }

        @Override
		public void setUnderline(final byte underline) {
			_underline = underline;
        }

		/**
		 * @see org.apache.poi.ss.usermodel.Font#getIndexAsInt()
		 * @deprecated Use {@link #getIndex()} instead.
		 */
		@SuppressWarnings("deprecation")
		@Deprecated
		@Override
		public int getIndexAsInt() {
			return getIndex();
		}
    }
}
