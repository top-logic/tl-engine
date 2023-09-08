/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.objects.inspect;

import static com.top_logic.knowledge.objects.inspect.condition.Conditions.*;
import static test.com.top_logic.basic.BasicTestCase.*;

import junit.framework.TestCase;

import test.com.top_logic.knowledge.dummy.DummyKnowledgeObject;

import com.top_logic.basic.NamedConstant;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.inspect.DecisionTree;
import com.top_logic.knowledge.objects.inspect.build.DecisionTreeBuilder;
import com.top_logic.knowledge.objects.inspect.condition.Condition;
import com.top_logic.knowledge.objects.inspect.condition.ConditionEvaluatorBuilder;
import com.top_logic.knowledge.objects.inspect.condition.DirectConditionEvaluator;
import com.top_logic.knowledge.objects.inspect.condition.Equals;
import com.top_logic.knowledge.objects.inspect.functions.GetAttribute;
import com.top_logic.knowledge.objects.inspect.functions.TypeOf;

/**
 * Test case for {@link DecisionTree}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDecisionTree extends TestCase {

	private MOClass _type1;

	private MOClass _type2;

	private MOAttributeImpl _attr1;

	private MOAttributeImpl _attr2;

	private MOClass _type3;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_type1 = new MOClassImpl("T1");
		_attr1 = new MOAttributeImpl("a1", MOPrimitive.STRING);
		_type1.addAttribute(_attr1);
		_type1.freeze();

		_type2 = new MOClassImpl("T2");
		_type2.setSuperclass(_type1);
		_attr2 = new MOAttributeImpl("a2", MOPrimitive.STRING);
		_type2.addAttribute(_attr2);
		_type2.freeze();

		_type3 = new MOClassImpl("T3");
		_type3.freeze();
	}

	public void testAnd() {
		assertEquals(and(), ifTrue());
		assertEquals(and(ifTrue()), ifTrue());
		assertEquals(and(ifTrue(), ifTrue()), ifTrue());
		assertEquals(and(ifTrue(), ifFalse()), ifFalse());
		assertEquals(and(ifEquals(TypeOf.INSTANCE, "foo")), ifEquals(TypeOf.INSTANCE, "foo"));
		assertEquals(and(ifTrue(), ifEquals(TypeOf.INSTANCE, "foo")), ifEquals(TypeOf.INSTANCE, "foo"));
		assertEquals(and(list(ifTrue(), ifEquals(TypeOf.INSTANCE, "foo"))), ifEquals(TypeOf.INSTANCE, "foo"));
	}

	public void testOr() {
		assertEquals(or(), ifFalse());
		assertEquals(or(ifTrue()), ifTrue());
		assertEquals(or(ifTrue(), ifTrue()), ifTrue());
		assertEquals(or(ifTrue(), ifFalse()), ifTrue());
		assertEquals(or(ifEquals(TypeOf.INSTANCE, "foo")), ifEquals(TypeOf.INSTANCE, "foo"));
		assertEquals(or(ifFalse(), ifEquals(TypeOf.INSTANCE, "foo")), ifEquals(TypeOf.INSTANCE, "foo"));
		assertEquals(or(list(ifFalse(), ifEquals(TypeOf.INSTANCE, "foo"))), ifEquals(TypeOf.INSTANCE, "foo"));
	}

	public void testAndBuild() throws DataObjectException {
		DecisionTreeBuilder<NamedConstant> builder = new DecisionTreeBuilder<>();
		
		Object value1 = "foo";
		Object value2 = "bar";
		Object value3 = "zzz";

		NamedConstant always1 = check(ifTrue());
		builder.add(ifTrue(), always1);

		NamedConstant always2 = check(ifTrue());
		builder.add(always2);

		Equals testType1 = ifEquals(TypeOf.INSTANCE, _type1);
		NamedConstant ifType1 = check(testType1);
		builder.add(testType1, ifType1);

		Equals testType2 = ifEquals(TypeOf.INSTANCE, _type2);
		NamedConstant ifType2 = check(testType2);
		builder.add(testType2, ifType2);

		Condition testValue1 = and(testType2, ifEquals(new GetAttribute(_attr1), value1));
		NamedConstant ifValue1 = check(testValue1);
		builder.add(testValue1, ifValue1);

		Condition testValue2 = and(testType2, ifEquals(new GetAttribute(_attr1), value2));
		NamedConstant ifValue2 = check(testValue2);
		builder.add(testValue2, ifValue2);
		
		Condition testValue3 = and(testType2, ifEquals(new GetAttribute(_attr2), value3));
		NamedConstant ifValue3 = check(testValue3);
		builder.add(testValue3, ifValue3);

		DecisionTree<NamedConstant> combined = builder.build();

		DummyKnowledgeObject o1 = DummyKnowledgeObject.item("o1", _type3);
		DummyKnowledgeObject o2 = DummyKnowledgeObject.item("o2", _type1);
		DummyKnowledgeObject o3 = DummyKnowledgeObject.item("o3", _type2);

		DummyKnowledgeObject o4 = DummyKnowledgeObject.item("o4", _type2);
		o4.setValue(_attr1, value1);

		DummyKnowledgeObject o5 = DummyKnowledgeObject.item("o5", _type2);
		o5.setValue(_attr1, value2);

		DummyKnowledgeObject o6 = DummyKnowledgeObject.item("o6", _type2);
		o6.setValue(_attr2, value3);

		assertEval(true, ifTrue(), o1);
		assertEval(false, ifFalse(), o1);
		assertEval(true, testValue1, o4);
		assertEval(false, testValue1, o5);

		Condition anyValue = or(testValue1, testValue2, testValue3);
		assertEval(true, anyValue, o4);
		assertEval(true, anyValue, o5);
		assertEval(true, anyValue, o6);
		assertEval(false, anyValue, o3);

		assertEquals(set(always1, always2), combined.getMatchSet(o1));
		assertEquals(set(always1, always2, ifType1), combined.getMatchSet(o2));
		assertEquals(set(always1, always2, ifType2), combined.getMatchSet(o3));
		assertEquals(set(always1, always2, ifType2, ifValue1), combined.getMatchSet(o4));
		assertEquals(set(always1, always2, ifType2, ifValue2), combined.getMatchSet(o5));
		assertEquals(set(always1, always2, ifType2, ifValue3), combined.getMatchSet(o6));
	}

	public void testOrBuild() throws DataObjectException {
		DecisionTreeBuilder<NamedConstant> builder = new DecisionTreeBuilder<>();
		NamedConstant condition1 = new NamedConstant("v1");

		String a1Value = "a1";
		DummyKnowledgeObject o1 = DummyKnowledgeObject.item("o1", _type1);
		o1.setValue(_attr1, a1Value);

		DummyKnowledgeObject o2 = DummyKnowledgeObject.item("o2", _type2);
		o2.setValue(_attr1, a1Value);

		DummyKnowledgeObject o3 = DummyKnowledgeObject.item("o3", _type3);

		Condition condition = and(
			or(
				ifEquals(TypeOf.INSTANCE, _type1),
				ifEquals(TypeOf.INSTANCE, _type2)),
			ifEquals(new GetAttribute(_attr1), a1Value));
		builder.add(condition, condition1);

		DecisionTree<NamedConstant> decision = builder.build();

		assertEquals(set(condition1), decision.getMatchSet(o1));
		assertEquals(set(condition1), decision.getMatchSet(o2));
		assertEquals(set(), decision.getMatchSet(o3));

		assertEval(true, condition, o1);
		assertEval(true, condition, o2);
		assertEval(false, condition, o3);
	}

	private void assertEval(boolean expected, Condition condition, KnowledgeItem item) {
		// The interpretative evaluation (inefficient for mass usage).
		assertEquals(expected, DirectConditionEvaluator.eval(condition, item));

		// The compiled evaluation (efficient for mass usage).
		assertEquals(expected, ConditionEvaluatorBuilder.build(condition).eval(item));
	}

	private NamedConstant check(Condition test) {
		return new NamedConstant(test.toString());
	}

}
