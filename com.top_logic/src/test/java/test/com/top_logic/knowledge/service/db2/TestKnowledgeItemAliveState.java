/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DeletedObjectAccess;
import com.top_logic.knowledge.service.db2.UpdateChainLink;

/**
 * Test case for checking different alive states of the {@link KnowledgeItem}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestKnowledgeItemAliveState extends AbstractDBKnowledgeBaseClusterTest {

	public void testNewDeleted() {
		Transaction tx1 = begin();
		
		BObj b1 = BObj.newBObj("b1");
		checkAlive(b1);
		b1.tDelete();

		try {
			checkAlive(b1);
			fail("New object is deleted.");
		} catch (DeletedObjectAccess ex) {
			// expected
			Logger.info("Deleted object: " + b1, TestKnowledgeItemAliveState.class);
		}
		Logger.info("Deleted object: " + b1, TestKnowledgeItemAliveState.class);
		rollback(tx1);
	}

	public void testAccessInForeignContext() throws InterruptedException {
		AtomicReference<BObj> foreignItem = new AtomicReference<>();
		try (SequentialBarrier barrier = new SequentialBarrier(TimeUnit.SECONDS.toMicros(1))) {
			inParallel(() -> {
				barrier.step(0);
				Transaction tx1 = begin();

				foreignItem.set(BObj.newBObj("b1"));
				barrier.step(2);
				commit(tx1);
			});
			barrier.step(1);
			try {
				checkAlive(foreignItem.get());
				fail("Must not access object created in foreign thread.");
			} catch (DeletedObjectAccess ex) {
				// expected
				Logger.info("Deleted object: " + foreignItem.get(), TestKnowledgeItemAliveState.class);
			}
		}
	}

	public void testLocallyDeleted() {
		Transaction tx1 = begin();

		BObj b1 = BObj.newBObj("b1");
		commit(tx1);

		Transaction tx2 = begin();
		b1.tDelete();
		try {
			checkAlive(b1);
			fail("Object locally deleted.");
		} catch (DeletedObjectAccess ex) {
			// expected
			Logger.info("Deleted object: " + b1, TestKnowledgeItemAliveState.class);
		}
		commit(tx2);
	}

	public void testAccessDeleted() {
		UpdateChainLink beforeCreation = getLastSessionRevision(kb());

		Transaction tx1 = begin();

		BObj b1 = BObj.newBObj("b1");
		commit(tx1);

		Transaction tx2 = begin();
		b1.tDelete();
		commit(tx2);

		try {
			checkAlive(b1);
			fail("Object deleted.");
		} catch (DeletedObjectAccess ex) {
			// expected
			Logger.info("Deleted object: " + b1, TestKnowledgeItemAliveState.class);
		}
		updateSessionRevision(beforeCreation);
		try {
			checkAlive(b1);
			fail("Object not yet created.");
		} catch (DeletedObjectAccess ex) {
			// expected
			Logger.info("Deleted object: " + b1, TestKnowledgeItemAliveState.class);
		}
	}

	void checkAlive(BObj item) {
		// Workaround: alive check is not accessible direct. Accessing any attribute triggers check.
		item.getA1();
	}

	public static Test suite() {
		return suite(TestKnowledgeItemAliveState.class);
	}

}
