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
 * Base class for {@link ObjectProducer}s.
 *
 * @author    <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@Deprecated
public abstract class AbstractObjectProducer implements ObjectProducer {

    private ObjectProducerConfiguration config;
    
    //protected Map options;
    
    public AbstractObjectProducer(InstantiationContext aContext, ObjectProducerConfiguration aConfig) {
        this.config = aConfig;
    }
    
    /** Retrieves the business objects. */
    protected abstract Collection _getObjectsInternal();
    
    @Override
	public Collection getObjects() {
        if (StringServices.isEmpty(this.getObjectType())) {
            throw new IllegalStateException("The object type must not be null!");
        }
        
        return _getObjectsInternal();
    }

    /** The name of the type of the produced objects. */
    public final String getObjectType() {
        return this.config.getObjectType();
    }
    
    /** The configuration of this producer. */
    protected final ObjectProducerConfiguration getConfiguration() {
        return this.config;
    }
}
