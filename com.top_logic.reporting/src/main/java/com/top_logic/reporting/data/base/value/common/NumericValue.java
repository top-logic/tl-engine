/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.base.value.common;

import com.top_logic.reporting.data.base.value.Value;

/**
 * A special Value containing numeric entries.
 * 
 * Additional to the functions of a basic value, a Numeric value
 * can be set to zero (which initializes all entries with zero)
 * and a NumericValue knows how to add other NumericValues to itself.
 * 
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public interface NumericValue extends Value {

    /**
     * Adds another NumericValue to this one.
     * 
     * @param    anOther    The other NumericValue to be added to this.
     * @return   The result of the operation.
     */
    public NumericValue plus(NumericValue anOther);
    


    /**
     * Subtract another NumericValue from this one.
     * 
     * @param    anOther    The other NumericValue to be substracted from this.
     * @return   The result of the operation.
     */
    public NumericValue minus(NumericValue anOther);

    /**
     * Multiply another NumericValue with this one.
     * 
     * @param    anOther    The other NumericValue to be multiplied with this.
     * @return   The result of the operation.
     */
    public NumericValue multiply(NumericValue anOther);

    /**
     * Divide another NumericValue from this one.
     * 
     * @param    anOther    The other NumericValue to be devided from this.
     * @return   The result of the operation.
     */
    public NumericValue devide(NumericValue anOther);

    /**
     * Find the maximum values between another NumericValue and this one.
     * 
     * @param    anOther    The other NumericValue to find the maximum.
     * @return   The result of the operation.
     */
    public NumericValue max(NumericValue anOther);

    /**
     * Find the minimum values between another NumericValue and this one.
     * 
     * If one of the values is <code>null</code>, the value in the result
     * will be the neutral element on that place.
     * 
     * @param    anOther    The other NumericValue to find the minimum.
     * @return   The result of the operation.
     */
    public NumericValue min(NumericValue anOther);
}
