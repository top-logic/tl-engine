/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.template;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.template.BufferedOutput;
import com.top_logic.basic.config.template.EmptyScope;
import com.top_logic.basic.config.template.Eval;
import com.top_logic.basic.config.template.Eval.EvalException;
import com.top_logic.basic.config.template.Expand;
import com.top_logic.basic.config.template.MapScope;
import com.top_logic.basic.config.template.TemplateExpression;
import com.top_logic.basic.config.template.TemplateScope;
import com.top_logic.basic.config.template.parser.ConfigTemplateParser;
import com.top_logic.basic.config.template.parser.ParseException;

/**
 * Test case for {@link TemplateExpression}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTemplateExpression extends TestCase {

	public interface A extends ConfigurationItem {

		String getS();

		void setS(String value);

		int getX();

		void setX(int value);

		long getY();

		void setY(long value);

		A getA();

		void setA(A next);

		@Mandatory
		String getM();

		void setM(String value);

		String getPropertyWithLongName();

		void setPropertyWithLongName(String value);
	}

	public void testLiteralText() throws ParseException {
		assertExpand(list("Hello World"), "Hello World");
	}

	public void testTag() throws ParseException {
		assertExpand(list("Hello ", "World"), "<div>Hello <span class=\"foobar\">World</span></div>");
	}

	public void testChoiceNull() throws ParseException {
		assertExpand(list("empty"), "{$s ? {string {$s}} : 'empty'}", var("s", null));
	}

	public void testChoiceBoolean() throws ParseException {
		assertExpand(list("yes"), "{$b ? {yes} : {no}}", var("b", true));
		assertExpand(list("no"), "{$b ? {yes} : {no}}", var("b", false));
	}

	public void testChoiceList() throws ParseException {
		assertExpand(list("empty"), "{$c ? {filled} : {empty}}", var("c", list()));
		assertExpand(list("filled"), "{$c ? {filled} : {empty}}", var("c", list("")));
	}

	public void testChoiceSet() throws ParseException {
		assertExpand(list("empty"), "{$c ? {filled} : {empty}}", var("c", set()));
		assertExpand(list("filled"), "{$c ? {filled} : {empty}}", var("c", set("")));
	}

	public void testChoiceMap() throws ParseException {
		assertExpand(list("empty"), "{$c ? {filled} : {empty}}", var("c", Collections.emptyMap()));
		assertExpand(list("filled"), "{$c ? {filled} : {empty}}", var("c", Collections.singletonMap("foo", "bar")));
	}

	public void testChoiceInt() throws ParseException {
		assertExpand(list("filled"), "{$c ? {filled} : {empty}}", var("c", 0));
		assertExpand(list("filled"), "{$c ? {filled} : {empty}}", var("c", 1));
	}

	public void testChoiceEmpty() throws ParseException {
		assertExpand(list("empty"), "{$s ? {string {$s}} : 'empty'}", var("s", ""));
	}

	public void testChoiceEmptyList() throws ParseException {
		assertExpand(list("empty"), "{$s ? {string {$s}} : 'empty'}", var("s", list()));
	}

	public void testChoiceEmptyMap() throws ParseException {
		assertExpand(list("empty"), "{$s ? {string {$s}} : 'empty'}", var("s", Collections.emptyMap()));
	}

	public void testChoiceNonEmpty() throws ParseException {
		assertExpand(list("string ", "foo"), "{$s ? {string {$s}} : 'empty'}", var("s", "foo"));
	}

	public void testAlternativeEmpty() throws ParseException {
		assertExpand(list("empty"), "{$s | 'empty'}", var("s", ""));
	}

	public void testAlternativeNonEmpty() throws ParseException {
		assertExpand(list("foo"), "{$s | 'empty'}", var("s", "foo"));
	}

	public void testAlternativeWithTemplateFallback() throws ParseException {
		assertExpand(list("fallback: ", "b-value"), "{$a | {fallback: {$b}}}", var("a", ""), var("b", "b-value"));
	}

	public void testAlternativeWithTemplateFallback3() throws ParseException {
		assertExpand(list("fallback: ", "c-value"), "{$a | $b | {fallback: {$c}}}",
			var("a", ""), var("b", null), var("c", "c-value"));
	}

	public void testAlternativeWithTemplateFallback2() throws ParseException {
		failExpand("Only simple expressions may be used in a boolean context",
			"{$a | {$b} | {fallback: {$c}}}", var("a", ""), var("b", null), var("c", "c-value"));
	}

	public void testListAccessAtZero() throws ParseException {
		assertExpand(list("foo"), "{$s[0]}", var("s", list("foo", "bar")));
	}

	public void testListAccessNegative() throws ParseException {
		assertExpand(list("bar"), "{$s[-1]}", var("s", list("foo", "bar")));
		assertExpand(list("foo"), "{$s[-2]}", var("s", list("foo", "bar")));
		assertExpand(list(), "{$s[-3]}", var("s", list("foo", "bar")));
	}

	public void testListAccessNegativeArray() throws ParseException {
		assertExpand(list("bar"), "{$s[-1]}", var("s", new Object[] { "foo", "bar" }));
		assertExpand(list("foo"), "{$s[-2]}", var("s", new Object[] { "foo", "bar" }));
		assertExpand(list(), "{$s[-3]}", var("s", new Object[] { "foo", "bar" }));
	}

	public void testListAccessAtOne() throws ParseException {
		assertExpand(list("bar"), "{$s[1]}", var("s", list("foo", "bar")));
	}

	public void testListAccessOutOfBounds() throws ParseException {
		assertExpand(list(), "{$a[1]}", var("a", list("foo")));
		assertExpand(list(), "{$s[2]}", var("s", new Object[] { "foo", "bar" }));
	}

	public void testNullCollectionAccess() throws ParseException {
		assertExpand(list(), "{$a[1]}", var("a", null));
	}

	public void testNestedListAccess() throws ParseException {
		assertExpand(list("bar"), "{$s[0][1][2]}", var("s", list(list("foo", list("foo", "foo", "bar")))));
	}

	public void testMapAccess() throws ParseException {
		assertExpand(list("bar"), "{$m['foo']}", var("m", Collections.singletonMap("foo", "bar")));
	}

	public void testSublist() throws ParseException {
		assertExpand(list("foo", ", ", "bar"), "{foreach(x : #sublist($s, 1, #sub(#size($s), 1)), ', ', $x)}",
			var("s", list("xxx", "foo", "bar", "xxx")));
		assertExpand(list("foo", ", ", "bar", ", ", "xxx"), "{foreach(x : #sublist($s, 1), ', ', $x)}",
			var("s", list("xxx", "foo", "bar", "xxx")));
	}

	public void testAdd() throws ParseException {
		assertExpand(list(7), "{#int(#add(#size($s), 5))}", var("s", list("foo", "bar")));
	}

	public void testMul() throws ParseException {
		assertExpand(list(10), "{#int(#mul(#size($s), 5))}", var("s", list("foo", "bar")));
	}

	public void testDiv() throws ParseException {
		assertExpand(list(4), "{#int(#div(9, #size($s)))}", var("s", list("foo", "bar")));
	}

	public void testSub() throws ParseException {
		assertExpand(list(7), "{#int(#sub(9, #size($s)))}", var("s", list("foo", "bar")));
	}

	public void testSize() throws ParseException {
		assertExpand(list(0, 1, 2, 3, 4), "{#size($a)}{#size($b)}{#size($c)}{#size($d)}{#size('abcd')}",
			var("d", Arrays.asList("x", "y", "z")), var("c", new Object[2]), var("b", 42), var("a", null));
	}

	public void testConcat() throws ParseException {
		assertExpand(list("foobar13"), "{#concat('foo', 'bar', $a, 13)}", var("a", null));
	}
	
	public void testSubstring() throws ParseException {
		assertExpand(list("oba"), "{#substring('foobar13', 2, 5)}");
		assertExpand(list("obar13"), "{#substring('foobar13', 2)}");
		failExpand("First argument must be a string", "{#substring(41, 1, 2)}");
		failExpand("Invalid number of arguments", "{#substring('foo', 1, 2, 3)}");
	}
	
	public void testIf() throws ParseException {
		assertExpand(list("yes"), "{#if($a, 'yes', 'no')}", var("a", true));
		assertExpand(list("no"), "{#if($a, 'yes', 'no')}", var("a", false));
		assertExpand(list("no"), "{#if($a, 'yes', 'no')}", var("a", null));
		assertExpand(list("no"), "{#if($a, 'yes', 'no')}", var("a", list()));
		assertExpand(list("yes"), "{#if($a, 'yes', 'no')}", var("a", list("foo")));
		assertExpand(list("no"), "{#if($a, 'yes', 'no')}", var("a", 0));
		assertExpand(list("yes"), "{#if($a, 'yes', 'no')}", var("a", 1));
	}

	public void testAnd() throws ParseException {
		assertExpand(list("yes"), "{#and($a, $b) ? 'yes' : 'no'}", var("b", "foo"), var("a", true));
		assertExpand(list("no"), "{#and($a, $b) ? 'yes' : 'no'}", var("b", "foo"), var("a", null));
	}

	public void testOr() throws ParseException {
		assertExpand(list("yes"), "{#or($a, $b) ? 'yes' : 'no'}", var("b", list("foo")), var("a", false));
		assertExpand(list("no"), "{#or($a, $b) ? 'yes' : 'no'}", var("b", list()), var("a", null));
	}

	public void testNot() throws ParseException {
		assertExpand(list("yes"), "{#not($a) ? 'yes' : 'no'}", var("a", new Object[0]));
		assertExpand(list("no"), "{#not($a) ? 'yes' : 'no'}", var("a", new Object[1]));
		assertExpand(list("no"), "{#not($a.property-with-long-name) ? 'yes' : 'no'}", var("a", newA("non-null")));
	}

	public void testEquals() throws ParseException {
		assertExpand(list("yes"), "{#equals('foo', $a) ? 'yes' : 'no'}", var("a", "foo"));
		assertExpand(list("no"), "{#equals(2, #size($a)) ? 'yes' : 'no'}", var("a", new Object[1]));
		assertExpand(list("yes"), "{#equals($a, $b) ? 'yes' : 'no'}", var("a", 3), var("b", 3L));
		assertExpand(list("no"), "{#equals($a, $b) ? 'yes' : 'no'}", var("a", 3.1), var("b", 3.2));
		assertExpand(list("no"), "{#equals($a, $b) ? 'yes' : 'no'}", var("a", 3.1), var("b", 3));
	}

	public void testGt() throws ParseException {
		assertExpand(list("yes"), "{#gt(2, 1) ? 'yes' : 'no'}");
		assertExpand(list("yes"), "{#gt(this.y, 1) ? 'yes' : 'no'}", newA(2));
		assertExpand(list("yes"), "{#gt(#int(this.s), 1) ? 'yes' : 'no'}", newA("2"));
		assertExpand(list("no"), "{#gt('a', 'a') ? 'yes' : 'no'}");
		assertExpand(list("no"), "{#gt(1, 2) ? 'yes' : 'no'}");
	}

	public void testGe() throws ParseException {
		assertExpand(list("yes"), "{#ge(2, 1) ? 'yes' : 'no'}");
		assertExpand(list("yes"), "{#ge(this.y, 1) ? 'yes' : 'no'}", newA(2));
		assertExpand(list("yes"), "{#ge(#int(this.s), 1) ? 'yes' : 'no'}", newA("2"));
		assertExpand(list("yes"), "{#ge('a', 'a') ? 'yes' : 'no'}");
		assertExpand(list("no"), "{#ge('a', 'ab') ? 'yes' : 'no'}");
	}

	public void testLt() throws ParseException {
		assertExpand(list("no"), "{#lt(2, 1) ? 'yes' : 'no'}");
		assertExpand(list("no"), "{#lt(this.y, 1) ? 'yes' : 'no'}", newA(2));
		assertExpand(list("no"), "{#lt(#int(this.s), 1) ? 'yes' : 'no'}", newA("2"));
		assertExpand(list("no"), "{#lt('a', 'a') ? 'yes' : 'no'}");
		assertExpand(list("yes"), "{#lt(1, 2) ? 'yes' : 'no'}");
	}

	public void testLe() throws ParseException {
		assertExpand(list("no"), "{#le(2, 1) ? 'yes' : 'no'}");
		assertExpand(list("no"), "{#le(this.y, 1) ? 'yes' : 'no'}", newA(2));
		assertExpand(list("no"), "{#le(#int(this.s), 1) ? 'yes' : 'no'}", newA("2"));
		assertExpand(list("yes"), "{#le('a', 'a') ? 'yes' : 'no'}");
		assertExpand(list("yes"), "{#le('a', 'ab') ? 'yes' : 'no'}");
	}

	public void testConvert() throws ParseException {
		assertExpand(list(1), "{#int(1)}");
		assertExpand(list(1), "{#int('1')}");
		assertExpand(list(1L), "{#long(1)}");
		assertExpand(list(1L), "{#long('1')}");
		assertExpand(list(1.0F), "{#float(1)}");
		assertExpand(list(1.0F), "{#float('1')}");
		assertExpand(list(1.0D), "{#double(1)}");
		assertExpand(list(1.0D), "{#double('1')}");
	}

	public void testFailFunctionCallWithArgumentMissmatch() throws ParseException {
		failExpand("requires exactly 2 arguments", "{#le(2) ? 'yes' : 'no'}");
		failExpand("requires exactly 2 arguments", "{#le(2, 3, 4) ? 'yes' : 'no'}");
		failExpand("Function 'sublist' requires at least 2 arguments", "{#sublist($a) ? 'yes' : 'no'}", var("a", list("foo")));
		failExpand("Invalid number of arguments", "{#sublist($a, 0, 1, 2) ? 'yes' : 'no'}", var("a", list("foo")));
	}

	public void testFailUnknownFunction() throws ParseException {
		failExpand("There is no function 'noSuchFunction'", "{#noSuchFunction() ? 'yes' : 'no'}");
	}

	public void testPropertyAccess() throws ParseException {
		assertExpand(list("bar"), "{$a.s}", var("a", newA("bar")));
		assertExpandNonNormative(list("bar"), "{property-with-long-name}", newA("bar"), var("a", "foo"));
	}

	public void testMandatoryPropertyAccess() throws ParseException {
		A bar = newA("bar");
		bar.setM("value");
		assertExpand(list("value"), "{$a.m}", var("a", bar));

		assertExpand(list(), "{$a.m}", var("a", newA("foo")));
	}

	public void testDirectPropertyAccess() throws ParseException {
		A a = newA("a");
		A b = newA("b");
		a.setA(b);
		A c = newA("c");
		b.setA(c);
		assertExpand(list("x", " ", "b", "."), "{this.a.a.s ? 'x' : 'y'} {this.a.s}.", a);
		assertExpand(list("y", " ", "c", "."), "{this.a.a.s ? 'x' : 'y'} {this.a.s}.", b);
	}

	public void testSelfAccess() throws ParseException {
		assertEquals(list("bar"), expand(newA("bar"), noVariables(), parse("{s}")));
		assertExpand(list("bar"), "{this.s}", newA("bar"));
	}

	public void testForeach() throws ParseException {
		List<String> expected = list("<", "(", "foo", ")", ",", "(", "bar", ")", ">");
		String template = "{foreach($c, {,}, {({this})}, {!<}, {!>})}";
		assertExpand(expected, template, var("c", list("foo", "bar")));
		assertExpand(expected, template, var("c", new String[] { "foo", "bar" }));
	}

	public void testForeachSimple() throws ParseException {
		List<String> expected = list("<", "foo", ",", "bar", ">");
		String template = "{foreach($c, ',', this, '<', '>')}";
		assertExpand(expected, template, var("c", list("foo", "bar")));
		assertExpand(expected, template, var("c", new Object[] { "foo", "bar" }));
	}

	public void testForeachSimpleVar() throws ParseException {
		List<String> expected = list("<", "foo", ",", "bar", ">");
		String template = "{foreach(x : $c, ',', $x, '<', '>')}";
		assertExpand(expected, template, var("c", list("foo", "bar")));
		assertExpand(expected, template, var("c", new Object[] { "foo", "bar" }));
	}

	public void testForeachEmpty() throws ParseException {
		List<String> expected = list("<", ">");
		String template = "{foreach($c, {,}, {({this})}, {!<}, {!>})}";
		assertExpand(expected, template, var("c", list()));
		assertExpand(expected, template, var("c", new Object[0]));
	}

	public void testForeachEmptyVar() throws ParseException {
		List<String> expected = list("<", ">");
		String template = "{foreach(x : $c, {,}, {({$x})}, {!<}, {!>})}";
		assertExpand(expected, template, var("c", list()));
		assertExpand(expected, template, var("c", new Object[0]));
	}

	public void testForeachNull() throws ParseException {
		assertExpand(list("<", ">"), "{foreach($c, {,}, {({this})}, {!<}, {!>})}", var("c", null));
	}

	public void testForeachMap() throws ParseException {
		assertExpand(list("<", "(", "bar", ")", ">"), "{foreach($c, {,}, {({this})}, {!<}, {!>})}",
			var("c", Collections.singletonMap("foo", "bar")));
	}

	public void testForeachNested() throws ParseException {
		assertExpand(list("!", "?", "a", "1", ",", "a", "2", ";", "b", "1", ",", "b", "2", "!", "?"),
			"{$x}{$y | '?'}{foreach(x : $c1, ';', foreach(y : $c2, ',', {{$x}{$y}}))}{$x}{$y | '?'}",
			var("c1", new Object[] { "a", "b" }), var("c2", list("1", "2")), var("x", "!"), var("y", null));
	}

	public void testTemplateReference() throws ParseException {
		assertEquals(
			list("bar"),
			expand(null, noVariables(), parseNormative("{->'foo'}"), scope("foo", "{'bar'}")));
		assertEquals(
			list("bar"),
			expand(null, vars(var("a", "foo")), parseNormative("{->$a}"), scope("foo", "{'bar'}")));
		assertEquals(
			list("bar"),
			expand(null, vars(var("a", newA("foo"))), parseNormative("{->$a.property-with-long-name}"),
				scope("foo", "{'bar'}")));
	}

	private MapScope scope(String name, String template) throws ParseException {
		return new MapScope(Collections.singletonMap(name, parse(template)));
	}

	public void testFailPropertyAccessOnNonConfigurationItem() throws ParseException {
		failExpand("Cannot access properties of target value", "{$a.foo}",
			var("a", Collections.singletonMap("foo", "bar")));
	}

	public void testFailUndefinedPropertyAccess() throws ParseException {
		failExpand("No property 'foo' in configuration", "{$a.foo}", var("a", newA("foo")));
	}

	public void testFailUndefinedVariableAccess() throws ParseException {
		failExpand("There is no binding for the variable 'b'", "{$b}", var("a", 42));
	}

	public void testFailWrongCollectionIndex() throws ParseException {
		failExpand("List index must be a number", "{$a['bar']}", var("a", list("foo")));
	}

	public void testFailWrongCollectionType() throws ParseException {
		failExpand("Not an indexed value", "{$a[1]}", var("a", "foo"));
	}

	public void testFailWrongForeachType() throws ParseException {
		failExpand("Not a collection in foreach", "{foreach($a, ',', this)}", var("a", "foo"));
	}

	public void testFailNoSuchTemplate() throws ParseException {
		failExpand("No such template", "{->'foo'}");
	}

	private A newA(String s) {
		A result = TypedConfiguration.newConfigItem(A.class);
		result.setS(s);
		result.setPropertyWithLongName(s);
		return result;
	}

	private A newA(long y) {
		A result = TypedConfiguration.newConfigItem(A.class);
		result.setY(y);
		return result;
	}

	public static Var var(String name, Object value) {
		return new Var(name, value);
	}

	public static class Var {
		public final String _name;

		public final Object _value;

		public Var(String name, Object value) {
			_name = name;
			_value = value;
		}
	}

	public static Map<String, Object> vars(Var... vars) {
		HashMap<String, Object> result = new HashMap<>();
		for (Var var : vars) {
			result.put(var._name, var._value);
		}
		return result;
	}

	private void assertExpand(List<?> expected, String template) throws ParseException {
		assertExpand(expected, template, noVariables());
	}

	private void assertExpand(List<?> expected, String template, Var var) throws ParseException {
		assertExpand(expected, template, null, vars(var));
	}

	private void assertExpand(List<?> expected, String template, Object context) throws ParseException {
		assertExpand(expected, template, context, noVariables());
	}

	public static Map<String, Object> noVariables() {
		return Collections.<String, Object> emptyMap();
	}

	private void assertExpand(List<?> expected, String template, Var... vars)
			throws ParseException {
		assertExpand(expected, template, null, vars(vars));
	}

	private void assertExpandNonNormative(List<?> expected, String template, Object context,
			Var... vars) throws ParseException {
		assertExpandNonNormative(expected, template, context, vars(vars));
	}

	private void assertExpandNonNormative(List<?> expected, String template, Object context,
			Map<String, Object> variables) throws ParseException {
		assertExpand(expected, context, variables, parse(template));
	}

	private void assertExpand(List<?> expected, String template, Object context, Map<String, Object> variables)
			throws ParseException {
		assertExpand(expected, context, variables, parseNormative(template));
	}

	private void assertExpand(List<?> expected, Object context, Map<String, Object> variables,
			TemplateExpression expression) {
		assertEquals(expected, expand(context, variables, expression));
	}

	private void failExpand(String message, String template) throws ParseException {
		failExpand(message, template, null, noVariables());
	}

	private void failExpand(String message, String template, Var... vars) throws ParseException {
		failExpand(message, template, null, vars(vars));
	}

	private void failExpand(String message, String template, Object context, Map<String, Object> variables) throws ParseException {
		try {
			expand(context, variables, parseNormative(template));
			fail("Failure expected.");
		} catch (EvalException ex) {
			assertContains(message, ex.getMessage());
		}
	}

	private List<Object> expand(Object context, Map<String, Object> variables, TemplateExpression template) {
		return expand(context, variables, template, EmptyScope.INSTANCE);
	}

	public static List<Object> expand(Object context, Map<String, Object> variables, TemplateExpression template,
			TemplateScope scope) {
		BufferedOutput output = new BufferedOutput();
		Expand expand = new Expand(scope, output);
		template.visit(expand, new Eval.EvalContext(context, variables));
		return output.getBuffer();
	}

	private TemplateExpression parseNormative(String template) throws ParseException {
		TemplateExpression result = parse(template);
		assertEquals(template, result.toString());
		return result;
	}

	public static TemplateExpression parse(String template) throws ParseException {
		return new ConfigTemplateParser(new StringReader(template)).template();
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(TestTemplateExpression.class);
	}
}
