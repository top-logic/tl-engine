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

import com.top_logic.basic.DateUtil;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.expr.sym.AttributeSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.ItemSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.ReferenceSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.Symbol;
import com.top_logic.knowledge.service.db2.expr.sym.TableSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.TupleSymbol;
import com.top_logic.knowledge.service.db2.expr.transform.ExpressionCompileProtocol;
import com.top_logic.knowledge.service.db2.expr.visit.ConcreteTypeComputation;
import com.top_logic.knowledge.service.db2.expr.visit.PolymorphicTypeComputation;
import com.top_logic.knowledge.service.db2.expr.visit.SymbolCreator;
import com.top_logic.knowledge.service.db2.expr.visit.TypeBinding;

/**
 * Test case for {@link SymbolCreator}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestSymbolCreator extends AbstractDBKnowledgeBaseTest {

	/**
	 * Test assignment of symbols of a simple expression.
	 */
	public void testItemSymbols() {
		Expression a1;
		SetExpression b;
		SetExpression expr = 
			filter(b = allOf(B_NAME),
				eqBinary(a1 = attribute(A_NAME, A1_NAME), literal("b1")));

		computeSymbols(expr);
		
		Symbol bSymbol = b.getSymbol();
		Symbol a1Symbol = a1.getSymbol();
		
		assertInstanceof(a1Symbol, AttributeSymbol.class);
		assertEquals(A1_NAME, ((AttributeSymbol) a1Symbol).getAttributeName());
		assertSame(bSymbol, ((AttributeSymbol) a1Symbol).getParent());
		assertSame(b, ((AttributeSymbol) a1Symbol).getParent().getDefinition());
		assertEquals(B_NAME, ((AttributeSymbol) a1Symbol).getParent().getType().getName());
	}
	
	/**
	 * Test assignment of symbols to tuple expressions.
	 */
	public void testTupleSymbols() {
		Expression a1;
		Expression a2;
		SetExpression expr = 
			map(allOf(B_NAME), tuple(a1 = attribute(A_NAME, A1_NAME), a2 = attribute(A_NAME, A2_NAME)));
		
		computeSymbols(expr);
		
		Symbol pSymbol = expr.getSymbol();
		Symbol a1Symbol = a1.getSymbol();
		Symbol a2Symbol = a2.getSymbol();
		
		assertInstanceof(pSymbol, TupleSymbol.class);
		assertInstanceof(a1Symbol, AttributeSymbol.class);
		assertInstanceof(a2Symbol, AttributeSymbol.class);
		assertEquals(a1Symbol, ((TupleSymbol) pSymbol).getSymbols().get(0));
		assertEquals(a2Symbol, ((TupleSymbol) pSymbol).getSymbols().get(1));
	}
	
	/**
	 * Test assignment of symbols to references.
	 */
	public void testReferenceSymbols() {
		SetExpression expr = 
			map(
				filter(
					crossProduct(
						allOf(B_NAME),
						filter(allOf(AB_NAME), eqBinary(attribute(AB_NAME, AB1_NAME), literal("ab1")))),
					eqBinary(getEntry(0), eval(getEntry(1), source()))),
				eval(getEntry(1), destination()));
		
		computeSymbols(expr);
		
		Symbol pSymbol = expr.getSymbol();
		
		assertInstanceof(pSymbol, ReferenceSymbol.class);
		assertEquals(AB_NAME, ((ReferenceSymbol) pSymbol).getParent().getType().getName());
	}
	
	/**
	 * Test attribute access to references.
	 */
	public void testNavigation() {
		Expression a2;
		SetExpression expr = 
			map(
				filter(allOf(AB_NAME),
					eval(destination(), and(hasType(B_NAME), eqBinary(a2 = attribute(A_NAME, A2_NAME), literal("v1"))))),
			   source());
		
		computeSymbols(expr);
		
		AttributeSymbol a2Symbol = (AttributeSymbol) a2.getSymbol();
		TableSymbol a2Context = (TableSymbol) a2Symbol.getParent();
		Symbol contextParent = a2Context.getParent();
		ItemSymbol symbol = (ItemSymbol) contextParent.getParent();
		assertEquals(AB_NAME, symbol.getType().getName());
	}
	
	/**
	 * Test with a scenario that has some semantics.
	 */
	public void testScenario() throws DataObjectException {
		String Milestone = X_NAME;
		String dueDate = X4_NAME;
		
		String hasMilestone = AB_NAME; // B -> X
		String isProjectLead = BC_NAME; // B -> C
		
		Comparable gestern = DateUtil.createDate(2009, 8, 29);
		Comparable inDreiWochen = DateUtil.createDate(2009, 8, 29 + 3*7);
		Object nutzer;
		{
			Transaction tx = begin();
			nutzer = newC("person");
			commit(tx);
		}
		
		SetExpression importantMilestones = 
			filter(
				allOf(Milestone),
				attributeRange(Milestone, dueDate, gestern, inDreiWochen));
		
		SetExpression myProjects = 
			map(
				filter(
					allOf(isProjectLead), 
					eqBinary(destination(), literal(nutzer))),
				source());
		
		SetExpression expr =
			filter(
				map(
					filter(
						allOf(hasMilestone),
						and(
							inSet(source(), myProjects),
							inSet(destination(), importantMilestones))),
					destination()),
				hasType(Milestone));

		computeSymbols(expr);
		
		assertEquals(types(Milestone), expr.getConcreteTypes());
	}
	
	private void computeSymbols(SetExpression expr) {
		TypeSystem typeSystem = (TypeSystem) kb().getMORepository();
		ExpressionCompileProtocol log = new ExpressionCompileProtocol(new AssertProtocol());
		
		computeSymbols(typeSystem, log, expr);
	}

	public static void computeSymbols(TypeSystem typeSystem, ExpressionCompileProtocol log, SetExpression expr) {
		TypeBinding.bindTypes(typeSystem, log, expr);
		log.checkErrors();
		
		PolymorphicTypeComputation.computeTypes(typeSystem, log, expr);
		log.checkErrors();
		
		ConcreteTypeComputation.computeTypes(typeSystem, log, expr);
		log.checkErrors();
		
		SymbolCreator.computeSymbols(typeSystem, log, queryUnresolved(expr));
		log.checkErrors();
	}

	/**
	 * Suite creation.
	 */
	@SuppressWarnings("unused")
	public static Test suite() {
		if (false) {
			return runOneTest(TestSymbolCreator.class, "");
		}
		return suite(TestSymbolCreator.class);
	}

}
