/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.benchmark;

import java.util.function.ToIntFunction;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.element.meta.benchmark.model.A;
import test.com.top_logic.element.meta.benchmark.model.BenchmarkA;
import test.com.top_logic.element.meta.benchmark.model.BenchmarkFactory;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Micro-benchmark of the fixed overhead of reading an attribute of a persistent object through
 * {@link TLObject#tValue(TLStructuredTypePart)} and {@link TLObject#tValueByName(String)}.
 *
 * <p>
 * The result is reported in nanoseconds per read, both to the log and to standard output.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@DeactivatedTest("Benchmark that must not be executed during the nightly build.")
public class TestBenchmarkAttributeRead extends BasicTestCase {

	private static final int OBJECT_CNT = 1000;

	private static final int READ_CNT = 1_000_000;

	private static final int WARMUP_CNT = 1_000_000;

	private static final int ROUNDS = 3;

	/**
	 * Upper bound for a single read that only detects a completely broken setup. Not a performance
	 * requirement.
	 */
	private static final double SANITY_NS_PER_OP = 50_000;

	private BenchmarkA[] _objects;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		BenchmarkFactory factory = BenchmarkFactory.getInstance();
		_objects = new BenchmarkA[OBJECT_CNT];
		try (Transaction tx = kb.beginTransaction()) {
			BenchmarkA target = factory.createA();
			target.setS1("target");
			for (int n = 0; n < OBJECT_CNT; n++) {
				A a = factory.createA();
				a.setS1("a" + n);
				a.setX(Long.valueOf(n));
				a.setRef(target);
				_objects[n] = a;
			}
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_objects = null;
		super.tearDown();
	}

	/**
	 * Measures reads of a string, a long and a reference attribute by part and by name.
	 */
	public void testRead() {
		TLClass type = BenchmarkFactory.getAType();
		TLStructuredTypePart partS1 = type.getPart(BenchmarkA.S1_ATTR);
		TLStructuredTypePart partX = type.getPart(BenchmarkA.X_ATTR);
		TLStructuredTypePart partRef = type.getPart(BenchmarkA.REF_ATTR);

		measure("tValue(s1)", obj -> hash(obj.tValue(partS1)));
		measure("tValue(x)", obj -> hash(obj.tValue(partX)));
		measure("tValue(ref)", obj -> hash(obj.tValue(partRef)));
		measure("tValueByName(s1)", obj -> hash(obj.tValueByName(BenchmarkA.S1_ATTR)));
	}

	private void measure(String name, ToIntFunction<TLObject> read) {
		run(read, WARMUP_CNT);

		long best = Long.MAX_VALUE;
		for (int round = 0; round < ROUNDS; round++) {
			long start = System.nanoTime();
			int check = run(read, READ_CNT);
			long elapsed = System.nanoTime() - start;
			best = Math.min(best, elapsed);

			// Use the result to prevent the reads from being optimized away.
			assertTrue(check != 42 || elapsed >= 0);
		}

		double nsPerOp = ((double) best) / READ_CNT;
		String message = "Attribute read benchmark " + name + ": " + String.format("%.1f", nsPerOp) + " ns/op (best of "
			+ ROUNDS + " rounds with " + READ_CNT + " reads)";
		Logger.info(message, TestBenchmarkAttributeRead.class);
		System.out.println(message);

		assertTrue("Read too slow: " + message, nsPerOp < SANITY_NS_PER_OP);
	}

	private int run(ToIntFunction<TLObject> read, int count) {
		BenchmarkA[] objects = _objects;
		int check = 0;
		for (int n = 0; n < count; n++) {
			check += read.applyAsInt(objects[n % OBJECT_CNT]);
		}
		return check;
	}

	private static int hash(Object value) {
		return value == null ? 0 : System.identityHashCode(value);
	}

	/**
	 * The test suite with the element setup.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(TestBenchmarkAttributeRead.class);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
