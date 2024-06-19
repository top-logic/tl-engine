/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.structured.model.PreloadTest;
import test.com.top_logic.element.structured.model.TestTypesFactory;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.element.meta.kbbased.storage.ForeignKeyStorage;
import com.top_logic.element.meta.kbbased.storage.InlineListStorage;
import com.top_logic.element.meta.kbbased.storage.InlineSetStorage;
import com.top_logic.element.meta.kbbased.storage.ListStorage;
import com.top_logic.element.meta.kbbased.storage.ReverseForeignKeyStorage;
import com.top_logic.element.meta.kbbased.storage.SetStorage;
import com.top_logic.element.meta.kbbased.storage.SingletonLinkStorage;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.util.model.ModelService;

/**
 * Test {@link PreloadContribution} of reference storages.
 * 
 * @see StorageImplementation#getPreload()
 * @see StorageImplementation#getReversePreload()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestReferencePreload extends BasicTestCase {

	/**
	 * Tests {@link SetStorage#getPreload()}.
	 */
	public void testSetPreload() {
		PreloadTest p1;
		PreloadTest p2;
		PreloadTest p3;
		PreloadTest p4;
		PreloadTest p5;
		PreloadTest p6;
		Revision r1;
		try (Transaction tx = kb().beginTransaction()) {
			p1 = factory().createPreloadTest();
			p2 = factory().createPreloadTest();
			p3 = factory().createPreloadTest();
			p4 = factory().createPreloadTest();
			p5 = factory().createPreloadTest();
			p6 = factory().createPreloadTest();
			p1.setLinkSet(set(p3, p4));
			p2.setLinkSet(set(p5, p6));
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		try (Transaction tx = kb().beginTransaction()) {
			// Dummy revision.
			factory().createANode();
			tx.commit();
		}
		PreloadTest hP1 = inRev(p1, r1);
		PreloadTest hP2 = inRev(p2, r1);
		assertNull(inRevCacheOnly(p3, r1));
		assertNull(inRevCacheOnly(p4, r1));
		assertNull(inRevCacheOnly(p5, r1));
		assertNull(inRevCacheOnly(p6, r1));
		TLReference linkSetAttr = TestTypesFactory.getLinkSetPreloadTestAttr();
		assertInstanceof("Test for " + SetStorage.class.getName(),
			linkSetAttr.getStorageImplementation(),
			SetStorage.class);
		try (PreloadContext preloadCtx = new PreloadContext()) {
			MetaElementUtil.preloadAttribute(preloadCtx, set(hP1, hP2), linkSetAttr);
			PreloadTest hP3 = inRevCacheOnly(p3, r1);
			assertNotNull("No preload operation executed.", hP3);
			PreloadTest hP4 = inRevCacheOnly(p4, r1);
			assertNotNull("No preload operation executed.", hP4);
			PreloadTest hP5 = inRevCacheOnly(p5, r1);
			assertNotNull("No preload operation executed.", hP5);
			PreloadTest hP6 = inRevCacheOnly(p6, r1);
			assertNotNull("No preload operation executed.", hP6);
			assertEquals(set(hP3, hP4), hP1.getLinkSet());
			assertEquals(set(hP5, hP6), hP2.getLinkSet());
		}
	}

	/**
	 * Tests {@link SetStorage#getReversePreload()}.
	 */
	public void testSetReversePreload() {
		PreloadTest p1;
		PreloadTest p2;
		PreloadTest p3;
		PreloadTest p4;
		PreloadTest p5;
		PreloadTest p6;
		Revision r1;
		try (Transaction tx = kb().beginTransaction()) {
			p1 = factory().createPreloadTest();
			p2 = factory().createPreloadTest();
			p3 = factory().createPreloadTest();
			p4 = factory().createPreloadTest();
			p5 = factory().createPreloadTest();
			p6 = factory().createPreloadTest();
			p1.setLinkSet(set(p3, p4));
			p2.setLinkSet(set(p5, p6));
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		try (Transaction tx = kb().beginTransaction()) {
			// Dummy revision.
			factory().createANode();
			tx.commit();
		}
		PreloadTest hP3 = inRev(p3, r1);
		PreloadTest hP5 = inRev(p5, r1);
		assertNull(inRevCacheOnly(p1, r1));
		assertNull(inRevCacheOnly(p2, r1));
		TLReference linkSetAttr = TestTypesFactory.getLinkSetPreloadTestAttr();
		assertInstanceof("Test for " + SetStorage.class.getName(),
			linkSetAttr.getStorageImplementation(),
			SetStorage.class);

		try (PreloadContext preloadCtx = new PreloadContext()) {
			MetaElementUtil.reversePreloadAttribute(preloadCtx, set(hP3, hP5), linkSetAttr);
			PreloadTest hP1 = inRevCacheOnly(p1, r1);
			assertNotNull("No preload operation executed.", hP1);
			PreloadTest hP2 = inRevCacheOnly(p2, r1);
			assertNotNull("No preload operation executed.", hP2);
			assertEquals(set(hP1), hP3.tReferers(linkSetAttr));
			assertEquals(set(hP1), inRev(p4, r1).tReferers(linkSetAttr));
			assertEquals(set(hP2), hP5.tReferers(linkSetAttr));
			assertEquals(set(hP2), inRev(p6, r1).tReferers(linkSetAttr));
		}
	}

	/**
	 * Tests {@link InlineSetStorage#getPreload()}.
	 */
	public void testInlineSetPreload() {
		PreloadTest p1;
		PreloadTest p2;
		PreloadTest p3;
		PreloadTest p4;
		PreloadTest p5;
		PreloadTest p6;
		Revision r1;
		try (Transaction tx = kb().beginTransaction()) {
			p1 = factory().createPreloadTest();
			p2 = factory().createPreloadTest();
			p3 = factory().createPreloadTest();
			p4 = factory().createPreloadTest();
			p5 = factory().createPreloadTest();
			p6 = factory().createPreloadTest();
			p1.setCompositeSet(set(p3, p4));
			p2.setCompositeSet(set(p5, p6));
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		try (Transaction tx = kb().beginTransaction()) {
			// Dummy revision.
			factory().createANode();
			tx.commit();
		}
		PreloadTest hP1 = inRev(p1, r1);
		PreloadTest hP2 = inRev(p2, r1);
		assertNull(inRevCacheOnly(p3, r1));
		assertNull(inRevCacheOnly(p4, r1));
		assertNull(inRevCacheOnly(p5, r1));
		assertNull(inRevCacheOnly(p6, r1));
		TLReference inlineSetAttr = TestTypesFactory.getCompositeSetPreloadTestAttr();
		assertInstanceof("Test for " + InlineSetStorage.class.getName(),
			inlineSetAttr.getStorageImplementation(),
			InlineSetStorage.class);
		try (PreloadContext preloadCtx = new PreloadContext()) {
			MetaElementUtil.preloadAttribute(preloadCtx, set(hP1, hP2), inlineSetAttr);
			PreloadTest hP3 = inRevCacheOnly(p3, r1);
			assertNotNull("No preload operation executed.", hP3);
			PreloadTest hP4 = inRevCacheOnly(p4, r1);
			assertNotNull("No preload operation executed.", hP4);
			PreloadTest hP5 = inRevCacheOnly(p5, r1);
			assertNotNull("No preload operation executed.", hP5);
			PreloadTest hP6 = inRevCacheOnly(p6, r1);
			assertNotNull("No preload operation executed.", hP6);
			assertEquals(set(hP3, hP4), hP1.getCompositeSet());
			assertEquals(set(hP5, hP6), hP2.getCompositeSet());
		}
	}

	/**
	 * Tests {@link InlineSetStorage#getReversePreload()}.
	 */
	public void testInlineSetReversePreload() {
		PreloadTest p1;
		PreloadTest p2;
		PreloadTest p3;
		PreloadTest p4;
		PreloadTest p5;
		PreloadTest p6;
		Revision r1;
		try (Transaction tx = kb().beginTransaction()) {
			p1 = factory().createPreloadTest();
			p2 = factory().createPreloadTest();
			p3 = factory().createPreloadTest();
			p4 = factory().createPreloadTest();
			p5 = factory().createPreloadTest();
			p6 = factory().createPreloadTest();
			p1.setCompositeSet(set(p3, p4));
			p2.setCompositeSet(set(p5, p6));
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		try (Transaction tx = kb().beginTransaction()) {
			// Dummy revision.
			factory().createANode();
			tx.commit();
		}
		PreloadTest hP3 = inRev(p3, r1);
		PreloadTest hP5 = inRev(p5, r1);
		assertNull(inRevCacheOnly(p1, r1));
		assertNull(inRevCacheOnly(p2, r1));
		TLReference inlineSetAttr = TestTypesFactory.getCompositeSetPreloadTestAttr();
		assertInstanceof("Test for " + InlineSetStorage.class.getName(),
			inlineSetAttr.getStorageImplementation(),
			InlineSetStorage.class);

		try (PreloadContext preloadCtx = new PreloadContext()) {
			MetaElementUtil.reversePreloadAttribute(preloadCtx, set(hP3, hP5), inlineSetAttr);
			PreloadTest hP1 = inRevCacheOnly(p1, r1);
			assertNotNull("No preload operation executed.", hP1);
			PreloadTest hP2 = inRevCacheOnly(p2, r1);
			assertNotNull("No preload operation executed.", hP2);
			assertEquals(set(hP1), hP3.tReferers(inlineSetAttr));
			assertEquals(set(hP1), inRev(p4, r1).tReferers(inlineSetAttr));
			assertEquals(set(hP2), hP5.tReferers(inlineSetAttr));
			assertEquals(set(hP2), inRev(p6, r1).tReferers(inlineSetAttr));
		}
	}

	/**
	 * Tests {@link ListStorage#getPreload()}.
	 */
	public void testListPreload() {
		PreloadTest p1;
		PreloadTest p2;
		PreloadTest p3;
		PreloadTest p4;
		PreloadTest p5;
		PreloadTest p6;
		Revision r1;
		try (Transaction tx = kb().beginTransaction()) {
			p1 = factory().createPreloadTest();
			p2 = factory().createPreloadTest();
			p3 = factory().createPreloadTest();
			p4 = factory().createPreloadTest();
			p5 = factory().createPreloadTest();
			p6 = factory().createPreloadTest();
			p1.setLinkList(list(p3, p4));
			p2.setLinkList(list(p5, p6));
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		try (Transaction tx = kb().beginTransaction()) {
			// Dummy revision.
			factory().createANode();
			tx.commit();
		}
		PreloadTest hP1 = inRev(p1, r1);
		PreloadTest hP2 = inRev(p2, r1);
		assertNull(inRevCacheOnly(p3, r1));
		assertNull(inRevCacheOnly(p4, r1));
		assertNull(inRevCacheOnly(p5, r1));
		assertNull(inRevCacheOnly(p6, r1));
		TLReference linkListAttr = TestTypesFactory.getLinkListPreloadTestAttr();
		assertInstanceof("Test for " + ListStorage.class.getName(),
			linkListAttr.getStorageImplementation(),
			ListStorage.class);

		try (PreloadContext preloadCtx = new PreloadContext()) {
			MetaElementUtil.preloadAttribute(preloadCtx, set(hP1, hP2), linkListAttr);
			PreloadTest hP3 = inRevCacheOnly(p3, r1);
			assertNotNull("No preload operation executed.", hP3);
			PreloadTest hP4 = inRevCacheOnly(p4, r1);
			assertNotNull("No preload operation executed.", hP4);
			PreloadTest hP5 = inRevCacheOnly(p5, r1);
			assertNotNull("No preload operation executed.", hP5);
			PreloadTest hP6 = inRevCacheOnly(p6, r1);
			assertNotNull("No preload operation executed.", hP6);
			assertEquals(list(hP3, hP4), hP1.getLinkList());
			assertEquals(list(hP5, hP6), hP2.getLinkList());
		}
	}

	/**
	 * Tests {@link ListStorage#getReversePreload()}.
	 */
	public void testListReversePreload() {
		PreloadTest p1;
		PreloadTest p2;
		PreloadTest p3;
		PreloadTest p4;
		PreloadTest p5;
		PreloadTest p6;
		Revision r1;
		try (Transaction tx = kb().beginTransaction()) {
			p1 = factory().createPreloadTest();
			p2 = factory().createPreloadTest();
			p3 = factory().createPreloadTest();
			p4 = factory().createPreloadTest();
			p5 = factory().createPreloadTest();
			p6 = factory().createPreloadTest();
			p1.setLinkList(list(p3, p4));
			p2.setLinkList(list(p5, p6));
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		try (Transaction tx = kb().beginTransaction()) {
			// Dummy revision.
			factory().createANode();
			tx.commit();
		}
		PreloadTest hP3 = inRev(p3, r1);
		PreloadTest hP5 = inRev(p5, r1);
		assertNull(inRevCacheOnly(p1, r1));
		assertNull(inRevCacheOnly(p2, r1));
		TLReference linkListAttr = TestTypesFactory.getLinkListPreloadTestAttr();
		assertInstanceof("Test for " + ListStorage.class.getName(),
			linkListAttr.getStorageImplementation(),
			ListStorage.class);

		try (PreloadContext preloadCtx = new PreloadContext()) {
			MetaElementUtil.reversePreloadAttribute(preloadCtx, set(hP3, hP5), linkListAttr);
			PreloadTest hP1 = inRevCacheOnly(p1, r1);
			assertNotNull("No preload operation executed.", hP1);
			PreloadTest hP2 = inRevCacheOnly(p2, r1);
			assertNotNull("No preload operation executed.", hP2);
			assertEquals(set(hP1), hP3.tReferers(linkListAttr));
			assertEquals(set(hP1), inRev(p4, r1).tReferers(linkListAttr));
			assertEquals(set(hP2), hP5.tReferers(linkListAttr));
			assertEquals(set(hP2), inRev(p6, r1).tReferers(linkListAttr));
		}
	}

	/**
	 * Tests {@link InlineListStorage#getPreload()}.
	 */
	public void testInlineListPreload() {
		PreloadTest p1;
		PreloadTest p2;
		PreloadTest p3;
		PreloadTest p4;
		PreloadTest p5;
		PreloadTest p6;
		Revision r1;
		try (Transaction tx = kb().beginTransaction()) {
			p1 = factory().createPreloadTest();
			p2 = factory().createPreloadTest();
			p3 = factory().createPreloadTest();
			p4 = factory().createPreloadTest();
			p5 = factory().createPreloadTest();
			p6 = factory().createPreloadTest();
			p1.setCompositeList(list(p3, p4));
			p2.setCompositeList(list(p5, p6));
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		try (Transaction tx = kb().beginTransaction()) {
			// Dummy revision.
			factory().createANode();
			tx.commit();
		}
		PreloadTest hP1 = inRev(p1, r1);
		PreloadTest hP2 = inRev(p2, r1);
		assertNull(inRevCacheOnly(p3, r1));
		assertNull(inRevCacheOnly(p4, r1));
		assertNull(inRevCacheOnly(p5, r1));
		assertNull(inRevCacheOnly(p6, r1));
		TLReference inlineListAttr = TestTypesFactory.getCompositeListPreloadTestAttr();
		assertInstanceof("Test for " + InlineListStorage.class.getName(),
			inlineListAttr.getStorageImplementation(),
			InlineListStorage.class);

		try (PreloadContext preloadCtx = new PreloadContext()) {
			MetaElementUtil.preloadAttribute(preloadCtx, set(hP1, hP2), inlineListAttr);
			PreloadTest hP3 = inRevCacheOnly(p3, r1);
			assertNotNull("No preload operation executed.", hP3);
			PreloadTest hP4 = inRevCacheOnly(p4, r1);
			assertNotNull("No preload operation executed.", hP4);
			PreloadTest hP5 = inRevCacheOnly(p5, r1);
			assertNotNull("No preload operation executed.", hP5);
			PreloadTest hP6 = inRevCacheOnly(p6, r1);
			assertNotNull("No preload operation executed.", hP6);
			assertEquals(list(hP3, hP4), hP1.getCompositeList());
			assertEquals(list(hP5, hP6), hP2.getCompositeList());
		}
	}

	/**
	 * Tests {@link InlineListStorage#getReversePreload()}.
	 */
	public void testInlineListReversePreload() {
		PreloadTest p1;
		PreloadTest p2;
		PreloadTest p3;
		PreloadTest p4;
		PreloadTest p5;
		PreloadTest p6;
		Revision r1;
		try (Transaction tx = kb().beginTransaction()) {
			p1 = factory().createPreloadTest();
			p2 = factory().createPreloadTest();
			p3 = factory().createPreloadTest();
			p4 = factory().createPreloadTest();
			p5 = factory().createPreloadTest();
			p6 = factory().createPreloadTest();
			p1.setCompositeList(list(p3, p4));
			p2.setCompositeList(list(p5, p6));
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		try (Transaction tx = kb().beginTransaction()) {
			// Dummy revision.
			factory().createANode();
			tx.commit();
		}
		PreloadTest hP3 = inRev(p3, r1);
		PreloadTest hP5 = inRev(p5, r1);
		assertNull(inRevCacheOnly(p1, r1));
		assertNull(inRevCacheOnly(p2, r1));
		TLReference inlineListAttr = TestTypesFactory.getCompositeListPreloadTestAttr();
		assertInstanceof("Test for " + InlineListStorage.class.getName(),
			inlineListAttr.getStorageImplementation(),
			InlineListStorage.class);

		try (PreloadContext preloadCtx = new PreloadContext()) {
			MetaElementUtil.reversePreloadAttribute(preloadCtx, set(hP3, hP5), inlineListAttr);
			PreloadTest hP1 = inRevCacheOnly(p1, r1);
			assertNotNull("No preload operation executed.", hP1);
			PreloadTest hP2 = inRevCacheOnly(p2, r1);
			assertNotNull("No preload operation executed.", hP2);
			assertEquals(set(hP1), hP3.tReferers(inlineListAttr));
			assertEquals(set(hP1), inRev(p4, r1).tReferers(inlineListAttr));
			assertEquals(set(hP2), hP5.tReferers(inlineListAttr));
			assertEquals(set(hP2), inRev(p6, r1).tReferers(inlineListAttr));
		}
	}

	/**
	 * Tests {@link ForeignKeyStorage#getPreload()}.
	 */
	public void testForeignKeyPreload() {
		PreloadTest p1;
		PreloadTest p2;
		PreloadTest p3;
		PreloadTest p4;
		Revision r1;
		try (Transaction tx = kb().beginTransaction()) {
			p1 = factory().createPreloadTest();
			p2 = factory().createPreloadTest();
			p3 = factory().createPreloadTest();
			p4 = factory().createPreloadTest();
			p1.setInline(p3);
			p2.setInline(p4);
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		try (Transaction tx = kb().beginTransaction()) {
			// Dummy revision.
			factory().createANode();
			tx.commit();
		}
		PreloadTest hP1 = inRev(p1, r1);
		PreloadTest hP2 = inRev(p2, r1);
		assertNull(inRevCacheOnly(p3, r1));
		assertNull(inRevCacheOnly(p4, r1));
		TLReference inlineAttr = TestTypesFactory.getInlinePreloadTestAttr();
		assertInstanceof("Test for " + ForeignKeyStorage.class.getName(),
			inlineAttr.getStorageImplementation(),
			ForeignKeyStorage.class);

		try (PreloadContext preloadCtx = new PreloadContext()) {
			MetaElementUtil.preloadAttribute(preloadCtx, set(hP1, hP2), inlineAttr);
			PreloadTest hP3 = inRevCacheOnly(p3, r1);
			assertNotNull("No preload operation executed.", hP3);
			assertSame(hP3, hP1.getInline());
			PreloadTest hP4 = inRevCacheOnly(p4, r1);
			assertNotNull("No preload operation executed.", hP4);
			assertSame(hP4, hP2.getInline());
		}
	}

	/**
	 * Tests {@link ForeignKeyStorage#getReversePreload()}.
	 */
	public void testForeignKeyReversePreload() {
		PreloadTest p1;
		PreloadTest p2;
		PreloadTest p3;
		PreloadTest p4;
		PreloadTest p5;
		Revision r1;
		try (Transaction tx = kb().beginTransaction()) {
			p1 = factory().createPreloadTest();
			p2 = factory().createPreloadTest();
			p3 = factory().createPreloadTest();
			p4 = factory().createPreloadTest();
			p5 = factory().createPreloadTest();
			p1.setInline(p3);
			p2.setInline(p4);
			p5.setInline(p4);
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		try (Transaction tx = kb().beginTransaction()) {
			// Dummy revision.
			factory().createANode();
			tx.commit();
		}
		PreloadTest hP3 = inRev(p3, r1);
		PreloadTest hP4 = inRev(p4, r1);
		assertNull(inRevCacheOnly(p1, r1));
		assertNull(inRevCacheOnly(p2, r1));
		assertNull(inRevCacheOnly(p5, r1));
		TLReference inlineAttr = TestTypesFactory.getInlinePreloadTestAttr();
		assertInstanceof("Test for " + ForeignKeyStorage.class.getName(),
			inlineAttr.getStorageImplementation(),
			ForeignKeyStorage.class);

		try (PreloadContext preloadCtx = new PreloadContext()) {
			MetaElementUtil.reversePreloadAttribute(preloadCtx, set(hP3, hP4), inlineAttr);
			PreloadTest hP1 = inRevCacheOnly(p1, r1);
			assertNotNull("No preload operation executed.", hP1);
			PreloadTest hP2 = inRevCacheOnly(p2, r1);
			assertNotNull("No preload operation executed.", hP2);
			PreloadTest hP5 = inRevCacheOnly(p5, r1);
			assertNotNull("No preload operation executed.", hP5);

			assertEquals(set(hP1), hP3.tReferers(inlineAttr));
			assertEquals(set(hP2, hP5), hP4.tReferers(inlineAttr));
		}
	}

	/**
	 * Tests {@link ReverseForeignKeyStorage#getPreload()}.
	 */
	public void testReverseForeignKeyPreload() {
		PreloadTest p1;
		PreloadTest p2;
		PreloadTest p3;
		PreloadTest p4;
		Revision r1;
		try (Transaction tx = kb().beginTransaction()) {
			p1 = factory().createPreloadTest();
			p2 = factory().createPreloadTest();
			p3 = factory().createPreloadTest();
			p4 = factory().createPreloadTest();
			p1.setInlineReverse(p3);
			p2.setInlineReverse(p4);
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		try (Transaction tx = kb().beginTransaction()) {
			// Dummy revision.
			factory().createANode();
			tx.commit();
		}
		PreloadTest hP1 = inRev(p1, r1);
		PreloadTest hP2 = inRev(p2, r1);
		assertNull(inRevCacheOnly(p3, r1));
		assertNull(inRevCacheOnly(p4, r1));
		TLReference inlineReverseAttr = TestTypesFactory.getInlineReversePreloadTestAttr();
		assertInstanceof("Test for " + ReverseForeignKeyStorage.class.getName(),
			inlineReverseAttr.getStorageImplementation(),
			ReverseForeignKeyStorage.class);

		try (PreloadContext preloadCtx = new PreloadContext()) {
			MetaElementUtil.preloadAttribute(preloadCtx, set(hP1, hP2), inlineReverseAttr);
			PreloadTest hP3 = inRevCacheOnly(p3, r1);
			assertNotNull("No preload operation executed.", hP3);
			assertSame(hP3, hP1.getInlineReverse());
			PreloadTest hP4 = inRevCacheOnly(p4, r1);
			assertNotNull("No preload operation executed.", hP4);
			assertSame(hP4, hP2.getInlineReverse());
		}
	}

	/**
	 * Tests {@link ReverseForeignKeyStorage#getReversePreload()}.
	 */
	public void testReverseForeignKeyReversePreload() {
		PreloadTest p1;
		PreloadTest p2;
		PreloadTest p3;
		PreloadTest p4;
		Revision r1;
		try (Transaction tx = kb().beginTransaction()) {
			p1 = factory().createPreloadTest();
			p2 = factory().createPreloadTest();
			p3 = factory().createPreloadTest();
			p4 = factory().createPreloadTest();
			p1.setInlineReverse(p3);
			p2.setInlineReverse(p4);
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		try (Transaction tx = kb().beginTransaction()) {
			// Dummy revision.
			factory().createANode();
			tx.commit();
		}
		PreloadTest hP3 = inRev(p3, r1);
		PreloadTest hP4 = inRev(p4, r1);
		assertNull(inRevCacheOnly(p1, r1));
		assertNull(inRevCacheOnly(p2, r1));
		TLReference inlineReverseAttr = TestTypesFactory.getInlineReversePreloadTestAttr();
		assertInstanceof("Test for " + ReverseForeignKeyStorage.class.getName(),
			inlineReverseAttr.getStorageImplementation(),
			ReverseForeignKeyStorage.class);

		try (PreloadContext preloadCtx = new PreloadContext()) {
			MetaElementUtil.reversePreloadAttribute(preloadCtx, set(hP3, hP4), inlineReverseAttr);
			PreloadTest hP1 = inRevCacheOnly(p1, r1);
			assertNotNull("No preload operation executed.", hP1);
			PreloadTest hP2 = inRevCacheOnly(p2, r1);
			assertNotNull("No preload operation executed.", hP2);

			assertEquals(set(hP1), hP3.tReferers(inlineReverseAttr));
			assertEquals(set(hP2), hP4.tReferers(inlineReverseAttr));
		}
	}

	/**
	 * Tests {@link SingletonLinkStorage#getPreload()}.
	 */
	public void testSingletonLinkPreload() {
		PreloadTest p1;
		PreloadTest p2;
		PreloadTest p3;
		PreloadTest p4;
		Revision r1;
		try (Transaction tx = kb().beginTransaction()) {
			p1 = factory().createPreloadTest();
			p2 = factory().createPreloadTest();
			p3 = factory().createPreloadTest();
			p4 = factory().createPreloadTest();
			p1.setSingletonLink(p3);
			p2.setSingletonLink(p4);
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		try (Transaction tx = kb().beginTransaction()) {
			// Dummy revision.
			factory().createANode();
			tx.commit();
		}
		PreloadTest hP1 = inRev(p1, r1);
		PreloadTest hP2 = inRev(p2, r1);
		assertNull(inRevCacheOnly(p3, r1));
		assertNull(inRevCacheOnly(p4, r1));
		TLReference singletonLinkAttr = TestTypesFactory.getSingletonLinkPreloadTestAttr();
		assertInstanceof("Test for " + SingletonLinkStorage.class.getName(),
			singletonLinkAttr.getStorageImplementation(),
			SingletonLinkStorage.class);

		try (PreloadContext preloadCtx = new PreloadContext()) {
			MetaElementUtil.preloadAttribute(preloadCtx, set(hP1, hP2), singletonLinkAttr);
			PreloadTest hP3 = inRevCacheOnly(p3, r1);
			assertNotNull("No preload operation executed.", hP3);
			assertSame(hP3, hP1.getSingletonLink());
			PreloadTest hP4 = inRevCacheOnly(p4, r1);
			assertNotNull("No preload operation executed.", hP4);
			assertSame(hP4, hP2.getSingletonLink());
		}
	}

	/**
	 * Tests {@link SingletonLinkStorage#getReversePreload()}.
	 */
	public void testSingletonLinkReversePreload() {
		PreloadTest p1;
		PreloadTest p2;
		PreloadTest p3;
		PreloadTest p4;
		PreloadTest p5;
		PreloadTest p6;
		Revision r1;
		try (Transaction tx = kb().beginTransaction()) {
			p1 = factory().createPreloadTest();
			p2 = factory().createPreloadTest();
			p3 = factory().createPreloadTest();
			p4 = factory().createPreloadTest();
			p5 = factory().createPreloadTest();
			p6 = factory().createPreloadTest();
			p3.setSingletonLink(p1);
			p4.setSingletonLink(p1);
			p5.setSingletonLink(p2);
			p6.setSingletonLink(p2);
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		try (Transaction tx = kb().beginTransaction()) {
			// Dummy revision.
			factory().createANode();
			tx.commit();
		}
		PreloadTest hP1 = inRev(p1, r1);
		PreloadTest hP2 = inRev(p2, r1);
		assertNull(inRevCacheOnly(p3, r1));
		assertNull(inRevCacheOnly(p4, r1));
		assertNull(inRevCacheOnly(p5, r1));
		assertNull(inRevCacheOnly(p6, r1));
		TLReference singletonLinkAttr = TestTypesFactory.getSingletonLinkPreloadTestAttr();
		assertInstanceof("Test for " + SingletonLinkStorage.class.getName(),
			singletonLinkAttr.getStorageImplementation(),
			SingletonLinkStorage.class);

		try (PreloadContext preloadCtx = new PreloadContext()) {
			MetaElementUtil.reversePreloadAttribute(preloadCtx, set(hP1, hP2), singletonLinkAttr);
			PreloadTest hP3 = inRevCacheOnly(p3, r1);
			assertNotNull("No preload operation executed.", hP3);
			PreloadTest hP4 = inRevCacheOnly(p4, r1);
			assertNotNull("No preload operation executed.", hP4);
			PreloadTest hP5 = inRevCacheOnly(p5, r1);
			assertNotNull("No preload operation executed.", hP5);
			PreloadTest hP6 = inRevCacheOnly(p6, r1);
			assertNotNull("No preload operation executed.", hP6);
			assertEquals(set(hP5, hP6), hP2.tReferers(singletonLinkAttr));
			assertEquals(set(hP3, hP4), hP1.tReferers(singletonLinkAttr));
		}
	}

	private <T extends TLObject> T inRev(T item, Revision rev) {
		ObjectKey key = key(item, rev);
		return item.tKnowledgeBase().resolveObjectKey(key).getWrapper();
	}

	private <T extends TLObject> T inRevCacheOnly(T item, Revision rev) {
		ObjectKey key = key(item, rev);
		KnowledgeItem cachedItem = item.tKnowledgeBase().resolveCachedObjectKey(key);
		if (cachedItem == null) {
			return null;
		}
		return cachedItem.getWrapper();
	}

	private static ObjectKey key(TLObject object, Revision rev) {
		return KBUtils.createHistoricObjectKey(object.tId(), rev.getCommitNumber());
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private static TestTypesFactory factory() {
		return TestTypesFactory.getInstance();
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestReferencePreload}.
	 */
	public static Test suite() {
		Test t = new TestSuite(TestReferencePreload.class);
		t = ServiceTestSetup.createSetup(t, ModelService.Module.INSTANCE);
		t = KBSetup.getKBTest(t, KBSetup.DEFAULT_KB);
		return t;
	}

}
