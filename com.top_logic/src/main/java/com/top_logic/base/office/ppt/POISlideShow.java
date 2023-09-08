/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSheet;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideMaster;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTitleMaster;
import org.apache.poi.sl.usermodel.GroupShape;
import org.apache.poi.sl.usermodel.TableCell;
import org.apache.poi.sl.usermodel.TableShape;
import org.apache.poi.sl.usermodel.TextShape;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFGroupShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSheet;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTableRow;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * Interface for generic access to power point interfaces from POI. 
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class POISlideShow<S, M> {

	private final InputStream stream;

	public POISlideShow(BinaryData aFile) throws FileNotFoundException, IOException {
		this.stream = aFile.getStream();

		this.initSlideshow(this.stream);
	}

	/** 
	 * Initialize the slide show the implementation will work on.
	 * 
	 * @param    aStream    The content of the power point file as stream.
	 * @throws   IOException    When reading the stream fails.
	 */
	protected abstract void initSlideshow(InputStream aStream) throws IOException;

	/** 
	 * Return the normal slides to get the shapes from (needed for {@link #getShapes(Object)}.
	 * 
	 * <p>This method will be used by the central {@link #doVisit(PPTVisitor)} method for inspecting
	 * all possible aspects.</p>
	 * 
	 * @return    The requested slides to get shapes from afterwards.
	 */
	protected abstract List<? extends M> getSlides();

	/** 
	 * Return the master slides to get the shapes from (needed for {@link #getShapes(Object)}.
	 * 
	 * <p>This method will be used by the central {@link #doVisit(PPTVisitor)} method for inspecting
	 * all possible aspects.</p>
	 * 
	 * @return    The requested master slides to get shapes from afterwards.
	 */
	protected abstract List<? extends M> getSlideMasters();

	/** 
	 * Return the title slides to get the shapes from (needed for {@link #getShapes(Object)}.
	 * 
	 * <p>This method will be used by the central {@link #doVisit(PPTVisitor)} method for inspecting
	 * all possible aspects.</p>
	 * 
	 * @return    The requested title slides to get shapes from afterwards.
	 */
	protected abstract List<? extends M> getTitleMasters();

	/** 
	 * Return the shapes contained in the given master sheet (aka. slide).
	 * 
	 * @param    aSheet    The sheet to get the shapes from.
	 * @return   The requested shapes to extract values from.
	 */
	protected abstract List<S> getShapes(M aSheet);

	/** 
	 * Implementation specific handling of a shape.
	 * 
	 * @param    aVisitor    The visitor to be called.
	 * @param    aMap        The map of values read from the shape.
	 * @param    aShape      The shape to be inspected.
	 */
	protected abstract void doVisit(PPTVisitor<S> aVisitor, Map<String, ?> aMap, S aShape);

	/** 
	 * Return the text value from a shape.
	 * 
	 * @param    aShape    The shape to get the text from.
	 * @return   The requested text.
	 */
	protected abstract String getText(S aShape);

	/** 
	 * @see POIPowerpoint#getFields(Object, java.util.Stack)
	 */
	@SuppressWarnings("unchecked")
	public Map<String, List<? /* Shapes */>> getFields() {
		return (Map<String, List<?>>) this.doVisit(new SetVisitor<>(this));
	}

	/** 
	 * @see POIPowerpoint#getValuesFromDoc(Object, com.top_logic.basic.col.Mapping, java.util.Stack)
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getValuesFromDoc() {
		return (Map<String, Object>) this.doVisit(new GetVisitor<>(this));
	}

	/** 
	 * @see POIPowerpoint#closeDocument(Object, java.util.Stack)
	 */
	public void close() {
		FileUtilities.close(this.stream);
	}

	/** 
	 * Central call to get the values out of the handled slide show.
	 * 
	 * @param    aVisitor    The visitor to collect values.
	 * @return   The requested content from the slide show.
	 */
	protected Map<String, ?> doVisit(PPTVisitor<S> aVisitor) {
        Map<String, ?> theMap      = new HashMap<>();
        int            theSlidePos = 1;

        theSlidePos = this.doVisitSlides(aVisitor, theMap, this.getSlides(),       theSlidePos);
        theSlidePos = this.doVisitSlides(aVisitor, theMap, this.getSlideMasters(), theSlidePos);
        theSlidePos = this.doVisitSlides(aVisitor, theMap, this.getTitleMasters(), theSlidePos);

        return theMap;
    }

	/** 
	 * Iterate through the given shapes and call the visitor via {@link #doVisit(PPTVisitor, Map, Object)}.
	 * 
	 * @param aVisitor      The visitor to be called.
	 * @param aMap          The map to be filled.
	 * @param someShapes    The shapes to be visited.
	 */
	protected void doVisitShapes(PPTVisitor<S> aVisitor, Map<String, ?> aMap, List<S> someShapes) {
		int theShapePos = 1;

		for (S theShape : someShapes) {
		    aVisitor.setShape(theShapePos);

		    this.doVisit(aVisitor, aMap, theShape);

		    theShapePos++;
		}
	}

	private int doVisitSlides(PPTVisitor<S> aVisitor, Map<String, ?> aResult, List<? extends M> slides,
			int aSlidePos) {
		int theSlidePos = aSlidePos;

		for (M slide : slides) {
			aVisitor.setSlide(theSlidePos);

			this.doVisitShapes(aVisitor, aResult, this.getShapes(slide));

			theSlidePos++;
		}

		return theSlidePos;
	}

	/**
	 * Simple visitor for powerpoint, which checks, if the visited shape has a text frame.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public abstract static class PPTVisitor<S> {

		// Attributes

		private String slide;

		private String shape;

		private String cell;

		private final POISlideShow<S, ?> show;

		// Constructors

		@SuppressWarnings("javadoc")
		public PPTVisitor(POISlideShow<S, ?> aShow) {
			this.show = aShow;
		}

		// Abstract methods

		/** 
		 * Put a value read from the given shape into the given map.
		 * 
		 * @param    aMap      The map to store the value in.
		 * @param    aShape    The shape to get the value from.
		 * @param    aText     The text value read from the shape.
		 * @return   <code>true</code> when setting a value to the given map succeeds.
		 */
		protected abstract boolean putValue(Map<String, ?> aMap, S aShape, String aText);

        // Public methods

		/** 
		 * Visitor method for bringing  values from the shape to the map. 
		 * 
		 * @param    aMap      The map to store the value in.
		 * @param    aShape    The shape to get the value from.
		 * @return   <code>true</code> when setting a value to the given map succeeds.
		 * @see      POISlideShow#getText(Object)
		 * @see      #putValue(Map, Object, String)
		 */
		public boolean visit(Map<String, ?> aMap, S aShape) {
			String theText = this.show.getText(aShape);

			if (theText != null) {
				return this.putValue(aMap, aShape, theText);
            }
			else {
				return false;
			}
		}

		/** 
		 * @see #getKey()
		 */
		public void setSlide(int aSlide) {
			this.slide = Integer.toString(aSlide);
        }

		/** 
		 * @see #getKey()
		 */
		public void setShape(int aShape) {
			this.shape = Integer.toString(aShape);
        }

		/** 
		 * @see #getKey()
		 */
		public void setCell(int anX, int aY) {
			if (anX != -1) {
				this.cell = "(" + aY + ',' + anX + ')';
            }
            else {
				this.cell = null;
            }
        }

		// Protected methods

		/** 
		 * Return the unique ID of the current shape (or cell) this visitor is acting on.
		 * 
		 * <p>The ID will be created as a path notation with "Sl[#].Sh[#].Tb[#]" where
		 * "[#]" is a number as set by the setter methods in this class.</p> 
		 * 
		 * @return    The requested unique ID, never <code>null</code>.
		 */
		protected String getKey() {
			StringBuffer theResult = new StringBuffer("Sl");

			theResult.append(this.slide);

			theResult.append(".Sh");
			theResult.append(this.shape);

			if (this.cell != null) {
				theResult.append(".Tb");
				theResult.append(this.cell);
            }

			return theResult.toString();
        }
	}

	/**
	 * Visitor for extracting text from a shape (used by {@link POISlideShow#getValuesFromDoc()}.
	 * 
	 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class GetVisitor<S> extends PPTVisitor<S> {

		// Constructors

		/** 
		 * Creates a {@link GetVisitor}.
		 */
		public GetVisitor(POISlideShow<S, ?> aShow) {
			super(aShow);
		}

		// Overridden methods from PPTVisitor

		@SuppressWarnings("unchecked")
		@Override
		protected boolean putValue(Map<String, ?> aMap, S aShape, String aText) {
			((Map<String, String>) aMap).put(this.getKey(), aText);

			return true;
		}
	}

	/**
	 * Visitor for extracting text from a shape (used by {@link POISlideShow#getFields()}.
	 * 
	 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class SetVisitor<S> extends PPTVisitor<S> {

		// Constructors

		/** 
		 * Creates a {@link GetVisitor}.
		 */
		public SetVisitor(POISlideShow<S, ?> aShow) {
			super(aShow);
		}

		// Overridden methods from PPTVisitor

		@SuppressWarnings("unchecked")
		@Override
		protected boolean putValue(Map<String, ?> aMap, S aShape, String aText) {
			List<S> theList = (List<S>) aMap.get(aText);

			if (theList == null) {
				theList = new ArrayList<>();

				((Map<String, List<S>>) aMap).put(aText, theList);
            }

			return theList.add(aShape);
        }
    }

	/**
	 * POI power point implementation based on HSLF (for PPT files). 
	 * 
	 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class StandardSlideShow extends POISlideShow<HSLFShape, HSLFSheet> {

		private HSLFSlideShow show;

		public StandardSlideShow(BinaryData aFile) throws FileNotFoundException, IOException {
			super(aFile);
		}

		@Override
		protected void initSlideshow(InputStream aStream) throws IOException {
			this.show = new HSLFSlideShow(aStream);
		}

		@Override
		protected String getText(HSLFShape aShape) {
			return (aShape instanceof TextShape) ? ((TextShape) aShape).getText() : null;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected List<HSLFSlide> getSlides() {
			return this.show.getSlides();
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected List<HSLFSlideMaster> getSlideMasters() {
			return this.show.getSlideMasters();
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected List<HSLFTitleMaster> getTitleMasters() {
			return this.show.getTitleMasters();
		}

		@SuppressWarnings("unchecked")
		@Override
		protected List<HSLFShape> getShapes(HSLFSheet aSheet) {
			return POIPowerpointUtil.shapesStable(aSheet);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void doVisit(PPTVisitor<HSLFShape> aVisitor, Map<String, ?> aMap, HSLFShape aShape) {
	        aVisitor.setCell(-1, -1);

			if (aShape instanceof TableShape) {
				TableShape theTable = (TableShape) aShape;
				int theRows = theTable.getNumberOfRows();
				int theCols = theTable.getNumberOfColumns();

				for (int theRow = 0; theRow < theRows; theRow++) {
					for (int theCol = 0; theCol < theCols; theCol++) {
						TableCell theCell = theTable.getCell(theRow, theCol);

						aVisitor.setCell(theRow, theCol);
						aVisitor.visit(aMap, (HSLFShape) theCell);
	        		}
	        	}
			} else if (aShape instanceof GroupShape) {
				GroupShape theGroup = (GroupShape) aShape;

				this.doVisitShapes(aVisitor, aMap, POIPowerpointUtil.shapesStable(theGroup));
	        }
	        else { 
	        	aVisitor.visit(aMap, aShape);
	        }
	    }
	}

	/**
	 * POI power point implementation based on XSLF (for PPTX files). 
	 * 
	 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class XMLBasedSlideShow extends POISlideShow<XSLFShape, XSLFSheet> {

		private XMLSlideShow show;

		public XMLBasedSlideShow(BinaryData aFile) throws FileNotFoundException, IOException {
			super(aFile);
		}

		@Override
		protected void initSlideshow(InputStream aStream) throws IOException {
			this.show = new XMLSlideShow(aStream);
		}

		@Override
		protected String getText(XSLFShape aShape) {
			return (aShape instanceof XSLFTextShape) ? ((XSLFTextShape) aShape).getText() : null;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected List<XSLFSlide> getSlides() {
			return this.show.getSlides();
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected List<XSLFSlideMaster> getSlideMasters() {
			return this.show.getSlideMasters();
		}

		@Override
		protected List<XSLFSlideMaster> getTitleMasters() {
			return Collections.emptyList();
		}

		@SuppressWarnings("unchecked")
		@Override
		protected List<XSLFShape> getShapes(XSLFSheet aSheet) {
			return POIPowerpointUtil.shapesStable(aSheet);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void doVisit(PPTVisitor<XSLFShape> aVisitor, Map<String, ?> aMap, XSLFShape aShape) {
	        aVisitor.setCell(-1, -1);

	    	if (aShape instanceof XSLFTable) {
	    		XSLFTable theTable = (XSLFTable) aShape;
	    		int 	  theRow   = 0; 

	    		for (XSLFTableRow theTableRow : theTable.getRows()) {
	    			int theCol = 0; 

	    			for (XSLFTableCell theCell : theTableRow.getCells()) {
	    				aVisitor.setCell(theRow, theCol);
						aVisitor.visit(aMap, theCell);

	    				theCol++;
	    			}
	    			theRow++;
	    		}
	    	}
	    	else if (aShape instanceof XSLFGroupShape) {
	    		XSLFGroupShape theGroup = (XSLFGroupShape) aShape;

				this.doVisitShapes(aVisitor, aMap, POIPowerpointUtil.shapesStable(theGroup));
	        }
	        else { 
	        	aVisitor.visit(aMap, aShape);
	        }
	    }
	}
}
