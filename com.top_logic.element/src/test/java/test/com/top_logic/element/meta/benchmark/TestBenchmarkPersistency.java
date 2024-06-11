/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.benchmark;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.element.meta.benchmark.model.BenchmarkA;
import test.com.top_logic.element.meta.benchmark.model.BenchmarkB;
import test.com.top_logic.element.meta.benchmark.model.BenchmarkFactory;
import test.com.top_logic.element.meta.benchmark.model.BenchmarkResult;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;

/**
 * Benchmarking object persistence operations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestBenchmarkPersistency extends BasicTestCase {

	private static final int CREATE_CNT = 256;

	private static final long MAX_TIME = 5000;

	KnowledgeBase kb;
	BenchmarkFactory fac;

	/* Save results:
	 * 
	 * insert into benchmark.result SELECT 'kb', TEST_NAME, OBJECT_CNT, MILLIS_PER_OPERATION FROM element.benchmark_result b;
	 * 
	 * 
	 * Compare results:
	 * 
	 * SELECT r1.TEST_NAME, r1.OBJECT_CNT, r1.MILLIS_PER_OPERATION, r2.MILLIS_PER_OPERATION FROM `benchmark`.result as r1, benchmark.result as r2 where r1.test='kb' and r2.test='vkb.partial-branches' and r1.TEST_NAME = r2.TEST_NAME and r1.OBJECT_CNT = r2.OBJECT_CNT
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		kb = PersistencyLayer.getKnowledgeBase();
		fac = BenchmarkFactory.getInstance();
		kb.commit();
	}
	
	@Override
	protected void tearDown() throws Exception {
		kb = null;
		fac = null;
		
		super.tearDown();
	}
	
	public void testFunction() {
		BenchmarkA a = newA("a1");
		
		BenchmarkB b1 = newB("b1");
		BenchmarkB b2 = newB("b2");
		BenchmarkB b3 = newB("b3");
		BenchmarkB b4 = newB("b4");
		BenchmarkB b5 = newB("b5");
		BenchmarkB b6 = newB("b6");
		
		List<BenchmarkA> collectionValue = Arrays.asList(new BenchmarkA[] { b6, b4, b2, b5, b3, b1 });
		
		a.setRef(b1);
		a.setRefSet(toSet(collectionValue));
		a.setRefList(collectionValue);
		
		assertTrue(kb.commit());
		
		assertEquals(b1, a.getRef());
		assertEquals(collectionValue, a.getRefList());
		assertTrue(a.getRefSet().containsAll(collectionValue));
	}
	
	public void testPlainCreate() throws Throwable {
		for (int cnt = 8; cnt <= CREATE_CNT; cnt*=2) {
			long elapsed = -System.currentTimeMillis();
			for (int n = 0; n < cnt; n++) {
				newA("create" + n);
			}
			assertTrue(kb.commit());
			elapsed += System.currentTimeMillis();
			
			reportResult("create.plain", cnt, (((double) elapsed) / cnt));
			
			if (elapsed > MAX_TIME) {
				break;
			}
		}
	}
	
	public void testSparselyLinked() throws Throwable {
		doTestLinked(false, true);
	}

	public void testHeavilyLinked() throws Throwable {
		doTestLinked(true, true);
	}
	
	public void testSparselyUncommittedLinked() throws Throwable {
		doTestLinked(false, false);
	}
	
	public void testHeavilyUncommittedLinked() throws Throwable {
		doTestLinked(true, false);
	}
	
	private void doTestLinked(boolean heavy, boolean committed) throws Throwable {
		String mod = heavy ? ".heavily" : "";
		if (! committed) {
			mod += ".uncommitted";
		}
		
		for (int cnt = 8; cnt <= CREATE_CNT; cnt*=2) {
			BenchmarkA lastA = null;
			{
				long elapsed = -System.currentTimeMillis();
				BenchmarkA[] as = new BenchmarkA[cnt];
				for (int n = 0; n < cnt; n++) {
					BenchmarkA newA = newA("create" + n);
					
					newA.setRef(lastA);
					lastA = newA;
					as[n] = newA;
				}
				
				if (heavy) {
					Set<BenchmarkA> allObjects = new HashSet<>(Arrays.asList(as));
					for (int n = 0; n < cnt; n++) {
						as[n].setRefSet(allObjects);
					}
				}
				
				if (committed) {
					assertTrue(kb.commit());
					elapsed += System.currentTimeMillis();
					
					reportResult("create.linked" + mod, cnt, (((double) elapsed) / cnt));
					if (elapsed > MAX_TIME) {
						break;
					}
				}
			}
			
			{
				long elapsed = -System.currentTimeMillis();
				int derefCnt = 0;
				BenchmarkA test = lastA;
				while (test != null) {
					test = test.getRef();
					derefCnt++;
				}
				elapsed += System.currentTimeMillis();
				
				reportResult("navigate" + mod, cnt, (((double) elapsed) / cnt));
				assertEquals(cnt, derefCnt);
				if (elapsed > MAX_TIME) {
					break;
				}
			}
			
			BenchmarkA firstA = null;
			{
				long elapsed = -System.currentTimeMillis();
				int derefCnt = 0;
				BenchmarkA test = lastA;
				while (test != null) {
					firstA = test;
					test = test.getRef();
					derefCnt++;
				}
				elapsed += System.currentTimeMillis();
				
				reportResult("navigate" + mod + ".cached", cnt, (((double) elapsed) / cnt));
				assertEquals(cnt, derefCnt);
				if (elapsed > MAX_TIME) {
					break;
				}
			}
			
			{
				long elapsed = -System.currentTimeMillis();
				int derefCnt = 0;
				BenchmarkA test = firstA;
				while (test != null) {
					test = test.getBackRef();
					derefCnt++;
				}
				elapsed += System.currentTimeMillis();
				
				reportResult("navigate" + mod + ".reverse", cnt, (((double) elapsed) / cnt));
				assertEquals(cnt, derefCnt);
				if (elapsed > MAX_TIME) {
					break;
				}
			}
			
			if (! committed) {
				kb.rollback();
			}
		}
	}

	private void reportResult(final String testName, final int cnt, final double millisPerOperation) throws Throwable {
		Logger.info(testName + ": " + millisPerOperation + "ms per operation (total " + cnt + " operations)", TestBenchmarkPersistency.class);
		
		// Commit in other thread / context to not disturb "uncommitted" tests.
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				newResult(testName, cnt, millisPerOperation);
				assertTrue(kb.commit());
			}
		});
	}
	
	void newResult(final String testName, final int cnt, final double millisPerOperation) {
		BenchmarkResult result = fac.createResult();
		result.setTestName(testName);
		result.setMillisPerOperation(Double.valueOf(millisPerOperation));
		result.setObjectCnt(Long.valueOf(cnt));
	}
	
	private BenchmarkA newA(String name) {
		BenchmarkA a1 = fac.createA();
		a1.setS1(name);
		return a1;
	}

	private BenchmarkB newB(String name) {
		BenchmarkB b1 = fac.createB();
		b1.setS1(name);
		return b1;
	}
	
    public static Test suite () {
		TestSuite theSuite = new TestSuite(TestBenchmarkPersistency.class);
        return ElementWebTestSetup.createElementWebTestSetup(theSuite);
    }

}
