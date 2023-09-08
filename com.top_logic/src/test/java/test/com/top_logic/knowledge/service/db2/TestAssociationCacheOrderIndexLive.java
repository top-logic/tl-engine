/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.DObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.EObj;

import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.db2.OrderedLinkQuery;
import com.top_logic.model.TLObject;

/**
 * @since 5.8.0
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestAssociationCacheOrderIndexLive extends TestAssociationCacheOrdered {

	private static final OrderedLinkQuery<EObj> ORDER_BY_INDEX_LIVE = AssociationQuery.createOrderedLinkQuery("ordered",
		EObj.class, E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME, E1_NAME, null, true, true);

	@Override
	protected void updateIndices(List<EObj> list, String attributeName) {
		for (int index = 0, size = list.size(); index < size; index++) {
			list.get(index).setValue(attributeName, Integer.valueOf(index));
		}
	}

	@Override
	protected OrderedLinkQuery<EObj> query() {
		return ORDER_BY_INDEX_LIVE;
	}

	public void testCorrectOrder() {
		DObj d1 = DObj.newDObj("b1");

		EObj e1 = EObj.newEObj("e1");
		EObj e2 = EObj.newEObj("e2");
		EObj e3 = EObj.newEObj("e3");
		EObj e4 = EObj.newEObj("e4");

		List<EObj> list = list(e2, e4, e1, e3);
		updateIndices(list, orderAttribute());
		List<EObj> list2 = getList(d1);
		assertEquals(list(), list2);
		e3.setReference(REFERENCE_POLY_CUR_LOCAL_NAME, d1);
		assertEquals(list(e3), list2);
		e1.setReference(REFERENCE_POLY_CUR_LOCAL_NAME, d1);
		assertEquals(list(e1, e3), list2);
		e2.setReference(REFERENCE_POLY_CUR_LOCAL_NAME, d1);
		assertEquals(list(e2, e1, e3), list2);
		e4.setReference(REFERENCE_POLY_CUR_LOCAL_NAME, d1);
		assertEquals(list(e2, e4, e1, e3), list2);
	}

	private static String orderAttribute() {
		return E1_NAME;
	}

	public void testOrderByLiveUpdate() {
		DObj d1 = DObj.newDObj("b1");

		EObj e1 = EObj.newEObj("e1");
		EObj e2 = EObj.newEObj("e2");
		EObj e3 = EObj.newEObj("e3");
		EObj e4 = EObj.newEObj("e4");

		List<EObj> list = getList(d1);
		list.add(e2);
		assertEqualsAndOrder(list(e2), list);
		list.add(e4);
		assertEqualsAndOrder(list(e2, e4), list);
		list.add(1, e1);
		assertEqualsAndOrder(list(e2, e1, e4), list);
		list.set(1, e3);
		assertEqualsAndOrder(list(e2, e3, e4), list);
		list.add(0, e1);
		assertEqualsAndOrder(list(e1, e2, e3, e4), list);
		list.removeAll(list(e2, e4));
		assertEqualsAndOrder(list(e1, e3), list);
		list.addAll(list(e2, e4));
		assertEqualsAndOrder(list(e1, e3, e2, e4), list);
		list.retainAll(list(e2, e4));
		assertEqualsAndOrder(list(e2, e4), list);
		list.addAll(1, list(e3, e1));
		assertEqualsAndOrder(list(e2, e3, e1, e4), list);
		list.clear();
		assertEqualsAndOrder(BasicTestCase.<EObj> list(), list);
	}

	private void assertEqualsAndOrder(List<EObj> expected, List<EObj> actual) {
		assertEquals(expected, actual);
		assertOrders(actual);
	}

	private void assertOrders(List<? extends TLObject> list) {
		for (int index = 0, size = list.size(); index < size; index++) {
			assertOrder(list.get(index), index);
		}
	}

	private void assertOrder(TLObject o, int order) {
		try {
			assertSame(((Number) o.tHandle().getAttributeValue(orderAttribute())).intValue(), order);
		} catch (NoSuchAttributeException ex) {
			throw fail("No attribute " + orderAttribute() + " in " + o, ex);
		}
	}

	public static Test suite() {
		return suite(TestAssociationCacheOrderIndexLive.class);
	}

}

