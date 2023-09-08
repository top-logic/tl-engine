/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.List;
import java.util.Properties;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.element.genericimport.interfaces.GenericImporter;
import com.top_logic.element.genericimport.interfaces.GenericImporter.GenericFileImporter;

/**
 * The GenericImporterProxy caches {@link #getColumns()} and {@link #getPlainObjects()} from an proxied GenericImporter
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class GenericImporterProxy implements GenericFileImporter {

    public static final String PROXY_IMPORTER = "proxyImporter";
    
    private GenericImporter importer;
    private List            objects;
    private String[]        columns;
    
    /** 
     * Creates a {@link GenericImporterProxy}.
     * 
     */
    public GenericImporterProxy(Properties someProps) {
        this.importer = (GenericImporter) GenericDataImportConfiguration.getInstanceOf(someProps, PROXY_IMPORTER, true);
    }
    
    @Override
	public String[] getColumns() {
        if (this.columns == null) {
            this.columns = this.importer.getColumns();
        }
        return this.columns;
    }

    @Override
	public List getPlainObjects() throws DatabaseAccessException, DataObjectException {
        if (this.objects == null) {
            this.objects = this.importer.getPlainObjects();
        }
        return this.objects;
    }

    @Override
	public void setImportFile(BinaryData aFile) {
        if (this.importer instanceof GenericFileImporter) {
            ((GenericFileImporter) this.importer).setImportFile(aFile);
            this.objects = null;
            this.columns = null;
        }
    }
    
    @Override
	public String[] getSupportedFileExtensions() {
        if (this.importer instanceof GenericFileImporter) {
            return ((GenericFileImporter) this.importer).getSupportedFileExtensions();
        }
        return new String[0];
    }
}

