/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.expr.visit;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.Collections;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.data.DefaultDataObject;
import com.top_logic.dob.meta.DefaultTypeSystem;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.expr.transform.ExpressionCompileProtocol;
import com.top_logic.knowledge.service.db2.expr.visit.SimpleExpressionEvaluator;
import com.top_logic.knowledge.service.db2.expr.visit.TypeBinding;

/**
 * Test case for {@link SimpleExpressionEvaluator}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestExpressionEvaluator extends AbstractDBKnowledgeBaseTest {

	public void testEvalReferenceAttributes() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject e2 = newE("e2");
		e1.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, e2);
		e2.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, e1);
		commit(tx);

		Expression search = eqBinary(
			eval(
				reference(E_NAME, REFERENCE_POLY_CUR_GLOBAL_NAME),
				attribute(A_NAME, A1_NAME)),
			literal("e1"));
		bindTypes(search);

		assertTrue(SimpleExpressionEvaluator.matches(search, e2));
		assertFalse(SimpleExpressionEvaluator.matches(search, e1));
	}

	public void testFlexAttributes() throws DataObjectException {
		String flexAttrName = "SomeFunnyFlexAttributeName";
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		b1.setAttributeValue(flexAttrName, "b1");
		KnowledgeObject b2 = newB("b1");
		commit(tx);

		assertTrue(SimpleExpressionEvaluator.matches(
			eqBinary(flex(MOPrimitive.STRING, flexAttrName), attribute(B_NAME, A1_NAME)), b1));
		assertFalse(SimpleExpressionEvaluator.matches(
			eqBinary(flex(MOPrimitive.STRING, flexAttrName), attribute(B_NAME, A1_NAME)), b2));
		assertTrue(SimpleExpressionEvaluator.matches(isNull(flex(MOPrimitive.STRING, "someDifferentName")), b1));
		assertFalse(SimpleExpressionEvaluator.matches(isNull(flex(MOPrimitive.STRING, flexAttrName)), b1));

		assertTrue(SimpleExpressionEvaluator.matches(isNull(flex(MOPrimitive.STRING, "someDifferentName")), b2));
		assertTrue(SimpleExpressionEvaluator.matches(isNull(flex(MOPrimitive.STRING, flexAttrName)), b2));
	}

	public void testIsNull() {
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(isNull(literal("not null")), null));
	}

	public void testAnd() {
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(and(literal(Boolean.TRUE), literal(Boolean.TRUE)), null));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(and(literal(Boolean.TRUE), literal(Boolean.FALSE)), null));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(and(literal(Boolean.FALSE), literal(Boolean.TRUE)), null));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(and(literal(Boolean.FALSE), literal(Boolean.FALSE)), null));
	}
	
	public void testOr() {
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(or(literal(Boolean.TRUE), literal(Boolean.TRUE)), null));
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(or(literal(Boolean.TRUE), literal(Boolean.FALSE)), null));
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(or(literal(Boolean.FALSE), literal(Boolean.TRUE)), null));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(or(literal(Boolean.FALSE), literal(Boolean.FALSE)), null));
	}
	
	public void testNot() {
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(not(literal(Boolean.TRUE)), null));
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(not(literal(Boolean.FALSE)), null));
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(not(
			eqBinary(literal(3), literal(4))), null));
	}
	
	public void testGt() {
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(gt(literal(Integer.valueOf(1)), literal(Integer.valueOf(1))), null));
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(gt(literal(Integer.valueOf(2)), literal(Integer.valueOf(1))), null));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(gt(literal(Integer.valueOf(0)), literal(Integer.valueOf(1))), null));
	}
	
	public void testGe() {
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(ge(literal(Integer.valueOf(1)), literal(Integer.valueOf(1))), null));
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(ge(literal(Integer.valueOf(2)), literal(Integer.valueOf(1))), null));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(ge(literal(Integer.valueOf(0)), literal(Integer.valueOf(1))), null));
	}
	
	public void testLt() {
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(lt(literal(Integer.valueOf(1)), literal(Integer.valueOf(1))), null));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(lt(literal(Integer.valueOf(2)), literal(Integer.valueOf(1))), null));
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(lt(literal(Integer.valueOf(0)), literal(Integer.valueOf(1))), null));
	}
	
	public void testLe() {
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(le(literal(Integer.valueOf(1)), literal(Integer.valueOf(1))), null));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(le(literal(Integer.valueOf(2)), literal(Integer.valueOf(1))), null));
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(le(literal(Integer.valueOf(0)), literal(Integer.valueOf(1))), null));
	}
	
	public void testEqBinary() {
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(eqBinary(literal(Integer.valueOf(1)), literal(Integer.valueOf(1))), null));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(eqBinary(literal(Integer.valueOf(2)), literal(Integer.valueOf(1))), null));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(eqBinary(literal(Integer.valueOf(0)), literal(Integer.valueOf(1))), null));
		
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(eqBinary(literal("aaa"), literal("aaa")), null));
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(eqBinary(literal(new String("aaa")), literal(new String("aaa"))), null));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(eqBinary(literal("aaa"), literal("aaA")), null));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(eqBinary(literal("aaa"), literal("aab")), null));
	}
	
	public void testEqCi() {
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(eqCi(literal("aaa"), literal("aaa")), null));
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(eqCi(literal(new String("aaa")), literal(new String("aaa"))), null));
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(eqCi(literal("aaa"), literal("aaA")), null));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(eqCi(literal("aaa"), literal("aab")), null));
	}
	
	public void testInLiteralSet() {
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(inLiteralSet(literal("aaa"), Collections.EMPTY_SET), null));
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(inLiteralSet(literal("aaa"), set("aaa")), null));
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(inLiteralSet(literal("aaa"), set("bbb", "aaa")), null));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(inLiteralSet(literal("aaa"), set("bbb", "ccc")), null));
	}
	
	public void testAttribute() throws DataObjectException {
		MOClassImpl tClass = new MOClassImpl("T");
		MOAttributeImpl xAttr = new MOAttributeImpl("x", MOPrimitive.INTEGER);
		tClass.addAttribute(xAttr);
		MOAttributeImpl yAttr = new MOAttributeImpl("y", MOPrimitive.STRING);
		tClass.addAttribute(yAttr);
		tClass.freeze();
		
		DefaultDataObject baseObject = new DefaultDataObject(tClass);
		baseObject.setAttributeValue("x", Integer.valueOf(42));
		baseObject.setAttributeValue("y", "Hello World!");
		
		assertEquals(Integer.valueOf(42), SimpleExpressionEvaluator.evaluate(attribute(xAttr), baseObject));
		assertEquals("Hello World!", SimpleExpressionEvaluator.evaluate(attribute(yAttr), baseObject));
	}
	
	public void testInstanceOf() throws DataObjectException {
		String sName = "S";
		MOClassImpl sClass = new MOClassImpl(sName);
		sClass.freeze();
		
		String tName = "T";
		MOClassImpl tClass = new MOClassImpl(tName);
		tClass.setSuperclass(sClass);
		tClass.freeze();
		
		DefaultDataObject sObject = new DefaultDataObject(sClass);
		DefaultDataObject tObject = new DefaultDataObject(tClass);
		
		Expression hasTypeS = hasType(sName);
		Expression hasTypeT = hasType(tName);
		Expression instanceOfS = instanceOf(sName);
		Expression instanceOfT = instanceOf(tName);
		
		TypeSystem ts = new DefaultTypeSystem(list(sClass, tClass));
		
		// Bind type implementations.
		bindTypes(ts, hasTypeS);
		bindTypes(ts, hasTypeT);
		bindTypes(ts, instanceOfS);
		bindTypes(ts, instanceOfT);
		
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(hasTypeS, sObject));
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(hasTypeT, tObject));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(hasTypeS, tObject));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(hasTypeT, sObject));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(hasTypeT, null));
		
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(instanceOfS, sObject));
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(instanceOfT, tObject));
		assertEquals(Boolean.TRUE, SimpleExpressionEvaluator.evaluate(instanceOfS, tObject));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(instanceOfT, sObject));
		assertEquals(Boolean.FALSE, SimpleExpressionEvaluator.evaluate(instanceOfT, null));
	}

	private void bindTypes(TypeSystem ts, Expression expression) {
		ExpressionCompileProtocol log = new ExpressionCompileProtocol(new AssertProtocol());
		TypeBinding.bindTypes(ts, log, expression);
		log.checkErrors();
	}

	private void bindTypes(Expression expression) {
		bindTypes(typeSystem(), expression);
	}

	public static Test suite() {
		return suiteDefaultDB(TestExpressionEvaluator.class);
	}
	
}
