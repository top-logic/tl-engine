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
 * This Operator can subtract an array of NumericTuples.
 * 
 * The result is another NumericTuple.
 * 
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class NumericValueDiffOperator extends AbstractNumericValueOperator {

    /**
     * Constructor for NumericValueDiffOperator.
     * 
     * @param    aName    The display name of this operator.
     */
    public NumericValueDiffOperator(String aName) {
        super(aName);
    }

    /**
     * @see com.top_logic.reporting.data.processing.operator.AbstractNumericValueOperator#getNeutralNumber()
     */
    @Override
	protected Number getNeutralNumber() {
        return (Double.valueOf(0));
    }

    /**
     * @see com.top_logic.reporting.data.processing.operator.AbstractNumericValueOperator#process(Value, Value)
     */
    @Override
	protected Value process(Value aSource, Value aDest) {
        NumericValue result = ((NumericValue) aSource).minus((NumericValue) aDest);
        return (result);

    }
}
