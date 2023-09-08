/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport.interfaces;

import com.top_logic.element.genericimport.GenericDataImportConfiguration;

/**
 * The GenericDataImportConfigurationAware knows about a {@link GenericDataImportConfiguration}
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface GenericDataImportConfigurationAware {
    public boolean setImportConfiguration(GenericDataImportConfiguration aConfig, String anInternalType);
    public GenericDataImportConfiguration getImportConfiguration();
}

