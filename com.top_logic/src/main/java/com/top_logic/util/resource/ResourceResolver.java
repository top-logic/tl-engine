/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Takes a resource name and resolves the properties.
 *
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ResourceResolver {

    /** The single instance of the resolver. */
    protected static ResourceResolver resolver;

    /**
     * Constructor
     */
    protected ResourceResolver() { 
        // singleton usage only.
    }

    /**
     * Get the properties for a given resource name.
     * 
     * @param aResourceName   the name of the resource
     * @return the Properties for the resource name. 
     * @throws IOException if the resource could not be opened.
     */
    public Properties getProperties(String aResourceName) 
        throws IOException {
        // for the moment properties are hosted in properties fiels.
        // the properties name is assumed to be the fully qualified 
        // file name.
        Properties  theResult        = new Properties();
        File        thePropertieFile = new File(aResourceName);
        InputStream theInput         = new FileInputStream(thePropertieFile);
        theResult.load(theInput);
        return theResult;
    }

    /**
     * Get an Instance of the ResourceResolver.
     * 
     * @return an instance of hte resource resolver.
     */
    public static ResourceResolver getInstance() {
        if (resolver == null) {
            resolver = new ResourceResolver();
        }
        return resolver;
    }

}
