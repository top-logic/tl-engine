/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.reflect;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import junit.framework.TestCase;

import com.top_logic.basic.reflect.DefaultMethodInvoker;

/**
 * Test case for {@link DefaultMethodInvoker}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDefaultMethodInvoker extends TestCase {
	
	public interface A {
		
		default String foo(int arg) {
			return "A:" + arg;
		}
		
		Lookup LOOKUP = MethodHandles.lookup();

	}

	public interface B extends A {

		@Override
		default String foo(int arg) {
			return "B:" + arg;
		}

		default boolean isB() {
			return true;
		}

		Lookup LOOKUP = MethodHandles.lookup();

	}
	
	@SuppressWarnings("unused")
	public static class ABImpl implements A, B {
		// Only default methods.
	}

	public void testInvoke() {
		InvocationHandler handler = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				return DefaultMethodInvoker.INSTANCE.invoke(proxy, method, args);
			}
		};
		
		Class<?>[] typeA = { A.class };
		Object a1 = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), typeA, handler);

		Class<?>[] typeB = { B.class };
		Object b1 = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), typeB, handler);

		Class<?>[] typeAB = { A.class, B.class };
		Object ab1 = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), typeAB, handler);

		Class<?>[] typeBA = { B.class, A.class };
		Object ba1 = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), typeBA, handler);

		assertEquals("A:42", ((A) a1).foo(42));

		assertTrue(((B) b1).isB());
		assertEquals("B:42", ((A) b1).foo(42));
		assertEquals("B:42", ((B) b1).foo(42));

		// This looks crazy: Because the redundant interface A is implemented "before" B, the
		// methods from A are invoked on the proxy (no matter if the static type is A or B).
		assertTrue(((B) ab1).isB());
		assertEquals("A:42", ((A) ab1).foo(42));
		assertEquals("A:42", ((B) ab1).foo(42));

		// When doing the same thing with a regular class, the methods from B are invoked as
		// expected.
		Object ab2 = new ABImpl();
		assertTrue(((B) ab2).isB());
		assertEquals("B:42", ((A) ab2).foo(42));
		assertEquals("B:42", ((B) ab2).foo(42));

		// When implementing the redundant interface A last, everything is as expected.
		assertTrue(((B) ba1).isB());
		assertEquals("B:42", ((A) ba1).foo(42));
		assertEquals("B:42", ((B) ba1).foo(42));
	}

}
