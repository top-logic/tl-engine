/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.generate;

import junit.framework.TestCase;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.generate.TypeUtil;

/**
 * Test case for {@link TypeUtil}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypeUtil extends TestCase {

	interface A {
		// Only type hierarchy is relevant.
	}

	interface B extends A {
		// Only type hierarchy is relevant.
	}

	interface C extends B {
		// Only type hierarchy is relevant.
	}

	interface CParam<T> extends C {
		// Only type hierarchy is relevant.
	}

	class X<T extends A> {
		// Only type hierarchy is relevant.
	}

	class Y<S extends B> extends X<S> {
		// Only type hierarchy is relevant.
	}

	class Z extends Y<C> {
		// Only type hierarchy is relevant.
	}

	@SuppressWarnings("rawtypes")
	class ZRaw extends Y {
		// Only type hierarchy is relevant.
	}

	class ZParam<T> extends Y<CParam<T>> {
		// Only type hierarchy is relevant.
	}

	class ZVarBound<S extends C, T extends S> extends Y<T> {
		// Only type hierarchy is relevant.
	}

	class ZParamBound<T, S extends CParam<T>> extends Y<S> {
		// Only type hierarchy is relevant.
	}

	class ZZ extends Z {
		// Only type hierarchy is relevant.
	}

	public void testFindTypeBound1() {
		assertEquals(C.class, TypeUtil.findTypeBound(ZZ.class, X.class, 0));
	}

	public void testFindTypeBound2() {
		assertEquals(C.class, TypeUtil.findTypeBound(Z.class, X.class, 0));
	}

	public void testFindTypeBound2Raw() {
		assertEquals(B.class, TypeUtil.findTypeBound(ZRaw.class, X.class, 0));
	}

	public void testFindTypeBound2Param() {
		assertEquals(CParam.class, TypeUtil.findTypeBound(ZParam.class, X.class, 0));
	}

	public void testFindTypeBound2VarBound() {
		assertEquals(C.class, TypeUtil.findTypeBound(ZVarBound.class, X.class, 0));
	}

	public void testFindTypeBound2ParamBound() {
		assertEquals(CParam.class, TypeUtil.findTypeBound(ZParamBound.class, X.class, 0));
	}

	public void testFindTypeBound3() {
		assertEquals(B.class, TypeUtil.findTypeBound(Y.class, X.class, 0));
	}

	public void testFindTypeBound4() {
		assertEquals(A.class, TypeUtil.findTypeBound(X.class, X.class, 0));
	}

	public void testFindTypeBound5() {
		assertEquals(C.class, TypeUtil.findTypeBound(ZZ.class, Y.class, 0));
	}

	public void testFindTypeBound6() {
		assertEquals(C.class, TypeUtil.findTypeBound(Z.class, Y.class, 0));
	}

	public void testFindTypeBound6Raw() {
		assertEquals(B.class, TypeUtil.findTypeBound(ZRaw.class, Y.class, 0));
	}

	public void testFindTypeBound7() {
		assertEquals(B.class, TypeUtil.findTypeBound(Y.class, Y.class, 0));
	}

	public void testFindTypeBound8() {
		try {
			TypeUtil.findTypeBound(X.class, Y.class, 0);
			fail("Start class is not a subclass of resolved class.");
		} catch (IllegalArgumentException ex) {
			// Expected.
			BasicTestCase.assertContains("not an anchestor class", ex.getMessage());
		}
	}

}
