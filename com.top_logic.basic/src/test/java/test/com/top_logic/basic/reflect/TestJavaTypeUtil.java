/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.reflect;

import java.math.BigDecimal;
import java.math.BigInteger;

import junit.framework.TestCase;

import com.top_logic.basic.reflect.JavaTypeUtil;

/**
 * Test case for {@link JavaTypeUtil}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestJavaTypeUtil extends TestCase {

	interface I<X> {
		// Empty
	}

	interface II<X> {
		// Empty
	}

	class A implements I<String> {
		// Empty.
	}

	class B extends A {
		// Empty.
	}

	class C<S, T> implements I<T> {
		// Empty.
	}

	class D extends C<String, Number> {
		// Empty.
	}

	@SuppressWarnings("rawtypes")
	class E extends C {
		// Empty.
	}

	class F<T extends Number> {
		// Empty.
	}

	class G<T extends BigDecimal> extends F<T> {
		// Empty.
	}

	class H extends G<MyNum> {
		// Empty.
	}

	@SuppressWarnings("rawtypes")
	class J extends G {
		// Empty.
	}

	class K extends C<Void, Double[][]> {
		// Empty.
	}

	class L<T extends Number> extends C<Void, T[][]> {
		// Empty.
	}

	class M extends L<Double> {
		// Empty.
	}

	class N extends G<MyNum> implements II<Double>, I<String> {
		// Empty.
	}

	class MyNum extends BigDecimal {
		public MyNum(BigInteger val) {
			super(val);
		}
	}

	public void testUnboundVar() {
		assertEquals(Object.class, JavaTypeUtil.getTypeBound(I.class, I.class, 0));
	}

	public void testBoundVar() {
		assertEquals(String.class, JavaTypeUtil.getTypeBound(A.class, I.class, 0));
	}

	public void testBindingInSuperType() {
		assertEquals(String.class, JavaTypeUtil.getTypeBound(B.class, I.class, 0));
	}

	public void testReanmedUnboundVar() {
		assertEquals(Object.class, JavaTypeUtil.getTypeBound(C.class, I.class, 0));
	}

	public void testReanmedBoundVar() {
		assertEquals(Number.class, JavaTypeUtil.getTypeBound(D.class, I.class, 0));
	}

	public void testRawSuperType() {
		assertEquals(Object.class, JavaTypeUtil.getTypeBound(E.class, I.class, 0));
	}

	public void testArrayType() {
		assertEquals(Double[][].class, JavaTypeUtil.getTypeBound(K.class, I.class, 0));
	}

	public void testGenericArrayTypeUpperBound() {
		assertEquals(Number[][].class, JavaTypeUtil.getTypeBound(L.class, I.class, 0));
	}

	public void testGenericArrayType() {
		assertEquals(Double[][].class, JavaTypeUtil.getTypeBound(M.class, I.class, 0));
	}

	public void testUnboundVarWithUpperBound() {
		assertEquals(Number.class, JavaTypeUtil.getTypeBound(F.class, F.class, 0));
	}

	public void testUnboundVarWithNarrowedUpperBound() {
		assertEquals(BigDecimal.class, JavaTypeUtil.getTypeBound(G.class, F.class, 0));
	}

	public void testBoundVarWithNarrowedUpperBound() {
		assertEquals(MyNum.class, JavaTypeUtil.getTypeBound(H.class, F.class, 0));
	}

	public void testRawSuperTypeWithVarWithNarrowedUpperBound() {
		assertEquals(BigDecimal.class, JavaTypeUtil.getTypeBound(J.class, F.class, 0));
	}

	public void testSearchInterfaceType() {
		assertEquals(String.class, JavaTypeUtil.getTypeBound(N.class, I.class, 0));
	}

}
