/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.service.KBTestMeta;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.WrapperValueAnalyzer;
import com.top_logic.knowledge.wrap.WrapperValueFactory;

/**
 * Test for {@link WrapperValueAnalyzer} and {@link WrapperValueFactory}.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestWrapperValueAnalyzer extends BasicTestCase {

	public void testSerialisationDeserialisation() throws DataObjectException, ParseException {
		KnowledgeObject ko = PersistencyLayer.getKnowledgeBase().createKnowledgeObject(KBTestMeta.TEST_B);
		assertEqualityThroughJSON(WrapperFactory.getWrapper(ko));
	}

	public void testSerialisationDeserialisationHistoricItem() throws DataObjectException, ParseException {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		if (!kb.getHistoryManager().hasHistory()) {
			/* Test with historic items is useless if no history is suported. */
			return;
		}
		Transaction tx1 = kb.beginTransaction();
		KnowledgeObject ko = kb.createKnowledgeObject(KBTestMeta.TEST_B);
		tx1.commit();
		Transaction tx2 = kb.beginTransaction();
		ko.setAttributeValue(KBTestMeta.TEST_B_NAME, "name");
		tx2.commit();
		HistoryManager hm = kb.getHistoryManager();
		Branch newBranch = hm.createBranch(hm.getTrunk(), hm.getRevision(hm.getLastRevision()),
			Collections.singleton(KBTestMeta.getMetaObject(KBTestMeta.TEST_B)));
		KnowledgeObject koOnBranch = (KnowledgeObject) HistoryUtils.getKnowledgeItem(newBranch, ko);
		Transaction tx3 = kb.beginTransaction();
		koOnBranch.setAttributeValue(KBTestMeta.TEST_B_NAME, "nameOnBranch");
		tx3.commit();

		assertEqualityThroughJSON(WrapperFactory.getWrapper(ko));
		assertEqualityThroughJSON(WrapperFactory.getWrapper(koOnBranch));
		assertEqualityThroughJSON(WrapperFactory.getWrapper((KnowledgeObject) HistoryUtils.getKnowledgeItem(
			tx1.getCommitRevision(), ko)));
		assertEqualityThroughJSON(WrapperFactory.getWrapper((KnowledgeObject) HistoryUtils.getKnowledgeItem(
			tx3.getCommitRevision(), koOnBranch)));
	}

	private static Object assertEqualityThroughJSON(Object obj) throws ParseException {
		String serialized = JSON.toString(WrapperValueAnalyzer.WRAPPER_INSTANCE, obj);
		Object deserialized = JSON.fromString(serialized, WrapperValueFactory.WRAPPER_INSTANCE);
		assertEquals(obj, deserialized);
		return deserialized;
	}

	public void testWrapperContent() throws DataObjectException, ParseException {
		List<Wrapper> wrapperList = new ArrayList<>();
		Map<String, Wrapper> wrapperMap = new HashMap<>();
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx1 = kb.beginTransaction();
		for (int i = 0; i < 10; i++) {
			Wrapper wrapper = WrapperFactory.getWrapper(kb.createKnowledgeObject(KBTestMeta.TEST_B));
			wrapperList.add(wrapper);
			wrapperMap.put(String.valueOf(i), wrapper);
		}
		tx1.commit();

		assertEqualityThroughJSON(wrapperList);
		assertEqualityThroughJSON(wrapperMap);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestWrapperValueAnalyzer}.
	 */
	public static Test suite() {
		return KBSetup.getKBTest(TestWrapperValueAnalyzer.class);
	}
}

