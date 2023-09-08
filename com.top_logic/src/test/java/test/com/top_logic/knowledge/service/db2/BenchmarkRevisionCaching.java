/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import junit.framework.Test;

import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.Logger;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;

/**
 * Benchmark testing performance of {@link Wrapper#getCreated()} lookups.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BenchmarkRevisionCaching extends AbstractDBKnowledgeBaseClusterTest {

	private static final long SECOND_NANOS = 1000000000L;

	public void testCaching() throws DataObjectException {

		long lastTime = System.nanoTime();
		for (int rev = 0; rev < 50; rev++) {
			Transaction tx = kbNode2().beginTransaction();
			for (int obj = 0; obj < 10000; obj++) {
				newBNode2("b-" + rev + "-" + obj);
			}
			tx.commit();

			long time = System.nanoTime();
			if (time - lastTime > SECOND_NANOS) {
				Logger.info("Created " + rev + " revisions.", BenchmarkRevisionCaching.class);
				lastTime = time;
			}
		}

		for (int n = 0; n < 2; n++) {
			int cnt = 0;
			for (BObj b : WrapperFactory.getWrappersForKOs(BObj.class, kb().getAllKnowledgeObjects(B_NAME))) {
				assertNotNull(b.getCreated());
				cnt++;

				long time = System.nanoTime();
				if (time - lastTime > SECOND_NANOS) {
					Logger.info("Inspected " + cnt + " objects.", BenchmarkRevisionCaching.class);
					lastTime = time;
				}
			}
		}

	}

	public static Test suite() {
		return suite(BenchmarkRevisionCaching.class, DatabaseTestSetup.DEFAULT_DB, DefaultTestFactory.INSTANCE);
	}

}
