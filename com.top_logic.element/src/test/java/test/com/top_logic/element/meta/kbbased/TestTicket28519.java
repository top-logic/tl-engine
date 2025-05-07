/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.meta.model.ticket28519.A;
import test.com.top_logic.element.meta.model.ticket28519.B;
import test.com.top_logic.element.meta.model.ticket28519.Ticket28519Factory;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLObject;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.util.model.ModelService;

/**
 * Test of Ticket 28519.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestTicket28519 extends BasicTestCase {

	public void testPreloadAttribute(){
		Ticket28519Factory factory = Ticket28519Factory.getInstance();
		A a1, a2, a3, a4;
		B b1, b2, b3, b4;
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			a1 = factory.createA();
			a2 = factory.createA();
			a3 = factory.createA();
			a4 = factory.createA();

			// Create
			b1 = factory.createB1();
			b2 = factory.createB2();
			b3 = factory.createB1();
			b4 = factory.createB2();

			a1.addB(b1);
			a2.addB(b2);
			a3.addB(b3);
			a4.addB(b4);
			tx.commit();
		}
		
		clearAssociationCaches(a1, a2, a3, a4);

		PreloadContext context = new PreloadContext();
		MetaElementUtil.preloadAttribute(context, set(a1, a2, a3, a4), Ticket28519Factory.getBsAAttr());

		assertEquals(set(b1), a1.getBs());
		assertEquals(set(b2), a2.getBs());
		assertEquals(set(b3), a3.getBs());
		assertEquals(set(b4), a4.getBs());
	}

	private void clearAssociationCaches(TLObject... objects) {
		ArrayUtil.forEach(objects, object -> {
			KnowledgeItem item = object.tHandle();
			Map<?, ?> caches = ReflectionUtils.getValue(item, "associationCaches", Map.class);
			if (caches != null) {
				caches.clear();
			}
		});

	}

	static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestTicket28519}.
	 */
	public static Test suite() {
		Test t = new TestSuite(TestTicket28519.class);
		t = ServiceTestSetup.createSetup(t, ModelService.Module.INSTANCE);
		t = KBSetup.getKBTest(t, KBSetup.DEFAULT_KB);
		return t;
	}
}
