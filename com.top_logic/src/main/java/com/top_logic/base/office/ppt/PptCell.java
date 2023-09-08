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
 * The PptCell is most likely a holder for format options. 
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

	public String getName() {
    	return (name);
    }

	public void setName(String name) {
    	this.name = name;
    }
	
	public String getValue() {
		return (value);
	}
	
	public void setValue(String aVal) {
		this.value = aVal;
	}

	public void setData(Object anObj) {
		this.data = anObj;
	}
	
	public Object getData() {
		return this.data;
	}
	
	public double getWidth() {
	    return this.width;
	}
	
	/*package protected */ void setWidth(double aWidth) {
	    this.width = aWidth;
	}
	
	public double getHeight() {
        return (this.height);
    }
	
	/*package protected */ void setHeight(double aHeight) {
        this.height = aHeight;
    }
	
	public void setFont(PptFont aFont) {
        this.font = aFont;
    }
	
	public PptFont getFont() {
        return (this.font);
    }
	
	public PptCellStyle getCellStyle() {
	    if (this.style == null) {
	        this.style =  new PptCellStyle(); // return a default style
	    }
	    return this.style;
	    
	}
	
	public void setStyle(PptCellStyle aStyle) {
        this.style = aStyle;
    }
	
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
	    
		public double getRotation() {
			return this.rotation;
		}

		public void setRotation(double value) {
			this.rotation = value;
		}

		public Hyperlink getHyperlink() {
			return this.link;
		}

	    public Color getFillColor() {
	        return this.fillColor;
	    }
	    
		public Color getLineColor() {
			return (this._lineColor);
        }
        
		public LineDash getLineDash() {
			return (this._lineDash);
        }
        
		public double getLineWidth() {
			return (this._lineWidth);
        }

		public void setHyperlink(Hyperlink aLink) {
			this.link = aLink;
		}

		public void setLineDash(LineDash lineDash) {
			this._lineDash = lineDash;
        }

		public void setLineWidth(double aBorderWidth) {
			this._lineWidth = aBorderWidth;
        }

		public void setLineColor(Color aBorderColor) {
			this._lineColor = aBorderColor;
        }

        public void setFillColor(Color aFillColor) {
            this.fillColor = aFillColor;
        }

        public double getMarginLeft() {
            return (this.marginLeft);
        }

        public void setMarginLeft(double aMarginLeft) {
            this.marginLeft = aMarginLeft;
        }

        public double getMarginRight() {
            return (this.marginRight);
        }

        public void setMarginRight(double aMarginRight) {
            this.marginRight = aMarginRight;
        }

        public double getMarginTop() {
            return (this.marginTop);
        }

        public void setMarginTop(double aMarginTop) {
            this.marginTop = aMarginTop;
        }

        public double getMarginBottom() {
            return (this.marginBottom);
        }

        public void setMarginBottom(double aMarginBottom) {
            this.marginBottom = aMarginBottom;
        }

        public void setMargin(double aMargin) {
            this.setMarginBottom(aMargin);
            this.setMarginTop   (aMargin);
            this.setMarginLeft  (aMargin);
            this.setMarginRight (aMargin);
        }
        
		public void setHorizontalAlignment(boolean centered) {
			this._centered = centered;
        }
        
		public boolean getHorizontalAlignment() {
			return _centered;
        }
        
		public void setVerticalAlignment(VerticalAlignment aAlign) {
            this.verticalAlign = aAlign;
        }
        
		public VerticalAlignment getVerticalAlignment() {
            return (this.verticalAlign);
        }
        
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

		public LineCompound getLineCompound() {
			return _lineCompound;
		}
        
		public void setLineCompound(LineCompound lineCompound) {
			_lineCompound = lineCompound;
		}

		public LineCap getLineCap() {
			return _lineCap;
		}

		public void setLineCap(LineCap lineCap) {
			_lineCap = lineCap;
		}

		public DecorationSize getLineTailWidth() {
			return _tailWidth;
		}

		public void setLineTailWidth(DecorationSize tailWidth) {
			_tailWidth = tailWidth;
		}

		public DecorationShape getLineTailShape() {
			return _tailShape;
		}

		public void setLineTailShape(DecorationShape tailShape) {
			_tailShape = tailShape;
		}

		public DecorationSize getLineTailLength() {
			return _tailLength;
		}

		public void setLineTailLength(DecorationSize tailLength) {
			_tailLength = tailLength;
		}

		public DecorationSize getLineHeadWidth() {
			return _headWidth;
		}

		public void setLineHeadWidth(DecorationSize headWidth) {
			_headWidth = headWidth;
		}

		public DecorationShape getLineHeadShape() {
			return _headShape;
		}

		public void setLineHeadShape(DecorationShape headShape) {
			_headShape = headShape;
		}

		public void setLineHeadLength(DecorationSize headLength) {
			_headLength = headLength;
		}

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
	    
        public String getName() {
            return (this.name);
        }
        
		public Double getSize() {
            return (this.size);
        }
        
        public boolean isBold() {
            return (this.bold);
        }
        
        public boolean isUnderline() {
            return (this.underline);
        }
        
        public boolean isItalic() {
            return (this.italic);
        }
        
        public void setBold(boolean aBold) {
            this.bold = aBold;
        }
        
        public void setItalic(boolean aItalic) {
            this.italic = aItalic;
        }
        
        public void setName(String aName) {
            this.name = aName;
        }
        
		public void setSize(Double aSize) {
            this.size = aSize;
        }
        
        public void setUnderline(boolean aUnderline) {
            this.underline = aUnderline;
        }
        
		public PaintStyle getColor() {
            return (this.color);
        }
        
		public void setColor(PaintStyle paintStyle) {
			this.color = paintStyle;
        }
        
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

		public static PptFont createFont(TextRun aRTR) {
			PptFont theFont = new PptFont(aRTR.getFontFamily(), aRTR.getFontSize());
            theFont.setColor(aRTR.getFontColor());
            theFont.setBold(aRTR.isBold());
            theFont.setItalic(aRTR.isItalic());
            theFont.setUnderline(aRTR.isUnderlined());
            return theFont;
        }
        
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
