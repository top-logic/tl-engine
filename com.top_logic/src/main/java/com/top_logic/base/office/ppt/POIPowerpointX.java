/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;

import com.top_logic.base.office.OfficeException;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.tool.export.AbstractOfficeExportHandler.OfficeExportValueHolder;

/**
 * POI version of Powerpoint working with pptx format only
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class POIPowerpointX extends Powerpoint {

	/**
	 * {@link Powerpoint.PowerpointFactory} creating {@link POIPowerpointX} instances.
	 */
	public static class Factory implements PowerpointFactory {
		@Override
		public Powerpoint newInstance() {
			return new POIPowerpointX();
		}
	}

	private static final String PREFIX_HYPERLINK = "HYPERLINK_";

	private final SlideReplacerX slideReplacer;

	/**
	 * Creates a {@link POIPowerpointX}.
	 * 
	 * @see Powerpoint#getInstance(boolean)
	 */
	protected POIPowerpointX() {
	    super();
		this.slideReplacer = this.createReplacer();
    }
	
	@Override
	public String getExtension() {
		return PPTX_EXT;
	}

	/**
	 * a new SlideReplacerX
	 */
	protected SlideReplacerX createReplacer() {
		return new SlideReplacerX(this);
	}

	@Override
	public File convert(String anIn, String anOut) {
		throw new UnsupportedOperationException("POI does not depend of the installed Powerpoint.");
	}

	@Override
	protected void closeDocument(Object doc, Stack someRefs) throws Exception {
		// Not needed for POI ?
	}

	@Override
	protected Object createApplication(Stack someRefs) throws Exception {
		// Not needed for POI ?
		return null;
	}

	@Override
	protected Object getDocument(BinaryData name, Object anApplication, Stack someRefs) throws Exception {
		// Not needed for POI ?
		return null;
	}

	@Override
	protected Map getFields(Object doc, Stack someRefs) throws Exception {
		// Not needed for POI ?
		return null;
	}

	@Override
	protected Map getValuesFromDoc(Object doc, Mapping mapping, Stack someRefs) throws Exception {
		// Not needed for POI ?
		throw new UnsupportedOperationException("getValuesFromDoc not implemented for POIPowerpointX");
	}

	@Override
	protected String getVersion(Stack someRefs) throws Exception {
		throw new UnsupportedOperationException("POI does not depend of the installed Powerpoint.");
	}

	@Override
	protected void releaseStack(Stack someRefs) {
		// do nothing because POIAccess does not need any office references 
	}

	@Override
	protected void save(Object anAppl, Object doc, String name, Stack someRefs) throws Exception {
		// Not needed for POI ?
	}

	@Override
	protected boolean setResult(Object anAppl, Object doc, Object anObject, String key, Object value, Stack someRefs) throws Exception {
		// Not needed for POI ?
		return false;
	}

	@Override
	public boolean setValues(InputStream aTemplate, File aDestFile, Map aValueMap) throws OfficeException {
		setValuesDirect(aTemplate, aDestFile, aValueMap);
		return true;
	}

	/**
	 * Set the values in a template replacing tokens there
	 * 
	 * @param templateStream
	 *        the template as a stream
	 * @param tmpFile
	 *        the file to write the result
	 * @param theValues
	 *        the token name - value map
	 */
	public void setValuesDirect(InputStream templateStream, File tmpFile, Map<String, Object> theValues) {
		try {
			// we have to extract hyperlinks from the values map because HyperlinkDefinitions in
			// SlideReplacements will lead to a broken powerpoint file if they are replaced before the slide
			// replacement takes place.
			Map<String, Object> hyperlinks = extractHyperlinks(theValues, 0);
			
			POIPowerpointXUtil theUtil = new POIPowerpointXUtil();
			XMLSlideShow theTemplate = new XMLSlideShow(templateStream);
			this.processSlideShow(theTemplate, theUtil, theValues, false);

			// Now after the slide replacement is done we can replace the hyperlink tokens with the actual
			// HyperlinkDefinitions.
			this.processSlideShow(theTemplate, theUtil, hyperlinks, false);
			
			// 2nd run for removal of empty ADDSLIDES
			theValues.clear();
			this.processSlideShow(theTemplate, theUtil, theValues, true);

			// final run for slide count
			List<XSLFSlide> slides = theTemplate.getSlides();
			theValues.put(POIPowerpointXUtil.SLIDE_COUNT, Integer.valueOf(slides.size()));
			this.processSlideShow(theTemplate, theUtil, theValues, true);
	        
			try (FileOutputStream fo = new FileOutputStream(tmpFile)) {
				theTemplate.write(fo);
			}
        }
        catch (IOException e) {
	        Logger.error("Error setting values.", e, this.getClass());
        }
    }

	/**
	 * Extracts all {@link HyperlinkDefinition HyperlinkDefinitions} from the given value map and
	 * replaces them with a new custom token value which can be replaced with the concrete
	 * {@link HyperlinkDefinition} later on.
	 */
	private Map<String, Object> extractHyperlinks(Map<String, Object> theValues, int counter) {
		Map<String, Object> hyperlinks = new HashMap<>();
		List<String> tokensToReplace = new ArrayList<>();

		for (String token : theValues.keySet()) {
			if (token.contains(POIPowerpointXUtil.PREFIX_VALUE)) {
				Object replacement = theValues.get(token);
				if (replacement instanceof HyperlinkDefinition) {
					tokensToReplace.add(token);
				}
			} else if (token.contains(POIPowerpointXUtil.PREFIX_FIXEDTABLE)) {
				List<Object> tableAsList = POIPowerpointXUtil.getTableAsList(theValues, token);
				if (CollectionUtil.isEmptyOrNull(tableAsList)) {
					continue;
				}
				Object firstObject = tableAsList.get(0);
				if (firstObject instanceof Object[]) {
					int tableCols = ((Object[]) firstObject).length;

					for (Object row : tableAsList) {
						if (row instanceof Object[]) {
							for (int col = 0; col < tableCols; col++) {
								if(((Object[]) row)[col] instanceof StyledValue) {
									Object value = ((StyledValue)((Object[]) row)[col]).getValue();
									if(value instanceof HyperlinkDefinition) {
										// store hyperlink definition with custom placeholder in hyperlink-map
										hyperlinks.put(POIPowerpointXUtil.PREFIX_VALUE + PREFIX_HYPERLINK + counter, value);
										// replace hyperlink definition with custom placeholder in value-map
										((StyledValue)((Object[]) row)[col]).setValue(POIPowerpointXUtil.PREFIX_VALUE + PREFIX_HYPERLINK + counter);
										counter++;
									}
								}
								else if (((Object[]) row)[col] instanceof HyperlinkDefinition) {
									// store hyperlink definition with custom placeholder in hyperlink-map
									hyperlinks.put(POIPowerpointXUtil.PREFIX_VALUE + PREFIX_HYPERLINK + counter, ((Object[]) row)[col]);
									// replace hyperlink definition with custom placeholder in value-map
									((Object[]) row)[col] = POIPowerpointXUtil.PREFIX_VALUE + PREFIX_HYPERLINK + counter;
									counter++;
								}
							}
						}
					}
				}
			} else if (token.contains(POIPowerpointXUtil.PREFIX_ADDSLIDES)) {
				Object replacement = theValues.get(token);
				if(replacement instanceof SlideReplacement) {
					for(OfficeExportValueHolder valueHolder : ((SlideReplacement) replacement).getOfficeExportValueHolders()) {
						if(valueHolder.exportData instanceof Map) {
							Map extractedHyperlinks = extractHyperlinks((Map) valueHolder.exportData, counter);
							hyperlinks.putAll(extractedHyperlinks);
							counter += extractedHyperlinks.size();
						}
					}
				}
			}
		}

		for (String token : tokensToReplace) {
			// store hyperlink definition with custom placeholder in hyperlink-map
			hyperlinks.put(POIPowerpointXUtil.PREFIX_VALUE + PREFIX_HYPERLINK + counter, theValues.remove(token));
			// replace hyperlink definition with custom placeholder in value-map
			theValues.put(token, POIPowerpointXUtil.PREFIX_VALUE + PREFIX_HYPERLINK + counter);
			counter++;
		}

		return hyperlinks;
	}

	/**
	 * Process a slide show to replace the tokens
	 * 
	 * @param aShow
	 *        the slide show
	 * @param aUtil
	 *        the powerpoint util to be used
	 * @param someReplacements
	 *        the token name - value map
	 * @param scdRun
	 *        if true this is the second run (only slide count and empty ADDSLIDES are replaced)
	 * @throws IOException
	 *         if an io operation with the slide shows or embedded pictures throws one
	 */
	protected void processSlideShow(XMLSlideShow aShow, POIPowerpointXUtil aUtil, Map<String, Object> someReplacements,
			boolean scdRun)
			throws IOException {
		this.preProcessSlideshow(aShow, aUtil, someReplacements, scdRun);

		List<XSLFSlideMaster> theMasters = aShow.getSlideMasters();
		for (int i = 0; i < theMasters.size(); i++) {
			this.processSlideMaster(theMasters.get(i), aUtil, someReplacements);
        }
        
		List<XSLFSlide> slides = aShow.getSlides();
		if (!scdRun) {
			for (int i = 0; i < slides.size(); i++) {
				this.processSlide(slides.get(i), i, aUtil, someReplacements);
			}
		}
        
		this.postProcessSlideshow(aShow, aUtil, someReplacements, scdRun);
	}
	
	/**
	 * Process a slide master to replace the tokens
	 * 
	 * @param aMaster
	 *        the slide master
	 * @param aUtil
	 *        the powerpoint util
	 * @param someReplacements
	 *        the token name - value map
	 * @throws IOException
	 *         if an io operation with the slide shows or embedded pictures throws one
	 */
	protected void processSlideMaster(XSLFSlideMaster aMaster, POIPowerpointXUtil aUtil,
			Map<String, Object> someReplacements)
			throws IOException {
		aUtil.parseForTokens(aMaster, -1, POIPowerpointUtil.shapesStable(aMaster), someReplacements, null);
	}
	
	/**
	 * Process a slide to replace the tokens
	 * 
	 * @param aSlide
	 *        the slide
	 * @param slideIdx
	 *        the index of the slide in the show
	 * @param aUtil
	 *        the powerpoint util
	 * @param someReplacements
	 *        the token name - value map
	 * @throws IOException
	 *         if an io operation with the slide shows or embedded pictures throws one
	 */
	protected void processSlide(XSLFSlide aSlide, int slideIdx, POIPowerpointXUtil aUtil,
			Map<String, Object> someReplacements)
			throws IOException {
		aUtil.parseForTokens(aSlide, slideIdx, POIPowerpointUtil.shapesStable(aSlide), someReplacements, null);
	}

	/**
	 * Pre-processing hook.
	 * 
	 * @param aShow
	 *        the slide show
	 * @param aUtil
	 *        the powerpoint util
	 * @param someReplacements
	 *        the token name - value map
	 * @param scdRun
	 *        flag indicating whether this is the second run of
	 * @throws IOException
	 *         if an io operation with the slide shows or embedded pictures throws one
	 */
	protected void preProcessSlideshow(XMLSlideShow aShow, POIPowerpointXUtil aUtil,
			Map<String, Object> someReplacements, boolean scdRun) throws IOException {
		// Hook for subclasses
	}

	/**
	 * Post process the slide show to do further (e.g. slide) replacements
	 * 
	 * @param aShow
	 *        the slide show
	 * @param aUtil
	 *        the powerpoint util
	 * @param someReplacements
	 *        the token name - value map
	 * @param scdRun
	 *        if true this is the second run (only slide count and empty ADDSLIDES are replaced)
	 * @throws IOException
	 *         if an io operation with the slide shows or embedded pictures throws one
	 */
	protected void postProcessSlideshow(XMLSlideShow aShow, POIPowerpointXUtil aUtil,
			Map<String, Object> someReplacements, boolean scdRun)
			throws IOException {
		int slideIdx = 0;
		while (slideIdx >= 0) {
			List<XSLFSlide> slides = aShow.getSlides();
			if (slideIdx < slides.size()) {
				slideIdx =
					this.slideReplacer.replaceSlide(slides.get(slideIdx), slideIdx, aUtil, someReplacements,
						scdRun);
			} else {
				slideIdx = -1;
			}
		}
	}

}
