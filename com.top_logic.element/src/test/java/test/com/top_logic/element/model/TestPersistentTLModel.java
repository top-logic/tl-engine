/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.element.model.PersistentTLModel;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.util.model.ModelService;

/**
 * Test of the module lookup of {@link PersistentTLModel} through {@link TLModel#getModule(String)}
 * and {@link TLModel#getModules()}.
 */
@SuppressWarnings("javadoc")
public class TestPersistentTLModel extends BasicTestCase {

	private static final String MODULE_NAME = "test.TestPersistentTLModel";

	private static final String RENAMED_MODULE_NAME = "test.TestPersistentTLModel.renamed";

	private Revision _preSetUpRevision;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_preSetUpRevision = HistoryUtils.getSessionRevision();
	}

	@Override
	protected void tearDown() throws Exception {
		KBUtils.revert(kb(), _preSetUpRevision);
		super.tearDown();
	}

	public void testLookupExisting() {
		TLModule module = model().getModule(TlModelFactory.TL_MODEL_STRUCTURE);
		assertNotNull(module);
		assertEquals(TlModelFactory.TL_MODEL_STRUCTURE, module.getName());
		assertTrue(model().getModules().contains(module));
	}

	public void testLookupMissing() {
		assertNull(model().getModule(MODULE_NAME));
	}

	public void testCreate() {
		TLModule module;
		try (Transaction tx = begin()) {
			module = model().addModule(model(), MODULE_NAME);
			assertSame("Created module found before commit.", module, model().getModule(MODULE_NAME));
			assertTrue(model().getModules().contains(module));
			tx.commit();
		}
		assertSame("Created module found after commit.", module, model().getModule(MODULE_NAME));
		assertTrue(model().getModules().contains(module));
	}

	public void testDelete() {
		TLModule module = createModule(MODULE_NAME);
		try (Transaction tx = begin()) {
			module.tDelete();
			assertNull("Deleted module not found before commit.", model().getModule(MODULE_NAME));
			assertFalse(model().getModules().contains(module));
			tx.commit();
		}
		assertNull("Deleted module not found after commit.", model().getModule(MODULE_NAME));
		assertFalse(model().getModules().contains(module));
	}

	public void testRename() {
		TLModule module = createModule(MODULE_NAME);
		try (Transaction tx = begin()) {
			module.setName(RENAMED_MODULE_NAME);
			assertSame("Renamed module found under new name before commit.", module,
				model().getModule(RENAMED_MODULE_NAME));
			assertNull("Renamed module not found under old name before commit.", model().getModule(MODULE_NAME));
			assertTrue(model().getModules().contains(module));
			tx.commit();
		}
		assertSame("Renamed module found under new name after commit.", module,
			model().getModule(RENAMED_MODULE_NAME));
		assertNull("Renamed module not found under old name after commit.", model().getModule(MODULE_NAME));
		assertTrue(model().getModules().contains(module));
	}

	private TLModule createModule(String name) {
		try (Transaction tx = begin()) {
			TLModule module = model().addModule(model(), name);
			tx.commit();
			return module;
		}
	}

	private static Transaction begin() {
		return kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
	}

	private static TLModel model() {
		return ModelService.getApplicationModel();
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	public static Test suite() {
		Test modelTest = ServiceTestSetup.createSetup(TestPersistentTLModel.class, ModelService.Module.INSTANCE);
		Test kbTest = KBSetup.getSingleKBTest(modelTest);
		return TestUtils.doNotMerge(kbTest);
	}

}
