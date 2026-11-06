/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.awt.Color;
import java.util.List;

import org.apache.poi.hslf.usermodel.HSLFSimpleShape;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.sl.usermodel.Hyperlink;
import org.apache.poi.sl.usermodel.LineDecoration;
import org.apache.poi.sl.usermodel.LineDecoration.DecorationShape;
import org.apache.poi.sl.usermodel.LineDecoration.DecorationSize;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.SimpleShape;
import org.apache.poi.sl.usermodel.StrokeStyle;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCap;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCompound;
import org.apache.poi.sl.usermodel.StrokeStyle.LineDash;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.sl.usermodel.TextShape;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.xslf.usermodel.XSLFSimpleShape;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

/**
 * The {@link PptCell} is most likely a holder for format options. 
 * 
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class PptCell {
	
    // TODO where is the difference between value and data?
    // a cell should have one data object, and the printer should deceide, what to do with it
    private String  name;
	private String  value;
	private Object  data;
	private double  width;  // width should be managed by table?
	private double  height; // height should be managed by table?
	private PptFont      font;
	private PptCellStyle style;
	
	
	public PptCell(String aName, String aVal) {
		this.name = aName;
		this.value = aVal;
	}

	/** The name. */
	public String getName() {
    	return (name);
    }

	/** Setter for {@link #getName()}. */
	public void setName(String name) {
    	this.name = name;
    }
	
	/** The value. */
	public String getValue() {
		return (value);
	}
	
	/** Setter for {@link #getValue()}. */
	public void setValue(String aVal) {
		this.value = aVal;
	}

	/** Setter for {@link #getData()}. */
	public void setData(Object anObj) {
		this.data = anObj;
	}
	
	/** The data. */
	public Object getData() {
		return this.data;
	}
	
	/** The width. */
	public double getWidth() {
	    return this.width;
	}
	
	/*package protected */ void setWidth(double aWidth) {
	    this.width = aWidth;
	}
	
	/** The height. */
	public double getHeight() {
        return (this.height);
    }
	
	/*package protected */ void setHeight(double aHeight) {
        this.height = aHeight;
    }
	
	/** Setter for {@link #getFont()}. */
	public void setFont(PptFont aFont) {
        this.font = aFont;
    }
	
	/** The font. */
	public PptFont getFont() {
        return (this.font);
    }
	
	/** The style of this cell. A default style is created on first access. */
	public PptCellStyle getCellStyle() {
	    if (this.style == null) {
	        this.style =  new PptCellStyle(); // return a default style
	    }
	    return this.style;
	    
	}
	
	/** Setter for {@link #getCellStyle()}. */
	public void setStyle(PptCellStyle aStyle) {
        this.style = aStyle;
    }
	
	/** Style settings of a {@link PptCell}. */
	public static class PptCellStyle {
        
		private LineDash _lineDash = LineDash.SOLID;

		private double _lineWidth = 1.0d;

		private Color _lineColor = Color.BLACK;
	    private Color  fillColor     = Color.WHITE;
	    private double marginLeft    = 1.0d;
	    private double marginRight   = 1.0d;
	    private double marginTop     = 1.0d;
	    private double marginBottom  = 1.0d;

		private VerticalAlignment verticalAlign = VerticalAlignment.TOP;

		private boolean _centered = false;

		private double rotation = 0;

		private Hyperlink link = null;

		private LineCompound _lineCompound = LineCompound.SINGLE;

		private LineCap _lineCap = LineCap.FLAT;

		private DecorationSize _tailWidth = DecorationSize.MEDIUM;

		private DecorationShape _tailShape = DecorationShape.NONE;

		private DecorationSize _tailLength = DecorationSize.MEDIUM;

		private DecorationSize _headWidth = DecorationSize.MEDIUM;

		private DecorationShape _headShape = DecorationShape.NONE;

		private DecorationSize _headLength = DecorationSize.MEDIUM;
	    
	    public PptCellStyle() {
        }
	    
		/** The rotation. */
		public double getRotation() {
			return this.rotation;
		}

		/** Setter for {@link #getRotation()}. */
		public void setRotation(double value) {
			this.rotation = value;
		}

		/** The hyperlink. */
		public Hyperlink getHyperlink() {
			return this.link;
		}

	    /** The fill color. */
	    public Color getFillColor() {
	        return this.fillColor;
	    }
	    
		/** The line color. */
		public Color getLineColor() {
			return (this._lineColor);
        }
        
		/** The line dash. */
		public LineDash getLineDash() {
			return (this._lineDash);
        }
        
		/** The line width. */
		public double getLineWidth() {
			return (this._lineWidth);
        }

		/** Setter for {@link #getHyperlink()}. */
		public void setHyperlink(Hyperlink aLink) {
			this.link = aLink;
		}

		/** Setter for {@link #getLineDash()}. */
		public void setLineDash(LineDash lineDash) {
			this._lineDash = lineDash;
        }

		/** Setter for {@link #getLineWidth()}. */
		public void setLineWidth(double aBorderWidth) {
			this._lineWidth = aBorderWidth;
        }

		/** Setter for {@link #getLineColor()}. */
		public void setLineColor(Color aBorderColor) {
			this._lineColor = aBorderColor;
        }

        /** Setter for {@link #getFillColor()}. */
        public void setFillColor(Color aFillColor) {
            this.fillColor = aFillColor;
        }

        /** The left margin. */
        public double getMarginLeft() {
            return (this.marginLeft);
        }

        /** Setter for {@link #getMarginLeft()}. */
        public void setMarginLeft(double aMarginLeft) {
            this.marginLeft = aMarginLeft;
        }

        /** The right margin. */
        public double getMarginRight() {
            return (this.marginRight);
        }

        /** Setter for {@link #getMarginRight()}. */
        public void setMarginRight(double aMarginRight) {
            this.marginRight = aMarginRight;
        }

        /** The top margin. */
        public double getMarginTop() {
            return (this.marginTop);
        }

        /** Setter for {@link #getMarginTop()}. */
        public void setMarginTop(double aMarginTop) {
            this.marginTop = aMarginTop;
        }

        /** The bottom margin. */
        public double getMarginBottom() {
            return (this.marginBottom);
        }

        /** Setter for {@link #getMarginBottom()}. */
        public void setMarginBottom(double aMarginBottom) {
            this.marginBottom = aMarginBottom;
        }

        /** Sets all margins to the given value. */
        public void setMargin(double aMargin) {
            this.setMarginBottom(aMargin);
            this.setMarginTop   (aMargin);
            this.setMarginLeft  (aMargin);
            this.setMarginRight (aMargin);
        }
        
		/** Setter for {@link #getHorizontalAlignment()}. */
		public void setHorizontalAlignment(boolean centered) {
			this._centered = centered;
        }
        
		/** Whether the content is centered horizontally. */
		public boolean getHorizontalAlignment() {
			return _centered;
        }
        
		/** Setter for {@link #getVerticalAlignment()}. */
		public void setVerticalAlignment(VerticalAlignment aAlign) {
            this.verticalAlign = aAlign;
        }
        
		/** The vertical alignment. */
		public VerticalAlignment getVerticalAlignment() {
            return (this.verticalAlign);
        }
        
		/** Creates a {@link PptCellStyle} from the style of the given shape. */
		public static PptCellStyle createCellStyle(SimpleShape<?, ?> aShape) {
            PptCellStyle theStyle = new PptCellStyle();
            
            theStyle.setFillColor(aShape.getFillColor());
			theStyle.setRotation(aShape.getRotation());

			LineDecoration lineDecoration = aShape.getLineDecoration();
			theStyle.setLineHeadLength(lineDecoration.getHeadLength());
			theStyle.setLineHeadShape(lineDecoration.getHeadShape());
			theStyle.setLineHeadWidth(lineDecoration.getHeadWidth());
			theStyle.setLineTailLength(lineDecoration.getTailLength());
			theStyle.setLineTailShape(lineDecoration.getTailShape());
			theStyle.setLineTailWidth(lineDecoration.getTailWidth());

			StrokeStyle strokeStyle = aShape.getStrokeStyle();
			theStyle.setLineCap(strokeStyle.getLineCap());
			theStyle.setLineCompound(strokeStyle.getLineCompound());
			theStyle.setLineDash(strokeStyle.getLineDash());
			theStyle.setLineWidth(strokeStyle.getLineWidth());

			if (aShape instanceof HSLFSimpleShape) {
				HSLFSimpleShape impl = (HSLFSimpleShape) aShape;
				theStyle.setLineColor(impl.getLineColor());
			} else if (aShape instanceof XSLFSimpleShape) {
				XSLFSimpleShape impl = (XSLFSimpleShape) aShape;
				theStyle.setLineColor(impl.getLineColor());
			}
            
			if (aShape instanceof HSLFTextShape) {
				HSLFTextShape hslf = (HSLFTextShape) aShape;
				theStyle.setMarginLeft(hslf.getLeftInset());
				theStyle.setMarginRight(hslf.getRightInset());
				theStyle.setMarginBottom(hslf.getBottomInset());
				theStyle.setMarginTop(hslf.getTopInset());
                // cannot extract this values due to buggy implementation in TextShape
                // there will be NPEs if theres no text in the TextShape
                //theStyle.setVAlign((   (TextShape) aShape).getVerticalAlignment());
				theStyle.setHorizontalAlignment(hslf.isHorizontalCentered());
            }
            
            return theStyle;
        }

		/** The line compound. */
		public LineCompound getLineCompound() {
			return _lineCompound;
		}
        
		/** Setter for {@link #getLineCompound()}. */
		public void setLineCompound(LineCompound lineCompound) {
			_lineCompound = lineCompound;
		}

		/** The line cap. */
		public LineCap getLineCap() {
			return _lineCap;
		}

		/** Setter for {@link #getLineCap()}. */
		public void setLineCap(LineCap lineCap) {
			_lineCap = lineCap;
		}

		/** The line tail width. */
		public DecorationSize getLineTailWidth() {
			return _tailWidth;
		}

		/** Setter for {@link #getLineTailWidth()}. */
		public void setLineTailWidth(DecorationSize tailWidth) {
			_tailWidth = tailWidth;
		}

		/** The line tail shape. */
		public DecorationShape getLineTailShape() {
			return _tailShape;
		}

		/** Setter for {@link #getLineTailShape()}. */
		public void setLineTailShape(DecorationShape tailShape) {
			_tailShape = tailShape;
		}

		/** The line tail length. */
		public DecorationSize getLineTailLength() {
			return _tailLength;
		}

		/** Setter for {@link #getLineTailLength()}. */
		public void setLineTailLength(DecorationSize tailLength) {
			_tailLength = tailLength;
		}

		/** The line head width. */
		public DecorationSize getLineHeadWidth() {
			return _headWidth;
		}

		/** Setter for {@link #getLineHeadWidth()}. */
		public void setLineHeadWidth(DecorationSize headWidth) {
			_headWidth = headWidth;
		}

		/** The line head shape. */
		public DecorationShape getLineHeadShape() {
			return _headShape;
		}

		/** Setter for {@link #getLineHeadShape()}. */
		public void setLineHeadShape(DecorationShape headShape) {
			_headShape = headShape;
		}

		/** Sets the length of the line head decoration. */
		public void setLineHeadLength(DecorationSize headLength) {
			_headLength = headLength;
		}

		/** Applies this style to the given shape. */
		public void applyStyle(SimpleShape aShape) {
            aShape.setFillColor(this.getFillColor());
			aShape.setRotation(this.getRotation());

			if (_lineColor != null) {
				aShape.setStrokeStyle(_lineWidth, _lineCap, _lineDash, _lineCompound, _lineColor);
			} else {
				aShape.setStrokeStyle();
			}

			if (aShape instanceof HSLFSimpleShape) {
				HSLFSimpleShape impl = (HSLFSimpleShape) aShape;
				impl.setLineHeadDecoration(_headShape);
				impl.setLineHeadLength(_headLength);
				impl.setLineHeadWidth(_headWidth);
				impl.setLineTailDecoration(_tailShape);
				impl.setLineTailLength(_tailLength);
				impl.setLineTailWidth(_tailWidth);
			}
			else if (aShape instanceof XSLFSimpleShape) {
				XSLFSimpleShape impl = (XSLFSimpleShape) aShape;
				impl.setLineHeadDecoration(_headShape);
				impl.setLineHeadLength(_headLength);
				impl.setLineHeadWidth(_headWidth);
				impl.setLineTailDecoration(_tailShape);
				impl.setLineTailLength(_tailLength);
				impl.setLineTailWidth(_tailWidth);
			}
            
			if (this.link != null) {
				Hyperlink<?, ?> theLink = aShape.createHyperlink();
				theLink.setAddress(this.link.getAddress());
				theLink.setLabel(this.link.getLabel());
			}

			// TODO should be sub classes for Pictures an TextShapes...
			if (aShape instanceof TextShape<?, ?>) {
				TextShape<?, ?> impl = (TextShape<?, ?>) aShape;
				impl.setHorizontalCentered(this.getHorizontalAlignment());
				impl.setVerticalAlignment(this.getVerticalAlignment());
			}
			if (aShape instanceof HSLFTextShape) {
				HSLFTextShape impl = (HSLFTextShape) aShape;
				impl.setBottomInset((float) this.getMarginBottom());
				impl.setLeftInset((float) this.getMarginLeft());
				impl.setRightInset((float) this.getMarginRight());
				impl.setTopInset((float) this.getMarginTop());
            }
			else if (aShape instanceof XSLFTextShape) {
				XSLFTextShape impl = (XSLFTextShape) aShape;
				impl.setBottomInset((float) this.getMarginBottom());
				impl.setLeftInset((float) this.getMarginLeft());
				impl.setRightInset((float) this.getMarginRight());
				impl.setTopInset((float) this.getMarginTop());
			}
        }
	}
	
	/** Font settings of a {@link PptCell}. */
	public static class PptFont {

	    
		private static final PptFont NO_FONT = new PptFont("None", null) {
			@Override
			public void applyFont(TextRun aRTR) {
				// Ignore.
			}
		};

		private String name;

		private Double size;
	    //private int     alignment = ALIGNMENT_LEFT;
		private PaintStyle color = null;
	    private boolean underline = false;
	    private boolean italic    = false;
	    private boolean bold      = false;
	    
	    
		public PptFont(String aName, Double aSize) {
	        this.name = aName;
	        this.size = aSize;
	    }
	    
        /** The name. */
        public String getName() {
            return (this.name);
        }
        
		/** The size. */
		public Double getSize() {
            return (this.size);
        }
        
        /** Whether the font is bold. */
        public boolean isBold() {
            return (this.bold);
        }
        
        /** Whether the font is underlined. */
        public boolean isUnderline() {
            return (this.underline);
        }
        
        /** Whether the font is italic. */
        public boolean isItalic() {
            return (this.italic);
        }
        
        /** Setter for {@link #isBold()}. */
        public void setBold(boolean aBold) {
            this.bold = aBold;
        }
        
        /** Setter for {@link #isItalic()}. */
        public void setItalic(boolean aItalic) {
            this.italic = aItalic;
        }
        
        /** Setter for {@link #getName()}. */
        public void setName(String aName) {
            this.name = aName;
        }
        
		/** Setter for {@link #getSize()}. */
		public void setSize(Double aSize) {
            this.size = aSize;
        }
        
        /** Setter for {@link #isUnderline()}. */
        public void setUnderline(boolean aUnderline) {
            this.underline = aUnderline;
        }
        
		/** The color. */
		public PaintStyle getColor() {
            return (this.color);
        }
        
		/** Setter for {@link #getColor()}. */
		public void setColor(PaintStyle paintStyle) {
			this.color = paintStyle;
        }
        
		/** Creates a {@link PptFont} from the font of the first text run of the given shape. */
		public static PptFont createFont(TextShape<?, ?> shape) {
			List<? extends TextParagraph<?, ?, ?>> paragraphs = shape.getTextParagraphs();
			if (paragraphs.isEmpty()) {
				return NO_FONT;
			}
			List<? extends TextRun> textRuns = paragraphs.get(0).getTextRuns();
			if (textRuns.isEmpty()) {
				return NO_FONT;
			}
			return createFont(textRuns.get(0));
		}

		/** Creates a {@link PptFont} from the font of the given text run. */
		public static PptFont createFont(TextRun aRTR) {
			PptFont theFont = new PptFont(aRTR.getFontFamily(), aRTR.getFontSize());
            theFont.setColor(aRTR.getFontColor());
            theFont.setBold(aRTR.isBold());
            theFont.setItalic(aRTR.isItalic());
            theFont.setUnderline(aRTR.isUnderlined());
            return theFont;
        }
        
		/** Applies this font to the given text run. */
		public void applyFont(TextRun aRTR) {
            aRTR.setFontColor  (this.getColor());
			aRTR.setFontFamily(this.getName());
            Double fontSize = this.getSize();
			if (fontSize != null) {
				aRTR.setFontSize(fontSize);
			}
            aRTR.setBold       (this.isBold());
            aRTR.setItalic     (this.isItalic());
            aRTR.setUnderlined (this.isUnderline());
        }
	}
	
}
