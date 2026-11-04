/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.genericimport;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.genericimport.GenericDataImportConfiguration;
import com.top_logic.element.genericimport.GenericDataImportHelper;
import com.top_logic.element.genericimport.interfaces.GenericDataImportConfigurationAware;
import com.top_logic.element.genericimport.interfaces.GenericImporter;
import com.top_logic.element.genericimport.interfaces.GenericImporter.GenericFileImporter;
import com.top_logic.element.genericimport.interfaces.GenericTypeResolver;
import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.boundsec.assistent.BoundAssistentComponent;
import com.top_logic.util.error.TopLogicException;

/**
 * Assistant leading the user through a generic data import defined by an import configuration
 * file.
 *
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class GenericDataImportAssistentComponent extends BoundAssistentComponent implements GenericDataImportConfigurationAware {

    /**
     * Configuration options for {@link GenericDataImportAssistentComponent}.
     */
    public interface Config extends BoundAssistentComponent.Config {

		/**
		 * The file with the definition of the import.
		 */
		@Name(FILE_NAME)
		@Mandatory
		String getImportConfigFile();
	}

	/**
	 * Configuration name of {@link Config#getImportConfigFile()}.
	 */
	public static final String FILE_NAME        = "importConfigFile";

    private String filename;
    
    /** 
     * Creates a {@link GenericDataImportProgressComponent}.
     */
    public GenericDataImportAssistentComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
        this.filename            = atts.getImportConfigFile();
    }

	@Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(initialModel());
		}
		return super.validateModel(context);
	}

	private Object initialModel() {
		GenericDataImportHelper initialModel = null;
		try {
			GenericDataImportConfiguration theConfig = GenericDataImportConfiguration.readConfiguration(this.filename);
			if (this.checkImportConfiguration(theConfig)) {
				initialModel = new GenericDataImportHelper(theConfig);
            }
		} catch (Exception e) {
			Logger.error("Failed to create configuration for " + this.filename, e, this);
        }
		return initialModel;
    }

    private GenericDataImportHelper getImportHelper() {
        return (GenericDataImportHelper) this.getModel();
    }
    
    @Override
	public GenericDataImportConfiguration getImportConfiguration() {
        return getImportHelper().getImportConfiguration();
    }
    
    @Override
	public boolean setImportConfiguration(GenericDataImportConfiguration aConfig, String anInternalType) {
        try {
            if (this.checkImportConfiguration(aConfig)) {
                return getImportHelper().setImportConfiguration(aConfig, null);
            }
        } catch (Exception ex) {
            Logger.error("invalid configuration", this);
            throw new TopLogicException(this.getClass(), "genericImport.setupAssistent.failed",ex);
        }
        return false;
    }

    /**
     * Checks whether the given import configuration can be used by this assistant.
     *
     * @param aConfig
     *        The configuration to check.
     * @return Whether the configuration is accepted.
     * @throws Exception
     *         If the configured importer cannot read uploaded files.
     */
    public boolean checkImportConfiguration(GenericDataImportConfiguration aConfig) throws Exception {
        GenericImporter theImporter = aConfig.getImporter();
        if (! (theImporter instanceof GenericFileImporter)) {
            throw new IllegalArgumentException("The importer must implement interface GenericFileImporter");
        }
        
        GenericTypeResolver theResolver = aConfig.getTypeResolver();
//        if (! (theResolver instanceof MetaElementBasedTypeResolver)) {
//        }
        return true;
    }
    
}

