/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ThreadContextSetup;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.TLObject;
import com.top_logic.model.internal.PersistentModelPart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Test class for {@link PersistentModelPart}s.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public abstract class TestWithModelExtension extends BasicTestCase {

	protected static class ModelExtensionTestSetup extends ThreadContextSetup {
		
		private final Class<?> _testClass;
	
		private final String _suffix;
	
		private final String _moduleName;
	
		public ModelExtensionTestSetup(Class<? extends Test> testClass) {
			this(new TestSuite(testClass), testClass);
		}
	
		public ModelExtensionTestSetup(Test test, Class<? extends Test> testClass) {
			this(test, testClass, "model.xml", testClass.getName());
		}
	
		public ModelExtensionTestSetup(Test test, Class<?> testClass, String suffix, String moduleName) {
			super(test);
			_testClass = testClass;
			_suffix = suffix;
			_moduleName = moduleName;
		}
	
		@Override
		protected void doSetUp() throws Exception {
			extendApplicationModel(PersistencyLayer.getKnowledgeBase(), _testClass, _suffix);
		}
	
		@Override
		protected void doTearDown() throws Exception {
			Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction();
			TLModule module = ModelService.getApplicationModel().getModule(_moduleName);
			for (TLModuleSingleton singleton : module.getSingletons()) {
				// Must delete instances of classes before the TLClass can be deleted.
				singleton.getSingleton().tDelete();
			}
			module.tDelete();
			tx.commit();
		}
	}

	protected KnowledgeBase _kb;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
	}

	protected TLModule addModule(String name) {
		Transaction tx = _kb.beginTransaction();
		TLModule module = TLModelUtil.addModule(ModelService.getApplicationModel(), name);
		tx.commit();
		return module;
	}

	protected TLClass addClass(TLModule module, String name) {
		Transaction tx = _kb.beginTransaction();
		TLClass clazz = TLModelUtil.addClass(module, name);
		tx.commit();
		return clazz;
	}

	protected void delete(TLObject... elements) {
		Transaction tx = _kb.beginTransaction();
		ArrayUtil.forEach(elements,TLObject::tDelete);
		tx.commit();
	}

	protected void extendApplicationModel(Class<?> testClass, String suffix) {
		KnowledgeBase kb = _kb;
		extendApplicationModel(kb, testClass, suffix);
	}

	protected static void extendApplicationModel(KnowledgeBase kb, Class<?> testClass, String suffix) {
		Protocol log = new AssertProtocol();
		TLModel model = DynamicModelService.getApplicationModel();
		BinaryContent modelXml = ClassRelativeBinaryContent.withSuffix(testClass, suffix);
	
		try (Transaction tx = kb.beginTransaction()) {
			DynamicModelService.extendModel(log, model, ModelService.getInstance().getFactory(), modelXml);
			tx.commit();
		}
	}

	protected static Test suite(Class<? extends TestWithModelExtension> testClass) {
		return suite(new TestSuite(testClass));
	}

	protected static Test suite(Test test) {
		return ElementWebTestSetup.createElementWebTestSetup(test);
	}

}
