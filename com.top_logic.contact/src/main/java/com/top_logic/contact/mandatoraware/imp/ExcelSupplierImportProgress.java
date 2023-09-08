/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.progress.AJAXProgressComponent;

/**
 * Show the Progress of the ExcelSupplierImport.
 * 
 * This is the GUI view into the {@link ExcelSupplier20060529Importer}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class ExcelSupplierImportProgress extends AJAXProgressComponent {

	public interface Config extends AJAXProgressComponent.Config {
		@Name(XML_ATTRIBUTE_DELTA_IMPORT)
		@BooleanDefault(true)
		boolean getDeltaImport();

		@Name(XML_ATTRIBUTE_SUPPLIER_IMPORT)
		@BooleanDefault(true)
		boolean getSupplierImport();
	}

	/** XML layout constants. */
    private static final String XML_ATTRIBUTE_DELTA_IMPORT    = "deltaImport";
	private static final String XML_ATTRIBUTE_SUPPLIER_IMPORT = "supplierImport";
	
	/** The importer is our model */
	protected transient ExcelSupplier20060529Importer model;
	/** If true interpret as supplier - as client otherwise */
	protected transient boolean isSupplierImport;
	/** If true add contacts - also delete unused contacts otherwise */
    protected transient boolean isDeltaImport;
    
    /** 
     * Create a new SAPVolumeProgressComponent from XML.
     */
    public ExcelSupplierImportProgress(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
        
        this.isDeltaImport    = atts.getDeltaImport();
        this.isSupplierImport = atts.getSupplierImport();
    }

}
