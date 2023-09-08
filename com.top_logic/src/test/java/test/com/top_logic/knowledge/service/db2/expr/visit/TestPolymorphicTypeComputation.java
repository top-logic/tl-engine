/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.expr.visit;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.ExpectedFailure;
import test.com.top_logic.basic.ExpectedFailureProtocol;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.db2.expr.transform.ExpressionCompileProtocol;
import com.top_logic.knowledge.service.db2.expr.visit.PolymorphicTypeComputation;
import com.top_logic.knowledge.service.db2.expr.visit.TypeBinding;

/**
 * Test case for {@link PolymorphicTypeComputation}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestPolymorphicTypeComputation extends AbstractDBKnowledgeBaseTest {

	public void testIllegalAttributeAccess() {
		SetExpression expr = 
			filter(
				allOf(B_NAME),
				eqBinary(attribute(D_NAME, D1_NAME), literal("d1")));
		
		assertTypeErrors(expr);
	}
	
	public void testIllegalAttributeAccessOnReference() {
		SetExpression expr = 
			filter(
				filter(
					map(anyOf(AB_NAME), destination()),
					instanceOf(B_NAME)),
				eqBinary(attribute(D_NAME, D1_NAME), literal("d1")));
		assertTypeErrors(expr);
	}

	public void testIllegalReferenceAccess() {
		SetExpression expr = 
			map(
				anyOf(B_NAME),
				destination());
		
		assertTypeErrors(expr);
	}
	
	public void testAttributeAccessOnReference() {
		SetExpression expr = 
			filter(
				filter(
					map(
						anyOf(AB_NAME),
						destination()), 
					instanceOf(B_NAME)),
				eqBinary(
					attribute(B_NAME, B1_NAME),
					literal("b1")));
		
		assertHasTypes(expr);
	}
	
	private void assertTypeErrors(SetExpression expr) {
		TypeSystem ts = typeSystem();
		ExpressionCompileProtocol log = new ExpressionCompileProtocol(new AssertProtocol());
		
		TypeBinding.bindTypes(ts, log, expr);
		log.checkErrors();
		
		try {
			ExpressionCompileProtocol typeLog = new ExpressionCompileProtocol(new ExpectedFailureProtocol());
			PolymorphicTypeComputation.computeTypes(ts, typeLog, expr);
			typeLog.checkErrors();
			
			fail("Type error not reported.");
		} catch (ExpectedFailure ex) {
			// Expected.
		}
	}

	private void assertHasTypes(SetExpression expr) {
		TypeSystem ts = typeSystem();
		ExpressionCompileProtocol log = new ExpressionCompileProtocol(new AssertProtocol());
		
		TypeBinding.bindTypes(ts, log, expr);
		log.checkErrors();
		
		PolymorphicTypeComputation.computeTypes(ts, log, expr);
		log.checkErrors();
	}

	/**
	 * Suite creation.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestPolymorphicTypeComputation.class);
	}

}
