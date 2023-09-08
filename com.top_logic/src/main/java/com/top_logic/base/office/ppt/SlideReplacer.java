/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hslf.model.Polygon;
import org.apache.poi.hslf.usermodel.HSLFAutoShape;
import org.apache.poi.hslf.usermodel.HSLFFill;
import org.apache.poi.hslf.usermodel.HSLFPictureShape;
import org.apache.poi.hslf.usermodel.HSLFPlaceholder;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.sl.usermodel.AutoShape;
import org.apache.poi.sl.usermodel.FreeformShape;
import org.apache.poi.sl.usermodel.Hyperlink;
import org.apache.poi.sl.usermodel.Line;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.PictureShape;
import org.apache.poi.sl.usermodel.PlaceableShape;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.SimpleShape;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.TableCell;
import org.apache.poi.sl.usermodel.TableShape;
import org.apache.poi.sl.usermodel.TextBox;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.sl.usermodel.TextShape;

import com.top_logic.base.office.ppt.PptCell.PptCellStyle;
import com.top_logic.basic.Logger;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.tool.export.AbstractOfficeExportHandler.OfficeExportValueHolder;

/**
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
class SlideReplacer {

	private final POIPowerpoint powerpoint;

	/* package protected */SlideReplacer(POIPowerpoint aPpt) {
		this.powerpoint = aPpt;
	}

	/* package protected */void replaceSlides(SlideShow aShow, POIPowerpointUtil anUtil, Map/*
																							 * <String
																							 * ,
																							 * SlideReplacement
																							 * >
																							 */someReplacements)
			throws IOException {

		for (Iterator theIt = someReplacements.keySet().iterator(); theIt.hasNext();) {
			String theKey = (String) theIt.next();

			if (theKey.startsWith(POIPowerpoint.ADD_SLIDES)) {
				SlideReplacement theReplace = (SlideReplacement) someReplacements.get(theKey);
				String theFilename = theReplace.getTemplateFilename();

				if (theFilename != null) {
					Slide slide = aShow.createSlide();
					if (slide instanceof HSLFSlide) {
						((HSLFSlide) slide).addTitle();
					}
					SlideShow<?, ?> theShow =
						this.powerpoint.createSlideShow(this.getTemplateFileInputStream(theFilename));
					// TODO Copy all slides!?
					Slide<?, ?> theSlide = theShow.getSlides().get(0);
					List<? extends Shape<?, ?>> theShapes = POIPowerpointUtil.shapesStable(theSlide);

					for (Iterator<OfficeExportValueHolder> theContext =
						theReplace.getOfficeExportValueHolders().iterator(); theContext.hasNext();) {
						fillEmptySlide(aShow.createSlide(), anUtil, theShapes, theContext.next());
					}

					aShow.getSlides().remove(0);
					aShow.getSlides().remove(0);
				}
			}
		}
	}

	protected Slide<?, ?> fillEmptySlide(Slide<?, ?> aSlide, POIPowerpointUtil anUtil,
			List<? extends Shape<?, ?>> someShapes,
			OfficeExportValueHolder someValues) throws IOException {
		// Copy all shapes of one sheet to another
		for (Shape<?, ?> theShape : someShapes) {
			// TODO cannot copy Background for now

			// copy text, font and style settings
			if (theShape instanceof HSLFPlaceholder && aSlide instanceof HSLFSlide) {
				this.copyPlaceholder((HSLFPlaceholder) theShape, (HSLFSlide) aSlide);
			} else if (theShape instanceof TextBox) {
				this.copyTextBox((TextBox) theShape, aSlide);
			} else if (theShape instanceof Polygon) {
				this.copyPolygon((Polygon) theShape, aSlide);
			} else if (theShape instanceof FreeformShape<?, ?>) {
				this.copyFreeform((FreeformShape<?, ?>) theShape, aSlide);
			} else if (theShape instanceof AutoShape) {
				this.copyAutoShape((AutoShape) theShape, aSlide, null);
			} else if (theShape instanceof TableShape) {
				this.copyTable((TableShape) theShape, aSlide);
			} else if (theShape instanceof PictureShape) {
				this.copyPicture((PictureShape) theShape, aSlide);
			} else if (theShape instanceof Line) {
				this.copyLine((Line) theShape, aSlide);
			}
		}

		// replace the values in the copy
		this.powerpoint.processSlide(aSlide, anUtil, (Map) someValues.exportData);

		return aSlide;
	}

	protected void copyTable(TableShape<?, ?> anOldTable, Slide aNew) {
		int theCols = anOldTable.getNumberOfColumns();
		int theRows = anOldTable.getNumberOfRows();
		TableShape<?, ?> theNewTable = aNew.createTable(theRows, theCols);

		Rectangle2D theCoords = anOldTable.getAnchor();

		aNew.addShape(theNewTable);

		for (int theCol = 0; theCol < theCols; theCol++) {
			for (int theRow = 0; theRow < theRows; theRow++) {
				TableCell theOldCell = anOldTable.getCell(theRow, theCol);
				TableCell theNewCell = theNewTable.getCell(theRow, theCol);

				if (theOldCell != null) {
					this.copyTableCell(theOldCell, theNewCell);
				}
			}
		}

		theNewTable.setAnchor(theCoords);
		// this.copyTableStyle(anOldTable, theNewTable);
	}

	protected void copyFill(Shape anOldShape, Shape aNewShape) {
		if (!(anOldShape instanceof HSLFShape && aNewShape instanceof HSLFShape)) {
			return;
		}
		// TODO handle fill types, esp. pictures
		HSLFFill theOldFill = ((HSLFShape) anOldShape).getFill();
		HSLFFill theNewFill = ((HSLFShape) aNewShape).getFill();
		Color theBG = theOldFill.getBackgroundColor();
		Color theFG = theOldFill.getForegroundColor();

		if (theBG != null) {
			theNewFill.setBackgroundColor(theBG);
		}

		if (theFG != null) {
			theNewFill.setForegroundColor(theFG);
		}
	}

	protected void copyShape(Shape anOldShape, Slide aNew, Shape aNewShape) {
		if (aNew != null) {
			aNew.addShape(aNewShape);
		}
		if (aNewShape instanceof PlaceableShape<?, ?>) {
			((PlaceableShape<?, ?>) aNewShape).setAnchor(anOldShape.getAnchor());
		}
		this.copyFill(anOldShape, aNewShape);
	}

	protected void copySimpleShape(SimpleShape anOldShape, Slide aNew, SimpleShape aNewShape) {
		this.copyShape(anOldShape, aNew, aNewShape);

		PptCellStyle.createCellStyle(anOldShape).applyStyle(aNewShape);
	}

	protected void copyTextShape(TextShape anOldShape, Slide aNew, TextShape aNewShape) {
		this.copySimpleShape(anOldShape, aNew, aNewShape);

		this.copyTextRun(anOldShape, aNewShape);
	}

	protected void copyTextBox(TextBox anOldShape, Slide aNew) {
		TextBox theNewShape = aNew.createTextBox();

		this.copyTextShape(anOldShape, aNew, theNewShape);
	}

	protected void copyPlaceholder(HSLFPlaceholder anOldShape, HSLFSlide aNew) {
		HSLFPlaceholder theNewShape = new HSLFPlaceholder(aNew);

		this.copyTextShape(anOldShape, aNew, theNewShape);
	}

	protected void copyTableCell(TableCell anOldShape, TableCell aNewShape) {
		this.copyTextShape(anOldShape, null, aNewShape);

		aNewShape.setInsets(anOldShape.getInsets().copy());
	}

	protected void copyLine(Line anOldShape, Slide aNew) {
		AutoShape theLine = aNew.createAutoShape();
		theLine.setShapeType(ShapeType.LINE);
		this.copySimpleShape(anOldShape, aNew, theLine);
		// TODO handle line styles
	}

	protected void copyAutoShape(AutoShape anOldShape, Slide aNew, AutoShape aNewShape) {
		if (aNewShape == null) {
			TextBox aBox = aNew.createTextBox();
			copyTextShape(anOldShape, aNew, aBox);
		} else {
			copyTextShape(anOldShape, aNew, aNewShape);

			if (aNewShape instanceof HSLFAutoShape && anOldShape instanceof HSLFAutoShape) {
				HSLFAutoShape newImpl = (HSLFAutoShape) aNewShape;
				HSLFAutoShape oldImpl = (HSLFAutoShape) anOldShape;
				for (int idx = 0; idx <= 9; idx++) {
					newImpl.setAdjustmentValue(idx, oldImpl.getAdjustmentValue(idx));
				}
			}
		}
	}

	protected void copyFreeform(FreeformShape<?, ?> anOldShape, Slide aNew) {
		FreeformShape<?, ?> newForm = aNew.createFreeform();

		this.copyAutoShape(anOldShape, aNew, newForm);

		newForm.setPath(anOldShape.getPath());
	}

	protected void copyPolygon(Polygon anOldShape, Slide aNew) {
		Polygon newForm = new Polygon();

		this.copyAutoShape(anOldShape, aNew, newForm);

		// TODO get Points from old shape (EscherProperties...)
		// newForm.setPoints(XXX);
	}

	/**
	 * Copy a given picture to a given slide
	 * 
	 * @param oldPicture
	 *        the old picture. Must not be <code>null</code>
	 * @param aNew
	 *        the new slide. Must not be <code>null</code>
	 */
	protected void copyPicture(PictureShape<?, ?> oldPicture, Slide aNew) {
		PictureData pictureData = oldPicture.getPictureData();

		PictureType pictureFormat = pictureData.getType();
		byte[] picData = pictureData.getData();

		try {
			PictureData newData = aNew.getSlideShow().addPicture(picData, pictureFormat);
			PictureShape<?, ?> newPicture = aNew.createPicture(newData);

			Hyperlink<?, ?> oldLink = oldPicture.getHyperlink();
			if (oldLink != null) {
				Hyperlink<?, ?> newLink = newPicture.createHyperlink();
				newLink.setAddress(oldLink.getAddress());
				newLink.setLabel(oldLink.getLabel());
			}
			copyPictureName(oldPicture, newPicture);
			newPicture.setRotation(oldPicture.getRotation());

			this.copySimpleShape(oldPicture, aNew, newPicture);
		} catch (IOException ioe) {
			Logger.warn("Cannot copy picture", this);
		}
	}

	private void copyPictureName(PictureShape<?, ?> oldPicture, PictureShape<?, ?> newPicture) {
		if (oldPicture instanceof HSLFPictureShape && newPicture instanceof HSLFPictureShape) {
			((HSLFPictureShape) newPicture).setPictureName(((HSLFPictureShape) oldPicture).getPictureName());
		}
	}

	/**
	 * Copy TextRun from old to new
	 */
	protected void copyTextRun(TextShape anOldShape, TextShape aNewShape) {
		this.setText(aNewShape.getSheet().getSlideShow(), aNewShape, anOldShape);
	}

	protected InputStream getTemplateFileInputStream(String aFilename) throws IOException {
		try {
			DataAccessProxy dap = new DataAccessProxy("webinf://reportTemplates", "ppt");
			dap = dap.getChildProxy(aFilename);
			return dap.getEntry();
		} catch (Exception e) {
			Logger.error("problem getting template file", e, this);
			throw new IOException("Problem getting template file");
		}
	}

	private void setText(SlideShow aShow, TextShape<?, ?> newShape, TextShape<?, ?> oldShape) {
		String theText = oldShape.getText();

		List<? extends TextParagraph<?, ?, ?>> oldParagraphs = oldShape.getTextParagraphs();
		List<? extends TextParagraph<?, ?, ?>> newParagraphs = newShape.getTextParagraphs();

		for (TextParagraph<?, ?, ?> oldParagraph : oldParagraphs) {
			boolean firstRun = true;
			for (TextRun oldRun : oldParagraph.getTextRuns()) {
				TextRun newRun = newShape.appendText(oldRun.getRawText(), firstRun);
				POIPowerpointUtil.applyRichTextRun(newRun, oldRun);
				firstRun = false;
			}
		}
	}
}
