/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.retrieval.common;

import java.util.Map;
import java.util.Set;

import com.top_logic.reporting.data.base.value.Value;
import com.top_logic.reporting.data.base.value.common.NumericValue;
import com.top_logic.reporting.data.retrieval.Range;
import com.top_logic.reporting.data.retrieval.simple.SimpleValueHolder;

/**
 * Implementation of a value holder specially for 
 * {@link com.top_logic.reporting.data.base.value.common.NumericValue}s.
 * 
 * This class is only able to provide instances of NumberValues!
 *
 *T
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public class NumberValueHolder extends SimpleValueHolder {

    /**
     * Creates a new value holder for number values.
     * 
     * The given map will be inspected and devided into the needed arrays
     * of values, which can be accessed by the normal interface. This
     * method expects the values to be {@link com.top_logic.reporting.data.base.value.common.NumericValue}s
     * so if there is a wrong value in the map, it'll throw an exception.
     * 
     * @param    someValues    A pair of Ranges with the matching values. Only NumericValues are allowed here
     * @param    aName         The name of this value holder to be used
     *                         in GUIs.
     * @throws   IllegalArgumentException    If one parameter is 
     *                                       <code>null</code>.
     * @throws   ClassCastException      If one key is not {@link java.lang.Comparable}
     *                                   or a value is not  {@link com.top_logic.reporting.data.base.value.common.NumericValue}.
     */
    public NumberValueHolder(Map someValues, String aName)
        throws IllegalArgumentException, ClassCastException {
        super(aName);

        if (someValues == null) {
            throw new IllegalArgumentException(
                "given map of ranges and " + "values is null!");
        }

        if (someValues.size() == 0) {
            throw new IllegalArgumentException(
                "given map of ranges and " + "values is empty!");
        }

        Set     theRange  = someValues.keySet();
        int     theSize   = theRange.size();
        Range[] theComp   = new Range[theSize];
        Value[] theValues = new Value[theSize];

        try {
            theComp = (Range[]) theRange.toArray(theComp);
        } catch (ArrayStoreException ex) {
            throw new ClassCastException("Keys of map are no Range instances.");
        }

        for (int thePos = 0; thePos < theSize; thePos++) {
            theValues[thePos] = (NumericValue) someValues.get(theComp[thePos]);
        }

        this.init(theComp, theValues);
    }

    /**
     * Constructor for NumberValueHolder.
     * 
     * @param    aName         The display name of this instance.
     * @param    aRange        The supported range of the values.
     * @param    someValues    The values held by the instance.
     * @throws   IllegalArgumentException     If the range has another size
     *                                        than the values or the given
     *                                        name is empty.
     */
    public NumberValueHolder(
        String aName,
        Range[] aRange,
        NumericValue[] someValues)
        throws IllegalArgumentException {
        super(aName, aRange, someValues);
    }
}
