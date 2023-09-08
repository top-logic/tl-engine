/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.util.BitSet;

/**
 * For some strange reason a BitSet cannot be shifted ?.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class ShiftableBitSet extends BitSet {

    
    public ShiftableBitSet() {
        super();
    }

    public ShiftableBitSet(int aNbits) {
        super(aNbits);
    }

    /** Shift Bits to the left, loosing the first n bits */
    public void shiftLeft(int n) {
        if (n <= 0) 
            throw new IllegalArgumentException("n must be greater than 1");
        int len = length();
        int i = 0;
        int j = n;
        while (i < len) {
            this.set(i++, this.get(j++));
        }
        this.clear(len - n, len - 1);
    }
    
    /** Shift Bits to the right, adding 0 */
    public void shiftRight(int n) {
        if (n <= 0) 
            throw new IllegalArgumentException("n must be greater than 1");
        int len = length();
        int i = len + n;
        int j = len;
        while (j >= 0) {
            this.set(i--, this.get(j--));
        }
        this.clear(0,n);
    }
}

