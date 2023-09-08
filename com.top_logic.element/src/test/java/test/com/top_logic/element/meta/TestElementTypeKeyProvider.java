/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ThreadContextDecorator;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.meta.benchmark.model.BenchmarkFactory;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.TypeKeyProvider.Key;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.provider.keys.TLTypeKeyProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientModelFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Test case for {@link TLTypeKeyProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestElementTypeKeyProvider extends BasicTestCase {

	public TestElementTypeKeyProvider(String name) {
		super(name);
	}

	public void testTransientObject() {
		TLModel model = new TLModelImpl();
		TLModule module = TLModelUtil.makeModule(model, "benchmark");
		TLClass type = TLModelUtil.makeClass(module, "A");

		TLObject a1 = TransientModelFactory.createTransientObject(type);
		TLObject a2 = TransientModelFactory.createTransientObject(type);

		TLTypeKeyProvider provider = new TLTypeKeyProvider();
		Key key1 = provider.lookupTypeKey(a1);
		assertNotNull(key1);

		Key key2 = provider.lookupTypeKey(a2);
		assertSame("Same types produce same keys.", key1, key2);
	}

	public void testInvalidObject() {
		TLTypeKeyProvider provider = new TLTypeKeyProvider();
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		TLObject a1;
		try (Transaction tx = kb.beginTransaction()) {
			a1 = BenchmarkFactory.getInstance().createA();
			tx.commit();
		}
		Key key1 = provider.lookupTypeKey(a1);

		try (Transaction tx = kb.beginTransaction()) {
			a1.tDelete();
			tx.commit();
		}
		Key key2 = provider.lookupTypeKey(a1);
		
		assertNotNull("Even invalid objects produce non-null keys.", key2);
		assertNotEquals("Invalid object have other keys than valid objects.", key1, key2);
	}

    public static Test suite () {
		TestSuite theSuite = new TestSuite(TestElementTypeKeyProvider.class);
        
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(ThreadContextDecorator.INSTANCE, theSuite,
				ModelService.Module.INSTANCE));
    }
	
}
