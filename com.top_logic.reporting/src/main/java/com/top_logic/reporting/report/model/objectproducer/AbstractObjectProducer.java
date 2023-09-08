/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.objectproducer;

import java.util.Collection;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;

/**
 * @author    <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@Deprecated
public abstract class AbstractObjectProducer implements ObjectProducer {

    private ObjectProducerConfiguration config;
    
    //protected Map options;
    
    public AbstractObjectProducer(InstantiationContext aContext, ObjectProducerConfiguration aConfig) {
        this.config = aConfig;
    }
    
    protected abstract Collection _getObjectsInternal();
    
    @Override
	public Collection getObjects() {
        if (StringServices.isEmpty(this.getObjectType())) {
            throw new IllegalStateException("The object type must not be null!");
        }
        
        return _getObjectsInternal();
    }

    public final String getObjectType() {
        return this.config.getObjectType();
    }
    
    protected final ObjectProducerConfiguration getConfiguration() {
        return this.config;
    }
}
