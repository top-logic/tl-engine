/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.usermodel.MasterSheet;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;

import com.top_logic.base.office.OfficeException;
import com.top_logic.base.office.ppt.POISlideShow.StandardSlideShow;
import com.top_logic.base.office.ppt.POISlideShow.XMLBasedSlideShow;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class POIPowerpoint extends Powerpoint {
	
	/**
	 * {@link Powerpoint.PowerpointFactory} creating {@link POIPowerpoint} instances.
	 */
	public static class Factory implements PowerpointFactory {
		@Override
		public Powerpoint newInstance() {
			return new POIPowerpoint();
		}
	}

	/**
	 * Keys in the Template starting with this key (e.g ...) will be replaces by
	 */
	public static final String ADD_SLIDES = "ADDSLIDES_";

	private final SlideReplacer slideReplacer;

	/**
	 * Creates a {@link POIPowerpoint}.
	 * 
	 * @see Powerpoint#getInstance(boolean)
	 */
	protected POIPowerpoint() {
	    super();
		this.slideReplacer = new SlideReplacer(this);
    }
	
	@Override
	public File convert(String anIn, String anOut) {
		throw new UnsupportedOperationException("POI does not depend of the installed Powerpoint.");
	}

	@Override
	protected void closeDocument(Object doc, Stack someRefs) throws Exception {
		((POISlideShow<?, ?>) doc).close();
	}

	@Override
	protected Object createApplication(Stack someRefs) throws Exception {
		// Not needed for POI.
		return null;
	}

	@Override
	protected Object getDocument(BinaryData in, Object anApplication, Stack someRefs) throws Exception {
		if (in.getName().endsWith(PPT_EXT)) {
			return new StandardSlideShow(in);
		} else {
			return new XMLBasedSlideShow(in);
		}
	}

	@Override
	protected Map<String, List<? /* Shapes */>> getFields(Object doc, Stack someRefs) throws Exception {
		return ((POISlideShow<?, ?>) doc).getFields();
	}

	@Override
	protected Map<String, Object> getValuesFromDoc(Object doc, Mapping mapping, Stack someRefs) throws Exception {
		return ((POISlideShow<?, ?>) doc).getValuesFromDoc();
	}

	@Override
	protected String getVersion(Stack someRefs) throws Exception {
		throw new UnsupportedOperationException("POI does not depend of the installed Powerpoint.");
	}

	@Override
	protected void releaseStack(Stack someRefs) {
		// do nothing because POIAccess does not need any office references.
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

	public void setValuesDirect(InputStream templateStream, File tmpFile, Map theValues) {
		try {
			POIPowerpointUtil theUtil = new POIPowerpointUtil();
	        SlideShow     theTemplate = this.createSlideShow(templateStream);
	        
	        this.processSlideShow(theTemplate, theUtil, theValues);
	        
			List slides = theTemplate.getSlides();
	        theValues.clear();
			theValues.put(POIPowerpointUtil.SLIDE_COUNT, Integer.valueOf(slides.size()));
	        
	        this.processSlideShow(theTemplate, theUtil, theValues);
	        
	        FileOutputStream fo = new FileOutputStream(tmpFile);
			try {
				theTemplate.write(fo);
			} finally {
				fo.close();
			}
        }
        catch (IOException e) {
	        Logger.error("Error setting values.", e, this.getClass());
        }
    }
	
	protected void processSlideShow(SlideShow<?, ?> aShow, POIPowerpointUtil aUtil, Map someReplacements)
			throws IOException {
		List<? extends MasterSheet<?, ?>> theMasters = aShow.getSlideMasters();
		for (int i = 0; i < theMasters.size(); i++) {
			this.processSlideMaster(theMasters.get(i), aUtil, someReplacements);
        }
        
		List<? extends Slide<?, ?>> slides = aShow.getSlides();
		for (int i = 0; i < slides.size(); i++) {
			this.processSlide(slides.get(i), aUtil, someReplacements);
        }
        
        this.postProcessSlideshow(aShow, aUtil, someReplacements);
	}
	
	protected SlideShow createSlideShow(InputStream aTemplateStream) throws IOException {
		return new HSLFSlideShow(aTemplateStream);
	}
	
	protected void processSlideMaster(MasterSheet<?, ?> aMaster, POIPowerpointUtil aUtil, Map someReplacements)
			throws IOException {
	    aUtil.parseForTokens(aMaster, POIPowerpointUtil.shapesStable(aMaster), someReplacements);
	}
	
	protected void processSlide(Slide aSlide, POIPowerpointUtil aUtil, Map someReplacements) throws IOException {
	    aUtil.parseForTokens(aSlide, POIPowerpointUtil.shapesStable(aSlide), someReplacements);
	}
	
	protected void postProcessSlideshow(SlideShow aShow, POIPowerpointUtil aUtil, Map someReplacements) throws IOException {
		this.slideReplacer.replaceSlides(aShow, aUtil, someReplacements);
	}
	
}
