/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.script;

import static com.top_logic.tl.ai.index.config.IndexTestModel.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.util.model.ModelService;

/**
 * The options of {@code aiIndexSearch} become the request the index understands.
 */
public class TestAiIndexScriptFunctions extends BasicTestCase {

	/** An enumeration value filters by its name, as the index stores a keyword. */
	public void testClassifierFiltersByItsName() {
		Map<String, Object> filters = Map.of("kind", List.of(kind("RISK"), "REQUIREMENT"));

		assertEquals(Map.of("kind", Set.of("RISK", "REQUIREMENT")),
			AiIndexScriptFunctions.request("pump", Map.of("filters", filters)).filters());
	}

	/** The knowledge base with the test model. */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(new TestSuite(TestAiIndexScriptFunctions.class), ModelService.Module.INSTANCE));
	}

}
