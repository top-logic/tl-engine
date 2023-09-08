/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.v5;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseClusterTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLObject;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.v5.AssociationNavigationPreload;

/**
 * Test case for {@link AssociationNavigationPreload}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestAssociationNavigationPreload extends AbstractDBKnowledgeBaseClusterTest {

	public void testLargePreload() throws DataObjectException, RefetchTimeout {
		// Setup large scenario in node 2: source --> b0...b1000
		KnowledgeObject source;

		Transaction tx = kbNode2().beginTransaction();
		try {
			source = newBNode2("source");

			for (int n = 0; n < 1001; n++) {
				KnowledgeObject target = newBNode2("b" + n);
				newAB(source, target);
			}

			tx.commit();
		} finally {
			tx.rollback();
		}

		// Preload in default node.

		// Refetch node 1 to be able to resolve item
		kb().refetch();
		KnowledgeObject source2 = (KnowledgeObject) node1Item(source);

		AssociationSetQuery<KnowledgeAssociation> query = AssociationQuery.createOutgoingQuery("ab", AB_NAME);
		AssociationNavigationPreload preload = new AssociationNavigationPreload(query);

		PreloadContext context = new PreloadContext();
		final Wrapper source2Wrapper = WrapperFactory.getWrapper(source2);
		preload.prepare(context, Arrays.asList(source2Wrapper));

		// Check that targets have been resolved.
		Set<KnowledgeAssociation> links = AbstractWrapper.resolveLinks(source2Wrapper, query);
		for (KnowledgeAssociation ka : links) {
			assertNotNull("No preload of destination.", kb().resolveCachedObjectKey(ka.getDestinationIdentity()));
		}
	}

	public void testHeterogeniousPreload() throws RefetchTimeout {
		// Create objects
		int cnt = 30;
		BObj[] sources = new BObj[cnt];
		BObj[] targets = new BObj[cnt];
		{
			Transaction tx = kbNode2().beginTransaction();
			try {
				for (int n = 0; n < cnt; n++) {
					sources[n] = BObj.newBObj(kbNode2(), "bs" + n);
					targets[n] = BObj.newBObj(kbNode2(), "bt" + n);
				}
				tx.commit();
			} finally {
				tx.rollback();
			}
		}

		// Make link changes.
		Random rnd = new Random(42);
		int revs = 20;
		int changesPerRev = cnt / 3;
		List<Revision> revisions = new ArrayList<>();
		for (int r = 0; r < revs; r++) {
			Transaction tx = kbNode2().beginTransaction();
			try {
				for (int n = 0; n < changesPerRev; n++) {
					BObj source = sources[rnd.nextInt(cnt)];
					BObj target = targets[rnd.nextInt(cnt)];

					if (source == target) {
						continue;
					}

					if (source.getAB().contains(target)) {
						source.removeAB(target);
					} else {
						source.addAB(target);
					}
				}
				tx.commit();

				revisions.add(tx.getCommitRevision());
			} finally {
				tx.rollback();
			}
		}

		// Refetch node 1 to be able to resolve objects.
		kb().refetch();
		BObj[] sourcesNode1 = new BObj[cnt];
		for (int n = 0; n < cnt; n++) {
			sourcesNode1[n] = node1Obj(sources[n]);
		}

		ArrayList<BObj> preloadObjects = new ArrayList<>();
		for (Revision revision : revisions) {
			for (int n = 0; n < cnt; n++) {
				preloadObjects.add((BObj) WrapperHistoryUtils.getWrapper(revision, sourcesNode1[n]));
			}
		}

		AssociationSetQuery<KnowledgeAssociation> query = AssociationQuery.createOutgoingQuery("ab", AB_NAME);
		AssociationNavigationPreload preload = new AssociationNavigationPreload(query);

		PreloadContext context = new PreloadContext();
		preload.prepare(context, preloadObjects);

		// Check that targets have been resolved.
		for (BObj source : preloadObjects) {
			Set<KnowledgeAssociation> links = AbstractWrapper.resolveLinks(source, query);

			// Resolve corresponding object in node 2 for test.
			BObj source2 = node2Obj(source);
			Set<?> ab2 = source2.getAB();

			for (KnowledgeAssociation ka : links) {
				ObjectKey targetId = ka.getDestinationIdentity();
				KnowledgeItem targetItem = kb().resolveCachedObjectKey(targetId);

				assertNotNull("No preload of destination.", targetItem);

				// Test that corresponding destination is found.
				TLObject target2 = node2Item(targetItem).getWrapper();
				assertTrue(ab2.contains(target2));
			}

			// Test that number of links match in reference scenario.
			assertEquals(ab2.size(), links.size());
		}
	}

	public static Test suite() {
		return suite(TestAssociationNavigationPreload.class);
	}

}
