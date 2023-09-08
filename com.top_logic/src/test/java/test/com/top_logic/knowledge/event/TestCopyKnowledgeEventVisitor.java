/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.event;

import java.util.Random;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.LongID;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.CopyKnowledgeEventVisitor;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;

/**
 * Test for {@link CopyKnowledgeEventVisitor}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestCopyKnowledgeEventVisitor extends BasicTestCase {

	private final Random _r = new Random(651654L);

	public void testCopyObjectCreation() {
		ObjectCreation event = new ObjectCreation(rev(), newObjectBranchId());
		event.setValue("int", null, 321);
		event.setValue("string", null, "blub");
		event.setValue("ref", null, newObjectKey());
		assertEquals(event, CopyKnowledgeEventVisitor.INSTANCE.copy(event));
	}

	public void testCopyItemUpdate() {
		ItemUpdate event = new ItemUpdate(rev(), newObjectBranchId(), true);
		event.setValue("int", 32654, 321);
		event.setValue("string", "bla", "blub");
		event.setValue("ref", newObjectKey(), newObjectKey());
		assertEquals(event, CopyKnowledgeEventVisitor.INSTANCE.copy(event));
	}

	public void testCopyItemDeletion() {
		ItemDeletion event = new ItemDeletion(rev(), newObjectBranchId());
		event.setValue("int", 32654, null);
		event.setValue("string", "bla", null);
		event.setValue("ref", newObjectKey(), null);
		assertEquals(event, CopyKnowledgeEventVisitor.INSTANCE.copy(event));
	}

	public void testCopyBranchEvent() {
		BranchEvent event = new BranchEvent(rev(), 651, 4, 3218);
		event.setBranchedTypeNames(set("type1", "type2"));
		assertEquals(event, CopyKnowledgeEventVisitor.INSTANCE.copy(event));
	}

	public void testCopyCommit() {
		CommitEvent event = new CommitEvent(rev(), TestCopyKnowledgeEventVisitor.class.getName(),
			System.currentTimeMillis(), "some log message");
		assertEquals(event, CopyKnowledgeEventVisitor.INSTANCE.copy(event));
	}

	private ObjectBranchId newObjectBranchId() {
		return new ObjectBranchId(_r.nextLong(), new DeferredMetaObject("testMO"), LongID.valueOf(_r.nextLong()));
	}

	private ObjectKey newObjectKey() {
		return newObjectBranchId().toCurrentObjectKey();
	}

	private long rev() {
		return 5;
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestCopyKnowledgeEventVisitor}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestCopyKnowledgeEventVisitor.class);
	}

}
