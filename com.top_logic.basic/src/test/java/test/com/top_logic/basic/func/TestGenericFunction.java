/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.func;

import java.util.Arrays;

import junit.framework.TestCase;

import com.top_logic.basic.func.And;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.Function2;
import com.top_logic.basic.func.Function3;
import com.top_logic.basic.func.GenericFunction;
import com.top_logic.basic.func.Or;

/**
 * Test case for {@link GenericFunction}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestGenericFunction extends TestCase {

	public void testFunction() {
		Function1<Integer, Integer> inc = new Function1<>() {
			@Override
			public Integer apply(Integer arg) {
				return arg + 1;
			}
		};

		assertEquals(2, inc.apply(1).intValue());
		assertEquals(2, inc.map(1).intValue());
		assertEquals(2, inc.invoke(1).intValue());
		checkGenericInvoke(inc);
	}

	public void testFunction0() {
		Function0<Integer> fortytwo = new Function0<>() {
			@Override
			public Integer apply() {
				return 42;
			}
		};

		assertEquals(42, fortytwo.apply().intValue());
		assertEquals(42, fortytwo.invoke().intValue());
		checkGenericInvoke(fortytwo);
	}

	public void testFunction2() {
		Function2<Integer, Integer, Integer> add = new Function2<>() {
			@Override
			public Integer apply(Integer arg1, Integer arg2) {
				return arg1 + arg2;
			}
		};

		assertEquals(3, add.apply(1, 2).intValue());
		assertEquals(3, add.invoke(1, 2).intValue());
		checkGenericInvoke(add);
	}

	public void testFunction3() {
		Function3<Integer, Integer, Integer, Integer> fun = new Function3<>() {
			@Override
			public Integer apply(Integer arg1, Integer arg2, Integer arg3) {
				return (arg1 + arg2) * arg3;
			}
		};

		assertEquals(9, fun.apply(1, 2, 3).intValue());
		assertEquals(9, fun.invoke(1, 2, 3).intValue());
		checkGenericInvoke(fun);
	}

	public void testAnd() {
		And and = new And();
		assertTrue(and.invoke());
		assertTrue(and.invoke(true));
		assertTrue(and.invoke(true, true));
		assertFalse(and.invoke(true, true, false));
	}

	public void testOr() {
		Or or = new Or();
		assertFalse(or.invoke());
		assertFalse(or.invoke(false));
		assertFalse(or.invoke(false, false));
		assertTrue(or.invoke(true));
		assertTrue(or.invoke(true, true));
		assertTrue(or.invoke(true, true, false));
	}

	private void checkGenericInvoke(GenericFunction<?> fun) {
		Object[] args = new Object[fun.getArgumentCount()];
		Arrays.fill(args, 1);
		fun.invoke(args);
	}

}
