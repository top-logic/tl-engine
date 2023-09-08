/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import com.top_logic.basic.io.CSVReader;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.element.genericimport.interfaces.GenericImporter.GenericFileImporter;
import com.top_logic.element.structured.wrap.Mandator;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class AbstractCSVDOImporter extends DataObjectImportTask implements
        GenericFileImporter {

    
	private BinaryData sourceFile;

    /** 
     * Creates a {@link AbstractCSVDOImporter}.
     */
    public AbstractCSVDOImporter(Properties aProp) {
        super(aProp);
    }

    @Override
	protected void checkFormat(MetaObject aMeta) throws DataObjectException {
    }

    @Override
	protected List getImportObjects(Mandator aMandator) throws DatabaseAccessException,
            DataObjectException {
        try {
            return this.setupReader().readAllLines();
        } catch (IOException ex) {
            throw new DatabaseAccessException(ex);
        } catch (Exception ex) {
            throw new DatabaseAccessException(ex);
        } 
    }

    @Override
	protected void importItem(DataObject aDo, Mandator aMandator) throws Exception {
    }

    @Override
	protected void startImport() {
    }

    @Override
	public String[] getSupportedFileExtensions() {
        return new String[] { "csv" };
    }

    @Override
	public void setImportFile(BinaryData aFile) {
        this.sourceFile = aFile;
    }

    @Override
	public String[] getColumns() {
        return null;
    }

    @Override
	public List getPlainObjects() throws DatabaseAccessException, DataObjectException {
        return null;
    }

    
    protected CSVReader setupReader() throws Exception {
		return new CSVReader(new InputStreamReader(this.sourceFile.getStream()));
    }
}

