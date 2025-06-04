/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.math;

import junit.framework.TestCase;

/**
 * Test case for {@link Mat}.
 */
@SuppressWarnings("javadoc")
public class TestMat extends TestCase {

	public void testApply() {
		assertEquals(Vec.vec(-13, 42), Mat.FLIP_X.apply(Vec.vec(13, 42)));
		assertEquals(Vec.vec(13, -42), Mat.FLIP_Y.apply(Vec.vec(13, 42)));
		assertEquals(Vec.vec(0, 1), Mat.ROT_90.apply(Vec.vec(1, 0)));
		assertEquals(Vec.vec(-1, 0), Mat.ROT_90.apply(Vec.vec(0, 1)));
	}

	public void testMul() {
		assertEquals(Mat.IDENTITY, Mat.ROT_270.apply(Mat.ROT_90));
		assertEquals(Mat.IDENTITY, Mat.FLIP_X.apply(Mat.FLIP_X));
		assertEquals(Mat.IDENTITY, Mat.FLIP_Y.apply(Mat.FLIP_Y));
		assertEquals(Mat.IDENTITY, Mat.ROT_180.apply(Mat.ROT_180));
	}

}
