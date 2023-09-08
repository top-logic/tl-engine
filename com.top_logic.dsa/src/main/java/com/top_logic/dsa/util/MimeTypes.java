
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.util;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.mime.MimeTypesModule;
import com.top_logic.dsa.DataAccessProxy;

/**
 * Class managing extensions, mime types and images.
 * <p>
 * Mimetypes are defined by 
 * <a href="http://www.mhonarc.org/~ehood/MIME/2046/rfc2046.html"
 * >RFC 2046</a>: The actual 
 * <a href="ftp://ftp.isi.edu/in-notes/iana/assignments/media-types/media-types">
 * MediaTypes</a> can be found here. Common extensions mappings are found
 * in the <code>web.xml</code> file, shared with the webcontainer.
 * The images are found in the <code>top-logic.xml</code> configuration file
 * using the keys: <code>TypeMap</code> and <code>ImageMap</code>.
 * <code>TypeMap</code> Translates the mime-types to readeable strings.
 * <code>ImageMap</code> returns gif-images for mime-types.
 * </p>
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class MimeTypes extends MimeTypesModule {

	/**
	 * Creates a {@link MimeTypes}.
	 */
	public MimeTypes(InstantiationContext context, ServiceConfiguration<?> config) {
		super(context, config);
	}

	/**
	 * Returns the mimetype for the given DataAccessProxy.
	 *
	 * @param aDAP
	 *        The DataAccessProxy to be checked for mime type.
	 * @return The mime type for the DataAccessProxy, null if unknown.
	 */
    public String getMimeType (DataAccessProxy aDAP) {
		String theType = aDAP.getMimeType();
        if (theType == null) {
            String thePath = aDAP.getPath();
            theType = this.getMimeType(thePath);
        }

        return (theType);
    }

    /**
     * Returns the only instance of this class. If this instance doesn't exist, it'll
     * be created automatically.
     *
     * @return    The only instance of this class.
     */
    public static MimeTypes getInstance() {
		return (MimeTypes) Module.INSTANCE.getImplementationInstance();
    }
    
}
