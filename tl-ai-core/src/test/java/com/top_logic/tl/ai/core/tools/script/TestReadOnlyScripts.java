/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.script;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.util.model.ModelService;

/**
 * Tests what {@code query} and {@code present} accept as a read.
 */
public class TestReadOnlyScripts extends BasicTestCase {

	/**
	 * An effect inside a function's argument is an effect. The engine's own check looks at the
	 * outermost call only, and let both of these run as reads.
	 */
	public void testAnEffectInsideAFunctionIsNoRead() {
		assertNotReadOnly("toString(log('from a read'))");
		assertNotReadOnly("all(`tl.ai.core:Agent`).foreach(a -> $a.delete()).firstElement()");
		assertNotReadOnly("toString(new(`tl.ai.core:Agent`))");
	}

	/** A calendar conversion and a {@code throw} change nothing, and a date question needs them. */
	public void testWhatChangesNothingStaysARead() {
		TLScriptExecutor.compileReadOnly("today().toUserCalendar().withDayAdded(30).toDate()");
		TLScriptExecutor.compileReadOnly("ifElse(1 > 2, throw('never'), 1)");
	}

	private static void assertNotReadOnly(String script) {
		try {
			TLScriptExecutor.compileReadOnly(script);
			fail("Expected '" + script + "' to be refused as a read.");
		} catch (TLScriptExecutor.NotReadOnly ex) {
			// Expected.
		}
	}

	/** The model and the knowledge base the scripts compile against. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestReadOnlyScripts.class), ModelService.Module.INSTANCE));
	}
}
