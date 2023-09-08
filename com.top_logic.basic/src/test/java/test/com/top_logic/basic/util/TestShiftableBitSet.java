/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.util.ShiftableBitSet;

/**
 * Test the {@link ShiftableBitSet}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestShiftableBitSet extends TestCase {

    public TestShiftableBitSet(String name) {
        super(name);
    }

    /**
     * Test method for {@link com.top_logic.basic.util.ShiftableBitSet#shiftLeft(int)}.
     */
    public void testShiftLeft() {
        ShiftableBitSet sbs = new ShiftableBitSet(128);
        sbs.set(0);
        sbs.set(63);
        sbs.set(64);
        sbs.set(127);
        sbs.shiftLeft(1);
        assertFalse(sbs.get(0));
        assertFalse(sbs.get(1));
        assertTrue (sbs.get(62));
        assertTrue (sbs.get(63));
        assertFalse(sbs.get(64));
        assertTrue (sbs.get(126));
        assertFalse(sbs.get(127));
        assertFalse(sbs.get(128));
    }

    /**
     * Test method for {@link com.top_logic.basic.util.ShiftableBitSet#shiftRight(int)}.
     */
    public void testShiftRight() {
        ShiftableBitSet sbs = new ShiftableBitSet(128);
        sbs.set(0);
        sbs.set(63);
        sbs.set(64);
        sbs.set(127);
        sbs.shiftRight(1);
        assertFalse(sbs.get(0));
        assertTrue (sbs.get(1));
        assertFalse(sbs.get(63));
        assertTrue (sbs.get(64));
        assertTrue (sbs.get(65));
        assertFalse(sbs.get(127));
        assertTrue (sbs.get(128));
    }

	public static Test suite() {
		return new TestSuite(TestShiftableBitSet.class);
	}

}

