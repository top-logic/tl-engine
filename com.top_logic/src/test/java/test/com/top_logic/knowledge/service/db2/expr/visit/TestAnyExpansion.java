/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.expr.visit;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.expr.QueryStructureTester;

import com.top_logic.basic.Logger;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.db2.expr.transform.AnyExpansion;
import com.top_logic.knowledge.service.db2.expr.transform.ExpressionCompileProtocol;
import com.top_logic.knowledge.service.db2.expr.visit.ExpressionPrinter;
import com.top_logic.knowledge.service.db2.expr.visit.TypeBinding;

/**
 * Test case for {@link AnyExpansion}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestAnyExpansion extends AbstractDBKnowledgeBaseTest {
	
	public void testExpandAny() throws DataObjectException {
		SetExpression expr = filter(anyOf(B_NAME), eqBinary(attribute(A_NAME, A1_NAME), literal("bbb")));
		
		SetExpression result = expandAny(expr);
		
		Logger.debug(ExpressionPrinter.toString(expr), TestAnyExpansion.class);
		Logger.debug(ExpressionPrinter.toString(result), TestAnyExpansion.class);
	}

	private SetExpression expandAny(SetExpression expr) throws DataObjectException {
		TypeSystem typeSystem = typeSystem();

		ExpressionCompileProtocol log = new ExpressionCompileProtocol(new AssertProtocol());
		TypeBinding.bindTypes(typeSystem, log, expr);
		SetExpression result = AnyExpansion.expandAny(typeSystem, expr);
		
		QueryStructureTester.assertTree(result);
		
		return result;
	}

	/**
	 * Suite creation.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestAnyExpansion.class);
	}

}
