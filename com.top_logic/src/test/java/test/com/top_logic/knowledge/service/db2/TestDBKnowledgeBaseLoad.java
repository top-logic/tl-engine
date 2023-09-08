/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.AObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.CObj;

import com.top_logic.basic.Log;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.H2Helper;
import com.top_logic.basic.sql.PostgreSQLHelper;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.BranchParam;
import com.top_logic.knowledge.search.RangeParam;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.db2.expr.transform.sql.SQLBuilder;

/**
 * Test case for the query result load strategy of {@link DBKnowledgeBase} and {@link SQLBuilder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDBKnowledgeBaseLoad extends AbstractDBKnowledgeBaseClusterTest {

	protected static final String CC_NAME = "CC";

	protected static final String HIDDEN_CLOB_TYPE = "hiddenClobAttr";

	@Override
	protected LocalTestSetup createSetup(Test self) {
		DBKnowledgeBaseTestSetup setup = new DBKnowledgeBaseClusterTestSetup(self);
		setup.addAdditionalTypes(new TypeProvider() {

			@Override
			public void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository) {
				createTypes(typeRepository);
			}

			private void createTypes(MORepository typeRepository) throws DataObjectException {
				MOClass CC = new MOKnowledgeItemImpl(CC_NAME);
				CC.setSuperclass((MOClass) typeRepository.getMetaObject(B_NAME));
				MOAttribute cc1 = new MOAttributeImpl(C1_NAME, MOPrimitive.CLOB);
				CC.addAttribute(cc1);
				MOAttribute cc2 = new MOAttributeImpl(C2_NAME, MOPrimitive.BLOB);
				CC.addAttribute(cc2);
				KnowledgeBaseTestScenarioImpl.setApplicationType(CC, SimpleWrapperFactoryTestScenario.CObj.class);
				typeRepository.addMetaObject(CC);

				MOClass hiddenClobType = new MOKnowledgeItemImpl(HIDDEN_CLOB_TYPE);
				hiddenClobType.setSuperclass((MOClass) typeRepository.getMetaObject(B_NAME));
				MOAttributeImpl hiddenClobAttr = new MOAttributeImpl(C1_NAME, MOPrimitive.STRING);
				hiddenClobAttr.setSQLSize(100 * 1000);
				hiddenClobType.addAttribute(hiddenClobAttr);
				KnowledgeBaseTestScenarioImpl.setApplicationType(hiddenClobType,
					SimpleWrapperFactoryTestScenario.CObj.class);
				typeRepository.addMetaObject(hiddenClobType);
			}
		});
		return setup;
	}

	public void testNormalIdLoad() throws KnowledgeBaseException, RefetchTimeout {
		Set<AObj> expectedResult = normalSetup();
		kbNode2().refetch();

		RevisionQuery<AObj> query = findFlexFA(B_NAME).setIdPreload();
		List<AObj> result = searchNode2(query);

		checkResultSet(expectedResult, result);
	}

	public void testNormalFullLoad() throws KnowledgeBaseException, RefetchTimeout {
		Set<AObj> expectedResult = normalSetup();
		kbNode2().refetch();

		RevisionQuery<AObj> query = findFlexFA(B_NAME).setFullLoad();
		List<AObj> result = searchNode2(query);

		checkResultSet(expectedResult, result);
	}

	public void testNormalIdOrderedLoad() throws KnowledgeBaseException, RefetchTimeout {
		List<AObj> expectedResult = normalOrderedSetup();
		kbNode2().refetch();

		RevisionQuery<AObj> query = findFlexFAOrdered(B_NAME).setIdPreload();
		List<AObj> result = searchNode2(query);

		checkResultList(expectedResult, result);
	}

	public void testNormalFullOrderedLoad() throws KnowledgeBaseException, RefetchTimeout, SQLException {
		List<AObj> expectedResult = normalOrderedSetup();
		kbNode2().refetch();
		DBHelper sqlDialect = kbNode2().getConnectionPool().getSQLDialect();

		RevisionQuery<AObj> query = findFlexFAOrdered(B_NAME).setFullLoad();
		List<AObj> result;
		try {
			result = searchNode2(query);
		} catch (KnowledgeBaseRuntimeException ex) {
			if (sqlDialect instanceof H2Helper) {
				/* Exptected due to the known bug in ticket #17735: Distinct ordered queries need
				 * order column in result. */
				return;
			}
			if (sqlDialect instanceof PostgreSQLHelper) {
				/* Exptected due to the known bug in ticket #26511: Distinct ordered queries need
				 * order column in result when using a collation. */
				return;
			}
			throw ex;
		}
		if (sqlDialect instanceof PostgreSQLHelper) {
			fail(
				"Test should fail due to the known bug in ticket #26511: Distinct ordered queries need order column in result when using a collation.");
		}

		checkResultList(expectedResult, result);
	}

	public void testNormalIdBranchLoad() throws KnowledgeBaseException, RefetchTimeout {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Set<AObj> expectedResult = normalBranchSetup();
		kbNode2().refetch();

		RevisionQuery<AObj> query = findFlexFAOrdered(B_NAME).setBranchParam(BranchParam.all).setIdPreload();
		List<AObj> result = searchNode2(query);

		checkResultSet(expectedResult, result);
	}

	public void testNormalFullBranchLoad() throws KnowledgeBaseException, RefetchTimeout, SQLException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Set<AObj> expectedResult = normalBranchSetup();
		kbNode2().refetch();
		DBHelper sqlDialect = kbNode2().getConnectionPool().getSQLDialect();

		RevisionQuery<AObj> query = findFlexFAOrdered(B_NAME).setBranchParam(BranchParam.all).setFullLoad();
		List<AObj> result;
		try {
			result = searchNode2(query);
		} catch (KnowledgeBaseRuntimeException ex) {
			if (sqlDialect instanceof H2Helper) {
				/* Exptected due to the known bug in ticket #17735: Distinct ordered queries need
				 * order column in result. */
				return;
			}
			if (sqlDialect instanceof PostgreSQLHelper) {
				/* Exptected due to the known bug in ticket #26511: Distinct ordered queries need
				 * order column in result when using a collation. */
				return;
			}
			throw ex;
		}
		if (sqlDialect instanceof PostgreSQLHelper) {
			fail(
				"Test should fail due to the known bug in ticket #26511: Distinct ordered queries need order column in result when using a collation.");
		}

		checkResultSet(expectedResult, result);
	}

	public void testNormalFirstLoad() throws KnowledgeBaseException, RefetchTimeout {
		Set<AObj> expectedResult = normalSetup();

		RevisionQuery<AObj> query = findFlexFA(B_NAME).setRangeParam(RangeParam.first);
		List<AObj> result = searchNode2(query);

		checkResultSet(expectedResult, result);
	}

	private Set<AObj> normalSetup() throws KnowledgeBaseException, RefetchTimeout {
		Transaction tx = kb().beginTransaction();
		AObj b1 = newBObj("b1", "fa");
		AObj b2 = newBObj("b2", "fb");
		commitRefetch(tx);

		assertNotNull(b2);
		Set<AObj> expectedResult = set(node2Obj(b1));
		return expectedResult;
	}

	private List<AObj> normalOrderedSetup() throws KnowledgeBaseException, RefetchTimeout {
		Transaction tx = kb().beginTransaction();
		AObj b1 = newBObj("b1", "fa");
		AObj b2 = newBObj("b2", "fb");
		AObj b3 = newBObj("b3", "fa");
		AObj b4 = newBObj("b4", "fa");
		AObj b5 = newBObj("b5", "fa");
		commitRefetch(tx);

		assertNotNull(b2);
		List<AObj> expectedResult = list(node2Obj(b5), node2Obj(b4), node2Obj(b3), node2Obj(b1));
		return expectedResult;
	}

	private Set<AObj> normalBranchSetup() throws KnowledgeBaseException, RefetchTimeout {
		Transaction tx = kb().beginTransaction();
		AObj b1 = newBObj("b1", "fa");
		AObj b2 = newBObj("b2", "fb");
		tx.commit();

		Branch branch = kb().createBranch(kb().getTrunk(), tx.getCommitRevision(), null);
		kbNode2().refetch();

		assertNotNull(b2);
		Set<AObj> expectedResult = set(node2Obj(b1), node2Obj(onBranch(branch, b1)));
		return expectedResult;
	}

	public void testHiddenCLobIdLoad() throws KnowledgeBaseException, RefetchTimeout {
		Set<AObj> expectedResult = hiddenClobSetup();
		kbNode2().refetch();

		RevisionQuery<AObj> query = findFlexFA(HIDDEN_CLOB_TYPE).setIdPreload();
		List<AObj> result = searchNode2(query);

		checkResultSet(expectedResult, result);
	}

	public void testHiddenCLobFullLoad() throws KnowledgeBaseException, RefetchTimeout {
		Set<AObj> expectedResult = hiddenClobSetup();
		kbNode2().refetch();

		RevisionQuery<AObj> query = findFlexFA(HIDDEN_CLOB_TYPE).setFullLoad();
		List<AObj> result = searchNode2(query);

		checkResultSet(expectedResult, result);
	}

	public void testHiddenClobIdOrderedLoad() throws KnowledgeBaseException, RefetchTimeout {
		List<AObj> expectedResult = hiddenClobOrderedSetup();
		kbNode2().refetch();

		RevisionQuery<AObj> query = findFlexFAOrdered(HIDDEN_CLOB_TYPE).setIdPreload();
		List<AObj> result = searchNode2(query);

		checkResultList(expectedResult, result);
	}

	public void testHiddenClobFullOrderedLoad() throws KnowledgeBaseException, RefetchTimeout, SQLException {
		List<AObj> expectedResult = hiddenClobOrderedSetup();
		kbNode2().refetch();
		DBHelper sqlDialect = kbNode2().getConnectionPool().getSQLDialect();

		RevisionQuery<AObj> query = findFlexFAOrdered(HIDDEN_CLOB_TYPE).setFullLoad();
		List<AObj> result;
		try {
			result = searchNode2(query);
		} catch (KnowledgeBaseRuntimeException ex) {
			if (sqlDialect instanceof H2Helper) {
				/* Exptected due to the known bug in ticket #17735: Distinct ordered queries need
				 * order column in result. */
				return;
			}
			if (sqlDialect instanceof PostgreSQLHelper) {
				/* Exptected due to the known bug in ticket #26511: Distinct ordered queries need
				 * order column in result when using a collation. */
				return;
			}
			throw ex;
		}
		if (sqlDialect instanceof PostgreSQLHelper) {
			fail(
				"Test should fail due to the known bug in ticket #26511: Distinct ordered queries need order column in result when using a collation.");
		}

		checkResultList(expectedResult, result);
	}

	public void testHiddenCLobIdBranchLoad() throws KnowledgeBaseException, RefetchTimeout {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Set<AObj> expectedResult = hiddenClobBranchSetup();
		kbNode2().refetch();

		RevisionQuery<AObj> query = findFlexFAOrdered(HIDDEN_CLOB_TYPE).setBranchParam(BranchParam.all).setIdPreload();
		List<AObj> result = searchNode2(query);

		checkResultSet(expectedResult, result);
	}

	public void testHiddenCLobFullBranchLoad() throws KnowledgeBaseException, RefetchTimeout, SQLException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Set<AObj> expectedResult = hiddenClobBranchSetup();
		kbNode2().refetch();
		DBHelper sqlDialect = kbNode2().getConnectionPool().getSQLDialect();

		RevisionQuery<AObj> query = findFlexFAOrdered(HIDDEN_CLOB_TYPE).setBranchParam(BranchParam.all).setFullLoad();
		List<AObj> result;
		try {
			result = searchNode2(query);
		} catch (KnowledgeBaseRuntimeException ex) {
			if (sqlDialect instanceof H2Helper) {
				/* Exptected due to the known bug in ticket #17735: Distinct ordered queries need
				 * order column in result. */
				return;
			}
			if (sqlDialect instanceof PostgreSQLHelper) {
				/* Exptected due to the known bug in ticket #26511: Distinct ordered queries need
				 * order column in result when using a collation. */
				return;
			}
			throw ex;
		}
		if (sqlDialect instanceof PostgreSQLHelper) {
			fail(
				"Test should fail due to the known bug in ticket #26511: Distinct ordered queries need order column in result when using a collation.");
		}

		checkResultSet(expectedResult, result);
	}

	public void testHiddenCLobFirstLoad() throws KnowledgeBaseException, RefetchTimeout {
		Set<AObj> expectedResult = hiddenClobSetup();
		kbNode2().refetch();

		RevisionQuery<AObj> query = findFlexFA(HIDDEN_CLOB_TYPE).setRangeParam(RangeParam.first);
		List<AObj> result = searchNode2(query);

		checkResultSet(expectedResult, result);
	}

	private Set<AObj> hiddenClobSetup() throws KnowledgeBaseException, RefetchTimeout {
		Transaction tx = kb().beginTransaction();
		AObj c1 = newHiddenClobObj("c1", "c1-clob", "fa");
		AObj c2 = newHiddenClobObj("c2", "c1-clob", "fb");
		commitRefetch(tx);

		assertNotNull(c2);
		Set<AObj> expectedResult = set(node2Obj(c1));
		return expectedResult;
	}

	private List<AObj> hiddenClobOrderedSetup() throws KnowledgeBaseException, RefetchTimeout {
		Transaction tx = kb().beginTransaction();
		AObj b1 = newHiddenClobObj("b1", "clob1", "fa");
		AObj b2 = newHiddenClobObj("b2", "clob2", "fb");
		AObj b3 = newHiddenClobObj("b3", "clob3", "fa");
		AObj b4 = newHiddenClobObj("b4", "clob4", "fa");
		AObj b5 = newHiddenClobObj("b5", "clob5", "fa");
		commitRefetch(tx);

		assertNotNull(b2);
		List<AObj> expectedResult = list(node2Obj(b5), node2Obj(b4), node2Obj(b3), node2Obj(b1));
		return expectedResult;
	}

	private Set<AObj> hiddenClobBranchSetup() throws KnowledgeBaseException, RefetchTimeout {
		Transaction tx = kb().beginTransaction();
		AObj b1 = newHiddenClobObj("b1", "clob1", "fa");
		AObj b2 = newHiddenClobObj("b2", "clob2", "fb");
		tx.commit();

		Branch branch = kb().createBranch(kb().getTrunk(), tx.getCommitRevision(), null);
		kbNode2().refetch();

		assertNotNull(b2);
		Set<AObj> expectedResult = set(node2Obj(b1), node2Obj(onBranch(branch, b1)));
		return expectedResult;
	}

	public void testCLobIdLoad() throws KnowledgeBaseException, RefetchTimeout {
		Set<AObj> expectedResult = clobSetup();
		kbNode2().refetch();

		RevisionQuery<AObj> query = findFlexFA(CC_NAME).setIdPreload();
		List<AObj> result = searchNode2(query);

		checkResultSet(expectedResult, result);
	}

	public void testCLobFullLoad() throws KnowledgeBaseException, RefetchTimeout {
		Set<AObj> expectedResult = clobSetup();
		kbNode2().refetch();

		RevisionQuery<AObj> query = findFlexFA(CC_NAME).setFullLoad();
		List<AObj> result = searchNode2(query);

		checkResultSet(expectedResult, result);
	}

	public void testClobIdOrderedLoad() throws KnowledgeBaseException, RefetchTimeout {
		List<AObj> expectedResult = clobOrderedSetup();
		kbNode2().refetch();

		RevisionQuery<AObj> query = findFlexFAOrdered(CC_NAME).setIdPreload();
		List<AObj> result = searchNode2(query);

		checkResultList(expectedResult, result);
	}

	public void testClobFullOrderedLoad() throws KnowledgeBaseException, RefetchTimeout {
		List<AObj> expectedResult = clobOrderedSetup();
		kbNode2().refetch();

		RevisionQuery<AObj> query = findFlexFAOrdered(CC_NAME).setFullLoad();
		List<AObj> result = searchNode2(query);

		checkResultList(expectedResult, result);
	}

	public void testCLobIdBranchLoad() throws KnowledgeBaseException, RefetchTimeout {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Set<AObj> expectedResult = clobBranchSetup();
		kbNode2().refetch();

		RevisionQuery<AObj> query = findFlexFAOrdered(CC_NAME).setBranchParam(BranchParam.all).setIdPreload();
		List<AObj> result = searchNode2(query);

		checkResultSet(expectedResult, result);
	}

	public void testCLobFullBranchLoad() throws KnowledgeBaseException, RefetchTimeout {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Set<AObj> expectedResult = clobBranchSetup();
		kbNode2().refetch();

		RevisionQuery<AObj> query = findFlexFAOrdered(CC_NAME).setBranchParam(BranchParam.all).setFullLoad();
		List<AObj> result = searchNode2(query);

		checkResultSet(expectedResult, result);
	}

	public void testCLobFirstLoad() throws KnowledgeBaseException, RefetchTimeout {
		Set<AObj> expectedResult = clobSetup();
		kbNode2().refetch();

		RevisionQuery<AObj> query = findFlexFA(CC_NAME).setRangeParam(RangeParam.first);
		List<AObj> result = searchNode2(query);

		checkResultSet(expectedResult, result);
	}

	private Set<AObj> clobSetup() throws KnowledgeBaseException, RefetchTimeout {
		Transaction tx = kb().beginTransaction();
		AObj c1 = newCCObj("c1", "c1-clob", "fa");
		AObj c2 = newCCObj("c2", "c1-clob", "fb");
		commitRefetch(tx);

		assertNotNull(c2);
		Set<AObj> expectedResult = set(node2Obj(c1));
		return expectedResult;
	}

	private List<AObj> clobOrderedSetup() throws KnowledgeBaseException, RefetchTimeout {
		Transaction tx = kb().beginTransaction();
		AObj b1 = newCCObj("b1", "clob1", "fa");
		AObj b2 = newCCObj("b2", "clob2", "fb");
		AObj b3 = newCCObj("b3", "clob3", "fa");
		AObj b4 = newCCObj("b4", "clob4", "fa");
		AObj b5 = newCCObj("b5", "clob5", "fa");
		commitRefetch(tx);

		assertNotNull(b2);
		List<AObj> expectedResult = list(node2Obj(b5), node2Obj(b4), node2Obj(b3), node2Obj(b1));
		return expectedResult;
	}

	private Set<AObj> clobBranchSetup() throws KnowledgeBaseException, RefetchTimeout {
		Transaction tx = kb().beginTransaction();
		AObj b1 = newCCObj("b1", "clob1", "fa");
		AObj b2 = newCCObj("b2", "clob2", "fb");
		tx.commit();

		Branch branch = kb().createBranch(kb().getTrunk(), tx.getCommitRevision(), null);
		kbNode2().refetch();

		assertNotNull(b2);
		Set<AObj> expectedResult = set(node2Obj(b1), node2Obj(onBranch(branch, b1)));
		return expectedResult;
	}

	private void commitRefetch(Transaction tx) throws KnowledgeBaseException, RefetchTimeout {
		tx.commit();
		kbNode2().refetch();
	}

	private void checkResultSet(Set<AObj> expectedResult, List<AObj> result) {
		assertEquals(expectedResult.size(), result.size());
		assertEquals(expectedResult, toSet(result));
	}

	private void checkResultList(List<AObj> expectedResult, List<AObj> result) {
		assertEquals(expectedResult.size(), result.size());
		assertEquals(expectedResult, result);
	}

	private RevisionQuery<AObj> findFlexFA(String type) {
		return queryResolved(filter(allOf(type), eqBinary(flex(MOPrimitive.STRING, AObj.F1_NAME), literal("fa"))),
			AObj.class);
	}

	private RevisionQuery<AObj> findFlexFAOrdered(String type) {
		return queryResolved(filter(allOf(type), eqBinary(flex(MOPrimitive.STRING, AObj.F1_NAME), literal("fa"))),
			order(attribute(A_NAME, A1_NAME), true), AObj.class);
	}

	private <T> List<T> searchNode2(RevisionQuery<T> query) {
		try {
			return kbNode2().search(query);
		} catch (KnowledgeBaseRuntimeException ex) {
			if (ex.getMessage().contains("ORA-00932")) {
				throw fail("Ticket #12920: Distinct CLOB load not supported with Oracle", ex);
			}
			throw ex;
		}
	}

	private AObj newBObj(String a1, String f1) {
		BObj result = BObj.newBObj(a1);
		result.setF1(f1);
		return result;
	}

	private AObj newCCObj(String a1, String cc1, String f1) {
		return newObj(CC_NAME, a1, cc1, f1);
	}

	private AObj newHiddenClobObj(String a1, String cc1, String f1) {
		return newObj(HIDDEN_CLOB_TYPE, a1, cc1, f1);
	}

	public AObj newObj(String typeName, String a1, String cc1, String f1) throws AssertionFailedError {
		{
			KnowledgeBase kb = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
			KnowledgeObject ko = kb.createKnowledgeObject(typeName);
			CObj result = (CObj) ko.getWrapper();
			result.setA1(a1);
			result.setValue(C1_NAME, cc1);
			result.setF1(f1);
			return result;
		}
	}

	public static Test suite() {
		return suite(TestDBKnowledgeBaseLoad.class);
	}

}
