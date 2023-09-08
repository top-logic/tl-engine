/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.objectproducer;

import java.util.Collection;

import com.top_logic.layout.Accessor;

/**
 * ObjectProducers are used to produce sets of (data)object to report from.
 * The producer is configured in the report xml an will be obtained from the
 * {@link ObjectProducerFactory}.
 * The objects must be accessible through {@link Accessor}s.
 * 
 * @author     <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@Deprecated
public interface ObjectProducer {
    /** 
     * This method produces the data objects. 
     * 
     * @return a collection of objects, never <code>null</code>
     */
    public Collection<Object> getObjects();
}
