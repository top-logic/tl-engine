/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport.interfaces;

import com.top_logic.element.genericimport.GenericDataImportConfiguration;

/**
 * The {@link GenericDataImportConfigurationAware} knows about a
 * {@link GenericDataImportConfiguration}.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface GenericDataImportConfigurationAware {

    /**
     * Sets the import configuration this object works with.
     *
     * @param aConfig
     *        The import configuration.
     * @param anInternalType
     *        The internal type this object is responsible for, {@code null} for a global
     *        import component.
     * @return Whether the configuration was accepted.
     */
    public boolean setImportConfiguration(GenericDataImportConfiguration aConfig, String anInternalType);

    /**
     * The import configuration this object works with.
     */
    public GenericDataImportConfiguration getImportConfiguration();
}

