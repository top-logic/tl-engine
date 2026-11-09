/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.operations;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyTemplate;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.operations.ConversionSite;
import com.top_logic.model.search.expr.config.operations.I18NConstants;
import com.top_logic.model.search.expr.config.operations.ScriptConversion;
import com.top_logic.model.search.expr.config.operations.ScriptConversions;
import com.top_logic.model.search.expr.config.operations.ValueConverter;
import com.top_logic.util.error.TopLogicException;

/**
 * Test case for {@link ScriptConversions}.
 */
@SuppressWarnings("javadoc")
public class TestScriptConversions extends TestCase {

	private static final ConversionSite ARGUMENT = new ConversionSite.FunctionArgument("fun", "arg");

	public enum Color {
		RED, GREEN;
	}

	/**
	 * {@link ValueConverter} for {@link Signatures#annotated()}.
	 */
	public static class Doubling implements ValueConverter {
		@Override
		public Object fromScript(Object scriptValue) {
			return scriptValue + "" + scriptValue;
		}
	}

	/**
	 * Methods providing the target types of the conversions under test.
	 */
	public interface Signatures<T extends Number> {
		List<Integer> integerList();

		Set<String> stringSet();

		List<?> anyList();

		List<? extends Number> numberList();

		T typeVariable();

		<X> X unboundTypeVariable();

		@ScriptConversion(Doubling.class)
		String annotated();

		int primitive();
	}

	public void testNumbers() {
		assertEquals(Integer.valueOf(3), convert(int.class, 3.0));
		assertEquals(Integer.valueOf(3), convert(Integer.class, 3.0));
		assertEquals(Long.valueOf(3), convert(long.class, 3.0));
		assertEquals(Long.valueOf(3), convert(Long.class, 3.0));
		assertEquals(Float.valueOf(1.5f), convert(float.class, 1.5));
		assertEquals(Float.valueOf(1.5f), convert(Float.class, 1.5));
		assertEquals(Double.valueOf(1.5), convert(double.class, 1.5));
		assertEquals(Double.valueOf(1.5), convert(Double.class, 1.5));
		assertEquals(Double.valueOf(3), convert(Double.class, 3L));
		assertNull(convert(Integer.class, null));
	}

	public void testBoolean() {
		assertEquals(Boolean.TRUE, convert(boolean.class, Boolean.TRUE));
		assertEquals(Boolean.FALSE, convert(boolean.class, null));
		assertNull(convert(Boolean.class, null));
		assertEquals(Boolean.TRUE, convert(Boolean.class, Boolean.TRUE));
	}

	public void testString() {
		assertEquals("abc", convert(String.class, "abc"));
		assertNull(convert(String.class, null));
	}

	public void testEnum() {
		assertEquals(Color.GREEN, convert(Color.class, "GREEN"));
		assertEquals(Color.RED, convert(Color.class, Color.RED));
		assertNull(convert(Color.class, null));
		TopLogicException ex = assertFails(Color.class, "blue");
		assertError(I18NConstants.ERROR_WRONG_ARGUMENT__FUN_ARG_EXPECTED_VAL, ex, "fun", "arg",
			"Color (RED, GREEN)", "blue");
	}

	public void testArray() {
		int[] ints = (int[]) convert(int[].class, List.of(1.0, 2.0));
		assertTrue(Arrays.equals(new int[] { 1, 2 }, ints));

		String[] strings = (String[]) convert(String[].class, "x");
		assertTrue(Arrays.equals(new String[] { "x" }, strings));

		Object[] objects = (Object[]) convert(Object[].class, List.of("a", 1.0));
		assertTrue(Arrays.equals(new Object[] { "a", 1.0 }, objects));
	}

	public void testList() throws Exception {
		assertEquals(List.of(1, 2), convert(returnType("integerList"), List.of(1.0, 2.0)));
		assertEquals(List.of(1), convert(returnType("integerList"), 1.0));
		assertEquals(List.of("a"), convert(returnType("anyList"), "a"));
		assertEquals(List.of(), convert(List.class, null));
	}

	public void testSet() throws Exception {
		assertEquals(Set.of("a", "b"), convert(returnType("stringSet"), List.of("a", "b")));
		assertEquals(Set.of("a"), convert(Set.class, "a"));
	}

	public void testWildcard() throws Exception {
		assertEquals(List.of(1.0), convert(returnType("numberList"), List.of(1.0)));
		TopLogicException ex = assertFails(returnType("numberList"), List.of("a"));
		assertError(I18NConstants.ERROR_WRONG_ARGUMENT__FUN_ARG_EXPECTED_VAL, ex, "fun", "arg", "Number", "a");
	}

	public void testTypeVariable() throws Exception {
		// Converted to the bound Number.
		assertEquals(1.0, convert(returnType("typeVariable"), 1.0));
		TopLogicException ex = assertFails(returnType("typeVariable"), "a");
		assertError(I18NConstants.ERROR_WRONG_ARGUMENT__FUN_ARG_EXPECTED_VAL, ex, "fun", "arg", "Number", "a");

		// Bound Object: no conversion.
		assertNull(ScriptConversions.fromScript(returnType("unboundTypeVariable"), ARGUMENT));
	}

	public void testObjectNeedsNoConversion() {
		assertNull(ScriptConversions.fromScript(Object.class, ARGUMENT));
	}

	public void testInstanceCheck() {
		assertEquals(List.of(), convert(Iterable.class, List.of()));
		TopLogicException ex = assertFails(Runnable.class, "a");
		assertError(I18NConstants.ERROR_WRONG_ARGUMENT__FUN_ARG_EXPECTED_VAL, ex, "fun", "arg", "Runnable", "a");
	}

	public void testAnnotatedMethod() throws Exception {
		Method method = Signatures.class.getMethod("annotated");
		ValueConverter converter =
			ScriptConversions.fromScript(method.getGenericReturnType(), method, resultSite(method));
		assertEquals("abab", converter.fromScript("ab"));
	}

	public void testAnnotatedParameter() throws Exception {
		Method method = Parameters.class.getMethod("withConversion", String.class);
		java.lang.reflect.Parameter parameter = method.getParameters()[0];
		ValueConverter converter =
			ScriptConversions.fromScript(parameter.getParameterizedType(), parameter, ARGUMENT);
		assertEquals("xx", converter.fromScript("x"));
	}

	public void testWrongResult() throws Exception {
		Method method = Signatures.class.getMethod("primitive");
		ConversionSite site = resultSite(method);
		ValueConverter converter = ScriptConversions.fromScript(method.getGenericReturnType(), method, site);
		assertEquals(Integer.valueOf(4), converter.fromScript(4.0));
		try {
			converter.fromScript("four");
			fail("Expected failure.");
		} catch (TopLogicException ex) {
			assertError(I18NConstants.ERROR_WRONG_RESULT__TYPE_METHOD_EXPECTED_VAL, ex,
				Signatures.class.getName(), "primitive", "Number", "four");
		}
	}

	public void testToScript() {
		Object value = List.of("a");
		assertSame(value, ScriptConversions.toScript(value, false));
		assertSame(value, SearchExpression.discoverable(value, false));
	}

	/**
	 * Holder of a method with an annotated parameter.
	 */
	public static class Parameters {
		public static String withConversion(@ScriptConversion(Doubling.class) String value) {
			return value;
		}
	}

	private static ConversionSite resultSite(Method method) {
		return new ConversionSite.MethodResult(Signatures.class, method);
	}

	private static Type returnType(String methodName) throws NoSuchMethodException {
		return Signatures.class.getMethod(methodName).getGenericReturnType();
	}

	private static Object convert(Type type, Object scriptValue) {
		ValueConverter converter = ScriptConversions.fromScript(type, null, ARGUMENT);
		assertNotNull("No conversion for: " + type, converter);
		return converter.fromScript(scriptValue);
	}

	private static TopLogicException assertFails(Type type, Object scriptValue) {
		try {
			convert(type, scriptValue);
		} catch (TopLogicException ex) {
			return ex;
		}
		fail("Expected conversion of '" + scriptValue + "' to '" + type + "' to fail.");
		return null;
	}

	private static void assertError(ResKeyTemplate expectedKey, TopLogicException ex, Object... expectedArguments) {
		ResKey errorKey = ex.getErrorKey();
		assertEquals(expectedKey.getKey(), errorKey.getKey());
		assertEquals(Arrays.asList(expectedArguments), Arrays.asList(errorKey.arguments()));
	}

}
