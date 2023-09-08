/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.aggregation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The NeedsAttribute Annotation must be specified for {@link AggregationFunction}s to declare
 * whether the attribute is mandatory or not.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @ interface NeedsAttribute {
	 // needs an attribute by default 
    boolean value() default true;
}
