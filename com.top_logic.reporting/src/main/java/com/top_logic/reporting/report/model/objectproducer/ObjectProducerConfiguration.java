/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.objectproducer;

import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@Deprecated
public interface ObjectProducerConfiguration extends PolymorphicConfiguration<ObjectProducer> {

    String getObjectType();
    void setObjectType(String anObjectType);
}

