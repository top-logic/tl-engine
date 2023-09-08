/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.tool.export.AbstractOfficeExportHandler.OfficeExportValueHolder;

/**
 * Handle slide replacements in {@link POIPowerpointX}
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class SlideReplacerX {

	public static final String DEFAULT_SLIDE_LAYOUT_NAME = "Leer";

	private final POIPowerpointX powerpoint;

	/**
	 * Create a new SlideReplacerX
	 * 
	 * @param aPpt
	 *        the POIPowerpointX
	 */
	protected SlideReplacerX(POIPowerpointX aPpt) {
		this.powerpoint = aPpt;
	}

	@SuppressWarnings("unchecked")
	/* package protected */int replaceSlide(XSLFSlide aSlide, int slideIdx, POIPowerpointXUtil anUtil,
			Map<String, Object> someReplacements, boolean scdRun) throws IOException {
		String addSlidesToken = getAddSlidesToken(aSlide);
		if (addSlidesToken != null) {
			XMLSlideShow slideShow = aSlide.getSlideShow();
			if (scdRun) { // Delete remaining ADDSLIDEs
				slideShow.removeSlide(slideIdx);
				return slideIdx;
			}

			SlideReplacement theReplace = (SlideReplacement) someReplacements.get(addSlidesToken);
			if (theReplace == null) { // No replacement -> skip, will be deleted in 2nd run
				return slideIdx + 1;
			}

			// Find the slide to be replaced
			XSLFSlideLayout layout = aSlide.getSlideLayout();

			for (OfficeExportValueHolder theHolder : theReplace.getOfficeExportValueHolders()) {
				XMLSlideShow theShow = null;
				BinaryData template = theHolder.templateData;
				if (template != null) {
					try (InputStream stream = template.getStream()) {
						theShow = new XMLSlideShow(stream);
					}
				}
				else {
					String theFilename = theReplace.getTemplateFilename(theHolder);
					if (theFilename != null) {
						try (InputStream stream = getTemplateFileInputStream(theFilename)) {
							theShow = new XMLSlideShow(stream);
						}
					}
				}

				if (theShow != null) {
					// Make a full replacement except for slide count
					Map<String, Object> innerReplacements = (Map<String, Object>) theHolder.exportData;
					// 1st run
					powerpoint.processSlideShow(theShow, anUtil, innerReplacements, false);

					// 2nd run for removal of empty ADDSLIDES
					innerReplacements.clear();
					powerpoint.processSlideShow(theShow, anUtil, innerReplacements, true);

					// Insert the slide into outer presentation
					boolean useTemplateLayout = theHolder.useTemplateLayout;
					int theSlideNo = theShow.getSlides().size();
					for (int theSlideIdx = 0; theSlideIdx < theSlideNo; theSlideIdx++) {
						XSLFSlide theSlide = theShow.getSlides().get(theSlideIdx);
						XSLFSlide createSlide;
						try {
							if (useTemplateLayout) {
								XSLFSlideLayout slideLayout = theSlide.getSlideLayout();
								XSLFSlideMaster slideMaster = slideLayout.getSlideMaster();
								String masterName = slideMaster.getXmlObject().getCSld().getName();
								// search for slide layout in the same master
								XSLFSlideLayout currentLayout = getSlideLayoutForNewSlide(slideShow, masterName, slideLayout.getName());
								if (currentLayout == null) {
									// search for slide layout in any master
									currentLayout = getSlideLayoutForNewSlide(slideShow, null, slideLayout.getName());
								}
								if (currentLayout == null) {
									// use fallback slide layout
									currentLayout = layout;
								}
								createSlide = slideShow.createSlide(currentLayout);
							}
							else {
								createSlide = slideShow.createSlide(layout);
							}
						}
						catch (IllegalArgumentException e) {
							// layout from ADDSLIDES_* slide not found, try by name 
							XSLFSlideLayout slideLayout = getSlideLayoutForNewSlide(slideShow, null, layout.getName());

							if (slideLayout == null) {
								// layout named like the ADDSLIDES_* slide not found, try by DEFAULT_SLIDE_LAYOUT_NAME
								slideLayout = getSlideLayoutForNewSlide(slideShow, null, DEFAULT_SLIDE_LAYOUT_NAME);

								if (slideLayout == null) {
									// no layout with DEFAULT_SLIDE_LAYOUT_NAME found, using any layout
									slideLayout = getSlideLayoutForNewSlide(slideShow, null, null);
								}
							}
							if (slideLayout == null) {
								throw new RuntimeException("No slide master layout found to create new slide.");
							}
							else {
								createSlide = slideShow.createSlide(slideLayout);
							}
						}
						createSlide.importContent(theSlide);
						slideIdx++;
						slideShow.setSlideOrder(createSlide, slideIdx);
					}
				}
			}
		}

		return slideIdx + 1;
	}

	/**
	 * Searches the given {@link XSLFSlide} for tokens starting with
	 * {@link POIPowerpointXUtil#PREFIX_ADDSLIDES} and returns the first occurrence.
	 */
	private String getAddSlidesToken(XSLFSlide aSlide) {
		for (XSLFShape theShape : POIPowerpointUtil.shapes(aSlide)) {
			if (theShape instanceof XSLFTextShape) {
				XSLFTextShape shape = (XSLFTextShape) theShape;
				String shapeText = shape.getText();
				List<String> tokens = findAddSlidesTokens(shapeText);
				if (!tokens.isEmpty()) {
					for (int i = 0; i < tokens.size(); i++) {
						String token = tokens.get(i);
						if (token.startsWith(POIPowerpointXUtil.PREFIX_ADDSLIDES)) {
							return token;
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Searches the given {@link String} for tokens starting with
	 * {@link POIPowerpointXUtil#PREFIX_ADDSLIDES} and returns a {@link List} of found tokens.
	 * 
	 * @param aSearchText
	 *        a String to parse.
	 * @return the tokens that start with {@link POIPowerpointXUtil#PREFIX_ADDSLIDES} found in the
	 *         given search text.
	 */
	private List<String> findAddSlidesTokens(String aSearchText) {
		if (aSearchText == null) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<>();
		String toTest = aSearchText;
		while (toTest.length() > 9) {
			boolean startsWithPrefix = false;
			if (toTest.startsWith(POIPowerpointXUtil.PREFIX_ADDSLIDES)) {
				startsWithPrefix = true;
			}

			if (startsWithPrefix) {
				toTest = POIPowerpointXUtil.extractToken(result, toTest);
			} else {
				toTest = toTest.substring(1);
			}
		}
		return result;
	}

	private XSLFSlideLayout getSlideLayoutForNewSlide(XMLSlideShow show, String masterName, String layoutName) {
		List<XSLFSlideMaster> slideMasters = show.getSlideMasters();
		if (masterName == null) {
			for (XSLFSlideMaster master : slideMasters) {
				XSLFSlideLayout[] slideLayouts = master.getSlideLayouts();
				if (layoutName == null) {
					if (slideLayouts.length > 0) return slideLayouts[0];
				}
				else {
					XSLFSlideLayout slideLayout = getSlideLayoutByName(slideLayouts, layoutName);
					if (slideLayout != null) return slideLayout;
				}
			}
			return null;
		}
		else {
			XSLFSlideMaster slideMaster = getSlideMasterByName(slideMasters, masterName);
			if (slideMaster == null) return null;
			XSLFSlideLayout[] slideLayouts = slideMaster.getSlideLayouts();
			if (layoutName == null) {
				return slideLayouts.length > 0 ? slideLayouts[0] : null;
			}
			else {
				return getSlideLayoutByName(slideLayouts, layoutName);
			}
		}
	}

	private XSLFSlideMaster getSlideMasterByName(List<XSLFSlideMaster> slideMasters, String masterName) {
		for (XSLFSlideMaster slideMaster : slideMasters) {
			if (masterName.equals(slideMaster.getXmlObject().getCSld().getName())) {
				return slideMaster;
			}
		}
		return null;
	}

	private XSLFSlideLayout getSlideLayoutByName(XSLFSlideLayout[] slideLayouts, String layoutName) {
		for (XSLFSlideLayout slideLayout : slideLayouts) {
			if (layoutName.equals(slideLayout.getName())) {
				return slideLayout;
			}
		}
		return null;
	}



	/**
	 * Create an input stream to read the template from
	 * 
	 * @param aFilename
	 *        the (relative) file name
	 * @return the input stream
	 * @throws IOException
	 *         if getting the stream fails
	 */
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

}
