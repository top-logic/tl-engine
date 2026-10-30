/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.admin.system;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.view.admin.system.DatabaseState;
import com.top_logic.model.TLModule;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Test of {@link DatabaseState#of(KnowledgeBase)}.
 */
@SuppressWarnings("javadoc")
public class TestDatabaseState extends BasicTestCase {

	public void testProduct() {
		DatabaseState state = DatabaseState.of(kb());

		// The test knowledge base is stored in a database.
		assertNotNull(state.product());
		assertNotNull(state.productVersion());
		assertNotNull(state.driver());
		assertNotNull(state.sqlDialect());
	}

	public void testRevisionRange() {
		DatabaseState state = DatabaseState.of(kb());

		assertTrue(state.firstRevision() >= Revision.FIRST_REV);
		assertTrue(state.lastRevision() >= state.firstRevision());
		assertNotNull(state.firstRevisionDate());
		assertNotNull(state.lastRevisionDate());
		assertFalse(state.lastRevisionDate().before(state.firstRevisionDate()));
	}

	public void testCommitMovesCurrentRevision() {
		DatabaseState before = DatabaseState.of(kb());

		TLModule module;
		try (Transaction tx = kb().beginTransaction()) {
			module = TLModelUtil.addModule(ModelService.getApplicationModel(), "TestDatabaseState");
			tx.commit();
		}
		try {
			DatabaseState after = DatabaseState.of(kb());
			assertTrue(after.lastRevision() > before.lastRevision());
			assertEquals(before.firstRevision(), after.firstRevision());
			assertEquals(before.firstRevisionDate(), after.firstRevisionDate());
		} finally {
			try (Transaction tx = kb().beginTransaction()) {
				TLModelUtil.deleteRecursive(module);
				tx.commit();
			}
		}
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestDatabaseState.class,
			ServiceTestSetup.createStarterFactoryForModules(TypeIndex.Module.INSTANCE, ModelService.Module.INSTANCE));
	}
}
