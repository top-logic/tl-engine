/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.aggregation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.top_logic.reporting.report.model.partition.function.PartitionFunction;


/**
 * The SupportsType Annotation must be specified for {@link AggregationFunction}s or {@link PartitionFunction}
 * to declare the supported types of MetaElements the function is able to handle 
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface SupportsType {
    // support all types by default 
    int[] value() default {};
}

