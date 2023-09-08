/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.base.value.common;

/** 
 * Simple implementation of a value of type Number.
 * 
 * This value contains only one number.
 *
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class NumberValue extends NumericTuple {

    /**
     * Constructor for NumberValue.
     */
    public NumberValue(int aValue) {
        super(new int[]{aValue});
    }

    /**
     * Constructor for NumberValue.
     */
    public NumberValue(double aValue) {
        super(new Double[]{aValue});
    }

    /**
     * Constructor for NumberValue.
     */
    public NumberValue(float aValue) {
        super(new float[]{aValue});
    }

    /**
     * Constructor for NumberValue.
     */
    public NumberValue(Number aNumber) {
        super(new Number[]{aNumber});
    }
}
