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
 * Base class for components of a generic import that keep a reference to their
 * {@link GenericDataImportConfiguration}.
 *
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
    
    /**
     * Checks whether the given import configuration can be used by this component.
     *
     * @param aConfig
     *        The configuration to check.
     * @param anInternalType
     *        The internal type this component is responsible for, may be {@code null}.
     * @return Whether the configuration is accepted.
     * @throws Exception
     *         If checking the configuration fails.
     */
    protected boolean checkImportConfiguration(GenericDataImportConfiguration aConfig, String anInternalType) throws Exception {
        return true;
    }
}

