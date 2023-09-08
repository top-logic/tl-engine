/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.Properties;

import com.top_logic.basic.Logger;
import com.top_logic.element.genericimport.interfaces.GenericDataImportConfigurationAware;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public abstract class AbstractGenericDataImportBase implements GenericDataImportConfigurationAware {

    private GenericDataImportConfiguration config;
    
    /** 
     * Creates a {@link AbstractGenericDataImportBase}.
     * 
     */
    public AbstractGenericDataImportBase(Properties someProps) {
    }

    @Override
	public final GenericDataImportConfiguration getImportConfiguration() {
        return this.config;
    }
    
    @Override
	public boolean setImportConfiguration(GenericDataImportConfiguration aConfig, String anInternalType) {
        try {
            if (this.checkImportConfiguration(aConfig, anInternalType)) {
                this.config = aConfig;
                return true;
            }
        }
        catch (Exception ex) {
            Logger.error("Unable to set import configuration", ex, this);
        }
        return false;
    }
    
    protected boolean checkImportConfiguration(GenericDataImportConfiguration aConfig, String anInternalType) throws Exception {
        return true;
    }
}

