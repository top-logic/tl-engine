/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.base;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Business logic plugin for the importer framework.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface ObjectProvider {

    /** 
     * Return the objects already known by the system.
     * 
     * <p>This will be used to look up the attributes during performing in {@link AbstractImportPerformer}.</p>
     * 
     * @param    aModel    The model we are working on.
     * @return   Requested collection of known attributed objects which might to be imported.
     */
    Collection<? extends Wrapper> getObjects(Wrapper aModel);

    /** 
     * Check, if the given model can be used for an import in {@link AbstractImportPerformer}.
     * 
     * @param    aModel    The model we are working on.
     * @return   <code>true</code> when model is supported (and {@link #getObjects(Wrapper)} can
     *           handle it for getting the objects already known by the system.
     */
    boolean supportsModel(Wrapper aModel);

    /**
     * Simple version of an {@link ObjectProvider} which knows no objects and accepts all models. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static final class SimpleObjectProvider implements ObjectProvider {

        @Override
        public Collection<? extends Wrapper> getObjects(Wrapper aModel) {
            return Collections.emptyList();
        }

        @Override
        public boolean supportsModel(Wrapper aModel) {
            return true;
        }
    }
}
