/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.format;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.format.JavaTypeFormat;
import com.top_logic.basic.reflect.JavaTypeUtil;

/**
 * Test case for {@link JavaTypeFormat}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestJavaTypeFormat extends TestCase {

	@SuppressWarnings("unused")
	private static class Holder<V> {
		Map<String, ? extends List<Integer>> _map;

		Map.Entry<? super Number, ?>[] _entries;

		int[][] _ints;

		List<V> _variable;
	}

	public void testRoundTrip() throws Exception {
		assertRoundTrip("java.util.Map<java.lang.String, ? extends java.util.List<java.lang.Integer>>",
			field("_map"));
		assertRoundTrip("java.util.Map$Entry<? super java.lang.Number, ?>[]", field("_entries"));
		assertRoundTrip("int[][]", field("_ints"));
		assertRoundTrip("java.lang.String", String.class);
	}

	public void testTypeVariableErased() throws Exception {
		Type type = field("_variable");
		assertEquals("java.util.List<java.lang.Object>", JavaTypeFormat.INSTANCE.getSpecification(type));
		assertEquals(JavaTypeUtil.eraseTypeVariables(type), parse("java.util.List<java.lang.Object>"));
	}

	public void testWhiteSpace() throws Exception {
		assertEquals(field("_map"), parse(" java.util.Map < java.lang.String ,? extends java.util.List<java.lang.Integer> > "));
	}

	public void testInvalid() {
		assertInvalid("java.util.List<");
		assertInvalid("java.util.List<java.lang.String");
		assertInvalid("java.util.List<java.lang.String, java.lang.String>");
		assertInvalid("java.util.List java.lang.String");
		assertInvalid("no.such.Clazz");
		assertInvalid("java.lang.String[");
	}

	private static void assertRoundTrip(String expectedSpec, Type type) throws ConfigurationException {
		String spec = JavaTypeFormat.INSTANCE.getSpecification(type);
		assertEquals(expectedSpec, spec);
		Type parsed = parse(spec);
		assertEquals(type, parsed);
		assertEquals(parsed, type);
		assertEquals(type.hashCode(), parsed.hashCode());
	}

	private static void assertInvalid(String spec) {
		try {
			parse(spec);
			fail("Invalid type expected: " + spec);
		} catch (ConfigurationException ex) {
			// Expected.
		}
	}

	private static Type parse(String spec) throws ConfigurationException {
		return JavaTypeFormat.INSTANCE.getValue("test", spec);
	}

	private static Type field(String name) throws NoSuchFieldException {
		return Holder.class.getDeclaredField(name).getGenericType();
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(TestJavaTypeFormat.class);
	}

}
