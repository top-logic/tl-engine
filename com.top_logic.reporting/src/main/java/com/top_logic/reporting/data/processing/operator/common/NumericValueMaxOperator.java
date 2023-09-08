/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.processing.operator.common;

import com.top_logic.reporting.data.base.value.Value;
import com.top_logic.reporting.data.base.value.common.NumericValue;
import com.top_logic.reporting.data.processing.operator.AbstractNumericValueOperator;

/** 
 * This Operator can find maximum values from an array of NumericValues.
 * 
 * The result is another {@link com.top_logic.reporting.data.base.value.common.NumericValue}.
 * Example:
 * <pre>
 * (10, 200, 231)
 * (20, 100, 222)
 * (30, 300, 213)
 * --------------
 * (30, 300, 231)
 * </pre>
 *
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class NumericValueMaxOperator extends AbstractNumericValueOperator {

    /**
     * Constructor for NumericValueMaxOperator.
     */
    public NumericValueMaxOperator(String aName) {
        super(aName);
    }

    /**
     * @see com.top_logic.reporting.data.processing.operator.AbstractNumericValueOperator#getNeutralNumber()
     */
    @Override
	protected Number getNeutralNumber() {
        return (Double.valueOf(Double.MIN_VALUE));
    }

    /**
     * @see com.top_logic.reporting.data.processing.operator.AbstractNumericValueOperator#process(Value, Value)
     */
    @Override
	protected Value process(Value aSource, Value aDest) {
        NumericValue result = ((NumericValue) aSource).max((NumericValue) aDest);
        return (result);
    }
}
