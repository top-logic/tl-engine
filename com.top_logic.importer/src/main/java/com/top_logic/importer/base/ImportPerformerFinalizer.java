/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.base;

import com.top_logic.importer.base.AbstractImportPerformer.ImportResult;
import com.top_logic.importer.logger.ImportLogger;

/**
 * Hook when {@link AbstractImportPerformer} has done the import before the commit has been called.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface ImportPerformerFinalizer {

    /** 
     * Called direct before the commit will be executed on the transaction contained in the given {@link ImportResult}.
     * 
     * <p>The performer will place all objects created or updated together with the raw values
     * adapted to them.</p>
     * 
     * @param aResult
     *        Result of the import performer.
     */
    void finalizeImport(ImportResult aResult, ImportLogger aLogger);

    /**
     * Simple implementation of {@link ImportPerformerFinalizer} doing nothing. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class SimpleImportPerformerFinalizer implements ImportPerformerFinalizer {

        @Override
        public void finalizeImport(ImportResult aResult, ImportLogger aLogger) {
            // Nothing in here
        }
    }
}
