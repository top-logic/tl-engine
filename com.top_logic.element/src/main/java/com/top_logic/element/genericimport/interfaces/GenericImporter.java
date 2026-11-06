/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport.interfaces;

import java.util.List;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dsa.DatabaseAccessException;

/**
 * The {@link GenericImporter} is used to retrieve a list of {@link DataObject}s 
 * from some kind of data source.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface GenericImporter {
    
    /**
     * Return all column names from the source table. These names must be appear as
     * attribute names of the imported objects. 
     */
    public String[] getColumns();
    
    /**
     * Retrieve all {@link DataObject} from a Source 
     */
    public List getPlainObjects() throws DatabaseAccessException, DataObjectException;
    
    
    /**
     * {@link GenericImporter} that reads its data from an uploaded file.
     */
    public static interface GenericFileImporter extends GenericImporter {

        /**
         * Sets the file to import data from.
         *
         * @param aFile
         *        The uploaded file.
         */
		public void setImportFile(BinaryData aFile);

        /**
         * The file name extensions of the files this importer can read.
         */
        public String[] getSupportedFileExtensions();
    }
}

