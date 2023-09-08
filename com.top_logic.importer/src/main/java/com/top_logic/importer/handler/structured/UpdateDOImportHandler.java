/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.handler.structured;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.importer.base.ImportValueProvider;
import com.top_logic.importer.base.StructuredDataImportPerformer.StructureImportResult;

/**
 * Update an attributed object with values from the imported data object.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class UpdateDOImportHandler<C extends AbstractAttributedDOImportHandler.Config, O extends Wrapper> extends AbstractAttributedDOImportHandler<C, O> {

    /** 
     * Creates a {@link UpdateDOImportHandler}.
     */
    public UpdateDOImportHandler(InstantiationContext aContext, C aConfig) {
        super(aContext, aConfig);
    }

    @Override
    public ResKey execute(Map<String, Object> someObjects, ImportValueProvider valueProvider, StructureImportResult aResult) {
        O      theChild = this.getCurrentObject(someObjects);
        ResKey theKey   = this.excecuteUpdate(theChild, valueProvider, aResult, false);

		if (theKey != null) {
			this.postProcess(theChild, valueProvider, theChild, false);
		}

        return theKey;
    }
}

