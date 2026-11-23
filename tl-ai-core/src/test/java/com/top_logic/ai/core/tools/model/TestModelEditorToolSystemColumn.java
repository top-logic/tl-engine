/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.tools.model;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Test that {@link ModelEditorTool#createProperty} refuses a property with the name of a system
 * column of each object.
 */
@SuppressWarnings("javadoc")
public class TestModelEditorToolSystemColumn extends BasicTestCase {

	private static final String MODULE = TestModelEditorToolSystemColumn.class.getName();

	public void testSystemColumnNameRefused() {
		TLModule module;
		TLClass type;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			module = TLModelUtil.addModule(ModelService.getApplicationModel(), MODULE);
			type = TLModelUtil.addClass(module, "Holder");
			tx.commit();
		}
		try {
			String json = new ModelEditorTool().createProperty(MODULE, "Holder", "created", "String",
				null, null, null, null, null, null, null, null, null);

			assertTrue(json, json.contains("\"error\":"));
			assertTrue(json, json.contains("system column"));
			assertNull(type.getPart("created"));
		} finally {
			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				module.tDelete();
				tx.commit();
			}
		}
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestModelEditorToolSystemColumn.class),
				ModelService.Module.INSTANCE));
	}

}
