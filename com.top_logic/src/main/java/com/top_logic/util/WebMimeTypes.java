/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import jakarta.activation.FileTypeMap;
import jakarta.servlet.ServletContext;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.services.ServletContextService;

/**
 * MimeTypes that live in a {@link ServletContext}.
 * 
 * @author    <a href=mailto:klaus.halfmann@top-logic.com>Klaus Halfmann</a>
 */
public class WebMimeTypes extends TLMimeTypes {

    private ServletContext _context;

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link WebMimeTypes}.
	 */
	public WebMimeTypes(InstantiationContext context, ServiceConfiguration<?> config) {
		super(context, config);

		_context = getServletContext();
	}

	private static ServletContext getServletContext() {
		if (ServletContextService.Module.INSTANCE.isActive()) {
			return ServletContextService.getInstance().getServletContext();
		}
		return null;
	}

    /**
     * Returns a Translated file type for the given Mime-Type.
     * 
     * If there is no translation for this mime-type
     * a translated "unknown" will be returned.
     *
     * @param    aType A MimeType
     * @return   A Translated name describing the MimeType.
     */
    @Override
	public String getDescription (String aType) {
    	if (_context == null) {
    		return super.getDescription(aType);
    	}

		String theName = Resources.getInstance().getStringOptional(I18NConstants.MIMETYPE_NAMES.key(aType));

        if (theName == null) {
            theName = Resources.getInstance().getString(I18NConstants.UNKNOWN_TYPE_RES_KEY);

            Logger.info ("Unable to find resource for \"" + aType + "\"!",
                         this);
        }

        return (theName);
    }
    
    @Override
	protected String lookupMimetype(String lowerCaseName) {
		if (_context == null) {
			return super.lookupMimetype(lowerCaseName);
		} else {
			return _context.getMimeType(lowerCaseName);
		}
	}

	@Override
    protected FileTypeMap getMimeTypeMap() {
    	if (_context == null) {
    		return super.getMimeTypeMap();
    	}
    	// is not used.
    	return null;
    }
    
    @Override
    public void shutDown() {
    	_context = null;
    	super.shutDown();
    }


}
