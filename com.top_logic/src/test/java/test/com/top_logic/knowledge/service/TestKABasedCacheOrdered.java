/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.DBKnowledgeBaseTestSetup;
import test.com.top_logic.knowledge.service.db2.KnowledgeBaseTestScenarioImpl;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.AObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.KIReference;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.db2.OrderedLinkQuery;
import com.top_logic.model.TLObject;

/**
 * Test case for {@link OrderedLinkQuery}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestKABasedCacheOrdered extends AbstractDBKnowledgeBaseTest {

	private static final List<RObj> EMPTY = list();

	private BObjExtended b1;

	private RObj r1;

	private RObj r2;

	private RObj r3;

	private RObj r4;

	private RObj r5;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Transaction tx = kb().beginTransaction();
		b1 = BObjExtended.newBObjExtended("b1");

		r1 = RObj.newRObj("r1");
		r2 = RObj.newRObj("r2");
		r3 = RObj.newRObj("r3");
		r4 = RObj.newRObj("r4");
		r5 = RObj.newRObj("r5");

		tx.commit();
	}

	public void testAdd() throws KnowledgeBaseException {
		setXY(list(r1, r2), EMPTY);
		ChangeState s1 = state(r1, r2);

		setXY(list(r1, r2, r3, r4), EMPTY);
		s1.checkUnmodified();
		ChangeState s2 = state(r1, r2, r3, r4);

		setXY(list(r5, r1, r2, r3, r4), EMPTY);
		s2.checkUnmodified();
	}

	public void testRemove() throws KnowledgeBaseException {
		setXY(list(r1, r2, r3, r4, r5), EMPTY);
		ChangeState s1 = state(r1, r3, r4, r5);

		setXY(list(r1, r3, r4, r5), EMPTY);
		s1.checkUnmodified();
		ChangeState s2 = state(r1, r5);

		setXY(list(r1, r5), EMPTY);
		s2.checkUnmodified();
		ChangeState s3 = state(r5);

		setXY(list(r5), EMPTY);
		s3.checkUnmodified();

		setXY(EMPTY, EMPTY);
	}

	public void testAddAndMove() throws KnowledgeBaseException {
		setXY(list(r1, r2, r3), EMPTY);
		setXY(list(r4, r3, r1, r2), EMPTY);
		setXY(list(r1, r2, r4, r3, r5), EMPTY);
	}

	public void testReorder() throws KnowledgeBaseException {
		List<RObj> forwards = list(r1, r2, r3, r4);
		List<RObj> backwards = list(r4, r3, r2, r1);
		
		setXY(forwards, backwards);
		setXY(backwards, forwards);
	}

	private Revision setXY(List<RObj> xs, List<RObj> ys) throws KnowledgeBaseException {
		Transaction tx = kb().beginTransaction();
		try {
			b1.setX(xs);
			b1.setY(ys);

			assertEquals(xs, b1.getX());
			assertEquals(ys, b1.getY());
			tx.commit();
		} finally {
			tx.rollback();
		}

		assertEquals(xs, b1.getX());
		assertEquals(ys, b1.getY());

		return tx.getCommitRevision();
	}

	interface ChangeState {
		void checkUnmodified();
	}

	private ChangeState state(TLObject... objs) {
		final Map<TLObject, Revision> lastUpdates = new HashMap<>();
		for (TLObject obj : objs) {
			lastUpdates.put(obj, HistoryUtils.getLastUpdate(obj.tHandle()));
		}
		return new ChangeState() {
			@Override
			public void checkUnmodified() {
				for (Entry<TLObject, Revision> entry : lastUpdates.entrySet()) {
					assertEquals(entry.getValue(), HistoryUtils.getLastUpdate(entry.getKey().tHandle()));
				}
			}
		};
	}

	public static class BObjExtended extends BObj {

		private static final OrderedLinkQuery<RObj> XS = AssociationQuery.createOrderedLinkQuery("xs",
			RObj.class, RObj.OBJECT_NAME, RObj.X_REF_NAME, RObj.X_REF_ORDER_NAME);

		private static final OrderedLinkQuery<RObj> YS = AssociationQuery.createOrderedLinkQuery("ys",
			RObj.class, RObj.OBJECT_NAME, RObj.Y_REF_NAME, RObj.Y_REF_ORDER_NAME);

		public BObjExtended(KnowledgeObject ko) {
			super(ko);
		}

		public static BObjExtended newBObjExtended(String a1) {
			return (BObjExtended) newBObj(a1);
		}

		public void setX(List<RObj> values) {
			updateOrderedLinkSet(XS, values);
		}

		public List<RObj> getX() {
			return resolveLinks(XS);
		}

		public void setY(List<RObj> values) {
			updateOrderedLinkSet(YS, values);
		}

		public List<RObj> getY() {
			return resolveLinks(YS);
		}
	}

	public static class RObj extends AObj {

		/**
		 * Name of {@link #ORDERED}.
		 */
		static final String OBJECT_NAME = "RObj";

		static final String X_REF_NAME = "x";

		static final String X_REF_ORDER_NAME = "xOrder";

		static final String Y_REF_NAME = "y";

		static final String Y_REF_ORDER_NAME = "yOrder";

		public RObj(KnowledgeObject ko) {
			super(ko);
		}

		public static RObj newRObj(String a1) {
			{
				RObj result = PersistencyLayer.getKnowledgeBase().createObject(OBJECT_NAME, RObj.class);
				result.setA1(a1);
				return result;
			}
		}

		/**
		 * Object type with two foreign key attributes with corresponding ordering attributes.
		 */
		static final MOClass ORDERED = new MOKnowledgeItemImpl(RObj.OBJECT_NAME);
		static {
			ORDERED.setAbstract(false);
			ORDERED.setSuperclass(KnowledgeBaseTestScenarioImpl.A);
			try {
				ORDERED.addAttribute(KIReference.referenceById(RObj.X_REF_NAME, KnowledgeBaseTestScenarioImpl.B));
				ORDERED.addAttribute(new MOAttributeImpl(RObj.X_REF_ORDER_NAME, MOPrimitive.INTEGER, false));

				ORDERED.addAttribute(KIReference.referenceById(RObj.Y_REF_NAME, KnowledgeBaseTestScenarioImpl.B));
				ORDERED.addAttribute(new MOAttributeImpl(RObj.Y_REF_ORDER_NAME, MOPrimitive.INTEGER, false));

				KnowledgeBaseTestScenarioImpl.setApplicationType(ORDERED, RObj.class);
			} catch (DuplicateAttributeException e) {
				throw new UnreachableAssertion(e);
			}
		}

	}


	@Override
	protected LocalTestSetup createSetup(Test self) {
		DBKnowledgeBaseTestSetup setup = new DBKnowledgeBaseTestSetup(self);
		setup.addAdditionalTypes(KnowledgeBaseTestScenarioImpl.newCopyProvider(RObj.ORDERED));
		return setup;
	}

	public static Test suite() {
		return suite(TestKABasedCacheOrdered.class);
	}

}
