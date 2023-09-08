/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.handler.structured;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.DataObject;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.importer.base.ImportValueProvider;
import com.top_logic.importer.base.StructuredDataImportPerformer.StructureImportResult;
import com.top_logic.importer.xml.XMLFileImportParser;

/**
 * Set the name of the given {@link Wrapper} from the data of the given {@link DataObject}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class GoIntoDOImportHandler<C extends XMLFileImportParser.MappingConfig, O extends Object> extends AbstractDOImportHandler<C, O> {

    /** 
     * Creates a {@link GoIntoDOImportHandler}.
     */
    public GoIntoDOImportHandler(InstantiationContext aContext, C aConfig) {
        super(aContext, aConfig);
    }

    @Override
    public ResKey execute(Map<String, Object> someObjects, ImportValueProvider valueProvider, StructureImportResult aResult) {
        // NOOP
        return null;
    }
}
