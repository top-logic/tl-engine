/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.poi.hslf.usermodel.HSLFTextRun;
import org.apache.poi.sl.usermodel.AutoShape;
import org.apache.poi.sl.usermodel.FillStyle;
import org.apache.poi.sl.usermodel.GroupShape;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.PictureShape;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.sl.usermodel.SimpleShape;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.TableCell;
import org.apache.poi.sl.usermodel.TableShape;
import org.apache.poi.sl.usermodel.TextBox;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextParagraph.BulletStyle;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.sl.usermodel.TextShape;
import org.apache.poi.ss.usermodel.Picture;

import com.top_logic.base.office.AbstractOffice.ImageReplacerData;
import com.top_logic.base.office.ppt.PptCell.PptFont;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.util.Utils;

/**
 * Util class to replace tokens in the PPT template.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class POIPowerpointUtil {
	
	/**
	 * Token prefixes
	 */
	public static final String PREFIX_VALUE          = "VALUE_";
	public static final String PREFIX_CELLVALUE      = "CELLVALUE_";
	public static final String PREFIX_PICTURE        = "PICTURE_";

	@Deprecated
	public static final String PREFIX_VTABLE         = "VTABLE_";

	public static final String PREFIX_AUTOTABLE      = "AUTOTABLE_";
	public static final String PREFIX_FIXEDTABLE     = "FIXEDTABLE_";

	@Deprecated
	public static final String PREFIX_TEMPLATESLIDE  = "TEMPSLIDE_";

	public static final String POSTFIX_TITLE         = "_TITLE";

	@Deprecated
	public static final String TEMPTABLE             = "tempTable";
	public static final String SLIDE_COUNT           = "SL_COUNT";


	public static final String[] PREFIX_VALUES = new String[] {
		PREFIX_VALUE, PREFIX_CELLVALUE, PREFIX_PICTURE, PREFIX_VTABLE,
		PREFIX_AUTOTABLE, PREFIX_FIXEDTABLE, PREFIX_TEMPLATESLIDE
	};

	public static final int FIRST_CELL = 1;
	
//	private static final String FONT_SIZE   = "fontSize";
//	private static final String FONT_NAME   = "fontName";
//	private static final String FONT_COLOR  = "fontColor";
//	private static final String FONT_INDEX  = "fontIndex";
//	private static final String FONT_ALIGN  = "fontIndex";
//	private static final String FONT_BOLD   = "bold";
//	private static final String FONT_ITALIC = "italic";
//	private static final String FONT_UL     = "underlined";
	
	private boolean doCreate;
	
	public POIPowerpointUtil() {
		this.doCreate = false;
	}
	
	/**
	 * Takes an array of {@link Slide}s and searches for a slide with the given
	 * title.
	 * 
	 * @param slides
	 *            an array of slides
	 * @param aTitle
	 *            a title
	 * @return a {@link Slide} with the given title or <code>null</code> if no
	 *         {@link Slide} with the given title was found.
	 */
	public static Slide getSlideByTitle(Slide[] slides, String aTitle) {
		if (aTitle == null) {
			return null;
		}
		for (int i = 0; i < slides.length; i++) {
			String title = extractTitle(slides[i]);
			if (title.equals(aTitle)) {
				return slides[i];
			}
		}
		return null;
	}
	
	/**
	 * Parses a given slide with its given shapes for tokens and replaces them
	 * with the respective values given in a token map.
	 * 
	 * @param aSlide
	 *            a {@link Slide} which should be parsed.
	 * @param someShapes
	 *            the shapes of the given slide
	 * @param someTokens
	 *            a {@link Map} of tokens to be used for replacement.
	 * @throws IOException
	 *             if the replacement of picture tokens goes wrong.
	 */
	public void parseForTokens(Sheet aSlide, List<? extends Shape<?, ?>> someShapes, Map<String, Object> someTokens)
			throws IOException {
		String theTitle = extractTitle(aSlide);
		boolean replaceTitle = false;
		Map<Shape, Object> pictures = new HashMap<>();
		if(theTitle != null && theTitle.startsWith(PREFIX_VTABLE)) {
			createTable(aSlide, someShapes, someTokens);
			replaceTitle = true;
		}
		else if (theTitle != null && theTitle.startsWith(PREFIX_TEMPLATESLIDE)) {
			createSlides(aSlide, someShapes, someTokens);
			replaceTitle = true;
		}
		else {
			for (Shape theShape : someShapes) {
				// shapes's anchor which defines the position of this shape in the
				// slide
				Rectangle2D anchor = theShape.getAnchor();

				if (theShape instanceof TextShape) {
					TextShape shape = (TextShape) theShape;
					String theShapeText = shape.getText();
					List<String> theTokens = findTokens(theShapeText);
					if(!CollectionUtil.isEmptyOrNull(theTokens)) {
						for(int i = 0; i < theTokens.size(); i++) {
							String theToken = theTokens.get(i);
							Object theTokenValue = someTokens.get(theToken);
							replaceToken(aSlide, shape, anchor, theToken, theTokenValue);
						}
					}
				}
				else if (theShape instanceof TableShape<?, ?>) {
					parseTable(aSlide, someTokens, pictures, theShape);
				}
				else if (theShape instanceof GroupShape<?, ?>) {
					GroupShape<?, ?> theGrp = (GroupShape<?, ?>) theShape;
					List<? extends Shape<?, ?>> sh = POIPowerpointUtil.shapesStable(theGrp);
					parseForTokens(aSlide, sh, someTokens);
				}
				// other shapes are not eligible for token replacement
			}
		}
		if(replaceTitle) {
			String theReplacer = (String) someTokens.get(theTitle + POSTFIX_TITLE);
			replaceToken(someShapes, theTitle, theReplacer);
		}
		if (!CollectionUtil.isEmptyOrNull(pictures)) {
			for (Iterator<Shape> theIt = pictures.keySet().iterator(); theIt.hasNext();) {
				Shape next = theIt.next();
				replacePicture(aSlide, next, pictures.get(next));
			}
		}
	}

	/**
	 * Parses a {@link TableShape}. If the table contains ONLY one token that starts with
	 * {@link #PREFIX_FIXEDTABLE} the cells starting with the row the token was will be replaced
	 * with the list of respective objects. Otherwise the table will be treated as a normal
	 * {@link GroupShape}.
	 * 
	 * @param aSlide
	 *        the slide the table is on
	 * @param someTokens
	 *        a {@link Map} of String tokens and objects the tokens shall be replaced with
	 * @param somePictures
	 *        a {@link Map} for shapes that shall be replaced with picture files
	 * @param aShape
	 *        the shape that shall be parsed as a table
	 * @throws IOException
	 *         if a picture token was encountered and its replacement failed
	 */
	private void parseTable(Sheet aSlide, Map<String, Object> someTokens, Map<Shape, Object> somePictures, Shape aShape)
			throws IOException {
		TableShape<?, ?> theTbl = (TableShape<?, ?>) aShape;
		int   cols   = theTbl.getNumberOfColumns();
		int   rows   = theTbl.getNumberOfRows();
		
		for (int j = 0; j < rows; j++) {
			for (int i = 0; i < cols; i++) {
				TableCell cell = theTbl.getCell(j, i);
				if (cell == null) {
					continue;
				}
				String       theCellText = cell.getText();
				List<String> theTokens   = findTokens(theCellText);
				
				if (!CollectionUtil.isEmptyOrNull(theTokens)) {
					for (int k = 0; k < theTokens.size();) {
						String theToken = theTokens.get(k);
						
						if (! theToken.startsWith(PREFIX_FIXEDTABLE)) {
							GroupShape<?, ?> theGrp = (GroupShape<?, ?>) aShape;
							List<? extends Shape<?, ?>> sh = POIPowerpointUtil.shapesStable(theGrp);
							parseForTokens(aSlide, sh, someTokens);
							return;
						}
						Object           theTokenValue = someTokens.get(theToken);
						List<Object>     theTableMap   = (theTokenValue instanceof List) ? (List<Object>) theTokenValue : (theTokenValue == null) ? null : Arrays.asList((Object[]) theTokenValue);
						if (CollectionUtil.isEmptyOrNull(theTableMap)) {
							return;
						}
						Iterator<Object> theIt = theTableMap.iterator();
						
						Object[] rowArray;
						for (int row = j; row < rows; row++) {
							rowArray = null;
							Object object = null;
							for (int col = 0; col < cols; col++) {
								TableCell cell2  = theTbl.getCell(row, col);
								if (rowArray == null && theIt.hasNext()) {
									object = theIt.next();
								}
								
								if (object instanceof String) {
									// this seems to only work if the table cell in the template
									// contains at least on character. This can as well be a space
									// but still...
									//TODO tbe, fsc, mga: any ideas
									cell2.setText((String) object);
								} 
								else if (object instanceof File || object instanceof ImageReplacerData
									|| object instanceof BinaryContent) {
									somePictures.put(cell2, object);
								}
								else if (object instanceof Color) {
									cell2.setFillColor((Color) object);
								} else if (object instanceof Object[]) {
									rowArray = (Object[]) object;
									Object rowArrayValue = rowArray[col];
									if (rowArrayValue != null) {
										if (rowArrayValue instanceof File || rowArrayValue instanceof ImageReplacerData
											|| rowArrayValue instanceof BinaryContent) {
											somePictures.put(cell2, rowArrayValue);
										} else {
											cell2.setText(rowArrayValue.toString());
										}
									}
								}
								if (!theIt.hasNext() && rowArray == null) {
									return;
								}
							}
						}
						return;
					}
				}
			}
		}
	}
	
	public static void setText(SlideShow aShow, TextShape<?,?> aNewShape, TextShape<?,?> anOldShape) {
		List<? extends TextParagraph<?, ?, ?>> oldParagraphs = anOldShape.getTextParagraphs();

		for (TextParagraph<?, ?, ?> oldParagraph : oldParagraphs) {
			boolean first = true;
			for (TextRun oldRun : oldParagraph.getTextRuns()) {
				TextRun newRun = aNewShape.appendText(oldRun.getRawText(), first);
				applyRichTextRun(newRun, oldRun);

				if (first) {
					TextParagraph<?, ?, ?> newParagraph = aNewShape.getTextParagraphs().get(0);
					applyStles(newParagraph, oldParagraph);
				}

				first = false;
        	}
        }
    } 

	private static void applyStles(TextParagraph<?, ?, ?> newParagraph, TextParagraph<?, ?, ?> oldParagraph) {
		BulletStyle oldStyle = oldParagraph.getBulletStyle();
		String bulletCharacter = oldStyle.getBulletCharacter();
		if (bulletCharacter.length() > 0) {
			BulletStyle newStyle = newParagraph.getBulletStyle();
			newStyle.setBulletFontColor(oldStyle.getBulletFontColor());
			newParagraph.setBulletStyle(
				oldStyle.getBulletFontSize(),
				bulletCharacter.charAt(0),
				oldStyle.getBulletFont());
		} else {
			newParagraph.setBulletStyle();
		}
		newParagraph.setIndent(oldParagraph.getIndent());
		newParagraph.setIndentLevel(oldParagraph.getIndentLevel());
		newParagraph.setLeftMargin(oldParagraph.getLeftMargin());
		newParagraph.setLineSpacing(oldParagraph.getLineSpacing());
		newParagraph.setRightMargin(oldParagraph.getRightMargin());
		newParagraph.setSpaceAfter(oldParagraph.getSpaceAfter());
		newParagraph.setSpaceBefore(oldParagraph.getSpaceBefore());
		newParagraph.setTextAlign(oldParagraph.getTextAlign());
	}

	public static void applyRichTextRun(TextRun aNew, TextRun anOld) {
        aNew.setBold(anOld.isBold());
        aNew.setItalic(anOld.isItalic());
        aNew.setStrikethrough(anOld.isStrikethrough());
        aNew.setUnderlined(anOld.isUnderlined());

        aNew.setFontColor(anOld.getFontColor());
		aNew.setFontSize(anOld.getFontSize());
		if (aNew instanceof HSLFTextRun && anOld instanceof HSLFTextRun) {
			applyRichTextRun((HSLFTextRun) aNew, (HSLFTextRun) anOld);
		}
	}

	private static void applyRichTextRun(HSLFTextRun aNew, HSLFTextRun anOld) {
		aNew.setEmbossed(anOld.isEmbossed());
		aNew.setShadowed(anOld.isShadowed());
        aNew.setFontIndex(anOld.getFontIndex());
		aNew.setSuperscript(anOld.getSuperscript());
    }
	
	protected static String extractTitle(Sheet aSheet) {
	    if (aSheet instanceof Slide) {
	        return StringServices.trim(((Slide) aSheet).getTitle());
	    }
	    return null;
	}
	
	/**
	 * Finds and replaces a given token in a given array of shapes with the given value.
	 * 
	 * @param someShapes the shapes to search through
	 * @param aToken a token to be replaced.
	 * @param aNewValue the new value for the token.
	 */
	private void replaceToken(List<? extends Shape<?, ?>> someShapes, String aToken, String aNewValue) {
		for (int i = 0; i < someShapes.size(); i++) {
			Shape theShape = someShapes.get(i);
			if (theShape instanceof TextShape) {
				TextShape theBox = (TextShape) theShape;
				String theText = theBox.getText();
				if(theText.equals(aToken)) {
					theBox.setText(StringServices.nonNull(aNewValue));
					return;
				}
			}
		}
    }

	/**
	 * A {@link Slide} with a title starting with PREFIX_TEMPLATESLIDE defines a
	 * template for new slides. Each newly created slide looks like the
	 * original. This method parses all shapes of the temp slide, sorts them
	 * into different groups and replaces the respective tokens.
	 * 
	 * @param aSlide
	 *            a template slide for slide production.
	 * @param someShapes
	 *            the shapes of the template slide
	 * @param someTokens
	 *            a Map of tokens which define the replacement.
	 */
	private void createSlides(Sheet aSlide, List<? extends Shape<?, ?>> someShapes, Map someTokens) {
		String theTitle  = this.extractTitle(aSlide);
		Object theVals = someTokens.get(theTitle);
		TextShape firstCopyCell = null;
		if(theVals != null) {
			List theValueShapes = new ArrayList();
			Map theParentShapes = new HashMap();
			Map theChildrenShapes = new HashMap();
			List thePictureShape = new ArrayList();
			List theHeaderShapes = new ArrayList();
			for (int i = 0; i < someShapes.size(); i++) {
				Shape theShape = someShapes.get(i);
				if (theShape instanceof GroupShape<?, ?>) {
					GroupShape<?, ?> theGrp = (GroupShape<?, ?>) theShape;
					List<? extends Shape<?, ?>> sh = POIPowerpointUtil.shapesStable(theGrp);
					firstCopyCell = parseShapeGroup(sh, theValueShapes, theParentShapes, thePictureShape, theHeaderShapes, theChildrenShapes);
				}
				else if (theShape instanceof TextShape) {
					TextShape shape = (TextShape) theShape;
					String theToken = shape.getText();
					if(theToken != null && theToken.startsWith(PREFIX_CELLVALUE)) {
						firstCopyCell = parseValueCells(theParentShapes, theChildrenShapes, firstCopyCell, shape, theToken);
					}
					else if(theToken != null && theToken.startsWith(PREFIX_VALUE)) {
						theValueShapes.add(theShape);
					}
					else if(theToken != null && theToken.startsWith(PREFIX_PICTURE)){
						thePictureShape.add(theShape);
					}
					else if(theToken != null && !theToken.startsWith(PREFIX_TEMPLATESLIDE)) {
						theHeaderShapes.add(theShape);
					}
				}
			}
			PptTableContainer theMain = (PptTableContainer) someTokens.get(theTitle);
			if(theMain != null && theMain.hasChildren()) {
				List theKPIs = theMain.getChildren();
				this.doCreate = false;
				parseChildren(theKPIs, theValueShapes, thePictureShape, theHeaderShapes, theChildrenShapes, theParentShapes, aSlide, firstCopyCell);
			}

			removeShapes(aSlide, theValueShapes);
			removeShapes(aSlide, thePictureShape);
			removeShapes(aSlide, theChildrenShapes);
			removeShapes(aSlide, theParentShapes);
			removeShapes(aSlide, theHeaderShapes);
		}
    }

	/**
	 * Takes a {@link GroupShape} and sorts its {@link Shape}s into the respective {@link Map}s or
	 * {@link List}s. Used for "PREFIX_TEMPLATESLIDE" slides.
	 * 
	 * @param someValueShapes
	 *        a List of shapes that do not need to be copied but merely replaced.
	 * @param someParentShapes
	 *        a Map of shapes that define the first level of a copie-replacement hierarchy.
	 * @param somePictureShape
	 *        a List of shapes that shall be replaced by pictures (token starts with
	 *        PREFIX_PICTURE).
	 * @param someHeaderShapes
	 *        a List of shapes that should be copied to each ne slide withoud being modified.
	 * @param someChildrenShapes
	 *        a Map of shapes that define the second level of a copie-replacement hierarchy.
	 * @return a {@link TextShape} that defines the first cell of this group in case it should be
	 *         copied.
	 */
	private TextShape parseShapeGroup(List<? extends Shape<?, ?>> aShapeArray, List someValueShapes,
			Map someParentShapes, List somePictureShape, List someHeaderShapes, Map someChildrenShapes) {
		TextShape theCopyReferenceShape = null;
		for (Shape theShape : aShapeArray) {
			if (theShape instanceof GroupShape<?, ?>) {
				GroupShape<?, ?> theGrp = (GroupShape<?, ?>) theShape;
				List<? extends Shape<?, ?>> sh = POIPowerpointUtil.shapesStable(theGrp);
				theCopyReferenceShape = parseShapeGroup(sh, someValueShapes, someParentShapes, somePictureShape, someHeaderShapes, someChildrenShapes);
			}
			else if (theShape instanceof TextShape) {
				TextShape shape = (TextShape) theShape;
				String theToken = shape.getText();
				if(theToken != null && theToken.startsWith(PREFIX_CELLVALUE)) {
					theCopyReferenceShape = parseValueCells(someParentShapes, someChildrenShapes, theCopyReferenceShape, shape, theToken);
				}
				else if(theToken != null && theToken.startsWith(PREFIX_VALUE)) {
					someValueShapes.add(theShape);
				}
				else if(theToken != null && theToken.startsWith(PREFIX_PICTURE)){
					somePictureShape.add(theShape);
				}
				else if(theToken != null && !theToken.startsWith(PREFIX_TEMPLATESLIDE)) {
					someHeaderShapes.add(theShape);
				}
			}
		}
		return theCopyReferenceShape;
    }

	/**
	 * A valueCell is a cell that starts with the prefix CELLVALUE_ . The prefix
	 * is followed by a number indicating the order. The number can be either a
	 * normal int (e.g. CELLVALUE_1) or a flat hierarchy. In case of a hierarchy
	 * two numbers are given separated by a ':' (e.g. CELLVALUE_1:3). The first
	 * number defines the level, the second the order.
	 * 
	 * The cells will be sorted into different {@link Map}s. In case of a
	 * hierarchy the first level cells will be stored in theParentShapes. the
	 * second level cells will be stored in theChildrenShapes. If no hierarchy
	 * is defined, theParentShapes are not used.
	 * 
	 * @param someParentShapes
	 *            Map for the cells in level 1. The passed in map will be filled
	 *            with the respective cell shapes.
	 * @param someChildrenShapes
	 *            Map for the level two cells or all cells if no hierarchy is
	 *            used. The passed in map will be filled with the respective
	 *            cell shapes.
	 * @param aCopyReferenceShape
	 *            the cell that is used to calculate the positions for the cells
	 *            that have to be created.
	 * @param aShape
	 *            the cell that is examined.
	 * @param aToken
	 *            the token that defines the replacement
	 * @return the aCopyReferenceShape.
	 */
	private TextShape parseValueCells(Map someParentShapes, Map someChildrenShapes, TextShape aCopyReferenceShape, TextShape aShape, String aToken) {
	    String theID = aToken.substring(aToken.lastIndexOf('_') + 1);
	    int pos = theID.indexOf(":");
	    if(pos == -1) {
	    	Integer idx = Integer.valueOf(theID);
	    	final int intIdx =  idx.intValue();
	    	if(intIdx == FIRST_CELL) {
	    		aCopyReferenceShape = aShape;
	    	}
	    	someChildrenShapes.put(idx, aShape);
	    }
	    else {
	    	Integer levelIdx = Integer.valueOf(theID.substring(0, pos));
	    	Integer childIdx = Integer.valueOf(theID.substring(pos+1));
	    	final int intLevelIdx = levelIdx.intValue();
	    	final int intChildIdx = childIdx.intValue();
	    	if(intLevelIdx == FIRST_CELL && intChildIdx == FIRST_CELL) {
	    		aCopyReferenceShape = aShape;
	    	}
	    	switch(intLevelIdx) {
	    	case 1:
	    		someParentShapes.put(childIdx, aShape);
	    		break;
	    	case 2:
	    		someChildrenShapes.put(childIdx, aShape);
	    		break;
	    	}
	    }
	    return aCopyReferenceShape;
    }

	private void parseChildren(List someChildren, List someValueShapes, List somePictureShapes, List someHeaderShapes, Map someChildrenShapes, Map someParentShapes, Sheet aSlide, TextShape aCopyReferenceShape) {
		for (int i = 0; i < someChildren.size(); i++) {
			PptTableContainer theTableContainer = (PptTableContainer) someChildren.get(i);
			if(theTableContainer.hasChildren()) {
				parseChildren(theTableContainer.getChildren(), someValueShapes, somePictureShapes, someHeaderShapes, someChildrenShapes, someParentShapes, aSlide, aCopyReferenceShape);
			}
			else {
				Sheet theNewSlide;
				SlideShow theSlideShow = aSlide.getSlideShow();
				if(this.doCreate) {
					try {
						theNewSlide = theSlideShow.createSlide();
					} catch (IOException ex) {
						throw new IOError(ex);
					}
				}
				else {
					theNewSlide = aSlide;
					this.doCreate = true;
				}
				for(int j = 0; j < someHeaderShapes.size(); j++) {
					AutoShape theShape = (AutoShape) someHeaderShapes.get(j);
					Rectangle2D theAnchor = theShape.getAnchor();
					AutoShape theNewShape = theShape.getSheet().createAutoShape();
					theNewShape.setShapeType(ShapeType.RECT);
					theNewShape.setAnchor(theAnchor);
					PptFont theFont = PptFont.createFont((TextShape<?, ?>) theShape);
					adaptFill(theShape, theNewShape);
					String text = theShape.getText();
					TextRun textRun = theNewShape.setText(text);
					theFont.applyFont(textRun);
					theNewSlide.addShape(theNewShape);
				}
				for(int j = 0; j < someValueShapes.size(); j++) {
					AutoShape theShape = (AutoShape) someValueShapes.get(j);
					String theToken = theShape.getText().toLowerCase().substring(PREFIX_VALUE.length());
					Rectangle2D theAnchor = theShape.getAnchor();
					AutoShape theNewShape = theNewSlide.createAutoShape();
					theNewShape.setShapeType(ShapeType.RECT);
					theNewShape.setAnchor(theAnchor);
					PptFont theFont = PptFont.createFont((TextShape<?, ?>) theShape);
					adaptFill(theShape, theNewShape);
					PptCell theCell = theTableContainer.getCell(theToken);
                    String value = null;
                    if(theCell != null) {
                        value = theCell.getValue();
						theNewShape.setFillColor(theCell.getCellStyle().getFillColor());
                    }
                    else {
						theNewShape.setFillColor(Color.WHITE);
                    }
					TextRun textRun = theNewShape.setText(value == null ? "" : value);
					theFont.applyFont(textRun);
					theNewSlide.addShape(theNewShape);
				}
				for(int j = 0; j < somePictureShapes.size(); j++) {
					try {
						TextShape theShape = (TextShape) somePictureShapes.get(j);
						String theToken = theShape.getText().toLowerCase().substring(PREFIX_PICTURE.length());
						PptCell theCell = theTableContainer.getCell(theToken);
						if(theCell != null) {
							Rectangle2D theAnchor = theShape.getAnchor();
							Object theData = theCell.getData();
							double x = theAnchor.getX() + theAnchor.getWidth()/2;
							double y = theAnchor.getY() + theAnchor.getHeight()/2;
	
							PictureData data =
								theSlideShow.addPicture(POIPowerpointXUtil.getImageBytes(theData), PictureType.PNG);
							PictureShape picShape = theShape.getSheet().createPicture(data);
							theNewSlide.addShape(picShape);
							double picHeight = picShape.getAnchor().getHeight();
							double picWidth = picShape.getAnchor().getWidth();
							picShape.setAnchor(new Rectangle2D.Double(x-picWidth/2, y-picHeight/2, picWidth, picHeight));
						}
					}
					catch(IOException ioe) {
						Logger.error("Could not add picture", ioe, this.getClass());
					}
				}
				PptCell theCell = theTableContainer.getCell(TEMPTABLE);
				if(theCell != null) {
					Object theVals = theCell.getData();
					if(!CollectionUtil.isEmptyOrNull(someParentShapes) && !CollectionUtil.isEmptyOrNull(someChildrenShapes)) {
						replaceParentsAndChildren(theNewSlide, someParentShapes, someChildrenShapes, theVals, aCopyReferenceShape);
					}
					else if(!CollectionUtil.isEmptyOrNull(someChildrenShapes)) {
						replaceChildrenOnly(theNewSlide, someChildrenShapes, theVals, aCopyReferenceShape);
					}
				}
			}
		}
    }

	private void createTable(Sheet aSlide, List<? extends Shape<?, ?>> aShapeArray, Map someTokens) {
		String theTitle  = extractTitle(aSlide);
		Object theVals = someTokens.get(theTitle);
		TextShape theCopyReferenceShape = null;
		Map theParents = new HashMap();
		Map theChildren = new HashMap();
		for (int i = 0; i < aShapeArray.size(); i++) {
			Shape theShape = aShapeArray.get(i);
			if (theShape instanceof GroupShape<?, ?>) {
				GroupShape<?, ?> theGrp = (GroupShape<?, ?>) theShape;
				List<? extends Shape<?, ?>> sh = POIPowerpointUtil.shapesStable(theGrp);
				createTable(aSlide, sh, someTokens);
			}
			else if (theShape instanceof TextShape) {
				TextShape shape = (TextShape) theShape;
				String theToken = shape.getText();
				if(theToken != null && theToken.startsWith(PREFIX_CELLVALUE)) {
					theCopyReferenceShape = parseValueCells(theParents, theChildren, theCopyReferenceShape, shape, theToken);
				}
			}
		}
		if(theVals == null ) {
			removeShapes(aSlide, theChildren);
			removeShapes(aSlide, theParents);
			return;				
		}
		else if(!CollectionUtil.isEmptyOrNull(theParents) && !CollectionUtil.isEmptyOrNull(theChildren)) {
			replaceParentsAndChildren(aSlide, theParents, theChildren, theVals, theCopyReferenceShape);
		}
		else if(!CollectionUtil.isEmptyOrNull(theChildren)) {
			replaceChildrenOnly(aSlide, theChildren, theVals, theCopyReferenceShape);
		}
		else {
			// in this case it was either the wrong shape group or a different error occurred
			return;
		}
	}

	private void replaceChildrenOnly(Sheet aSlide, Map someChildren, Object someVals, TextShape aRefShape) {
		if(someVals instanceof Object[][]) {
			Object[][] theRepls = (Object[][]) someVals;
			int number = theRepls.length;
			Rectangle2D firstAnchor = aRefShape.getAnchor();
			double firstY = firstAnchor.getY();
			Rectangle2D lastAnchor = ((Shape) someChildren.get(Integer.valueOf(someChildren.size()))).getAnchor();
			double lastY = lastAnchor.getY();
			double grpHeight = lastY + lastAnchor.getHeight() - firstY;
			double currentY = firstY;
			for (int i = 0; i < number; i++) {
				for(int j = 1; j <= someChildren.size(); j++) {
					Integer theKey = Integer.valueOf(j);
					AutoShape theCurrent = (AutoShape) someChildren.get(theKey);
					PptFont theFont = PptFont.createFont((TextShape<?, ?>) theCurrent);
					AutoShape theNew = aSlide.createAutoShape();
					theNew.setShapeType(ShapeType.RECT);
					Rectangle2D anchor = theCurrent.getAnchor();
					Rectangle2D theA = new Rectangle2D.Double(anchor.getX(), currentY, anchor.getWidth(), anchor.getHeight());
					theNew.setAnchor(theA);
					FillStyle fill = theNew.getFillStyle();
					theNew.setFillColor(Color.WHITE);
					TextRun textRun = theNew.setText(StringServices.getEmptyString((String) theRepls[i][j - 1]));
					theFont.applyFont(textRun);
					aSlide.addShape(theNew);
				}
				currentY += grpHeight;
			}
			removeShapes(aSlide, someChildren);
		}
    }

	private void replaceParentsAndChildren(Sheet aSlide, Map someParents, Map someChildren, Object someVals, TextShape aRefShape) {
		if(someVals instanceof List) {
			List theParentContainers = (List) someVals;
			int numParents = theParentContainers.size();
			Rectangle2D firstAnchor = aRefShape.getAnchor();
			double firstY = firstAnchor.getY();
			double currentY = firstY;
			for(int i = 0; i < numParents; i++) {
				PptTableContainer theParent = (PptTableContainer) theParentContainers.get(i);
				List theChildren = theParent.getChildren();
				int numChildren = theChildren.size();
				
				for(int h = 1; h <= someParents.size(); h++) {
					Integer theKey = Integer.valueOf(h);
					AutoShape theParentShape = (AutoShape) someParents.get(theKey);
					PptFont theFont = PptFont.createFont((TextShape<?, ?>) theParentShape);
					AutoShape theNew = aSlide.createAutoShape();
					theNew.setShapeType(ShapeType.RECT);
					Rectangle2D anchor = theParentShape.getAnchor();
					Rectangle2D theA = new Rectangle2D.Double(anchor.getX(), currentY, anchor.getWidth(), anchor.getHeight());
					theNew.setAnchor(theA);
					
					adaptFill(theParentShape, theNew);
					
					TextRun textRun = theNew.setText(theParent.getName());
					theFont.applyFont(textRun);
					aSlide.addShape(theNew);
					currentY += firstAnchor.getHeight();
					for(int j = 1; j <= numChildren; j++) {
						PptTableContainer theChildContainer = (PptTableContainer) theChildren.get(j-1);
						for(int k = 1; k <= someChildren.size(); k++) {
							theKey = Integer.valueOf(k);
							AutoShape theChildShape = (AutoShape) someChildren.get(theKey);
							AutoShape theNewChild = aSlide.createAutoShape();
							theNewChild.setShapeType(ShapeType.RECT);
							anchor = theChildShape.getAnchor();
							theA = new Rectangle2D.Double(anchor.getX(), currentY, anchor.getWidth(), anchor.getHeight());
							theNewChild.setAnchor(theA);
							
							adaptFill(theChildShape, theNewChild);
							PptCell theCell = theChildContainer.getCellByIndex(k-1);
							String theCellValue = theCell.getValue();
							String theStringVal = theCellValue == null ? "" : theCellValue;
							theNewChild.setFillColor(theCell.getCellStyle().getFillColor());
							TextRun run = theNewChild.setText(theStringVal);
							theFont.applyFont(run);
							aSlide.addShape(theNewChild);
						}
						currentY += anchor.getHeight();
					}
				}
			}
			removeShapes(aSlide, someParents);
			removeShapes(aSlide, someChildren);
		}
    }

	/**
	 * Adapts the {@link SimpleShape} settings from one fill to another.
	 * 
	 * @param aRefFill
	 *        the original {@link SimpleShape} which shall be "copied"
	 * @param aFill
	 *        the {@link SimpleShape} that shall be adapted.
	 */
	private void adaptFill(SimpleShape<?, ?> aRefFill, SimpleShape<?, ?> aFill) {
		Color backgroundColor = aRefFill.getFillColor();
		aFill.setFillColor(backgroundColor == null ? Color.WHITE : backgroundColor);
    }

	/**
	 * Takes a {@link Slide} and removes all {@link Shape}s defined in the
	 * given {@link Map}.
	 * 
	 * @param aSlide
	 *            a {@link Slide} which contains the {@link Shape}s to be
	 *            removed.
	 * @param aMap
	 *            a {@link Map} of {@link Shape}s to be removed.
	 */
	private void removeShapes(Sheet aSlide, Map aMap) {
		Set theKeys = aMap.keySet();
	    for(Iterator theIt = theKeys.iterator(); theIt.hasNext();) {
	    	Object theKey = theIt.next();
			AutoShape theShape = (AutoShape) aMap.get(theKey);
			ShapeContainer thePShape = theShape.getParent();
			if (thePShape instanceof GroupShape<?, ?>) {
				aSlide.removeShape((GroupShape<?, ?>) thePShape);
				continue;
			}
			else {
				boolean removed = aSlide.removeShape(theShape);
				if(!removed) {
					Logger.info("Problems removing shape " + theShape.getText(), POIPowerpointUtil.class);
				}
	    	}
	    }
    }
	
	/**
	 * Takes a {@link Slide} and removes all {@link Shape}s defined in the
	 * given {@link List}.
	 * 
	 * @param aSlide
	 *            a {@link Slide} which contains the {@link Shape}s to be
	 *            removed.
	 * @param aList
	 *            a {@link List} of {@link Shape}s to be removed.
	 */
	private void removeShapes(Sheet aSlide, List<Shape> aList) {
		for(int i = 0; i < aList.size(); i++) {
			Shape theShape = aList.get(i);
			ShapeContainer thePShape = theShape.getParent();
			if (thePShape instanceof GroupShape<?, ?>) {
				aSlide.removeShape((GroupShape<?, ?>) thePShape);
				continue;
			}
			else {
				boolean removed = aSlide.removeShape(theShape);
				if(!removed) {
					Logger.info("Problems removing shape " + theShape, POIPowerpointUtil.class);
				}
			}
		}
	}

	/**
	 * Takes a String and parses it for tokens starting with one of the
	 * predefined prefixes. The tokens found are stored in a {@link List} which
	 * is also the return value.
	 * 
	 * @param aSearchText
	 *            a String to parse.
	 * @return the tokens found in the theShapeText.
	 */
	private List<String> findTokens(String aSearchText) {
		if(aSearchText == null) {
			return null;
		}
		List<String> result = new ArrayList<>();
		String toTest = aSearchText;
		while(toTest.length() > 6) {
			boolean startsWithPrefix = false;
			for (int i = 0; i < PREFIX_VALUES.length; i++) {
				if (toTest.startsWith(PREFIX_VALUES[i])) {
					startsWithPrefix = true;
				}
			}

			if (startsWithPrefix || toTest.startsWith(SLIDE_COUNT)) {
				toTest = extractToken(result, toTest);
			}
			else {
				toTest = toTest.substring(1);
			}
		}
	    return result;
    }

	/**
	 * Takes a {@link String} that starts with one of the defined Token prefixes
	 * removes the token from the String, adds it to the result {@link List} and
	 * returns the rest of the string.
	 * 
	 * @param aResultList
	 *            a List of Tokens
	 * @param toTest
	 *            a String starting with one of the token prefixes
	 * @return the passed in string without the token
	 */
	private String extractToken(List<String> aResultList, String toTest) {
	    String[] split = toTest.split("[^\\w]");
	    String theToken = split[0];
	    if (split.length > 1) {
	    	toTest = toTest.substring(theToken.length());
	    	aResultList.add(theToken);
	    }
	    else {
	    	aResultList.add(theToken);
	    	toTest = StringServices.EMPTY_STRING;
	    }
	    return toTest;
    }

	/**
	 * Replaces picture and value token with the values defined in the given map
	 * of tokens.
	 * 
	 * @param aSlide
	 *            a slide that contains the shapes with the tokens to be
	 *            replaced.
	 * @param aShape
	 *            the shape that contains the token
	 * @param anAchor
	 *            the anchor of the shape to be replace. Needed for correct
	 *            positioning of pictures.
	 * @param aToken
	 *            the token to be replaced
	 * @param aReplacable
	 *            an object that is used to replace the token
	 * @throws IOException
	 *             if the picture shape could not be added to the {@link Slide}/
	 *             {@link SlideShow}.
	 */
	private void replaceToken(Sheet aSlide, TextShape aShape, Rectangle2D anAchor, String aToken, Object aReplacable) throws IOException {
		/*SlideShow ppt = */ aSlide.getSlideShow();
		if (aReplacable == null && !aToken.startsWith(SLIDE_COUNT)) {
			// remove token from text
			String text = aShape.getText();
			aShape.setText(text.replace(aToken, ""));
			return;
		}
		if (aToken.startsWith(PREFIX_AUTOTABLE)) {
		    if (aReplacable instanceof PptTable) { 
		        //PptTable.writeAsTable(aSlide, aShape, (PptTable) aReplacable);
		        PptTable.writeAsShapes(aSlide, aShape, (PptTable) aReplacable);
		    }
		    else if (aReplacable != null) {
		        throw new IllegalArgumentException("The value for '"+aToken+"' must be a PptTable");
		    }
		}
		else if(aToken.startsWith(PREFIX_PICTURE)) {
			if (aReplacable instanceof File || aReplacable instanceof ImageReplacerData
				|| aReplacable instanceof BinaryContent) {
				replacePicture(aSlide, aShape, aReplacable);
            }
            else if (aReplacable != null) {
                throw new IllegalArgumentException("The value for '"+aToken+"' must be a File");
            }
		}
		else if(aToken.startsWith(PREFIX_VALUE)) {
			String theShString = aShape.getText();
			String replaceAll = "";
			if(aReplacable instanceof String) {
				replaceAll = theShString.replaceAll(aToken, ((String) aReplacable).replaceAll("([\\$\\\\])", "\\\\$1"));
			}
			else if(aReplacable instanceof PptCell) {
				PptCell pptCell = ((PptCell)aReplacable);
				String value = pptCell.getValue();
				replaceAll = value == null ? "" : value;
				aShape.setFillColor(pptCell.getCellStyle().getFillColor());
			}
			
			try {
				this.setText2TextShape(aSlide, aShape, replaceAll);
			}
			catch (NullPointerException e) {
			    int          theLength = replaceAll.length();
                StringBuffer theBuffer = new StringBuffer(theLength);

                for (int thePos = 0; thePos < theLength; thePos++) {
                    char theChar = replaceAll.charAt(thePos);

                    if (Character.isLetterOrDigit(theChar)) {
                        theBuffer.append(theChar);
                    }
                    else {
                        switch (theChar) {
                        case ' ':
                        case '.':
                        case ',':
                        case ':':
                        case ';':
                        case '"':
                        case '+':
                        case '-':
                        case '*':
                        case '/':
                        case '=':
                        case '€':
                        case '$':
                        case '(':
                        case ')':
                        case '!':
                        case '?':
                        case '\'':
                            theBuffer.append(theChar);
                            break;
                        default:
                            theBuffer.append('_');
                            break;
                        }
                    }
                }

                try {
                    this.setText2TextShape(aSlide, aShape, theBuffer.toString());
                }
                catch (NullPointerException ex) {
    				Logger.error("The value for the field '" + theShString.replaceAll(PREFIX_VALUE, "") + "' most likely contains chars which can not be interpreted by POI", ex, POIPowerpointUtil.class);
    				aShape.setText("");
                }
			}
		}
		else if (aReplacable != null && aToken.startsWith(SLIDE_COUNT)) {
			String theShString = aShape.getText();
			String replaceAll = "";

			if (aReplacable instanceof String) {
				replaceAll = theShString.replaceAll(aToken, (String)aReplacable);
			}
			else if(aReplacable instanceof Integer) {
				Integer num = (Integer) aReplacable;
				replaceAll = theShString.replaceAll(aToken, num.toString());
			}

			aShape.setText(replaceAll);
		}
	}

    protected void setText2TextShape(Sheet aSlide, TextShape aShape, String aText) {
		aShape.setText(aText);
    }

	private Shape replacePicture(Sheet aSlide, Shape aShape, Object aReplacable) throws IOException {
		Rectangle2D theBox = adaptToAspectRatio(aShape.getAnchor().getBounds2D(), aReplacable);
		PictureData data =
			aSlide.getSlideShow().addPicture(POIPowerpointXUtil.getImageBytes(aReplacable), PictureType.PNG);
		PictureShape picShape = aSlide.createPicture(data);
        picShape.setAnchor(theBox); // use width and height from the layout box
        
        aSlide.addShape(picShape);
        aSlide.removeShape(aShape);
        
        return picShape;
	}
	
	private Rectangle2D adaptToAspectRatio(Rectangle2D aBox, Object anImage) {
		if (anImage instanceof ImageReplacerData) {
			return POIPowerpointXUtil.adaptToAspectRatio(aBox, (ImageReplacerData) anImage);
		}
		try {
			ImageInputStream theIOS = POIPowerpointXUtil.getImageInputStream(anImage);
			Iterator<ImageReader> theIter  = ImageIO.getImageReaders(theIOS);
			
			if (theIter.hasNext()) {
				ImageReader theReader = theIter.next();
				theReader.setInput(theIOS);
				
				float aspectRatio = theReader.getAspectRatio(0);
				Dimension inputDim = new Dimension(Math.round((float)aBox.getWidth()),
												   Math.round((float)aBox.getHeight()));
				Dimension outDim = Utils.getImageDimension(inputDim, aspectRatio);
				return new Rectangle(Math.round((float)aBox.getX()),
									 Math.round((float)aBox.getY()),
									 outDim.width,
									 outDim.height); 
			}
		} catch (IOException e) {
			Logger.warn("Failed to adapt aspect ratio for '" + POIPowerpointXUtil.getImageDebugName(anImage)
				+ "'. Using original size.", e, POIPowerpointUtil.class);
		}
		return aBox;
	}
	
	
	/**
	 * Prints the given {@link Shape}s with the given indentation to
	 * System.out. For each {@link Shape} the type of the shape, its position
	 * and (if present) its text will be printed.
	 * 
	 * @param anIndentation
	 *            a String defining the indentation.
	 * @param aShapeArray
	 *            a Array of {@link Shape}s to print.
	 */
	public static void printShapes(String anIndentation, List<? extends Shape<?, ?>> aShapeArray) {
		for (Shape<?, ?> theShape : aShapeArray) {
			// shapes's anchor which defines the position of this shape in the
			// slide
			Rectangle2D anchor = theShape.getAnchor();

			if (theShape instanceof AutoShape) {
				AutoShape shape = (AutoShape) theShape;
				System.out.println(anIndentation + "ShapeType: AutoShape");
				System.out.println(anIndentation + "\t" + shape.getText());
				System.out.println(anIndentation + "\tx: " + anchor.getX() + ", y: " + anchor.getY());
				System.out.println(anIndentation + "\theight: " + anchor.getHeight() + ", width: " + anchor.getWidth());
			}
			else if (theShape instanceof TextBox) {
				TextBox shape = (TextBox) theShape;
				System.out.println(anIndentation + "ShapeType: TextBox");
				System.out.println(anIndentation + "\t" + shape.getText());
			}
			else if (theShape instanceof Picture) {
				System.out.println(anIndentation + "ShapeType: Picture");
			}
			else if (theShape instanceof TableShape<?, ?>) {
				TableShape<?, ?> table = (TableShape<?, ?>) theShape;
				System.out.println(anIndentation + "ShapeType: Table");
			}
			else if (theShape instanceof GroupShape<?, ?>) {
				GroupShape<?, ?> theGrp = (GroupShape<?, ?>) theShape;
				System.out.println(anIndentation + "ShapeType: ShapeGroup");
				printShapes(anIndentation + "\t", POIPowerpointUtil.shapes(theGrp));
			}
			else {
				System.out.println(anIndentation + "shape: " + theShape);
			}
		}
	}

	public static <S extends Shape<S, P>, P extends TextParagraph<S, P, ?>> List<S> shapesStable(
			ShapeContainer<S, P> shapeContainer) {
		return new ArrayList<>(shapes(shapeContainer));
	}

	public static <S extends Shape<S, P>, P extends TextParagraph<S, P, ?>> List<S> shapes(
			ShapeContainer<S, P> shapeContainer) {
		return shapeContainer.getShapes();
	}
}
